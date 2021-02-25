package project.study.jgm.customvocabulary.api;

import com.querydsl.core.QueryResults;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import project.study.jgm.customvocabulary.common.dto.CriteriaDto;
import project.study.jgm.customvocabulary.common.dto.ListResponseDto;
import project.study.jgm.customvocabulary.common.dto.PaginationDto;
import project.study.jgm.customvocabulary.common.dto.ResponseDto;
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
            if (!findVocabulary.getMember().getId().equals(member.getId())) {
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
            if (!findVocabulary.getMember().getId().equals(member.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CHECK_MEMORIZE_OF_VOCABULARY_OF_DIFFERENT_MEMBER);
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
            if (!findVocabulary.getMember().getId().equals(member.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>("다른 회원의 단어장은 수정할 수 없습니다."));
            }
            vocabularyService.modifyPersonalVocabulary(vocabularyId, vocabularyUpdateDto);

            PersonalVocabularyDetailDto personalVocabularyDetailDto = PersonalVocabularyDetailDto.personalVocabularyToDetail(findVocabulary, modelMapper);

            return ResponseEntity.ok(new ResponseDto<>(personalVocabularyDetailDto, "단어장이 정상적으로 수정되었습니다."));
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
            @RequestParam Long categoryId,
            @CurrentUser Member member
    ) {

        try {

            Vocabulary findVocabulary = vocabularyService.getVocabulary(vocabularyId);
            if (!findVocabulary.getMember().getId().equals(member.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>("다른 회원의 단어장은 공유할 수 없습니다."));
            }
            Vocabulary sharedVocabulary = vocabularyService.share(vocabularyId, categoryId);
            SharedVocabularyDetailDto sharedVocabularyDetailDto = SharedVocabularyDetailDto.sharedVocabularyToDetail(sharedVocabulary, modelMapper);
            URI getSharedVocabularyUri = linkTo(VocabularyApiController.class).slash("shared").slash(sharedVocabulary.getId()).toUri();

            return ResponseEntity.created(getSharedVocabularyUri)
                    .body(new ResponseDto<>(sharedVocabularyDetailDto, "단어장이 성공적으로 공유되었습니다."));
        } catch (VocabularyNotFoundException | CategoryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        } catch (BadRequestByDivision | DivisionMismatchException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }
    }

    @GetMapping("/personal/{memberId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> getPersonalVocabularyList(
            @PathVariable Long memberId,
            @ModelAttribute CriteriaDto criteriaDto,
            @RequestParam Long categoryId,
            @CurrentUser Member member
    ) {

        if (!member.getRoles().contains(MemberRole.ADMIN)) {
            if (!member.getId().equals(memberId)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("다른 회원의 개인 단어장 목록은 조회할 수 없습니다.");
            }
        }

        QueryResults<Vocabulary> results = vocabularyService.getVocabularyListByMember(criteriaDto, VocabularyDivision.PERSONAL, memberId, categoryId);
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

        return ResponseEntity.ok(new ResponseDto<>(listResponseDto, "개인 단어장 목록이 정상적으로 조회되었습니다."));
    }


    /**
     * Common
     */

    @PutMapping("/category/{vocabularyId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<? extends ResponseDto<?>> moveCategory(
            @PathVariable Long vocabularyId,
            @RequestParam Long categoryId,
            @CurrentUser Member member
    ) {


        try {

            Vocabulary findVocabulary = vocabularyService.getVocabulary(vocabularyId);
            if (!findVocabulary.getMember().getId().equals(member.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>("다른 회원의 단어장의 카테고리는 이동시킬 수 없습니다."));
            }

            vocabularyService.moveCategory(vocabularyId, categoryId);

            if (findVocabulary.getDivision() == VocabularyDivision.PERSONAL || findVocabulary.getDivision() == VocabularyDivision.COPIED) {
                PersonalVocabularyDetailDto personalVocabularyDetailDto = PersonalVocabularyDetailDto.personalVocabularyToDetail(findVocabulary, modelMapper);
                return ResponseEntity.ok(new ResponseDto<>(personalVocabularyDetailDto, "개인 단어장의 카테고리가 정상적으로 변경되었습니다."));
            } else { //findVocabulary.getDivision() == SHARED
                SharedVocabularyDetailDto sharedVocabularyDetailDto = SharedVocabularyDetailDto.sharedVocabularyToDetail(findVocabulary, modelMapper);
                return ResponseEntity.ok(new ResponseDto<>(sharedVocabularyDetailDto, "공유 단어장의 카테고리가 정상적으로 변경되었습니다."));
            }

        } catch (VocabularyNotFoundException | CategoryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        } catch (DoNotMoveException | DivisionMismatchException |
                MemberMismatchAfterMovingWithCurrentMemberException | BadRequestByDivision e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }
    }

    @GetMapping("/{vocabularyId}")
    @PreAuthorize("hasRole('ROLE_USER')")
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
                    if (!member.getRoles().contains(MemberRole.ADMIN)) {
                        if (!findVocabulary.getMember().getId().equals(member.getId())) {
                            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>("다른 회원의 개인 단어장은 조회할 수 없습니다."));
                        }
                    }
                    personalVocabularyDetailDto = PersonalVocabularyDetailDto.personalVocabularyToDetail(findVocabulary, modelMapper);
                    return ResponseEntity.ok(new ResponseDto<>(personalVocabularyDetailDto, "개인 단어장이 정상적으로 조회되었습니다."));
                case SHARED:
                    sharedVocabularyDetailDto = SharedVocabularyDetailDto.sharedVocabularyToDetail(findVocabulary, modelMapper);
                    return ResponseEntity.ok(new ResponseDto<>(sharedVocabularyDetailDto, "공유 단어장이 정상적으로 조회되었습니다."));

                case DELETE:
                    if (!member.getRoles().contains(MemberRole.ADMIN)) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>("삭제된 단어장은 조회가 불가능합니다."));
                    } else {
                        personalVocabularyDetailDto = PersonalVocabularyDetailDto.personalVocabularyToDetail(findVocabulary, modelMapper);
                        return ResponseEntity.ok(new ResponseDto<>(personalVocabularyDetailDto, "관리자 권한으로 삭제된 단어장이 조회되었습니다."));
                    }
                case UNSHARED:
                    if (!member.getRoles().contains(MemberRole.ADMIN)) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>("공유가 취소된 단어장은 조회가 불가능합니다."));
                    } else {
                        sharedVocabularyDetailDto = SharedVocabularyDetailDto.sharedVocabularyToDetail(findVocabulary, modelMapper);
                        return ResponseEntity.ok(new ResponseDto<>(sharedVocabularyDetailDto, "관리자 권한으로 공유가 취소된 단어장이 조회되었습니다."));
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
    public ResponseEntity<ResponseDto<ListResponseDto<Object>>> getSharedVocabularyListByMember(
            @PathVariable Long memberId,
            @RequestParam CriteriaDto criteriaDto
    ) {

        QueryResults<Vocabulary> results = vocabularyService.getVocabularyListByMember(criteriaDto, VocabularyDivision.SHARED, memberId, null);

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

        return ResponseEntity.ok(new ResponseDto<>(listResponseDto, "해당 회원이 공유한 단어 목록이 정상적으로 조회되었습니다."));
    }

}
