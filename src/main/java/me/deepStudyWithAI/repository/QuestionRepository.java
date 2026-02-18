package me.deepStudyWithAI.repository;

import me.deepStudyWithAI.domain.Question;
import me.deepStudyWithAI.domain.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByTopicOrderByCreatedAtAsc(Topic topic);
}
