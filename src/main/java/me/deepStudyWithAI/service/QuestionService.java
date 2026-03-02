package me.deepStudyWithAI.service;

import lombok.RequiredArgsConstructor;
import me.deepStudyWithAI.domain.Answer;
import me.deepStudyWithAI.domain.Question;
import me.deepStudyWithAI.domain.Topic;
import me.deepStudyWithAI.dto.OpenAiChatRequest;
import me.deepStudyWithAI.repository.AnswerRepository;
import me.deepStudyWithAI.repository.QuestionRepository;
import me.deepStudyWithAI.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final OpenAiService openAiService;

    // 모든 토픽 조회
    public List<Topic> getAllTopics() {
        return topicRepository.findAll();
    }

    // 초기 질문 생성 및 AI 답변 받기
    @Transactional
    public Mono<Question> createQuestionAndGetAnswer(String content, Long topicId) {
        return createQuestionAndGetAnswer(content, topicId, null);
    }

    // 추가 질문 생성 및 AI 답변 받기 (토픽의 모든 질문/답변 컨텍스트 포함)
    @Transactional
    public Mono<Question> createQuestionAndGetAnswer(String content, Long topicId, Long parentQuestionId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid topic ID: " + topicId));

        Question question = new Question(content, topic);
        questionRepository.save(question);

        // Fetch all previous questions and answers for the topic to build history
        List<Question> history = questionRepository.findByTopicOrderByCreatedAtAsc(topic);
        List<OpenAiChatRequest.Message> messages = buildMessageHistory(history, content);

        Mono<String> aiResponseMono = openAiService.getChatResponseWithMessages(messages);

        // AI 답변 요청 및 저장
        return aiResponseMono
                .flatMap(aiResponseContent -> {
                    Answer answer = new Answer(aiResponseContent, question);
                    answerRepository.save(answer);
                    return Mono.just(question);
                });
    }

    private List<OpenAiChatRequest.Message> buildMessageHistory(List<Question> history, String currentContent) {
        List<OpenAiChatRequest.Message> messages = new ArrayList<>();
        
        // System message for setting the persona
        messages.add(OpenAiChatRequest.Message.builder()
                .role("system")
                .content("You are a helpful AI assistant focused on deep study. " +
                        "Your goal is to provide detailed, comprehensive explanations to help the user understand the topic deeply. " +
                        "Please format your responses using Markdown (headings, lists, bold text, etc.) for better readability. " +
                        "All responses must be in Korean.")
                .build());

        for (Question q : history) {
            // Only add previous questions that already have answers (excluding the current one)
            if (!q.getContent().equals(currentContent) && q.getAnswer() != null) {
                messages.add(OpenAiChatRequest.Message.builder()
                        .role("user")
                        .content(q.getContent())
                        .build());
                messages.add(OpenAiChatRequest.Message.builder()
                        .role("assistant")
                        .content(q.getAnswer().getAiContent())
                        .build());
            }
        }

        // Add the current question as the last user message
        messages.add(OpenAiChatRequest.Message.builder()
                .role("user")
                .content(currentContent)
                .build());

        return messages;
    }


    // 질문 상세 조회 (답변 포함)
    public Question getQuestionWithAnswer(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid question ID: " + questionId));
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        questionRepository.deleteById(questionId);
    }

    // 요약 저장
    @Transactional
    public void saveSummary(Long answerId, String userSummary) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid answer ID: " + answerId));
        answer.updateSummary(userSummary);
        answerRepository.save(answer);
    }

    // 요약 검증 및 저장
    @Transactional
    public Mono<Void> validateAndSaveSummary(Long answerId, String userSummary) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid answer ID: " + answerId));
        Question question = answer.getQuestion();

        List<OpenAiChatRequest.Message> messages = new ArrayList<>();
        messages.add(OpenAiChatRequest.Message.builder()
                .role("system")
                .content("You are an expert educator. Your task is to evaluate a student's summary based on the original question and the AI's answer. " +
                        "Identify if they understood correctly, highlight any misconceptions, and provide detailed, encouraging, constructive feedback. " +
                        "Please use Markdown formatting for clarity and provide a comprehensive evaluation in Korean.")
                .build());

        String prompt = String.format(
                "### Original Question: %s\n\n" +
                "### AI's Original Answer: %s\n\n" +
                "### Student's Summary: %s\n\n" +
                "Please evaluate the student's summary in detail and provide feedback using Markdown.",
                question.getContent(), answer.getAiContent(), userSummary
        );

        messages.add(OpenAiChatRequest.Message.builder()
                .role("user")
                .content(prompt)
                .build());

        return openAiService.getChatResponseWithMessages(messages)
                .flatMap(feedback -> {
                    answer.updateSummary(userSummary);
                    answer.updateFeedback(feedback);
                    answerRepository.save(answer);
                    return Mono.empty();
                });
    }
}
