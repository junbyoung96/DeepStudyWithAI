package me.deepStudyWithAI.repository;

import me.deepStudyWithAI.domain.Member;
import me.deepStudyWithAI.domain.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    Optional<Topic> findByTitle(String title);
    List<Topic> findByMemberOrderByCreatedAtDesc(Member member);
}
