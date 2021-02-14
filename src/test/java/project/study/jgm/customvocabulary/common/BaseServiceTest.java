package project.study.jgm.customvocabulary.common;

import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import project.study.jgm.customvocabulary.members.*;
import project.study.jgm.customvocabulary.vocabulary.VocabularyRepository;
import project.study.jgm.customvocabulary.vocabulary.VocabularyService;
import project.study.jgm.customvocabulary.vocabulary.category.*;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Disabled
public class BaseServiceTest {

    @Autowired
    protected EntityManager em;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected CategoryService categoryService;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected MemberService memberService;

    @Autowired
    protected CategoryQueryRepository categoryQueryRepository;

    @Autowired
    protected SecurityProperties securityProperties;

    @Autowired
    protected VocabularyService vocabularyService;

    @Autowired
    protected VocabularyRepository vocabularyRepository;

    protected Member createUserMember(String joinId, String nickname) {
        Member member = Member.builder()
                .joinId(joinId)
                .email(joinId + "@gmail.com")
                .password("password")
                .name("testname")
                .nickname(nickname)
                .dateOfBirth(LocalDate.now())
                .gender(Gender.MALE)
                .simpleAddress("simple")
                .sharedVocabularyCount(0)
                .bbsCount(0)
                .roles(List.of(MemberRole.USER))
                .loginInfo(LoginInfo.initialize(securityProperties))
                .registerDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        memberRepository.save(member);

        return member;
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
