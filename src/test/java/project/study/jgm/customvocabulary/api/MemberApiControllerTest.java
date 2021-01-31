package project.study.jgm.customvocabulary.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import project.study.jgm.customvocabulary.members.Gender;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRepository;
import project.study.jgm.customvocabulary.members.MemberService;
import project.study.jgm.customvocabulary.members.dto.MemberCreateDto;
import project.study.jgm.customvocabulary.security.JwtTokenProvider;
import project.study.jgm.customvocabulary.security.dto.LoginDto;
import project.study.jgm.customvocabulary.security.dto.TokenDto;

import java.time.LocalDate;
import java.util.stream.Collectors;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(value = SpringExtension.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MemberApiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    public void setup(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))  // 필터 추가
                .apply(springSecurity())    //springSecurityFilter 를 타기 위해서 허용해줘야함. -> 없으면 filter를 타지 않음
                .alwaysDo(print())
                .build();

        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입")
    public void join() throws Exception {
        //given
        MemberCreateDto createDto = createMemberCreateDto();

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

    private MemberCreateDto createMemberCreateDto() {
        MemberCreateDto createDto = MemberCreateDto.builder()
                .joinId("testJoinid")
                .email("test@email.com")
                .password("test")
                .name("정구민")
                .nickname("test")
                .dateOfBirth(LocalDate.now())
                .gender(Gender.MALE)
                .simpleAddress("address")
                .build();
        return createDto;
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
        MemberCreateDto createDto = createMemberCreateDto();
        Member member = memberService.userJoin(createDto);
        LoginDto loginDto = createLoginDto(createDto);

        TokenDto tokenDto = memberService.login(loginDto);

        //when
        mockMvc.perform(
                get("/api/members/" + member.getId())
                        .header("X-AUTH-TOKEN", tokenDto.getAccessToken())
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
    @DisplayName("인증 권한이 없는 회원이 회원을 조회할 경우")
    public void getMember_No_Authentication() throws Exception {
        //given
        MemberCreateDto memberCreateDto = createMemberCreateDto();
        Member member = memberService.userJoin(memberCreateDto);
        //when

        //then
        mockMvc.perform(
                get("/api/members/" + member.getId())
//                        .header("X-AUTH-TOKEN", tokenDto.getAccessToken())
        )
                .andDo(print())
                .andExpect(status().isUnauthorized())
//                .andExpect(jsonPath())
        ;

    }

    @Test
    @DisplayName("인증된 사용자와 조회하려는 사용자가 다른 경우")
    public void getMember_Unauthorized() throws Exception {
        //given

        //when

        //then

    }

    private LoginDto createLoginDto(MemberCreateDto memberCreateDto) {
        LoginDto loginDto = new LoginDto();
        loginDto.setJoinId(memberCreateDto.getJoinId());
        loginDto.setPassword(memberCreateDto.getPassword());
        return loginDto;
    }


}