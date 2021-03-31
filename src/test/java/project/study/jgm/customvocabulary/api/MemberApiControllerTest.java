package project.study.jgm.customvocabulary.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import project.study.jgm.customvocabulary.common.BaseControllerTest;
import project.study.jgm.customvocabulary.common.dto.MessageVo;
import project.study.jgm.customvocabulary.members.Gender;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRole;
import project.study.jgm.customvocabulary.members.dto.MemberCreateDto;
import project.study.jgm.customvocabulary.members.dto.MemberUpdateDto;
import project.study.jgm.customvocabulary.members.dto.PasswordUpdateDto;
import project.study.jgm.customvocabulary.members.dto.search.MemberSearchType;
import project.study.jgm.customvocabulary.members.dto.search.MemberSortType;
import project.study.jgm.customvocabulary.members.exception.ExistDuplicatedMemberException;
import project.study.jgm.customvocabulary.members.exception.MemberAlreadyHasAuthorityException;
import project.study.jgm.customvocabulary.members.exception.MemberNotFoundException;
import project.study.jgm.customvocabulary.security.dto.LoginDto;
import project.study.jgm.customvocabulary.security.dto.TokenDto;
import project.study.jgm.customvocabulary.security.exception.PasswordMismatchException;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static project.study.jgm.customvocabulary.common.LinkToCreator.linkToGetMember;
import static project.study.jgm.customvocabulary.common.LinkToCreator.linkToIndex;

public class MemberApiControllerTest extends BaseControllerTest {

    @BeforeEach
    public void setup() {
        replyLikeRepository.deleteAll();
        replyRepository.deleteAll();
        bbsLikeRepository.deleteAll();
        bbsRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입")
    public void join() throws Exception {
        //given
        String joinId = "testJoinid";
        String nickname = "test";
        MemberCreateDto createDto = getMemberCreateDto(joinId, nickname);

        //when
        mockMvc.perform(
                post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)
                        ))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.joinId").value(createDto.getJoinId()))
                .andExpect(jsonPath("data.email").value(createDto.getEmail()))
                .andExpect(jsonPath("data.name").value(createDto.getName()))
                .andExpect(jsonPath("data.nickname").value(createDto.getNickname()))
                .andExpect(jsonPath("data.dateOfBirth").value(createDto.getDateOfBirth().toString()))
                .andExpect(jsonPath("data.gender").value(createDto.getGender().name()))
                .andExpect(jsonPath("data.simpleAddress").value(createDto.getSimpleAddress()))
                .andExpect(jsonPath("data.sharedVocabularyCount").value(0))
                .andExpect(jsonPath("data.bbsCount").value(0))
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("data.updateDate").exists())
                .andExpect(jsonPath("data.loginInfo.refreshToken").exists())
                .andExpect(jsonPath("data.loginInfo.refreshTokenExpirationPeriodDateTime").exists())
