package me.deepStudyWithAI.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL)
    private Answer answer;

    public Question(String content, Topic topic) {
        this.content = content;
        this.topic = topic;
    }

    public void attachAnswer(Answer answer) {
        this.answer = answer;
    }
}
