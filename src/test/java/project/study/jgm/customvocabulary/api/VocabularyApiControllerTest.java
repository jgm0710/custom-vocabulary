package project.study.jgm.customvocabulary.api;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.ResultActions;
import project.study.jgm.customvocabulary.common.BaseControllerTest;
import project.study.jgm.customvocabulary.common.dto.CriteriaDto;
import project.study.jgm.customvocabulary.common.dto.MessageVo;
import project.study.jgm.customvocabulary.common.exception.ExistLikeException;
import project.study.jgm.customvocabulary.common.exception.SelfLikeException;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.dto.MemberCreateDto;
import project.study.jgm.customvocabulary.security.dto.OnlyTokenDto;
import project.study.jgm.customvocabulary.security.dto.TokenDto;
import project.study.jgm.customvocabulary.vocabulary.Vocabulary;
import project.study.jgm.customvocabulary.vocabulary.VocabularyDivision;
import project.study.jgm.customvocabulary.vocabulary.VocabularySortCondition;
import project.study.jgm.customvocabulary.vocabulary.category.Category;
import project.study.jgm.customvocabulary.vocabulary.category.CategoryDivision;
import project.study.jgm.customvocabulary.vocabulary.category.exception.CategoryNotFoundException;
import project.study.jgm.customvocabulary.vocabulary.dto.PersonalVocabularySimpleDto;
import project.study.jgm.customvocabulary.vocabulary.dto.SharedVocabularySearchDto;
import project.study.jgm.customvocabulary.vocabulary.dto.VocabularyCreateDto;
import project.study.jgm.customvocabulary.vocabulary.dto.VocabularyUpdateDto;
import project.study.jgm.customvocabulary.vocabulary.exception.*;
import project.study.jgm.customvocabulary.vocabulary.upload.VocabularyThumbnailImageFile;
import project.study.jgm.customvocabulary.vocabulary.upload.exception.VocabularyThumbnailImageFileNotFoundException;
import project.study.jgm.customvocabulary.vocabulary.word.LanguageType;
import project.study.jgm.customvocabulary.vocabulary.word.Word;
import project.study.jgm.customvocabulary.vocabulary.word.dto.OnlyWordRequestListDto;
import project.study.jgm.customvocabulary.vocabulary.word.dto.WordRequestDto;
import project.study.jgm.customvocabulary.vocabulary.word.upload.WordImageFile;
import project.study.jgm.customvocabulary.vocabulary.word.upload.exception.WordImageFileNotFoundException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VocabularyApiControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("개인 단어장 추가")
    public void addPersonalVocabulary() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategory = createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);
        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        int difficulty = 5;
        String title = "sample vocabulary";
        LanguageType mainLanguage = LanguageType.KOREAN;
        LanguageType subLanguage = LanguageType.ENGLISH;
        VocabularyCreateDto vocabularyCreateDto = VocabularyCreateDto.builder()
                .title(title)
                .difficulty(difficulty)
                .mainLanguage(mainLanguage)
                .subLanguage(subLanguage)
                .imageFileId(vocabularyThumbnailImageFile.getId())
                .categoryId(personalCategory.getId())
                .build();

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/personal")
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(vocabularyCreateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isCreated())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.writer.id").value(user1.getId()))
                .andExpect(jsonPath("data.writer.nickname").value(user1.getNickname()))
                .andExpect(jsonPath("data.category.id").value(personalCategory.getId()))
                .andExpect(jsonPath("data.category.name").value(personalCategory.getName()))
                .andExpect(jsonPath("data.thumbnailInfo.fileId").value(vocabularyThumbnailImageFile.getId()))
                .andExpect(jsonPath("data.thumbnailInfo.fileName").value(vocabularyThumbnailImageFile.getFileName()))
                .andExpect(jsonPath("data.thumbnailInfo.fileDownloadUri").value(vocabularyThumbnailImageFile.getFileDownloadUri()))
                .andExpect(jsonPath("data.thumbnailInfo.fileType").value(vocabularyThumbnailImageFile.getFileType()))
                .andExpect(jsonPath("data.thumbnailInfo.size").value(vocabularyThumbnailImageFile.getSize()))
                .andExpect(jsonPath("data.title").value(title))
                .andExpect(jsonPath("data.mainLanguage").value(mainLanguage.name()))
                .andExpect(jsonPath("data.subLanguage").value(subLanguage.name()))
                .andExpect(jsonPath("data.wordList").isEmpty())
                .andExpect(jsonPath("data.difficulty").value(difficulty))
                .andExpect(jsonPath("data.memorisedCount").value(0))
                .andExpect(jsonPath("data.totalWordCount").value(0))
                .andExpect(jsonPath("data.division").value(VocabularyDivision.PERSONAL.name()))
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("message").value(MessageVo.ADD_VOCABULARY_SUCCESSFULLY))

        ;

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 개인 단어장을 추가하는 경우")
    public void addPersonalVocabulary_UnAuthentication() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategory = createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);
        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        int difficulty = 5;
        String title = "sample vocabulary";
        LanguageType mainLanguage = LanguageType.KOREAN;
        LanguageType subLanguage = LanguageType.ENGLISH;
        VocabularyCreateDto vocabularyCreateDto = VocabularyCreateDto.builder()
                .title(title)
                .difficulty(difficulty)
                .mainLanguage(mainLanguage)
                .subLanguage(subLanguage)
                .imageFileId(vocabularyThumbnailImageFile.getId())
                .categoryId(personalCategory.getId())
                .build();

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/personal")
//                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(vocabularyCreateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("개인 단어장 추가 시 입력값이 없는 경우")
    public void addPersonalVocabulary_Empty() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategory = createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);
        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        int difficulty = 5;
        String title = "sample vocabulary";
        LanguageType mainLanguage = LanguageType.KOREAN;
        LanguageType subLanguage = LanguageType.ENGLISH;
        VocabularyCreateDto vocabularyCreateDto = VocabularyCreateDto.builder()
//                .title(title)
//                .difficulty(difficulty)
//                .mainLanguage(mainLanguage)
//                .subLanguage(subLanguage)
//                .imageFileId(vocabularyThumbnailImageFile.getId())
                .build();

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/personal")
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(vocabularyCreateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].code").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].field").exists())
        ;

    }

    @Test
    @DisplayName("개인 단어장 추가 시 카테고리를 찾을 수 없는 경우")
    public void addPersonalVocabulary_CategoryNotFound() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategory = createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);
        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        int difficulty = 5;
        String title = "sample vocabulary";
        LanguageType mainLanguage = LanguageType.KOREAN;
        LanguageType subLanguage = LanguageType.ENGLISH;
        VocabularyCreateDto vocabularyCreateDto = VocabularyCreateDto.builder()
                .title(title)
                .difficulty(difficulty)
                .mainLanguage(mainLanguage)
                .subLanguage(subLanguage)
                .imageFileId(vocabularyThumbnailImageFile.getId())
                .categoryId(10000L)
                .build();

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/personal")
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(vocabularyCreateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new CategoryNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("개인 단어장 추가 시 썸네일 파일을 찾을 수 없는 경우")
    public void addPersonalVocabulary_ThumbnailNotFound() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategory = createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);
        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        int difficulty = 5;
        String title = "sample vocabulary";
        LanguageType mainLanguage = LanguageType.KOREAN;
        LanguageType subLanguage = LanguageType.ENGLISH;
        VocabularyCreateDto vocabularyCreateDto = VocabularyCreateDto.builder()
                .title(title)
                .difficulty(difficulty)
                .mainLanguage(mainLanguage)
                .subLanguage(subLanguage)
                .imageFileId(10000L)
                .categoryId(personalCategory.getId())
                .build();

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/personal")
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(vocabularyCreateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new VocabularyThumbnailImageFileNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("공유 카테고리에 개인 단어장을 추가하는 경우")
    public void addPersonalVocabulary_DivisionMismatchException() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category sharedCategory = createCategory(null, CategoryDivision.SHARED, "shared category", null, 1);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);
        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        int difficulty = 5;
        String title = "sample vocabulary";
        LanguageType mainLanguage = LanguageType.KOREAN;
        LanguageType subLanguage = LanguageType.ENGLISH;
        VocabularyCreateDto vocabularyCreateDto = VocabularyCreateDto.builder()
                .title(title)
                .difficulty(difficulty)
                .mainLanguage(mainLanguage)
                .subLanguage(subLanguage)
                .imageFileId(vocabularyThumbnailImageFile.getId())
                .categoryId(sharedCategory.getId())
                .build();

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/personal")
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(vocabularyCreateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new DivisionMismatchException().getMessage()));

    }

    @Test
    @DisplayName("개인 단어장 단어 목록 변경")
    public void updateWordListOfPersonalVocabulary() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategory = createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);
        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        int difficulty = 5;
        String title = "sample vocabulary";
        LanguageType mainLanguage = LanguageType.KOREAN;
        LanguageType subLanguage = LanguageType.ENGLISH;
        VocabularyCreateDto vocabularyCreateDto = VocabularyCreateDto.builder()
                .title(title)
                .difficulty(difficulty)
                .mainLanguage(mainLanguage)
                .subLanguage(subLanguage)
                .imageFileId(vocabularyThumbnailImageFile.getId())
                .build();

        Vocabulary vocabulary = vocabularyService.addPersonalVocabulary(user1.getId(), personalCategory.getId(), vocabularyCreateDto);

        List<WordRequestDto> wordRequestDtoList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            MockMultipartFile mockMultipartFile1 = getMockMultipartFile("file", testImageFilePath);
            WordImageFile wordImageFile = wordFileStorageService.uploadWordImageFile(mockMultipartFile1);

            WordRequestDto wordRequestDto = WordRequestDto.builder()
                    .imageFileId(wordImageFile.getId())
                    .mainWord("main" + i)
                    .subWord("sub" + i)
                    .memorisedCheck(false)
                    .build();

            wordRequestDtoList.add(wordRequestDto);
        }

        OnlyWordRequestListDto onlyWordRequestListDto = new OnlyWordRequestListDto(wordRequestDtoList);


        //when
        ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/personal/words/" + vocabulary.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(onlyWordRequestListDto))
        )
                .andDo(print());


        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.writer.id").value(user1.getId()))
                .andExpect(jsonPath("data.writer.nickname").value(user1.getNickname()))
                .andExpect(jsonPath("data.category.id").value(personalCategory.getId()))
                .andExpect(jsonPath("data.category.name").value(personalCategory.getName()))
                .andExpect(jsonPath("data.thumbnailInfo.fileId").value(vocabularyThumbnailImageFile.getId()))
                .andExpect(jsonPath("data.thumbnailInfo.fileName").value(vocabularyThumbnailImageFile.getFileName()))
                .andExpect(jsonPath("data.thumbnailInfo.fileDownloadUri").value(vocabularyThumbnailImageFile.getFileDownloadUri()))
                .andExpect(jsonPath("data.thumbnailInfo.fileType").value(vocabularyThumbnailImageFile.getFileType()))
                .andExpect(jsonPath("data.thumbnailInfo.size").value(vocabularyThumbnailImageFile.getSize()))
                .andExpect(jsonPath("data.title").value(title))
                .andExpect(jsonPath("data.mainLanguage").value(mainLanguage.name()))
                .andExpect(jsonPath("data.subLanguage").value(subLanguage.name()))
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileId").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileName").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileType").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.size").exists())
                .andExpect(jsonPath("data.wordList[0].mainWord").exists())
                .andExpect(jsonPath("data.wordList[0].subWord").exists())
                .andExpect(jsonPath("data.wordList[0].memorisedCheck").exists())
                .andExpect(jsonPath("data.difficulty").value(difficulty))
                .andExpect(jsonPath("data.memorisedCount").value(0))
                .andExpect(jsonPath("data.totalWordCount").value(wordRequestDtoList.size()))
                .andExpect(jsonPath("data.division").value(VocabularyDivision.PERSONAL.name()))
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("message").value(MessageVo.UPDATE_WORD_LIST_OF_PERSONAL_VOCABULARY_SUCCESSFULLY))
        ;

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 단어 목록을 변경하는 경우")
    public void updateWordListOfPersonalVocabulary_UnAuthentication() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategory = createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);
        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        int difficulty = 5;
        String title = "sample vocabulary";
        LanguageType mainLanguage = LanguageType.KOREAN;
        LanguageType subLanguage = LanguageType.ENGLISH;
        VocabularyCreateDto vocabularyCreateDto = VocabularyCreateDto.builder()
                .title(title)
                .difficulty(difficulty)
                .mainLanguage(mainLanguage)
                .subLanguage(subLanguage)
                .imageFileId(vocabularyThumbnailImageFile.getId())
                .build();

        Vocabulary vocabulary = vocabularyService.addPersonalVocabulary(user1.getId(), personalCategory.getId(), vocabularyCreateDto);

        List<WordRequestDto> wordRequestDtoList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            MockMultipartFile mockMultipartFile1 = getMockMultipartFile("file", testImageFilePath);
            WordImageFile wordImageFile = wordFileStorageService.uploadWordImageFile(mockMultipartFile1);

            WordRequestDto wordRequestDto = WordRequestDto.builder()
                    .imageFileId(wordImageFile.getId())
                    .mainWord("main" + i)
                    .subWord("sub" + i)
                    .memorisedCheck(false)
                    .build();

            wordRequestDtoList.add(wordRequestDto);
        }

        OnlyWordRequestListDto onlyWordRequestListDto = new OnlyWordRequestListDto(wordRequestDtoList);


        //when
        ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/personal/words/" + vocabulary.getId())
//                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(onlyWordRequestListDto))
        )
                .andDo(print());

        //then
        perform
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("개인 단어장에 단어 목록 변경 시 값이 비어있는 경우")
    public void updateWordListOfPersonalVocabulary_Empty() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategory = createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);
        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        int difficulty = 5;
        String title = "sample vocabulary";
        LanguageType mainLanguage = LanguageType.KOREAN;
        LanguageType subLanguage = LanguageType.ENGLISH;
        VocabularyCreateDto vocabularyCreateDto = VocabularyCreateDto.builder()
                .title(title)
                .difficulty(difficulty)
                .mainLanguage(mainLanguage)
                .subLanguage(subLanguage)
                .imageFileId(vocabularyThumbnailImageFile.getId())
                .build();

        Vocabulary vocabulary = vocabularyService.addPersonalVocabulary(user1.getId(), personalCategory.getId(), vocabularyCreateDto);

        List<WordRequestDto> wordRequestDtoList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            MockMultipartFile mockMultipartFile1 = getMockMultipartFile("file", testImageFilePath);
            WordImageFile wordImageFile = wordFileStorageService.uploadWordImageFile(mockMultipartFile1);

            WordRequestDto wordRequestDto = WordRequestDto.builder()
                    .imageFileId(wordImageFile.getId())
