package project.study.jgm.customvocabulary.vocabulary.category;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import static project.study.jgm.customvocabulary.vocabulary.category.QCategory.category;

@Repository
@RequiredArgsConstructor
public class CategoryQueryRepository {

    private final EntityManager em;

    private final JPAQueryFactory queryFactory;

    /**
     * personal
     */
    public List<Category> findAllByMember(Long memberId) {

        QCategory parent = new QCategory("parent");

        return queryFactory
                .select(category)
                .from(category)
                .where(
                        category.member.id.eq(memberId),
                        category.division.eq(CategoryDivision.PERSONAL),
                        category.status.eq(CategoryStatus.REGISTER),
                        category.parent.isNull()
                )
                .fetch();
    }

    /**
     * Common
     */

    public Category findByParentIdAndOrders(Long parentId, int orders, CategoryDivision categoryDivision) {
        return queryFactory
                .selectFrom(category)
                .where(
                        whereFrom(parentId, orders, categoryDivision),
                        category.status.eq(CategoryStatus.REGISTER)
                )
                .fetchOne();
    }

    private BooleanExpression whereFrom(Long parentId, int orders, CategoryDivision categoryDivision) {
        BooleanExpression booleanExpression = category.id.gt(0);
        if (parentIdEq(parentId) != null) {
            booleanExpression = booleanExpression.and(parentIdEq(parentId));
        }
        return booleanExpression.and(ordersEq(orders)).and(divisionEq(categoryDivision));
    }
    private BooleanExpression divisionEq(CategoryDivision categoryDivision) {
        return category.division.eq(categoryDivision);
    }


    private BooleanExpression ordersEq(int orders) {
        return category.orders.eq(orders);
    }

    private BooleanExpression parentIdEq(Long parentId) {
        if (parentId == null) {
            return null;
        }
        return category.parent.id.eq(parentId);
    }

    /**
     * shared
     */
    public List<Category> findAllSharedCategory() {
        QCategory parent = new QCategory("parent");

        return queryFactory
                .select(category)
                .from(category)
                .where(
                        category.division.eq(CategoryDivision.SHARED),
                        category.status.eq(CategoryStatus.REGISTER),
                        category.parent.isNull()
                )
                .fetch();
    }
}