//                .andExpect(jsonPath("_links.self.href").exists())
//                .andExpect(jsonPath("_links.login.href").exists())
                .andExpect(jsonPath("message").value(MessageVo.MEMBER_JOIN_SUCCESSFULLY))
                .andDo(document("join",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                        ),
                        requestFields(
                                fieldWithPath("joinId").description("로그인을 위해 사용할 ID"),
                                fieldWithPath("email").description("가입자 개인 Email"),
                                fieldWithPath("password").description("로그인 시점에 사용할 비밀번호"),
                                fieldWithPath("name").description("가입자 성함"),
                                fieldWithPath("nickname").description("Custom Vocabulary 내의 활동명"),
                                fieldWithPath("dateOfBirth").description("생년월일"),
                                fieldWithPath("gender").description("성별 [MALE, FEMALE]"),
                                fieldWithPath("simpleAddress").description("간략한 주소지 기입 (생략 가능)")
                        ),
                        responseFields(
                                fieldWithPath("data.id").description("가입자의 식별 ID 값"),
                                fieldWithPath("data.joinId").description("가입자의 로그인 ID"),
                                fieldWithPath("data.email").description("가입자의 Email"),
                                fieldWithPath("data.name").description("가입자 성함"),
                                fieldWithPath("data.nickname").description("Custom Vocabulary 활동명"),
                                fieldWithPath("data.dateOfBirth").description("생년월일"),
                                fieldWithPath("data.gender").description("성별"),
                                fieldWithPath("data.simpleAddress").description("간략한 주소지"),
                                fieldWithPath("data.sharedVocabularyCount").description("해당 회원이 공유한 단어장 개수"),
                                fieldWithPath("data.bbsCount").description("해당 회원이 작성한 게시글 개수"),
                                fieldWithPath("data.loginInfo.refreshToken").description("Access Token을 재발급 받기 위한 Refresh Token"),
                                fieldWithPath("data.loginInfo.refreshTokenExpirationPeriodDateTime").description("Refresh Token 만료 날짜"),
                                fieldWithPath("data.registerDate").description("가입일시"),
                                fieldWithPath("data.updateDate").description("회원정보 수정 일시"),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;

        //then

    }

    @Test
    @DisplayName("회원가입 할 때 회원정보를 빼먹었을 경우 테스트")
    public void join_Empty() throws Exception {
        //given
        MemberCreateDto createDto = MemberCreateDto.builder().build();

        //when

        //then
        mockMvc.perform(
                post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)
                        ))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;

    }

    @Test
    @DisplayName("회원가입 시 중복된 회원이 있는 경우")
    public void join_ExistDuplicatedMemberException() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");

        //when
        mockMvc.perform(
                post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberCreateDto1)
                        ))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new ExistDuplicatedMemberException().getMessage()))
        ;

        //then

    }

    @Test
    @DisplayName("회원조회")
    public void getMember() throws Exception {
        //given
        String joinId = "testJoinid";
        String nickname = "test";
        MemberCreateDto createDto = getMemberCreateDto(joinId, nickname);
        Member member = memberService.userJoin(createDto);
        LoginDto loginDto = getLoginDto(createDto);

        TokenDto tokenDto = memberService.login(loginDto);

        //when

        mockMvc.perform(
                get("/api/members/" + member.getId())
                        .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").value(member.getId()))
                .andExpect(jsonPath("data.joinId").value(createDto.getJoinId()))
                .andExpect(jsonPath("data.email").value(createDto.getEmail()))
                .andExpect(jsonPath("data.name").value(createDto.getName()))
                .andExpect(jsonPath("data.nickname").value(createDto.getNickname()))
                .andExpect(jsonPath("data.dateOfBirth").value(createDto.getDateOfBirth().toString()))
                .andExpect(jsonPath("data.gender").value(createDto.getGender().name()))
                .andExpect(jsonPath("data.simpleAddress").value(createDto.getSimpleAddress()))
                .andExpect(jsonPath("data.sharedVocabularyCount").value(0))
                .andExpect(jsonPath("data.bbsCount").value(0))
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("data.updateDate").exists())
                .andExpect(jsonPath("data.loginInfo.refreshToken").exists())
                .andExpect(jsonPath("data.loginInfo.refreshTokenExpirationPeriodDateTime").exists())
//                .andExpect(jsonPath("_links.self.href").exists())
//                .andExpect(jsonPath("_links.index.href").exists())
                .andExpect(jsonPath("message").value(MessageVo.GET_MEMBER_SUCCESSFULLY))
                .andDo(document("get-member",
                        requestHeaders(
                                headerWithName(X_AUTH_TOKEN).description(X_AUTH_TOKEN_DESCRIPTION)
                        ),
                        responseFields(
                                fieldWithPath("data.id").description("가입자의 식별 ID 값"),
                                fieldWithPath("data.joinId").description("가입자의 로그인 ID"),
                                fieldWithPath("data.email").description("가입자의 Email"),
                                fieldWithPath("data.name").description("가입자 성함"),
                                fieldWithPath("data.nickname").description("Custom Vocabulary 활동명"),
                                fieldWithPath("data.dateOfBirth").description("생년월일"),
                                fieldWithPath("data.gender").description("성별"),
                                fieldWithPath("data.simpleAddress").description("간략한 주소지"),
                                fieldWithPath("data.sharedVocabularyCount").description("해당 회원이 공유한 단어장 개수"),
                                fieldWithPath("data.bbsCount").description("해당 회원이 작성한 게시글 개수"),
                                fieldWithPath("data.loginInfo.refreshToken").description("Access Token을 재발급 받기 위한 Refresh Token"),
                                fieldWithPath("data.loginInfo.refreshTokenExpirationPeriodDateTime").description("Refresh Token 만료 날짜"),
                                fieldWithPath("data.registerDate").description("가입일시"),
                                fieldWithPath("data.updateDate").description("회원정보 수정 일시"),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;
        //then

    }

    @Test
    @DisplayName("탈퇴한 회원이 회원을 조회하는 경우")
    public void getMember_By_SecessionMember() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid", "test");
        Member userMember = memberService.userJoin(memberCreateDto);

        memberService.secession(userMember.getId(), memberCreateDto.getPassword());

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto tokenDto = memberService.login(loginDto);

        //when

        //then
        mockMvc.perform(
                get("/api/members/" + userMember.getId())
                        .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
        )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("회원 조회 시 조회할 회원이 없는 경우")
    public void getMember_NotFound() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid", "test");
        memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto tokenDto = memberService.login(loginDto);

        //when

        //then
        mockMvc.perform(
                get("/api/members/" + 1000L)
                        .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
        )
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").exists())
//                .andExpect(jsonPath("_links.self.href").exists())
//                .andExpect(jsonPath("_links.index.href").exists())
        ;

    }

    @Test
    @DisplayName("인증 권한이 없는 회원이 회원을 조회할 경우")
    public void getMember_No_Authentication() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid", "test");
        Member member = memberService.userJoin(memberCreateDto);
        //when

        //then
        mockMvc.perform(
                get("/api/members/" + member.getId())
//                        .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
        )
                .andDo(print())
                .andExpect(status().isForbidden())
        ;

    }

    @Test
    @DisplayName("관리자가 다른 사용자를 조회하는 경우")
    public void admin_Get_Of_DifferentMember() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid", "test");
        memberService.adminJoin(memberCreateDto);

        LoginDto adminLoginDto = getLoginDto(memberCreateDto);
        TokenDto adminTokenDto = memberService.login(adminLoginDto);

        memberCreateDto.setJoinId("userjoinId");
        memberCreateDto.setNickname("testusernickname");
        Member userMember = memberService.userJoin(memberCreateDto);

        //when

        //then
        mockMvc
                .perform(
                        get("/api/members/" + userMember.getId())
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").value(userMember.getId()))
                .andExpect(jsonPath("data.joinId").value(memberCreateDto.getJoinId()))
                .andExpect(jsonPath("data.email").value(memberCreateDto.getEmail()))
                .andExpect(jsonPath("data.name").value(memberCreateDto.getName()))
                .andExpect(jsonPath("data.nickname").value(memberCreateDto.getNickname()))
                .andExpect(jsonPath("data.dateOfBirth").value(memberCreateDto.getDateOfBirth().toString()))
                .andExpect(jsonPath("data.gender").value(memberCreateDto.getGender().name()))
                .andExpect(jsonPath("data.simpleAddress").value(memberCreateDto.getSimpleAddress()))
                .andExpect(jsonPath("data.sharedVocabularyCount").value(0))
                .andExpect(jsonPath("data.bbsCount").value(0))
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("data.updateDate").exists())
                .andExpect(jsonPath("data.roles.[0]").value(MemberRole.USER.name()))
                .andExpect(jsonPath("message").value(MessageVo.GET_MEMBER_BY_ADMIN_SUCCESSFULLY))
//                .andExpect(jsonPath("loginInfo.refreshToken").exists())
//                .andExpect(jsonPath("loginInfo.refreshTokenExpirationPeriodDateTime").exists())
//                .andExpect(jsonPath("_links.self.href").exists())
//                .andExpect(jsonPath("_links.index.href").exists())
        ;

    }

    @Test
    @DisplayName("인증된 사용자와 조회하려는 사용자가 다른 경우")
    public void getMember_Unauthorized() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid", "test");
        memberService.userJoin(memberCreateDto);
        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto tokenDto = memberService.login(loginDto);
        memberCreateDto.setJoinId("jonfdsa");
        memberCreateDto.setNickname("testusernickname");
        Member adminMember = memberService.adminJoin(memberCreateDto);
        //when

        //then
        mockMvc
                .perform(
                        get("/api/members/" + adminMember.getId())
                                .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
                )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.GET_DIFFERENT_MEMBER_INFO))
