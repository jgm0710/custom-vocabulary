package project.study.jgm.customvocabulary.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import project.study.jgm.customvocabulary.common.BaseControllerTest;
import project.study.jgm.customvocabulary.common.dto.MessageVo;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.dto.MemberCreateDto;
import project.study.jgm.customvocabulary.security.dto.OnlyTokenDto;
import project.study.jgm.customvocabulary.security.dto.TokenDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FileApiControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("게시글에 첨부 파일 목록 등록")
    public void uploadBbsMultipleFiles() throws Exception {
        //given
        MockMultipartFile multipartFile1 = getMultipartFile("/static/test/text.txt");
        MockMultipartFile multipartFile2 = getMultipartFile("/static/test/사진1.jpg");

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
                .andExpect(jsonPath("message").value(MessageVo.ADD_FILE_LIST_TO_BBS_SUCCESSFULLY))
        ;

    }

}