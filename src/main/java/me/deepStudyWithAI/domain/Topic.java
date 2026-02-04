package me.deepStudyWithAI.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Topic extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    // 부모 토픽
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Topic parent;

    // 하위 토픽
    @OneToMany(mappedBy = "parent")
    private List<Topic> children = new ArrayList<>();

    @OneToMany(mappedBy = "topic")
    private List<Question> questions = new ArrayList<>();

    public Topic(String title, Topic parent) {
        this.title = title;
        this.parent = parent;
    }
}
