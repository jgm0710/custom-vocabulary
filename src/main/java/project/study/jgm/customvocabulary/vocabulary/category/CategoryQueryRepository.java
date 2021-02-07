package project.study.jgm.customvocabulary.vocabulary.category;

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
                        category.status.eq(CategoryStatus.REGISTER)
                )
                .fetch();
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
                        category.status.eq(CategoryStatus.REGISTER)
                )
                .fetch();
    }
}
