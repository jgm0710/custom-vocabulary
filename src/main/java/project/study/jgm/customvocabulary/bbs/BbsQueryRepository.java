package project.study.jgm.customvocabulary.bbs;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.study.jgm.customvocabulary.bbs.dto.BbsSearchDto;
import project.study.jgm.customvocabulary.bbs.dto.BbsSearchType;
import project.study.jgm.customvocabulary.bbs.dto.BbsSortType;
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

    public QueryResults<Bbs> findAll(BbsSearchDto bbsSearchDto) {
        return queryFactory
                .select(bbs)
                .from(bbs)
                .where(whereFrom(bbsSearchDto))
                .orderBy(sortConditionEq(bbsSearchDto.getBbsSortType()).toArray(OrderSpecifier[]::new))
                .offset(bbsSearchDto.getCriteriaDto().getOffset())
                .limit(bbsSearchDto.getCriteriaDto().getLimit())
                .fetchResults();
    }

    private BooleanExpression whereFrom(BbsSearchDto bbsSearchDto) {
        return searchBy(bbsSearchDto).and(bbs.status.eq(bbsSearchDto.getBbsStatus()));
    }

    private BooleanExpression searchBy(BbsSearchDto bbsSearchDto) {
        if (bbsSearchDto.getSearchType() == BbsSearchType.TITLE) {
            return bbs.title.contains(bbsSearchDto.getKeyword());
        } else if (bbsSearchDto.getSearchType() == BbsSearchType.CONTENT) {
            return bbs.content.contains(bbsSearchDto.getKeyword());
        } else if (bbsSearchDto.getSearchType() == BbsSearchType.TITLE_OR_CONTENT) {
            return bbs.title.contains(bbsSearchDto.getKeyword()).or(bbs.content.contains(bbsSearchDto.getKeyword()));
        } else if (bbsSearchDto.getSearchType() == BbsSearchType.WRITER) {
            return bbs.member.nickname.contains(bbsSearchDto.getKeyword());
        } else {
            return bbs.id.gt(0);
        }
    }

    private List<OrderSpecifier<?>> sortConditionEq(BbsSortType sortType) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        if (sortType == BbsSortType.LATEST_ASC) {
            orderSpecifiers.add(bbs.registerDate.asc());
            orderSpecifiers.add(bbs.id.asc());
        } else if (sortType == BbsSortType.LATEST_DESC) {
            orderSpecifiers.add(bbs.registerDate.desc());
            orderSpecifiers.add(bbs.id.desc());
        } else if (sortType == BbsSortType.VIEWS_ASC) {
            orderSpecifiers.add(bbs.views.asc());
            orderSpecifiers.add(bbs.id.desc());
        } else if (sortType == BbsSortType.VIEWS_DESC) {
            orderSpecifiers.add(bbs.views.desc());
            orderSpecifiers.add(bbs.id.desc());
        } else if (sortType == BbsSortType.LIKE_ASC) {
            orderSpecifiers.add(bbs.likeCount.asc());
            orderSpecifiers.add(bbs.id.desc());
        } else if (sortType == BbsSortType.LIKE_DESC) {
            orderSpecifiers.add(bbs.likeCount.desc());
            orderSpecifiers.add(bbs.id.desc());
        } else if (sortType == BbsSortType.REPLY_COUNT_ASC) {
            orderSpecifiers.add(bbs.replyCount.asc());
            orderSpecifiers.add(bbs.id.desc());
        } else if (sortType == BbsSortType.REPLY_COUNT_DESC) {
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
