package project.study.jgm.customvocabulary.vocabulary.category;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import project.study.jgm.customvocabulary.common.BaseServiceTest;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.vocabulary.Vocabulary;
import project.study.jgm.customvocabulary.vocabulary.VocabularyRepository;
import project.study.jgm.customvocabulary.vocabulary.category.dto.CategoryUpdateDto;
import project.study.jgm.customvocabulary.vocabulary.category.dto.PersonalCategoryCreateDto;
import project.study.jgm.customvocabulary.vocabulary.category.dto.CategoryResponseDto;
import project.study.jgm.customvocabulary.vocabulary.category.exception.CategoryExistsInTheCorrespondingOrdersException;
import project.study.jgm.customvocabulary.vocabulary.category.exception.CategoryNotFoundException;
import project.study.jgm.customvocabulary.vocabulary.category.exception.ExistSubCategoryException;
import project.study.jgm.customvocabulary.vocabulary.category.exception.ParentNotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CategoryServiceTest extends BaseServiceTest {

    @BeforeEach
    public void setUp() {
        memberRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("개인용 카테고리 생성")
    public void createPersonalCategory() throws Exception {
        //given
        String joinId = "user1";
        String nickname = "user1";
        Member user1 = createUserMember(joinId, nickname);

//        Category sampleCategory = createCategory(user1, CategoryDivision.PERSONAL, "sample category", null, 1, CategoryStatus.REGISTER);

        em.flush();
        em.clear();

        String category_name = "service category";
        PersonalCategoryCreateDto createDto = PersonalCategoryCreateDto.builder()
                .name(category_name)
                .parentId(null)
                .orders(1)
                .build();

        //when
        Category savedCategory = categoryService.addPersonalCategory(user1.getId(), createDto);

        //then
        assertEquals(savedCategory.getMember().getId(), user1.getId());
        assertEquals(savedCategory.getOrders(), 1);
        assertNull(savedCategory.getParent());
        assertEquals(savedCategory.getName(), category_name);

    }

    @Test
    @DisplayName("부모 카테고리가 지정이 됐는데 부모 카테고리를 찾을 수 없는 경우")
    public void createPersonalCategory_ParentNotFound() throws Exception {
        //given
        String joinId = "user1";
        String nickname = "user1";
        Member user1 = createUserMember(joinId, nickname);

        Category sampleCategory = createCategory(user1, CategoryDivision.PERSONAL, "sample category", null, 1, CategoryStatus.REGISTER);

        em.flush();
        em.clear();

        String category_name = "service category";
        PersonalCategoryCreateDto createDto = PersonalCategoryCreateDto.builder()
                .name(category_name)
                .parentId(1000L)
                .orders(1)
                .build();

        //when

        //then
        assertThrows(ParentNotFoundException.class, () -> categoryService.addPersonalCategory(user1.getId(), createDto));

    }

    @Test
    @DisplayName("카테고리 생성 시 지정한 순서에 해당하는 카테고리가 이미 존재하는 경우")
    public void createPersonalCategory_CategoryExistsInTheCorrespondingOrders() throws Exception {
        //given
        String joinId = "user1";
        String nickname = "user1";
        Member user1 = createUserMember(joinId, nickname);

        Category sampleCategory = createCategory(user1, CategoryDivision.PERSONAL, "sample category", null, 1, CategoryStatus.REGISTER);

        em.flush();
        em.clear();

        String category_name = "service category";
        PersonalCategoryCreateDto createDto = PersonalCategoryCreateDto.builder()
                .name(category_name)
                .parentId(null)
                .orders(1)
                .build();

        //when

        //then
        assertThrows(CategoryExistsInTheCorrespondingOrdersException.class, () -> categoryService.addPersonalCategory(user1.getId(), createDto));

    }

    @Test
    @DisplayName("카테고리 수정")
    public void modifyCategory() throws Exception {
        //given
        Member userMember = createUserMember("user1", "user1");
        Category sampleCategory = createCategory(userMember, CategoryDivision.PERSONAL, "sample category", null, 1000, CategoryStatus.REGISTER);

        Category sampleCategory2 = createCategory(userMember, CategoryDivision.PERSONAL, "sample2 category", null, 2, CategoryStatus.REGISTER);

        em.flush();
        em.clear();

        String update_name = "update name";
        int updateOrders = 4;
        CategoryUpdateDto updateDto = CategoryUpdateDto.builder()
                .name(update_name)
                .parentId(sampleCategory2.getId())
                .orders(updateOrders)
                .build();
        //when
        categoryService.modifyCategory(sampleCategory.getId(), updateDto);

        //then
        Category findCategory = categoryRepository.findById(sampleCategory.getId()).get();
        System.out.println("findCategory = " + findCategory);
        System.out.println("findCategory.getParent().toString() = " + findCategory.getParent().toString());
        assertEquals(findCategory.getMember().getId(), userMember.getId());
        assertEquals(findCategory.getName(), update_name);
        assertEquals(updateOrders, findCategory.getOrders());
        assertEquals(findCategory.getParent().getId(), sampleCategory2.getId());

    }

    @Test
    @DisplayName("카테고리 수정 시 수정할 카테고리를 찾을 수 없는 경우")
    public void modifyCategory_NotFound() throws Exception {
        //given
        Member userMember = createUserMember("user1", "user1");
        Category sampleCategory = createCategory(userMember, CategoryDivision.PERSONAL, "sample category", null, 1, CategoryStatus.REGISTER);

        Category sampleCategory2 = createCategory(userMember, CategoryDivision.PERSONAL, "sample2 category", null, 2, CategoryStatus.REGISTER);

        em.flush();
        em.clear();

        String update_name = "update name";
        int updateOrders = 4;
        CategoryUpdateDto updateDto = CategoryUpdateDto.builder()
                .name(update_name)
                .parentId(sampleCategory2.getId())
                .orders(updateOrders)
                .build();

        //when

        //then
        assertThrows(CategoryNotFoundException.class, () -> categoryService.modifyCategory(1000L, updateDto));

    }

    @Test
    @DisplayName("카테고리 수정 시 카테고리를 옮길 부모 카테고리를 찾을 수 없는 경우")
    public void modifyCategory_ParentNotFound() throws Exception {
        //given
        Member userMember = createUserMember("user1", "user1");
        Category sampleCategory = createCategory(userMember, CategoryDivision.PERSONAL, "sample category", null, 1, CategoryStatus.REGISTER);

        Category sampleCategory2 = createCategory(userMember, CategoryDivision.PERSONAL, "sample2 category", null, 2, CategoryStatus.REGISTER);

        em.flush();
        em.clear();

        String update_name = "update name";
        int updateOrders = 4;
        CategoryUpdateDto updateDto = CategoryUpdateDto.builder()
                .name(update_name)
                .parentId(10000L)
                .orders(updateOrders)
                .build();
        //when

        //then
        assertThrows(ParentNotFoundException.class, () -> categoryService.modifyCategory(sampleCategory.getId(), updateDto));
    }

    @Test
    @DisplayName("공유 카테고리 목록 조회 테스트")
    public void getSharedCategoryList() throws Exception {
        //given
        List<Category> categoryList = createCategoryList();

        em.flush();
        em.clear();

        //when
        List<Category> findList = categoryService.getSharedCategoryList();

        //then
        for (Category category : findList) {
            System.out.println("category = " + category);
        }

    }

    private List<Category> createCategoryList() {
        Category sub1 = createCategory(null, CategoryDivision.SHARED, "sub1", null, 0, CategoryStatus.REGISTER);
        Category sub2 = createCategory(null, CategoryDivision.SHARED, "sub2", null, 0, CategoryStatus.REGISTER);
        Category sub11 = createCategory(null, CategoryDivision.SHARED, "sub1-1", sub1, 0, CategoryStatus.REGISTER);
        Category sub12 = createCategory(null, CategoryDivision.SHARED, "sub1-2", sub1, 0, CategoryStatus.REGISTER);
        Category sub21 = createCategory(null, CategoryDivision.SHARED, "sub2-1", sub2, 0, CategoryStatus.REGISTER);
        Category sub22 = createCategory(null, CategoryDivision.SHARED, "sub2-2", sub2, 0, CategoryStatus.REGISTER);

        categoryRepository.save(sub1);
        categoryRepository.save(sub2);
        categoryRepository.save(sub11);
        categoryRepository.save(sub12);
        categoryRepository.save(sub21);
        categoryRepository.save(sub22);

        return List.of(sub1, sub2, sub11, sub12, sub21, sub22);
    }


    @Test
    @DisplayName("목록 조회 PersonalCategoryResponseDto Test")
    public void PersonalCategoryResponseDto_Test() throws Exception {
        //given
        List<Category> categoryList = createCategoryList();
        em.flush();
        em.clear();

        //when
        List<Category> sharedCategoryList = categoryService.getSharedCategoryList();

        List<CategoryResponseDto> categoryResponseDtoList = CategoryResponseDto.categoryListToResponseList(sharedCategoryList);

        //then
        for (CategoryResponseDto categoryResponseDto : categoryResponseDtoList) {
            System.out.println("personalCategoryResponseDto.toString() = " + categoryResponseDto.toString());
        }

    }

    @Test
    @DisplayName("카테고리 삭제")
    public void deleteCategory() throws Exception {
        //given
        Member user = createUserMember("user1", "user1");

        Category sub1 = createCategory(null, CategoryDivision.SHARED, "sub1", null, 0, CategoryStatus.REGISTER);
        Category sub2 = createCategory(null, CategoryDivision.SHARED, "sub2", null, 0, CategoryStatus.REGISTER);
        Category sub11 = createCategory(null, CategoryDivision.SHARED, "sub1-1", sub1, 0, CategoryStatus.REGISTER);
        Category sub12 = createCategory(null, CategoryDivision.SHARED, "sub1-2", sub1, 0, CategoryStatus.REGISTER);
        Category sub21 = createCategory(null, CategoryDivision.SHARED, "sub2-1", sub2, 0, CategoryStatus.REGISTER);
        Category sub22 = createCategory(null, CategoryDivision.SHARED, "sub2-2", sub2, 0, CategoryStatus.REGISTER);

        categoryRepository.save(sub1);
        categoryRepository.save(sub2);
        categoryRepository.save(sub11);
        categoryRepository.save(sub12);
        categoryRepository.save(sub21);
        categoryRepository.save(sub22);

        createVocabularyList(user, sub1);
        createVocabularyList(user, sub2);
        createVocabularyList(user, sub11);
        createVocabularyList(user, sub12);
        createVocabularyList(user, sub21);
        createVocabularyList(user, sub22);

        em.flush();
        em.clear();

        //when

        categoryService.deleteCategory(sub11.getId());
        //then
        List<Vocabulary> results = vocabularyRepository.findAll();
        for (Vocabulary result : results) {
            System.out.println("result = " + result);
        }

    }

    @Test
    @DisplayName("카테고리 삭제 시 자식 카테고리가 있는 경우")
    public void deleteCategory_ExistSubCategory() throws Exception {
        //given
        Member user = createUserMember("user1", "user1");

        Category sub1 = createCategory(null, CategoryDivision.SHARED, "sub1", null, 0, CategoryStatus.REGISTER);
        Category sub2 = createCategory(null, CategoryDivision.SHARED, "sub2", null, 0, CategoryStatus.REGISTER);
        Category sub11 = createCategory(null, CategoryDivision.SHARED, "sub1-1", sub1, 0, CategoryStatus.REGISTER);
        Category sub12 = createCategory(null, CategoryDivision.SHARED, "sub1-2", sub1, 0, CategoryStatus.REGISTER);
        Category sub21 = createCategory(null, CategoryDivision.SHARED, "sub2-1", sub2, 0, CategoryStatus.REGISTER);
        Category sub22 = createCategory(null, CategoryDivision.SHARED, "sub2-2", sub2, 0, CategoryStatus.REGISTER);

        categoryRepository.save(sub1);
        categoryRepository.save(sub2);
        categoryRepository.save(sub11);
        categoryRepository.save(sub12);
        categoryRepository.save(sub21);
        categoryRepository.save(sub22);

        createVocabularyList(user, sub1);
        createVocabularyList(user, sub2);
        createVocabularyList(user, sub11);
        createVocabularyList(user, sub12);
        createVocabularyList(user, sub21);
        createVocabularyList(user, sub22);

        em.flush();
        em.clear();

        //when

        //then
        assertThrows(ExistSubCategoryException.class, () -> categoryService.deleteCategory(sub1.getId()));

    }

    private void createVocabularyList(Member user, Category category) {
        for (int i = 0; i < 5; i++) {
            Vocabulary vocabulary = Vocabulary.builder()
                    .member(user)
                    .category(category)
                    .title("vocabulary" + i)
                    .build();

            vocabularyRepository.save(vocabulary);
        }
    }
}