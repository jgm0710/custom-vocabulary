package project.study.jgm.customvocabulary.bbs.reply.like;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.bbs.reply.Reply;
import project.study.jgm.customvocabulary.members.Member;

import javax.persistence.*;
import java.time.LocalDateTime;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReplyLike {

    @Id
    @GeneratedValue
    @Column(name = "reply_like_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "reply_id")
    private Reply reply;

    private LocalDateTime registerDate;

    public static ReplyLike createReplyLike(Member member, Reply reply) {
        ReplyLike replyLike = ReplyLike.builder()
                .member(member)
                .reply(reply)
                .registerDate(LocalDateTime.now())
                .build();

        reply.increaseLikeCount();

        return replyLike;
    }

    public void delete(ReplyLikeRepository replyLikeRepository) {
        this.reply.decreaseLikeCount();
        replyLikeRepository.delete(this);
    }
}