//                .andExpect(jsonPath("_links.self.href").exists())
//                .andExpect(jsonPath("_links.index.href").exists())
        ;


    }

    @Test
    @DisplayName("회원 정보 수정")
    public void modifyMember() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid", "test");
        Member joinMember = memberService.userJoin(memberCreateDto);
        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto tokenDto = memberService.login(loginDto);

        MemberUpdateDto memberUpdateDto = getMemberUpdateDto();
        memberUpdateDto.setPassword(memberCreateDto.getPassword());

        //when

        //then
        mockMvc
                .perform(
                        put("/api/members/" + joinMember.getId())
                                .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
//                                .param("password", memberCreateDto.getPassword())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(memberUpdateDto))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data.id").value(joinMember.getId()))
                .andExpect(jsonPath("data.joinId").value(memberUpdateDto.getJoinId()))
                .andExpect(jsonPath("data.email").value(memberUpdateDto.getEmail()))
                .andExpect(jsonPath("data.name").value(memberUpdateDto.getName()))
                .andExpect(jsonPath("data.nickname").value(memberUpdateDto.getNickname()))
                .andExpect(jsonPath("data.dateOfBirth").value(memberUpdateDto.getDateOfBirth().toString()))
                .andExpect(jsonPath("data.gender").value(memberUpdateDto.getGender().name()))
                .andExpect(jsonPath("data.simpleAddress").value(memberUpdateDto.getSimpleAddress()))
                .andExpect(jsonPath("data.sharedVocabularyCount").value(joinMember.getSharedVocabularyCount()))
                .andExpect(jsonPath("data.bbsCount").value(joinMember.getBbsCount()))
                .andExpect(jsonPath("data.loginInfo.refreshToken").value(joinMember.getLoginInfo().getRefreshToken()))
                .andExpect(jsonPath("data.loginInfo.refreshTokenExpirationPeriodDateTime").exists())
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("data.updateDate").exists())
                .andExpect(jsonPath("message").value(MessageVo.MODIFIED_MEMBER_INFO_SUCCESSFULLY))
                .andDo(document("modify-member",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type"),
                                headerWithName(X_AUTH_TOKEN).description(X_AUTH_TOKEN_DESCRIPTION)
                        ),
                        requestFields(
                                fieldWithPath("joinId").description("수정할 로그인 ID"),
                                fieldWithPath("password").description("본인확인을 위한 비밀번호"),
                                fieldWithPath("email").description("수정할 개인 Email"),
                                fieldWithPath("name").description("수정할 이름"),
                                fieldWithPath("nickname").description("수정할 활동명"),
                                fieldWithPath("dateOfBirth").description("수정할 생년월일"),
                                fieldWithPath("gender").description("수정할 성별 [MALE, FEMALE]"),
                                fieldWithPath("simpleAddress").description("수정할 간략한 주소지")
                        ),
                        responseFields(
                                fieldWithPath("data.id").description("가입자의 식별 ID"),
                                fieldWithPath("data.joinId").description("수정된 로그인 ID"),
                                fieldWithPath("data.email").description("수정된 Email"),
                                fieldWithPath("data.name").description("수정된 이름"),
                                fieldWithPath("data.nickname").description("수정된 활동명"),
                                fieldWithPath("data.dateOfBirth").description("수정된 생년월일"),
                                fieldWithPath("data.gender").description("수정된 성별"),
                                fieldWithPath("data.simpleAddress").description("수정된 간략한 주소지"),
                                fieldWithPath("data.sharedVocabularyCount").description("해당 회원이 공유한 단어장 개수"),
                                fieldWithPath("data.bbsCount").description("해당 회원이 작성한 게시글 개수"),
                                fieldWithPath("data.loginInfo.refreshToken").description("Access Token을 재발급 받기 위한 Refresh Token"),
                                fieldWithPath("data.loginInfo.refreshTokenExpirationPeriodDateTime").description("Refresh Token 만료 날짜"),
                                fieldWithPath("data.registerDate").description("가입일시"),
                                fieldWithPath("data.updateDate").description("회원정보 수정 일시"),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;

    }

    @Test
    @DisplayName("회원 정보 수정 시 아이디가 중복된 회원이 있는 경우")
    public void modifyMember_ExistDuplicatedMemberException() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid", "test");
        Member joinMember = memberService.userJoin(memberCreateDto);
        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto tokenDto = memberService.login(loginDto);

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        Member user2 = memberService.userJoin(memberCreateDto1);

        MemberUpdateDto memberUpdateDto = getMemberUpdateDto(user2.getJoinId(), "update nickname");
        memberUpdateDto.setPassword(memberCreateDto1.getPassword());
        //when

        //then
        mockMvc
                .perform(
                        put("/api/members/" + joinMember.getId())
                                .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
//                                .param("password", memberCreateDto.getPassword())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(memberUpdateDto))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new ExistDuplicatedMemberException().getMessage()))
        ;

    }

    @Test
    @DisplayName("회원 수정 시 수정 정보가 비어 있는 경우")
    public void modifyMember_Empty() throws Exception {
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid", "test");
        Member joinMember = memberService.userJoin(memberCreateDto);
        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto tokenDto = memberService.login(loginDto);

        MemberUpdateDto memberUpdateDto = MemberUpdateDto.builder().build();
        //when

        //then
        mockMvc
                .perform(
                        put("/api/members/" + joinMember.getId())
                                .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
                                .param("password", memberCreateDto.getPassword())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(memberUpdateDto))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].code").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].field").exists())
        ;
        //then

    }

    private MemberUpdateDto getMemberUpdateDto() {
        String joinId = "updateId";
        String nickname = "updateNickname";
        return MemberUpdateDto.builder()
                .joinId(joinId)
                .email("update@email.com")
                .name("updateName")
                .nickname(nickname)
                .dateOfBirth(LocalDate.of(1996, 11, 8))
                .gender(Gender.FEMALE)
                .simpleAddress("서울 성북구")
                .build();
    }

    private MemberUpdateDto getMemberUpdateDto(String joinId, String nickname) {
        return MemberUpdateDto.builder()
                .joinId(joinId)
                .email("update@email.com")
                .name("updateName")
                .nickname(nickname)
                .dateOfBirth(LocalDate.of(1996, 11, 8))
                .gender(Gender.FEMALE)
                .simpleAddress("서울 성북구")
                .build();
    }

