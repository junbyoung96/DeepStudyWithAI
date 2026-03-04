package me.deepStudyWithAI.controller;

import me.deepStudyWithAI.domain.Member;
import me.deepStudyWithAI.domain.Topic;
import me.deepStudyWithAI.service.QuestionService;
import me.deepStudyWithAI.service.TopicService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TopicController.class)
class TopicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TopicService topicService;

    @MockitoBean
    private QuestionService questionService;

    @Test
    @DisplayName("토픽 목록 페이지는 로그인한 사용자에게 공개된다.")
    @WithMockUser(username = "test@test.com")
    void listTopics_Authenticated() throws Exception {
        // given
        given(topicService.getTopicsByMember(any())).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/topics"))
                .andExpect(status().isOk())
                .andExpect(view().name("topic-list"))
                .andExpect(model().attributeExists("topics"));
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 토픽 목록을 요청하면 로그인 페이지로 리다이렉트된다.")
    void listTopics_Anonymous() throws Exception {
        mockMvc.perform(get("/topics"))
                .andExpect(status().isFound()); // 302 Found
    }

    @Test
    @DisplayName("새로운 토픽 생성을 POST로 요청하면 상세 페이지로 리다이렉트된다.")
    @WithMockUser(username = "test@test.com")
    void createTopic_Redirects() throws Exception {
        // given
        Topic savedTopic = new Topic("새 주제", null);
        given(topicService.createTopic(anyString(), any())).willReturn(savedTopic);

        // when & then
        mockMvc.perform(post("/topics")
                        .param("title", "새 주제")
                        .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("/topics/*"));
    }
}
