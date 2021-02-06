package project.study.jgm.customvocabulary.bbs.reply.like;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import static project.study.jgm.customvocabulary.bbs.reply.like.QReplyLike.replyLike;

@Repository
@RequiredArgsConstructor
public class ReplyLikeQueryRepository {

    private final EntityManager em;

    private final JPAQueryFactory queryFactory;

    public ReplyLike findByMemberAndReply(Long memberId, Long replyId) {
        return queryFactory
                .selectFrom(replyLike)
                .where(
                        replyLike.member.id.eq(memberId),
                        replyLike.reply.id.eq(replyId)
                )
                .fetchOne();
    }

}
