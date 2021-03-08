package project.study.jgm.customvocabulary.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.multipart.MultipartFile;
import project.study.jgm.customvocabulary.bbs.BbsRepository;
import project.study.jgm.customvocabulary.bbs.BbsService;
import project.study.jgm.customvocabulary.bbs.like.BbsLikeRepository;
import project.study.jgm.customvocabulary.bbs.like.BbsLikeService;
import project.study.jgm.customvocabulary.bbs.reply.ReplyRepository;
import project.study.jgm.customvocabulary.bbs.reply.ReplyService;
import project.study.jgm.customvocabulary.bbs.reply.like.ReplyLikeRepository;
import project.study.jgm.customvocabulary.bbs.reply.like.ReplyLikeService;
import project.study.jgm.customvocabulary.bbs.upload.BbsFileStorageService;
import project.study.jgm.customvocabulary.bbs.upload.BbsUploadFile;
import project.study.jgm.customvocabulary.bbs.upload.BbsUploadFileRepository;
import project.study.jgm.customvocabulary.common.upload.OnlyFileIdDto;
import project.study.jgm.customvocabulary.members.*;
import project.study.jgm.customvocabulary.members.dto.MemberCreateDto;
import project.study.jgm.customvocabulary.security.JwtTokenProvider;
import project.study.jgm.customvocabulary.security.dto.LoginDto;
import project.study.jgm.customvocabulary.vocabulary.VocabularyRepository;
import project.study.jgm.customvocabulary.vocabulary.VocabularyService;
import project.study.jgm.customvocabulary.vocabulary.category.*;
import project.study.jgm.customvocabulary.vocabulary.like.VocabularyLikeRepository;
import project.study.jgm.customvocabulary.vocabulary.like.VocabularyLikeService;
import project.study.jgm.customvocabulary.vocabulary.upload.VocabularyFileStorageService;
import project.study.jgm.customvocabulary.vocabulary.upload.VocabularyThumbnailImageFileRepository;
import project.study.jgm.customvocabulary.vocabulary.word.upload.WordFileStorageService;
import project.study.jgm.customvocabulary.vocabulary.word.upload.WordImageFileRepository;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@ExtendWith(value = {SpringExtension.class, RestDocumentationExtension.class})
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ActiveProfiles("test")
@Disabled
@Transactional
public abstract class BaseControllerTest {

    @Autowired
    protected VocabularyLikeService vocabularyLikeService;

    @Autowired
    protected VocabularyLikeRepository vocabularyLikeRepository;

    @Autowired
    protected VocabularyThumbnailImageFileRepository vocabularyThumbnailImageFileRepository;

    @Autowired
    protected VocabularyFileStorageService vocabularyFileStorageService;

    @Autowired
    protected WordImageFileRepository wordImageFileRepository;

    @Autowired
    protected WordFileStorageService wordFileStorageService;

    @Autowired
    protected ModelMapper modelMapper;

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

    @Autowired
    protected BbsFileStorageService bbsFileStorageService;

    @Autowired
    protected BbsUploadFileRepository bbsUploadFileRepository;

    protected final String X_AUTH_TOKEN = "X-AUTH-TOKEN";

    protected final String testImageFilePath = "/static/test/사진1.jpg";

    protected final String testTextFilePath = "/static/test/text.txt";

    protected final String testImageFilePath2 = "/static/test/사진2.jpg";

    protected final String testZipFilePath = "/static/test/새 폴더.zip";

    protected final String X_AUTH_TOKEN_DESCRIPTION = "해당 리소스에 접근하기 위해 발급된 Access Token 기입";

    protected final String MESSAGE_DESCRIPTION = "해당 요청에 대한 응답의 간략한 설명";

    protected final String br = " + \n";

    @BeforeEach
    public void setup(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))  // 필터 추가
                .apply(springSecurity())    //springSecurityFilter 를 타기 위해서 허용해줘야함. -> 없으면 filter를 타지 않음
                .alwaysDo(print())
                .apply(documentationConfiguration(restDocumentation)
                        .operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint()))
                .build();

        vocabularyLikeRepository.deleteAll();
        vocabularyThumbnailImageFileRepository.deleteAll();
        wordImageFileRepository.deleteAll();
        bbsUploadFileRepository.deleteAll();
        vocabularyRepository.deleteAll();
        categoryRepository.deleteAll();
        bbsLikeRepository.deleteAll();
        replyLikeRepository.deleteAll();
        replyRepository.deleteAll();
        bbsRepository.deleteAll();
        memberRepository.deleteAll();
    }

    protected LoginDto getLoginDto(MemberCreateDto memberCreateDto) {
        LoginDto loginDto = new LoginDto();
        loginDto.setJoinId(memberCreateDto.getJoinId());
        loginDto.setPassword(memberCreateDto.getPassword());
        return loginDto;
    }

    protected MemberCreateDto getMemberCreateDto(String joinId, String nickname) {
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

    protected List<Category> createCategoryList(Member member, CategoryDivision division) {
        Category sub1 = createCategory(member, division, "sub1", null, 2);
        Category sub2 = createCategory(member, division, "sub2", null, 3);
        Category sub3 = createCategory(member, division, "sub3", null, 1);
        Category sub11 = createCategory(member, division, "sub1-1", sub1, 3);
        Category sub12 = createCategory(member, division, "sub1-2", sub1, 2);
        Category sub13 = createCategory(member, division, "sub1-3", sub1, 1);
        Category sub21 = createCategory(member, division, "sub2-1", sub2, 2);
        Category sub22 = createCategory(member, division, "sub2-2", sub2, 1);
        Category sub23 = createCategory(member, division, "sub2-2", sub2, 3);
        Category sub31 = createCategory(member, division, "sub3-1", sub3, 1);
        Category sub32 = createCategory(member, division, "sub3-2", sub3, 3);
        Category sub33 = createCategory(member, division, "sub3-3", sub3, 2);

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

    protected Category createCategory(Member userMember, CategoryDivision division, String name, Category parent, int orders) {
        Category category = Category.builder()
                .name(name)
                .member(userMember)
                .parent(parent)
                .vocabularyCount(0)
                .division(division)
                .orders(orders)
                .build();

        categoryRepository.save(category);

        return category;
    }

    protected List<OnlyFileIdDto> getOnlyFileIdDtos() throws IOException {
        String path = "/static/test/text.txt";
        MultipartFile multipartFile = getMockMultipartFile("files", path);
        BbsUploadFile bbsUploadFile = bbsFileStorageService.uploadBbsFile(multipartFile);

        String path2 = "/static/test/사진1.jpg";
        MultipartFile multipartFile1 = getMockMultipartFile("files", path2);
        BbsUploadFile bbsUploadFile1 = bbsFileStorageService.uploadBbsFile(multipartFile1);

        OnlyFileIdDto onlyFileIdDto = new OnlyFileIdDto(bbsUploadFile.getId());
        OnlyFileIdDto onlyFileIdDto1 = new OnlyFileIdDto(bbsUploadFile1.getId());

        List<OnlyFileIdDto> onlyFileIdDtos = new ArrayList<>();
        onlyFileIdDtos.add(onlyFileIdDto);
        onlyFileIdDtos.add(onlyFileIdDto1);
        return onlyFileIdDtos;
    }

    protected MockMultipartFile getMockMultipartFile(String parameterName, String path) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(path);
        String filename = classPathResource.getFilename();
        String contentType = URLConnection.guessContentTypeFromName(filename);
        return new MockMultipartFile(parameterName, filename, contentType, classPathResource.getInputStream());
    }


}
