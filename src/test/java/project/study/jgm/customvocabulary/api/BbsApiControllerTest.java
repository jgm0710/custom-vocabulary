package project.study.jgm.customvocabulary.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import project.study.jgm.customvocabulary.bbs.Bbs;
import project.study.jgm.customvocabulary.bbs.BbsStatus;
import project.study.jgm.customvocabulary.bbs.dto.*;
import project.study.jgm.customvocabulary.bbs.exception.BbsNotFoundException;
import project.study.jgm.customvocabulary.bbs.exception.DeletedBbsException;
import project.study.jgm.customvocabulary.common.BaseControllerTest;
import project.study.jgm.customvocabulary.common.dto.CriteriaDto;
import project.study.jgm.customvocabulary.common.dto.MessageDto;
import project.study.jgm.customvocabulary.common.exception.ExistLikeException;
import project.study.jgm.customvocabulary.common.exception.NoExistLikeException;
import project.study.jgm.customvocabulary.common.exception.SelfLikeException;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.dto.MemberCreateDto;
import project.study.jgm.customvocabulary.security.dto.LoginDto;
import project.study.jgm.customvocabulary.security.dto.OnlyTokenDto;
import project.study.jgm.customvocabulary.security.dto.TokenDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BbsApiControllerTest extends BaseControllerTest {

    @BeforeEach
    public void setUp() {
        bbsLikeRepository.deleteAll();
        bbsRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("게시글 추가")
    public void addBbs() throws Exception {
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
                                .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bbsCreateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("message").value(MessageDto.BBS_REGISTERED_SUCCESSFULLY))
        ;

    }

    @Test
    @DisplayName("게시글 추가 시 제목이나 내용이 비어 있는 경우")
    public void addBbs_TitleOrContent_Null() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
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
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.[0].id").exists())
                .andExpect(jsonPath("data.[0].writer").exists())
                .andExpect(jsonPath("data.[0].title").exists())
                .andExpect(jsonPath("data.[0].views").exists())
                .andExpect(jsonPath("data.[0].likeCount").exists())
                .andExpect(jsonPath("data.[0].replyCount").exists())
                .andExpect(jsonPath("data.[0].registerDate").exists())
                .andExpect(jsonPath("paging.totalCount").exists())
                .andExpect(jsonPath("paging.criteriaDto.pageNum").value(1))
                .andExpect(jsonPath("paging.criteriaDto.limit").value(15))
                .andExpect(jsonPath("paging.startPage").exists())
                .andExpect(jsonPath("paging.endPage").exists())
                .andExpect(jsonPath("paging.endPage").exists())
                .andExpect(jsonPath("paging.prev").exists())
                .andExpect(jsonPath("paging.next").exists())
                .andExpect(jsonPath("paging.totalPage").exists())
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
                .andExpect(jsonPath("data.[0].id").exists())
                .andExpect(jsonPath("data.[0].writer").exists())
                .andExpect(jsonPath("data.[0].title").exists())
                .andExpect(jsonPath("data.[0].views").exists())
                .andExpect(jsonPath("data.[0].likeCount").exists())
                .andExpect(jsonPath("data.[0].replyCount").exists())
                .andExpect(jsonPath("data.[0].registerDate").exists())
                .andExpect(jsonPath("paging.totalCount").exists())
                .andExpect(jsonPath("paging.criteriaDto.pageNum").value(1))
                .andExpect(jsonPath("paging.criteriaDto.limit").value(15))
                .andExpect(jsonPath("paging.startPage").exists())
                .andExpect(jsonPath("paging.endPage").exists())
                .andExpect(jsonPath("paging.endPage").exists())
                .andExpect(jsonPath("paging.prev").exists())
                .andExpect(jsonPath("paging.next").exists())
                .andExpect(jsonPath("paging.totalPage").exists())
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
                .andExpect(jsonPath("data.[0].id").exists())
                .andExpect(jsonPath("data.[0].writer").exists())
                .andExpect(jsonPath("data.[0].title").exists())
                .andExpect(jsonPath("data.[0].views").exists())
                .andExpect(jsonPath("data.[0].likeCount").exists())
                .andExpect(jsonPath("data.[0].replyCount").exists())
                .andExpect(jsonPath("data.[0].registerDate").exists())
                .andExpect(jsonPath("data.[0].status").exists())
                .andExpect(jsonPath("paging.totalCount").exists())
                .andExpect(jsonPath("paging.criteriaDto.pageNum").value(1))
                .andExpect(jsonPath("paging.criteriaDto.limit").value(10))
                .andExpect(jsonPath("paging.startPage").exists())
                .andExpect(jsonPath("paging.endPage").exists())
                .andExpect(jsonPath("paging.endPage").exists())
                .andExpect(jsonPath("paging.prev").exists())
                .andExpect(jsonPath("paging.next").exists())
                .andExpect(jsonPath("paging.totalPage").exists())
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

        Bbs bbsSample = getBbsSample(userMember, BbsStatus.REGISTER);
        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs/" + bbsSample.getId())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("writer").exists())
                .andExpect(jsonPath("title").exists())
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("views").exists())
                .andExpect(jsonPath("likeCount").exists())
                .andExpect(jsonPath("replyCount").exists())
                .andExpect(jsonPath("registerDate").exists())
                .andExpect(jsonPath("updateDate").exists())
        ;

    }

    @Test
    @DisplayName("USER 권한의 사용자가 게시글 한 건을 조회하는 경우")
    public void getBbs_By_User() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(userMember, BbsStatus.REGISTER);

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
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("writer").exists())
                .andExpect(jsonPath("title").exists())
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("views").exists())
                .andExpect(jsonPath("likeCount").exists())
                .andExpect(jsonPath("replyCount").exists())
                .andExpect(jsonPath("registerDate").exists())
                .andExpect(jsonPath("updateDate").exists())
                .andExpect(jsonPath("viewLike").value(false))
                .andExpect(jsonPath("_links.update-bbs.href").exists())
        ;

    }

    @Test
    @DisplayName("USER 권한의 사용자가 다른 회원의 게시글을 조회한 경우 - 해당 회원이 해당 게시글에 좋아요를 누른 경우")
    public void getBbs_Of_DifferentMember() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(userMember, BbsStatus.REGISTER);

        memberCreateDto.setJoinId("fdafdas");
        Member userMember2 = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto user2TokenDto = memberService.login(loginDto);

        bbsLikeService.like(userMember2.getId(), bbsSample.getId());

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs/" + bbsSample.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("writer").exists())
                .andExpect(jsonPath("title").exists())
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("views").exists())
                .andExpect(jsonPath("likeCount").exists())
                .andExpect(jsonPath("replyCount").exists())
                .andExpect(jsonPath("registerDate").exists())
                .andExpect(jsonPath("updateDate").exists())
                .andExpect(jsonPath("like").value(true))
                .andExpect(jsonPath("viewLike").value(true))
                .andExpect(jsonPath("_links.update-bbs.href").doesNotExist())
        ;

    }

    @Test
    @DisplayName("관리자가 게시글 한 건을 조회하는 경우")
    public void getBbs_By_Admin() throws Exception {
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member adminMember = memberService.adminJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(adminMember, BbsStatus.DELETE);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto adminTokenDto = memberService.login(loginDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs/" + bbsSample.getId())
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("writer").exists())
                .andExpect(jsonPath("title").exists())
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("views").exists())
                .andExpect(jsonPath("likeCount").exists())
                .andExpect(jsonPath("replyCount").exists())
                .andExpect(jsonPath("registerDate").exists())
                .andExpect(jsonPath("updateDate").exists())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("_links.update-bbs.href").exists())
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
                .andExpect(jsonPath("message").value(MessageDto.UNAUTHORIZED_USERS_VIEW_DELETED_POSTS))
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
                                .header(X_AUTH_TOKEN, userTokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bbsUpdateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value(MessageDto.MODIFIED_BBS_SUCCESSFULLY))
                .andExpect(jsonPath("_links.get-bbs.href").exists())
        ;

        Bbs findBbs = bbsService.getBbs(bbsSample.getId());
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

        memberCreateDto.setJoinId("differentMember");
        Member userMember2 = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
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
                .andExpect(jsonPath("message").value(MessageDto.MODIFY_BBS_OF_DIFFERENT_MEMBER));

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
                .andExpect(jsonPath("message").value(MessageDto.DELETE_BBS_SUCCESSFULLY))
        ;

    }

    @Test
    @DisplayName("관리자가 회원의 게시글을 삭제")
    public void deleteBbs_By_Admin() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(userMember, BbsStatus.REGISTER);

        memberCreateDto.setJoinId("adminjoinid");
        Member adminMember = memberService.adminJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto adminTokenDto = memberService.login(loginDto);

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
                .andExpect(jsonPath("message").value(MessageDto.DELETE_BBS_SUCCESSFULLY));

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

        memberCreateDto.setJoinId("differentJoinId");
        Member user2 = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
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
                .andExpect(jsonPath("message").value(MessageDto.DELETE_BBS_OF_DIFFERENT_MEMBER))
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

        memberCreateDto.setJoinId("differentMember");
        Member user2 = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto user2TokenDto = memberService.login(loginDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs/like/" + bbsSample.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value(MessageDto.ADD_LIKE_TO_BBS_SUCCESSFULLY))
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

        memberCreateDto.setJoinId("differentMember");
        Member user2 = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto user2TokenDto = memberService.login(loginDto);

        bbsLikeService.like(user2.getId(), bbsSample.getId());

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs/like/" + bbsSample.getId())
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

        memberCreateDto.setJoinId("differentMember");
        Member user2 = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto user2TokenDto = memberService.login(loginDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs/like/" + 1000000L)
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
                        get("/api/bbs/like/" + bbsSample.getId())
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

        memberCreateDto.setJoinId("differentMember");
        Member user2 = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto user2TokenDto = memberService.login(loginDto);

        bbsService.deleteBbs(bbsSample.getId());

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs/like/" + bbsSample.getId())
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

        memberCreateDto.setJoinId("user2");
        Member user2 = memberService.userJoin(memberCreateDto);

        bbsLikeService.like(user2.getId(), bbsSample.getId());

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs/unlike/" + bbsSample.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value(MessageDto.UNLIKE_BBS_SUCCESSFULLY));

    }

    @Test
    @DisplayName("게시글에 좋아요를 등록하지 않았는데 좋아요를 해제하는 경우")
    public void unLike_NoExistLike() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member user1 = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        memberCreateDto.setJoinId("user2");
        Member user2 = memberService.userJoin(memberCreateDto);

//        bbsLikeService.like(user2.getId(), bbsSample.getId());
        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs/unlike/" + bbsSample.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new NoExistLikeException().getMessage()));

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