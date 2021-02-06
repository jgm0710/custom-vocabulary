package project.study.jgm.customvocabulary.bbs.like;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import static project.study.jgm.customvocabulary.bbs.like.QBbsLike.bbsLike;

@Repository
@RequiredArgsConstructor
public class BbsLikeQueryRepository {

    private final EntityManager em;

    private final JPAQueryFactory queryFactory;

    public BbsLike findLikeByMemberAndBbs(Long memberId, Long bbsId) {
        return queryFactory
                .select(bbsLike)
                .from(bbsLike)
                .where(
                        bbsLike.member.id.eq(memberId),
                        bbsLike.bbs.id.eq(bbsId)
                )
                .fetchOne();
    }
}
