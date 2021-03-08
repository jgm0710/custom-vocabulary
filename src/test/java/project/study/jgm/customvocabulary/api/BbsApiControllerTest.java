package project.study.jgm.customvocabulary.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import project.study.jgm.customvocabulary.bbs.Bbs;
import project.study.jgm.customvocabulary.bbs.BbsStatus;
import project.study.jgm.customvocabulary.bbs.dto.*;
import project.study.jgm.customvocabulary.bbs.exception.BbsNotFoundException;
import project.study.jgm.customvocabulary.common.BaseControllerTest;
import project.study.jgm.customvocabulary.common.dto.MessageVo;
import project.study.jgm.customvocabulary.common.exception.ExistLikeException;
import project.study.jgm.customvocabulary.common.exception.NoExistLikeException;
import project.study.jgm.customvocabulary.common.exception.SelfLikeException;
import project.study.jgm.customvocabulary.common.upload.OnlyFileIdDto;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.dto.MemberCreateDto;
import project.study.jgm.customvocabulary.security.dto.LoginDto;
import project.study.jgm.customvocabulary.security.dto.OnlyTokenDto;
import project.study.jgm.customvocabulary.security.dto.TokenDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BbsApiControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("게시글 추가")
    public void addBbs() throws Exception {
        //given
        String joinId = "testJoinid";
        String nickname = "test";
        MemberCreateDto memberCreateDto = getMemberCreateDto(joinId, nickname);
        Member userMember = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto tokenDto = memberService.login(loginDto);

        List<OnlyFileIdDto> onlyFileIdDtos = getOnlyFileIdDtos();

        BbsCreateDto bbsCreateDto = BbsCreateDto.builder()
                .title("test bbs")
                .content("test content")
                .fileIdList(onlyFileIdDtos)
                .build();

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/bbs")
                                .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bbsCreateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.writer").exists())
                .andExpect(jsonPath("data.title").exists())
                .andExpect(jsonPath("data.content").exists())
                .andExpect(jsonPath("data.views").exists())
                .andExpect(jsonPath("data.likeCount").exists())
                .andExpect(jsonPath("data.replyCount").exists())
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("data.updateDate").exists())
                .andExpect(jsonPath("data.like").exists())
                .andExpect(jsonPath("data.viewLike").exists())
                .andExpect(jsonPath("data.permissionToDeleteAndModify").exists())
                .andExpect(jsonPath("data.uploadFiles[0].fileId").exists())
                .andExpect(jsonPath("data.uploadFiles[0].fileName").exists())
                .andExpect(jsonPath("data.uploadFiles[0].fileDownloadUri").exists())
                .andExpect(jsonPath("data.uploadFiles[0].fileType").exists())
                .andExpect(jsonPath("data.uploadFiles[0].size").exists())
                .andExpect(jsonPath("message").value(MessageVo.BBS_REGISTERED_SUCCESSFULLY))
                .andDo(document("add-bbs",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type"),
                                headerWithName(X_AUTH_TOKEN).description(X_AUTH_TOKEN_DESCRIPTION)
                        ),
                        requestFields(
                                fieldWithPath("title").description("등록할 게시글의 제목"),
                                fieldWithPath("content").description("등록할 게시글의 내용"),
                                fieldWithPath("fileIdList[0].fileId").description("등록할 게시글에 저장할 업로드 파일의 식별 ID 목록 중 첫 번째 파일 ID")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("data.id").description("등록된 게시글의 식별 ID"),
                                fieldWithPath("data.writer").description("게시글을 작성한 회원의 활동명"),
                                fieldWithPath("data.title").description("등록된 게시글의 제목"),
                                fieldWithPath("data.content").description("등록된 게시글의 내용"),
                                fieldWithPath("data.views").description("등록된 게시글의 조회 수"),
                                fieldWithPath("data.likeCount").description("등록된 게시글의 좋아요 수"),
                                fieldWithPath("data.replyCount").description("등록된 게시글의 댓글 수"),
                                fieldWithPath("data.registerDate").description("등록된 게시글의 작성 날짜"),
                                fieldWithPath("data.updateDate").description("등록된 게시글의 수정 날짜"),
                                fieldWithPath("data.like").description("게시글 등록의 경우 자신이 작성한 게시글이므로 좋아요 등록 및 확인 삭제 권한은 없음"),
                                fieldWithPath("data.viewLike").description("등록된 게시글에 대한 해당 회원의 좋아요 등록 및 확인 권한 여부"),
                                fieldWithPath("data.permissionToDeleteAndModify").description("등록된 게시글에 대한 해당 회원의 수정 및 삭제 권한 여부"),
                                fieldWithPath("data.uploadFiles[0].fileId").description("등록된 게시글에 등록된 파일 목록 중 첫 번째 파일의 식별 ID"),
                                fieldWithPath("data.uploadFiles[0].fileName").description("등록된 게시글에 등록된 파일 목록 중 첫 번째 파일의 이름"),
                                fieldWithPath("data.uploadFiles[0].fileDownloadUri").description("등록된 게시글에 등록된 파일 목록 중 첫 번째 파일의 다운로드 URI"),
                                fieldWithPath("data.uploadFiles[0].fileType").description("등록된 게시글에 등록된 파일 목록 중 첫 번째 파일의 타입"),
                                fieldWithPath("data.uploadFiles[0].size").description("등록된 게시글에 등록된 파일 목록 중 첫 번째 파일의 크기"),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;

    }


    @Test
    @DisplayName("게시글 추가 시 제목이나 내용이 비어 있는 경우")
    public void addBbs_TitleOrContent_Null() throws Exception {
        //given
        String joinId = "testJoinid";
        String nickname = "test";
        MemberCreateDto memberCreateDto = getMemberCreateDto(joinId, nickname);
        Member userMember = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto tokenDto = memberService.login(loginDto);

        BbsCreateDto bbsCreateDto = BbsCreateDto.builder()
//                .title("")
//                .content("")
                .build();

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/bbs")
                                .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bbsCreateDto))
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
    @DisplayName("인증되지 않은 사용자가 게시글을 작성한 경우")
    public void addBbs_Unauthorized() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto tokenDto = memberService.login(loginDto);

        BbsCreateDto bbsCreateDto = BbsCreateDto.builder()
                .title("test bbs")
                .content("test content")
                .build();

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/bbs")
//                                .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bbsCreateDto))
                )
                .andDo(print());


        //then
        perform
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 게시글 목록을 조회하는 경우")
    public void getBbsList_UnAuthentication() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        createBbsList(userMember);

        //when
        mockMvc
                .perform(
                        get("/api/bbs")
                                .param("searchType", BbsSearchType.TITLE.name())
                                .param("keyword", "bbs7")
                                .param("bbsSortType", BbsSortType.REPLY_COUNT_DESC.name())
                                .param("criteriaDto.pageNum", "1")
                                .param("criteriaDto.limit", "20")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.list[0].id").exists())
                .andExpect(jsonPath("data.list[0].writer").exists())
                .andExpect(jsonPath("data.list[0].title").exists())
                .andExpect(jsonPath("data.list[0].views").exists())
                .andExpect(jsonPath("data.list[0].likeCount").exists())
                .andExpect(jsonPath("data.list[0].replyCount").exists())
                .andExpect(jsonPath("data.list[0].registerDate").exists())
                .andExpect(jsonPath("data.paging.totalCount").exists())
                .andExpect(jsonPath("data.paging.criteriaDto.pageNum").value(1))
                .andExpect(jsonPath("data.paging.criteriaDto.limit").value(20))
                .andExpect(jsonPath("data.paging.startPage").exists())
                .andExpect(jsonPath("data.paging.endPage").exists())
                .andExpect(jsonPath("data.paging.endPage").exists())
                .andExpect(jsonPath("data.paging.prev").exists())
                .andExpect(jsonPath("data.paging.next").exists())
                .andExpect(jsonPath("data.paging.totalPage").exists())
                .andExpect(jsonPath("message").value(MessageVo.GET_BBS_LIST_SUCCESSFULLY))
                .andDo(document("get-bbs-list",
                        requestParameters(
                                parameterWithName("searchType").description("게시글 목록 조회 시 검색 조건 :" + br +
                                        "게시글 제목, 게시글 내용, 게시글 제목 또는 내용, 게시글 작성자 "+br+
                                        "[TITLE, CONTENT, TITLE_OR_CONTENT, WRITER]"),
                                parameterWithName("keyword").description("게시글 목록 조회 시 검색 키워드"),
                                parameterWithName("bbsSortType").description("게시글 목록 조회 시 정렬 조건"),
                                parameterWithName("criteriaDto.pageNum").description("게시글 목록 조회 시 몇 페이지를 조회할 것인지 기입"),
                                parameterWithName("criteriaDto.limit").description("게시글 목록 조회 시 몇 개의 결과를 조회할 것인지 기입")
                        ),
                        responseFields(
                                fieldWithPath("data.list[0].id").description("게시글 목록 중 첫 번째 게시글의 식별 ID"),
                                fieldWithPath("data.list[0].writer").description("게시글 목록 중 첫 번째 게시글을 작성한 회원의 활동명"),
                                fieldWithPath("data.list[0].title").description("게시글 목록 중 첫 번째 게시글의 제목"),
                                fieldWithPath("data.list[0].views").description("게시글 목록 중 첫 번째 게시글의 조회수"),
                                fieldWithPath("data.list[0].likeCount").description("게시글 목록 중 첫 번째 게시글의 좋아요 수"),
                                fieldWithPath("data.list[0].replyCount").description("게시글 목록 중 첫 번째 게시글의 댓글 수"),
                                fieldWithPath("data.list[0].registerDate").description("게시글 목록 중 첫 번째 게시글의 등록 날짜"),
                                fieldWithPath("data.paging.totalCount").description("요청한 검색 조건에 의해 조회되는 게시글의 총 개수"),
                                fieldWithPath("data.paging.criteriaDto.pageNum").description("요청한 페이지"),
                                fieldWithPath("data.paging.criteriaDto.limit").description("요청한 검색 결과의 개수"),
                                fieldWithPath("data.paging.startPage").description("현재 요청한 페이지 기준 첫 번째 페이지"),
                                fieldWithPath("data.paging.endPage").description("현재 요청한 페이지 기준 마지막 페이지 "),
                                fieldWithPath("data.paging.prev").description("현재 요청한 페이지 기준 이전 페이지 목록이 있는지 여부"),
                                fieldWithPath("data.paging.next").description("현재 요천항 페이지 기준 다음 페이지 목록이 있는지 여부"),
                                fieldWithPath("data.paging.totalPage").description("요청한 검색 조건에 의해 조회 되는 게시글 목록의 총 페이지 수"),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;
        //then

    }

    @Test
    @DisplayName("USER 권한의 사용자가 게시글 목록을 조회하는 경우")
    public void getBbsList_By_User() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto userTokenDto = memberService.login(loginDto);

        createBbsList(userMember);


        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs")
                                .header(X_AUTH_TOKEN, userTokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.list[0].id").exists())
                .andExpect(jsonPath("data.list[0].writer").exists())
                .andExpect(jsonPath("data.list[0].title").exists())
                .andExpect(jsonPath("data.list[0].views").exists())
                .andExpect(jsonPath("data.list[0].likeCount").exists())
                .andExpect(jsonPath("data.list[0].replyCount").exists())
                .andExpect(jsonPath("data.list[0].registerDate").exists())
                .andExpect(jsonPath("data.paging.totalCount").exists())
                .andExpect(jsonPath("data.paging.criteriaDto.pageNum").value(1))
                .andExpect(jsonPath("data.paging.criteriaDto.limit").value(15))
                .andExpect(jsonPath("data.paging.startPage").exists())
                .andExpect(jsonPath("data.paging.endPage").exists())
                .andExpect(jsonPath("data.paging.endPage").exists())
                .andExpect(jsonPath("data.paging.prev").exists())
                .andExpect(jsonPath("data.paging.next").exists())
                .andExpect(jsonPath("data.paging.totalPage").exists())
                .andExpect(jsonPath("message").value(MessageVo.GET_BBS_LIST_SUCCESSFULLY))
        ;

    }

    @Test
    @DisplayName("관리자가 게시글 목록을 조회하는 경우")
    public void getBbsList_By_Admin() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member adminMember = memberService.adminJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto adminTokenDto = memberService.login(loginDto);

        createBbsList(adminMember);

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs")
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                                .param("searchType",BbsSearchType.TITLE.name())
                                .param("keyword","s1")
                                .param("criteriaDto.pageNum","1")
                                .param("criteriaDto.limit","10")
                                .param("bbsSortType",BbsSortType.REPLY_COUNT_ASC.name())
                                .param("bbsStatus",BbsStatus.DELETE.name())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.list.[0].id").exists())
                .andExpect(jsonPath("data.list.[0].writer").exists())
                .andExpect(jsonPath("data.list.[0].title").exists())
                .andExpect(jsonPath("data.list.[0].views").exists())
                .andExpect(jsonPath("data.list.[0].likeCount").exists())
                .andExpect(jsonPath("data.list.[0].replyCount").exists())
                .andExpect(jsonPath("data.list.[0].registerDate").exists())
                .andExpect(jsonPath("data.list.[0].status").exists())
                .andExpect(jsonPath("data.paging.totalCount").exists())
                .andExpect(jsonPath("data.paging.criteriaDto.pageNum").value(1))
                .andExpect(jsonPath("data.paging.criteriaDto.limit").value(10))
                .andExpect(jsonPath("data.paging.startPage").exists())
                .andExpect(jsonPath("data.paging.endPage").exists())
                .andExpect(jsonPath("data.paging.endPage").exists())
                .andExpect(jsonPath("data.paging.prev").exists())
                .andExpect(jsonPath("data.paging.next").exists())
                .andExpect(jsonPath("data.paging.totalPage").exists())
                .andDo(document("get-bbs-list-by-admin",
                        requestParameters(
                                parameterWithName("searchType").description("게시글 목록 조회 시 검색 조건 :" + br +
                                        " 게시글 제목, 게시글 내용, 게시글 제목 또는 내용, 게시글 작성자 " + br +
                                        "[TITLE, CONTENT, TITLE_OR_CONTENT, WRITER]"),
                                parameterWithName("keyword").description("게시글 목록 조회 시 검색 키워드"),
                                parameterWithName("bbsSortType").description("게시글 목록 조회 시 정렬 조건"),
                                parameterWithName("criteriaDto.pageNum").description("게시글 목록 조회 시 몇 페이지를 조회할 것인지 기입"),
                                parameterWithName("criteriaDto.limit").description("게시글 목록 조회 시 몇 개의 결과를 조회할 것인지 기입"),
                                parameterWithName("bbsStatus").description("게시글 조회 시 조회할 게시글의 상태 : 등록, 삭제 [REGISTER, DELETE]")
                        ),
                        responseFields(
                                fieldWithPath("data.list[0].id").description("게시글 목록 중 첫 번째 게시글의 식별 ID"),
                                fieldWithPath("data.list[0].writer").description("게시글 목록 중 첫 번째 게시글을 작성한 회원의 활동명"),
                                fieldWithPath("data.list[0].title").description("게시글 목록 중 첫 번째 게시글의 제목"),
                                fieldWithPath("data.list[0].views").description("게시글 목록 중 첫 번째 게시글의 조회수"),
                                fieldWithPath("data.list[0].likeCount").description("게시글 목록 중 첫 번째 게시글의 좋아요 수"),
                                fieldWithPath("data.list[0].replyCount").description("게시글 목록 중 첫 번째 게시글의 댓글 수"),
                                fieldWithPath("data.list[0].registerDate").description("게시글 목록 중 첫 번째 게시글의 등록 날짜"),
                                fieldWithPath("data.list[0].status").description("게시글의 상태 : 등록, 삭제 [REGISTER, DELETE]"),
                                fieldWithPath("data.paging.totalCount").description("요청한 검색 조건에 의해 조회되는 게시글의 총 개수"),
                                fieldWithPath("data.paging.criteriaDto.pageNum").description("요청한 페이지"),
                                fieldWithPath("data.paging.criteriaDto.limit").description("요청한 검색 결과의 개수"),
                                fieldWithPath("data.paging.startPage").description("현재 요청한 페이지 기준 첫 번째 페이지"),
                                fieldWithPath("data.paging.endPage").description("현재 요청한 페이지 기준 마지막 페이지 "),
                                fieldWithPath("data.paging.prev").description("현재 요청한 페이지 기준 이전 페이지 목록이 있는지 여부"),
                                fieldWithPath("data.paging.next").description("현재 요천항 페이지 기준 다음 페이지 목록이 있는지 여부"),
                                fieldWithPath("data.paging.totalPage").description("요청한 검색 조건에 의해 조회 되는 게시글 목록의 총 페이지 수"),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;
    }

    @Test
    @DisplayName("게시글 목록 조회 시 검색 조건이 없는데 키워드가 있는 경우")
    public void getBbsList_BadRequest() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member adminMember = memberService.adminJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto adminTokenDto = memberService.login(loginDto);

        createBbsList(adminMember);


        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs")
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
//                                .param("searchType",BbsSearchType.TITLE.name())
                                .param("keyword","1")
                                .param("criteriaDto.pageNum","1")
                                .param("criteriaDto.limit","10")
                                .param("bbsSortType",BbsSortType.REPLY_COUNT_ASC.name())
                                .param("bbsStatus",BbsStatus.REGISTER.name())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].code").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
        ;

    }

    @Test
    @DisplayName("게시글 목록 조회 시 관리자가 아닌 사용자가 삭제된 게시글 목록을 조회하는 경우")
    public void getBbsList_Unauthorized() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto userTokenDto = memberService.login(loginDto);

        createBbsList(userMember);


        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs")
                                .header(X_AUTH_TOKEN, userTokenDto.getAccessToken())
                                .param("searchType",BbsSearchType.TITLE.name())
                                .param("keyword","1")
                                .param("criteriaDto.pageNum","1")
                                .param("criteriaDto.limit","10")
                                .param("bbsSortType",BbsSortType.REPLY_COUNT_ASC.name())
                                .param("bbsStatus",BbsStatus.DELETE.name())
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
    @DisplayName("인증되지 않은 사용자가 게시글 한 건을 조회하는 경우")
    public void getBbs_UnAuthentication() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

//        Bbs bbsSample = getBbsSample(userMember, BbsStatus.REGISTER);

        Bbs bbs = createBbsByService(userMember);

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs/" + bbs.getId())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.writer").exists())
                .andExpect(jsonPath("data.title").exists())
                .andExpect(jsonPath("data.content").exists())
                .andExpect(jsonPath("data.views").exists())
                .andExpect(jsonPath("data.likeCount").exists())
                .andExpect(jsonPath("data.replyCount").exists())
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("data.updateDate").exists())
                .andExpect(jsonPath("data.like").exists())
                .andExpect(jsonPath("data.viewLike").value(true))
                .andExpect(jsonPath("data.permissionToDeleteAndModify").value(false))
                .andExpect(jsonPath("data.uploadFiles[0].fileId").exists())
                .andExpect(jsonPath("data.uploadFiles[0].fileName").exists())
                .andExpect(jsonPath("data.uploadFiles[0].fileDownloadUri").exists())
                .andExpect(jsonPath("data.uploadFiles[0].fileType").exists())
                .andExpect(jsonPath("data.uploadFiles[0].size").exists())
                .andExpect(jsonPath("message").value(MessageVo.GET_BBS_SUCCESSFULLY))
        ;

    }

    private Bbs createBbsByService(Member userMember) throws IOException {
        List<OnlyFileIdDto> onlyFileIdDtos = getOnlyFileIdDtos();
        BbsCreateDto bbsCreateDto = BbsCreateDto.builder()
                .title("test bbs")
                .content("test content")
                .fileIdList(onlyFileIdDtos)
                .build();

        return bbsService.addBbs(userMember.getId(), bbsCreateDto);
    }

    @Test
    @DisplayName("USER 권한의 사용자가 게시글 한 건을 조회하는 경우")
    public void getBbs_By_User() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

