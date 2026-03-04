package me.deepStudyWithAI.service;

import lombok.RequiredArgsConstructor;
import me.deepStudyWithAI.domain.Member;
import me.deepStudyWithAI.domain.Question;
import me.deepStudyWithAI.domain.Topic;
import me.deepStudyWithAI.repository.QuestionRepository;
import me.deepStudyWithAI.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TopicService {

    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;

    @Transactional
    public Topic createTopic(String title, Member member) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("토픽 제목은 비어 있을 수 없습니다.");
        }
        
        Topic topic = new Topic(title, member);
        return topicRepository.save(topic);
    }

    public List<Topic> getTopicsByMember(Member member) {
        return topicRepository.findByMemberOrderByCreatedAtDesc(member);
    }

    public List<Topic> getAllTopics() {
        return topicRepository.findAll();
    }

    public Optional<Topic> findTopicById(Long id) {
        return topicRepository.findById(id);
    }

    public List<Question> getQuestionsByTopic(Topic topic) {
        return questionRepository.findByTopicOrderByCreatedAtAsc(topic);
    }

    @Transactional
    public void deleteTopic(Long id) {
        topicRepository.deleteById(id);
    }
}
