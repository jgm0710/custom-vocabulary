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
import org.springframework.transaction.annotation.Transactional;
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
import project.study.jgm.customvocabulary.vocabulary.VocabularyRepository;
import project.study.jgm.customvocabulary.vocabulary.VocabularyService;
import project.study.jgm.customvocabulary.vocabulary.category.*;

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

    @Autowired
    protected CategoryService categoryService;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected VocabularyService vocabularyService;

    @Autowired
    protected VocabularyRepository vocabularyRepository;


    @BeforeEach
    public void setup(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))  // 필터 추가
                .apply(springSecurity())    //springSecurityFilter 를 타기 위해서 허용해줘야함. -> 없으면 filter를 타지 않음
                .alwaysDo(print())
                .build();

        vocabularyRepository.deleteAll();
        categoryRepository.deleteAll();
        bbsLikeRepository.deleteAll();
        replyLikeRepository.deleteAll();
        replyRepository.deleteAll();
        bbsRepository.deleteAll();
        memberRepository.deleteAll();
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

    protected List<Category> createCategoryList(Member member,CategoryDivision division) {
        Category sub1 = createCategory(member, division, "sub1", null, 2, CategoryStatus.REGISTER);
        Category sub2 = createCategory(member, division, "sub2", null, 3, CategoryStatus.REGISTER);
        Category sub3 = createCategory(member, division, "sub3", null, 1, CategoryStatus.REGISTER);
        Category sub11 = createCategory(member, division, "sub1-1", sub1, 3, CategoryStatus.REGISTER);
        Category sub12 = createCategory(member, division, "sub1-2", sub1, 2, CategoryStatus.REGISTER);
        Category sub13 = createCategory(member, division, "sub1-3", sub1, 1, CategoryStatus.REGISTER);
        Category sub21 = createCategory(member, division, "sub2-1", sub2, 2, CategoryStatus.REGISTER);
        Category sub22 = createCategory(member, division, "sub2-2", sub2, 1, CategoryStatus.REGISTER);
        Category sub23 = createCategory(member, division, "sub2-2", sub2, 3, CategoryStatus.REGISTER);
        Category sub31 = createCategory(member, division, "sub3-1", sub3, 1, CategoryStatus.REGISTER);
        Category sub32 = createCategory(member, division, "sub3-2", sub3, 3, CategoryStatus.REGISTER);
        Category sub33 = createCategory(member, division, "sub3-3", sub3, 2, CategoryStatus.REGISTER);

        categoryRepository.save(sub1);
        categoryRepository.save(sub2);
        categoryRepository.save(sub3);
        categoryRepository.save(sub11);
        categoryRepository.save(sub12);
        categoryRepository.save(sub13);
        categoryRepository.save(sub21);
        categoryRepository.save(sub22);
        categoryRepository.save(sub23);
        categoryRepository.save(sub31);
        categoryRepository.save(sub32);
        categoryRepository.save(sub33);

        return List.of(sub1, sub2, sub11, sub12, sub21, sub22);
    }

    protected Category createCategory(Member userMember, CategoryDivision division, String name, Category parent, int orders, CategoryStatus status) {
        Category category = Category.builder()
                .name(name)
                .member(userMember)
                .parent(parent)
                .vocabularyCount(0)
                .division(division)
                .orders(orders)
                .status(status)
                .build();

        categoryRepository.save(category);

        return category;
    }



}