//        Bbs bbsSample = getBbsSample(userMember, BbsStatus.REGISTER);
        Bbs bbs = createBbsByService(userMember);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto userTokenDto = memberService.login(loginDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs/" + bbs.getId())
                                .header(X_AUTH_TOKEN, userTokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.writer").exists())
                .andExpect(jsonPath("data.title").exists())
                .andExpect(jsonPath("data.content").exists())
                .andExpect(jsonPath("data.views").exists())
                .andExpect(jsonPath("data.likeCount").exists())
                .andExpect(jsonPath("data.replyCount").exists())
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("data.updateDate").exists())
                .andExpect(jsonPath("data.like").exists())
                .andExpect(jsonPath("data.viewLike").value(false))
                .andExpect(jsonPath("data.permissionToDeleteAndModify").value(true))
                .andExpect(jsonPath("data.uploadFiles[0].fileId").exists())
                .andExpect(jsonPath("data.uploadFiles[0].fileName").exists())
                .andExpect(jsonPath("data.uploadFiles[0].fileDownloadUri").exists())
                .andExpect(jsonPath("data.uploadFiles[0].fileType").exists())
                .andExpect(jsonPath("data.uploadFiles[0].size").exists())
                .andExpect(jsonPath("message").value(MessageVo.GET_BBS_SUCCESSFULLY))
//                .andExpect(jsonPath("_links.update-bbs.href").exists())
        ;

    }

    @Test
    @DisplayName("USER 권한의 사용자가 다른 회원의 게시글을 조회한 경우 - 해당 회원이 해당 게시글에 좋아요를 누른 경우")
    public void getBbs_Of_DifferentMember() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

