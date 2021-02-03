package project.study.jgm.customvocabulary.members;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.study.jgm.customvocabulary.common.CriteriaDto;
import project.study.jgm.customvocabulary.members.dto.MemberSearchDto;
import project.study.jgm.customvocabulary.members.dto.MemberSearchType;

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
                .orderBy(sortFrom(searchDto))
                .fetch();
    }

    private OrderSpecifier<?> sortFrom(MemberSearchDto searchDto) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        OrderSpecifier<Integer> asc = member.bbsCount.asc();
        orderSpecifiers.add(asc);

//        orderSpecifiers.toArray(new OrderSpecifier<>());
        return null;
    }

    private BooleanExpression whereFrom(MemberSearchDto searchDto) {
        MemberSearchType searchType = searchDto.getSearchType();
        if (searchType == MemberSearchType.JOIN_ID) {
            return member.joinId.eq(searchDto.getSearchContent());
        } else if (searchType == MemberSearchType.EMAIL) {
            return member.email.eq(searchDto.getSearchContent());
        } else if (searchType == MemberSearchType.NAME) {
            return member.name.eq(searchDto.getSearchContent());
        } else if (searchType == MemberSearchType.NICKNAME) {
            return member.nickname.eq(searchDto.getSearchContent());
        } else {
            return member.id.gt(0);
        }
    }

    public Long findTotalCount() {
        return queryFactory
                .selectFrom(member)
                .fetchCount();
    }

    private BooleanExpression loginInfoEq(String refreshToken) {
        return member.loginInfo.refreshToken.eq(refreshToken);
    }

}