//                    .mainWord("main" + i)
//                    .subWord("sub" + i)
                    .memorisedCheck(false)
                    .build();

            wordRequestDtoList.add(wordRequestDto);
        }

        OnlyWordRequestListDto onlyWordRequestListDto = new OnlyWordRequestListDto(wordRequestDtoList);


        //when
        ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/personal/words/" + vocabulary.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(onlyWordRequestListDto))
        )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].code").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].field").exists())
        ;

    }

    @Test
    @DisplayName("다른 회원의 개인 단어장의 단어 목록을 변경하는 경우")
    public void updateWordListOfPersonalVocabulary_Of_DifferentMember() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategory = createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1);

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        Member user2 = memberService.userJoin(memberCreateDto1);
        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);
        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        int difficulty = 5;
        String title = "sample vocabulary";
        LanguageType mainLanguage = LanguageType.KOREAN;
        LanguageType subLanguage = LanguageType.ENGLISH;
        VocabularyCreateDto vocabularyCreateDto = VocabularyCreateDto.builder()
                .title(title)
                .difficulty(difficulty)
                .mainLanguage(mainLanguage)
                .subLanguage(subLanguage)
                .imageFileId(vocabularyThumbnailImageFile.getId())
                .build();

        Vocabulary vocabulary = vocabularyService.addPersonalVocabulary(user1.getId(), personalCategory.getId(), vocabularyCreateDto);

        List<WordRequestDto> wordRequestDtoList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            MockMultipartFile mockMultipartFile1 = getMockMultipartFile("file", testImageFilePath);
            WordImageFile wordImageFile = wordFileStorageService.uploadWordImageFile(mockMultipartFile1);

            WordRequestDto wordRequestDto = WordRequestDto.builder()
                    .imageFileId(wordImageFile.getId())
                    .mainWord("main" + i)
                    .subWord("sub" + i)
                    .memorisedCheck(false)
                    .build();

            wordRequestDtoList.add(wordRequestDto);
        }

        OnlyWordRequestListDto onlyWordRequestListDto = new OnlyWordRequestListDto(wordRequestDtoList);


        //when
        ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/personal/words/" + vocabulary.getId())
                        .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(onlyWordRequestListDto))
        )
                .andDo(print());

        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.UPDATE_WORD_LIST_OF_VOCABULARY_OF_DIFFERENT_MEMBER));

    }

    @Test
    @DisplayName("개인 단어장 단어 목록 변경 시 단어장을 찾을 수 없는 경우")
    public void updateWordListOfPersonalVocabulary_VocabularyNotFound() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategory = createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);
        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        int difficulty = 5;
        String title = "sample vocabulary";
        LanguageType mainLanguage = LanguageType.KOREAN;
        LanguageType subLanguage = LanguageType.ENGLISH;
        VocabularyCreateDto vocabularyCreateDto = VocabularyCreateDto.builder()
                .title(title)
                .difficulty(difficulty)
                .mainLanguage(mainLanguage)
                .subLanguage(subLanguage)
                .imageFileId(vocabularyThumbnailImageFile.getId())
                .build();

        Vocabulary vocabulary = vocabularyService.addPersonalVocabulary(user1.getId(), personalCategory.getId(), vocabularyCreateDto);

        List<WordRequestDto> wordRequestDtoList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            MockMultipartFile mockMultipartFile1 = getMockMultipartFile("file", testImageFilePath);
            WordImageFile wordImageFile = wordFileStorageService.uploadWordImageFile(mockMultipartFile1);

            WordRequestDto wordRequestDto = WordRequestDto.builder()
                    .imageFileId(wordImageFile.getId())
                    .mainWord("main" + i)
                    .subWord("sub" + i)
                    .memorisedCheck(false)
                    .build();

            wordRequestDtoList.add(wordRequestDto);
        }

        OnlyWordRequestListDto onlyWordRequestListDto = new OnlyWordRequestListDto(wordRequestDtoList);


        //when
        ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/personal/words/" + 10000L)
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(onlyWordRequestListDto))
        )
                .andDo(print());


        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new VocabularyNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("개인 단어장 단어 목록 변경 시 단어의 이미지 파일을 찾을 수 없는 경우")
    public void updateWordListOfPersonalVocabulary_WordImageFileNotFoundException() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategory = createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);
        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        int difficulty = 5;
        String title = "sample vocabulary";
        LanguageType mainLanguage = LanguageType.KOREAN;
        LanguageType subLanguage = LanguageType.ENGLISH;
        VocabularyCreateDto vocabularyCreateDto = VocabularyCreateDto.builder()
                .title(title)
                .difficulty(difficulty)
                .mainLanguage(mainLanguage)
                .subLanguage(subLanguage)
                .imageFileId(vocabularyThumbnailImageFile.getId())
                .build();

        Vocabulary vocabulary = vocabularyService.addPersonalVocabulary(user1.getId(), personalCategory.getId(), vocabularyCreateDto);

        List<WordRequestDto> wordRequestDtoList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            MockMultipartFile mockMultipartFile1 = getMockMultipartFile("file", testImageFilePath);
            WordImageFile wordImageFile = wordFileStorageService.uploadWordImageFile(mockMultipartFile1);

            WordRequestDto wordRequestDto = WordRequestDto.builder()
                    .imageFileId(10000L)
                    .mainWord("main" + i)
                    .subWord("sub" + i)
                    .memorisedCheck(false)
                    .build();

            wordRequestDtoList.add(wordRequestDto);
        }

        OnlyWordRequestListDto onlyWordRequestListDto = new OnlyWordRequestListDto(wordRequestDtoList);


        //when
        ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/personal/words/" + vocabulary.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(onlyWordRequestListDto))
        )
                .andDo(print());


        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new WordImageFileNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("공유 단어장의 단어 목록을 수정하는 경우")
    public void updateWordListOfSharedVocabulary() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategory = createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);
        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        int difficulty = 5;
        String title = "sample vocabulary";
        LanguageType mainLanguage = LanguageType.KOREAN;
        LanguageType subLanguage = LanguageType.ENGLISH;
        VocabularyCreateDto vocabularyCreateDto = VocabularyCreateDto.builder()
                .title(title)
                .difficulty(difficulty)
                .mainLanguage(mainLanguage)
                .subLanguage(subLanguage)
                .imageFileId(vocabularyThumbnailImageFile.getId())
                .build();

        Vocabulary vocabulary = vocabularyService.addPersonalVocabulary(user1.getId(), personalCategory.getId(), vocabularyCreateDto);

        Vocabulary sharedVocabulary = vocabularyService.share(vocabulary.getId(), null);

        List<WordRequestDto> wordRequestDtoList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            MockMultipartFile mockMultipartFile1 = getMockMultipartFile("file", testImageFilePath);
            WordImageFile wordImageFile = wordFileStorageService.uploadWordImageFile(mockMultipartFile1);

            WordRequestDto wordRequestDto = WordRequestDto.builder()
                    .imageFileId(wordImageFile.getId())
                    .mainWord("main" + i)
                    .subWord("sub" + i)
                    .memorisedCheck(false)
                    .build();

            wordRequestDtoList.add(wordRequestDto);
        }

        OnlyWordRequestListDto onlyWordRequestListDto = new OnlyWordRequestListDto(wordRequestDtoList);


        //when
        ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/personal/words/" + sharedVocabulary.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(onlyWordRequestListDto))
        )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("구분이 잘못된 요청입니다. : 개인 단어장 이외에는 단어 목록을 수정할 수 없습니다. 다운로드 받은 단어장, 삭제된 단어장 또한 단어 목록 수정이 불가능합니다."))
        ;


    }

    @Test
    @DisplayName("개인 단어장 단어에 암기 체크")
    public void checkMemorise() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        Category personalCategory = createPersonalCategorySample(user1);

        final Vocabulary personalVocabulary = createPersonalVocabularySample(user1, personalCategory);

        List<WordRequestDto> wordRequestDtoList = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabulary.getId(), wordRequestDtoList);

        em.flush();
        em.clear();

        final Vocabulary findVocabulary = vocabularyService.getVocabulary(personalVocabulary.getId());

        final Word findWord = findVocabulary.getWordList().get(1);

        //when
        final ResultActions perform = mockMvc
                .perform(
                        put("/api/vocabulary/personal/memorized/" + personalVocabulary.getId() + "/" + findWord.getId())
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.imageInfo.fileId").value(findWord.getWordImageFile().getId()))
                .andExpect(jsonPath("data.imageInfo.fileName").value(findWord.getWordImageFile().getFileName()))
                .andExpect(jsonPath("data.imageInfo.fileDownloadUri").value(findWord.getWordImageFile().getFileDownloadUri()))
                .andExpect(jsonPath("data.imageInfo.fileType").value(findWord.getWordImageFile().getFileType()))
                .andExpect(jsonPath("data.imageInfo.size").value(findWord.getWordImageFile().getSize()))
                .andExpect(jsonPath("data.mainWord").value(findWord.getMainWord()))
                .andExpect(jsonPath("data.subWord").value(findWord.getSubWord()))
                .andExpect(jsonPath("data.memorisedCheck").value(true))
                .andExpect(jsonPath("message").value(MessageVo.CHECK_MEMORIZE_SUCCESSFULLY))
        ;

    }

    @Test
    @DisplayName("좋아요가 등록되 있는 단어에 암기 체크를 하는 경우")
    public void checkMemorise_TrueToFalse() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        Category personalCategory = createPersonalCategorySample(user1);

        final Vocabulary personalVocabulary = createPersonalVocabularySample(user1, personalCategory);

        List<WordRequestDto> wordRequestDtoList = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabulary.getId(), wordRequestDtoList);

        em.flush();
        em.clear();

        final Vocabulary findVocabulary = vocabularyService.getVocabulary(personalVocabulary.getId());

        final Word findWord = findVocabulary.getWordList().get(1);

        vocabularyService.checkMemorise(findWord.getId());

        em.flush();
        em.clear();

        assertTrue(findWord.isMemorisedCheck());

        //when
        final ResultActions perform = mockMvc
                .perform(
                        put("/api/vocabulary/personal/memorized/" + personalVocabulary.getId() + "/" + findWord.getId())
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.imageInfo.fileId").value(findWord.getWordImageFile().getId()))
                .andExpect(jsonPath("data.imageInfo.fileName").value(findWord.getWordImageFile().getFileName()))
                .andExpect(jsonPath("data.imageInfo.fileDownloadUri").value(findWord.getWordImageFile().getFileDownloadUri()))
                .andExpect(jsonPath("data.imageInfo.fileType").value(findWord.getWordImageFile().getFileType()))
                .andExpect(jsonPath("data.imageInfo.size").value(findWord.getWordImageFile().getSize()))
                .andExpect(jsonPath("data.mainWord").value(findWord.getMainWord()))
                .andExpect(jsonPath("data.subWord").value(findWord.getSubWord()))
                .andExpect(jsonPath("data.memorisedCheck").value(false))
                .andExpect(jsonPath("message").value(MessageVo.CHECK_MEMORIZE_SUCCESSFULLY))
        ;

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 단어에 암기 체크를 하는 경우")
    public void checkMemorise_UnAuthentication() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        Category personalCategory = createPersonalCategorySample(user1);

        final Vocabulary personalVocabulary = createPersonalVocabularySample(user1, personalCategory);

        List<WordRequestDto> wordRequestDtoList = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabulary.getId(), wordRequestDtoList);

        em.flush();
        em.clear();

        final Vocabulary findVocabulary = vocabularyService.getVocabulary(personalVocabulary.getId());

        final Word findWord = findVocabulary.getWordList().get(1);

        vocabularyService.checkMemorise(findWord.getId());

        em.flush();
        em.clear();

        assertTrue(findWord.isMemorisedCheck());

        //when
        final ResultActions perform = mockMvc
                .perform(
                        put("/api/vocabulary/personal/memorized/" + personalVocabulary.getId() + "/" + findWord.getId())
//                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                )
                .andDo(print());


        //then
        perform.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("다른 회원의 단어장의 단어에 암기 체크를 하는 경우")
    public void checkMemorise_Of_DifferentMember() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        final TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        Category personalCategory = createPersonalCategorySample(user1);

        final Vocabulary personalVocabulary = createPersonalVocabularySample(user1, personalCategory);

        List<WordRequestDto> wordRequestDtoList = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabulary.getId(), wordRequestDtoList);

        em.flush();
        em.clear();

        final Vocabulary findVocabulary = vocabularyService.getVocabulary(personalVocabulary.getId());

        final Word findWord = findVocabulary.getWordList().get(1);

        vocabularyService.checkMemorise(findWord.getId());

        em.flush();
        em.clear();

        assertTrue(findWord.isMemorisedCheck());

        //when
        final ResultActions perform = mockMvc
                .perform(
                        put("/api/vocabulary/personal/memorized/" + personalVocabulary.getId() + "/" + findWord.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                )
                .andDo(print());


        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.CHECK_MEMORIZE_OF_VOCABULARY_OF_DIFFERENT_MEMBER))
        ;

    }

    @Test
    @DisplayName("단어 암기 체크 시 단어장을 찾을 수 없는 경우")
    public void checkMemorise_VocabularyNotFound() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        Category personalCategory = createPersonalCategorySample(user1);

        final Vocabulary personalVocabulary = createPersonalVocabularySample(user1, personalCategory);

        List<WordRequestDto> wordRequestDtoList = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabulary.getId(), wordRequestDtoList);

        em.flush();
        em.clear();

        final Vocabulary findVocabulary = vocabularyService.getVocabulary(personalVocabulary.getId());

        final Word findWord = findVocabulary.getWordList().get(1);

        vocabularyService.checkMemorise(findWord.getId());

        em.flush();
        em.clear();

        assertTrue(findWord.isMemorisedCheck());

        //when
        final ResultActions perform = mockMvc
                .perform(
                        put("/api/vocabulary/personal/memorized/" + 10000L + "/" + findWord.getId())
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                )
                .andDo(print());


        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new VocabularyNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("단어 암기 체크 시 단어를 찾을 수 없는 경우")
    public void checkMemorise_WordNotFound() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        Category personalCategory = createPersonalCategorySample(user1);

        final Vocabulary personalVocabulary = createPersonalVocabularySample(user1, personalCategory);

        List<WordRequestDto> wordRequestDtoList = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabulary.getId(), wordRequestDtoList);

        em.flush();
        em.clear();

        final Vocabulary findVocabulary = vocabularyService.getVocabulary(personalVocabulary.getId());

        final Word findWord = findVocabulary.getWordList().get(1);

        vocabularyService.checkMemorise(findWord.getId());

        em.flush();
        em.clear();

        assertTrue(findWord.isMemorisedCheck());

        //when
        final ResultActions perform = mockMvc
                .perform(
                        put("/api/vocabulary/personal/memorized/" + personalVocabulary.getId() + "/" + 10000L)
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                )
                .andDo(print());


        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new WordNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("공유 단어장에 암기 체크를 하는 경우")
    public void checkMemorise_Of_ShardVocabulary() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        Category personalCategory = createPersonalCategorySample(user1);

        final Vocabulary personalVocabulary = createPersonalVocabularySample(user1, personalCategory);

        List<WordRequestDto> wordRequestDtoList = createWordListSample();

        em.flush();
        em.clear();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabulary.getId(), wordRequestDtoList);

        em.flush();
        em.clear();

        final Vocabulary sharedVocabulary = vocabularyService.share(personalVocabulary.getId(), null);

        em.flush();
        em.clear();

        final Vocabulary findVocabulary = vocabularyService.getVocabulary(sharedVocabulary.getId());

        final Word findWord = findVocabulary.getWordList().get(1);

        em.flush();
        em.clear();

        //when
        final ResultActions perform = mockMvc
                .perform(
                        put("/api/vocabulary/personal/memorized/" + personalVocabulary.getId() + "/" + findWord.getId())
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                )
                .andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("구분이 잘못된 요청입니다. : 개인 단어장 이외에는 암기 체크를 할 수 없습니다. 삭제된 단어장 또한 암기 체크를 할 수 없습니다."));

    }

    @Test
    @DisplayName("삭제된 단어장의 단어에 암기 체크를 하는 경우")
    public void checkMemorise_Of_DeletedVocabulary() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        Category personalCategory = createPersonalCategorySample(user1);

        final Vocabulary personalVocabulary = createPersonalVocabularySample(user1, personalCategory);

        List<WordRequestDto> wordRequestDtoList = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabulary.getId(), wordRequestDtoList);

        vocabularyService.deletePersonalVocabulary(personalVocabulary.getId());

        em.flush();
        em.clear();

        final Vocabulary findVocabulary = vocabularyService.getVocabulary(personalVocabulary.getId());

        final Word findWord = findVocabulary.getWordList().get(1);

        em.flush();
        em.clear();

        //when
        final ResultActions perform = mockMvc
                .perform(
                        put("/api/vocabulary/personal/memorized/" + personalVocabulary.getId() + "/" + findWord.getId())
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                )
                .andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("구분이 잘못된 요청입니다. : 개인 단어장 이외에는 암기 체크를 할 수 없습니다. 삭제된 단어장 또한 암기 체크를 할 수 없습니다."));

    }

    @Test
    @DisplayName("개인 단어장 수정")
    public void modifyPersonalVocabulary() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath2);
        final VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final String update_title = "update title";
        final int update_difficulty = 10;
        final Long updateImageFileId = vocabularyThumbnailImageFile.getId();
        final VocabularyUpdateDto vocabularyUpdateDto = VocabularyUpdateDto.builder()
                .title(update_title)
                .difficulty(update_difficulty)
                .imageFileId(updateImageFileId)
                .build();

        //when
        final ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/personal/" + personalVocabularySample.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vocabularyUpdateDto))
        )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.writer.id").value(user1.getId()))
                .andExpect(jsonPath("data.writer.nickname").value(user1.getNickname()))
                .andExpect(jsonPath("data.category.id").value(personalCategorySample.getId()))
                .andExpect(jsonPath("data.category.name").value(personalCategorySample.getName()))
                .andExpect(jsonPath("data.thumbnailInfo.fileId").value(vocabularyThumbnailImageFile.getId()))
                .andExpect(jsonPath("data.thumbnailInfo.fileName").value(vocabularyThumbnailImageFile.getFileName()))
                .andExpect(jsonPath("data.thumbnailInfo.fileDownloadUri").value(vocabularyThumbnailImageFile.getFileDownloadUri()))
                .andExpect(jsonPath("data.thumbnailInfo.fileType").value(vocabularyThumbnailImageFile.getFileType()))
                .andExpect(jsonPath("data.title").value(update_title))
                .andExpect(jsonPath("data.mainLanguage").value(personalVocabularySample.getMainLanguage().name()))
                .andExpect(jsonPath("data.subLanguage").value(personalVocabularySample.getSubLanguage().name()))
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileId").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileName").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileType").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.size").exists())
                .andExpect(jsonPath("data.wordList[0].mainWord").exists())
                .andExpect(jsonPath("data.wordList[0].subWord").exists())
                .andExpect(jsonPath("data.wordList[0].memorisedCheck").exists())
                .andExpect(jsonPath("data.difficulty").value(update_difficulty))
                .andExpect(jsonPath("data.memorisedCount").value(personalVocabularySample.getMemorisedCount()))
                .andExpect(jsonPath("data.totalWordCount").value(personalVocabularySample.getTotalWordCount()))
                .andExpect(jsonPath("data.division").value(VocabularyDivision.PERSONAL.name()))
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("message").value(MessageVo.MODIFY_PERSONAL_VOCABULARY_SUCCESSFULLY))
        ;

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 개인 단어장을 수정하는 경우")
    public void modifyPersonalVocabulary_UnAuthentication() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath2);
        final VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final String update_title = "update title";
        final int update_difficulty = 10;
        final Long updateImageFileId = vocabularyThumbnailImageFile.getId();
        final VocabularyUpdateDto vocabularyUpdateDto = VocabularyUpdateDto.builder()
                .title(update_title)
                .difficulty(update_difficulty)
                .imageFileId(updateImageFileId)
                .build();

        //when
        final ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/personal/" + personalVocabularySample.getId())
