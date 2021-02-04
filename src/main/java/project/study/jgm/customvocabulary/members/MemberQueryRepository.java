package project.study.jgm.customvocabulary.members;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.study.jgm.customvocabulary.members.dto.search.MemberSearchDto;
import project.study.jgm.customvocabulary.members.dto.search.MemberSearchType;
import project.study.jgm.customvocabulary.members.dto.search.MemberSortType;

import java.util.ArrayList;
import java.util.List;

import static project.study.jgm.customvocabulary.members.QMember.member;

@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Member findByRefreshToken(String refreshToken) {
        return queryFactory
                .selectFrom(member)
                .where(loginInfoEq(refreshToken))
                .fetchOne();
    }

    public List<Member> findAll(MemberSearchDto searchDto) {
        return queryFactory
                .selectFrom(member)
                .where(whereFrom(searchDto))
                .offset(searchDto.getCriteriaDto().getOffset())
                .limit(searchDto.getCriteriaDto().getLimit())
                .orderBy(sortFrom(searchDto).toArray(OrderSpecifier[]::new))
                .fetch();
    }

    private List<OrderSpecifier<?>> sortFrom(MemberSearchDto searchDto) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        MemberSortType sortType = searchDto.getSortType();

        if (sortType == MemberSortType.OLDEST) {
            orderSpecifiers.add(member.id.asc());
        } else if (sortType == MemberSortType.BBS_COUNT_DESC) {
            orderSpecifiers.add(member.bbsCount.desc());
            orderSpecifiers.add(member.id.desc());
        } else if (sortType == MemberSortType.BBS_COUNT_ASC) {
            orderSpecifiers.add(member.bbsCount.asc());
            orderSpecifiers.add(member.id.desc());
        } else if (sortType == MemberSortType.SHARED_VOCABULARY_COUNT_DESC) {
            orderSpecifiers.add(member.sharedVocabularyCount.desc());
            orderSpecifiers.add(member.id.desc());
        } else if (sortType == MemberSortType.SHARED_VOCABULARY_COUNT_ASC) {
            orderSpecifiers.add(member.sharedVocabularyCount.asc());
            orderSpecifiers.add(member.id.desc());
        } else {
            orderSpecifiers.add(member.id.desc());
        }

        return orderSpecifiers;
    }

    private BooleanExpression whereFrom(MemberSearchDto searchDto) {
        MemberSearchType searchType = searchDto.getSearchType();

        if (searchType == MemberSearchType.JOIN_ID) {
            return member.joinId.contains(searchDto.getKeyword());
        } else if (searchType == MemberSearchType.EMAIL) {
            return member.email.contains(searchDto.getKeyword());
        } else if (searchType == MemberSearchType.NAME) {
            return member.name.contains(searchDto.getKeyword());
        } else if (searchType == MemberSearchType.NICKNAME) {
            return member.nickname.contains(searchDto.getKeyword());
        } else {
            return member.id.gt(0);
        }
    }

    public Long findTotalCount(MemberSearchDto memberSearchDto) {
        return queryFactory
                .selectFrom(member)
                .where(whereFrom(memberSearchDto))
                .fetchCount();
    }

    private BooleanExpression loginInfoEq(String refreshToken) {
        return member.loginInfo.refreshToken.eq(refreshToken);
    }

}
