package project.study.jgm.customvocabulary.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import project.study.jgm.customvocabulary.bbs.BbsRepository;
import project.study.jgm.customvocabulary.bbs.BbsService;
import project.study.jgm.customvocabulary.bbs.like.BbsLikeRepository;
import project.study.jgm.customvocabulary.bbs.like.BbsLikeService;
import project.study.jgm.customvocabulary.bbs.reply.ReplyRepository;
import project.study.jgm.customvocabulary.bbs.reply.ReplyService;
import project.study.jgm.customvocabulary.bbs.reply.like.ReplyLikeRepository;
import project.study.jgm.customvocabulary.bbs.reply.like.ReplyLikeService;
import project.study.jgm.customvocabulary.members.*;
import project.study.jgm.customvocabulary.members.dto.MemberCreateDto;
import project.study.jgm.customvocabulary.security.JwtTokenProvider;
import project.study.jgm.customvocabulary.security.dto.LoginDto;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@ExtendWith(value = SpringExtension.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Disabled
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MemberService memberService;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected JwtTokenProvider jwtTokenProvider;

    @Autowired
    protected SecurityProperties securityProperties;

    @Autowired
    protected EntityManager em;

    @Autowired
    protected BbsRepository bbsRepository;

    @Autowired
    protected BbsService bbsService;

    @Autowired
    protected BbsLikeService bbsLikeService;

    @Autowired
    protected BbsLikeRepository bbsLikeRepository;

    @Autowired
    protected ReplyLikeService replyLikeService;

    @Autowired
    protected ReplyLikeRepository replyLikeRepository;

    @Autowired
    protected ReplyService replyService;

    @Autowired
    protected ReplyRepository replyRepository;

    @BeforeEach
    public void setup(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))  // 필터 추가
                .apply(springSecurity())    //springSecurityFilter 를 타기 위해서 허용해줘야함. -> 없으면 filter를 타지 않음
                .alwaysDo(print())
                .build();
    }

    protected final String X_AUTH_TOKEN = "X-AUTH-TOKEN";

    protected final String MEMBER_API_CONTROLLER_REQUEST_VALUE = "http://localhost/api/members";

    protected LoginDto getLoginDto(MemberCreateDto memberCreateDto) {
        LoginDto loginDto = new LoginDto();
        loginDto.setJoinId(memberCreateDto.getJoinId());
        loginDto.setPassword(memberCreateDto.getPassword());
        return loginDto;
    }

    protected MemberCreateDto getMemberCreateDto(String joinId,String nickname) {
        return MemberCreateDto.builder()
                .joinId(joinId)
                .email("test@email.com")
                .password("test")
                .name("정구민")
                .nickname(nickname)
                .dateOfBirth(LocalDate.now())
                .gender(Gender.MALE)
                .simpleAddress("address")
                .build();
    }

    protected void createMemberList() {
        for (int i = 0; i < 1000; i++) {
            Random random = new Random();

            Member member = Member.builder()
                    .joinId("aajoinId" + i)
                    .email("fadsuser" + random.nextInt(1000) + "@email.com")
                    .password("fadspassword" + random.nextInt(1000))
                    .name("fdasname" + random.nextInt(1000))
                    .nickname("fdsanickname" + i)
                    .dateOfBirth(LocalDate.now())
                    .gender(Gender.MALE)
                    .simpleAddress("fadsaddress")
                    .sharedVocabularyCount(random.nextInt(1000))
                    .bbsCount(random.nextInt(1000))
                    .roles(List.of(MemberRole.USER))
                    .loginInfo(LoginInfo.initialize(securityProperties))
                    .registerDate(LocalDateTime.now())
                    .updateDate(LocalDateTime.now())
                    .build();

            memberRepository.save(member);
        }
    }



}
