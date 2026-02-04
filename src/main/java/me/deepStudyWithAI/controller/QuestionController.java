package me.deepStudyWithAI.controller;


import lombok.RequiredArgsConstructor;
import me.deepStudyWithAI.service.OpenAiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class QuestionController {

    private final OpenAiService openAiService;

    @GetMapping("/questions")
    public String questionForm() {
        return "question-form";
    }

    @PostMapping("/questions")
    public String askQuestion(
            @RequestParam String question,
            Model model
    ) {
        String aiAnswer = openAiService.ask(question);

        model.addAttribute("question", question);
        model.addAttribute("aiAnswer", aiAnswer);

        return "question-form";
    }
}