//                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vocabularyUpdateDto))
        )
                .andDo(print());


        //then
        perform
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("다른 회원의 단어장을 수정하는 경우")
    public void modifyPersonalVocabulary_Of_DifferentMember() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        final TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath2);
        final VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final String update_title = "update title";
        final int update_difficulty = 10;
        final Long updateImageFileId = vocabularyThumbnailImageFile.getId();
        final VocabularyUpdateDto vocabularyUpdateDto = VocabularyUpdateDto.builder()
                .title(update_title)
                .difficulty(update_difficulty)
                .imageFileId(updateImageFileId)
                .build();

        //when
        final ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/personal/" + personalVocabularySample.getId())
                        .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vocabularyUpdateDto))
        )
                .andDo(print());


        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.MODIFY_PERSONAL_VOCABULARY_OF_DIFFERENT_MEMBER));

    }

    @Test
    @DisplayName("관리자가 다른 회원의 단어장을 수정하는 경우")
    public void modifyPersonalVocabulary_By_Admin() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("adminMember", "adminMember");
        final Member admin = memberService.adminJoin(memberCreateDto1);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(admin.getLoginInfo().getRefreshToken());
        final TokenDto adminTokenDto = memberService.refresh(onlyTokenDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath2);
        final VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final String update_title = "update title";
        final int update_difficulty = 10;
        final Long updateImageFileId = vocabularyThumbnailImageFile.getId();
        final VocabularyUpdateDto vocabularyUpdateDto = VocabularyUpdateDto.builder()
                .title(update_title)
                .difficulty(update_difficulty)
                .imageFileId(updateImageFileId)
                .build();

        //when
        final ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/personal/" + personalVocabularySample.getId())
                        .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vocabularyUpdateDto))
        )
                .andDo(print());


        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.MODIFY_PERSONAL_VOCABULARY_OF_DIFFERENT_MEMBER));

    }

    @Test
    @DisplayName("개인 단어장 수정 시 단어장을 찾을 수 없는 경우")
    public void modifyPersonalVocabulary_VocabularyNotFound() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath2);
        final VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final String update_title = "update title";
        final int update_difficulty = 10;
        final Long updateImageFileId = vocabularyThumbnailImageFile.getId();
        final VocabularyUpdateDto vocabularyUpdateDto = VocabularyUpdateDto.builder()
                .title(update_title)
                .difficulty(update_difficulty)
                .imageFileId(updateImageFileId)
                .build();

        //when
        final ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/personal/" + 10000L)
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vocabularyUpdateDto))
        )
                .andDo(print());


        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new VocabularyNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("개인 단어장 수정 시 썸네일 파일을 찾을 수 없는 경우")
    public void modifyPersonalVocabulary_VocabularyThumbnailImageFileNotFoundException() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath2);
        final VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final String update_title = "update title";
        final int update_difficulty = 10;
        final Long updateImageFileId = 10000L;
        final VocabularyUpdateDto vocabularyUpdateDto = VocabularyUpdateDto.builder()
                .title(update_title)
                .difficulty(update_difficulty)
                .imageFileId(updateImageFileId)
                .build();

        //when
        final ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/personal/" + personalVocabularySample.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vocabularyUpdateDto))
        )
                .andDo(print());


        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new VocabularyThumbnailImageFileNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("공유 단어장을 수정하는 경우")
    public void modifySharedVocabulary() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath2);
        final VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), null);

        final String update_title = "update title";
        final int update_difficulty = 10;
        final Long updateImageFileId = vocabularyThumbnailImageFile.getId();
        final VocabularyUpdateDto vocabularyUpdateDto = VocabularyUpdateDto.builder()
                .title(update_title)
                .difficulty(update_difficulty)
                .imageFileId(updateImageFileId)
                .build();

        //when
        final ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/personal/" + sharedVocabulary.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vocabularyUpdateDto))
        )
                .andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("구분이 잘못된 요청입니다. : 개인 단어장 외에는 수정할 수 없습니다. 다운로드 받은 단어장, 삭제된 단어장 또한 수정할 수 없습니다."))
        ;

    }

    @Test
    @DisplayName("개인 단어장 공유하기")
    public void sharePersonalVocabulary() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Category sharedCategory = createCategory(null, CategoryDivision.SHARED, "shared category", null, 1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();



        //when
        final ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/shared/" + personalVocabularySample.getId())
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .param("categoryId", sharedCategory.getId().toString())
                ).andDo(print());

        //then
        perform
                .andExpect(status().isCreated())
                .andExpect(jsonPath("data.id").value(Matchers.not(personalVocabularySample.getId())))
                .andExpect(jsonPath("data.writer.id").value(user1.getId()))
                .andExpect(jsonPath("data.writer.nickname").value(user1.getNickname()))
                .andExpect(jsonPath("data.category.id").value(sharedCategory.getId()))
                .andExpect(jsonPath("data.category.name").value(sharedCategory.getName()))
                .andExpect(jsonPath("data.thumbnailInfo.fileId").value(Matchers.not(personalVocabularySample.getVocabularyThumbnailImageFile().getId())))
                .andExpect(jsonPath("data.thumbnailInfo.fileName").value(personalVocabularySample.getVocabularyThumbnailImageFile().getFileName()))
                .andExpect(jsonPath("data.thumbnailInfo.fileDownloadUri").value(personalVocabularySample.getVocabularyThumbnailImageFile().getFileDownloadUri()))
                .andExpect(jsonPath("data.thumbnailInfo.fileType").value(personalVocabularySample.getVocabularyThumbnailImageFile().getFileType()))
                .andExpect(jsonPath("data.thumbnailInfo.size").value(personalVocabularySample.getVocabularyThumbnailImageFile().getSize()))
                .andExpect(jsonPath("data.title").value(personalVocabularySample.getTitle()))
                .andExpect(jsonPath("data.mainLanguage").value(personalVocabularySample.getMainLanguage().name()))
                .andExpect(jsonPath("data.subLanguage").value(personalVocabularySample.getSubLanguage().name()))
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileId").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileName").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileType").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.size").exists())
                .andExpect(jsonPath("data.wordList[0].mainWord").exists())
                .andExpect(jsonPath("data.wordList[0].subWord").exists())
                .andExpect(jsonPath("data.wordList[0].memorisedCheck").exists())
                .andExpect(jsonPath("data.difficulty").value(personalVocabularySample.getDifficulty()))
                .andExpect(jsonPath("data.views").value(0))
                .andExpect(jsonPath("data.likeCount").value(0))
                .andExpect(jsonPath("data.downloadCount").value(0))
                .andExpect(jsonPath("data.totalWordCount").value(personalVocabularySample.getTotalWordCount()))
                .andExpect(jsonPath("data.division").value(VocabularyDivision.SHARED.name()))
                .andExpect(jsonPath("data.allowModificationAndDeletion").value(true))
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("message").value(MessageVo.SHARE_VOCABULARY_SUCCESSFULLY))
        ;

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 단어장 공유")
    public void sharePersonalVocabulary_UnAuthentication() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Category sharedCategory = createCategory(null, CategoryDivision.SHARED, "shared category", null, 1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();



        //when
        final ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/shared/" + personalVocabularySample.getId())
//                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .param("categoryId", sharedCategory.getId().toString())
                ).andDo(print());


        //then
        perform
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("다른 회원의 단어장을 공유하는 경우")
    public void sharePersonalVocabulary_Of_DifferentMember() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        final TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Category sharedCategory = createCategory(null, CategoryDivision.SHARED, "shared category", null, 1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();



        //when
        final ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/shared/" + personalVocabularySample.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                                .param("categoryId", sharedCategory.getId().toString())
                ).andDo(print());


        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.SHARE_VOCABULARY_OF_DIFFERENT_MEMBER));

    }

    @Test
    @DisplayName("관리자가 회원의 단어장을 공유하는 경우")
    public void sharePersonalVocabulary_By_Admin() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("adminMember", "adminMember");
        final Member admin = memberService.adminJoin(memberCreateDto1);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(admin.getLoginInfo().getRefreshToken());
        final TokenDto adminTokenDto = memberService.refresh(onlyTokenDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Category sharedCategory = createCategory(null, CategoryDivision.SHARED, "shared category", null, 1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();



        //when
        final ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/shared/" + personalVocabularySample.getId())
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                                .param("categoryId", sharedCategory.getId().toString())
                ).andDo(print());


        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.SHARE_VOCABULARY_OF_DIFFERENT_MEMBER));

    }

    @Test
    @DisplayName("단어장 공유 시 단어장을 찾을 수 없는 경우")
    public void sharePersonalVocabulary_VocabularyNotFound() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Category sharedCategory = createCategory(null, CategoryDivision.SHARED, "shared category", null, 1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();



        //when
        final ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/shared/" + 10000L)
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .param("categoryId", sharedCategory.getId().toString())
                ).andDo(print());


        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new VocabularyNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("단어장 공유 시 공유 카테고리를 찾을 수 없는 경우")
    public void sharePersonalVocabulary_SharedCategoryNotFound() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Category sharedCategory = createCategory(null, CategoryDivision.SHARED, "shared category", null, 1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();



        //when
        final ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/shared/" + personalVocabularySample.getId())
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .param("categoryId", 10000L + "")
                ).andDo(print());


        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new CategoryNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("단어장 공유 시 단어장이 개인 단어장이 아닐 경우")
    public void sharePersonalVocabulary_VocabularyIsNotPersonal() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Category sharedCategory = createCategory(null, CategoryDivision.SHARED, "shared category", null, 1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        final Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), sharedCategory.getId());

        em.flush();
        em.clear();



        //when
        final ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/shared/" + sharedVocabulary.getId())
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .param("categoryId", sharedCategory.getId().toString())
                ).andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("구분이 잘못된 요청입니다. : 자신이 생성한 단어장만 공유할 수 있습니다. 삭제된 단어장, 다운로드 받은 단어장, 이미 공유된 단어장은 공유할 수 없습니다."));

    }

    @Test
    @DisplayName("단어장 공유 시 공유할 카테고리가 공유 카테고리가 아닐 경우")
    public void sharePersonalVocabulary_To_PersonalCategory() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Category personalCategory2 = createCategory(null, CategoryDivision.PERSONAL, "personal category", null, 2);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();



        //when
        final ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/shared/" + personalVocabularySample.getId())
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .param("categoryId", personalCategory2.getId().toString())
                ).andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("구분이 잘못된 요청입니다. : 단어를 공유할 카테고리가 공유 카테고리가 아닙니다."))
        ;

    }

    @Test
    @DisplayName("개인 단어장 목록 조회")
    public void getPersonalVocabularyList() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        final Category personalCategory1 = createCategory(user1, CategoryDivision.PERSONAL, "uer1 personal category1", null, 1);
        final Category personalCategory2 = createCategory(user1, CategoryDivision.PERSONAL, "uer1 personal category2", null, 1);

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final Category personalCategory3 = createCategory(user2, CategoryDivision.PERSONAL, "user2 personal category1", null, 1);
        final Category personalCategory4 = createCategory(user2, CategoryDivision.PERSONAL, "user2 personal category2", null, 1);


        for (int i = 0; i < 10; i++) {
            final Vocabulary user1Vocabulary1 = createPersonalVocabularySample(user1, personalCategory1, "user1 vocabulary1");
            final Vocabulary user1Vocabulary2 = createPersonalVocabularySample(user1, personalCategory2, "user1 vocabulary2");
            final Vocabulary user2Vocabulary1 = createPersonalVocabularySample(user2, personalCategory3, "user2 vocabulary1");
            final Vocabulary user2Vocabulary2 = createPersonalVocabularySample(user2, personalCategory4, "user2 vocabulary2");
        }


        em.flush();
        em.clear();

        //when
        final ResultActions perform = mockMvc.perform(
                RestDocumentationRequestBuilders.get("/api/vocabulary/personal/" + user1.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .param("pageNum", 1 + "")
                        .param("limit", 15 + "")
                        .param("categoryId", personalCategory1.getId().toString())
        ).andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.list[0].id").exists())
                .andExpect(jsonPath("data.list[0].writer.id").exists())
                .andExpect(jsonPath("data.list[0].writer.nickname").exists())
                .andExpect(jsonPath("data.list[0].category.id").exists())
                .andExpect(jsonPath("data.list[0].category.name").exists())
                .andExpect(jsonPath("data.list[0].thumbnailInfo.fileId").exists())
                .andExpect(jsonPath("data.list[0].thumbnailInfo.fileName").exists())
                .andExpect(jsonPath("data.list[0].thumbnailInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.list[0].thumbnailInfo.fileType").exists())
                .andExpect(jsonPath("data.list[0].thumbnailInfo.size").exists())
                .andExpect(jsonPath("data.list[0].title").exists())
                .andExpect(jsonPath("data.list[0].mainLanguage").exists())
                .andExpect(jsonPath("data.list[0].subLanguage").exists())
                .andExpect(jsonPath("data.list[0].difficulty").exists())
                .andExpect(jsonPath("data.list[0].memorisedCount").exists())
                .andExpect(jsonPath("data.list[0].totalWordCount").exists())
                .andExpect(jsonPath("data.list[0].division").exists())
                .andExpect(jsonPath("data.list[0].registerDate").exists())
                .andExpect(jsonPath("data.paging.totalCount").exists())
                .andExpect(jsonPath("data.paging.criteriaDto").exists())
                .andExpect(jsonPath("data.paging.startPage").exists())
                .andExpect(jsonPath("data.paging.endPage").exists())
                .andExpect(jsonPath("data.paging.prev").exists())
                .andExpect(jsonPath("data.paging.next").exists())
                .andExpect(jsonPath("data.paging.totalPage").exists())
                .andExpect(jsonPath("message").value(MessageVo.GET_PERSONAL_VOCABULARY_LIST_SUCCESSFULLY))
        ;

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 개인 단어장 목록을 조회하는 경우")
    public void getPersonalVocabularyList_UnAuthentication() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        final Category personalCategory1 = createCategory(user1, CategoryDivision.PERSONAL, "uer1 personal category1", null, 1);
        final Category personalCategory2 = createCategory(user1, CategoryDivision.PERSONAL, "uer1 personal category2", null, 1);

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final Category personalCategory3 = createCategory(user2, CategoryDivision.PERSONAL, "user2 personal category1", null, 1);
        final Category personalCategory4 = createCategory(user2, CategoryDivision.PERSONAL, "user2 personal category2", null, 1);


        for (int i = 0; i < 10; i++) {
            final Vocabulary user1Vocabulary1 = createPersonalVocabularySample(user1, personalCategory1, "user1 vocabulary1");
            final Vocabulary user1Vocabulary2 = createPersonalVocabularySample(user1, personalCategory2, "user1 vocabulary2");
            final Vocabulary user2Vocabulary1 = createPersonalVocabularySample(user2, personalCategory3, "user2 vocabulary1");
            final Vocabulary user2Vocabulary2 = createPersonalVocabularySample(user2, personalCategory4, "user2 vocabulary2");
        }


        em.flush();
        em.clear();

        //when
        final ResultActions perform = mockMvc.perform(
                RestDocumentationRequestBuilders.get("/api/vocabulary/personal/" + user1.getId())
//                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .param("pageNum", 1 + "")
                        .param("limit", 15 + "")
                        .param("categoryId", personalCategory1.getId().toString())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("다른 회원의 개인 단어장 목록을 조회하는 경우")
    public void getPersonalVocabularyList_Of_DifferentMember() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategory1 = createCategory(user1, CategoryDivision.PERSONAL, "uer1 personal category1", null, 1);
        final Category personalCategory2 = createCategory(user1, CategoryDivision.PERSONAL, "uer1 personal category2", null, 1);

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        final TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        final Category personalCategory3 = createCategory(user2, CategoryDivision.PERSONAL, "user2 personal category1", null, 1);
        final Category personalCategory4 = createCategory(user2, CategoryDivision.PERSONAL, "user2 personal category2", null, 1);


        for (int i = 0; i < 10; i++) {
            final Vocabulary user1Vocabulary1 = createPersonalVocabularySample(user1, personalCategory1, "user1 vocabulary1");
            final Vocabulary user1Vocabulary2 = createPersonalVocabularySample(user1, personalCategory2, "user1 vocabulary2");
            final Vocabulary user2Vocabulary1 = createPersonalVocabularySample(user2, personalCategory3, "user2 vocabulary1");
            final Vocabulary user2Vocabulary2 = createPersonalVocabularySample(user2, personalCategory4, "user2 vocabulary2");
        }

        em.flush();
        em.clear();

        //when
        final ResultActions perform = mockMvc.perform(
                RestDocumentationRequestBuilders.get("/api/vocabulary/personal/" + user1.getId())
                        .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                        .param("pageNum", 1 + "")
                        .param("limit", 15 + "")
                        .param("categoryId", personalCategory1.getId().toString())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.GET_PERSONAL_VOCABULARY_LIST_OF_DIFFERENT_MEMBER));

    }

    @Test
    @DisplayName("단어장 목록 조회 시 페이징 정보를 잘 못 입력한 경우")
    public void getPersonalVocabularyList_WrongCriteria() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        final Category personalCategory1 = createCategory(user1, CategoryDivision.PERSONAL, "uer1 personal category1", null, 1);
        final Category personalCategory2 = createCategory(user1, CategoryDivision.PERSONAL, "uer1 personal category2", null, 1);

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final Category personalCategory3 = createCategory(user2, CategoryDivision.PERSONAL, "user2 personal category1", null, 1);
        final Category personalCategory4 = createCategory(user2, CategoryDivision.PERSONAL, "user2 personal category2", null, 1);


        for (int i = 0; i < 10; i++) {
            final Vocabulary user1Vocabulary1 = createPersonalVocabularySample(user1, personalCategory1, "user1 vocabulary1");
            final Vocabulary user1Vocabulary2 = createPersonalVocabularySample(user1, personalCategory2, "user1 vocabulary2");
            final Vocabulary user2Vocabulary1 = createPersonalVocabularySample(user2, personalCategory3, "user2 vocabulary1");
            final Vocabulary user2Vocabulary2 = createPersonalVocabularySample(user2, personalCategory4, "user2 vocabulary2");
        }


        em.flush();
        em.clear();

        //when
        final ResultActions perform = mockMvc.perform(
                RestDocumentationRequestBuilders.get("/api/vocabulary/personal/" + user1.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .param("pageNum", -1 + "")
                        .param("limit", 0 + "")
                        .param("categoryId", personalCategory1.getId().toString())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].code").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].field").exists())
                .andExpect(jsonPath("$[0].rejectedValue").exists())
        ;

    }

    @Test
    @DisplayName("관리자가 다른 회원의 개인 단어장 목록을 조회하는 경우")
    public void getPersonalVocabularyList_By_Admin() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategory1 = createCategory(user1, CategoryDivision.PERSONAL, "uer1 personal category1", null, 1);
        final Category personalCategory2 = createCategory(user1, CategoryDivision.PERSONAL, "uer1 personal category2", null, 1);

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final Category personalCategory3 = createCategory(user2, CategoryDivision.PERSONAL, "user2 personal category1", null, 1);
        final Category personalCategory4 = createCategory(user2, CategoryDivision.PERSONAL, "user2 personal category2", null, 1);


        for (int i = 0; i < 10; i++) {
            final Vocabulary user1Vocabulary1 = createPersonalVocabularySample(user1, personalCategory1, "user1 vocabulary1");
            final Vocabulary user1Vocabulary2 = createPersonalVocabularySample(user1, personalCategory2, "user1 vocabulary2");
            final Vocabulary user2Vocabulary1 = createPersonalVocabularySample(user2, personalCategory3, "user2 vocabulary1");
            final Vocabulary user2Vocabulary2 = createPersonalVocabularySample(user2, personalCategory4, "user2 vocabulary2");
        }

        final MemberCreateDto memberCreateDto2 = getMemberCreateDto("adminMember", "adminMember");
        final Member admin = memberService.adminJoin(memberCreateDto2);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(admin.getLoginInfo().getRefreshToken());
        final TokenDto adminTokenDto = memberService.refresh(onlyTokenDto);


        em.flush();
        em.clear();

        //when
        final ResultActions perform = mockMvc.perform(
                RestDocumentationRequestBuilders.get("/api/vocabulary/personal/" + user1.getId())
                        .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                        .param("pageNum", 1 + "")
                        .param("limit", 15 + "")
                        .param("categoryId", personalCategory1.getId().toString())
        ).andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.list[0].id").exists())
                .andExpect(jsonPath("data.list[0].writer.id").exists())
                .andExpect(jsonPath("data.list[0].writer.nickname").exists())
                .andExpect(jsonPath("data.list[0].category.id").exists())
                .andExpect(jsonPath("data.list[0].category.name").exists())
                .andExpect(jsonPath("data.list[0].thumbnailInfo.fileId").exists())
                .andExpect(jsonPath("data.list[0].thumbnailInfo.fileName").exists())
                .andExpect(jsonPath("data.list[0].thumbnailInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.list[0].thumbnailInfo.fileType").exists())
                .andExpect(jsonPath("data.list[0].thumbnailInfo.size").exists())
                .andExpect(jsonPath("data.list[0].title").exists())
                .andExpect(jsonPath("data.list[0].mainLanguage").exists())
                .andExpect(jsonPath("data.list[0].subLanguage").exists())
                .andExpect(jsonPath("data.list[0].difficulty").exists())
                .andExpect(jsonPath("data.list[0].memorisedCount").exists())
                .andExpect(jsonPath("data.list[0].totalWordCount").exists())
                .andExpect(jsonPath("data.list[0].division").exists())
                .andExpect(jsonPath("data.list[0].registerDate").exists())
                .andExpect(jsonPath("data.paging.totalCount").exists())
                .andExpect(jsonPath("data.paging.criteriaDto").exists())
                .andExpect(jsonPath("data.paging.startPage").exists())
                .andExpect(jsonPath("data.paging.endPage").exists())
                .andExpect(jsonPath("data.paging.prev").exists())
                .andExpect(jsonPath("data.paging.next").exists())
                .andExpect(jsonPath("data.paging.totalPage").exists())
                .andExpect(jsonPath("message").value(MessageVo.GET_PERSONAL_VOCABULARY_LIST_SUCCESSFULLY));

    }

    @Test
    @DisplayName("개인 단어장 조회 시 카테고리가 공유 카테고리인 경우")
    public void getPersonalVocabularyList_Of_SharedCategory() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        final Category personalCategory1 = createCategory(user1, CategoryDivision.PERSONAL, "uer1 personal category1", null, 1);
        final Category personalCategory2 = createCategory(user1, CategoryDivision.PERSONAL, "uer1 personal category2", null, 1);

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final Category personalCategory3 = createCategory(user2, CategoryDivision.PERSONAL, "user2 personal category1", null, 1);
        final Category personalCategory4 = createCategory(user2, CategoryDivision.PERSONAL, "user2 personal category2", null, 1);


        for (int i = 0; i < 10; i++) {
            final Vocabulary user1Vocabulary1 = createPersonalVocabularySample(user1, personalCategory1, "user1 vocabulary1");
            final Vocabulary user1Vocabulary2 = createPersonalVocabularySample(user1, personalCategory2, "user1 vocabulary2");
            final Vocabulary user2Vocabulary1 = createPersonalVocabularySample(user2, personalCategory3, "user2 vocabulary1");
            final Vocabulary user2Vocabulary2 = createPersonalVocabularySample(user2, personalCategory4, "user2 vocabulary2");
        }

        final Category sharedCategory = createCategory(null, CategoryDivision.SHARED, "shared category", null, 1);


        em.flush();
        em.clear();

        //when
        final ResultActions perform = mockMvc.perform(
                RestDocumentationRequestBuilders.get("/api/vocabulary/personal/" + user1.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .param("pageNum", 1 + "")
                        .param("limit", 15 + "")
                        .param("categoryId", sharedCategory.getId().toString())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
        .andExpect(jsonPath("message").value("구분이 잘못된 요청입니다. : 개인 카테고리의 단어 목록만 조회할 수 있습니다."))
        ;

    }

    @Test
    @DisplayName("개인 단어장 목록 조회 시 카테고리가 다른 회원의 카테고리인 경우")
    public void getPersonalVocabularyList_Of_Category_Of_DifferentMember() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        final Category personalCategory1 = createCategory(user1, CategoryDivision.PERSONAL, "uer1 personal category1", null, 1);
        final Category personalCategory2 = createCategory(user1, CategoryDivision.PERSONAL, "uer1 personal category2", null, 1);

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final Category personalCategory3 = createCategory(user2, CategoryDivision.PERSONAL, "user2 personal category1", null, 1);
        final Category personalCategory4 = createCategory(user2, CategoryDivision.PERSONAL, "user2 personal category2", null, 1);


        for (int i = 0; i < 10; i++) {
            final Vocabulary user1Vocabulary1 = createPersonalVocabularySample(user1, personalCategory1, "user1 vocabulary1");
            final Vocabulary user1Vocabulary2 = createPersonalVocabularySample(user1, personalCategory2, "user1 vocabulary2");
            final Vocabulary user2Vocabulary1 = createPersonalVocabularySample(user2, personalCategory3, "user2 vocabulary1");
            final Vocabulary user2Vocabulary2 = createPersonalVocabularySample(user2, personalCategory4, "user2 vocabulary2");
        }


        em.flush();
        em.clear();

        //when
        final ResultActions perform = mockMvc.perform(
                RestDocumentationRequestBuilders.get("/api/vocabulary/personal/" + user1.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .param("pageNum", 1 + "")
                        .param("limit", 15 + "")
                        .param("categoryId", personalCategory3.getId().toString())
        ).andDo(print());


        //then
        perform.andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new MemberAndCategoryMemberDifferentException().getMessage()));

    }

    @Test
    @DisplayName("개인 단어장 목록 조회 시 카테고리를 찾을 수 없는 경우")
    public void getPersonalVocabularyList_CategoryNotFound() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        final Category personalCategory1 = createCategory(user1, CategoryDivision.PERSONAL, "uer1 personal category1", null, 1);
        final Category personalCategory2 = createCategory(user1, CategoryDivision.PERSONAL, "uer1 personal category2", null, 1);

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final Category personalCategory3 = createCategory(user2, CategoryDivision.PERSONAL, "user2 personal category1", null, 1);
        final Category personalCategory4 = createCategory(user2, CategoryDivision.PERSONAL, "user2 personal category2", null, 1);


        for (int i = 0; i < 10; i++) {
            final Vocabulary user1Vocabulary1 = createPersonalVocabularySample(user1, personalCategory1, "user1 vocabulary1");
            final Vocabulary user1Vocabulary2 = createPersonalVocabularySample(user1, personalCategory2, "user1 vocabulary2");
            final Vocabulary user2Vocabulary1 = createPersonalVocabularySample(user2, personalCategory3, "user2 vocabulary1");
            final Vocabulary user2Vocabulary2 = createPersonalVocabularySample(user2, personalCategory4, "user2 vocabulary2");
        }


        em.flush();
        em.clear();

        //when
        final ResultActions perform = mockMvc.perform(
                RestDocumentationRequestBuilders.get("/api/vocabulary/personal/" + user1.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .param("pageNum", 1 + "")
                        .param("limit", 15 + "")
                        .param("categoryId", 10000L+"")
        ).andDo(print());


        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new CategoryNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("개인 단어장의 카테고리 이동")
    public void moveCategory_Of_PersonalVocabulary() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category user1PersonalCategory1 = createCategory(user1, CategoryDivision.PERSONAL, "user1 personal category1", null, 1);
        final Category user1PersonalCategory2 = createCategory(user1, CategoryDivision.PERSONAL, "user1 personal category2", null, 2);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, user1PersonalCategory1);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        em.flush();
        em.clear();

        //when
        final ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/belongedCategory/" + personalVocabularySample.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .param("categoryId", user1PersonalCategory2.getId().toString())
        ).andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.writer.id").value(user1.getId().toString()))
                .andExpect(jsonPath("data.writer.nickname").value(user1.getNickname()))
                .andExpect(jsonPath("data.category.id").value(user1PersonalCategory2.getId()))
                .andExpect(jsonPath("data.category.name").value(user1PersonalCategory2.getName()))
                .andExpect(jsonPath("data.thumbnailInfo.fileId").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileName").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileType").exists())
                .andExpect(jsonPath("data.thumbnailInfo.size").exists())
                .andExpect(jsonPath("data.title").value(personalVocabularySample.getTitle()))
                .andExpect(jsonPath("data.mainLanguage").value(personalVocabularySample.getMainLanguage().name()))
                .andExpect(jsonPath("data.subLanguage").value(personalVocabularySample.getSubLanguage().name()))
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileId").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileName").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileType").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.size").exists())
                .andExpect(jsonPath("data.wordList[0].mainWord").exists())
                .andExpect(jsonPath("data.wordList[0].subWord").exists())
                .andExpect(jsonPath("data.wordList[0].memorisedCheck").exists())
                .andExpect(jsonPath("data.difficulty").value(personalVocabularySample.getDifficulty()))
                .andExpect(jsonPath("data.memorisedCount").value(personalVocabularySample.getMemorisedCount()))
                .andExpect(jsonPath("data.totalWordCount").value(personalVocabularySample.getTotalWordCount()))
                .andExpect(jsonPath("data.division").value(VocabularyDivision.PERSONAL.name()))
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("message").value(MessageVo.MOVE_CATEGORY_OF_PERSONAL_VOCABULARY_SUCCESSFULLY))
        ;

    }

    @Test
    @DisplayName("다운로드 받은 단어장의 카테고리를 이동시키는 경우")
    public void moveCategory_Of_CopiedVocabulary() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category user1PersonalCategory1 = createCategory(user1, CategoryDivision.PERSONAL, "user1 personal category1", null, 1);
        final Category user1PersonalCategory2 = createCategory(user1, CategoryDivision.PERSONAL, "user1 personal category2", null, 2);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, user1PersonalCategory1);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), null);

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final Vocabulary downloadVocabulary = vocabularyService.download(sharedVocabulary.getId(), user2.getId(), null);

        final Category user2PersonalCategory = createCategory(user2, CategoryDivision.PERSONAL, "user2 personal category", null, 1);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        final TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);


        //when
        final ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/belongedCategory/" + downloadVocabulary.getId())
                        .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                        .param("categoryId", user2PersonalCategory.getId().toString())
        ).andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.writer.id").value(user1.getId().toString()))
                .andExpect(jsonPath("data.writer.nickname").value(user1.getNickname()))
                .andExpect(jsonPath("data.category.id").value(user2PersonalCategory.getId()))
                .andExpect(jsonPath("data.category.name").value(user2PersonalCategory.getName()))
                .andExpect(jsonPath("data.thumbnailInfo.fileId").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileName").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileType").exists())
                .andExpect(jsonPath("data.thumbnailInfo.size").exists())
                .andExpect(jsonPath("data.title").value(personalVocabularySample.getTitle()))
                .andExpect(jsonPath("data.mainLanguage").value(personalVocabularySample.getMainLanguage().name()))
                .andExpect(jsonPath("data.subLanguage").value(personalVocabularySample.getSubLanguage().name()))
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileId").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileName").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileType").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.size").exists())
                .andExpect(jsonPath("data.wordList[0].mainWord").exists())
                .andExpect(jsonPath("data.wordList[0].subWord").exists())
                .andExpect(jsonPath("data.wordList[0].memorisedCheck").exists())
                .andExpect(jsonPath("data.difficulty").value(personalVocabularySample.getDifficulty()))
                .andExpect(jsonPath("data.memorisedCount").value(personalVocabularySample.getMemorisedCount()))
                .andExpect(jsonPath("data.totalWordCount").value(personalVocabularySample.getTotalWordCount()))
                .andExpect(jsonPath("data.division").value(VocabularyDivision.COPIED.name()))
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("message").value(MessageVo.MOVE_CATEGORY_OF_PERSONAL_VOCABULARY_SUCCESSFULLY))
        ;

    }

    @Test
    @DisplayName("다운로드 받은 단어장의 카테고리를 다른 회원의 카테고리로 이동 시키는 경우")
    public void moveCategory_Of_CopiedVocabulary_To_CategoryOfDifferentMember() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category user1PersonalCategory1 = createCategory(user1, CategoryDivision.PERSONAL, "user1 personal category1", null, 1);
        final Category user1PersonalCategory2 = createCategory(user1, CategoryDivision.PERSONAL, "user1 personal category2", null, 2);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, user1PersonalCategory1);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), null);

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final Vocabulary downloadVocabulary = vocabularyService.download(sharedVocabulary.getId(), user2.getId(), null);

        final Category user2PersonalCategory = createCategory(user2, CategoryDivision.PERSONAL, "user2 personal category", null, 1);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        final TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);


        //when
        final ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/belongedCategory/" + downloadVocabulary.getId())
                        .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                        .param("categoryId", user1PersonalCategory2.getId().toString())
        ).andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new MemberMismatchAfterMovingWithCurrentMemberException().getMessage()));

    }

    @Test
    @DisplayName("다른 회원의 개인 단어장의 카테고리를 이동시키는 경우")
    public void moveCategory_Of_PersonalVocabulary_Of_DifferentMember() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category user1PersonalCategory1 = createCategory(user1, CategoryDivision.PERSONAL, "user1 personal category1", null, 1);
        final Category user1PersonalCategory2 = createCategory(user1, CategoryDivision.PERSONAL, "user1 personal category2", null, 2);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, user1PersonalCategory1);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        final TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);


        em.flush();
        em.clear();

        //when
        final ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/belongedCategory/" + personalVocabularySample.getId())
                        .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                        .param("categoryId", user1PersonalCategory2.getId().toString())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.MOVE_CATEGORY_OF_VOCABULARY_OF_DIFFERENT_MEMBER));

    }

    @Test
    @DisplayName("개인 단어장 카테고리 이동 시 단어장을 찾을 수 없는 경우")
    public void moveCategory_VocabularyNotFound() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category user1PersonalCategory1 = createCategory(user1, CategoryDivision.PERSONAL, "user1 personal category1", null, 1);
        final Category user1PersonalCategory2 = createCategory(user1, CategoryDivision.PERSONAL, "user1 personal category2", null, 2);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, user1PersonalCategory1);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        em.flush();
        em.clear();

        //when
        final ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/belongedCategory/" + 10000L)
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .param("categoryId", user1PersonalCategory2.getId().toString())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new VocabularyNotFoundException().getMessage()));
    }

    @Test
    @DisplayName("개인 단어장 카테고리 이동 시 카테고리를 찾을 수 없는 경우")
    public void moveCategory_CategoryNotFound() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category user1PersonalCategory1 = createCategory(user1, CategoryDivision.PERSONAL, "user1 personal category1", null, 1);
        final Category user1PersonalCategory2 = createCategory(user1, CategoryDivision.PERSONAL, "user1 personal category2", null, 2);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, user1PersonalCategory1);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        em.flush();
        em.clear();

        //when
        final ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/belongedCategory/" + personalVocabularySample.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .param("categoryId", 10000L+"")
        ).andDo(print());


        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new CategoryNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("개인 단어장 카테고리 이동 시 이동할 카테고리가 null 인 경우")
    public void moveCategory_CategoryIsNull() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category user1PersonalCategory1 = createCategory(user1, CategoryDivision.PERSONAL, "user1 personal category1", null, 1);
        final Category user1PersonalCategory2 = createCategory(user1, CategoryDivision.PERSONAL, "user1 personal category2", null, 2);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, user1PersonalCategory1);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        em.flush();
        em.clear();

        //when
        final ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/belongedCategory/" + personalVocabularySample.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
