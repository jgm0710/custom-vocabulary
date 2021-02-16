package project.study.jgm.customvocabulary.vocabulary;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.study.jgm.customvocabulary.common.dto.CriteriaDto;
import project.study.jgm.customvocabulary.vocabulary.exception.BadRequestByDivision;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static project.study.jgm.customvocabulary.vocabulary.QVocabulary.vocabulary;

@Repository
@RequiredArgsConstructor
public class VocabularyQueryRepository {

    private final EntityManager em;

    private final JPAQueryFactory queryFactory;

    /**
     * personal
     */
    public QueryResults<Vocabulary> findAllByPersonal(CriteriaDto criteriaDto, VocabularyDivision division, Long memberId, Long categoryId) {
        return queryFactory
                .select(vocabulary)
                .from(vocabulary)
                .where(
                        divisionEq(division),
                        memberIdEq(memberId),
                        categoryIdEq(categoryId)
                )
                .orderBy(vocabulary.id.desc())
                .offset(criteriaDto.getOffset())
                .limit(criteriaDto.getLimit())
                .fetchResults();
    }

    public long getCountOfPersonalVocabularyWhereCategoryIsNull(Long memberId) {
        return queryFactory
                .selectFrom(vocabulary)
                .where(
                        vocabulary.member.id.eq(memberId),
                        vocabulary.category.isNull(),
                        vocabulary.division.eq(VocabularyDivision.PERSONAL)
                )
                .fetchCount();
    }

    public long getCountOfSharedVocabularyWhereCategoryIsNull() {
        return queryFactory
                .selectFrom(vocabulary)
                .where(
                        vocabulary.division.eq(VocabularyDivision.SHARED),
                        vocabulary.category.isNull()
                )
                .fetchCount();
    }

    private BooleanExpression categoryIdEq(Long categoryId) {
        return categoryId != null ? vocabulary.category.id.eq(categoryId) : null;
    }

    private BooleanExpression memberIdEq(Long memberId) {
        return memberId != null ? vocabulary.member.id.eq(memberId) : null;
    }

    private BooleanExpression divisionEq(VocabularyDivision division) {
        switch (division) {
            case PERSONAL:
                return vocabulary.division.eq(VocabularyDivision.PERSONAL);
            case SHARED:
                return vocabulary.division.eq(VocabularyDivision.SHARED);
            case COPIED:
                return vocabulary.division.eq(VocabularyDivision.COPIED);
            default:
                throw new BadRequestByDivision();
        }
    }

    /**
     * shared
     */
    public QueryResults<Vocabulary> findAllByShared(CriteriaDto criteriaDto, Long categoryId, String title, VocabularySortCondition sortCondition) {
        return queryFactory
                .select(vocabulary)
                .from(vocabulary)
                .where(
                        categoryIdEq(categoryId),
                        vocabulary.division.eq(VocabularyDivision.SHARED),
                        titleLike(title)
                )
                .orderBy(sortConditionEq(sortCondition).toArray(OrderSpecifier[]::new))
                .offset(criteriaDto.getOffset())
                .limit(criteriaDto.getLimit())
                .fetchResults();
    }

    private List<OrderSpecifier<?>> sortConditionEq(VocabularySortCondition sortCondition) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        if (sortCondition == VocabularySortCondition.LATEST_ASC) {
            orderSpecifiers.add(vocabulary.registerDate.asc());
            orderSpecifiers.add(vocabulary.id.asc());
        } else if (sortCondition == VocabularySortCondition.LATEST_DESC) {
            orderSpecifiers.add(vocabulary.registerDate.desc());
            orderSpecifiers.add(vocabulary.id.desc());
        } else if (sortCondition == VocabularySortCondition.LIKE_ASC) {
            orderSpecifiers.add(vocabulary.likeCount.asc());
            orderSpecifiers.add(vocabulary.id.desc());
        } else if (sortCondition == VocabularySortCondition.LIKE_DESC) {
            orderSpecifiers.add(vocabulary.likeCount.desc());
            orderSpecifiers.add(vocabulary.id.desc());
        } else if (sortCondition == VocabularySortCondition.VIEWS_ASC) {
            orderSpecifiers.add(vocabulary.views.asc());
            orderSpecifiers.add(vocabulary.id.desc());
        } else if (sortCondition == VocabularySortCondition.VIEWS_DESC) {
            orderSpecifiers.add(vocabulary.views.desc());
            orderSpecifiers.add(vocabulary.id.desc());
        } else if (sortCondition == VocabularySortCondition.DOWNLOAD_ASC) {
            orderSpecifiers.add(vocabulary.downloadCount.asc());
            orderSpecifiers.add(vocabulary.id.desc());
        } else if (sortCondition == VocabularySortCondition.DOWNLOAD_DESC) {
            orderSpecifiers.add(vocabulary.downloadCount.desc());
            orderSpecifiers.add(vocabulary.id.desc());
        } else if (sortCondition == VocabularySortCondition.DIFFICULTY_ASC) {
            orderSpecifiers.add(vocabulary.difficulty.asc());
            orderSpecifiers.add(vocabulary.downloadCount.desc());
            orderSpecifiers.add(vocabulary.likeCount.desc());
            orderSpecifiers.add(vocabulary.id.desc());
        } else if (sortCondition == VocabularySortCondition.DIFFICULTY_DESC) {
            orderSpecifiers.add(vocabulary.difficulty.desc());
            orderSpecifiers.add(vocabulary.downloadCount.desc());
            orderSpecifiers.add(vocabulary.likeCount.desc());
            orderSpecifiers.add(vocabulary.id.desc());
        } else {
            orderSpecifiers.add(vocabulary.id.desc());
        }

        return orderSpecifiers;
    }

    private BooleanExpression titleLike(String title) {
        return title != null ? vocabulary.title.contains(title) : null;
    }
}
