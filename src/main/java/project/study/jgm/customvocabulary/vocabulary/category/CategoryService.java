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
import project.study.jgm.customvocabulary.vocabulary.category.dto.CategoryCreateDto;
import project.study.jgm.customvocabulary.vocabulary.category.exception.*;

import java.util.List;

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
    public Category addPersonalCategory(Long memberId, CategoryCreateDto createDto) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);

        Category parent = null;
        parent = ifParentIdIsNotNullGetParent(parent, createDto.getParentId());

        checkDivisionBetweenParentAndChild(parent, CategoryDivision.PERSONAL);

        checkMemberOfParent(memberId, parent);

        checkCategoryExistsInTheCorrespondingOrders(memberId, createDto.getParentId(), createDto.getOrders(), CategoryDivision.PERSONAL);

        Category category = Category.createPersonalCategory(member, createDto.getName(), parent, createDto.getOrders());
        categoryRepository.save(category);

        return category;
    }

    private void checkDivisionBetweenParentAndChild(Category parent, CategoryDivision childDivision) {
        if (parent != null) {
            if (parent.getDivision() != childDivision) {
                throw new DivisionBetweenParentAndChildIsDifferentException();
            }
        }
    }

    private void checkMemberOfParent(Long memberId, Category parent) {
        if (parent != null) {
            if (!parent.getMember().getId().equals(memberId)) {
                throw new ParentBelongToOtherMembersException();
            }
        }
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
        parent = ifParentIdIsNotNullGetParent(parent, updateDto.getParentId());

        checkDivisionBetweenParentAndChild(parent, category.getDivision());
        if (category.getDivision() == CategoryDivision.PERSONAL) {
            checkMemberOfParent(category.getMember().getId(), parent);
        }

        if (category.getOrders() != updateDto.getOrders()) {
            Long memberId = null;
            if (parent != null) {
                memberId = parent.getMember().getId();
            }
            checkCategoryExistsInTheCorrespondingOrders(memberId, updateDto.getParentId(), updateDto.getOrders(), category.getDivision());
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
        if (!findCategory.getChildren().isEmpty()) {
            throw new ExistSubCategoryException();
        }
        categoryRepository.delete(findCategory);
    }

    public Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
    }

    /**
     * shared
     */
    @Transactional
    public Category addSharedCategory(CategoryCreateDto createDto) {
        Category parent = null;
        parent = ifParentIdIsNotNullGetParent(parent, createDto.getParentId());

        checkCategoryExistsInTheCorrespondingOrders(null, createDto.getParentId(), createDto.getOrders(), CategoryDivision.SHARED);

        if (parent != null) {
            checkDivisionBetweenParentAndChild(parent, CategoryDivision.SHARED);
        }

        Category category = Category.createSharedCategory(createDto.getName(), parent, createDto.getOrders());
        categoryRepository.save(category);

        return category;
    }

    public List<Category> getSharedCategoryList() {
        return categoryQueryRepository.findAllSharedCategory();
    }

    private void checkCategoryExistsInTheCorrespondingOrders(Long memberId, Long parentId, Integer orders, CategoryDivision personal) {
        Category findCategory = categoryQueryRepository.findByParentIdAndOrders(memberId, parentId, orders, personal);
        if (findCategory != null) {
            throw new CategoryExistsInTheCorrespondingOrdersException(orders);
        }
    }

    private Category ifParentIdIsNotNullGetParent(Category parent, Long parentId) {
        if (parentId != null) {
            parent = categoryRepository.findById(parentId).orElseThrow(() -> new ParentNotFoundException(parentId));
        }
        return parent;
    }
}
