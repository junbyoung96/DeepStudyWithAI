package me.deepStudyWithAI.service;

import me.deepStudyWithAI.domain.Member;
import me.deepStudyWithAI.domain.Topic;
import me.deepStudyWithAI.repository.QuestionRepository;
import me.deepStudyWithAI.repository.TopicRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class) // Mockito를 JUnit5와 연결
class TopicServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private TopicService topicService;

    @Test
    @DisplayName("새로운 토픽을 성공적으로 생성한다.")
    void createTopic_Success() {
        // given: 테스트를 위해 준비하는 단계
        String title = "테스트 토픽";
        Member member = Member.builder().email("test@test.com").password("password").build();
        Topic topic = new Topic(title, member);
        
        given(topicRepository.save(any(Topic.class))).willReturn(topic);

        // when: 실제 검증하려는 로직을 실행하는 단계
        Topic createdTopic = topicService.createTopic(title, member);

        // then: 결과가 예상과 일치하는지 검증하는 단계
        assertThat(createdTopic.getTitle()).isEqualTo(title);
        assertThat(createdTopic.getMember()).isEqualTo(member);
        verify(topicRepository).save(any(Topic.class)); // 실제 save가 호출되었는지 검증
    }

    @Test
    @DisplayName("제목이 비어있으면 토픽 생성 시 예외가 발생한다.")
    void createTopic_Fail_EmptyTitle() {
        // given
        String title = "";
        Member member = Member.builder().email("test@test.com").password("password").build();

        // when & then: 예외가 발생하는지 확인
        assertThatThrownBy(() -> topicService.createTopic(title, member))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("토픽 제목은 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("ID로 토픽을 조회할 수 있다.")
    void findTopicById_Success() {
        // given
        Long topicId = 1L;
        Topic topic = new Topic("제목", null);
        given(topicRepository.findById(topicId)).willReturn(Optional.of(topic));

        // when
        Optional<Topic> result = topicService.findTopicById(topicId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("제목");
    }
}
