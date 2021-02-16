package project.study.jgm.customvocabulary.api;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import project.study.jgm.customvocabulary.common.dto.ResponseDto;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRole;
import project.study.jgm.customvocabulary.members.exception.MemberNotFoundException;
import project.study.jgm.customvocabulary.security.CurrentUser;
import project.study.jgm.customvocabulary.vocabulary.VocabularyQueryRepository;
import project.study.jgm.customvocabulary.vocabulary.category.Category;
import project.study.jgm.customvocabulary.vocabulary.category.CategoryService;
import project.study.jgm.customvocabulary.vocabulary.category.dto.CategoryResponseDto;
import project.study.jgm.customvocabulary.vocabulary.category.dto.CategoryUpdateDto;
import project.study.jgm.customvocabulary.vocabulary.category.dto.CategoryCreateDto;
import project.study.jgm.customvocabulary.vocabulary.category.exception.*;

import javax.validation.Valid;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static project.study.jgm.customvocabulary.common.dto.MessageVo.*;
import static project.study.jgm.customvocabulary.common.dto.MessageVo.ADD_PERSONAL_CATEGORY_SUCCESSFULLY;
import static project.study.jgm.customvocabulary.common.dto.MessageVo.GET_PERSONAL_CATEGORY_LIST_SUCCESSFULLY;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vocabulary/category")
public class CategoryApiController {

    private final CategoryService categoryService;

    private final VocabularyQueryRepository vocabularyQueryRepository;

    private final ModelMapper modelMapper;

    /**
     * Personal
     */

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity addPersonalCategory(
            @RequestBody @Valid CategoryCreateDto createDto,
            Errors errors,
            @CurrentUser Member member
    ) {

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        Category savedCategory = null;

        try {
            savedCategory = categoryService.addPersonalCategory(member.getId(), createDto);
        } catch (ParentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        } catch (CategoryExistsInTheCorrespondingOrdersException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        } catch (MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>(e.getMessage()));
        } catch (DivisionBetweenParentAndChildIsDifferentException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        } catch (ParentBelongToOtherMembersException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>(e.getMessage()));
        }

        CategoryResponseDto categoryResponseDto = CategoryResponseDto.categoryToResponseDto(savedCategory, modelMapper);
        URI categoryListLinkOfThisMember = linkTo(CategoryApiController.class).slash(member.getId()).toUri();

        return ResponseEntity.created(categoryListLinkOfThisMember)
                .body(new ResponseDto<>(categoryResponseDto, ADD_PERSONAL_CATEGORY_SUCCESSFULLY));
    }

    @GetMapping("/{memberId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity getPersonalCategoryList(
            @PathVariable("memberId") Long memberId,
            @CurrentUser Member member
    ) {

        if (!member.getRoles().contains(MemberRole.ADMIN)) {
            if (!memberId.equals(member.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ResponseDto<>(GET_PERSONAL_CATEGORY_LIST_OF_DIFFERENT_MEMBER));
            }
        }
        List<Category> findList = categoryService.getPersonalCategoryList(memberId);
        List<CategoryResponseDto> categoryResponseDtos = CategoryResponseDto.categoryListToResponseList(findList);

        long countOfPersonalVocabularyWhereCategoryIsNull = vocabularyQueryRepository
                .getCountOfPersonalVocabularyWhereCategoryIsNull(memberId);
        CategoryResponseDto other = new CategoryResponseDto(0L, "기타", null, null, countOfPersonalVocabularyWhereCategoryIsNull, 0);
        categoryResponseDtos.add(other);

        return ResponseEntity.ok(new ResponseDto<>(categoryResponseDtos, GET_PERSONAL_CATEGORY_LIST_SUCCESSFULLY));
    }


    /**
     * Common
     */

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

        Category findCategory = null;

        try {

            findCategory = categoryService.getCategory(categoryId);

            if (findCategory.getMember() != null) {
                if (!findCategory.getMember().getId().equals(member.getId())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new ResponseDto<>(MODIFY_CATEGORY_OF_DIFFERENT_MEMBER));
                }
            } else {    //findCategory.getMember == null -> Shared Category
                if (!member.getRoles().contains(MemberRole.ADMIN)) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new ResponseDto<>(MODIFY_SHARED_CATEGORY_BY_USER));
                }
            }
        } catch (CategoryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        }

        try {
            categoryService.modifyCategory(categoryId, categoryUpdateDto);
        } catch (ParentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        } catch (CategoryExistsInTheCorrespondingOrdersException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        } catch (DivisionBetweenParentAndChildIsDifferentException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        } catch (ParentBelongToOtherMembersException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>(e.getMessage()));
        }

        CategoryResponseDto categoryResponseDto = CategoryResponseDto.categoryToResponseDto(findCategory, modelMapper);

        return ResponseEntity.ok(new ResponseDto<>(categoryResponseDto, MODIFY_CATEGORY_SUCCESSFULLY));
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity deleteCategory(
            @PathVariable("categoryId") Long categoryId,
            @CurrentUser Member member
    ) {

        try {
            Category findCategory = categoryService.getCategory(categoryId);
            if (!findCategory.getMember().getId().equals(member.getId())) { //카테고리 생성회원가 인증된 회원이 다르면
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ResponseDto<>(DELETE_CATEGORY_OF_DIFFERENT_MEMBER));
            }
        } catch (CategoryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        }

        try {
            categoryService.deleteCategory(categoryId);

        } catch (ExistSubCategoryException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }

        return ResponseEntity.ok(new ResponseDto<>(DELETE_CATEGORY_SUCCESSFULLY));
    }

    /**
     * Shared == Admin
     */

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity addShredCategory(
            @RequestBody @Valid CategoryCreateDto createDto,
            Errors errors,
            @CurrentUser Member member
    ) {

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        Category savedCategory = null;
        try {
            savedCategory = categoryService.addSharedCategory(createDto);

        } catch (ParentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDto<>(e.getMessage()));
        } catch (CategoryExistsInTheCorrespondingOrdersException e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto<>(e.getMessage()));
        } catch (DivisionBetweenParentAndChildIsDifferentException e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto<>(e.getMessage()));
        }

        CategoryResponseDto categoryResponseDto = CategoryResponseDto.categoryToResponseDto(savedCategory, modelMapper);
        URI getSharedCategoryListUri = linkTo(CategoryApiController.class).slash("admin").toUri();

        return ResponseEntity.created(getSharedCategoryListUri)
                .body(new ResponseDto<>(categoryResponseDto, ADD_SHARED_CATEGORY_BY_ADMIN_SUCCESSFULLY));
    }


}
