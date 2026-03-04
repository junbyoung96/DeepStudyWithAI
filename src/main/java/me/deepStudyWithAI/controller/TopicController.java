package me.deepStudyWithAI.controller;

import lombok.RequiredArgsConstructor;
import me.deepStudyWithAI.domain.Member;
import me.deepStudyWithAI.domain.Question;
import me.deepStudyWithAI.domain.Topic;
import me.deepStudyWithAI.service.QuestionService;
import me.deepStudyWithAI.service.TopicService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping
    public String listTopics(@AuthenticationPrincipal Member member, Model model) {
        model.addAttribute("topics", topicService.getTopicsByMember(member));
        return "topic-list";
    }

    @GetMapping("/new")
    public String topicForm(@AuthenticationPrincipal Member member, Model model) {
        model.addAttribute("topics", topicService.getTopicsByMember(member));
        return "topic-form";
    }

    @PostMapping
    public String createTopic(@AuthenticationPrincipal Member member, @RequestParam String title, RedirectAttributes redirectAttributes) {
        try {
            Topic topic = topicService.createTopic(title, member);
            redirectAttributes.addFlashAttribute("message", "토픽 '" + title + "'이(가) 성공적으로 생성되었습니다.");
            return "redirect:/topics/" + topic.getId();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/topics/new";
        }
    }

    @GetMapping("/{id}")
    public String getTopicDetail(@PathVariable Long id, @AuthenticationPrincipal Member member, Model model) {
        Topic topic = topicService.findTopicById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 토픽입니다: " + id));

        if (!topic.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("해당 토픽에 접근 권한이 없습니다.");
        }

        List<Question> questions = topicService.getQuestionsByTopic(topic);

        model.addAttribute("topic", topic);
        model.addAttribute("questions", questions);
        return "topic-detail";
    }

    @PostMapping("/{id}/delete")
    public String deleteTopic(@PathVariable Long id, @AuthenticationPrincipal Member member, RedirectAttributes redirectAttributes) {
        Topic topic = topicService.findTopicById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 토픽입니다: " + id));

        if (!topic.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("해당 토픽을 삭제할 권한이 없습니다.");
        }

        topicService.deleteTopic(id);
        redirectAttributes.addFlashAttribute("message", "토픽이 삭제되었습니다.");
        return "redirect:/topics";
    }

    @PostMapping("/{id}/questions")
    public String addQuestion(
            @PathVariable("id") Long topicId,
            @AuthenticationPrincipal Member member,
            @RequestParam String content,
            RedirectAttributes redirectAttributes
    ) {
        Topic topic = topicService.findTopicById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 토픽입니다: " + topicId));

        if (!topic.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("해당 토픽에 질문을 추가할 권한이 없습니다.");
        }

        Question createdQuestion = questionService.createQuestionAndGetAnswer(content, topicId).block();

        if (createdQuestion != null) {
            redirectAttributes.addFlashAttribute("message", "질문이 등록되었습니다.");
        } else {
            redirectAttributes.addFlashAttribute("error", "질문 등록에 실패했습니다.");
        }
        return "redirect:/topics/" + topicId;
    }

    @PostMapping("/{id}/questions/{questionId}/summary/save")
    public String saveSummaryOnly(
            @PathVariable("id") Long topicId,
            @PathVariable("questionId") Long questionId,
            @AuthenticationPrincipal Member member,
            @RequestParam Long answerId,
            @RequestParam String userSummary,
            RedirectAttributes redirectAttributes
    ) {
        Topic topic = topicService.findTopicById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 토픽입니다: " + topicId));

        if (!topic.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        questionService.saveSummary(answerId, userSummary);
        redirectAttributes.addFlashAttribute("message", "요약이 저장되었습니다.");
        return "redirect:/topics/" + topicId;
    }

    @PostMapping("/{id}/questions/{questionId}/summary/validate")
    public String saveAndValidateSummary(
            @PathVariable("id") Long topicId,
            @PathVariable("questionId") Long questionId,
            @AuthenticationPrincipal Member member,
            @RequestParam Long answerId,
            @RequestParam String userSummary,
            RedirectAttributes redirectAttributes
    ) {
        Topic topic = topicService.findTopicById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 토픽입니다: " + topicId));

        if (!topic.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        questionService.validateAndSaveSummary(answerId, userSummary).block();
        redirectAttributes.addFlashAttribute("message", "요약이 저장되었으며 AI 검증이 완료되었습니다.");
        return "redirect:/topics/" + topicId;
    }
}