//    @Test
//    @DisplayName("회원 정보 수정 시 수정할 회원이 없는 경우")
//    public void modifyMember_NouFound() throws Exception {
//        //given
//        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid","test");
//        memberService.userJoin(memberCreateDto);
//
//        LoginDto loginDto = getLoginDto(memberCreateDto);
//        TokenDto tokenDto = memberService.login(loginDto);
//
//        MemberUpdateDto memberUpdateDto = getMemberUpdateDto();
//        //when
//
//        //then
//        mockMvc
//                .perform(
//                        put("/api/members/" + 1000000L)
//                                .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
//                                .param("password", memberCreateDto.getPassword())
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(objectMapper.writeValueAsString(memberUpdateDto))
//                )
//                .andDo(print())
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("message").exists())
////                .andExpect(jsonPath("_links.self.href").exists())
////                .andExpect(jsonPath("_links.index.href").exists())
//        ;
//
//    }

    @Test
    @DisplayName("인증되지 않은 사용자가 회원 정보를 수정할 경우")
    public void modify_Unauthorized() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid", "test");
        Member joinMember = memberService.userJoin(memberCreateDto);

        MemberUpdateDto memberUpdateDto = getMemberUpdateDto();

        //when

        //then
        mockMvc
                .perform(
                        put("/api/members/" + joinMember.getId())
//                                .header(X_AUTH_TOKEN,tokenDto.getAccessToken())
                                .param("password", memberCreateDto.getPassword())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(memberUpdateDto))
                )
                .andDo(print())
                .andExpect(status().isForbidden())
        ;

    }

    @Test
    @DisplayName("다른 회원의 정보를 수정할 경우")
    public void modify_DifferentMember() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid", "test");
        memberService.userJoin(memberCreateDto);

        LoginDto userLoginDto = getLoginDto(memberCreateDto);
        TokenDto userTokenDto = memberService.login(userLoginDto);

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("adminuser", "adminuser");
        Member adminMember = memberService.adminJoin(memberCreateDto1);

        MemberUpdateDto memberUpdateDto = getMemberUpdateDto();
        memberUpdateDto.setPassword(memberCreateDto.getPassword());
        //when

        //then
        mockMvc
                .perform(
                        put("/api/members/" + adminMember.getId())
                                .header(X_AUTH_TOKEN, userTokenDto.getAccessToken())
//                                .param("password", memberCreateDto.getPassword())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(memberUpdateDto))
                )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.MODIFY_DIFFERENT_MEMBER_INFO))
//                .andExpect(jsonPath("_links.self.href").exists())
//                .andExpect(jsonPath("_links.index.href").exists())
        ;

    }

    @Test
    @DisplayName("회원 수정 시 비밀번호를 잘못 입력한 경우")
    public void modifyMember_Password_Mismatch() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid", "test");
        Member userMember = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto tokenDto = memberService.login(loginDto);

        MemberUpdateDto memberUpdateDto = getMemberUpdateDto();
        memberUpdateDto.setPassword("fdasfdsafdas");

        //when

        //then
        mockMvc
                .perform(
                        put("/api/members/" + userMember.getId())
                                .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
//                                .param("password", "fdjasklfdjak")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(memberUpdateDto))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new PasswordMismatchException().getMessage()))
//                .andExpect(jsonPath("_links.self.href").exists())
//                .andExpect(jsonPath("_links.index.href").exists())
        ;

    }


    @Test
    @DisplayName("비밀번호 수정")
    public void updatePassword() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid", "test");
        Member userMember = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto userTokenDto = memberService.login(loginDto);

        PasswordUpdateDto passwordUpdateDto = PasswordUpdateDto.builder()
                .newPassword("newPassword")
                .oldPassword(memberCreateDto.getPassword())
                .build();
        //when

        //then
        mockMvc
                .perform(
                        put("/api/members/password/" + userMember.getId())
                                .header(X_AUTH_TOKEN, userTokenDto.getAccessToken())
                                .content(objectMapper.writeValueAsString(passwordUpdateDto))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value(MessageVo.CHANGED_PASSWORD_SUCCESSFULLY))
                .andDo(document("update-password",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type"),
                                headerWithName(X_AUTH_TOKEN).description("해당 리소스에 접근하기 위해 발급된 Access Token 기입")
                        ),
                        requestFields(
                                fieldWithPath("oldPassword").description("기존에 사용하던 비밀번호"),
                                fieldWithPath("newPassword").description("변경할 새로운 비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("data").description("비밀번호 변경은 별도로 data가 출력되지 않습니다."),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;

        Member findMember = memberService.getMember(userMember.getId());

        Assertions.assertNull(findMember.getLoginInfo().getRefreshToken());
        Assertions.assertNull(findMember.getLoginInfo().getRefreshTokenExpirationPeriodDateTime());

    }

    @Test
    @DisplayName("비밀번호 수정 시 수정 정보가 없는 경우")
    public void updatePassword_Empty() throws Exception {
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid", "test");
        Member userMember = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto userTokenDto = memberService.login(loginDto);

        PasswordUpdateDto passwordUpdateDto = PasswordUpdateDto.builder()
//                .newPassword("newPassword")
//                .oldPassword(memberCreateDto.getPassword())
                .build();
        //when

        //then
        mockMvc
                .perform(
                        put("/api/members/password/" + userMember.getId())
                                .header(X_AUTH_TOKEN, userTokenDto.getAccessToken())
                                .content(objectMapper.writeValueAsString(passwordUpdateDto))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].code").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].field").exists())
        ;

        //then

    }

//    @Test
//    @DisplayName("비밀번호 변경 시 변경할 회원이 없는 경우")
//    public void updatePassword_NotFound() throws Exception {
//        //given
//        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid","test");
//        memberService.userJoin(memberCreateDto);
//
//        LoginDto loginDto = getLoginDto(memberCreateDto);
//        TokenDto tokenDto = memberService.login(loginDto);
//
//        String newPassword = "newPassword";
//        PasswordUpdateDto passwordUpdateDto = PasswordUpdateDto.builder()
//                .newPassword(newPassword)
//                .oldPassword(memberCreateDto.getPassword())
//                .build();
//
//        //when
//
//        //then
//        mockMvc
//                .perform(
//                        put("/api/members/password/" + 1000L)
//                                .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(objectMapper.writeValueAsString(passwordUpdateDto))
//                )
//                .andDo(print())
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("message").value(new MemberNotFoundException().getMessage()))
////                .andExpect(jsonPath("_links.self.href").exists())
////                .andExpect(jsonPath("_links.index.href").exists())
//        ;
//
//    }

    @Test
    @DisplayName("인증되지 않은 사용자가 비밀번호를 수정하는 경우")
    public void updatePassword_Unauthorized() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        PasswordUpdateDto passwordUpdateDto = PasswordUpdateDto.builder()
                .newPassword("newPassword")
                .oldPassword(memberCreateDto.getPassword())
                .build();

        //when

        //then
        mockMvc
                .perform(
                        put("/api/members/password/" + userMember.getId())
//                                .header(X_AUTH_TOKEN, userTokenDto.getAccessToken())
                                .content(objectMapper.writeValueAsString(passwordUpdateDto))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isForbidden())
        ;


    }

    @Test
    @DisplayName("다른 회원의 비밀번호를 수정하는 경우")
    public void update_DifferentMember_Password() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto userTokenDto = memberService.login(loginDto);

        memberCreateDto.setJoinId("different");
        memberCreateDto.setNickname("testusernickname");
        memberCreateDto.setPassword("di");
        Member userMember2 = memberService.userJoin(memberCreateDto);


        PasswordUpdateDto passwordUpdateDto = PasswordUpdateDto.builder()
                .newPassword("newPassword")
                .oldPassword(memberCreateDto.getPassword())
                .build();

        //when

        //then
        mockMvc
                .perform(
                        put("/api/members/password/" + userMember2.getId())
                                .header(X_AUTH_TOKEN, userTokenDto.getAccessToken())
                                .content(objectMapper.writeValueAsString(passwordUpdateDto))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.MODIFY_DIFFERENT_MEMBER_INFO))
