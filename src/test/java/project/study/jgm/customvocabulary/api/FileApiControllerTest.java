package project.study.jgm.customvocabulary.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import project.study.jgm.customvocabulary.bbs.upload.BbsUploadFile;
import project.study.jgm.customvocabulary.bbs.upload.exception.BbsUploadFileNotFoundException;
import project.study.jgm.customvocabulary.common.BaseControllerTest;
import project.study.jgm.customvocabulary.common.dto.MessageVo;
import project.study.jgm.customvocabulary.common.upload.exception.DeniedFileExtensionException;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.dto.MemberCreateDto;
import project.study.jgm.customvocabulary.security.dto.OnlyTokenDto;
import project.study.jgm.customvocabulary.security.dto.TokenDto;
import project.study.jgm.customvocabulary.vocabulary.upload.VocabularyThumbnailImageFile;
import project.study.jgm.customvocabulary.vocabulary.word.upload.WordImageFile;
import project.study.jgm.customvocabulary.common.upload.exception.NotImageTypeException;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FileApiControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("게시글에 첨부 파일 목록 등록")
    public void uploadBbsMultipleFiles() throws Exception {
        //given
        MockMultipartFile multipartFile1 = getMockMultipartFile("files", testTextFilePath);
        MockMultipartFile multipartFile2 = getMockMultipartFile("files", testImageFilePath);

        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);


        //when
        ResultActions perform = mockMvc
                .perform(
                        multipart("/api/bbs/uploadMultipleFiles")
                                .file(multipartFile1)
                                .file(multipartFile2)
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data[0].fileId").exists())
                .andExpect(jsonPath("data[0].fileName").exists())
                .andExpect(jsonPath("data[0].fileDownloadUri").exists())
                .andExpect(jsonPath("data[0].fileType").exists())
                .andExpect(jsonPath("data[0].size").exists())
                .andExpect(jsonPath("data[0].fileId").exists())
                .andExpect(jsonPath("message").value(MessageVo.UPLOAD_BBS_FILE_LIST_SUCCESSFULLY))
                .andDo(document("file-bbs-upload-files",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type"),
                                headerWithName(X_AUTH_TOKEN).description(X_AUTH_TOKEN_DESCRIPTION)
                        ),
                        responseFields(
                                fieldWithPath("data[0].fileId").description("업로드된 파일의 식별 ID"),
                                fieldWithPath("data[0].fileName").description("업로드된 파일의 이름"),
                                fieldWithPath("data[0].fileDownloadUri").description("업로드된 파일을 다운로드 받을 수 있는 URI"),
                                fieldWithPath("data[0].fileType").description("업로드된 파일의 파일 타입 (실행 파일등은 업로드 불가능)"),
                                fieldWithPath("data[0].size").description("업로드된 파일의 크기"),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 게시글 파일 등록")
    public void uploadBbsMultipleFiles_UnAuthentication() throws Exception {
        //given
        MockMultipartFile multipartFile1 = getMockMultipartFile("files", testTextFilePath);
        MockMultipartFile multipartFile2 = getMockMultipartFile("files", testImageFilePath);

        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);


        //when
        ResultActions perform = mockMvc
                .perform(
                        multipart("/api/bbs/uploadMultipleFiles")
                                .file(multipartFile1)
                                .file(multipartFile2)
//                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("게시글에 파일 업로드 시 zip 파일을 업로드 하는 경우")
    public void uploadBbsMultipleFiles_UploadZipFile() throws Exception {
        //given
        MockMultipartFile multipartFile1 = getMockMultipartFile("files", testZipFilePath);
        MockMultipartFile multipartFile2 = getMockMultipartFile("files", testImageFilePath);

        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);


        //when
        ResultActions perform = mockMvc
                .perform(
                        multipart("/api/bbs/uploadMultipleFiles")
                                .file(multipartFile1)
                                .file(multipartFile2)
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new DeniedFileExtensionException().getMessage()));

    }

    @Test
    @DisplayName("게시글에 등록된 파일 다운로드")
    public void downloadBbsFile() throws Exception {
        //given
        MockMultipartFile multipartFile = getMockMultipartFile("files", testImageFilePath);

        BbsUploadFile bbsUploadFile = bbsFileStorageService.uploadBbsFile(multipartFile);

        String fileName = bbsUploadFile.getFileName();

        String fileDownloadUri = bbsUploadFile.getFileDownloadUri();
        String decode = URLDecoder.decode(fileDownloadUri, StandardCharsets.UTF_8);

        //when
        ResultActions perform = mockMvc
                .perform(
                        get(decode)
//                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andDo(document("file-bbs-download-file"))
        ;

    }

    @Test
    @DisplayName("파일 다운로드 시 파일 이름으로 파일을 찾을 수 없는 경우")
    public void downloadBbsFile_FileNameNotFound() throws Exception {
        //given
        MockMultipartFile multipartFile = getMockMultipartFile("files", testTextFilePath);

        BbsUploadFile bbsUploadFile = bbsFileStorageService.uploadBbsFile(multipartFile);

        String fileName = bbsUploadFile.getFileName();

        String fileDownloadUri = bbsUploadFile.getFileDownloadUri() + "아무거나 더 붙여서 못찾게 만들기";
        String decode = URLDecoder.decode(fileDownloadUri, StandardCharsets.UTF_8);

        //when
        ResultActions perform = mockMvc
                .perform(
                        get(decode)
//                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new BbsUploadFileNotFoundException().getMessage()))
        ;

    }

    @Test
    @DisplayName("게시글에 등록된 파일의 썸네일 이미지 다운로드")
    public void displayThumbnailOfBbsImage() throws Exception {
        //given
        MockMultipartFile multipartFile = getMockMultipartFile("files", testImageFilePath);

        BbsUploadFile bbsUploadFile = bbsFileStorageService.uploadBbsFile(multipartFile);

        String fileName = bbsUploadFile.getFileName();

        String decode = URLDecoder.decode(fileName, StandardCharsets.UTF_8);

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs/displayThumbnail/" + decode)
//                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andDo(document("file-bbs-display-thumbnail"))
        ;

    }

    @Test
    @DisplayName("파일의 썸네일 이미지를 다운로드 시 파일 이름으로 파일을 찾을 수 없는 경우")
    public void displayThumbnailOfBbsImage_FileNameNotFound() throws Exception {
        //given
        MockMultipartFile multipartFile = getMockMultipartFile("files", testImageFilePath);

        BbsUploadFile bbsUploadFile = bbsFileStorageService.uploadBbsFile(multipartFile);

        String fileName = bbsUploadFile.getFileName() + "아무거나 붙여서 파일 못찾게 하기";

        String decode = URLDecoder.decode(fileName, StandardCharsets.UTF_8);

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs/displayThumbnail/" + decode)
//                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new BbsUploadFileNotFoundException().getMessage()))
        ;

    }

    @Test
    @DisplayName("게시글에 등록된 파일의 썸네일 이미지 다운로드 시 이미지 파일이 아니라 썸네일 파일을 찾을 수 없는 경우")
    public void displayThumbnailOfBbsImage_ThumbnailFileNotFound() throws Exception {
        //given
        MockMultipartFile multipartFile = getMockMultipartFile("files", testTextFilePath);

        BbsUploadFile bbsUploadFile = bbsFileStorageService.uploadBbsFile(multipartFile);


        String fileName = bbsUploadFile.getFileName();

        String decode = URLDecoder.decode(fileName, StandardCharsets.UTF_8);

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs/displayThumbnail/" + decode)
//                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("File not found " + "s_" + fileName))
//                .andExpect(jsonPath("message").value(new BbsUploadFileNotFoundException().getMessage()))
        ;

    }

    @Test
    @DisplayName("단어 이미지 등록")
    public void uploadWordImageFile() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);

        //when
        ResultActions perform = mockMvc.perform(
                multipart("/api/vocabulary/word/uploadImageFile")
                        .file(mockMultipartFile)
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
        ).andDo(print());

        //then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("data.fileId").exists())
                .andExpect(jsonPath("data.fileName").exists())
                .andExpect(jsonPath("data.fileDownloadUri").exists())
                .andExpect(jsonPath("data.fileType").exists())
                .andExpect(jsonPath("data.size").exists())
                .andExpect(jsonPath("data.fileId").exists())
                .andExpect(jsonPath("message").value(MessageVo.UPLOAD_WORD_IMAGE_FILE_SUCCESSFULLY))
                .andDo(document("file-word-upload-image-file",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type"),
                                headerWithName(X_AUTH_TOKEN).description(X_AUTH_TOKEN_DESCRIPTION)
                        ),
                        responseFields(
                                fieldWithPath("data.fileId").description("업로드된 단어 이미지 파일의 식별 ID"),
                                fieldWithPath("data.fileName").description("업로드된 단어 이미지 파일의 이름"),
                                fieldWithPath("data.fileDownloadUri").description("업로드된 단어 이미지 파일의 다운로드 URI"),
                                fieldWithPath("data.fileType").description("업로드된 단어 이미지 파일의 타입 (이미지 파일만 등록 가능)"),
                                fieldWithPath("data.size").description("업로드된 단어 이미지 파일의 크기"),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;

    }

    @Test
    @DisplayName("단어장 이미지 등록")
    public void uploadVocabularyThumbnailImageFile() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);

        //when
        ResultActions perform = mockMvc.perform(
                multipart("/api/vocabulary/uploadImageFile")
                        .file(mockMultipartFile)
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
        ).andDo(print());

        //then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("data.fileId").exists())
                .andExpect(jsonPath("data.fileName").exists())
                .andExpect(jsonPath("data.fileDownloadUri").exists())
                .andExpect(jsonPath("data.fileType").exists())
                .andExpect(jsonPath("data.size").exists())
                .andExpect(jsonPath("data.fileId").exists())
                .andExpect(jsonPath("message").value(MessageVo.UPLOAD_VOCABULARY_THUMBNAIL_IMAGE_FILE_SUCCESSFULLY))
                .andDo(document("file-vocabulary-upload-thumbnail-file",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type"),
                                headerWithName(X_AUTH_TOKEN).description(X_AUTH_TOKEN_DESCRIPTION)
                        ),
                        responseFields(
                                fieldWithPath("data.fileId").description("업로드된 단어장 썸네일 이미지 파일의 식별 ID"),
                                fieldWithPath("data.fileName").description("업로드된 단어장 썸네일 이미지 파일의 이름"),
                                fieldWithPath("data.fileDownloadUri").description("업로드된 단어장 썸네일 이미지 파일의 다운로드 URI"),
                                fieldWithPath("data.fileType").description("업로드된 단어장 썸네일 이미지 파일의 타입 (이미지 파일만 업로드 가능)"),
                                fieldWithPath("data.size").description("업로드된 단어장 썸네일 이미지 파일의 크기"),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 단어장 이미지 등록")
    public void uploadVocabularyThumbnailImageFile_UnAuthentication() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);

        //when
        ResultActions perform = mockMvc.perform(
                multipart("/api/vocabulary/uploadImageFile")
                        .file(mockMultipartFile)
//                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
        ).andDo(print());

        //then
        perform.andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 단어 이미지를 등록하는 경우")
    public void uploadWordImageFile_UnAuthentication() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);

        //when
        ResultActions perform = mockMvc.perform(
                multipart("/api/vocabulary/word/uploadImageFile")
                        .file(mockMultipartFile)
//                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
        ).andDo(print());

        //then
        perform
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("단어 이미지 등록 시 이미지 파일이 아닐 경우")
    public void uploadWordImageFile_NotImageFileException() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testTextFilePath);

        //when
        ResultActions perform = mockMvc.perform(
                multipart("/api/vocabulary/word/uploadImageFile")
                        .file(mockMultipartFile)
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
        ).andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new NotImageTypeException().getMessage()))
        ;

    }

    @Test
    @DisplayName("단어장 이미지 등록 시 이미지 파일이 아닐 경우")
    public void uploadVocabularyThumbnailImageFile_NotImageFileException() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testTextFilePath);

        //when
        ResultActions perform = mockMvc.perform(
                multipart("/api/vocabulary/uploadImageFile")
                        .file(mockMultipartFile)
                        .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
        ).andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new NotImageTypeException().getMessage()))
        ;

    }

    @Test
    @DisplayName("단어에 등록된 이미지 파일 다운로드")
    public void downloadWordFile() throws Exception {
        //given
        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);

        WordImageFile wordImageFile = wordFileStorageService.uploadWordImageFile(mockMultipartFile);

        String fileName = wordImageFile.getFileName();

        String decode = URLDecoder.decode(wordImageFile.getFileDownloadUri(), StandardCharsets.UTF_8);

        //when
        ResultActions perform = mockMvc.perform(
                get(decode)
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        ).andDo(print());

        //then
        perform.andExpect(status().isOk())
                .andDo(document("file-word-download-image-file"))
        ;

    }

    @Test
    @DisplayName("단어장에 등록된 이미지 파일 다운로드")
    public void downloadVocabularyThumbnailImageFile() throws Exception {
        //given
        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);

        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        String fileName = vocabularyThumbnailImageFile.getFileName();

        String decode = URLDecoder.decode(vocabularyThumbnailImageFile.getFileDownloadUri(), StandardCharsets.UTF_8);

        //when
        ResultActions perform = mockMvc.perform(
                get(decode)
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        ).andDo(print());

        //then
        perform.andExpect(status().isOk())
                .andDo(document("file-vocabulary-download-thumbnail-file"))
        ;

    }

    @Test
    @DisplayName("단어에 등록된 이미지의 썸네일 이미지 다운로드")
    public void displayThumbnailOfWordImage() throws Exception {
        //given
        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);

        WordImageFile wordImageFile = wordFileStorageService.uploadWordImageFile(mockMultipartFile);

        String fileName = wordImageFile.getFileName();

        //when
        ResultActions perform = mockMvc.perform(
                get("/api/vocabulary/word/displayThumbnail/" + fileName)
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        ).andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andDo(document("file-word-display-thumbnail"))
        ;

    }

    @Test
    @DisplayName("단어장에 등록된 이미지의 썸네일 이미지 다운로드")
    public void displayThumbnailOfVocabularyThumbnailImage() throws Exception {
        //given
        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);

        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        String fileName = vocabularyThumbnailImageFile.getFileName();

        //when
        ResultActions perform = mockMvc.perform(
                get("/api/vocabulary/displayThumbnail/" + fileName)
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        ).andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andDo(document("file-vocabulary-display-thumbnail"))
        ;

    }

}