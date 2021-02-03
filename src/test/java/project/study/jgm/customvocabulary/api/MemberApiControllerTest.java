package project.study.jgm.customvocabulary.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import project.study.jgm.customvocabulary.common.BaseControllerTest;
import project.study.jgm.customvocabulary.common.LinkToVo;
import project.study.jgm.customvocabulary.common.MessageDto;
import project.study.jgm.customvocabulary.members.Gender;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRole;
import project.study.jgm.customvocabulary.members.dto.MemberCreateDto;
import project.study.jgm.customvocabulary.members.dto.MemberUpdateDto;
import project.study.jgm.customvocabulary.members.dto.PasswordUpdateDto;
import project.study.jgm.customvocabulary.members.exception.MemberNotFoundException;
import project.study.jgm.customvocabulary.security.dto.LoginDto;
import project.study.jgm.customvocabulary.security.dto.TokenDto;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MemberApiControllerTest extends BaseControllerTest {

    final String X_AUTH_TOKEN = "X-AUTH-TOKEN";

    @BeforeEach
    public void setup() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입")
    public void join() throws Exception {
        //given
        MemberCreateDto createDto = getMemberCreateDto();

        //when
        mockMvc.perform(
                post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)
                        ))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("joinId").value(createDto.getJoinId()))
                .andExpect(jsonPath("email").value(createDto.getEmail()))
                .andExpect(jsonPath("name").value(createDto.getName()))
                .andExpect(jsonPath("nickname").value(createDto.getNickname()))
                .andExpect(jsonPath("dateOfBirth").value(createDto.getDateOfBirth().toString()))
                .andExpect(jsonPath("gender").value(createDto.getGender().name()))
                .andExpect(jsonPath("simpleAddress").value(createDto.getSimpleAddress()))
                .andExpect(jsonPath("sharedVocabularyCount").value(0))
                .andExpect(jsonPath("bbsCount").value(0))
                .andExpect(jsonPath("registerDate").exists())
                .andExpect(jsonPath("updateDate").exists())
                .andExpect(jsonPath("loginInfo.refreshToken").exists())
                .andExpect(jsonPath("loginInfo.refreshTokenExpirationPeriodDateTime").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.login.href").exists())
        ;

        //then

    }

    private MemberCreateDto getMemberCreateDto() {
        return MemberCreateDto.builder()
                .joinId("testJoinid")
                .email("test@email.com")
                .password("test")
                .name("정구민")
                .nickname("test")
                .dateOfBirth(LocalDate.now())
                .gender(Gender.MALE)
                .simpleAddress("address")
                .build();
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
    @DisplayName("회원조회")
    public void getMember() throws Exception {
        //given
        MemberCreateDto createDto = getMemberCreateDto();
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
                .andExpect(jsonPath("id").value(member.getId()))
                .andExpect(jsonPath("joinId").value(createDto.getJoinId()))
                .andExpect(jsonPath("email").value(createDto.getEmail()))
                .andExpect(jsonPath("name").value(createDto.getName()))
                .andExpect(jsonPath("nickname").value(createDto.getNickname()))
                .andExpect(jsonPath("dateOfBirth").value(createDto.getDateOfBirth().toString()))
                .andExpect(jsonPath("gender").value(createDto.getGender().name()))
                .andExpect(jsonPath("simpleAddress").value(createDto.getSimpleAddress()))
                .andExpect(jsonPath("sharedVocabularyCount").value(0))
                .andExpect(jsonPath("bbsCount").value(0))
                .andExpect(jsonPath("registerDate").exists())
                .andExpect(jsonPath("updateDate").exists())
                .andExpect(jsonPath("loginInfo.refreshToken").exists())
                .andExpect(jsonPath("loginInfo.refreshTokenExpirationPeriodDateTime").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.index.href").exists())
        ;
        //then

    }

    @Test
    @DisplayName("회원 조회 시 조회할 회원이 없는 경우")
    public void getMember_NotFound() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
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
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.index.href").exists())
        ;

    }

    @Test
    @DisplayName("인증 권한이 없는 회원이 회원을 조회할 경우")
    public void getMember_No_Authentication() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member member = memberService.userJoin(memberCreateDto);
        //when

        //then
        mockMvc.perform(
                get("/api/members/" + member.getId())
//                        .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
        )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.refresh.href").exists())
        ;

    }

    @Test
    @DisplayName("관리자가 다른 사용자를 조회하는 경우")
    public void admin_Get_Of_DifferentMember() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        memberService.adminJoin(memberCreateDto);

        LoginDto adminLoginDto = getLoginDto(memberCreateDto);
        TokenDto adminTokenDto = memberService.login(adminLoginDto);

        memberCreateDto.setJoinId("userjoinId");
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
                .andExpect(jsonPath("id").value(userMember.getId()))
                .andExpect(jsonPath("joinId").value(memberCreateDto.getJoinId()))
                .andExpect(jsonPath("email").value(memberCreateDto.getEmail()))
                .andExpect(jsonPath("name").value(memberCreateDto.getName()))
                .andExpect(jsonPath("nickname").value(memberCreateDto.getNickname()))
                .andExpect(jsonPath("dateOfBirth").value(memberCreateDto.getDateOfBirth().toString()))
                .andExpect(jsonPath("gender").value(memberCreateDto.getGender().name()))
                .andExpect(jsonPath("simpleAddress").value(memberCreateDto.getSimpleAddress()))
                .andExpect(jsonPath("sharedVocabularyCount").value(0))
                .andExpect(jsonPath("bbsCount").value(0))
                .andExpect(jsonPath("registerDate").exists())
                .andExpect(jsonPath("updateDate").exists())
                .andExpect(jsonPath("loginInfo.refreshToken").exists())
                .andExpect(jsonPath("loginInfo.refreshTokenExpirationPeriodDateTime").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.index.href").exists())
        ;

    }

    @Test
    @DisplayName("인증된 사용자와 조회하려는 사용자가 다른 경우")
    public void getMember_Unauthorized() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        memberService.userJoin(memberCreateDto);
        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto tokenDto = memberService.login(loginDto);
        memberCreateDto.setJoinId("jonfdsa");
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
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.index.href").exists())
        ;


    }

    @Test
    @DisplayName("회원 정보 수정")
    public void modifyMember() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member joinMember = memberService.userJoin(memberCreateDto);
        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto tokenDto = memberService.login(loginDto);

        MemberUpdateDto memberUpdateDto = getMemberUpdateDto();
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
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.index.href").exists())
                .andExpect(jsonPath("_links.get-member.href").exists())
        ;

        Member findMember = memberService.getMember(joinMember.getId());

        Assertions.assertEquals(findMember.getJoinId(), memberUpdateDto.getJoinId());
        Assertions.assertEquals(findMember.getEmail(), memberUpdateDto.getEmail());
        Assertions.assertEquals(findMember.getName(), memberUpdateDto.getName());
        Assertions.assertEquals(findMember.getNickname(), memberUpdateDto.getNickname());
        Assertions.assertEquals(findMember.getDateOfBirth(), memberUpdateDto.getDateOfBirth());
        Assertions.assertEquals(findMember.getGender(), memberUpdateDto.getGender());
        Assertions.assertEquals(findMember.getSimpleAddress(), memberUpdateDto.getSimpleAddress());
    }

    private MemberUpdateDto getMemberUpdateDto() {
        return MemberUpdateDto.builder()
                .joinId("updateId")
                .email("update@email.com")
                .name("updateName")
                .nickname("updateNickname")
                .dateOfBirth(LocalDate.of(1996, 11, 8))
                .gender(Gender.FEMALE)
                .simpleAddress("서울 성북구")
                .build();
    }

    @Test
    @DisplayName("회원 정보 수정 시 수정할 회원이 없는 경우")
    public void modifyMember_NouFound() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto tokenDto = memberService.login(loginDto);

        MemberUpdateDto memberUpdateDto = getMemberUpdateDto();
        //when

        //then
        mockMvc
                .perform(
                        put("/api/members/" + 1000L)
                                .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
                                .param("password", memberCreateDto.getPassword())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(memberUpdateDto))
                )
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.index.href").exists())
        ;

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 회원 정보를 수정할 경우")
    public void modify_Unauthorized() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
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
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.index.href").exists())
                .andExpect(jsonPath("_links.refresh.href").exists())
        ;

    }

    @Test
    @DisplayName("다른 회원의 정보를 수정할 경우")
    public void modify_DifferentMember() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        memberService.userJoin(memberCreateDto);

        LoginDto userLoginDto = getLoginDto(memberCreateDto);
        TokenDto userTokenDto = memberService.login(userLoginDto);

        memberCreateDto.setJoinId("adminjoinid");
        Member adminMember = memberService.adminJoin(memberCreateDto);

        MemberUpdateDto memberUpdateDto = getMemberUpdateDto();
        //when

        //then
        mockMvc
                .perform(
                        put("/api/members/" + adminMember.getId())
                                .header(X_AUTH_TOKEN, userTokenDto.getAccessToken())
                                .param("password", memberCreateDto.getPassword())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(memberUpdateDto))
                )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.index.href").exists())
        ;
    }

    @Test
    @DisplayName("회원 수정 시 비밀번호를 잘못 입력한 경우")
    public void modifyMember_Password_Mismatch() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto tokenDto = memberService.login(loginDto);

        MemberUpdateDto memberUpdateDto = getMemberUpdateDto();
        //when

        //then
        mockMvc
                .perform(
                        put("/api/members/" + userMember.getId())
                                .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
                                .param("password", "fdjasklfdjak")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(memberUpdateDto))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.index.href").exists())
        ;
    }

    private LoginDto getLoginDto(MemberCreateDto memberCreateDto) {
        LoginDto loginDto = new LoginDto();
        loginDto.setJoinId(memberCreateDto.getJoinId());
        loginDto.setPassword(memberCreateDto.getPassword());
        return loginDto;
    }

    @Test
    @DisplayName("비밀번호 수정")
    public void updatePassword() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
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
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.login.href").exists())
                .andExpect(jsonPath("_links.index.href").exists())
        ;

        Member findMember = memberService.getMember(userMember.getId());

        Assertions.assertNull(findMember.getLoginInfo());

    }

    @Test
    @DisplayName("비밀번호 변경 시 변경할 회원이 없는 경우")
    public void updatePassword_NotFound() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto tokenDto = memberService.login(loginDto);

        String newPassword = "newPassword";
        PasswordUpdateDto passwordUpdateDto = PasswordUpdateDto.builder()
                .newPassword(newPassword)
                .oldPassword(memberCreateDto.getPassword())
                .build();

        //when

        //then
        mockMvc
                .perform(
                        put("/api/members/password/" + 1000L)
                                .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(passwordUpdateDto))
                )
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.index.href").exists())
        ;

    }

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
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.refresh.href").exists())
                .andExpect(jsonPath("_links.index.href").exists())
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
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.index.href").exists())
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
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.index.href").exists())
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
                        put("/api/members/secession/" + userMember.getId())
                                .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
                );

        //then
        perform
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value(MessageDto.SECESSION_SUCCESSFULLY))
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.index.href").value(LinkToVo.linkToIndex().toUri().toString()))
        ;

        Member findMember = memberService.getMember(userMember.getId());

        assertTrue(findMember.getRoles().contains(MemberRole.SECESSION));
        assertFalse(findMember.getRoles().contains(MemberRole.USER));
        assertFalse(findMember.getRoles().contains(MemberRole.ADMIN));
        assertFalse(findMember.getRoles().contains(MemberRole.BAN));

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
                        put("/api/members/secession/" + userMember.getId())
//                                .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
                );

        //then
        perform
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageDto.UN_AUTHENTICATION))
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.index.href").value(LinkToVo.linkToIndex().toUri().toString()))
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
        memberCreateDto.setPassword("fdafdafdasf");
        Member userMember2 = memberService.userJoin(memberCreateDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/members/secession/" + userMember2.getId())
                                .header(X_AUTH_TOKEN, userMember1TokenDto.getAccessToken())
                );

        //then
        perform
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageDto.MODIFY_DIFFERENT_MEMBER_INFO))
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.index.href").value(LinkToVo.linkToIndex().toUri().toString()))
        ;

    }

    @Test
    @DisplayName("탈퇴하려는 회원이 없는 경우")
    public void secession_NotFound() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto tokenDto = memberService.login(loginDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/members/secession/" + 10000L)
                                .header(X_AUTH_TOKEN, tokenDto.getAccessToken())
                );

        //then
        perform
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new MemberNotFoundException().getMessage()))
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.index.href").value(LinkToVo.linkToIndex().toUri().toString()))
        ;
    }

}