//                        .param("categoryId", "")
        ).andDo(print());


        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.writer.id").value(user1.getId().toString()))
                .andExpect(jsonPath("data.writer.nickname").value(user1.getNickname()))
                .andExpect(jsonPath("data.category").isEmpty())
                .andExpect(jsonPath("data.thumbnailInfo.fileId").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileName").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileType").exists())
                .andExpect(jsonPath("data.thumbnailInfo.size").exists())
                .andExpect(jsonPath("data.title").value(personalVocabularySample.getTitle()))
                .andExpect(jsonPath("data.mainLanguage").value(personalVocabularySample.getMainLanguage().name()))
                .andExpect(jsonPath("data.subLanguage").value(personalVocabularySample.getSubLanguage().name()))
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileId").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileName").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileType").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.size").exists())
                .andExpect(jsonPath("data.wordList[0].mainWord").exists())
                .andExpect(jsonPath("data.wordList[0].subWord").exists())
                .andExpect(jsonPath("data.wordList[0].memorisedCheck").exists())
                .andExpect(jsonPath("data.difficulty").value(personalVocabularySample.getDifficulty()))
                .andExpect(jsonPath("data.memorisedCount").value(personalVocabularySample.getMemorisedCount()))
                .andExpect(jsonPath("data.totalWordCount").value(personalVocabularySample.getTotalWordCount()))
                .andExpect(jsonPath("data.division").value(VocabularyDivision.PERSONAL.name()))
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("message").value(MessageVo.MOVE_CATEGORY_OF_PERSONAL_VOCABULARY_SUCCESSFULLY))
        ;


    }

    @Test
    @DisplayName("개인 단어장 카테고리 이동 시 카테고리가 바뀌지 않은 경우")
    public void moveCategory_DoNotMove() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category user1PersonalCategory1 = createCategory(user1, CategoryDivision.PERSONAL, "user1 personal category1", null, 1);
        final Category user1PersonalCategory2 = createCategory(user1, CategoryDivision.PERSONAL, "user1 personal category2", null, 2);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, user1PersonalCategory1);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        em.flush();
        em.clear();

        //when
        final ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/belongedCategory/" + personalVocabularySample.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .param("categoryId", user1PersonalCategory1.getId().toString())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new DoNotMoveException().getMessage()));

    }

    @Test
    @DisplayName("개인 단어장을 공유 카테고리로 이동시키는 경우")
    public void moveCategory_To_SharedCategory() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category user1PersonalCategory1 = createCategory(user1, CategoryDivision.PERSONAL, "user1 personal category1", null, 1);
        final Category user1PersonalCategory2 = createCategory(user1, CategoryDivision.PERSONAL, "user1 personal category2", null, 2);

        final Category sharedCategory = createCategory(null, CategoryDivision.SHARED, "shared category", null, 1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, user1PersonalCategory1);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        em.flush();
        em.clear();

        //when
        final ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/belongedCategory/" + personalVocabularySample.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .param("categoryId", sharedCategory.getId().toString())
        ).andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("Category와 Vocabulary의 구분이 일치하지 않습니다. : 자신이 생성하거나 다운로드 받은 단어장은 개인카테고리로만 이동시킬 수 있습니다."))
        ;

    }

    @Test
    @DisplayName("개인 단어장을 다른 회원의 카테고리에 이동시키는 경우")
    public void moveCategory_To_CategoryOfDifferentMember() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category user1PersonalCategory1 = createCategory(user1, CategoryDivision.PERSONAL, "user1 personal category1", null, 1);
        final Category user1PersonalCategory2 = createCategory(user1, CategoryDivision.PERSONAL, "user1 personal category2", null, 2);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, user1PersonalCategory1);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        em.flush();
        em.clear();

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final Category user2PersonalCategory = createCategory(user2, CategoryDivision.PERSONAL, "user2 personal category", null, 2);

        //when
        final ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/belongedCategory/" + personalVocabularySample.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .param("categoryId", user2PersonalCategory.getId().toString())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new MemberMismatchAfterMovingWithCurrentMemberException().getMessage()))
        ;

    }

    @Test
    @DisplayName("삭제된 단어장의 카테고리를 이동시키는 경우")
    public void moveCategory_Of_DeletedVocabulary() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category user1PersonalCategory1 = createCategory(user1, CategoryDivision.PERSONAL, "user1 personal category1", null, 1);
        final Category user1PersonalCategory2 = createCategory(user1, CategoryDivision.PERSONAL, "user1 personal category2", null, 2);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, user1PersonalCategory1);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        em.flush();
        em.clear();

        vocabularyService.deletePersonalVocabulary(personalVocabularySample.getId());
        em.flush();
        em.clear();

        //when
        final ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/belongedCategory/" + personalVocabularySample.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .param("categoryId", user1PersonalCategory2.getId().toString())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("구분이 잘못된 요청입니다. : 삭제되거나 공유가 해제된 단어장은 카테고리 이동이 불가능합니다."))
        ;

    }

    @Test
    @DisplayName("공유 단어장의 카테고리 이동")
    public void moveCategory_Of_SharedVocabulary() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), null);

        final Category sharedCategory = createCategory(null, CategoryDivision.SHARED, "shared category", null, 1);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        final ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/belongedCategory/" + sharedVocabulary.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .param("categoryId", sharedCategory.getId().toString())
        ).andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.writer.id").value(user1.getId().toString()))
                .andExpect(jsonPath("data.writer.nickname").value(user1.getNickname()))
                .andExpect(jsonPath("data.category.id").value(sharedCategory.getId()))
                .andExpect(jsonPath("data.category.name").value(sharedCategory.getName()))
                .andExpect(jsonPath("data.thumbnailInfo.fileId").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileName").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileType").exists())
                .andExpect(jsonPath("data.thumbnailInfo.size").exists())
                .andExpect(jsonPath("data.title").value(sharedVocabulary.getTitle()))
                .andExpect(jsonPath("data.mainLanguage").value(sharedVocabulary.getMainLanguage().name()))
                .andExpect(jsonPath("data.subLanguage").value(sharedVocabulary.getSubLanguage().name()))
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileId").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileName").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileType").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.size").exists())
                .andExpect(jsonPath("data.wordList[0].mainWord").exists())
                .andExpect(jsonPath("data.wordList[0].subWord").exists())
                .andExpect(jsonPath("data.wordList[0].memorisedCheck").exists())
                .andExpect(jsonPath("data.difficulty").value(sharedVocabulary.getDifficulty()))
                .andExpect(jsonPath("data.views").exists())
                .andExpect(jsonPath("data.likeCount").exists())
                .andExpect(jsonPath("data.downloadCount").exists())
                .andExpect(jsonPath("data.totalWordCount").value(sharedVocabulary.getWordList().size()))
                .andExpect(jsonPath("data.division").value(VocabularyDivision.SHARED.name()))
                .andExpect(jsonPath("data.allowModificationAndDeletion").value(true))
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("message").value(MessageVo.MOVE_CATEGORY_OF_SHARED_VOCABULARY_SUCCESSFULLY))
        ;

    }

    @Test
    @DisplayName("공유 단어장을 개인 카테고리로 이동시키는 경우")
    public void moveCategory_To_PersonalCategory() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), null);

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final Category user2Category = createPersonalCategorySample(user2);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        final ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/belongedCategory/" + sharedVocabulary.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .param("categoryId", user2Category.getId().toString())
        ).andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
        .andExpect(jsonPath("message").value("Category와 Vocabulary의 구분이 일치하지 않습니다. : 공유단어장은 공유카테고리로만 이동시킬 수 있습니다."))
        ;

    }

    @Test
    @DisplayName("공유가 해제된 단어장의 카테고리를 이동시키는 경우")
    public void moveCategory_Of_UnsharedVocabulary() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), null);

        final Category sharedCategory = createCategory(null, CategoryDivision.SHARED, "shared category", null, 1);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        vocabularyService.unshared(sharedVocabulary.getId());

        em.flush();
        em.clear();

        //when
        final ResultActions perform = mockMvc.perform(
                put("/api/vocabulary/belongedCategory/" + sharedVocabulary.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .param("categoryId", sharedCategory.getId().toString())
        ).andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("구분이 잘못된 요청입니다. : 삭제되거나 공유가 해제된 단어장은 카테고리 이동이 불가능합니다."))
        ;

    }


    @Test
    @DisplayName("개인 단어장 조회")
    public void getPersonalVocabulary() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        final ResultActions perform = mockMvc.perform(
                get("/api/vocabulary/" + personalVocabularySample.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
        ).andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.writer.id").exists())
                .andExpect(jsonPath("data.writer.nickname").exists())
                .andExpect(jsonPath("data.category.id").exists())
                .andExpect(jsonPath("data.category.name").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileId").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileName").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileType").exists())
                .andExpect(jsonPath("data.thumbnailInfo.size").exists())
                .andExpect(jsonPath("data.title").exists())
                .andExpect(jsonPath("data.mainLanguage").exists())
                .andExpect(jsonPath("data.subLanguage").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileId").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileName").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileType").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.size").exists())
                .andExpect(jsonPath("data.wordList[0].mainWord").exists())
                .andExpect(jsonPath("data.wordList[0].subWord").exists())
                .andExpect(jsonPath("data.wordList[0].memorisedCheck").exists())
                .andExpect(jsonPath("data.difficulty").exists())
                .andExpect(jsonPath("data.memorisedCount").exists())
                .andExpect(jsonPath("data.totalWordCount").exists())
                .andExpect(jsonPath("data.division").value(VocabularyDivision.PERSONAL.name()))
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("message").value(MessageVo.GET_PERSONAL_VOCABULARY_SUCCESSFULLY))
        ;

    }

    @Test
    @DisplayName("USER 권한의 사용자가 다른 회원의 단어장을 조회하는 경우")
    public void getPersonalVocabulary_Unauthorized() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        final TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        final ResultActions perform = mockMvc.perform(
                get("/api/vocabulary/" + personalVocabularySample.getId())
                        .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
        ).andDo(print());
        

        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.GET_PERSONAL_VOCABULARY_OF_DIFFERENT_MEMBER));

    }

    @Test
    @DisplayName("관리자가 회원의 개인 단어장을 조회")
    public void getPersonalVocabulary_By_Admin() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("adminMember", "adminMember");
        final Member admin = memberService.adminJoin(memberCreateDto1);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(admin.getLoginInfo().getRefreshToken());
        final TokenDto adminTokenDto = memberService.refresh(onlyTokenDto);

        //when
        final ResultActions perform = mockMvc.perform(
                get("/api/vocabulary/" + personalVocabularySample.getId())
                        .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.writer.id").exists())
                .andExpect(jsonPath("data.writer.nickname").exists())
                .andExpect(jsonPath("data.category.id").exists())
                .andExpect(jsonPath("data.category.name").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileId").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileName").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileType").exists())
                .andExpect(jsonPath("data.thumbnailInfo.size").exists())
                .andExpect(jsonPath("data.title").exists())
                .andExpect(jsonPath("data.mainLanguage").exists())
                .andExpect(jsonPath("data.subLanguage").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileId").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileName").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileType").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.size").exists())
                .andExpect(jsonPath("data.wordList[0].mainWord").exists())
                .andExpect(jsonPath("data.wordList[0].subWord").exists())
                .andExpect(jsonPath("data.wordList[0].memorisedCheck").exists())
                .andExpect(jsonPath("data.difficulty").exists())
                .andExpect(jsonPath("data.memorisedCount").exists())
                .andExpect(jsonPath("data.totalWordCount").exists())
                .andExpect(jsonPath("data.division").value(VocabularyDivision.PERSONAL.name()))
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("message").value(MessageVo.GET_PERSONAL_VOCABULARY_SUCCESSFULLY))
        ;


    }

    @Test
    @DisplayName("인증되지 않은 사용자가 개인 단어장 조회")
    public void getPersonalVocabulary_Unauthenticated() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        final ResultActions perform = mockMvc.perform(
                get("/api/vocabulary/" + personalVocabularySample.getId())
//                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.ACCESS_TO_SENSITIVE_VOCABULARY_BY_UNAUTHORIZED_USER));

    }

    @Test
    @DisplayName("USER 권한의 사용자가 개인 삭제된 단어장을 조회하는 경우")
    public void getDeletedVocabulary_By_User() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        vocabularyService.deletePersonalVocabulary(personalVocabularySample.getId());

        em.flush();
        em.clear();

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        final ResultActions perform = mockMvc.perform(
                get("/api/vocabulary/" + personalVocabularySample.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
        ).andDo(print());
        

        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.GET_DELETED_VOCABULARY_BY_USER));

    }

    @Test
    @DisplayName("관리자가 삭제된 단어장을 조회하는 경우")
    public void getDeletedVocabulary_By_Admin() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        vocabularyService.deletePersonalVocabulary(personalVocabularySample.getId());

        em.flush();
        em.clear();

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("adminMember", "adminMember");
        final Member admin = memberService.adminJoin(memberCreateDto1);
        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(admin.getLoginInfo().getRefreshToken());
        final TokenDto adminTokenDto = memberService.refresh(onlyTokenDto);

        //when
        final ResultActions perform = mockMvc.perform(
                get("/api/vocabulary/" + personalVocabularySample.getId())
                        .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.writer.id").exists())
                .andExpect(jsonPath("data.writer.nickname").exists())
                .andExpect(jsonPath("data.category.id").exists())
                .andExpect(jsonPath("data.category.name").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileId").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileName").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileType").exists())
                .andExpect(jsonPath("data.thumbnailInfo.size").exists())
                .andExpect(jsonPath("data.title").exists())
                .andExpect(jsonPath("data.mainLanguage").exists())
                .andExpect(jsonPath("data.subLanguage").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileId").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileName").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileType").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.size").exists())
                .andExpect(jsonPath("data.wordList[0].mainWord").exists())
                .andExpect(jsonPath("data.wordList[0].subWord").exists())
                .andExpect(jsonPath("data.wordList[0].memorisedCheck").exists())
                .andExpect(jsonPath("data.difficulty").exists())
                .andExpect(jsonPath("data.memorisedCount").exists())
                .andExpect(jsonPath("data.totalWordCount").exists())
                .andExpect(jsonPath("data.division").value(VocabularyDivision.DELETE.name()))
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("message").value(MessageVo.GET_DELETED_VOCABULARY_BY_ADMIN))
        ;

    }

    @Test
    @DisplayName("단어장 조회 시 단어장을 찾을 수 없는 경우")
    public void getPersonalVocabulary_VocabularyNotFound() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        final ResultActions perform = mockMvc.perform(
                get("/api/vocabulary/" + 10000L)
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
        ).andDo(print());


        //then
        perform.andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new VocabularyNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("다운로드 받은 단어장 조회")
    public void getCopiedVocabulary() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), null);

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final Category user2Category = createPersonalCategorySample(user2);

        final Vocabulary downloadVocabulary = vocabularyService.download(sharedVocabulary.getId(), user2.getId(), user2Category.getId());

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        final TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        final ResultActions perform = mockMvc.perform(
                get("/api/vocabulary/" + downloadVocabulary.getId())
                        .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.writer.id").exists())
                .andExpect(jsonPath("data.writer.nickname").exists())
                .andExpect(jsonPath("data.category.id").exists())
                .andExpect(jsonPath("data.category.name").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileId").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileName").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileType").exists())
                .andExpect(jsonPath("data.thumbnailInfo.size").exists())
                .andExpect(jsonPath("data.title").exists())
                .andExpect(jsonPath("data.mainLanguage").exists())
                .andExpect(jsonPath("data.subLanguage").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileId").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileName").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileType").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.size").exists())
                .andExpect(jsonPath("data.wordList[0].mainWord").exists())
                .andExpect(jsonPath("data.wordList[0].subWord").exists())
                .andExpect(jsonPath("data.wordList[0].memorisedCheck").exists())
                .andExpect(jsonPath("data.difficulty").exists())
                .andExpect(jsonPath("data.memorisedCount").exists())
                .andExpect(jsonPath("data.totalWordCount").exists())
                .andExpect(jsonPath("data.division").value(VocabularyDivision.COPIED.name()))
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("message").value(MessageVo.GET_PERSONAL_VOCABULARY_SUCCESSFULLY))
        ;

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 공유 단어장 조회")
    public void getSharedVocabulary() throws Exception {
         //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        final Category sharedCategory = createCategory(null, CategoryDivision.SHARED, "shared category", null, 1);

        final Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), sharedCategory.getId());

        //when
        final ResultActions perform = mockMvc.perform(
                get("/api/vocabulary/" + sharedVocabulary.getId())
//                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.writer.id").exists())
                .andExpect(jsonPath("data.writer.nickname").exists())
                .andExpect(jsonPath("data.category.id").exists())
                .andExpect(jsonPath("data.category.name").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileId").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileName").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileType").exists())
                .andExpect(jsonPath("data.thumbnailInfo.size").exists())
                .andExpect(jsonPath("data.title").exists())
                .andExpect(jsonPath("data.mainLanguage").exists())
                .andExpect(jsonPath("data.subLanguage").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileId").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileName").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileType").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.size").exists())
                .andExpect(jsonPath("data.wordList[0].mainWord").exists())
                .andExpect(jsonPath("data.wordList[0].subWord").exists())
                .andExpect(jsonPath("data.wordList[0].memorisedCheck").exists())
                .andExpect(jsonPath("data.difficulty").exists())
                .andExpect(jsonPath("data.views").exists())
                .andExpect(jsonPath("data.likeCount").exists())
                .andExpect(jsonPath("data.downloadCount").exists())
                .andExpect(jsonPath("data.totalWordCount").exists())
                .andExpect(jsonPath("data.division").value(VocabularyDivision.SHARED.name()))
                .andExpect(jsonPath("data.allowModificationAndDeletion").value(false))
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("message").value(MessageVo.GET_SHARED_VOCABULARY_SUCCESSFULLY))
        ;

    }

    @Test
    @DisplayName("자신이 공유한 단어장에 공유 단어장 조회")
    public void getSharedVocabulary_SelfGet() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        final Category sharedCategory = createCategory(null, CategoryDivision.SHARED, "shared category", null, 1);

        final Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), sharedCategory.getId());

        //when
        final ResultActions perform = mockMvc.perform(
                get("/api/vocabulary/" + sharedVocabulary.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.writer.id").exists())
                .andExpect(jsonPath("data.writer.nickname").exists())
                .andExpect(jsonPath("data.category.id").exists())
                .andExpect(jsonPath("data.category.name").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileId").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileName").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileType").exists())
                .andExpect(jsonPath("data.thumbnailInfo.size").exists())
                .andExpect(jsonPath("data.title").exists())
                .andExpect(jsonPath("data.mainLanguage").exists())
                .andExpect(jsonPath("data.subLanguage").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileId").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileName").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileType").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.size").exists())
                .andExpect(jsonPath("data.wordList[0].mainWord").exists())
                .andExpect(jsonPath("data.wordList[0].subWord").exists())
                .andExpect(jsonPath("data.wordList[0].memorisedCheck").exists())
                .andExpect(jsonPath("data.difficulty").exists())
                .andExpect(jsonPath("data.views").exists())
                .andExpect(jsonPath("data.likeCount").exists())
                .andExpect(jsonPath("data.downloadCount").exists())
                .andExpect(jsonPath("data.totalWordCount").exists())
                .andExpect(jsonPath("data.division").value(VocabularyDivision.SHARED.name()))
                .andExpect(jsonPath("data.allowModificationAndDeletion").value(true))
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("message").value(MessageVo.GET_SHARED_VOCABULARY_SUCCESSFULLY))
        ;

    }

    @Test
    @DisplayName("USER 권한의 사용자가 공유가 해제된 단어장을 조회")
    public void getUnsharedVocabulary_By_User() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        final TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        final Category sharedCategory = createCategory(null, CategoryDivision.SHARED, "shared category", null, 1);

        final Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), sharedCategory.getId());

        vocabularyService.unshared(sharedVocabulary.getId());

        em.flush();
        em.clear();

        //when
        final ResultActions perform = mockMvc.perform(
                get("/api/vocabulary/" + sharedVocabulary.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.GET_UNSHARED_VOCABULARY_BY_USER));

    }

    @Test
    @DisplayName("관리자 권한에 의해 공유가 해제된 단어장을 조회")
    public void getUnsharedVocabulary_By_Admin() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final Category sharedCategory = createCategory(null, CategoryDivision.SHARED, "shared category", null, 1);

        final Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), sharedCategory.getId());

        vocabularyService.unshared(sharedVocabulary.getId());

        em.flush();
        em.clear();

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("adminMember", "adminMember");
        final Member admin = memberService.adminJoin(memberCreateDto1);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(admin.getLoginInfo().getRefreshToken());
        final TokenDto adminTokenDto = memberService.refresh(onlyTokenDto);

        //when
        final ResultActions perform = mockMvc.perform(
                get("/api/vocabulary/" + sharedVocabulary.getId())
                        .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
        ).andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.writer.id").exists())
                .andExpect(jsonPath("data.writer.nickname").exists())
                .andExpect(jsonPath("data.category.id").exists())
                .andExpect(jsonPath("data.category.name").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileId").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileName").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileType").exists())
                .andExpect(jsonPath("data.thumbnailInfo.size").exists())
                .andExpect(jsonPath("data.title").exists())
                .andExpect(jsonPath("data.mainLanguage").exists())
                .andExpect(jsonPath("data.subLanguage").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileId").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileName").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileType").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.size").exists())
                .andExpect(jsonPath("data.wordList[0].mainWord").exists())
                .andExpect(jsonPath("data.wordList[0].subWord").exists())
                .andExpect(jsonPath("data.wordList[0].memorisedCheck").exists())
                .andExpect(jsonPath("data.difficulty").exists())
                .andExpect(jsonPath("data.views").exists())
                .andExpect(jsonPath("data.likeCount").exists())
                .andExpect(jsonPath("data.downloadCount").exists())
                .andExpect(jsonPath("data.totalWordCount").exists())
                .andExpect(jsonPath("data.division").value(VocabularyDivision.UNSHARED.name()))
                .andExpect(jsonPath("data.allowModificationAndDeletion").value(false))
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("message").value(MessageVo.GET_UNSHARED_VOCABULARY_BY_ADMIN))
        ;

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 특정 회원이 공유한 단어장 목록 조회")
    public void getSharedVocabularyListByMember() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Category sharedCategory = createCategory(null, CategoryDivision.SHARED, "shared category", null, 1);
        final Category sharedCategory2 = createCategory(null, CategoryDivision.SHARED, "shared category2", null, 2);


        for (int i = 0; i < 5; i++) {
            final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample, "personal to shared vocabulary" + i);
            vocabularyService.share(personalVocabularySample.getId(), sharedCategory.getId());
            final Vocabulary personalVocabularySample1 = createPersonalVocabularySample(user1, personalCategorySample, "personal to shared vocabulary" + i * 4);
            vocabularyService.share(personalVocabularySample1.getId(), sharedCategory2.getId());
        }

        //when
        final ResultActions perform = mockMvc.perform(
                get("/api/vocabulary/shared/" + user1.getId())
        ).andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.list[0].id").exists())
                .andExpect(jsonPath("data.list[0].writer.id").exists())
                .andExpect(jsonPath("data.list[0].writer.nickname").exists())
                .andExpect(jsonPath("data.list[0].category.id").exists())
                .andExpect(jsonPath("data.list[0].category.name").exists())
                .andExpect(jsonPath("data.list[0].thumbnailInfo.fileId").exists())
                .andExpect(jsonPath("data.list[0].thumbnailInfo.fileName").exists())
                .andExpect(jsonPath("data.list[0].thumbnailInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.list[0].thumbnailInfo.fileType").exists())
                .andExpect(jsonPath("data.list[0].thumbnailInfo.size").exists())
                .andExpect(jsonPath("data.list[0].title").exists())
                .andExpect(jsonPath("data.list[0].mainLanguage").exists())
                .andExpect(jsonPath("data.list[0].subLanguage").exists())
                .andExpect(jsonPath("data.list[0].difficulty").exists())
                .andExpect(jsonPath("data.list[0].views").exists())
                .andExpect(jsonPath("data.list[0].likeCount").exists())
                .andExpect(jsonPath("data.list[0].downloadCount").exists())
                .andExpect(jsonPath("data.list[0].totalWordCount").exists())
                .andExpect(jsonPath("data.list[0].division").exists())
                .andExpect(jsonPath("data.list[0].registerDate").exists())
                .andExpect(jsonPath("data.paging.totalCount").exists())
                .andExpect(jsonPath("data.paging.criteriaDto.pageNum").exists())
                .andExpect(jsonPath("data.paging.criteriaDto.limit").exists())
                .andExpect(jsonPath("data.paging.startPage").exists())
                .andExpect(jsonPath("data.paging.endPage").exists())
                .andExpect(jsonPath("data.paging.prev").exists())
                .andExpect(jsonPath("data.paging.totalPage").exists())
                .andExpect(jsonPath("message").value(MessageVo.GET_SHARED_VOCABULARY_LIST_BY_MEMBER_SUCCESSFULLY))
        ;

    }

    @Test
    @DisplayName("특정 회원이 공유한 단어장 목록 조회 시 페이징 값을 잘 못 입력한 경우")
    public void getSharedVocabularyListByMember_WrongCriteria() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Category sharedCategory = createCategory(null, CategoryDivision.SHARED, "shared category", null, 1);
        final Category sharedCategory2 = createCategory(null, CategoryDivision.SHARED, "shared category2", null, 2);


        for (int i = 0; i < 5; i++) {
            final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample, "personal to shared vocabulary" + i);
            vocabularyService.share(personalVocabularySample.getId(), sharedCategory.getId());
            final Vocabulary personalVocabularySample1 = createPersonalVocabularySample(user1, personalCategorySample, "personal to shared vocabulary" + i * 4);
            vocabularyService.share(personalVocabularySample1.getId(), sharedCategory2.getId());
        }

        //when
        final ResultActions perform = mockMvc.perform(
                get("/api/vocabulary/shared/" + user1.getId())
                        .param("pageNum", "-1")
                        .param("limit", "-1")
        ).andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].code").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].field").exists())
                .andExpect(jsonPath("$[0].rejectedValue").exists())
        ;

    }

    @Test
    @DisplayName("전체 공유 단어장 목록 조회")
    public void getSharedVocabularyList() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategory = createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1);

        final Category sharedCategory1 = createCategory(user1, CategoryDivision.SHARED, "shared category 1", null, 1);
        final Category sharedCategory2 = createCategory(user1, CategoryDivision.SHARED, "shared category 2", null, 2);

        List<Vocabulary> sharedVocabularyList1 = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final Vocabulary vocabulary = createPersonalVocabularySample(user1, personalCategory, "shared category 1 vocabulary" + new Random().nextInt(1000));
            final Vocabulary share = vocabularyService.share(vocabulary.getId(), sharedCategory1.getId());
            sharedVocabularyList1.add(share);
        }

        List<Vocabulary> sharedVocabularyList2 = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final Vocabulary vocabulary = createPersonalVocabularySample(user1, personalCategory, "shared category 2 vocabulary" + new Random().nextInt(1000));
            final Vocabulary share = vocabularyService.share(vocabulary.getId(), sharedCategory2.getId());
            sharedVocabularyList2.add(share);
        }

        em.flush();
        em.clear();

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        final TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        vocabularyLikeService.like(user2.getId(), sharedVocabularyList1.get(2).getId());
        vocabularyLikeService.like(user2.getId(), sharedVocabularyList1.get(0).getId());
        vocabularyLikeService.like(user2.getId(), sharedVocabularyList2.get(3).getId());
        vocabularyLikeService.like(user2.getId(), sharedVocabularyList2.get(4).getId());

        em.flush();
        em.clear();

        final SharedVocabularySearchDto searchDto = SharedVocabularySearchDto.builder()
