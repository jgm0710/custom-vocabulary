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
import project.study.jgm.customvocabulary.members.MemberRepository;
import project.study.jgm.customvocabulary.members.MemberService;
import project.study.jgm.customvocabulary.security.JwtTokenProvider;

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

    @BeforeEach
    public void setup(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))  // 필터 추가
                .apply(springSecurity())    //springSecurityFilter 를 타기 위해서 허용해줘야함. -> 없으면 filter를 타지 않음
                .alwaysDo(print())
                .build();
    }

}
