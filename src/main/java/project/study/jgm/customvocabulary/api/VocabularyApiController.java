package project.study.jgm.customvocabulary.api;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
import project.study.jgm.customvocabulary.vocabulary.word.dto.WordRequestDto;
import project.study.jgm.customvocabulary.vocabulary.word.upload.exception.WordImageFileNotFoundException;

import javax.validation.Valid;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static project.study.jgm.customvocabulary.common.dto.MessageVo.*;

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
    public ResponseEntity<?> addPersonalVocabulary(
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
                    .body(new ResponseDto<>(personalVocabularyDetailResponseDto, ADD_VOCABULARY_SUCCESSFULLY));

        } catch (MemberNotFoundException | CategoryNotFoundException | VocabularyThumbnailImageFileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        } catch (DivisionMismatchException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }
    }

    @PostMapping("/updateWords/{vocabularyId}")
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
            PersonalVocabularyDetailResponseDto personalVocabularyDetailResponseDto = PersonalVocabularyDetailResponseDto.vocabularyToDetail(findVocabulary, modelMapper);

            return ResponseEntity.ok(new ResponseDto<>(personalVocabularyDetailResponseDto, UPDATE_WORD_LIST_OF_PERSONAL_VOCABULARY_SUCCESSFULLY));

        } catch (VocabularyNotFoundException | WordImageFileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        } catch (BadRequestByDivision e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }
    }
}
