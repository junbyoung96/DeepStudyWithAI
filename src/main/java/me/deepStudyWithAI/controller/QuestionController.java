package me.deepStudyWithAI.controller;

import lombok.RequiredArgsConstructor;
import me.deepStudyWithAI.domain.Member;
import me.deepStudyWithAI.domain.Topic;
import me.deepStudyWithAI.service.QuestionService;
import me.deepStudyWithAI.service.TopicService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final TopicService topicService;

    @PostMapping("/{id}/delete")
    public String deleteQuestion(
            @PathVariable Long id, 
            @RequestParam Long topicId, 
            @AuthenticationPrincipal Member member,
            RedirectAttributes redirectAttributes
    ) {
        Topic topic = topicService.findTopicById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 토픽입니다: " + topicId));
                
        if (!topic.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        questionService.deleteQuestion(id);
        redirectAttributes.addFlashAttribute("message", "질문이 삭제되었습니다.");
        return "redirect:/topics/" + topicId;
    }
}
