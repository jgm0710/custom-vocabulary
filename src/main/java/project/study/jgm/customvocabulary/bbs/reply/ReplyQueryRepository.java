package project.study.jgm.customvocabulary.bbs.reply;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.study.jgm.customvocabulary.bbs.BbsStatus;
import project.study.jgm.customvocabulary.common.dto.CriteriaDto;

import java.util.ArrayList;
import java.util.List;

import static project.study.jgm.customvocabulary.bbs.reply.QReply.reply;

@Repository
@RequiredArgsConstructor
public class ReplyQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<Reply> findParents(CriteriaDto criteriaDto, Long bbsId, ReplySortType sortType) {
        return queryFactory
                .select(reply)
                .from(reply)
                .where(
                        reply.bbs.id.eq(bbsId),
                        reply.status.eq(ReplyStatus.REGISTER),
                        reply.parent.isNull()
                )
                .orderBy(sortConditionEq(sortType).toArray(OrderSpecifier[]::new))
                .offset(criteriaDto.getOffset())
                .limit(criteriaDto.getLimit())
                .fetch();
    }

    public List<Reply> findChildren(CriteriaDto criteriaDto, Long parentId) {
        return queryFactory
                .selectFrom(reply)
                .where(reply.parent.id.eq(parentId))
                .orderBy(reply.id.desc())
                .offset(criteriaDto.getOffset())
                .limit(criteriaDto.getLimit())
                .fetch();
    }

    private List<OrderSpecifier<?>> sortConditionEq(ReplySortType sortType) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        if (sortType == ReplySortType.LATEST_ASC) {
            orderSpecifiers.add(reply.registerDate.asc());
            orderSpecifiers.add(reply.id.asc());
        } else if (sortType == ReplySortType.LATEST_DESC) {
            orderSpecifiers.add(reply.registerDate.desc());
            orderSpecifiers.add(reply.id.desc());
        } else if (sortType == ReplySortType.LIKE_ASC) {
            orderSpecifiers.add(reply.likeCount.asc());
            orderSpecifiers.add(reply.id.desc());
        } else if (sortType == ReplySortType.LIKE_DESC) {
            orderSpecifiers.add(reply.likeCount.desc());
            orderSpecifiers.add(reply.id.desc());
        } else {
            orderSpecifiers.add(reply.likeCount.desc());
            orderSpecifiers.add(reply.id.desc());
        }

        return orderSpecifiers;
    }

}
