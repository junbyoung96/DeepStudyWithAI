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
                .content("You are a helpful AI assistant focused on deep study. Maintain the context of the conversation.")
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

    // 요약 저장
    @Transactional
    public void saveSummary(Long answerId, String userSummary) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid answer ID: " + answerId));
        answer.updateSummary(userSummary);
        answerRepository.save(answer);
    }
}