//                .categoryId(sharedCategory1.getId())
                .criteriaDto(new CriteriaDto(1, 20))
                .sortCondition(VocabularySortCondition.LATEST_DESC)
//                .title()
                .build();
        //when
        final ResultActions perform = mockMvc.perform(
                get("/api/vocabulary/shared")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchDto))
        ).andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.list[0].id").exists())
                .andExpect(jsonPath("data.list[0].writer.id").exists())
                .andExpect(jsonPath("data.list[0].writer.nickname").exists())
                .andExpect(jsonPath("data.list[0].category.id").exists())
                .andExpect(jsonPath("data.list[0].category.name").exists())
                .andExpect(jsonPath("data.list[0].thumbnailInfo.fileId").exists())
                .andExpect(jsonPath("data.list[0].thumbnailInfo.fileName").exists())
                .andExpect(jsonPath("data.list[0].thumbnailInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.list[0].thumbnailInfo.fileType").exists())
                .andExpect(jsonPath("data.list[0].thumbnailInfo.size").exists())
                .andExpect(jsonPath("data.list[0].title").exists())
                .andExpect(jsonPath("data.list[0].mainLanguage").exists())
                .andExpect(jsonPath("data.list[0].subLanguage").exists())
                .andExpect(jsonPath("data.list[0].difficulty").exists())
                .andExpect(jsonPath("data.list[0].views").exists())
                .andExpect(jsonPath("data.list[0].likeCount").exists())
                .andExpect(jsonPath("data.list[0].downloadCount").exists())
                .andExpect(jsonPath("data.list[0].totalWordCount").exists())
                .andExpect(jsonPath("data.list[0].division").exists())
                .andExpect(jsonPath("data.list[0].registerDate").exists())
                .andExpect(jsonPath("data.paging.totalCount").exists())
                .andExpect(jsonPath("data.paging.criteriaDto.pageNum").exists())
                .andExpect(jsonPath("data.paging.criteriaDto.limit").exists())
                .andExpect(jsonPath("data.paging.startPage").exists())
                .andExpect(jsonPath("data.paging.endPage").exists())
                .andExpect(jsonPath("data.paging.prev").exists())
                .andExpect(jsonPath("data.paging.next").exists())
                .andExpect(jsonPath("data.paging.totalPage").exists())
                .andExpect(jsonPath("message").value(MessageVo.GET_SHARED_VOCABULARY_LIST_SUCCESSFULLY))
        ;

    }

    @Test
    @DisplayName("전체 공유 단어장 목록 조회 시 페이징 값을 잘못 입력한 경우")
    public void getSharedVocabularyList_WrongCriteria() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategory = createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1);

        final Category sharedCategory1 = createCategory(user1, CategoryDivision.SHARED, "shared category 1", null, 1);
        final Category sharedCategory2 = createCategory(user1, CategoryDivision.SHARED, "shared category 2", null, 2);

        List<Vocabulary> sharedVocabularyList1 = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final Vocabulary vocabulary = createPersonalVocabularySample(user1, personalCategory, "shared category 1 vocabulary" + new Random().nextInt(1000));
            final Vocabulary share = vocabularyService.share(vocabulary.getId(), sharedCategory1.getId());
            sharedVocabularyList1.add(share);
        }

        List<Vocabulary> sharedVocabularyList2 = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final Vocabulary vocabulary = createPersonalVocabularySample(user1, personalCategory, "shared category 2 vocabulary" + new Random().nextInt(1000));
            final Vocabulary share = vocabularyService.share(vocabulary.getId(), sharedCategory2.getId());
            sharedVocabularyList2.add(share);
        }

        em.flush();
        em.clear();

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        final TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        vocabularyLikeService.like(user2.getId(), sharedVocabularyList1.get(2).getId());
        vocabularyLikeService.like(user2.getId(), sharedVocabularyList1.get(0).getId());
        vocabularyLikeService.like(user2.getId(), sharedVocabularyList2.get(3).getId());
        vocabularyLikeService.like(user2.getId(), sharedVocabularyList2.get(4).getId());

        em.flush();
        em.clear();

        final SharedVocabularySearchDto searchDto = SharedVocabularySearchDto.builder()
