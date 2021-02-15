package project.study.jgm.customvocabulary.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import project.study.jgm.customvocabulary.common.dto.MessageDto;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRole;
import project.study.jgm.customvocabulary.members.exception.MemberNotFoundException;
import project.study.jgm.customvocabulary.security.CurrentUser;
import project.study.jgm.customvocabulary.vocabulary.VocabularyQueryRepository;
import project.study.jgm.customvocabulary.vocabulary.category.Category;
import project.study.jgm.customvocabulary.vocabulary.category.CategoryService;
import project.study.jgm.customvocabulary.vocabulary.category.dto.CategoryResponseDto;
import project.study.jgm.customvocabulary.vocabulary.category.dto.CategoryResponseDtoComparator;
import project.study.jgm.customvocabulary.vocabulary.category.dto.CategoryUpdateDto;
import project.study.jgm.customvocabulary.vocabulary.category.dto.PersonalCategoryCreateDto;
import project.study.jgm.customvocabulary.vocabulary.category.exception.CategoryExistsInTheCorrespondingOrdersException;
import project.study.jgm.customvocabulary.vocabulary.category.exception.CategoryNotFoundException;
import project.study.jgm.customvocabulary.vocabulary.category.exception.ParentNotFoundException;

import javax.validation.Valid;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vocabulary/category")
public class CategoryApiController {

    private final CategoryService categoryService;

    private final VocabularyQueryRepository vocabularyQueryRepository;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity addPersonalCategory(
            @RequestBody @Valid PersonalCategoryCreateDto createDto,
            Errors errors,
            @CurrentUser Member member
    ) {

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            categoryService.addPersonalCategory(member.getId(), createDto);
        } catch (ParentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageDto(e.getMessage()));
        } catch (CategoryExistsInTheCorrespondingOrdersException e) {
            return ResponseEntity.badRequest().body(new MessageDto(e.getMessage()));
        } catch (MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageDto(e.getMessage()));
        }

        URI categoryListLinkOfThisMember = linkTo(CategoryApiController.class).slash(member.getId()).toUri();
        return ResponseEntity.created(categoryListLinkOfThisMember).body(new MessageDto(MessageDto.ADD_PERSONAL_CATEGORY_SUCCESSFULLY));
    }

    @GetMapping("/{memberId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity getCategoryListByMember(
            @PathVariable("memberId") Long memberId,
            @CurrentUser Member member
    ) {

        if (!member.getRoles().contains(MemberRole.ADMIN)) {
            if (!memberId.equals(member.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageDto(MessageDto.GET_PERSONAL_CATEGORY_LIST_OF_DIFFERENT_MEMBER));
            }
        }
        List<Category> findList = categoryService.getPersonalCategoryList(memberId);
        List<CategoryResponseDto> categoryResponseDtos = CategoryResponseDto.categoryListToResponseList(findList);

        long countOfPersonalVocabularyWhereCategoryIsNull = vocabularyQueryRepository.getCountOfPersonalVocabularyWhereCategoryIsNull(memberId);
        CategoryResponseDto other = new CategoryResponseDto(0L, "기타", null, null, countOfPersonalVocabularyWhereCategoryIsNull, 0);
        categoryResponseDtos.add(other);

        return ResponseEntity.ok(categoryResponseDtos);
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity modifyCategory(
            @PathVariable("categoryId") Long categoryId,
            @RequestBody @Valid CategoryUpdateDto categoryUpdateDto,
            Errors errors,
            @CurrentUser Member member
    ) {

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            Category findCategory = categoryService.getCategory(categoryId);
            if (findCategory.getMember() != null) {
                if (!findCategory.getMember().getId().equals(member.getId())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageDto(MessageDto.MODIFY_CATEGORY_OF_DIFFERENT_MEMBER));
                }
            } else {    //findCategory.getMember == null -> Shared Category
                if (!member.getRoles().contains(MemberRole.ADMIN)) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageDto(MessageDto.MODIFY_SHARED_CATEGORY_BY_USER));
                }
            }
        } catch (CategoryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageDto(e.getMessage()));
        }

        try {
            categoryService.modifyCategory(categoryId, categoryUpdateDto);
        } catch (ParentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageDto(e.getMessage()));
        } catch (CategoryExistsInTheCorrespondingOrdersException e) {
            return ResponseEntity.badRequest().body(new MessageDto(e.getMessage()));
        }

        return ResponseEntity.ok(new MessageDto(MessageDto.MODIFY_CATEGORY_SUCCESSFULLY));
    }
}
