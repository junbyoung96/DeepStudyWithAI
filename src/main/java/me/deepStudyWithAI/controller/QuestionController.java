package me.deepStudyWithAI.controller;

import lombok.RequiredArgsConstructor;
import me.deepStudyWithAI.domain.Question;
import me.deepStudyWithAI.service.QuestionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/questions")
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/{id}")
    public String getQuestionDetail(@PathVariable Long id, Model model) {
        Question question = questionService.getQuestionWithAnswer(id);
        model.addAttribute("question", question);
        return "question-detail"; // question-detail.html 템플릿 필요
    }

    @PostMapping("/{id}/delete")
    public String deleteQuestion(@PathVariable Long id, @RequestParam Long topicId, RedirectAttributes redirectAttributes) {
        questionService.deleteQuestion(id);
        redirectAttributes.addFlashAttribute("message", "질문이 삭제되었습니다.");
        return "redirect:/topics/" + topicId;
    }

    @PostMapping("/{id}/summary")
    public String saveSummary(
            @PathVariable("id") Long questionId,
            @RequestParam("answerId") Long answerId,
            @RequestParam("userSummary") String userSummary,
            RedirectAttributes redirectAttributes
    ) {
        questionService.validateAndSaveSummary(answerId, userSummary).block();
        redirectAttributes.addFlashAttribute("message", "요약이 성공적으로 저장되었습니다.");
        return "redirect:/questions/" + questionId;
    }

    @PostMapping("/{id}/add-question")
    public String addQuestion(
            @PathVariable("id") Long parentQuestionId,
            @RequestParam("content") String content,
            @RequestParam("topicId") Long topicId,
            RedirectAttributes redirectAttributes
    ) {
        // Use the overloaded method to pass parentQuestionId for context
        Question newQuestion = questionService.createQuestionAndGetAnswer(content, topicId, parentQuestionId).block();
        if (newQuestion != null) {
            redirectAttributes.addFlashAttribute("message", "추가 질문이 성공적으로 등록되었고 AI 답변이 생성되었습니다.");
            return "redirect:/questions/" + newQuestion.getId();
        } else {
            redirectAttributes.addFlashAttribute("error", "추가 질문 등록 및 AI 답변 생성에 실패했습니다.");
            return "redirect:/questions/" + parentQuestionId; // Redirect back to the parent question if creation fails
        }
    }
}
