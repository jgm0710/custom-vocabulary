package project.study.jgm.customvocabulary.vocabulary.category;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import project.study.jgm.customvocabulary.common.BaseServiceTest;
import project.study.jgm.customvocabulary.members.Member;

import static org.junit.jupiter.api.Assertions.*;

class CategoryQueryRepositoryTest extends BaseServiceTest {

    @BeforeEach
    public void setUp() {
        memberRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("parentId 와 orders 로 category 조회 - parentId 가 null 일 경우")
    public void findByParentIdAndOrders_NullParent() {
        //given
        Member userMember = createUserMember("user1", "user1");

        CategoryDivision division = CategoryDivision.PERSONAL;
        String name = "test category";
        Category parent = null;
        int orders = 1;
        Category category = createCategory(userMember, division, name, parent, orders);

        em.flush();
        em.clear();

        //when
        Category findCategory = categoryQueryRepository.findByParentIdAndOrders(userMember.getId(), null, orders, division);

        //then
        System.out.println("test result findCategory = " + findCategory.toString());
        System.out.println("findCategory.getParent() = " + findCategory.getParent());
    }

    @Test
    @DisplayName("paerntId와 orders 로 category 조회")
    public void findByParentIdAndOrders() {
        //given
        Member userMember = createUserMember("user1", "user1");

        CategoryDivision division = CategoryDivision.PERSONAL;
        String name = "test category";
        int orders = 1;
        Category parent = createCategory(userMember, division, name, null, orders);

        Category child = createCategory(userMember, CategoryDivision.PERSONAL, "child category", parent, 1);

        em.flush();
        em.clear();

        //when
        Category findCategory = categoryQueryRepository.findByParentIdAndOrders(userMember.getId(), parent.getId(), 1, CategoryDivision.PERSONAL);

        //then
        System.out.println("findCategory.toString() = " + findCategory.toString());
        System.out.println("findCategory.getParent() = " + findCategory.getParent());

    }
}