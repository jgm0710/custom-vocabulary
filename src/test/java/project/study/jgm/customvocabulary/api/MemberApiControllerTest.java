package project.study.jgm.customvocabulary.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import project.study.jgm.customvocabulary.common.BaseControllerTest;
import project.study.jgm.customvocabulary.common.dto.MessageDto;
import project.study.jgm.customvocabulary.members.Gender;
import project.study.jgm.customvocabulary.members.LoginInfo;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRole;
import project.study.jgm.customvocabulary.members.dto.MemberCreateDto;
import project.study.jgm.customvocabulary.members.dto.MemberUpdateDto;
import project.study.jgm.customvocabulary.members.dto.PasswordUpdateDto;
import project.study.jgm.customvocabulary.members.dto.search.MemberSearchType;
import project.study.jgm.customvocabulary.members.dto.search.MemberSortType;
import project.study.jgm.customvocabulary.members.exception.MemberAlreadyHasAuthorityException;
import project.study.jgm.customvocabulary.members.exception.MemberNotFoundException;
import project.study.jgm.customvocabulary.security.dto.LoginDto;
import project.study.jgm.customvocabulary.security.dto.TokenDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
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
//                .andExpect(jsonPath("loginInfo.refreshToken").exists())
//                .andExpect(jsonPath("loginInfo.refreshTokenExpirationPeriodDateTime").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.login.href").exists())
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
//                .andExpect(jsonPath("loginInfo.refreshToken").exists())
//                .andExpect(jsonPath("loginInfo.refreshTokenExpirationPeriodDateTime").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.index.href").exists())
        ;
        //then

    }

    @Test
    @DisplayName("탈퇴한 회원이 회원을 조회하는 경우")
    public void getMember_By_SecessionMember() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid", "test");
        Member userMember = memberService.userJoin(memberCreateDto);

        memberService.secession(userMember.getId());

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
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.index.href").exists())
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
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid","test");
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
                .andExpect(jsonPath("roles.[0]").value(MemberRole.USER.name()))
