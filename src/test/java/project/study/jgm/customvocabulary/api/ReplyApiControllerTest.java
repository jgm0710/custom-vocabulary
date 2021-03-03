package project.study.jgm.customvocabulary.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import project.study.jgm.customvocabulary.bbs.Bbs;
import project.study.jgm.customvocabulary.bbs.BbsStatus;
import project.study.jgm.customvocabulary.bbs.exception.BbsNotFoundException;
import project.study.jgm.customvocabulary.bbs.reply.Reply;
import project.study.jgm.customvocabulary.bbs.reply.ReplySortType;
import project.study.jgm.customvocabulary.bbs.reply.dto.ReplyCreateDto;
import project.study.jgm.customvocabulary.bbs.reply.dto.ReplyUpdateDto;
import project.study.jgm.customvocabulary.bbs.reply.exception.ReplyNotFoundException;
import project.study.jgm.customvocabulary.bbs.reply.like.exception.AddLikeToChildReplyException;
import project.study.jgm.customvocabulary.common.BaseControllerTest;
import project.study.jgm.customvocabulary.common.dto.CriteriaDto;
import project.study.jgm.customvocabulary.common.dto.MessageVo;
import project.study.jgm.customvocabulary.common.dto.ResponseDto;
import project.study.jgm.customvocabulary.common.exception.ExistLikeException;
import project.study.jgm.customvocabulary.common.exception.NoExistLikeException;
import project.study.jgm.customvocabulary.common.exception.SelfLikeException;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.dto.MemberCreateDto;
import project.study.jgm.customvocabulary.security.dto.OnlyTokenDto;
import project.study.jgm.customvocabulary.security.dto.TokenDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReplyApiControllerTest extends BaseControllerTest {

    @BeforeEach
    public void setUp() {
        replyLikeRepository.deleteAll();
        replyRepository.deleteAll();
        bbsRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("댓글 등록")
    @Transactional
    public void addReply() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid","test");
        Member user1 = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        memberCreateDto.setJoinId("user2");
        memberCreateDto.setNickname("user2");
        Member user2 = memberService.userJoin(memberCreateDto);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        String test_content = "test content";
        ReplyCreateDto replyCreateDto = new ReplyCreateDto(test_content);
        //when

        ResultActions perform = mockMvc
                .perform(
                        post("/api/bbs/reply/" + bbsSample.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(replyCreateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.writer").exists())
                .andExpect(jsonPath("data.content").exists())
                .andExpect(jsonPath("data.likeCount").exists())
                .andExpect(jsonPath("data.childrenCount").exists())
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("data.viewLike").value(false))
                .andExpect(jsonPath("data.allowModificationAndDeletion").value(true))
                .andExpect(jsonPath("message").value(MessageVo.REPLY_REGISTER_SUCCESSFULLY))
        ;

        List<Reply> findReplyList = replyRepository.findAll();
        boolean replyContainsFlag = false;
        for (Reply reply : findReplyList) {
            boolean contains = reply.getContent().contains(test_content);
            if (contains) {
                replyContainsFlag = true;
            }
        }
        assertTrue(replyContainsFlag);

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 댓글을 등록하는 경우")
    public void addReply_Unauthorized() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid","test");
        Member user1 = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        memberCreateDto.setJoinId("user2");
        memberCreateDto.setNickname("user2");
        Member user2 = memberService.userJoin(memberCreateDto);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        String test_content = "test content";
        ReplyCreateDto replyCreateDto = new ReplyCreateDto(test_content);
        //when

        ResultActions perform = mockMvc
                .perform(
                        post("/api/bbs/reply/" + bbsSample.getId())
//                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(replyCreateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("댓글 등록 시 게시글을 찾을 수 없는 경우")
    public void addReply_BbsNotFound() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid","test");
        Member user1 = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        memberCreateDto.setJoinId("user2");
        memberCreateDto.setNickname("user2");
        Member user2 = memberService.userJoin(memberCreateDto);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        String test_content = "test content";
        ReplyCreateDto replyCreateDto = new ReplyCreateDto(test_content);
        //when

        ResultActions perform = mockMvc
                .perform(
                        post("/api/bbs/reply/" + 100000L)
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(replyCreateDto))
                )
                .andDo(print());


        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new BbsNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("삭제된 게시글에 댓글을 작성하는 경우")
    public void addReply_To_DeletedBbs() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid","test");
        Member user1 = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.DELETE);

        memberCreateDto.setJoinId("user2");
        memberCreateDto.setNickname("user2");
        Member user2 = memberService.userJoin(memberCreateDto);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        String test_content = "test content";
        ReplyCreateDto replyCreateDto = new ReplyCreateDto(test_content);

        //when

        ResultActions perform = mockMvc
                .perform(
                        post("/api/bbs/reply/" + bbsSample.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(replyCreateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("삭제된 게시글 입니다. : 삭제된 게시글에는 댓글을 작성할 수 없습니다."));

    }

    @Test
    @DisplayName("댓글 등록 시 댓글 내용이 비어있는 경우")
    public void addReply_Empty() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid","test");
        Member user1 = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        memberCreateDto.setJoinId("user2");
        memberCreateDto.setNickname("user2");
        Member user2 = memberService.userJoin(memberCreateDto);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        ReplyCreateDto replyCreateDto = new ReplyCreateDto();
        //when

        ResultActions perform = mockMvc
                .perform(
                        post("/api/bbs/reply/" + bbsSample.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(replyCreateDto))
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
    @DisplayName("댓글에 댓글 등록")
    public void addReplyOfReply() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid","test");
        Member user1 = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        memberCreateDto.setJoinId("user2");
        memberCreateDto.setNickname("user2");
        Member user2 = memberService.userJoin(memberCreateDto);

        Reply parent = replyService.addReply(user2.getId(), bbsSample.getId(), "test content");

        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user3", "user3");
        Member user3 = memberService.userJoin(memberCreateDto2);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user3.getLoginInfo().getRefreshToken());
        TokenDto user3TokenDto = memberService.refresh(onlyTokenDto);

        ReplyCreateDto replyCreateDto = new ReplyCreateDto("test reply of reply content");
        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/bbs/reply/reply/" + parent.getId())
                                .header(X_AUTH_TOKEN, user3TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(replyCreateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isCreated())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.writer").exists())
                .andExpect(jsonPath("data.content").exists())
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("data.allowModificationAndDeletion").value(true))
                .andExpect(jsonPath("message").value(MessageVo.REPLY_REGISTER_SUCCESSFULLY))
                .andExpect(redirectedUrl("http://localhost/api/bbs/reply"))
        ;

        List<Reply> findReplyList = replyRepository.findAll();

        boolean replyOfReplyContainsFlag = false;
        for (Reply reply : findReplyList) {
            if (reply.getParent() != null) {
                boolean equals = reply.getParent().getId().equals(parent.getId());
                if (equals) {
                    replyOfReplyContainsFlag = true;
                    break;
                }
            }
        }

        assertTrue(replyOfReplyContainsFlag);

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 댓글에 댓글을 등록하는 경우")
    public void addReplyOfReply_Unauthorized() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid","test");
        Member user1 = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        memberCreateDto.setJoinId("user2");
        memberCreateDto.setNickname("user2");
        Member user2 = memberService.userJoin(memberCreateDto);

        Reply parent = replyService.addReply(user2.getId(), bbsSample.getId(), "test content");

        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user3", "user3");
        Member user3 = memberService.userJoin(memberCreateDto2);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user3.getLoginInfo().getRefreshToken());
        TokenDto user3TokenDto = memberService.refresh(onlyTokenDto);

        ReplyCreateDto replyCreateDto = new ReplyCreateDto("test reply of reply content");
        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/bbs/reply/reply/" + parent.getId())
//                                .header(X_AUTH_TOKEN, user3TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(replyCreateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("댓글에 댓글 등록 시 부모 댓글이 없는 경우")
    public void addReplyOfReply_Parent_NotFound() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid","test");
        Member user1 = memberService.userJoin(memberCreateDto);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        memberCreateDto.setJoinId("user2");
        memberCreateDto.setNickname("user2");
        Member user2 = memberService.userJoin(memberCreateDto);

        Reply parent = replyService.addReply(user2.getId(), bbsSample.getId(), "test content");

        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user3", "user3");
        Member user3 = memberService.userJoin(memberCreateDto2);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user3.getLoginInfo().getRefreshToken());
        TokenDto user3TokenDto = memberService.refresh(onlyTokenDto);

        ReplyCreateDto replyCreateDto = new ReplyCreateDto("test reply of reply content");
        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/bbs/reply/reply/" + 10000L)
                                .header(X_AUTH_TOKEN, user3TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(replyCreateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new ReplyNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("부모 댓글 목록 조회")
    public void getReplyParentList() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        MemberCreateDto memberCreateDto3 = getMemberCreateDto("user3", "user3");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);
        Member user3 = memberService.userJoin(memberCreateDto3);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        List<Reply> replyList = createReplyList(user2, bbsSample);

        replyService.addReply(user3.getId(), bbsSample.getId(), "user3 content");

        replyLikeService.like(user3.getId(), replyList.get(3).getId());
        replyLikeService.like(user3.getId(), replyList.get(5).getId());

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user3.getLoginInfo().getRefreshToken());
        TokenDto user3TokenDto = memberService.refresh(onlyTokenDto);
        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs/reply/" + bbsSample.getId())
                                .header(X_AUTH_TOKEN, user3TokenDto.getAccessToken())
                                .param("pageNum", "1")
                                .param("limit", "15")
                                .param("sortType", ReplySortType.LATEST_DESC.name())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data[0].id").exists())
                .andExpect(jsonPath("data[0].writer").exists())
                .andExpect(jsonPath("data[0].content").exists())
                .andExpect(jsonPath("data[0].likeCount").exists())
                .andExpect(jsonPath("data[0].registerDate").exists())
                .andExpect(jsonPath("data[0].like").exists())
                .andExpect(jsonPath("data[0].viewLike").exists())
                .andExpect(jsonPath("data[0].allowModificationAndDeletion").exists())
                .andExpect(jsonPath("message").value(MessageVo.GET_PARENT_REPLY_LIST_SUCCESSFULLY))
        ;

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 부모 댓글 목록을 조회하는 경우")
    public void getReplyParentList_UnAuthentication() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        MemberCreateDto memberCreateDto3 = getMemberCreateDto("user3", "user3");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);
        Member user3 = memberService.userJoin(memberCreateDto3);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        List<Reply> replyList = createReplyList(user2, bbsSample);

        replyService.addReply(user3.getId(), bbsSample.getId(), "user3 content");

        replyLikeService.like(user3.getId(), replyList.get(3).getId());
        replyLikeService.like(user3.getId(), replyList.get(5).getId());

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user3.getLoginInfo().getRefreshToken());
        TokenDto user3TokenDto = memberService.refresh(onlyTokenDto);
        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs/reply/" + bbsSample.getId())
