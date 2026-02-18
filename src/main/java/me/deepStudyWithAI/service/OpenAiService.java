package me.deepStudyWithAI.service;

import lombok.RequiredArgsConstructor;
import me.deepStudyWithAI.dto.OpenAiChatRequest;
import me.deepStudyWithAI.dto.OpenAiChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final WebClient openAiWebClient;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    public Mono<String> getChatResponse(String prompt) {
        OpenAiChatRequest.Message message = OpenAiChatRequest.Message.builder()
                .role("user")
                .content(prompt)
                .build();

        return getChatResponseWithMessages(Collections.singletonList(message));
    }

    public Mono<String> getChatResponseWithMessages(List<OpenAiChatRequest.Message> messages) {
        OpenAiChatRequest request = OpenAiChatRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .build();

        return openAiWebClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + openAiApiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpenAiChatResponse.class)
                .map(response -> response.getChoices().get(0).getMessage().getContent());
    }
}