//                .andExpect(jsonPath("_links.self.href").exists())
//                .andExpect(jsonPath("_links.index.href").exists())
        ;


    }

    @Test
    @DisplayName("비밀번호 수정 시 oldPassword 를 틀린 경우")
    public void updatePassword_Password_Mismatch() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto userTokenDto = memberService.login(loginDto);

        PasswordUpdateDto passwordUpdateDto = PasswordUpdateDto.builder()
                .newPassword("newPassword")
                .oldPassword("fdasfdasfas")
                .build();

        //when

        //then
        mockMvc
                .perform(
                        put("/api/members/password/" + userMember.getId())
                                .header(X_AUTH_TOKEN, userTokenDto.getAccessToken())
                                .content(objectMapper.writeValueAsString(passwordUpdateDto))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new PasswordMismatchException().getMessage()))
//                .andExpect(jsonPath("_links.self.href").exists())
//                .andExpect(jsonPath("_links.index.href").exists())
        ;


    }

    @Test
    @DisplayName("회원 탈퇴")
    public void secession() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto tokenDto = memberService.login(loginDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        delete("/api/members/secession/" + userMember.getId())
                                .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
                                .param("password", memberCreateDto.getPassword())
                );

        //then
        perform
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value(MessageVo.SECESSION_SUCCESSFULLY))
                .andDo(document("secession",
                        requestHeaders(
                                headerWithName(X_AUTH_TOKEN).description(X_AUTH_TOKEN_DESCRIPTION)
                        ),
                        requestParameters(
                                parameterWithName("password").description("회원 탈퇴 시 본인 확인을 위한 비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("data").description("회원 탈퇴는 별도의 data가 출력되지 않습니다."),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;

        Member findMember = memberService.getMember(userMember.getId());

        assertTrue(findMember.getRoles().contains(MemberRole.SECESSION));
        assertFalse(findMember.getRoles().contains(MemberRole.USER));
        assertFalse(findMember.getRoles().contains(MemberRole.ADMIN));
        assertFalse(findMember.getRoles().contains(MemberRole.BAN));

    }

    @Test
    @DisplayName("회원 탈퇴 시 비밀번호를 틀린 경우")
    public void secession_PasswordMismatch() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto tokenDto = memberService.login(loginDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        delete("/api/members/secession/" + userMember.getId())
                                .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
                                .param("password", "ㄹㅇㅁㄴㄹㅇㄴㅁ")
                );

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new PasswordMismatchException().getMessage()));

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 회원 탈퇴를 하는 경우")
    public void secession_Unauthorized() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        memberService.login(loginDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        delete("/api/members/secession/" + userMember.getId())
//                                .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
                                .param("password", memberCreateDto.getPassword())
                );

        //then
        perform
                .andDo(print())
                .andExpect(status().isForbidden())
        ;

    }

    @Test
    @DisplayName("다른 회원을 탈퇴하려고 하는 경우")
    public void secession_DifferentMember() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto userMember1TokenDto = memberService.login(loginDto);

        memberCreateDto.setJoinId("fdasfdjsaiojdioa");
        memberCreateDto.setNickname("testusernickname");
        memberCreateDto.setPassword("fdafdafdasf");
        Member userMember2 = memberService.userJoin(memberCreateDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        delete("/api/members/secession/" + userMember2.getId())
                                .header(X_AUTH_TOKEN, userMember1TokenDto.getAccessToken())
                                .param("password", memberCreateDto.getPassword())
                );

        //then
        perform
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.MODIFY_DIFFERENT_MEMBER_INFO))
//                .andExpect(jsonPath("_links.self.href").exists())
//                .andExpect(jsonPath("_links.index.href").value(linkToIndex().toUri().toString()))
        ;

    }