//                .andExpect(jsonPath("loginInfo.refreshToken").exists())
//                .andExpect(jsonPath("loginInfo.refreshTokenExpirationPeriodDateTime").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.index.href").exists())
        ;

    }

    @Test
    @DisplayName("인증된 사용자와 조회하려는 사용자가 다른 경우")
    public void getMember_Unauthorized() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid","test");
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
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.index.href").exists())
        ;


    }

    @Test
    @DisplayName("회원 정보 수정")
    public void modifyMember() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid","test");
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

    @Test
    @DisplayName("회원 수정 시 수정 정보가 비어 있는 경우")
    public void modifyMember_Empty() throws Exception {
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid","test");
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
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid","test");
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
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid","test");
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
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid","test");
        memberService.userJoin(memberCreateDto);

        LoginDto userLoginDto = getLoginDto(memberCreateDto);
        TokenDto userTokenDto = memberService.login(userLoginDto);

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("adminuser", "adminuser");
        Member adminMember = memberService.adminJoin(memberCreateDto1);

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
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid","test");
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


    @Test
    @DisplayName("비밀번호 수정")
    public void updatePassword() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid","test");
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
    @DisplayName("비밀번호 수정 시 수정 정보가 없는 경우")
    public void updatePassword_Empty() throws Exception {
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid","test");
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

    @Test
    @DisplayName("비밀번호 변경 시 변경할 회원이 없는 경우")
    public void updatePassword_NotFound() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("testJoinid","test");
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
                .andExpect(jsonPath("_links.index.href").value(linkToIndex().toUri().toString()))
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
                        put("/api/members/secession/" + userMember2.getId())
                                .header(X_AUTH_TOKEN, userMember1TokenDto.getAccessToken())
                );

        //then
        perform
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageDto.MODIFY_DIFFERENT_MEMBER_INFO))
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.index.href").value(linkToIndex().toUri().toString()))
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
                .andExpect(jsonPath("_links.index.href").value(linkToIndex().toUri().toString()))
        ;
    }

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
                .andExpect(jsonPath("paging.totalCount").exists())
                .andExpect(jsonPath("paging.criteriaDto.pageNum").value(12))
                .andExpect(jsonPath("paging.criteriaDto.limit").value(4))
                .andExpect(jsonPath("paging.startPage").value(11))
                .andExpect(jsonPath("paging.endPage").value(20))
                .andExpect(jsonPath("paging.prev").value(true))
                .andExpect(jsonPath("paging.next").value(true))
                .andExpect(jsonPath("paging.totalPage").exists())
                .andExpect(jsonPath("_links.self.href").value(MEMBER_API_CONTROLLER_REQUEST_VALUE+"?searchType=JOIN_ID&keyword=joinId7&criteriaDto.pageNum=12&criteriaDto.limit=4&sortType=OLDEST"))
                .andExpect(jsonPath("_links.prev-list.href").value("http://localhost/api/members?searchType=JOIN_ID&keyword=joinId7&criteriaDto.pageNum=10&criteriaDto.limit=4&sortType=OLDEST"))
                .andExpect(jsonPath("_links.next-list.href").value("http://localhost/api/members?searchType=JOIN_ID&keyword=joinId7&criteriaDto.pageNum=21&criteriaDto.limit=4&sortType=OLDEST"))
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
    @DisplayName("회원 목록 조회 시페이징에 대한 정보를 잘 못 준 경우")
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
                                .param("criteriaDto.pageNum", ""+pageNum)
                                .param("criteriaDto.limit", ""+limit)
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
                .andExpect(jsonPath("message").value(MessageDto.BAN_SUCCESSFULLY))
                .andExpect(jsonPath("_links.self.href").value("http://localhost/api/members/ban/"+userMember.getId()))
                .andExpect(jsonPath("_links.get-member.href").value(linkToGetMember(userMember.getId()).toUri().toString()))
                .andExpect(jsonPath("_links.index.href").value(linkToIndex().toUri().toString()))
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
                .andExpect(jsonPath("_links.self.href").value(MEMBER_API_CONTROLLER_REQUEST_VALUE + "/ban/" + 10000L))
                .andExpect(jsonPath("_links.index.href").value(linkToIndex().toUri().toString()))
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

        memberService.secession(userMember.getId());

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/members/changeToUser/" + userMember.getId())
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value(MessageDto.CHANGE_MEMBER_ROLE_TO_USER_SUCCESSFULLY))
                .andExpect(jsonPath("_links.self.href").value(MEMBER_API_CONTROLLER_REQUEST_VALUE + "/changeToUser/" + userMember.getId()))
                .andExpect(jsonPath("_links.get-member.href").value(linkToGetMember(userMember.getId()).toUri().toString()))
                .andExpect(jsonPath("_links.index.href").value(linkToIndex().toUri().toString()))
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
                        put("/api/members/changeToUser/" + userMember.getId())
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value(MessageDto.CHANGE_MEMBER_ROLE_TO_USER_SUCCESSFULLY))
                .andExpect(jsonPath("_links.self.href").value(MEMBER_API_CONTROLLER_REQUEST_VALUE + "/changeToUser/" + userMember.getId()))
                .andExpect(jsonPath("_links.get-member.href").value(linkToGetMember(userMember.getId()).toUri().toString()))
                .andExpect(jsonPath("_links.index.href").value(linkToIndex().toUri().toString()))
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
                        put("/api/members/changeToUser/" + userMember.getId())
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
                        put("/api/members/changeToUser/" + userMember.getId())
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
                        put("/api/members/changeToUser/" + adminMember2.getId())
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new MemberAlreadyHasAuthorityException().getMessage()))
                .andExpect(jsonPath("_links.self.href").value(MEMBER_API_CONTROLLER_REQUEST_VALUE + "/changeToUser/" + adminMember2.getId()))
                .andExpect(jsonPath("_links.index.href").value(linkToIndex().toUri().toString()))
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
                        put("/api/members/changeToUser/" + userMember.getId())
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new MemberAlreadyHasAuthorityException().getMessage()))
                .andExpect(jsonPath("_links.self.href").value(MEMBER_API_CONTROLLER_REQUEST_VALUE + "/changeToUser/" + userMember.getId()))
                .andExpect(jsonPath("_links.index.href").value(linkToIndex().toUri().toString()))
        ;
    }

    private MemberCreateDto getMemberCreateDto() {
        return getMemberCreateDto("testJoinid", "test");
    }
}