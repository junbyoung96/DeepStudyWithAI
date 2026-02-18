package me.deepStudyWithAI.repository;

import me.deepStudyWithAI.domain.Answer;
import me.deepStudyWithAI.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    Optional<Answer> findByQuestion(Question question);
}
