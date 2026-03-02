package me.deepStudyWithAI.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Answer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // AI 원본 답변
    @Column(columnDefinition = "TEXT")
    private String aiContent;

    // 내가 이해한 요약
    @Column(columnDefinition = "TEXT")
    private String userSummary;

    // AI의 요약 검증 피드백
    @Column(columnDefinition = "TEXT")
    private String aiFeedback;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    public Answer(String aiContent, Question question) {
        this.aiContent = aiContent;
        this.question = question;
        question.attachAnswer(this);
    }

    public void updateSummary(String userSummary) {
        this.userSummary = userSummary;
    }

    public void updateFeedback(String aiFeedback) {
        this.aiFeedback = aiFeedback;
    }
}
