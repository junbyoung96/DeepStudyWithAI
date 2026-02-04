package me.deepStudyWithAI.service;

import lombok.RequiredArgsConstructor;
import me.deepStudyWithAI.dto.OpenAiChatRequest;
import me.deepStudyWithAI.dto.OpenAiChatResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final WebClient openAiWebClient;

    public String ask(String question) {

        OpenAiChatRequest request = OpenAiChatRequest.builder()
                .model("gpt-4.1-mini")
                .messages(List.of(
                        OpenAiChatRequest.Message.builder()
                                .role("system")
                                .content("당신은 지식을 쉽게 설명해주는 친절한 전문가입니다.")
                                .build(),
                        OpenAiChatRequest.Message.builder()
                                .role("user")
                                .content(question)
                                .build()
                ))
                .build();

        OpenAiChatResponse response = openAiWebClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpenAiChatResponse.class)
                .block();

        return response.getChoices().get(0).getMessage().getContent();
    }
}