//    @Test
//    @DisplayName("탈퇴하려는 회원이 없는 경우")
//    public void secession_NotFound() throws Exception {
//        //given
//        MemberCreateDto memberCreateDto = getMemberCreateDto();
//        memberService.userJoin(memberCreateDto);
//
//        LoginDto loginDto = getLoginDto(memberCreateDto);
//        TokenDto tokenDto = memberService.login(loginDto);
//
//        //when
//        ResultActions perform = mockMvc
//                .perform(
//                        put("/api/members/secession/" + 10000L)
//                                .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
//                );
//
//        //then
//        perform
//                .andDo(print())
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("message").value(new MemberNotFoundException().getMessage()))
////                .andExpect(jsonPath("_links.self.href").exists())
////                .andExpect(jsonPath("_links.index.href").value(linkToIndex().toUri().toString()))
//        ;
//    }

    @Test
    @DisplayName("관리자가 회원 목록 조회")
    public void getMemberList() throws Exception {
        //given
        createMemberList();

        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member adminMember = memberService.adminJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto adminTokenDto = memberService.login(loginDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/members")
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                                .param("criteriaDto.pageNum", "12")
                                .param("criteriaDto.limit", "4")
                                .param("searchType", MemberSearchType.JOIN_ID.name())
                                .param("keyword", "joinId7")
                                .param("sortType", MemberSortType.OLDEST.name())
                );
        //then
        perform
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.list[0].id").exists())
                .andExpect(jsonPath("data.list[0].joinId").exists())
                .andExpect(jsonPath("data.list[0].email").exists())
                .andExpect(jsonPath("data.list[0].name").exists())
                .andExpect(jsonPath("data.list[0].nickname").exists())
                .andExpect(jsonPath("data.list[0].dateOfBirth").exists())
                .andExpect(jsonPath("data.list[0].gender").exists())
                .andExpect(jsonPath("data.list[0].simpleAddress").exists())
                .andExpect(jsonPath("data.list[0].sharedVocabularyCount").exists())
                .andExpect(jsonPath("data.list[0].bbsCount").exists())
                .andExpect(jsonPath("data.list[0].roles").exists())
                .andExpect(jsonPath("data.list[0].registerDate").exists())
                .andExpect(jsonPath("data.list[0].updateDate").exists())
                .andExpect(jsonPath("data.paging.totalCount").exists())
                .andExpect(jsonPath("data.paging.criteriaDto.pageNum").value(12))
                .andExpect(jsonPath("data.paging.criteriaDto.limit").value(4))
                .andExpect(jsonPath("data.paging.startPage").value(11))
                .andExpect(jsonPath("data.paging.endPage").value(20))
                .andExpect(jsonPath("data.paging.prev").value(true))
                .andExpect(jsonPath("data.paging.next").value(true))
                .andExpect(jsonPath("data.paging.totalPage").exists())
                .andExpect(jsonPath("message").value(MessageVo.GET_MEMBER_LIST_BY_ADMIN_SUCCESSFULLY))
                .andDo(document("get-member-list",
                        requestHeaders(
                                headerWithName(X_AUTH_TOKEN).description(X_AUTH_TOKEN_DESCRIPTION)
                        ),
                        requestParameters(
                                parameterWithName("criteriaDto.pageNum").description("조회할 Page 번호"),
                                parameterWithName("criteriaDto.limit").description("조회할 개수"),
                                parameterWithName("searchType").description("검색 조건 : 회원 ID 로 검색, 회원 Email 로 검색, 회원 이름으로 검색, 회원 Nickname 으로 검색 " +
                                        " [JOIN_ID, EMAIL, NAME, NICKNAME]"),
                                parameterWithName("keyword").description("검색 키워드 (검색 시 검색 조건에 해당 키워드를 포함하는 결과들을 반환해 줌.)"),
                                parameterWithName("sortType").description("정렬 조건 : 최신 순, 오래된 순, 게시글 많이 작성한 순, 게시글 조금 작성한 순, 공유한 단어장이 많은 순, 공유한 단어장이 적은 순  " +
                                        " [LATEST, OLDEST, BBS_COUNT_DESC, BBS_COUNT_ASC, SHARED_VOCABULARY_COUNT_DESC, SHARED_VOCABULARY_COUNT_ASC]")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("data.list[0].id").description("회원 목록 중 첫 번째 회원의 식별 ID"),
                                fieldWithPath("data.list[0].joinId").description("회원 목록 중 첫 번째 회원의 로그인 ID"),
                                fieldWithPath("data.list[0].email").description("회원 목록 중 첫 번째 회원의 개인 Email"),
                                fieldWithPath("data.list[0].name").description("회원 목록 중 첫 번째 회원의 이름"),
                                fieldWithPath("data.list[0].nickname").description("회원 목록 중 첫 번째 회원의 활동명"),
                                fieldWithPath("data.list[0].dateOfBirth").description("회원 목록 중 첫 번째 회원의 생년월일"),
                                fieldWithPath("data.list[0].gender").description("회원 목록 중 첫 번째 회원의 성별"),
                                fieldWithPath("data.list[0].simpleAddress").description("회원 목록 중 첫 번째 회원의 간략한 주소지"),
                                fieldWithPath("data.list[0].sharedVocabularyCount").description("회원 목록 중 첫 번째 회원이 공유한 단어장 개수"),
                                fieldWithPath("data.list[0].bbsCount").description("회원 목록 중 첫 번째 회원이 작성한 게시글 개수"),
                                fieldWithPath("data.list[0].roles").description("회원 목록 중 첫 번째 회원의 권한"),
                                fieldWithPath("data.list[0].registerDate").description("회원 목록 중 첫 번째 회원의 가입 일시"),
                                fieldWithPath("data.list[0].updateDate").description("회원 목록 중 첫 번째 회원의 개인 정보 수정 일시"),
                                fieldWithPath("data.paging.totalCount").description("요청 시 입력된 조건에 따라 조회되는 회원의 총 인원 수"),
                                fieldWithPath("data.paging.criteriaDto.pageNum").description("조회된 Page"),
                                fieldWithPath("data.paging.criteriaDto.limit").description("조회된 인원 수"),
                                fieldWithPath("data.paging.startPage").description("요청의 pageNum 에 따른 시작 페이지 (12Page -> 11Page 가 startPage)"),
                                fieldWithPath("data.paging.endPage").description("요청의 pageNum 과 조회 결과에 따라 변동되는 마지막 페이지 " +
                                        " (조회된 회원이 충분히 많을 경우 12Page -> 20Page 가 endPage)"),
                                fieldWithPath("data.paging.prev").description("이전 페이지 목록을 조회 할 수 있는지 여부 " +
                                        " (1 Page 를 요청하게 되면 1 Page 보다 이전의 Page 는 없으므로 prev = false, 12 Page 를 요청하게 될 경우 1~10 Page 는 요청이 가능하므로 prev = true)"),
                                fieldWithPath("data.paging.next").description("다음 페이지 목록을 조회 할 수 있는지 여부 " +
                                        " (endPage 가 30일 경우 25 Page 를 요청하게 되면 30 이후의 31~40 Page 는 요청할 수 없으므로 next = false, " +
                                        " 같은 경우 15 Page 를 요청하게 되면 21~30 Page 에 대한 요청도 가능하므로 next = true)"),
                                fieldWithPath("data.paging.totalPage").description("요청의 검색 조건에 의해 조회되는 총 페이지 수"),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 회원 목록을 조회하는 경우")
    public void getMemberList_UnAuthentication() throws Exception {
        //given
        createMemberList();

//        MemberCreateDto memberCreateDto = getMemberCreateDto();
//        Member adminMember = memberService.adminJoin(memberCreateDto);
//
//        LoginDto loginDto = getLoginDto(memberCreateDto);
//        TokenDto adminTokenDto = memberService.login(loginDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/members")
//                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                                .param("criteriaDto.pageNum", "12")
                                .param("criteriaDto.limit", "4")
                                .param("searchType", MemberSearchType.JOIN_ID.name())
                                .param("keyword", "joinId7")
                                .param("sortType", MemberSortType.OLDEST.name())
                );
        //then
        perform
                .andDo(print())
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("관리자가 아닌 사용자가 회원 목록을 조회하는 경우")
    public void getMemberList_Unauthorized() throws Exception {
        //given
        createMemberList();

        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member adminMember = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto adminTokenDto = memberService.login(loginDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/members")
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                                .param("criteriaDto.pageNum", "12")
                                .param("criteriaDto.limit", "4")
                                .param("searchType", MemberSearchType.JOIN_ID.name())
                                .param("keyword", "joinId7")
                                .param("sortType", MemberSortType.OLDEST.name())
                );
        //then
        perform
                .andDo(print())
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("검색 조건이 없는데 키워드가 있는 경우")
    public void getMemberList_Wrong() throws Exception {
        //given
        createMemberList();

        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member adminMember = memberService.adminJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto adminTokenDto = memberService.login(loginDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/members")
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                                .param("criteriaDto.pageNum", "12")
                                .param("criteriaDto.limit", "4")
//                                .param("searchType", MemberSearchType.JOIN_ID.name())
                                .param("keyword", "joinId7")
                                .param("sortType", MemberSortType.OLDEST.name())
                );
        //then
        perform
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].code").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
        ;

    }

    @ParameterizedTest(name = "test {index} : {2}")
    @MethodSource("paramsForGetMemberListTest")
    @DisplayName("회원 목록 조회 시 페이징에 대한 정보를 잘 못 준 경우")
    public void getMemberList_Wrong2(int pageNum, int limit, String message) throws Exception {
        //given
        createMemberList();

        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member adminMember = memberService.adminJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto adminTokenDto = memberService.login(loginDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/members")
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                                .param("criteriaDto.pageNum", "" + pageNum)
                                .param("criteriaDto.limit", "" + limit)
                                .param("searchType", MemberSearchType.JOIN_ID.name())
                                .param("keyword", "joinId7")
                                .param("sortType", MemberSortType.OLDEST.name())
                );
        //then
        perform
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].code").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].field").exists())
                .andExpect(jsonPath("$[0].rejectedValue").exists())
        ;

    }

    private static Stream<Arguments> paramsForGetMemberListTest() {
        return Stream.of(
                Arguments.of(-1, 10, "음수의 페이지 번호를 인가한 경우 badRequest"),
                Arguments.of(0, 10, "0의 페이지 번호를 인가한 경우 badRequest"),
                Arguments.of(1, -1, "음의 개수의 결과를 조회한 경우 badRequest"),
                Arguments.of(1, 0, "0개의 결과를 조회한 경우 badRequest"),
                Arguments.of(1, 101, "100개가 넘는 개수의 결과를 조회한 경우 badRequest")
        );
    }

    @Test
    @DisplayName("관리자가 회원의 활동을 정지")
    public void ban() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member adminMember = memberService.adminJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto adminTokenDto = memberService.login(loginDto);

        memberCreateDto.setJoinId("testUser");
        memberCreateDto.setNickname("testusernickname");
        memberCreateDto.setPassword("testPassword");
        Member userMember = memberService.userJoin(memberCreateDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/members/ban/" + userMember.getId())
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.joinId").exists())
                .andExpect(jsonPath("data.email").exists())
                .andExpect(jsonPath("data.name").exists())
                .andExpect(jsonPath("data.nickname").exists())
                .andExpect(jsonPath("data.dateOfBirth").exists())
                .andExpect(jsonPath("data.gender").exists())
                .andExpect(jsonPath("data.simpleAddress").exists())
                .andExpect(jsonPath("data.sharedVocabularyCount").exists())
                .andExpect(jsonPath("data.bbsCount").exists())
                .andExpect(jsonPath("data.roles").exists())
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("data.updateDate").exists())
                .andExpect(jsonPath("message").value(MessageVo.BAN_SUCCESSFULLY))
                .andDo(document("ban",
                        requestHeaders(
                                headerWithName(X_AUTH_TOKEN).description(X_AUTH_TOKEN_DESCRIPTION)
                        ),
                        responseFields(
                                fieldWithPath("data.id").description("활동이 금지된 회원의 식별 ID"),
                                fieldWithPath("data.joinId").description("활동이 금지된 회원의 로그인 ID"),
                                fieldWithPath("data.email").description("활동이 금지된 회원의 개인 ID"),
                                fieldWithPath("data.name").description("활동이 금지된 회원의 이름"),
                                fieldWithPath("data.nickname").description("활동이 금지된 회원의 활동명"),
                                fieldWithPath("data.dateOfBirth").description("활동이 금지된 회원의 생년월일"),
                                fieldWithPath("data.gender").description("활동이 금지된 회원의 성별"),
                                fieldWithPath("data.simpleAddress").description("활동이 금지된 회원의 간략한 주소지"),
                                fieldWithPath("data.sharedVocabularyCount").description("활동이 금지된 회원이 공유한 단어장 개수"),
                                fieldWithPath("data.bbsCount").description("활동이 금지된 회원이 작성한 게시글 수"),
                                fieldWithPath("data.roles").description("활동이 금지된 회원의 권한"),
                                fieldWithPath("data.registerDate").description("활동이 금지된 회원의 가입 일시"),
                                fieldWithPath("data.updateDate").description("활동이 금지된 회원의 회원 정보 수정 일시"),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;

        Member findMember = memberService.getMember(userMember.getId());
        assertTrue(findMember.getRoles().contains(MemberRole.BAN));
        assertFalse(findMember.getRoles().contains(MemberRole.USER));
        assertFalse(findMember.getRoles().contains(MemberRole.ADMIN));
        assertFalse(findMember.getRoles().contains(MemberRole.SECESSION));

    }

    @Test
    @DisplayName("회원이 회원의 활동을 정지 시키는 경우")
    public void ban_Unauthorized() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember1 = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto user1TokenDto = memberService.login(loginDto);

        memberCreateDto.setJoinId("testUser");
        memberCreateDto.setNickname("testusernickname");
        memberCreateDto.setPassword("testPassword");
        Member userMember2 = memberService.userJoin(memberCreateDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/members/ban/" + userMember2.getId())
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("인증되지 않는 사용자가 회원의 활동을 정지 시키는 경우")
    public void ban_UnAuthentication() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member adminMember = memberService.adminJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto adminTokenDto = memberService.login(loginDto);

        memberCreateDto.setJoinId("testUser");
        memberCreateDto.setNickname("testusernickname");
        memberCreateDto.setPassword("testPassword");
        Member userMember = memberService.userJoin(memberCreateDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/members/ban/" + userMember.getId())
//                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("활동 정지 시킬 회원이 없는 경우")
    public void ban_NotFound() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member adminMember = memberService.adminJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto adminTokenDto = memberService.login(loginDto);

        memberCreateDto.setJoinId("testUser");
        memberCreateDto.setNickname("testUser");
        memberCreateDto.setPassword("testPassword");
        Member userMember = memberService.userJoin(memberCreateDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/members/ban/" + 10000L)
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new MemberNotFoundException().getMessage()))
//                .andExpect(jsonPath("_links.self.href").value(MEMBER_API_CONTROLLER_REQUEST_VALUE + "/ban/" + 10000L))
//                .andExpect(jsonPath("_links.index.href").value(linkToIndex().toUri().toString()))
        ;

    }

    @Test
    @DisplayName("탈퇴한 회원의 권한을 USER로 변환")
    public void changeMemberRoleToUser_Of_SecessionMember() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member adminMember = memberService.adminJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto adminTokenDto = memberService.login(loginDto);

        memberCreateDto.setJoinId("testUserJOinId");
        memberCreateDto.setNickname("testusernickname");
        memberCreateDto.setPassword("testUserPassword");
        Member userMember = memberService.userJoin(memberCreateDto);

        memberService.secession(userMember.getId(), memberCreateDto.getPassword());

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/members/restoration/" + userMember.getId())
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.joinId").exists())
                .andExpect(jsonPath("data.email").exists())
                .andExpect(jsonPath("data.name").exists())
                .andExpect(jsonPath("data.nickname").exists())
                .andExpect(jsonPath("data.dateOfBirth").exists())
                .andExpect(jsonPath("data.gender").exists())
                .andExpect(jsonPath("data.simpleAddress").exists())
                .andExpect(jsonPath("data.sharedVocabularyCount").exists())
                .andExpect(jsonPath("data.bbsCount").exists())
                .andExpect(jsonPath("data.roles").exists())
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("data.updateDate").exists())
                .andExpect(jsonPath("message").value(MessageVo.CHANGE_MEMBER_ROLE_TO_USER_SUCCESSFULLY))
                .andDo(document("restoration",
                        requestHeaders(
                                headerWithName(X_AUTH_TOKEN).description(X_AUTH_TOKEN_DESCRIPTION)
                        ),
                        responseFields(
                                fieldWithPath("data.id").description("권한이 복구된 회원의 식별 ID"),
                                fieldWithPath("data.joinId").description("권한이 복구된 회원의 로그인 ID"),
                                fieldWithPath("data.email").description("권한이 복구된 회원의 개인 Email"),
                                fieldWithPath("data.name").description("권한이 복구된 회원의 이름"),
                                fieldWithPath("data.nickname").description("권한이 복구된 회원의 활동명"),
                                fieldWithPath("data.dateOfBirth").description("권한이 복구된 회원의 생년월일"),
                                fieldWithPath("data.gender").description("권한이 복구된 회원의 성별"),
                                fieldWithPath("data.simpleAddress").description("권한이 복구된 회원의 간략한 주소지"),
                                fieldWithPath("data.sharedVocabularyCount").description("권한이 복구된 회원이 공유했던 단어장 개수"),
                                fieldWithPath("data.bbsCount").description("권한이 복구된 회원이 작성했던 게시글 개수"),
                                fieldWithPath("data.roles").description("권한이 복구된 회원의 권한"),
                                fieldWithPath("data.registerDate").description("권한이 복구된 회원의 가입 일시"),
                                fieldWithPath("data.updateDate").description("권한이 복구된 회원의 수정 일시"),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;

    }

    @Test
    @DisplayName("활동이 금지된 회원의 권한을 USER로 변환")
    public void changeMemberRoleToUser_Of_BanedMember() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member adminMember = memberService.adminJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto adminTokenDto = memberService.login(loginDto);

        memberCreateDto.setJoinId("testUserJOinId");
        memberCreateDto.setNickname("testusernickname");
        memberCreateDto.setPassword("testUserPassword");
        Member userMember = memberService.userJoin(memberCreateDto);

        memberService.ban(userMember.getId());

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/members/restoration/" + userMember.getId())
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.joinId").exists())
                .andExpect(jsonPath("data.email").exists())
                .andExpect(jsonPath("data.name").exists())
                .andExpect(jsonPath("data.nickname").exists())
                .andExpect(jsonPath("data.dateOfBirth").exists())
                .andExpect(jsonPath("data.gender").exists())
                .andExpect(jsonPath("data.simpleAddress").exists())
                .andExpect(jsonPath("data.sharedVocabularyCount").exists())
                .andExpect(jsonPath("data.bbsCount").exists())
                .andExpect(jsonPath("data.roles").exists())
                .andExpect(jsonPath("data.registerDate").exists())
                .andExpect(jsonPath("data.updateDate").exists())
                .andExpect(jsonPath("message").value(MessageVo.CHANGE_MEMBER_ROLE_TO_USER_SUCCESSFULLY))
//                .andExpect(jsonPath("_links.self.href").value(MEMBER_API_CONTROLLER_REQUEST_VALUE + "/restoration/" + userMember.getId()))
//                .andExpect(jsonPath("_links.get-member.href").value(linkToGetMember(userMember.getId()).toUri().toString()))
//                .andExpect(jsonPath("_links.index.href").value(linkToIndex().toUri().toString()))
        ;

    }

    @Test
    @DisplayName("회원이 회원의 권한을 USER로 변화 시키는 경우")
    public void changeMemberRoleToUser_Unauthorized() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember1 = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto userMember1TokenDto = memberService.login(loginDto);

        memberCreateDto.setJoinId("testUserJOinId");
        memberCreateDto.setNickname("testusernickname");
        memberCreateDto.setPassword("testUserPassword");
        Member userMember = memberService.userJoin(memberCreateDto);

        memberService.ban(userMember.getId());

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/members/restoration/" + userMember.getId())
                                .header(X_AUTH_TOKEN, userMember1TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 회원의 권한을 USER로 변환 시키는 경우")
    public void changeMemberRoleToUser_UnAuthentication() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member adminMember = memberService.adminJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto adminTokenDto = memberService.login(loginDto);

        memberCreateDto.setJoinId("testUserJOinId");
        memberCreateDto.setNickname("testNickname2");
        memberCreateDto.setPassword("testUserPassword");
        Member userMember = memberService.userJoin(memberCreateDto);

        memberService.ban(userMember.getId());

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/members/restoration/" + userMember.getId())
//                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("관리자의 권한을 USER로 변화 시키는 경우")
    public void changeMemberRoleToUser_BadRequest1() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member adminMember = memberService.adminJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto adminTokenDto = memberService.login(loginDto);

        memberCreateDto.setJoinId("testUserJOinId");
        memberCreateDto.setNickname("testusernickname");
        memberCreateDto.setPassword("testUserPassword");
        Member adminMember2 = memberService.adminJoin(memberCreateDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/members/restoration/" + adminMember2.getId())
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new MemberAlreadyHasAuthorityException().getMessage()))
//                .andExpect(jsonPath("_links.self.href").value(MEMBER_API_CONTROLLER_REQUEST_VALUE + "/restoration/" + adminMember2.getId()))
//                .andExpect(jsonPath("_links.index.href").value(linkToIndex().toUri().toString()))
        ;
    }

    @Test
    @DisplayName("이미 USER 권한이 있는 사용자를 USER로 변화 시키는 경우")
    public void changeMemberRoleToUser_BadRequest2() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member adminMember = memberService.adminJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto adminTokenDto = memberService.login(loginDto);

        memberCreateDto.setJoinId("testUserJOinId");
        memberCreateDto.setNickname("testusernickname");
        memberCreateDto.setPassword("testUserPassword");
        Member userMember = memberService.userJoin(memberCreateDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/members/restoration/" + userMember.getId())
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new MemberAlreadyHasAuthorityException().getMessage()))
//                .andExpect(jsonPath("_links.self.href").value(MEMBER_API_CONTROLLER_REQUEST_VALUE + "/restoration/" + userMember.getId()))
//                .andExpect(jsonPath("_links.index.href").value(linkToIndex().toUri().toString()))
        ;
    }

    private MemberCreateDto getMemberCreateDto() {
        return getMemberCreateDto("testJoinid", "test");
    }
}