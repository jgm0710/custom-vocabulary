package project.study.jgm.customvocabulary.api;

import com.querydsl.core.QueryResults;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import project.study.jgm.customvocabulary.common.dto.CriteriaDto;
import project.study.jgm.customvocabulary.common.dto.ListResponseDto;
import project.study.jgm.customvocabulary.common.dto.PaginationDto;
import project.study.jgm.customvocabulary.common.dto.ResponseDto;
import project.study.jgm.customvocabulary.common.exception.ExistLikeException;
import project.study.jgm.customvocabulary.common.exception.NoExistLikeException;
import project.study.jgm.customvocabulary.common.exception.SelfLikeException;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRole;
import project.study.jgm.customvocabulary.members.exception.MemberNotFoundException;
import project.study.jgm.customvocabulary.security.CurrentUser;
import project.study.jgm.customvocabulary.vocabulary.Vocabulary;
import project.study.jgm.customvocabulary.vocabulary.VocabularyDivision;
import project.study.jgm.customvocabulary.vocabulary.VocabularyService;
import project.study.jgm.customvocabulary.vocabulary.category.exception.CategoryNotFoundException;
import project.study.jgm.customvocabulary.vocabulary.dto.*;
import project.study.jgm.customvocabulary.vocabulary.exception.*;
import project.study.jgm.customvocabulary.vocabulary.like.VocabularyLikeService;
import project.study.jgm.customvocabulary.vocabulary.upload.exception.VocabularyThumbnailImageFileNotFoundException;
import project.study.jgm.customvocabulary.vocabulary.word.Word;
import project.study.jgm.customvocabulary.vocabulary.word.dto.OnlyWordRequestListDto;
import project.study.jgm.customvocabulary.vocabulary.word.dto.WordResponseDto;
import project.study.jgm.customvocabulary.vocabulary.word.upload.exception.WordImageFileNotFoundException;

import javax.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static project.study.jgm.customvocabulary.common.dto.MessageVo.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/vocabulary")
public class VocabularyApiController {

    private final VocabularyService vocabularyService;

    private final VocabularyLikeService vocabularyLikeService;

    private final ModelMapper modelMapper;

    /**
     * Personal
     */

    @PostMapping("/personal")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> addPersonalVocabulary(
            @RequestBody @Valid VocabularyCreateDto createDto,
            Errors errors,
            @CurrentUser Member member
    ) {

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        try {

            Vocabulary savedVocabulary = vocabularyService.addPersonalVocabulary(member.getId(), createDto.getCategoryId(), createDto);
            PersonalVocabularyDetailDto personalVocabularyDetailDto = PersonalVocabularyDetailDto.personalVocabularyToDetail(savedVocabulary, modelMapper);
            URI getVocabularyUri = linkTo(VocabularyApiController.class).slash("personal").slash(savedVocabulary.getId()).toUri();

            return ResponseEntity.created(getVocabularyUri)
                    .body(new ResponseDto<>(personalVocabularyDetailDto, ADD_VOCABULARY_SUCCESSFULLY));

        } catch (MemberNotFoundException | CategoryNotFoundException | VocabularyThumbnailImageFileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        } catch (DivisionMismatchException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }
    }

