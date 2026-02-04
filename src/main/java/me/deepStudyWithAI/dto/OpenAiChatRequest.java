package me.deepStudyWithAI.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OpenAiChatRequest {

    private String model;
    private List<Message> messages;

    @Getter
    @Builder
    public static class Message {
        private String role;   // system, user
        private String content;
    }
}