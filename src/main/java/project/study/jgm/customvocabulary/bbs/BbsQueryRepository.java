package project.study.jgm.customvocabulary.bbs;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.study.jgm.customvocabulary.common.dto.CriteriaDto;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static project.study.jgm.customvocabulary.bbs.QBbs.bbs;

@Repository
@RequiredArgsConstructor
public class BbsQueryRepository {

    private final EntityManager em;

    private final JPAQueryFactory queryFactory;

    public QueryResults<Bbs> findAll(CriteriaDto criteriaDto, BbsSortCondition sortCondition) {
        return queryFactory
                .select(bbs)
                .from(bbs)
                .where(bbs.status.eq(BbsStatus.REGISTER))
                .orderBy(sortConditionEq(sortCondition).toArray(OrderSpecifier[]::new))
                .offset(criteriaDto.getOffset())
                .limit(criteriaDto.getLimit())
                .fetchResults();
    }

    private List<OrderSpecifier<?>> sortConditionEq(BbsSortCondition sortCondition) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        if (sortCondition == BbsSortCondition.LATEST_ASC) {
            orderSpecifiers.add(bbs.registerDate.asc());
            orderSpecifiers.add(bbs.id.asc());
        } else if (sortCondition == BbsSortCondition.LATEST_DESC) {
            orderSpecifiers.add(bbs.registerDate.desc());
            orderSpecifiers.add(bbs.id.desc());
        } else if (sortCondition == BbsSortCondition.VIEWS_ASC) {
            orderSpecifiers.add(bbs.views.asc());
            orderSpecifiers.add(bbs.id.desc());
        } else if (sortCondition == BbsSortCondition.VIEWS_DESC) {
            orderSpecifiers.add(bbs.views.desc());
            orderSpecifiers.add(bbs.id.desc());
        } else if (sortCondition == BbsSortCondition.LIKE_ASC) {
            orderSpecifiers.add(bbs.likeCount.asc());
            orderSpecifiers.add(bbs.id.desc());
        } else if (sortCondition == BbsSortCondition.LIKE_DESC) {
            orderSpecifiers.add(bbs.likeCount.desc());
            orderSpecifiers.add(bbs.id.desc());
        } else if (sortCondition == BbsSortCondition.REPLY_COUNT_ASC) {
            orderSpecifiers.add(bbs.replyCount.asc());
            orderSpecifiers.add(bbs.id.desc());
        } else if (sortCondition == BbsSortCondition.REPLY_COUNT_DESC) {
            orderSpecifiers.add(bbs.replyCount.desc());
            orderSpecifiers.add(bbs.id.desc());
        } else {
            orderSpecifiers.add(bbs.id.desc());
        }

        return orderSpecifiers;
    }

    public QueryResults<Bbs> findAllByMember(CriteriaDto criteriaDto, Long memberId) {
        return queryFactory
                .select(bbs)
                .from(bbs)
                .where(
                        bbs.member.id.eq(memberId),
                        bbs.status.eq(BbsStatus.REGISTER)
                )
                .orderBy(bbs.id.desc())
                .offset(criteriaDto.getOffset())
                .limit(criteriaDto.getLimit())
                .fetchResults();
    }
}
