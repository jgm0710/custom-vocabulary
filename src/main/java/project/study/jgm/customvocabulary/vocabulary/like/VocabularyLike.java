package project.study.jgm.customvocabulary.vocabulary.like;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.vocabulary.Vocabulary;

import javax.persistence.*;
import java.time.LocalDateTime;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VocabularyLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vocabulary_like_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "vocabulary_id")
    private Vocabulary vocabulary;

    private LocalDateTime registerDate;

   /* @Override
    public String toString() {
        return "Like{" +
                "id=" + id +
                ", member=" + member +
                ", vocabulary=" + vocabulary +
                ", registerDate=" + registerDate +
                '}';
    }*/

    public static VocabularyLike createVocabularyLike(Member member, Vocabulary vocabulary) {
        return VocabularyLike.builder()
                .member(member)
                .vocabulary(vocabulary)
                .registerDate(LocalDateTime.now())
                .build();
    }

}
