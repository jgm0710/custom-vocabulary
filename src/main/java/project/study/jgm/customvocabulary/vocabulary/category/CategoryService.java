package project.study.jgm.customvocabulary.vocabulary.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRepository;
import project.study.jgm.customvocabulary.members.exception.MemberNotFoundException;
import project.study.jgm.customvocabulary.vocabulary.Vocabulary;
import project.study.jgm.customvocabulary.vocabulary.VocabularyRepository;
import project.study.jgm.customvocabulary.vocabulary.category.dto.CategoryUpdateDto;
import project.study.jgm.customvocabulary.vocabulary.category.dto.PersonalCategoryCreateDto;
import project.study.jgm.customvocabulary.vocabulary.category.dto.SharedCategoryResponseDto;
import project.study.jgm.customvocabulary.vocabulary.category.dto.SharedCategoryCreateDto;
import project.study.jgm.customvocabulary.vocabulary.category.exception.CategoryExistsInTheCorrespondingOrdersException;
import project.study.jgm.customvocabulary.vocabulary.category.exception.CategoryNotFoundException;
import project.study.jgm.customvocabulary.vocabulary.category.exception.ExistSubCategoryException;
import project.study.jgm.customvocabulary.vocabulary.category.exception.ParentNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private final CategoryQueryRepository categoryQueryRepository;

    private final MemberRepository memberRepository;

    private final VocabularyRepository vocabularyRepository;

    /**
     * Personal
     */
    @Transactional
    public Category addPersonalCategory(Long memberId, PersonalCategoryCreateDto createDto) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        Category parent = null;
        if (createDto.getParentId() != null) {
            parent = categoryRepository.findById(createDto.getParentId()).orElseThrow(() -> new ParentNotFoundException(createDto.getParentId()));
        }
        Category findCategory = categoryQueryRepository.findByParentIdAndOrders(createDto.getParentId(), createDto.getOrders(), CategoryDivision.PERSONAL);
        if (findCategory!=null) {
            throw new CategoryExistsInTheCorrespondingOrdersException(createDto.getOrders());
        }
        Category category = Category.createPersonalCategory(member, createDto.getName(), parent, createDto.getOrders());
        categoryRepository.save(category);

        return category;
    }

    public List<Category> getPersonalCategoryList(Long memberId) {
        return categoryQueryRepository.findAllByMember(memberId);
    }

    /**
     * Common
     */

    @Transactional
    public void modifyCategory(Long categoryId, CategoryUpdateDto updateDto) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
        Category parent = null;
        if (updateDto.getParentId() != null) {
            parent=categoryRepository.findById(updateDto.getParentId()).orElseThrow(() -> new ParentNotFoundException(updateDto.getParentId()));
        }
        category.updateCategory(updateDto.getName(), parent, updateDto.getOrders());
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        List<Vocabulary> findVocabularyList = vocabularyRepository.findByCategoryId(categoryId);
        for (Vocabulary vocabulary : findVocabularyList) {
            vocabulary.deleteCategory();
        }
        Category findCategory = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
        System.out.println("여기 걸림?");
        if (!findCategory.getChildren().isEmpty()) {
            throw new ExistSubCategoryException();
        }
        categoryRepository.delete(findCategory);
    }

    /**
     * shared
     */
    @Transactional
    public Category createSharedCategory(SharedCategoryCreateDto createDto) {
        Category parent = null;
        if (createDto.getParentId() != null) {
            parent = categoryRepository.findById(createDto.getParentId()).orElseThrow(() -> new ParentNotFoundException(createDto.getParentId()));
        }
        Category findCategory = categoryQueryRepository.findByParentIdAndOrders(createDto.getParentId(), createDto.getOrders(), CategoryDivision.SHARED);
        if (findCategory!=null) {
            throw new CategoryExistsInTheCorrespondingOrdersException(createDto.getOrders());
        }
        Category category = Category.createSharedCategory(createDto.getName(), parent, createDto.getOrders());
        categoryRepository.save(category);

        return category;
    }

    public List<Category> getSharedCategoryList() {
        return categoryQueryRepository.findAllSharedCategory();
    }
}
