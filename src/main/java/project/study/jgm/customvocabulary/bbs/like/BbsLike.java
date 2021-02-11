package project.study.jgm.customvocabulary.bbs.like;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.bbs.Bbs;
import project.study.jgm.customvocabulary.members.Member;

import javax.persistence.*;
import java.time.LocalDateTime;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BbsLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bbs_like_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "bbs_id")
    private Bbs bbs;

    private LocalDateTime registerDate;

    public static BbsLike createBbsLike(Member member, Bbs bbs) {
        return BbsLike.builder()
                .member(member)
                .bbs(bbs)
                .registerDate(LocalDateTime.now())
                .build();
    }
}
