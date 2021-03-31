package project.study.jgm.customvocabulary.bbs.reply;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import project.study.jgm.customvocabulary.bbs.Bbs;
import project.study.jgm.customvocabulary.bbs.BbsRepository;
import project.study.jgm.customvocabulary.bbs.BbsStatus;
import project.study.jgm.customvocabulary.bbs.reply.dto.ReplyCreateDto;
import project.study.jgm.customvocabulary.members.*;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ReplyServiceTest {

    @Autowired
    ReplyService replyService;

    @Autowired
    ReplyRepository replyRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BbsRepository bbsRepository;

    @Autowired
    EntityManager em;

    @Test
    void addReply() {
        String joinId = "retestjoinid";
        String nickname = "testnickname";
        Member member1 = getMember(joinId, nickname);
        Member member2 = getMember("test2", "test2nick");

        BbsStatus status = BbsStatus.REGISTER;
        String title = "testtitle";
        String content = "test content";
        Bbs bbs = getBbs(member1, status, title, content);

        em.flush();
        em.clear();

        String test_content_reply = "test content reply";
        Reply parent = replyService.addReply(member1.getId(), bbs.getId(), test_content_reply);

        String child1_content = "child1 content";
        Reply child1 = replyService.addReplyOfReply(member2.getId(), parent.getId(), child1_content);
        String child2_content = "child2 content";
        Reply child2 = replyService.addReplyOfReply(member2.getId(), parent.getId(), child2_content);

        em.flush();
        em.clear();

        Reply findParent = replyRepository.findById(parent.getId()).get();

        assertEquals(findParent.getMember().getId(), member1.getId());
        assertEquals(findParent.getBbs().getId(), bbs.getId());
        assertEquals(findParent.getChildrenCount(), 2);

        System.out.println("findParent.getChildrenCount() = " + findParent.getChildrenCount());

        Reply findChild1 = replyRepository.findById(child1.getId()).get();
        Reply findChild2 = replyRepository.findById(child2.getId()).get();


        assertEquals(findChild1.getParent().getId(), findParent.getId());
        assertEquals(findChild2.getParent().getId(), findParent.getId());

    }

    private Bbs getBbs(Member member1, BbsStatus status, String title, String content) {
        Bbs bbs = Bbs.builder()
                .member(member1)
                .title(title)
                .content(content)
                .views(0)
                .likeCount(0)
                .replyCount(0)
                .registerDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .status(status)
                .build();

        bbsRepository.save(bbs);
        return bbs;
    }

    private Member getMember(String joinId, String nickname) {
        Member member = Member.builder()
                .joinId(joinId)
                .email("testemail@email.com")
                .password("testpassword")
                .name("testname")
                .nickname(nickname)
                .dateOfBirth(LocalDate.now())
                .gender(Gender.MALE)
                .simpleAddress("address")
                .sharedVocabularyCount(0)
                .bbsCount(0)
                .roles(List.of(MemberRole.USER))
                .loginInfo(LoginInfo.deleteInfo())
                .registerDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        memberRepository.save(member);

        return member;
    }

    @Test
    void deleteReply() {
        String joinId = "retestjoinid";
        String nickname = "testnickname";
        Member member1 = getMember(joinId, nickname);
        Member member2 = getMember("test2", "test2nick");

        BbsStatus status = BbsStatus.REGISTER;
        String title = "testtitle";
        String content = "test content";
        Bbs bbs = getBbs(member1, status, title, content);

        em.flush();
        em.clear();

        String test_content_reply = "test content reply";
        Reply parent = replyService.addReply(member1.getId(), bbs.getId(), test_content_reply);

        String child1_content = "child1 content";
        Reply child1 = replyService.addReplyOfReply(member2.getId(), parent.getId(), child1_content);
        String child2_content = "child2 content";
        Reply child2 = replyService.addReplyOfReply(member2.getId(), parent.getId(), child2_content);

        em.flush();
        em.clear();

        replyService.deleteReply(child2.getId());

        em.flush();
        em.clear();

        Reply findParent = replyRepository.findById(parent.getId()).get();

        assertEquals(findParent.getMember().getId(), member1.getId());
        assertEquals(findParent.getBbs().getId(), bbs.getId());
        assertEquals(findParent.getChildrenCount(), 1);

        System.out.println("findParent.getChildrenCount() = " + findParent.getChildrenCount());

        Reply findChild1 = replyRepository.findById(child1.getId()).get();
        Reply findChild2 = replyRepository.findById(child2.getId()).get();

        assertEquals(findChild1.getParent().getId(), findParent.getId());
        assertEquals(findChild2.getParent().getId(), findParent.getId());
    }
}