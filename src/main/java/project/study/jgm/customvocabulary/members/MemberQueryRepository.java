package project.study.jgm.customvocabulary.members;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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

    private BooleanExpression loginInfoEq(String refreshToken) {
        return member.loginInfo.refreshToken.eq(refreshToken);
    }

}
