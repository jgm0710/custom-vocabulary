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
import static project.study.jgm.customvocabulary.vocabulary.VocabularyDivision.*;

@Repository
@RequiredArgsConstructor
public class VocabularyQueryRepository {

    private final EntityManager em;

    private final JPAQueryFactory queryFactory;

    /**
     * personal
     */
    public QueryResults<Vocabulary> findAllByMember(CriteriaDto criteria, Long memberId, VocabularySearchBy searchBy, Long categoryId, VocabularyDivision... divisions) {

        return queryFactory
                .select(vocabulary)
                .from(vocabulary)
                .where(
                        divisionsEq(divisions),
                        proprietorIdEq(memberId),
                        categoryIdEq(searchBy, categoryId)
                )
                .orderBy(vocabulary.id.desc())
                .offset(criteria.getOffset())
                .limit(criteria.getLimit())
                .fetchResults();
    }

    private BooleanExpression divisionsEq(VocabularyDivision... divisions) {
        BooleanExpression booleanExpression = null;
        for (VocabularyDivision division : divisions) {
            if (booleanExpression == null) {
                booleanExpression = divisionEq(division);
            } else {
                booleanExpression = booleanExpression.or(divisionEq(division));
            }
        }

        return booleanExpression;
    }

    public long getCountOfPersonalVocabularyWhereCategoryIsNull(Long proprietorId) {
        return queryFactory
                .selectFrom(vocabulary)
                .where(
                        proprietorIdEq(proprietorId),
                        vocabulary.category.isNull(),
                        divisionsEq(PERSONAL, COPIED)
                )
                .fetchCount();
    }

    public long getCountOfSharedVocabularyWhereCategoryIsNull() {
        return queryFactory
                .selectFrom(vocabulary)
                .where(
                        divisionEq(SHARED),
                        vocabulary.category.isNull()
                )
                .fetchCount();
    }

    private BooleanExpression categoryIdEq(VocabularySearchBy searchBy, Long categoryId) {
        if (searchBy == VocabularySearchBy.BY_CATEGORY) {
            if (categoryId != null) {
                return vocabulary.category.id.eq(categoryId);
            } else {
                return vocabulary.category.id.isNull();
            }
        } else {
            return null;
        }
    }

    private BooleanExpression proprietorIdEq(Long proprietorId) {
        return proprietorId != null ? vocabulary.proprietor.id.eq(proprietorId) : null;
    }

    private BooleanExpression divisionEq(VocabularyDivision division) {
        switch (division) {
            case PERSONAL:
                return vocabulary.division.eq(PERSONAL);
            case SHARED:
                return vocabulary.division.eq(SHARED);
            case COPIED:
                return vocabulary.division.eq(COPIED);
            case DELETE:
                return vocabulary.division.eq(DELETE);
            case UNSHARED:
                return vocabulary.division.eq(UNSHARED);
            default:
                throw new BadRequestByDivision();
        }
    }

    /**
     * shared
     */
    public QueryResults<Vocabulary> findAllByShared(CriteriaDto criteria, VocabularySearchBy searchBy, Long categoryId, String title, VocabularySortCondition sortCondition) {
        return queryFactory
                .select(vocabulary)
                .from(vocabulary)
                .where(
                        categoryIdEq(searchBy, categoryId),
                        vocabulary.division.eq(SHARED),
                        titleLike(title)
                )
                .orderBy(sortConditionEq(sortCondition).toArray(OrderSpecifier[]::new))
                .offset(criteria.getOffset())
                .limit(criteria.getLimit())
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