    @PutMapping("/personal/words/{vocabularyId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> updateWordListOfPersonalVocabulary(
            @PathVariable Long vocabularyId,
            @RequestBody @Valid OnlyWordRequestListDto onlyWordRequestListDto,
            Errors errors,
            @CurrentUser Member member
    ) {

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            vocabularyService.updateWordListOfPersonalVocabulary(vocabularyId, onlyWordRequestListDto.getWordList());

            Vocabulary findVocabulary = vocabularyService.getVocabulary(vocabularyId);

            if (!findVocabulary.getProprietor().getId().equals(member.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>(UPDATE_WORD_LIST_OF_VOCABULARY_OF_DIFFERENT_MEMBER));
            }
            PersonalVocabularyDetailDto personalVocabularyDetailDto = PersonalVocabularyDetailDto.personalVocabularyToDetail(findVocabulary, modelMapper);

            return ResponseEntity.ok(new ResponseDto<>(personalVocabularyDetailDto, UPDATE_WORD_LIST_OF_PERSONAL_VOCABULARY_SUCCESSFULLY));

        } catch (VocabularyNotFoundException | WordImageFileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        } catch (BadRequestByDivision e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }
    }

    @PutMapping("/personal/memorized/{vocabularyId}/{wordId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> checkMemorise(
            @PathVariable Long vocabularyId,
            @PathVariable Long wordId,
            @CurrentUser Member member
    ) {

        try {

            Vocabulary findVocabulary = vocabularyService.getVocabulary(vocabularyId);

            if (!findVocabulary.getProprietor().getId().equals(member.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>(CHECK_MEMORIZE_OF_VOCABULARY_OF_DIFFERENT_MEMBER));
            }
            vocabularyService.checkMemorise(wordId);
            Word word = vocabularyService.getWord(wordId);
            WordResponseDto wordResponseDto = WordResponseDto.wordToResponse(word, modelMapper);
            return ResponseEntity.ok(new ResponseDto<>(wordResponseDto, CHECK_MEMORIZE_SUCCESSFULLY));

        } catch (VocabularyNotFoundException | WordNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        } catch (BadRequestByDivision e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }
    }

    @PutMapping("/personal/{vocabularyId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> modifyPersonalVocabulary(
            @PathVariable Long vocabularyId,
            @RequestBody @Valid VocabularyUpdateDto vocabularyUpdateDto,
            Errors errors,
            @CurrentUser Member member
    ) {

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            Vocabulary findVocabulary = vocabularyService.getVocabulary(vocabularyId);

            if (!findVocabulary.getProprietor().getId().equals(member.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>(MODIFY_PERSONAL_VOCABULARY_OF_DIFFERENT_MEMBER));
            }
            vocabularyService.modifyPersonalVocabulary(vocabularyId, vocabularyUpdateDto);
            PersonalVocabularyDetailDto personalVocabularyDetailDto = PersonalVocabularyDetailDto.personalVocabularyToDetail(findVocabulary, modelMapper);

            return ResponseEntity.ok(new ResponseDto<>(personalVocabularyDetailDto, MODIFY_PERSONAL_VOCABULARY_SUCCESSFULLY));

        } catch (VocabularyNotFoundException | VocabularyThumbnailImageFileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        } catch (BadRequestByDivision e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }
    }

    @PostMapping("/shared/{vocabularyId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<? extends ResponseDto<?>> sharePersonalVocabulary(
            @PathVariable Long vocabularyId,
            @RequestParam(required = false) Long categoryId,
            @CurrentUser Member member
    ) {

        try {

            Vocabulary findVocabulary = vocabularyService.getVocabulary(vocabularyId);

            if (!findVocabulary.getProprietor().getId().equals(member.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>(SHARE_VOCABULARY_OF_DIFFERENT_MEMBER));
            }
            Vocabulary sharedVocabulary = vocabularyService.share(vocabularyId, categoryId);
            SharedVocabularyDetailDto sharedVocabularyDetailDto = SharedVocabularyDetailDto.sharedVocabularyToDetail(sharedVocabulary, modelMapper);
            sharedVocabularyDetailDto.setAllowModificationAndDeletion(true);
            URI getSharedVocabularyUri = linkTo(VocabularyApiController.class).slash("shared").slash(sharedVocabulary.getId()).toUri();

            return ResponseEntity.created(getSharedVocabularyUri)
                    .body(new ResponseDto<>(sharedVocabularyDetailDto, SHARE_VOCABULARY_SUCCESSFULLY));
        } catch (VocabularyNotFoundException | CategoryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        } catch (BadRequestByDivision e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }
    }

    @GetMapping("/personal/{memberId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> getPersonalVocabularyList(
            @PathVariable Long memberId,
            @ModelAttribute @Valid CriteriaDto criteriaDto,
            BindingResult bindingResult,
            @RequestParam(required = false) Long categoryId,
            @CurrentUser Member member
    ) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult);
        }

        if (!member.getRoles().contains(MemberRole.ADMIN)) {
            if (!member.getId().equals(memberId)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>(GET_PERSONAL_VOCABULARY_LIST_OF_DIFFERENT_MEMBER));
            }
        }

        try {
            QueryResults<Vocabulary> results = vocabularyService.getVocabularyListByMember(criteriaDto, memberId, categoryId, VocabularyDivision.PERSONAL, VocabularyDivision.COPIED);
            List<Vocabulary> findVocabularyList = results.getResults();

            List<PersonalVocabularySimpleDto> personalVocabularySimpleDtos = new ArrayList<>();
            for (Vocabulary findVocabulary : findVocabularyList) {
                PersonalVocabularySimpleDto personalVocabularySimpleDto = PersonalVocabularySimpleDto.personalVocabularyToSimple(findVocabulary, modelMapper);
                personalVocabularySimpleDtos.add(personalVocabularySimpleDto);
            }

            long totalCount = results.getTotal();

            PaginationDto paginationDto = new PaginationDto(totalCount, criteriaDto);

            ListResponseDto<Object> listResponseDto = ListResponseDto.builder()
                    .list(personalVocabularySimpleDtos)
                    .paging(paginationDto)
                    .build();

            return ResponseEntity.ok(new ResponseDto<>(listResponseDto, GET_PERSONAL_VOCABULARY_LIST_SUCCESSFULLY));

        } catch (CategoryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        } catch (BadRequestByDivision | MemberAndCategoryMemberDifferentException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }
    }

    @DeleteMapping("/personal/{vocabularyId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ResponseDto<Object>> deletePersonalVocabulary(
            @PathVariable Long vocabularyId,
            @CurrentUser Member member
    ) {

        try {
            Vocabulary findVocabulary = vocabularyService.getVocabulary(vocabularyId);
            if (!findVocabulary.getProprietor().getId().equals(member.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>(DELETE_PERSONAL_VOCABULARY_OF_DIFFERENT_MEMBER));
            }
            vocabularyService.deletePersonalVocabulary(vocabularyId);

            return ResponseEntity.ok(new ResponseDto<>(DELETE_PERSONAL_VOCABULARY_SUCCESSFULLY));

        } catch (VocabularyNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        } catch (BadRequestByDivision e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }
    }


    /**
     * Common
     */

    @PutMapping("/belongedCategory/{vocabularyId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<? extends ResponseDto<?>> moveCategory(
            @PathVariable Long vocabularyId,
            @RequestParam(required = false) Long categoryId,
            @CurrentUser Member member
    ) {


        try {

            Vocabulary findVocabulary = vocabularyService.getVocabulary(vocabularyId);
            decreaseViewWhenGettingSharedVocabulary(findVocabulary);

            if (!findVocabulary.getProprietor().getId().equals(member.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>(MOVE_CATEGORY_OF_VOCABULARY_OF_DIFFERENT_MEMBER));
            }

            vocabularyService.moveCategory(vocabularyId, categoryId);

            if (findVocabulary.getDivision() == VocabularyDivision.PERSONAL || findVocabulary.getDivision() == VocabularyDivision.COPIED) {
                PersonalVocabularyDetailDto personalVocabularyDetailDto = PersonalVocabularyDetailDto.personalVocabularyToDetail(findVocabulary, modelMapper);
                return ResponseEntity.ok(new ResponseDto<>(personalVocabularyDetailDto, MOVE_CATEGORY_OF_PERSONAL_VOCABULARY_SUCCESSFULLY));
            } else { //findVocabulary.getDivision() == SHARED
                SharedVocabularyDetailDto sharedVocabularyDetailDto = SharedVocabularyDetailDto.sharedVocabularyToDetail(findVocabulary, modelMapper);
                sharedVocabularyDetailDto.setAllowModificationAndDeletion(true);
                return ResponseEntity.ok(new ResponseDto<>(sharedVocabularyDetailDto, MOVE_CATEGORY_OF_SHARED_VOCABULARY_SUCCESSFULLY));
            }

        } catch (VocabularyNotFoundException | CategoryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        } catch (DoNotMoveException | DivisionMismatchException |
                MemberMismatchAfterMovingWithCurrentMemberException | BadRequestByDivision e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }
    }

    private void decreaseViewWhenGettingSharedVocabulary(Vocabulary findVocabulary) {
        if (findVocabulary.getDivision() == VocabularyDivision.SHARED) {
            findVocabulary.decreaseViews();
        }
    }

    @GetMapping("/{vocabularyId}")
    public ResponseEntity<? extends ResponseDto<?>> getVocabulary(
            @PathVariable Long vocabularyId,
            @CurrentUser Member member
    ) {

        try {

            Vocabulary findVocabulary = vocabularyService.getVocabulary(vocabularyId);
            PersonalVocabularyDetailDto personalVocabularyDetailDto;
            SharedVocabularyDetailDto sharedVocabularyDetailDto;

            switch (findVocabulary.getDivision()) {
                case COPIED:
                case PERSONAL:
                    if (member == null) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>(ACCESS_TO_SENSITIVE_VOCABULARY_BY_UNAUTHORIZED_USER));
                    }
                    if (!member.getRoles().contains(MemberRole.ADMIN)) {
                        if (!findVocabulary.getProprietor().getId().equals(member.getId())) {
                            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>(GET_PERSONAL_VOCABULARY_OF_DIFFERENT_MEMBER));
                        }
                    }
                    personalVocabularyDetailDto = PersonalVocabularyDetailDto.personalVocabularyToDetail(findVocabulary, modelMapper);
                    return ResponseEntity.ok(new ResponseDto<>(personalVocabularyDetailDto, GET_PERSONAL_VOCABULARY_SUCCESSFULLY));

                case DELETE:
                    if (member == null) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>(ACCESS_TO_SENSITIVE_VOCABULARY_BY_UNAUTHORIZED_USER));
                    }
                    if (!member.getRoles().contains(MemberRole.ADMIN)) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>(GET_DELETED_VOCABULARY_BY_USER));
                    } else {
                        personalVocabularyDetailDto = PersonalVocabularyDetailDto.personalVocabularyToDetail(findVocabulary, modelMapper);
                        return ResponseEntity.ok(new ResponseDto<>(personalVocabularyDetailDto, GET_DELETED_VOCABULARY_BY_ADMIN));
                    }

                case SHARED:
                    sharedVocabularyDetailDto = SharedVocabularyDetailDto.sharedVocabularyToDetail(findVocabulary, modelMapper);
                    if (member != null) {
                        if (member.getId().equals(findVocabulary.getWriter().getId())) {
                            sharedVocabularyDetailDto.setAllowModificationAndDeletion(true);
                            sharedVocabularyDetailDto.setViewLike(false);
                        } else {
                            final boolean existLike = vocabularyLikeService.getExistLike(member.getId(), findVocabulary.getId());
                            sharedVocabularyDetailDto.setLike(existLike);
                        }
                    }
                    return ResponseEntity.ok(new ResponseDto<>(sharedVocabularyDetailDto, GET_SHARED_VOCABULARY_SUCCESSFULLY));

                case UNSHARED:
                    if (member == null) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>(ACCESS_TO_SENSITIVE_VOCABULARY_BY_UNAUTHORIZED_USER));
                    }
                    if (!member.getRoles().contains(MemberRole.ADMIN)) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>(GET_UNSHARED_VOCABULARY_BY_USER));
                    } else {
                        sharedVocabularyDetailDto = SharedVocabularyDetailDto.sharedVocabularyToDetail(findVocabulary, modelMapper);
                        return ResponseEntity.ok(new ResponseDto<>(sharedVocabularyDetailDto, GET_UNSHARED_VOCABULARY_BY_ADMIN));
                    }
                default:
                    return ResponseEntity.status(500).body(new ResponseDto<>("해당 구분의 단어장은 존재하지 않습니다."));
            }

        } catch (VocabularyNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        }
    }

    /**
     * Shared
     */

    @GetMapping("/shared/{memberId}")
    public ResponseEntity<?> getSharedVocabularyListByMember(
            @PathVariable Long memberId,
            @ModelAttribute @Valid CriteriaDto criteriaDto,
            BindingResult bindingResult
    ) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult);
        }

        QueryResults<Vocabulary> results = vocabularyService.getVocabularyListByMember(criteriaDto, memberId, null, VocabularyDivision.SHARED);

        List<Vocabulary> findVocabularyList = results.getResults();
        List<SharedVocabularySimpleDto> sharedVocabularySimpleDtos = new ArrayList<>();
        for (Vocabulary findVocabulary : findVocabularyList) {
            SharedVocabularySimpleDto sharedVocabularySimpleDto = SharedVocabularySimpleDto.sharedVocabularyToSimple(findVocabulary, modelMapper);
            sharedVocabularySimpleDtos.add(sharedVocabularySimpleDto);
        }

        long totalCount = results.getTotal();
        PaginationDto paginationDto = new PaginationDto(totalCount, criteriaDto);

        ListResponseDto<Object> listResponseDto = ListResponseDto.builder()
                .list(sharedVocabularySimpleDtos)
                .paging(paginationDto)
                .build();

        return ResponseEntity.ok(new ResponseDto<>(listResponseDto, GET_SHARED_VOCABULARY_LIST_BY_MEMBER_SUCCESSFULLY));
    }

    @GetMapping("/shared")
    public ResponseEntity<?> getSharedVocabularyList(
            @RequestBody @Valid SharedVocabularySearchDto searchDto,
            Errors errors
    ) {

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

         QueryResults<Vocabulary> results = vocabularyService.getVocabularyListByShared(searchDto.getCriteriaDto(), searchDto.getCategoryId(), searchDto.getTitle(), searchDto.getSortCondition());
         List<Vocabulary> sharedVocabularyList = results.getResults();
         long totalCount = results.getTotal();

        List<SharedVocabularySimpleDto> sharedVocabularySimpleDtos = new ArrayList<>();
        for (Vocabulary sharedVocabulary : sharedVocabularyList) {
             SharedVocabularySimpleDto sharedVocabularySimpleDto = SharedVocabularySimpleDto.sharedVocabularyToSimple(sharedVocabulary, modelMapper);
            sharedVocabularySimpleDtos.add(sharedVocabularySimpleDto);
        }

         PaginationDto paginationDto = new PaginationDto(totalCount, searchDto.getCriteriaDto());

         ListResponseDto<Object> listResponseDto = ListResponseDto.builder()
                .list(sharedVocabularySimpleDtos)
                .paging(paginationDto)
                .build();

        return ResponseEntity.ok(new ResponseDto<>(listResponseDto, GET_SHARED_VOCABULARY_LIST_SUCCESSFULLY));
    }

    @PostMapping("/shared/download/{vocabularyId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<? extends ResponseDto<?>> downloadSharedVocabulary(
            @PathVariable Long vocabularyId,
            @RequestParam(required = false) Long categoryId,
            @CurrentUser Member member
    ) {

        try {
             Vocabulary downloadVocabulary = vocabularyService.download(vocabularyId, member.getId(), categoryId);
             PersonalVocabularyDetailDto personalVocabularyDetailDto = PersonalVocabularyDetailDto.personalVocabularyToDetail(downloadVocabulary, modelMapper);

            return ResponseEntity.ok(new ResponseDto<>(personalVocabularyDetailDto, DOWNLOAD_SHARED_VOCABULARY_SUCCESSFULLY));

        } catch (VocabularyNotFoundException | CategoryNotFoundException | MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        } catch (BadRequestByDivision | MemberAndCategoryMemberDifferentException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }
    }

    @DeleteMapping("/shared/{vocabularyId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ResponseDto<Object>> unsharedSharedVocabulary(
            @PathVariable Long vocabularyId,
            @CurrentUser Member member
    ) {

        try {
             Vocabulary findVocabulary = vocabularyService.getVocabulary(vocabularyId);
            decreaseViewWhenGettingSharedVocabulary(findVocabulary);

            if (!findVocabulary.getWriter().getId().equals(member.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>(UNSHARED_SHARED_VOCABULARY_OF_DIFFERENT_MEMBER));
            }

            vocabularyService.unshared(vocabularyId);

            return ResponseEntity.ok(new ResponseDto<>(UNSHARED_SHARED_VOCABULARY_SUCCESSFULLY));

        } catch (VocabularyNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        } catch (BadRequestByDivision e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }
    }

    @PostMapping("/shared/like/{vocabularyId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ResponseDto<Object>> addLikeToSharedVocabulary(
            @PathVariable Long vocabularyId,
            @CurrentUser Member member
    ) {

        try {
            vocabularyLikeService.like(member.getId(), vocabularyId);
            return ResponseEntity.ok(new ResponseDto<>(ADD_LIKE_TO_SHARED_VOCABULARY_SUCCESSFULLY));

        } catch (MemberNotFoundException | VocabularyNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        } catch (BadRequestByDivision | ExistLikeException | SelfLikeException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }
    }

    @DeleteMapping("/shared/like/{vocabularyId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ResponseDto<Object>> unlikeSharedVocabulary(
            @PathVariable Long vocabularyId,
            @CurrentUser Member member
    ) {

        try {
            vocabularyLikeService.unLike(member.getId(), vocabularyId);
            return ResponseEntity.ok(new ResponseDto<>(UNLIKE_SHARED_VOCABULARY_SUCCESSFULLY));
        } catch (NoExistLikeException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }
    }

    /**
     * Admin
     */

    @GetMapping("/deleted/{memberId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getDeletedVocabularyListByMember(
            @PathVariable Long memberId,
            @ModelAttribute @Valid CriteriaDto criteriaDto,
            BindingResult bindingResult,
            @RequestParam(required = false) Long categoryId
    ) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult);
        }

        try {
             QueryResults<Vocabulary> results = vocabularyService.getVocabularyListByMember(criteriaDto, memberId, categoryId, VocabularyDivision.DELETE);
             List<Vocabulary> deletedVocabularyList = results.getResults();
             long totalCount = results.getTotal();

            List<PersonalVocabularySimpleDto> personalVocabularySimpleDtos = new ArrayList<>();
            for (Vocabulary deletedVocabulary : deletedVocabularyList) {
                 PersonalVocabularySimpleDto personalVocabularySimpleDto = PersonalVocabularySimpleDto.personalVocabularyToSimple(deletedVocabulary, modelMapper);
                personalVocabularySimpleDtos.add(personalVocabularySimpleDto);
            }

             PaginationDto paginationDto = new PaginationDto(totalCount, criteriaDto);
             ListResponseDto<Object> listResponseDto = ListResponseDto.builder()
                    .list(personalVocabularySimpleDtos)
                    .paging(paginationDto)
                    .build();

            return ResponseEntity.ok(new ResponseDto<>(listResponseDto, GET_DELETED_VOCABULARY_LIST_OF_MEMBER_SUCCESSFULLY));

        } catch (CategoryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        } catch (BadRequestByDivision | MemberAndCategoryMemberDifferentException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }

    }

    @GetMapping("/unshared/{memberId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getUnsharedVocabularyListByMember(
            @PathVariable Long memberId,
            @ModelAttribute @Valid CriteriaDto criteriaDto,
            BindingResult bindingResult
    ) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult);
        }

        try {

             QueryResults<Vocabulary> results = vocabularyService.getVocabularyListByMember(criteriaDto, memberId, null, VocabularyDivision.UNSHARED);
             List<Vocabulary> unsharedVocabularyList = results.getResults();
             long totalCount = results.getTotal();

            List<SharedVocabularySimpleDto> sharedVocabularySimpleDtos = new ArrayList<>();
            for (Vocabulary unsharedVocabulary : unsharedVocabularyList) {
                 SharedVocabularySimpleDto sharedVocabularySimpleDto = SharedVocabularySimpleDto.sharedVocabularyToSimple(unsharedVocabulary, modelMapper);
                sharedVocabularySimpleDtos.add(sharedVocabularySimpleDto);
            }

             PaginationDto paginationDto = new PaginationDto(totalCount, criteriaDto);
             ListResponseDto<Object> listResponseDto = ListResponseDto.builder()
                    .list(sharedVocabularySimpleDtos)
                    .paging(paginationDto)
                    .build();

            return ResponseEntity.ok(new ResponseDto<>(listResponseDto, GET_UNSHARED_VOCABULARY_LIST_OF_MEMBER_SUCCESSFULLY));

        } catch (CategoryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        } catch (BadRequestByDivision | MemberAndCategoryMemberDifferentException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }
    }
}
