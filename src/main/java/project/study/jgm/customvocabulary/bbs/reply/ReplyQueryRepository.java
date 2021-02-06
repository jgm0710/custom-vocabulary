package project.study.jgm.customvocabulary.bbs.reply;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.study.jgm.customvocabulary.common.dto.CriteriaDto;

import java.util.ArrayList;
import java.util.List;

import static project.study.jgm.customvocabulary.bbs.reply.QReply.reply;

@Repository
@RequiredArgsConstructor
public class ReplyQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<Reply> findAll(CriteriaDto criteriaDto, Long bbsId, ReplySortCondition sortCondition) {
        return queryFactory
                .select(reply)
                .from(reply)
                .where(
                        reply.bbs.id.eq(bbsId),
                        reply.status.eq(ReplyStatus.REGISTER)
                )
                .orderBy(sortConditionEq(sortCondition).toArray(OrderSpecifier[]::new))
                .offset(criteriaDto.getOffset())
                .limit(criteriaDto.getLimit())
                .fetch();
    }

    private List<OrderSpecifier<?>> sortConditionEq(ReplySortCondition sortCondition) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        if (sortCondition == ReplySortCondition.LATEST_ASC) {
            orderSpecifiers.add(reply.registerDate.asc());
            orderSpecifiers.add(reply.id.asc());
        } else if (sortCondition == ReplySortCondition.LATEST_DESC) {
            orderSpecifiers.add(reply.registerDate.desc());
            orderSpecifiers.add(reply.id.desc());
        } else if (sortCondition == ReplySortCondition.LIKE_ASC) {
            orderSpecifiers.add(reply.likeCount.asc());
            orderSpecifiers.add(reply.id.desc());
        } else if (sortCondition == ReplySortCondition.LIKE_DESC) {
            orderSpecifiers.add(reply.likeCount.desc());
            orderSpecifiers.add(reply.id.desc());
        } else {
            orderSpecifiers.add(reply.likeCount.desc());
            orderSpecifiers.add(reply.id.desc());
        }

        return orderSpecifiers;
    }

}