//                .categoryId(sharedCategory1.getId())
                .criteriaDto(new CriteriaDto(-1, -1))
                .sortCondition(VocabularySortCondition.LATEST_DESC)
//                .title()
                .build();
        //when
        final ResultActions perform = mockMvc.perform(
                get("/api/vocabulary/shared")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchDto))
        ).andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].code").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].field").exists())
                .andExpect(jsonPath("$[0].rejectedValue").exists())
        ;

    }

    @Test
    @DisplayName("공유 단어장 다운로드")
    public void download() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final Category sharedCategory = createCategory(user1, CategoryDivision.SHARED, "shared category", null, 1);

        final Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), sharedCategory.getId());

        final String title = sharedVocabulary.getTitle();
        final LanguageType mainLanguage = sharedVocabulary.getMainLanguage();
        final LanguageType subLanguage = sharedVocabulary.getSubLanguage();
        final int difficulty = sharedVocabulary.getDifficulty();

        em.flush();
        em.clear();

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final Category user2Category = createCategory(user2, CategoryDivision.PERSONAL, "user2 category", null, 1);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        final TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        final ResultActions perform = mockMvc.perform(
                post("/api/vocabulary/shared/download/" + sharedVocabulary.getId())
                        .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                        .param("categoryId", user2Category.getId().toString())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.writer.id").value(user1.getId()))
                .andExpect(jsonPath("data.writer.nickname").value(user1.getNickname()))
                .andExpect(jsonPath("data.category.id").value(user2Category.getId()))
                .andExpect(jsonPath("data.category.name").value(user2Category.getName()))
                .andExpect(jsonPath("data.thumbnailInfo.fileId").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileName").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.thumbnailInfo.fileType").exists())
                .andExpect(jsonPath("data.thumbnailInfo.size").exists())
                .andExpect(jsonPath("data.title").value(title))
                .andExpect(jsonPath("data.mainLanguage").value(mainLanguage.name()))
                .andExpect(jsonPath("data.subLanguage").value(subLanguage.name()))
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileId").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileName").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileDownloadUri").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.fileType").exists())
                .andExpect(jsonPath("data.wordList[0].imageInfo.size").exists())
                .andExpect(jsonPath("data.wordList[0].mainWord").exists())
                .andExpect(jsonPath("data.wordList[0].subWord").exists())
                .andExpect(jsonPath("data.wordList[0].memorisedCheck").exists())
                .andExpect(jsonPath("data.difficulty").value(difficulty))
                .andExpect(jsonPath("data.memorisedCount").value(0))
                .andExpect(jsonPath("data.totalWordCount").value(wordListSample.size()))
                .andExpect(jsonPath("data.division").value(VocabularyDivision.COPIED.name()))
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("message").value(MessageVo.DOWNLOAD_SHARED_VOCABULARY_SUCCESSFULLY))
        ;

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 단어장을 다운로드 받는 경우")
    public void downloadSharedVocabulary_Unauthenticated() throws Exception {
        //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final Category sharedCategory = createCategory(user1, CategoryDivision.SHARED, "shared category", null, 1);

        final Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), sharedCategory.getId());

        final String title = sharedVocabulary.getTitle();
        final LanguageType mainLanguage = sharedVocabulary.getMainLanguage();
        final LanguageType subLanguage = sharedVocabulary.getSubLanguage();
        final int difficulty = sharedVocabulary.getDifficulty();

        em.flush();
        em.clear();

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final Category user2Category = createCategory(user2, CategoryDivision.PERSONAL, "user2 category", null, 1);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        final TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        final ResultActions perform = mockMvc.perform(
                post("/api/vocabulary/shared/download/" + sharedVocabulary.getId())
//                        .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                        .param("categoryId", user2Category.getId().toString())
        ).andDo(print());

        //then
        perform
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("단어장 다운로드 시 다운로드 받을 단어장을 찾을 수 없는 경우")
    public void downloadSharedVocabulary_VocabularyNotFound() throws Exception {
           //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final Category sharedCategory = createCategory(user1, CategoryDivision.SHARED, "shared category", null, 1);

        final Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), sharedCategory.getId());

        final String title = sharedVocabulary.getTitle();
        final LanguageType mainLanguage = sharedVocabulary.getMainLanguage();
        final LanguageType subLanguage = sharedVocabulary.getSubLanguage();
        final int difficulty = sharedVocabulary.getDifficulty();

        em.flush();
        em.clear();

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final Category user2Category = createCategory(user2, CategoryDivision.PERSONAL, "user2 category", null, 1);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        final TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        final ResultActions perform = mockMvc.perform(
                post("/api/vocabulary/shared/download/" + 10000L)
                        .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                        .param("categoryId", user2Category.getId().toString())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new VocabularyNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("단어장 다운로드 시 다운로드 받을 개인 카테고리를 찾을 수 없는 경우")
    public void downloadSharedVocabulary_PersonalCategoryNotFound() throws Exception {
           //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final Category sharedCategory = createCategory(user1, CategoryDivision.SHARED, "shared category", null, 1);

        final Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), sharedCategory.getId());

        final String title = sharedVocabulary.getTitle();
        final LanguageType mainLanguage = sharedVocabulary.getMainLanguage();
        final LanguageType subLanguage = sharedVocabulary.getSubLanguage();
        final int difficulty = sharedVocabulary.getDifficulty();

        em.flush();
        em.clear();

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final Category user2Category = createCategory(user2, CategoryDivision.PERSONAL, "user2 category", null, 1);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        final TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        final ResultActions perform = mockMvc.perform(
                post("/api/vocabulary/shared/download/" + sharedVocabulary.getId())
                        .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                        .param("categoryId", "1000000")
        ).andDo(print());


        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new CategoryNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("다운로드 받을 단어장이 공유 단어장이 아닌 경우")
    public void downloadSharedVocabulary_VocabularyIsNotShared() throws Exception {
           //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final Category sharedCategory = createCategory(user1, CategoryDivision.SHARED, "shared category", null, 1);

        final Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), sharedCategory.getId());

        final String title = sharedVocabulary.getTitle();
        final LanguageType mainLanguage = sharedVocabulary.getMainLanguage();
        final LanguageType subLanguage = sharedVocabulary.getSubLanguage();
        final int difficulty = sharedVocabulary.getDifficulty();

        em.flush();
        em.clear();

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final Category user2Category = createCategory(user2, CategoryDivision.PERSONAL, "user2 category", null, 1);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        final TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        final ResultActions perform = mockMvc.perform(
                post("/api/vocabulary/shared/download/" + personalVocabularySample.getId())
                        .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                        .param("categoryId", user2Category.getId().toString())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("구분이 잘못된 요청입니다. : 공유 단어장이 아닐 경우 다운로드가 불가능합니다."))
        ;

    }

    @Test
    @DisplayName("다운로드 시 다운로드 받을 카테고리가 개인 카테고리가 아닌 경우")
    public void downloadSharedVocabulary_TargetCategoryIsNotShared() throws Exception {
           //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final Category sharedCategory = createCategory(user1, CategoryDivision.SHARED, "shared category", null, 1);

        final Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), sharedCategory.getId());

        final String title = sharedVocabulary.getTitle();
        final LanguageType mainLanguage = sharedVocabulary.getMainLanguage();
        final LanguageType subLanguage = sharedVocabulary.getSubLanguage();
        final int difficulty = sharedVocabulary.getDifficulty();

        em.flush();
        em.clear();

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final Category user2Category = createCategory(user2, CategoryDivision.PERSONAL, "user2 category", null, 1);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        final TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        final ResultActions perform = mockMvc.perform(
                post("/api/vocabulary/shared/download/" + sharedVocabulary.getId())
                        .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                        .param("categoryId", sharedCategory.getId().toString())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("구분이 잘못된 요청입니다. : 공유 카테고리에는 공유 단어장을 다운로드 할 수 없습니다. 카테고리를 다시 확인해주세요."))
        ;

    }

    @Test
    @DisplayName("다운로드 받을 회원과 다운로드 받을 카테고리를 소유한 회원이 다를 경우")
    public void downloadSharedVocabulary_MemberAndCategoryMemberDifferentException() throws Exception {
           //given
        final MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        final Member user1 = memberService.userJoin(memberCreateDto);

        final Category personalCategorySample = createPersonalCategorySample(user1);

        final Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        final List<WordRequestDto> wordListSample = createWordListSample();

        vocabularyService.updateWordListOfPersonalVocabulary(personalVocabularySample.getId(), wordListSample);

        em.flush();
        em.clear();

        final Category sharedCategory = createCategory(user1, CategoryDivision.SHARED, "shared category", null, 1);

        final Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), sharedCategory.getId());

        final String title = sharedVocabulary.getTitle();
        final LanguageType mainLanguage = sharedVocabulary.getMainLanguage();
        final LanguageType subLanguage = sharedVocabulary.getSubLanguage();
        final int difficulty = sharedVocabulary.getDifficulty();

        em.flush();
        em.clear();

        final MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        final Member user2 = memberService.userJoin(memberCreateDto1);

        final Category user2Category = createCategory(user2, CategoryDivision.PERSONAL, "user2 category", null, 1);

        final OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        final TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        final ResultActions perform = mockMvc.perform(
                post("/api/vocabulary/shared/download/" + sharedVocabulary.getId())
                        .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                        .param("categoryId", personalCategorySample.getId().toString())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new MemberAndCategoryMemberDifferentException().getMessage()));

    }

    @Test
    @DisplayName("개인 단어장 삭제")
    public void deletePersonalVocabulary() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategorySample = createPersonalCategorySample(user1);

        Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc.perform(
                delete("/api/vocabulary/personal/" + personalVocabularySample.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
        ).andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value(MessageVo.DELETE_PERSONAL_VOCABULARY_SUCCESSFULLY))
        ;

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 단어장 삭제")
    public void deletePersonalVocabulary_Unauthenticated() throws Exception {
          //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategorySample = createPersonalCategorySample(user1);

        Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc.perform(
                delete("/api/vocabulary/personal/" + personalVocabularySample.getId())
//                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("다운로드 받은 단어장 삭제")
    public void deleteDownloadVocabulary() throws Exception {
          //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategorySample = createPersonalCategorySample(user1);

        Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), null);

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        Member user2 = memberService.userJoin(memberCreateDto1);

        Vocabulary downloadVocabulary = vocabularyService.download(sharedVocabulary.getId(), user2.getId(), null);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc.perform(
                delete("/api/vocabulary/personal/" + downloadVocabulary.getId())
                        .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
        ).andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value(MessageVo.DELETE_PERSONAL_VOCABULARY_SUCCESSFULLY))
        ;

    }

    @Test
    @DisplayName("다른 회원의 단어장을 삭제하는 경우")
    public void deletePersonalVocabulary_Of_DifferentMember() throws Exception {
          //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategorySample = createPersonalCategorySample(user1);

        Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        Member user2 = memberService.userJoin(memberCreateDto1);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc.perform(
                delete("/api/vocabulary/personal/" + personalVocabularySample.getId())
                        .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.DELETE_PERSONAL_VOCABULARY_OF_DIFFERENT_MEMBER));

    }

    @Test
    @DisplayName("공유 단어장을 삭제하는 경우")
    public void deleteSharedVocabulary() throws Exception {
          //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategorySample = createPersonalCategorySample(user1);

        Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), null);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc.perform(
                delete("/api/vocabulary/personal/" + sharedVocabulary.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
        .andExpect(jsonPath("message").value("구분이 잘못된 요청입니다. : 개인 단어장에 속한 단어장이 아니면 삭제가 불가능합니다."))
        ;

    }

    @Test
    @DisplayName("단어장 삭제 시 단어장을 찾을 수 없는 경우")
    public void deletePersonalVocabulary_VocabularyNotFound() throws Exception {
          //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategorySample = createPersonalCategorySample(user1);

        Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc.perform(
                delete("/api/vocabulary/personal/" + 10000L)
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new VocabularyNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("이미 삭제된 단어장을 삭제하는 경우")
    public void deleteDeletedVocabulary() throws Exception {
          //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategorySample = createPersonalCategorySample(user1);

        Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        vocabularyService.deletePersonalVocabulary(personalVocabularySample.getId());

        em.flush();
        em.clear();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc.perform(
                delete("/api/vocabulary/personal/" + personalVocabularySample.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("구분이 잘못된 요청입니다. : 개인 단어장에 속한 단어장이 아니면 삭제가 불가능합니다."))
        ;

    }

    @Test
    @DisplayName("공유 단어장 공유 해제")
    public void unsharedSharedVocabulary() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategorySample = createPersonalCategorySample(user1);

        Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), null);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc.perform(
                delete("/api/vocabulary/shared/" + sharedVocabulary.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
        ).andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value(MessageVo.UNSHARED_SHARED_VOCABULARY_SUCCESSFULLY));

    }

    @Test
    @DisplayName("다른 회원이 공유한 단어장을 공유 해제하는 경우")
    public void unsharedSharedVocabulary_Of_DifferentMember() throws Exception {
         //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategorySample = createPersonalCategorySample(user1);

        Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), null);

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        Member user2 = memberService.userJoin(memberCreateDto1);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc.perform(
                delete("/api/vocabulary/shared/" + sharedVocabulary.getId())
                        .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.UNSHARED_SHARED_VOCABULARY_OF_DIFFERENT_MEMBER));

    }

    @Test
    @DisplayName("공유 단어장 공유 해제 시 단어장을 찾을 수 없는 경우")
    public void unsharedSharedVocabulary_VocabularyNotFound() throws Exception {
         //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategorySample = createPersonalCategorySample(user1);

        Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), null);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc.perform(
                delete("/api/vocabulary/shared/" + 10000L)
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new VocabularyNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("개인 단어장을 공유 해제하는 경우")
    public void unsharedPersonalVocabulary() throws Exception {
         //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategorySample = createPersonalCategorySample(user1);

        Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), null);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc.perform(
                delete("/api/vocabulary/shared/" + personalVocabularySample.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("구분이 잘못된 요청입니다. : 공유 단어장 외에는 공유를 취소할 수 없습니다."))
        ;

    }

    @Test
    @DisplayName("이미 공유 해제 상태인 단어장을 공유 해제 하는 경우")
    public void unshared_UnsharedVocabulary() throws Exception {
         //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategorySample = createPersonalCategorySample(user1);

        Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), null);

        vocabularyService.unshared(sharedVocabulary.getId());

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc.perform(
                delete("/api/vocabulary/shared/" + sharedVocabulary.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("구분이 잘못된 요청입니다. : 공유 단어장 외에는 공유를 취소할 수 없습니다."));

    }

    @Test
    @DisplayName("공유 단어장에 좋아요 등록")
    public void addLikeToSharedVocabulary() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategorySample = createPersonalCategorySample(user1);

        Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), null);

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        Member user2 = memberService.userJoin(memberCreateDto1);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc.perform(
                post("/api/vocabulary/shared/like/" + sharedVocabulary.getId())
                        .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
        ).andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value(MessageVo.ADD_LIKE_TO_SHARED_VOCABULARY_SUCCESSFULLY));

        boolean existLike = vocabularyLikeService.getExistLike(user2.getId(), sharedVocabulary.getId());
        assertTrue(existLike);

    }

    @Test
    @DisplayName("공유 단어장에 좋아요 등록 시 단어장을 찾을 수 없는 경우")
    public void addLikeToSharedVocabulary_VocabularyNotFound() throws Exception {
          //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategorySample = createPersonalCategorySample(user1);

        Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), null);

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        Member user2 = memberService.userJoin(memberCreateDto1);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc.perform(
                post("/api/vocabulary/shared/like/" + 10000L)
                        .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new VocabularyNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("개인 단어장에 좋아요를 등록하는 경우")
    public void addLikeToPersonalVocabulary() throws Exception {
          //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategorySample = createPersonalCategorySample(user1);

        Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), null);

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        Member user2 = memberService.userJoin(memberCreateDto1);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc.perform(
                post("/api/vocabulary/shared/like/" + personalVocabularySample.getId())
                        .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("구분이 잘못된 요청입니다. : 공유 단어장 외에는 좋아요를 등록하는 것이 불가능합니다. 공유가 해제된 단어장 또한 좋아요 등록이 불가능합니다."));

    }

    @Test
    @DisplayName("공유가 해제된 단어장에 좋아요를 등록하는 경우")
    public void addLikeToUnsharedVocabulary() throws Exception {
          //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategorySample = createPersonalCategorySample(user1);

        Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), null);

        vocabularyService.unshared(sharedVocabulary.getId());

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        Member user2 = memberService.userJoin(memberCreateDto1);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc.perform(
                post("/api/vocabulary/shared/like/" + sharedVocabulary.getId())
                        .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
        ).andDo(print());


        //then
        perform.andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("구분이 잘못된 요청입니다. : 공유 단어장 외에는 좋아요를 등록하는 것이 불가능합니다. 공유가 해제된 단어장 또한 좋아요 등록이 불가능합니다."));

    }

    @Test
    @DisplayName("공유 단어장에 좋아요 등록 시 이미 좋아요를 등록한 단어장인 경우")
    public void addLikeToSharedVocabulary_ExistLike() throws Exception {
          //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategorySample = createPersonalCategorySample(user1);

        Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), null);

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        Member user2 = memberService.userJoin(memberCreateDto1);

        vocabularyLikeService.like(user2.getId(), sharedVocabulary.getId());

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc.perform(
                post("/api/vocabulary/shared/like/" + sharedVocabulary.getId())
                        .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new ExistLikeException().getMessage()));

    }

    @Test
    @DisplayName("자기 자신이 공유한 단어장에 좋아요를 등록하는 경우")
    public void addLikeToSharedVocabulary_SelfLike() throws Exception {
          //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category personalCategorySample = createPersonalCategorySample(user1);

        Vocabulary personalVocabularySample = createPersonalVocabularySample(user1, personalCategorySample);

        Vocabulary sharedVocabulary = vocabularyService.share(personalVocabularySample.getId(), null);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc.perform(
                post("/api/vocabulary/shared/like/" + sharedVocabulary.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
        ).andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new SelfLikeException().getMessage()));

    }

    private List<WordRequestDto> createWordListSample() throws IOException {
        List<WordRequestDto> wordRequestDtoList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            MockMultipartFile mockMultipartFile1 = getMockMultipartFile("file", testImageFilePath);
            WordImageFile wordImageFile = wordFileStorageService.uploadWordImageFile(mockMultipartFile1);

            WordRequestDto wordRequestDto = WordRequestDto.builder()
                    .imageFileId(wordImageFile.getId())
                    .mainWord("main" + i)
                    .subWord("sub" + i)
                    .memorisedCheck(false)
                    .build();

            wordRequestDtoList.add(wordRequestDto);
        }
        return wordRequestDtoList;
    }

    private Vocabulary createPersonalVocabularySample(Member user1, Category personalCategory, String title) throws IOException {
        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);
        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        int difficulty = 5;
        LanguageType mainLanguage = LanguageType.KOREAN;
        LanguageType subLanguage = LanguageType.ENGLISH;
        VocabularyCreateDto vocabularyCreateDto = VocabularyCreateDto.builder()
                .title(title)
                .difficulty(difficulty)
                .mainLanguage(mainLanguage)
                .subLanguage(subLanguage)
                .imageFileId(vocabularyThumbnailImageFile.getId())
                .build();

        return vocabularyService.addPersonalVocabulary(user1.getId(), personalCategory.getId(), vocabularyCreateDto);
    }

    private Vocabulary createPersonalVocabularySample(Member user1, Category personalCategory) throws IOException {
        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);
        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        int difficulty = 5;
        String title = "sample vocabulary";
        LanguageType mainLanguage = LanguageType.KOREAN;
        LanguageType subLanguage = LanguageType.ENGLISH;
        VocabularyCreateDto vocabularyCreateDto = VocabularyCreateDto.builder()
                .title(title)
                .difficulty(difficulty)
                .mainLanguage(mainLanguage)
                .subLanguage(subLanguage)
                .imageFileId(vocabularyThumbnailImageFile.getId())
                .build();

        return vocabularyService.addPersonalVocabulary(user1.getId(), personalCategory.getId(), vocabularyCreateDto);
    }

    private Category createPersonalCategorySample(Member user1) {
        return createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1);
    }
}