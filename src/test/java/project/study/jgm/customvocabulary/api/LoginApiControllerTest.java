package project.study.jgm.customvocabulary.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import project.study.jgm.customvocabulary.common.BaseControllerTest;
import project.study.jgm.customvocabulary.common.dto.MessageVo;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.dto.MemberCreateDto;
import project.study.jgm.customvocabulary.members.exception.RefreshTokenExpirationException;
import project.study.jgm.customvocabulary.members.exception.RefreshTokenNotFoundException;
import project.study.jgm.customvocabulary.security.dto.LoginDto;
import project.study.jgm.customvocabulary.security.dto.OnlyTokenDto;
import project.study.jgm.customvocabulary.security.dto.TokenDto;
import project.study.jgm.customvocabulary.security.exception.PasswordMismatchException;

import java.time.LocalDateTime;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoginApiControllerTest extends BaseControllerTest {

    @BeforeEach
    public void setUp() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("로그인")
    public void login() throws Exception {
        //given
        String joinId = "userHong";
        String nickname = "userHong";
        MemberCreateDto memberCreateDto = getMemberCreateDto(joinId, nickname);
        Member userMember = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.accessToken").exists())
                .andExpect(jsonPath("data.accessTokenExpirationSecond").value(securityProperties.getTokenValidSecond()))
                .andExpect(jsonPath("data.refreshToken").exists())
                .andExpect(jsonPath("data.refreshTokenExpirationPeriodDateTime").exists())
                .andDo(document("authentication-login",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                        ),
                        requestFields(
                                fieldWithPath("joinId").description("회원 가입 시 입력한 로그인 ID"),
                                fieldWithPath("password").description("회원 가입 시 입력한 로그인 비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("data.accessToken").description("리소스에 접근하기 위한 Access Token"),
                                fieldWithPath("data.accessTokenExpirationSecond").description("Access Token 유효 시간 [sec]"),
                                fieldWithPath("data.refreshToken").description("Access Token 을 재발급하기 위한 Refresh Token"),
                                fieldWithPath("data.refreshTokenExpirationPeriodDateTime").description("Refresh Token 만료 날짜"),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;

    }

    @Test
    @DisplayName("로그인 시 로그인 아이디가 틀린 경우")
    public void login_Wrong_Join_Id() throws Exception {
        //given
        String joinId = "testJoinid";
        String nickname = "test";
        MemberCreateDto memberCreateDto = getMemberCreateDto(joinId,nickname);
        Member userMember = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        String joinId2 = "fdasfdsajflkdasjlk";
        loginDto.setJoinId(joinId2);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new UsernameNotFoundException("해당 아이디의 사용자가 없습니다. ID : "+joinId2).getMessage()));

    }

    @Test
    @DisplayName("로그인 시 로그인 비밀번호가 틀린경우")
    public void login_PasswordMismatch() throws Exception {
        //given
        //given
        String joinId = "testJoinid";
        String nickname = "test";
        MemberCreateDto memberCreateDto = getMemberCreateDto(joinId, nickname);
        Member userMember = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        loginDto.setPassword("fdjaklfjdkafjdasl");

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginDto))
                )
                .andDo(print());
        //when

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new PasswordMismatchException().getMessage()));

    }

    @Test
    @DisplayName("refresh token 으로 로그인")
    public void refresh() throws Exception {
        //given
        String joinId = "testJoinid";
        String nickname = "test";
        MemberCreateDto memberCreateDto = getMemberCreateDto(joinId,nickname);
        Member userMember = memberService.userJoin(memberCreateDto);

        String refreshToken = userMember.getLoginInfo().getRefreshToken();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(refreshToken);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(onlyTokenDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.accessToken").exists())
                .andExpect(jsonPath("data.accessTokenExpirationSecond").value(securityProperties.getTokenValidSecond()))
                .andExpect(jsonPath("data.refreshToken").exists())
                .andExpect(jsonPath("data.refreshTokenExpirationPeriodDateTime").exists())
                .andDo(document("authentication-refresh",
                        requestFields(
                                fieldWithPath("refreshToken").description("로그인 시 발급해주는 Refresh Token 을 기입")
                        ),
                        responseFields(
                                fieldWithPath("data.accessToken").description("재발급된 Access Token"),
                                fieldWithPath("data.accessTokenExpirationSecond").description("재발급된 Access Token 유효 시간"),
                                fieldWithPath("data.refreshToken").description("사용한 Refresh Token"),
                                fieldWithPath("data.refreshTokenExpirationPeriodDateTime").description("사용한 Refresh Token 의 만료 날짜"),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;

    }

    @Test
    @DisplayName("refresh token 이 틀린 경우")
    public void refresh_Wrong() throws Exception {
        //given
        String joinId = "testJoinid";
        String nickname = "test";
        MemberCreateDto memberCreateDto = getMemberCreateDto(joinId, nickname);
        Member userMember = memberService.userJoin(memberCreateDto);

        String refreshToken = userMember.getLoginInfo().getRefreshToken();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto("fdafdasfdasfdsafdsa");

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(onlyTokenDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(new RefreshTokenNotFoundException().getMessage()));
    }

    @Test
    @DisplayName("refresh token 기간이 만료된 경우")
    @Transactional
    public void refreshTokenPeriodExpired() throws Exception {
        //given
        String joinId = "testJoinid";
        String nickname = "test";
        MemberCreateDto memberCreateDto = getMemberCreateDto(joinId, nickname);
        Member userMember = memberService.userJoin(memberCreateDto);

        String refreshToken = userMember.getLoginInfo().getRefreshToken();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(refreshToken);

        userMember.getLoginInfo().setRefreshTokenExpirationPeriodDateTime(LocalDateTime.now().minusDays(1));

        em.flush();
        em.clear();

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(onlyTokenDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(new RefreshTokenExpirationException().getMessage()));

    }

    @Test
    @DisplayName("로그아웃")
    public void logout() throws Exception {
        //given
        String joinId = "testJoinid";
        String nickname = "test";
        MemberCreateDto memberCreateDto = getMemberCreateDto(joinId, nickname);
        Member userMember = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto tokenDto = memberService.login(loginDto);

        //when

        //then
        mockMvc
                .perform(
                        get("/api/logout")
                                .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value(MessageVo.LOGOUT_SUCCESSFULLY))
                .andDo(document("authentication-logout",
                        requestHeaders(
                                headerWithName(X_AUTH_TOKEN).description(X_AUTH_TOKEN_DESCRIPTION)
                        ),
                        responseFields(
                                fieldWithPath("data").description("로그아웃은 별도의 data 를 출력하지 않습니다."),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;

    }
}