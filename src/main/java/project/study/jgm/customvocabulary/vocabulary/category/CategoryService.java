package project.study.jgm.customvocabulary.vocabulary.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRepository;
import project.study.jgm.customvocabulary.members.exception.MemberNotFoundException;
import project.study.jgm.customvocabulary.vocabulary.category.dto.CategoryUpdateDto;
import project.study.jgm.customvocabulary.vocabulary.category.dto.PersonalCategoryCreateDto;
import project.study.jgm.customvocabulary.vocabulary.category.dto.SharedCategoryCreateDto;
import project.study.jgm.customvocabulary.vocabulary.category.exception.CategoryNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private final CategoryQueryRepository categoryQueryRepository;

    private final MemberRepository memberRepository;

    /**
     * Personal
     */
    @Transactional
    public Category createPersonalCategory(Long memberId, PersonalCategoryCreateDto createDto) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        Category category = Category.createPersonalCategory(member, createDto);
        categoryRepository.save(category);

        return category;
    }

    public List<Category> getPersonalCategoryList(Long memberId) {
        return categoryQueryRepository.findAllByMember(memberId);
    }

    @Transactional
    public void modifyCategory(Long categoryId, CategoryUpdateDto updateDto) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
        category.updateCategory(updateDto);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
        category.changeStatusToDelete();
    }

    /**
     * shared
     */
    @Transactional
    public Category createSharedCategory(SharedCategoryCreateDto createDto) {
        Category category = Category.createSharedCategory(createDto);
        categoryRepository.save(category);

        return category;
    }

    public List<Category> getSharedCategoryList() {
        return categoryQueryRepository.findAllSharedCategory();
    }
}
