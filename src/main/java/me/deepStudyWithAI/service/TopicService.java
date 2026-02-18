package me.deepStudyWithAI.service;

import lombok.RequiredArgsConstructor;
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
    public Topic createTopic(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Topic title cannot be empty.");
        }
        // Optional: Check for duplicate topic names
         if (topicRepository.findByTitle(title).isPresent()) {
             throw new IllegalArgumentException("Topic with title '" + title + "' already exists.");
        }
        Topic topic = new Topic(title);
        return topicRepository.save(topic);
    }

    public Optional<Topic> findTopicById(Long id) {
        return topicRepository.findById(id);
    }

    public List<Question> getQuestionsByTopic(Topic topic) {
        return questionRepository.findByTopicOrderByCreatedAtAsc(topic);
    }
}
