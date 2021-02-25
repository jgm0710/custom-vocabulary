package project.study.jgm.customvocabulary.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import project.study.jgm.customvocabulary.common.BaseControllerTest;
import project.study.jgm.customvocabulary.common.dto.MessageVo;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.dto.MemberCreateDto;
import project.study.jgm.customvocabulary.security.dto.OnlyTokenDto;
import project.study.jgm.customvocabulary.security.dto.TokenDto;
import project.study.jgm.customvocabulary.vocabulary.Vocabulary;
import project.study.jgm.customvocabulary.vocabulary.category.Category;
import project.study.jgm.customvocabulary.vocabulary.category.CategoryDivision;
import project.study.jgm.customvocabulary.vocabulary.category.CategoryStatus;
import project.study.jgm.customvocabulary.vocabulary.category.exception.CategoryNotFoundException;
import project.study.jgm.customvocabulary.vocabulary.dto.VocabularyCreateDto;
import project.study.jgm.customvocabulary.vocabulary.exception.DivisionMismatchException;
import project.study.jgm.customvocabulary.vocabulary.exception.VocabularyNotFoundException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
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

        Category personalCategory = createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1, CategoryStatus.REGISTER);

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

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/" + personalCategory.getId())
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

        Category personalCategory = createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1, CategoryStatus.REGISTER);

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

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/" + personalCategory.getId())
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

        Category personalCategory = createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1, CategoryStatus.REGISTER);

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
                        post("/api/vocabulary/" + personalCategory.getId())
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

        Category personalCategory = createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1, CategoryStatus.REGISTER);

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

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/" + 10000L)
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

        Category personalCategory = createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1, CategoryStatus.REGISTER);

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
                .build();

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/" + personalCategory.getId())
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

        Category sharedCategory = createCategory(null, CategoryDivision.SHARED, "shared category", null, 1, CategoryStatus.REGISTER);

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

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/" + sharedCategory.getId())
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

        Category personalCategory = createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1, CategoryStatus.REGISTER);

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
                put("/api/vocabulary/words/" + vocabulary.getId())
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

        Category personalCategory = createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1, CategoryStatus.REGISTER);

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
                post("/api/vocabulary/words/" + vocabulary.getId())
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

        Category personalCategory = createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1, CategoryStatus.REGISTER);

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
                put("/api/vocabulary/words/" + vocabulary.getId())
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

        Category personalCategory = createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1, CategoryStatus.REGISTER);

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
                put("/api/vocabulary/words/" + vocabulary.getId())
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

        Category personalCategory = createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1, CategoryStatus.REGISTER);

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
                put("/api/vocabulary/words/" + 10000L)
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

        Category personalCategory = createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1, CategoryStatus.REGISTER);

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
                put("/api/vocabulary/words/" + vocabulary.getId())
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

        Category personalCategory = createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1, CategoryStatus.REGISTER);

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
                put("/api/vocabulary/words/" + sharedVocabulary.getId())
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(onlyWordRequestListDto))
        )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("구분이 잘못된 요청입니다. : 개인 단어장 이외에는 단어 목록을 수정할 수 없습니다."))
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

        //when

        //then

    }

    @Test
    @DisplayName("다른 회원의 단어장의 단어에 암기 체크를 하는 경우")
    public void checkMemorise_Of_DifferentMember() throws Exception {
        //given

        //when

        //then

    }

    @Test
    @DisplayName("단어 암기 체크 시 단어장을 찾을 수 없는 경우")
    public void checkMemorise_VocabularyNotFound() throws Exception {
        //given

        //when

        //then

    }

    @Test
    @DisplayName("단어 암기 체크 시 단어를 찾을 수 없는 경우")
    public void checkMemorise_WordNotFound() throws Exception {
        //given

        //when

        //then

    }

    @Test
    @DisplayName("공유 단어장에 암기 체크를 하는 경우")
    public void checkMemorise_Of_ShardVocabulary() throws Exception {
        //given

        //when

        //then

    }

    @Test
    @DisplayName("삭제된 단어장의 단어에 암기 체크를 하는 경우")
    public void checkMemorise_Of_DeletedVocabulary() throws Exception {
        //given

        //when

        //then

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

        final Vocabulary personalVocabulary = vocabularyService.addPersonalVocabulary(user1.getId(), personalCategory.getId(), vocabularyCreateDto);
        return personalVocabulary;
    }

    private Category createPersonalCategorySample(Member user1) {
        return createCategory(user1, CategoryDivision.PERSONAL, "personal category", null, 1, CategoryStatus.REGISTER);
    }
}