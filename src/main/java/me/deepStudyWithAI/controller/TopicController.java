package me.deepStudyWithAI.controller;

import lombok.RequiredArgsConstructor;
import me.deepStudyWithAI.domain.Question;
import me.deepStudyWithAI.domain.Topic;
import me.deepStudyWithAI.service.QuestionService;
import me.deepStudyWithAI.service.TopicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/topics")
public class TopicController {

    private final TopicService topicService;
    private final QuestionService questionService;

    @GetMapping("/new")
    public String picForm(Model model) {
        model.addAttribute("topics", questionService.getAllTopics()); // 기존 토픽 목록 추가
        return "topic-form"; // topic-form.html 템플릿 반환
    }

    @PostMapping
    public String createTopic(@RequestParam String title, RedirectAttributes redirectAttributes) {
        try {
            Topic topic = topicService.createTopic(title);
            redirectAttributes.addFlashAttribute("message", "토픽 '" + title + "'이(가) 성공적으로 생성되었습니다.");
            return "redirect:/topics/" + topic.getId(); // 상세 화면으로 리다이렉트
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/topics/new"; // 에러 발생 시 토픽 생성 폼으로 돌아감
        }
    }

    @GetMapping("/{id}")
    public String getTopicDetail(@PathVariable Long id, Model model) {
        Topic topic = topicService.findTopicById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid topic Id:" + id));
        List<Question> questions = topicService.getQuestionsByTopic(topic);

        model.addAttribute("topic", topic);
        model.addAttribute("questions", questions);
        return "topic-detail";
    }

    @PostMapping("/{id}/questions")
    public String addQuestion(
            @PathVariable("id") Long topicId,
            @RequestParam String content,
            RedirectAttributes redirectAttributes
    ) {
        Question createdQuestion = questionService.createQuestionAndGetAnswer(content, topicId).block();

        if (createdQuestion != null) {
            redirectAttributes.addFlashAttribute("message", "질문이 등록되었습니다.");
        } else {
            redirectAttributes.addFlashAttribute("error", "질문 등록에 실패했습니다.");
        }
        return "redirect:/topics/" + topicId;
    }

    @PostMapping("/{id}/questions/{questionId}/summary")
    public String saveSummary(
            @PathVariable("id") Long topicId,
            @PathVariable("questionId") Long questionId,
            @RequestParam Long answerId,
            @RequestParam String userSummary,
            RedirectAttributes redirectAttributes
    ) {
        questionService.saveSummary(answerId, userSummary);
        redirectAttributes.addFlashAttribute("message", "요약이 저장되었습니다.");
        return "redirect:/topics/" + topicId;
    }
}
