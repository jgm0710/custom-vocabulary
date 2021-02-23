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
import project.study.jgm.customvocabulary.members.exception.MemberNotFoundException;
import project.study.jgm.customvocabulary.security.CurrentUser;
import project.study.jgm.customvocabulary.vocabulary.Vocabulary;
import project.study.jgm.customvocabulary.vocabulary.VocabularyService;
import project.study.jgm.customvocabulary.vocabulary.category.exception.CategoryNotFoundException;
import project.study.jgm.customvocabulary.vocabulary.dto.PersonalVocabularyDetailResponseDto;
import project.study.jgm.customvocabulary.vocabulary.dto.VocabularyCreateDto;
import project.study.jgm.customvocabulary.vocabulary.exception.BadRequestByDivision;
import project.study.jgm.customvocabulary.vocabulary.exception.DivisionMismatchException;
import project.study.jgm.customvocabulary.vocabulary.exception.VocabularyNotFoundException;
import project.study.jgm.customvocabulary.vocabulary.like.VocabularyLikeService;
import project.study.jgm.customvocabulary.vocabulary.upload.VocabularyFileStorageService;
import project.study.jgm.customvocabulary.vocabulary.upload.exception.VocabularyThumbnailImageFileNotFoundException;
import project.study.jgm.customvocabulary.vocabulary.word.dto.OnlyWordRequestListDto;
import project.study.jgm.customvocabulary.vocabulary.word.upload.exception.WordImageFileNotFoundException;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/vocabulary")
public class VocabularyApiController {

    private final VocabularyService vocabularyService;

    private final VocabularyFileStorageService vocabularyFileStorageService;

    private final VocabularyLikeService vocabularyLikeService;

    private final ModelMapper modelMapper;

    @PostMapping("/{categoryId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity addPersonalVocabulary(
            @PathVariable Long categoryId,
            @RequestBody @Valid VocabularyCreateDto createDto,
            Errors errors,
            @CurrentUser Member member
    ) {

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        try {

            Vocabulary savedVocabulary = vocabularyService.addPersonalVocabulary(member.getId(), categoryId, createDto);
            PersonalVocabularyDetailResponseDto personalVocabularyDetailResponseDto = PersonalVocabularyDetailResponseDto.vocabularyToDetail(savedVocabulary, modelMapper);
            URI getVocabularyUri = linkTo(VocabularyApiController.class).slash(savedVocabulary.getId()).toUri();

            return ResponseEntity.created(getVocabularyUri)
                    .body(new ResponseDto<>(personalVocabularyDetailResponseDto, "단어장이 성공적으로 추가되었습니다."));

        } catch (MemberNotFoundException | CategoryNotFoundException | VocabularyThumbnailImageFileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (DivisionMismatchException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{vocabularyId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity addWordList(
            @PathVariable Long vocabularyId,
            @RequestBody @Valid OnlyWordRequestListDto wordRequestListDto,
            Errors errors,
            @CurrentUser Member member
    ) {

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            vocabularyService.addWordListToPersonalVocabulary(vocabularyId, wordRequestListDto.getWordRequestDtoList());

            Vocabulary findVocabulary = vocabularyService.getVocabulary(vocabularyId);
            if (!findVocabulary.getMember().getId().equals(member.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>("다른 회원의 단어장에는 단어 목록을 추가할 수 없습니다."));
            }
            PersonalVocabularyDetailResponseDto personalVocabularyDetailResponseDto = PersonalVocabularyDetailResponseDto.vocabularyToDetail(findVocabulary, modelMapper);

            return ResponseEntity.ok(new ResponseDto<>(personalVocabularyDetailResponseDto, "단어장에 단어 목록이 성공적으로 추가되었습니다."));

        } catch (VocabularyNotFoundException | WordImageFileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestByDivision e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }

    }
}