//                                .header(X_AUTH_TOKEN, user3TokenDto.getAccessToken())
                                .param("pageNum", "1")
                                .param("limit", "15")
                                .param("sortType", ReplySortType.LATEST_DESC.name())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data[0].id").exists())
                .andExpect(jsonPath("data[0].writer").exists())
                .andExpect(jsonPath("data[0].content").exists())
                .andExpect(jsonPath("data[0].likeCount").exists())
                .andExpect(jsonPath("data[0].registerDate").exists())
                .andExpect(jsonPath("data[0].like").exists())
                .andExpect(jsonPath("data[0].viewLike").exists())
                .andExpect(jsonPath("data[0].allowModificationAndDeletion").exists())
        ;

    }

    @Test
    @DisplayName("댓글을 조회하려는 게시글을 찾을 수 없는 경우")
    public void getReplyParentList_Bbs_NotFound() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        MemberCreateDto memberCreateDto3 = getMemberCreateDto("user3", "user3");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);
        Member user3 = memberService.userJoin(memberCreateDto3);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        List<Reply> replyList = createReplyList(user2, bbsSample);

        replyService.addReply(user3.getId(), bbsSample.getId(), "user3 content");

        replyLikeService.like(user3.getId(), replyList.get(3).getId());
        replyLikeService.like(user3.getId(), replyList.get(5).getId());

        CriteriaDto criteriaDto = new CriteriaDto();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user3.getLoginInfo().getRefreshToken());
        TokenDto user3TokenDto = memberService.refresh(onlyTokenDto);
        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs/reply/" + 10000L)
                                .header(X_AUTH_TOKEN, user3TokenDto.getAccessToken())
                                .param("pageNum", "1")
                                .param("limit", "15")
                                .param("sortType", ReplySortType.LATEST_DESC.name())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new BbsNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("댓글을 조회하려는 게시글이 삭제된 게시글인 경우")
    public void getReplyParentList_Of_DeletedBbs() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        MemberCreateDto memberCreateDto3 = getMemberCreateDto("user3", "user3");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);
        Member user3 = memberService.userJoin(memberCreateDto3);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        List<Reply> replyList = createReplyList(user2, bbsSample);

        replyService.addReply(user3.getId(), bbsSample.getId(), "user3 content");

        replyLikeService.like(user3.getId(), replyList.get(3).getId());
        replyLikeService.like(user3.getId(), replyList.get(5).getId());

        CriteriaDto criteriaDto = new CriteriaDto();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user3.getLoginInfo().getRefreshToken());
        TokenDto user3TokenDto = memberService.refresh(onlyTokenDto);

        bbsService.deleteBbs(bbsSample.getId());
        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs/reply/" + bbsSample.getId())
                                .header(X_AUTH_TOKEN, user3TokenDto.getAccessToken())
                                .param("pageNum", "1")
                                .param("limit", "15")
                                .param("sortType", ReplySortType.LATEST_DESC.name())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("삭제된 게시글 입니다. : 삭제된 게시글의 댓글은 조회할 수 없습니다."))
        ;

    }

    @Test
    @DisplayName("댓글에 등록된 댓글 목록 조회")
    public void getReplyChildList() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        MemberCreateDto memberCreateDto3 = getMemberCreateDto("user3", "user3");
        MemberCreateDto memberCreateDto4 = getMemberCreateDto("user4", "user4");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);
        Member user3 = memberService.userJoin(memberCreateDto3);
        Member user4 = memberService.userJoin(memberCreateDto4);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        Reply parent = replyService.addReply(user2.getId(), bbsSample.getId(), "user2 parent reply");

        createReplyChildList(parent, user3.getId(), "user3 child reply content");
        createReplyChildList(parent, user4.getId(), "user4 child reply content");

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs/reply/reply/" + parent.getId())
                                .param("pageNum", "1")
                                .param("limit", "15")
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data[0].id").exists())
                .andExpect(jsonPath("data[0].writer").exists())
                .andExpect(jsonPath("data[0].content").exists())
                .andExpect(jsonPath("data[0].registerDate").exists())
                .andExpect(jsonPath("data[0].allowModificationAndDeletion").exists())
                .andExpect(jsonPath("message").value(MessageVo.GET_CHILD_REPLY_LIST_SUCCESSFULLY))
        ;

    }

    @Test
    @DisplayName("USER 권한의 회원이 댓글에 등록된 댓글 목록을 조회하는 경우")
    public void getReplyChildList_By_User() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        MemberCreateDto memberCreateDto3 = getMemberCreateDto("user3", "user3");
        MemberCreateDto memberCreateDto4 = getMemberCreateDto("user4", "user4");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);
        Member user3 = memberService.userJoin(memberCreateDto3);
        Member user4 = memberService.userJoin(memberCreateDto4);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        Reply parent = replyService.addReply(user2.getId(), bbsSample.getId(), "user2 parent reply");

        createReplyChildList(parent, user3.getId(), "user3 child reply content");
        createReplyChildList(parent, user4.getId(), "user4 child reply content");

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user3.getLoginInfo().getRefreshToken());
        TokenDto user3TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs/reply/reply/" + parent.getId())
                                .header(X_AUTH_TOKEN, user3TokenDto.getAccessToken())
                                .param("pageNum", "1")
                                .param("limit", "15")
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data[0].id").exists())
                .andExpect(jsonPath("data[0].writer").exists())
                .andExpect(jsonPath("data[0].content").exists())
                .andExpect(jsonPath("data[0].registerDate").exists())
                .andExpect(jsonPath("data[0].allowModificationAndDeletion").exists())
                .andExpect(jsonPath("message").value(MessageVo.GET_CHILD_REPLY_LIST_SUCCESSFULLY))
        ;

    }

    private void createReplyChildList(Reply parent, Long user3Id, String user3Content) {
        for (int i = 0; i < 6; i++) {
            replyService.addReplyOfReply(user3Id, parent.getId(), user3Content + i);
        }
    }

    @Test
    @DisplayName("댓글에 등록된 댓글 목록 조회 시 부모 댓글이 없는 경우")
    public void getReplyChildList_ParentReply_NotFound() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        MemberCreateDto memberCreateDto3 = getMemberCreateDto("user3", "user3");
        MemberCreateDto memberCreateDto4 = getMemberCreateDto("user4", "user4");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);
        Member user3 = memberService.userJoin(memberCreateDto3);
        Member user4 = memberService.userJoin(memberCreateDto4);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        Reply parent = replyService.addReply(user2.getId(), bbsSample.getId(), "user2 parent reply");

        createReplyChildList(parent, user3.getId(), "user3 child reply content");
        createReplyChildList(parent, user4.getId(), "user4 child reply content");

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs/reply/reply/" + 10000L)
                                .param("pageNum", "1")
                                .param("limit", "15")
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new ReplyNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("댓글에 등록된 댓글 목록 조회 시 부모 댓글이 삭제된 댓글일 경우")
    public void getReplyChildList_Of_DeletedParent() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        MemberCreateDto memberCreateDto3 = getMemberCreateDto("user3", "user3");
        MemberCreateDto memberCreateDto4 = getMemberCreateDto("user4", "user4");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);
        Member user3 = memberService.userJoin(memberCreateDto3);
        Member user4 = memberService.userJoin(memberCreateDto4);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        Reply parent = replyService.addReply(user2.getId(), bbsSample.getId(), "user2 parent reply");

        createReplyChildList(parent, user3.getId(), "user3 child reply content");
        createReplyChildList(parent, user4.getId(), "user4 child reply content");

        replyService.deleteReply(parent.getId());

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/bbs/reply/reply/" + parent.getId())
                                .param("pageNum", "1")
                                .param("limit", "15")
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("삭제된 댓글 입니다. : 삭제된 댓글에 등록된 댓글은 조회가 불가능합니다."))
        ;

    }

    @Test
    @DisplayName("댓글 수정")
    public void modifyReply() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        Reply reply = replyService.addReply(user2.getId(), bbsSample.getId(), "user2 reply content");

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        String user2_update_content = "user2 update content";
        ReplyUpdateDto replyUpdateDto = new ReplyUpdateDto(user2_update_content);

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/bbs/reply/" + reply.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(replyUpdateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.writer").exists())
                .andExpect(jsonPath("data.content").exists())
                .andExpect(jsonPath("data.likeCount").exists())
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("data.like").exists())
                .andExpect(jsonPath("data.viewLike").value(false))
                .andExpect(jsonPath("data.allowModificationAndDeletion").value(true))
                .andExpect(jsonPath("message").value(MessageVo.MODIFY_REPLY_SUCCESSFULLY));

        Reply findReply = replyService.getReply(reply.getId());
        assertEquals(findReply.getContent(), user2_update_content);

    }

    @Test
    @DisplayName("다른 회원의 댓글을 수정하는 경우")
    public void modifyReply_Of_DifferentMember() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        Reply reply = replyService.addReply(user2.getId(), bbsSample.getId(), "user2 reply content");

        MemberCreateDto memberCreateDto3 = getMemberCreateDto("user3", "user3");
        Member user3 = memberService.userJoin(memberCreateDto3);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user3.getLoginInfo().getRefreshToken());
        TokenDto user3TokenDto = memberService.refresh(onlyTokenDto);

        String user3_update_content = "user3 update content";
        ReplyUpdateDto replyUpdateDto = new ReplyUpdateDto(user3_update_content);

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/bbs/reply/" + reply.getId())
                                .header(X_AUTH_TOKEN, user3TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(replyUpdateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.MODIFY_REPLY_OF_DIFFERENT_MEMBER));

    }

    @Test
    @DisplayName("댓글 수정 시 수정하려는 댓글을 찾을 수 없는 경우")
    public void modifyReply_NotFound() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        Reply reply = replyService.addReply(user2.getId(), bbsSample.getId(), "user2 reply content");

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        String user2_update_content = "user2 update content";
        ReplyUpdateDto replyUpdateDto = new ReplyUpdateDto(user2_update_content);

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/bbs/reply/" + 10000L)
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(replyUpdateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new ReplyNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("삭제된 댓글을 수정하는 경우")
    public void modify_DeletedReply() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        Reply reply = replyService.addReply(user2.getId(), bbsSample.getId(), "user2 reply content");

        replyService.deleteReply(reply.getId());

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        String user2_update_content = "user2 update content";
        ReplyUpdateDto replyUpdateDto = new ReplyUpdateDto(user2_update_content);

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/bbs/reply/" + reply.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(replyUpdateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("삭제된 댓글 입니다. : 삭제된 댓글은 수정할 수 없습니다."))
        ;

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 댓글을 수정하는 경우")
    public void modifyReply_Unauthorized() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        Reply reply = replyService.addReply(user2.getId(), bbsSample.getId(), "user2 reply content");

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        String user2_update_content = "user2 update content";
        ReplyUpdateDto replyUpdateDto = new ReplyUpdateDto(user2_update_content);

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/bbs/reply/" + reply.getId())
//                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(replyUpdateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("댓글 삭제")
    public void deleteReply() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        Reply reply = replyService.addReply(user2.getId(), bbsSample.getId(), "user2 reply content");

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        delete("/api/bbs/reply/" + reply.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value(MessageVo.DELETE_REPLY_SUCCESSFULLY));

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 댓글을 삭제하는 경우")
    public void deleteReply_Unauthorized() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        Reply reply = replyService.addReply(user2.getId(), bbsSample.getId(), "user2 reply content");

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        delete("/api/bbs/reply/" + reply.getId())
//                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("관리자가 댓글을 삭제하는 경우")
    public void deleteReply_By_Admin() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);

        MemberCreateDto adminCreateDto = getMemberCreateDto("admin", "admin");
        Member admin = memberService.adminJoin(adminCreateDto);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        Reply reply = replyService.addReply(user2.getId(), bbsSample.getId(), "user2 reply content");

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(admin.getLoginInfo().getRefreshToken());
        TokenDto adminTokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        delete("/api/bbs/reply/" + reply.getId())
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value(MessageVo.DELETE_REPLY_SUCCESSFULLY));
    }

    @Test
    @DisplayName("댓글 삭제 시 인증된 회원과 삭제되는 게시글의 작성자가 다른 경우")
    public void deleteReply_Of_DifferentMember() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        Reply reply = replyService.addReply(user2.getId(), bbsSample.getId(), "user2 reply content");

        MemberCreateDto memberCreateDto3 = getMemberCreateDto("user3", "user3");
        Member user3 = memberService.userJoin(memberCreateDto3);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user3.getLoginInfo().getRefreshToken());
        TokenDto user3TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        delete("/api/bbs/reply/" + reply.getId())
                                .header(X_AUTH_TOKEN, user3TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.DELETE_REPLY_OF_DIFFERENT_MEMBER));

    }

    @Test
    @DisplayName("댓글 삭제 시 삭제할 댓글을 찾을 수 없는 경우")
    public void deleteReply_NotFound() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        Reply reply = replyService.addReply(user2.getId(), bbsSample.getId(), "user2 reply content");

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        delete("/api/bbs/reply/" + 100000L)
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new ReplyNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("댓글 삭제 시 이미 삭제된 댓글인 경우")
    public void delete_DeletedReply() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        Reply reply = replyService.addReply(user2.getId(), bbsSample.getId(), "user2 reply content");

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        replyService.deleteReply(reply.getId());

        //when
        ResultActions perform = mockMvc
                .perform(
                        delete("/api/bbs/reply/" + reply.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("message").value())
        ;

    }

    @Test
    @DisplayName("댓글에 좋아요 등록")
    public void addLikeToReply() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        MemberCreateDto memberCreateDto3 = getMemberCreateDto("user3", "user3");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);
        Member user3 = memberService.userJoin(memberCreateDto3);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        Reply user2Reply = replyService.addReply(user2.getId(), bbsSample.getId(), "user2 reply content");

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user3.getLoginInfo().getRefreshToken());
        TokenDto user3TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/bbs/reply/like/" + user2Reply.getId())
                                .header(X_AUTH_TOKEN, user3TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value(MessageVo.ADD_LIKE_TO_REPLY_SUCCESSFULLY));

    }

    @Test
    @DisplayName("댓글에 좋아요 등록 시 댓글이 없는경우")
    public void addLikeToReply_NotFound() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        MemberCreateDto memberCreateDto3 = getMemberCreateDto("user3", "user3");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);
        Member user3 = memberService.userJoin(memberCreateDto3);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        Reply user2Reply = replyService.addReply(user2.getId(), bbsSample.getId(), "user2 reply content");

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user3.getLoginInfo().getRefreshToken());
        TokenDto user3TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/bbs/reply/like/" + 10000L)
                                .header(X_AUTH_TOKEN, user3TokenDto.getAccessToken())
                )
                .andDo(print());


        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new ReplyNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("댓글에 등록된 댓글에 좋아요를 등록하는 경우")
    public void addLikeToChildReply() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        MemberCreateDto memberCreateDto3 = getMemberCreateDto("user3", "user3");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);
        Member user3 = memberService.userJoin(memberCreateDto3);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        Reply user2Reply = replyService.addReply(user2.getId(), bbsSample.getId(), "user2 reply content");

        Reply user3ChildReply = replyService.addReplyOfReply(user3.getId(), user2Reply.getId(), "user3 child reply content");

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/bbs/reply/like/" + user3ChildReply.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                )
                .andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new AddLikeToChildReplyException().getMessage()));

    }

    @Test
    @DisplayName("이미 좋아요를 등록한 댓글에 다시 좋아요를 등록하는 경우")
    public void addLikeToReply_ExistLike() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        MemberCreateDto memberCreateDto3 = getMemberCreateDto("user3", "user3");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);
        Member user3 = memberService.userJoin(memberCreateDto3);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        Reply user2Reply = replyService.addReply(user2.getId(), bbsSample.getId(), "user2 reply content");

        replyLikeService.like(user3.getId(), user2Reply.getId());

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user3.getLoginInfo().getRefreshToken());
        TokenDto user3TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/bbs/reply/like/" + user2Reply.getId())
                                .header(X_AUTH_TOKEN, user3TokenDto.getAccessToken())
                )
                .andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new ExistLikeException().getMessage()));

    }

    @Test
    @DisplayName("삭제된 댓글에 좋아요를 등록하는 경우")
    public void addLikeToDeletedReply() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        MemberCreateDto memberCreateDto3 = getMemberCreateDto("user3", "user3");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);
        Member user3 = memberService.userJoin(memberCreateDto3);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        Reply user2Reply = replyService.addReply(user2.getId(), bbsSample.getId(), "user2 reply content");

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user3.getLoginInfo().getRefreshToken());
        TokenDto user3TokenDto = memberService.refresh(onlyTokenDto);

        replyService.deleteReply(user2Reply.getId());

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/bbs/reply/like/" + user2Reply.getId())
                                .header(X_AUTH_TOKEN, user3TokenDto.getAccessToken())
                )
                .andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
        .andExpect(jsonPath("message").value("삭제된 댓글 입니다. : 삭제된 댓글에는 좋아요를 누를 수 없습니다."))
        ;

    }

    @Test
    @DisplayName("자신이 쓴 댓글에 좋아요를 등록하는 경우")
    public void addLikeToReply_SelfLike() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        Reply user2Reply = replyService.addReply(user2.getId(), bbsSample.getId(), "user2 reply content");

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/bbs/reply/like/" + user2Reply.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                )
                .andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new SelfLikeException().getMessage()));

    }

    @Test
    @DisplayName("댓글에 등록된 좋아요 해제")
    public void unLikeReply() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        MemberCreateDto memberCreateDto3 = getMemberCreateDto("user3", "user3");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);
        Member user3 = memberService.userJoin(memberCreateDto3);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        Reply reply = replyService.addReply(user2.getId(), bbsSample.getId(), "user2 reply content");

        replyLikeService.like(user3.getId(), reply.getId());

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user3.getLoginInfo().getRefreshToken());
        TokenDto user3TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        delete("/api/bbs/reply/like/" + reply.getId())
                                .header(X_AUTH_TOKEN, user3TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value(MessageVo.UNLIKE_REPLY_SUCCESSFULLY));

    }

    @Test
    @DisplayName("좋아요가 등록되지 않은 댓글의 좋아요를 해제하는 경우")
    public void unLikeReply_NoExistLike() throws Exception {
        //given
        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        MemberCreateDto memberCreateDto2 = getMemberCreateDto("user2", "user2");
        MemberCreateDto memberCreateDto3 = getMemberCreateDto("user3", "user3");
        Member user1 = memberService.userJoin(memberCreateDto1);
        Member user2 = memberService.userJoin(memberCreateDto2);
        Member user3 = memberService.userJoin(memberCreateDto3);

        Bbs bbsSample = getBbsSample(user1, BbsStatus.REGISTER);

        Reply reply = replyService.addReply(user2.getId(), bbsSample.getId(), "user2 reply content");

//        replyLikeService.like(user3.getId(), reply.getId());

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user3.getLoginInfo().getRefreshToken());
        TokenDto user3TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        delete("/api/bbs/reply/like/" + reply.getId())
                                .header(X_AUTH_TOKEN, user3TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new NoExistLikeException().getMessage()));

    }

    private List<Reply> createReplyList(Member user2, Bbs bbsSample) {
        List<Reply> replyList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Reply reply = replyService.addReply(user2.getId(), bbsSample.getId(), "user2 content" + i);
            replyList.add(reply);
        }
        return replyList;
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