//        Bbs bbsSample = getBbsSample(userMember, BbsStatus.REGISTER);
        Bbs bbs = createBbsByService(userMember);

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("differentMember", "differentMember");
        Member user2 = memberService.userJoin(memberCreateDto1);

        LoginDto loginDto = getLoginDto(memberCreateDto1);
        TokenDto user2TokenDto = memberService.login(loginDto);

        bbsLikeService.like(user2.getId(), bbs.getId());

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs/" + bbs.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.writer").exists())
                .andExpect(jsonPath("data.title").exists())
                .andExpect(jsonPath("data.content").exists())
                .andExpect(jsonPath("data.views").exists())
                .andExpect(jsonPath("data.likeCount").exists())
                .andExpect(jsonPath("data.replyCount").exists())
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("data.updateDate").exists())
                .andExpect(jsonPath("data.like").value(true))
                .andExpect(jsonPath("data.viewLike").value(true))
                .andExpect(jsonPath("data.permissionToDeleteAndModify").value(false))
                .andExpect(jsonPath("data.uploadFiles[0].fileId").exists())
                .andExpect(jsonPath("data.uploadFiles[0].fileName").exists())
                .andExpect(jsonPath("data.uploadFiles[0].fileDownloadUri").exists())
                .andExpect(jsonPath("data.uploadFiles[0].fileType").exists())
                .andExpect(jsonPath("data.uploadFiles[0].size").exists())
                .andExpect(jsonPath("message").value(MessageVo.GET_BBS_SUCCESSFULLY))
                .andDo(document("get-bbs",
                        requestHeaders(
                                headerWithName(X_AUTH_TOKEN).description(X_AUTH_TOKEN_DESCRIPTION)
                        ),
                        responseFields(
                                fieldWithPath("data.id").description("조회된 게시글의 식별 ID"),
                                fieldWithPath("data.writer").description("조회된 게시글을 작성한 회원의 활동명"),
                                fieldWithPath("data.title").description("조회된 게시글의 제목"),
                                fieldWithPath("data.content").description("조회된 게시글의 내용"),
                                fieldWithPath("data.views").description("조회된 게시글의 조회 수"),
                                fieldWithPath("data.likeCount").description("조회된 게시글의 좋아요 수"),
                                fieldWithPath("data.replyCount").description("조회된 게시글의 댓글 수"),
                                fieldWithPath("data.registerDate").description("조회된 게시글의 작성 날짜"),
                                fieldWithPath("data.updateDate").description("조회된 게시글의 수정 날짜"),
                                fieldWithPath("data.like").description("게시글을 조회한 회원이 해당 게시글에 좋아요를 등록했는지 여부"),
                                fieldWithPath("data.viewLike").description("조회된 게시글의 좋아요 등록, 확인 등에 대한 권한 여부"),
                                fieldWithPath("data.permissionToDeleteAndModify").description("조회된 게시글에 대한 수정 및 삭제 권한 여부"),
                                fieldWithPath("data.uploadFiles[0].fileId").description("조회된 게시글에 등록된 업로드 파일 목록 중 첫 번째 파일의 식별 ID"),
                                fieldWithPath("data.uploadFiles[0].fileName").description("조회된 게시글에 등록된 업로드 파일 목록 중 첫 번째 파일의 이름"),
                                fieldWithPath("data.uploadFiles[0].fileDownloadUri").description("조회된 게시글에 등록된 업로드 파일 목록 중 첫 번째 파일의 다운로드 URI"),
                                fieldWithPath("data.uploadFiles[0].fileType").description("조회된 게시글에 등록된 업로드 파일 목록 중 첫 번째 파일의 타입"),
                                fieldWithPath("data.uploadFiles[0].size").description("조회된 게시글에 등록된 업로드 파일 목록 중 첫 번째 파일의 크기"),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
//                .andExpect(jsonPath("_links.update-bbs.href").doesNotExist())
        ;

    }

    @Test
    @DisplayName("관리자가 게시글 한 건을 조회하는 경우")
    public void getBbs_By_Admin() throws Exception {
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member adminMember = memberService.adminJoin(memberCreateDto);

        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto2);
//        Bbs bbsSample = getBbsSample(adminMember, BbsStatus.DELETE);
        Bbs bbs = createBbsByService(user1);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto adminTokenDto = memberService.login(loginDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs/" + bbs.getId())
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.writer").exists())
                .andExpect(jsonPath("data.title").exists())
                .andExpect(jsonPath("data.content").exists())
                .andExpect(jsonPath("data.views").exists())
                .andExpect(jsonPath("data.likeCount").exists())
                .andExpect(jsonPath("data.replyCount").exists())
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("data.updateDate").exists())
                .andExpect(jsonPath("data.status").exists())
                .andExpect(jsonPath("data.uploadFiles[0].fileId").exists())
                .andExpect(jsonPath("data.uploadFiles[0].fileName").exists())
                .andExpect(jsonPath("data.uploadFiles[0].fileDownloadUri").exists())
                .andExpect(jsonPath("data.uploadFiles[0].fileType").exists())
                .andExpect(jsonPath("data.uploadFiles[0].size").exists())
                .andExpect(jsonPath("data.permissionToDeleteAndModify").value(true))
                .andDo(document("get-bbs-by-admin",
                        requestHeaders(
                                headerWithName(X_AUTH_TOKEN).description(X_AUTH_TOKEN_DESCRIPTION)
                        ),
                        responseFields(
                                fieldWithPath("data.id").description("조회된 게시글의 식별 ID"),
                                fieldWithPath("data.writer").description("조회된 게시글을 작성한 회원의 활동명"),
                                fieldWithPath("data.title").description("조회된 게시글의 제목"),
                                fieldWithPath("data.content").description("조회된 게시글의 내용"),
                                fieldWithPath("data.views").description("조회된 게시글의 조회 수"),
                                fieldWithPath("data.likeCount").description("조회된 게시글의 좋아요 수"),
                                fieldWithPath("data.replyCount").description("조회된 게시글의 댓글 수"),
                                fieldWithPath("data.registerDate").description("조회된 게시글의 작성 날짜"),
                                fieldWithPath("data.updateDate").description("조회된 게시글의 수정 날짜"),
                                fieldWithPath("data.status").description("조회된 게시글의 상태 : 등록됨, 삭제됨 [REGISTER, DELETE]"),
                                fieldWithPath("data.permissionToDeleteAndModify").description("조회된 게시글에 대한 해당 회원의 수정 및 삭제 권한"),
                                fieldWithPath("data.uploadFiles[0].fileId").description("조회된 게시글에 등록된 업로드 파일 목록 중 첫 번째 파일의 식별 ID"),
                                fieldWithPath("data.uploadFiles[0].fileName").description("조회된 게시글에 등록된 업로드 파일 목록 중 첫 번째 파일의 이름"),
                                fieldWithPath("data.uploadFiles[0].fileDownloadUri").description("조회된 게시글에 등록된 업로드 파일 목록 중 첫 번째 파일의 다운로드 URI"),
                                fieldWithPath("data.uploadFiles[0].fileType").description("조회된 게시글에 등록된 업로드 파일 목록 중 첫 번째 파일의 타입"),
                                fieldWithPath("data.uploadFiles[0].size").description("조회된 게시글에 등록된 업로드 파일 목록 중 첫 번째 파일의 크기"),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;

    }

    @Test
    @DisplayName("조회할 게시글이 없는 경우")
    public void getBbs_NotFound() throws Exception {
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(userMember, BbsStatus.REGISTER);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto userTokenDto = memberService.login(loginDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs/" + 100000000L)
                                .header(X_AUTH_TOKEN, userTokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new BbsNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("관리자가 아닌 사용자가 삭제된 게시글을 조회하는 경우")
    public void getBbs_Unauthorized() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(userMember, BbsStatus.DELETE);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto userTokenDto = memberService.login(loginDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs/" + bbsSample.getId())
                                .header(X_AUTH_TOKEN, userTokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(MessageVo.UNAUTHORIZED_USERS_VIEW_DELETED_POSTS))
        ;

    }

    @Test
    @DisplayName("게시글 수정")
    public void modifyBbs() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto userTokenDto = memberService.login(loginDto);

//        Bbs bbsSample = getBbsSample(userMember, BbsStatus.REGISTER);
        Bbs bbs = createBbsByService(userMember);

        String update_title = "update bbs";
        String update_content = "update content";
        List<OnlyFileIdDto> onlyFileIdDtos = getOnlyFileIdDtos();

        BbsUpdateDto bbsUpdateDto = BbsUpdateDto.builder()
                .title(update_title)
                .content(update_content)
                .fileIdList(onlyFileIdDtos)
                .build();

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/bbs/" + bbs.getId())
                                .header(X_AUTH_TOKEN, userTokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bbsUpdateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.writer").exists())
                .andExpect(jsonPath("data.title").exists())
                .andExpect(jsonPath("data.content").exists())
                .andExpect(jsonPath("data.views").exists())
                .andExpect(jsonPath("data.likeCount").exists())
                .andExpect(jsonPath("data.replyCount").exists())
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("data.updateDate").exists())
                .andExpect(jsonPath("data.like").exists())
                .andExpect(jsonPath("data.viewLike").value(false))
                .andExpect(jsonPath("data.permissionToDeleteAndModify").value(true))
                .andExpect(jsonPath("data.uploadFiles").exists())
                .andExpect(jsonPath("message").value(MessageVo.MODIFIED_BBS_SUCCESSFULLY))
                .andDo(document("modify-bbs",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type"),
                                headerWithName(X_AUTH_TOKEN).description(X_AUTH_TOKEN_DESCRIPTION)
                        ),
                        requestFields(
                                fieldWithPath("title").description("수정할 제목"),
                                fieldWithPath("content").description("수정할 내용"),
                                fieldWithPath("fileIdList[0].fileId").description("게시글 수정 시 등록할 파일 목록의 식별 ID 중 첫 번째 파일 ID" + br +
                                        "기존에 등록된 게시글 목록은 삭제하고 다시 등록하는 방식을 사용합니다.")
                        ),
                        responseFields(
                                fieldWithPath("data.id").description("수정된 게시글의 식별 ID"),
                                fieldWithPath("data.writer").description("수정된 게시글을 작성한 회원의 활동명"),
                                fieldWithPath("data.title").description("수정된 게시글 제목"),
                                fieldWithPath("data.content").description("수정된 게시글 내용"),
                                fieldWithPath("data.views").description("수정된 게시글의 조회 수"),
                                fieldWithPath("data.likeCount").description("수정된 게시글의 좋아요 수"),
                                fieldWithPath("data.replyCount").description("수정된 게시글의 댓글 수"),
                                fieldWithPath("data.registerDate").description("수정된 게시글의 작성 날짜"),
                                fieldWithPath("data.updateDate").description("수정된 게시글의 수정 날짜"),
                                fieldWithPath("data.like").description("자신이 작성한 게시글이므로 좋아요에 대한 부분은 참고하지 않는다."),
                                fieldWithPath("data.viewLike").description("수정된 게시글에 대한 해당 회원의 좋아요 등록, 해제, 확인 권한"),
                                fieldWithPath("data.permissionToDeleteAndModify").description("수정된 게시글에 대한 해당 회원의 수정 및 삭제 권한"),
                                fieldWithPath("data.uploadFiles[0].fileId").description("수정된 게시글에 등록된 파일 목록 중 첫 번째 파일의 식별 ID"),
                                fieldWithPath("data.uploadFiles[0].fileName").description("수정된 게시글에 등록된 파일 목록 중 첫 번째 파일의 이름"),
                                fieldWithPath("data.uploadFiles[0].fileDownloadUri").description("수정된 게시글에 등록된 파일 목록 중 첫 번째 파일의 다운로드 URI"),
                                fieldWithPath("data.uploadFiles[0].fileType").description("수정된 게시글에 등록된 파일 목록 중 첫 번째 파일의 타입"),
                                fieldWithPath("data.uploadFiles[0].size").description("수정된 게시글에 등록된 파일 목록 중 첫 번째 파일의 크기"),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )

                ))
        ;

        Bbs findBbs = bbsService.getBbs(bbs.getId());
        assertEquals(findBbs.getTitle(), update_title);
        assertEquals(findBbs.getContent(), update_content);


    }

    @Test
    @DisplayName("인증되지 않은 사용자가 게시글을 수정하는 경우")
    public void modifyBbs_Unauthorized() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto userTokenDto = memberService.login(loginDto);

        Bbs bbsSample = getBbsSample(userMember, BbsStatus.REGISTER);

        String update_title = "update bbs";
        String update_content = "update content";
        BbsUpdateDto bbsUpdateDto = BbsUpdateDto.builder()
                .title(update_title)
                .content(update_content)
                .build();

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/bbs/" + bbsSample.getId())
//                                .header(X_AUTH_TOKEN, userTokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bbsUpdateDto))
                )
                .andDo(print());


        //then
        perform
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("게시글 수정 시 수정할 값이 없는 경우")
    public void modifyBbs_Empty() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto userTokenDto = memberService.login(loginDto);

        Bbs bbsSample = getBbsSample(userMember, BbsStatus.REGISTER);

        BbsUpdateDto bbsUpdateDto = BbsUpdateDto.builder()
//                .title("update bbs")
//                .content("update content")
                .build();

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/bbs/" + bbsSample.getId())
                                .header(X_AUTH_TOKEN, userTokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bbsUpdateDto))
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
    @DisplayName("다른 사람이 쓴 게시글을 수정하는 경우")
    public void modifyBbs_DifferentMember() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("differentMember", "differentMember");
        Member userMember2 = memberService.userJoin(memberCreateDto1);

        LoginDto loginDto = getLoginDto(memberCreateDto1);
        TokenDto user2TokenDto = memberService.login(loginDto);

        Bbs bbsSample = getBbsSample(userMember, BbsStatus.REGISTER);

        BbsUpdateDto bbsUpdateDto = BbsUpdateDto.builder()
                .title("update bbs")
                .content("update content")
                .build();

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/bbs/" + bbsSample.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bbsUpdateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.MODIFY_BBS_OF_DIFFERENT_MEMBER));

    }

    @Test
    @DisplayName("수정할 게시글이 없는 경우")
    public void modifyBbs_NotFound() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto userTokenDto = memberService.login(loginDto);

        Bbs bbsSample = getBbsSample(userMember, BbsStatus.REGISTER);

        BbsUpdateDto bbsUpdateDto = BbsUpdateDto.builder()
                .title("update bbs")
                .content("update content")
                .build();

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/bbs/" + 10000000L)
                                .header(X_AUTH_TOKEN, userTokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bbsUpdateDto))
                )
                .andDo(print());
        //then

        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new BbsNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("삭제된 게시글을 수정하는 경우")
    public void modify_DeletedBbs() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto userTokenDto = memberService.login(loginDto);

        Bbs bbsSample = getBbsSample(userMember, BbsStatus.DELETE);

        BbsUpdateDto bbsUpdateDto = BbsUpdateDto.builder()
                .title("update bbs")
                .content("update content")
                .build();

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/bbs/" + bbsSample.getId())
                                .header(X_AUTH_TOKEN, userTokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bbsUpdateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("삭제된 게시글 입니다. : 삭제된 게시글은 수정이 불가능합니다."));

    }

    @Test
    @DisplayName("게시글 삭제")
    public void deleteBbs() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(userMember, BbsStatus.REGISTER);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto userTokenDto = memberService.login(loginDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        delete("/api/bbs/" + bbsSample.getId())
                                .header(X_AUTH_TOKEN, userTokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value(MessageVo.DELETE_BBS_SUCCESSFULLY))
                .andDo(document("delete-bbs",
                        requestHeaders(
                                headerWithName(X_AUTH_TOKEN).description(X_AUTH_TOKEN_DESCRIPTION)
                        ),
                        responseFields(
                                fieldWithPath("data").description("게시글 삭제의 경우 별도의 data 를 출력하지 않습니다."),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;

    }

    @Test
    @DisplayName("관리자가 회원의 게시글을 삭제")
    public void deleteBbs_By_Admin() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(userMember, BbsStatus.REGISTER);

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("adminUser", "adminUser");
        Member adminMember = memberService.adminJoin(memberCreateDto1);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(adminMember.getLoginInfo().getRefreshToken());
        TokenDto adminTokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        delete("/api/bbs/" + bbsSample.getId())
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                )
                .andDo(print());


        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value(MessageVo.DELETE_BBS_SUCCESSFULLY))
                .andDo(document("delete-bbs-by-admin",
                        requestHeaders(
                                headerWithName(X_AUTH_TOKEN).description(X_AUTH_TOKEN_DESCRIPTION)
                        ),
                        responseFields(
                                fieldWithPath("data").description("게시글 삭제의 경우 별도의 data 를 출력하지 않습니다."),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 게시글을 삭제하는 경우")
    public void deleteBbs_Unauthorized() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(userMember, BbsStatus.REGISTER);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto userTokenDto = memberService.login(loginDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        delete("/api/bbs/" + bbsSample.getId())
//                                .header(X_AUTH_TOKEN, userTokenDto.getAccessToken())
                )
                .andDo(print());


        //then
        perform
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("다른 회원의 게시글을 삭제하는 경우")
    public void deleteBbs_Of_DifferentMember() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(userMember, BbsStatus.REGISTER);

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("differentMember", "differentMember");
        Member user2 = memberService.userJoin(memberCreateDto1);

        LoginDto loginDto = getLoginDto(memberCreateDto1);
        TokenDto user2TokenDto = memberService.login(loginDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        delete("/api/bbs/" + bbsSample.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                )
                .andDo(print());


        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.DELETE_BBS_OF_DIFFERENT_MEMBER))
        ;

    }

    @Test
    @DisplayName("삭제할 게시글을 찾을 수 없는 경우")
    public void deleteBbs_NotFound() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(userMember, BbsStatus.REGISTER);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto userTokenDto = memberService.login(loginDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        delete("/api/bbs/" + 1000000L)
                                .header(X_AUTH_TOKEN, userTokenDto.getAccessToken())
                )
                .andDo(print());


        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new BbsNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("이미 삭제된 게시글을 삭제하는 경우")
    public void delete_DeletedBbs() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(userMember, BbsStatus.DELETE);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto userTokenDto = memberService.login(loginDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        delete("/api/bbs/" + bbsSample.getId())
                                .header(X_AUTH_TOKEN, userTokenDto.getAccessToken())
                )
                .andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").exists());

    }

    @Test
    @DisplayName("게시글에 좋아요 등록")
    public void addLikeToBbs() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member user1 = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("differentMember", "differentMember");
        Member user2 = memberService.userJoin(memberCreateDto1);

        LoginDto loginDto = getLoginDto(memberCreateDto1);
        TokenDto user2TokenDto = memberService.login(loginDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/bbs/like/" + bbsSample.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value(MessageVo.ADD_LIKE_TO_BBS_SUCCESSFULLY))
                .andDo(document("add-like-to-bbs",
                        requestHeaders(
                                headerWithName(X_AUTH_TOKEN).description(X_AUTH_TOKEN_DESCRIPTION)
                        ),
                        responseFields(
                                fieldWithPath("data").description("게시글에 좋아요 등록의 경우 별도의 data 를 출력하지 않습니다."),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;

        boolean existLike = bbsLikeService.getExistLike(user2.getId(), bbsSample.getId());
        assertTrue(existLike);
    }

    @Test
    @DisplayName("게시글에 좋아요 등록 시 이미 좋아요가 등록된 게시글일 경우")
    public void addLikeToBbs_ExistLike() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member user1 = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("differentMember", "differentMember");
        Member user2 = memberService.userJoin(memberCreateDto1);

        LoginDto loginDto = getLoginDto(memberCreateDto1);
        TokenDto user2TokenDto = memberService.login(loginDto);

        bbsLikeService.like(user2.getId(), bbsSample.getId());

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/bbs/like/" + bbsSample.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new ExistLikeException().getMessage()));

    }

    @Test
    @DisplayName("좋아요를 등록할 게시글을 찾을 수 없는 경우")
    public void addLikeToBbs_NotFound() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member user1 = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("differentMember", "differentMember");
        Member user2 = memberService.userJoin(memberCreateDto1);

        LoginDto loginDto = getLoginDto(memberCreateDto1);
        TokenDto user2TokenDto = memberService.login(loginDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/bbs/like/" + 1000000L)
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new BbsNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("자기 자신의 게시글에 좋아요를 누르는 경우")
    public void addLikeToBbs_SelfLike() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member user1 = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto user1TokenDto = memberService.login(loginDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/bbs/like/" + bbsSample.getId())
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new SelfLikeException().getMessage()));

    }

    @Test
    @DisplayName("삭제된 게시글에 좋아요를 누른 경우")
    public void addLikeToBbs_DeletedBbs() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member user1 = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

//        memberCreateDto.setJoinId("differentMember");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("differentMember", "differentMember");
        Member user2 = memberService.userJoin(memberCreateDto2);

        LoginDto loginDto = getLoginDto(memberCreateDto2);
        TokenDto user2TokenDto = memberService.login(loginDto);

        bbsService.deleteBbs(bbsSample.getId());

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/bbs/like/" + bbsSample.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("삭제된 게시글 입니다. : 삭제된 게시글에는 좋아요를 누를 수 없습니다."));

    }

    @Test
    @DisplayName("게시글 좋아요 해제")
    public void unLikeBbs() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member user1 = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        Member user2 = memberService.userJoin(memberCreateDto1);

        bbsLikeService.like(user2.getId(), bbsSample.getId());

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        delete("/api/bbs/like/" + bbsSample.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value(MessageVo.UNLIKE_BBS_SUCCESSFULLY))
                .andDo(document("unlike-bbs",
                        requestHeaders(
                                headerWithName(X_AUTH_TOKEN).description(X_AUTH_TOKEN_DESCRIPTION)
                        ),
                        responseFields(
                                fieldWithPath("data").description("게시글 좋아요 해제의 경우 별도의 data 를 출력하지 않습니다."),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;

    }

    @Test
    @DisplayName("게시글에 좋아요를 등록하지 않았는데 좋아요를 해제하는 경우")
    public void unLike_NoExistLike() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member user1 = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        Member user2 = memberService.userJoin(memberCreateDto1);

//        bbsLikeService.like(user2.getId(), bbsSample.getId());
        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        delete("/api/bbs/like/" + bbsSample.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new NoExistLikeException().getMessage()));

    }

    private MemberCreateDto getMemberCreateDto() {
        return getMemberCreateDto("testJoinid", "test");
    }

    private List<Bbs> createBbsList(Member member) {
        List<Bbs> bbsList = new ArrayList<>();

        for (int i = 0; i < 500; i++) {
            BbsStatus status;
            if (i % 2 == 1) {
                status = BbsStatus.REGISTER;
            } else {
                status = BbsStatus.DELETE;
            }

            Bbs bbs = getBbsSample(member, status);
            bbsList.add(bbs);
        }

        return bbsList;
    }

    private Bbs getBbsSample(Member member, BbsStatus status) {
        Bbs bbs = Bbs.builder()
                .member(member)
                .title("test bbs" + new Random().nextInt(1000))
                .content("test bbs content" + new Random().nextInt(1000))
                .views(new Random().nextInt(1000))
                .likeCount(new Random().nextInt(1000))
                .replyCount(new Random().nextInt(1000))
                .registerDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .status(status)
                .build();

        bbsRepository.save(bbs);
        return bbs;
    }
}