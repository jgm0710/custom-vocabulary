package project.study.jgm.customvocabulary.bbs;

import com.querydsl.core.QueryResults;
import org.hibernate.sql.Delete;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import project.study.jgm.customvocabulary.bbs.dto.*;
import project.study.jgm.customvocabulary.bbs.exception.DeletedBbsException;
import project.study.jgm.customvocabulary.common.SecurityProperties;
import project.study.jgm.customvocabulary.common.dto.CriteriaDto;
import project.study.jgm.customvocabulary.members.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class BbsServiceTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BbsRepository bbsRepository;

    @Autowired
    BbsService bbsService;

    @Autowired
    SecurityProperties securityProperties;

    @Autowired
    EntityManager em;

    @BeforeEach
    void setUp() {
        bbsRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    void addBbs() {
        Member member = getMember("testNickname");

        BbsCreateDto bbsCreateDto = BbsCreateDto.builder()
                .title("test bbs")
                .content("test bbs content")
                .build();

        Bbs savedBbs = bbsService.addBbs(member.getId(), bbsCreateDto);

        assertEquals(savedBbs.getMember().getId(), member.getId());
        assertEquals(savedBbs.getTitle(), bbsCreateDto.getTitle());
        assertEquals(savedBbs.getContent(), bbsCreateDto.getContent());
        assertEquals(savedBbs.getViews(), 0);
        assertEquals(savedBbs.getReplyCount(), 0);
        assertEquals(savedBbs.getLikeCount(), 0);
    }

    private Member getMember(String joinId, String testNickname) {
        Member member = Member.builder()
                .joinId(joinId)
                .email("test@email.com")
                .password("testPassword")
                .name("testName")
                .nickname(testNickname)
                .dateOfBirth(LocalDate.of(1997, 1, 4))
                .gender(Gender.MALE)
                .simpleAddress("test address")
                .sharedVocabularyCount(0)
                .bbsCount(0)
                .roles(List.of(MemberRole.USER))
                .loginInfo(LoginInfo.initialize(securityProperties))
                .registerDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        Member savedMember = memberRepository.save(member);
        return member;
    }

    @ParameterizedTest
    @MethodSource(value = "paramsForGetBbsListTest")
    void getBbsList(BbsSearchType bbsSearchType, String keyword, BbsSortType bbsSortType) {
        Member member1 = getMember("testNickname1");
        Member member2 = getMember("testjoinId", "testNickname2");

        em.flush();
        em.clear();

        createBbsList(member1);
        createBbsList(member2);

        BbsSearchDto bbsSearchDto = BbsSearchDto.builder()
                .searchType(bbsSearchType)
                .keyword(keyword)
                .criteriaDto(new CriteriaDto())
                .bbsSortType(bbsSortType)
                .build();

        em.flush();
        em.clear();

        QueryResults<Bbs> results = bbsService.getBbsList(bbsSearchDto);

        long total = results.getTotal();
        List<Bbs> findBbsList = results.getResults();

        if (bbsSearchType == BbsSearchType.TITLE) {
            for (Bbs findBbs : findBbsList) {
                assertTrue(findBbs.getTitle().contains(keyword));
            }
        } else if (bbsSearchType == BbsSearchType.CONTENT) {
            for (Bbs findBbs : findBbsList) {
                assertTrue(findBbs.getContent().contains(keyword));
            }
        } else if (bbsSearchType == BbsSearchType.TITLE_OR_CONTENT) {
            for (Bbs findBbs : findBbsList) {
                boolean contains = findBbs.getTitle().contains(keyword) || findBbs.getContent().contains(keyword);
                assertTrue(contains);
            }
        } else {    //bbsSearchType == BbsSearchType.WRITER
            for (Bbs findBbs : findBbsList) {
                assertTrue(findBbs.getMember().getNickname().contains(keyword));
            }
        }

        int tmp = 100000;
        boolean sortFlag = true;

        for (Bbs findBbs : findBbsList) {
            int replyCount = findBbs.getReplyCount();

            if (replyCount <= tmp) {
                tmp = replyCount;
            } else {
                sortFlag = false;
                break;
            }
        }

        assertTrue(sortFlag);
    }

    static Stream<Arguments> paramsForGetBbsListTest() {
        return Stream.of(
//                Arguments.of(null, null, null),
                Arguments.of(BbsSearchType.TITLE, "bbs7", BbsSortType.REPLY_COUNT_DESC),
                Arguments.of(BbsSearchType.CONTENT, "content2", BbsSortType.REPLY_COUNT_DESC),
                Arguments.of(BbsSearchType.TITLE_OR_CONTENT, "bbs7", BbsSortType.REPLY_COUNT_DESC),
                Arguments.of(BbsSearchType.TITLE_OR_CONTENT, "content2", BbsSortType.REPLY_COUNT_DESC),
                Arguments.of(BbsSearchType.WRITER, "name1", BbsSortType.REPLY_COUNT_DESC),
                Arguments.of(BbsSearchType.WRITER, "name2", BbsSortType.REPLY_COUNT_DESC)
        );
    }

    private List<Bbs> createBbsList(Member member) {
        List<Bbs> bbsList = new ArrayList<>();

        for (int i = 0; i < 500; i++) {
            BbsStatus status;
            if (i % 2 == 1) {
                status = BbsStatus.REGISTER;
            } else {
                status = BbsStatus.DELETE;
            }

            Bbs bbs = getBbsSample(member, status);
            bbsList.add(bbs);
        }

        return bbsList;
    }

    private Bbs getBbsSample(Member member, BbsStatus status) {
        Bbs bbs = Bbs.builder()
                .member(member)
                .title("test bbs" + new Random().nextInt(1000))
                .content("test bbs content" + new Random().nextInt(1000))
                .views(new Random().nextInt(1000))
                .likeCount(new Random().nextInt(1000))
                .replyCount(new Random().nextInt(1000))
                .registerDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .status(status)
                .build();

        bbsRepository.save(bbs);
        return bbs;
    }

    @Test
    void getBbsListByMember() {
        Member member = getMember("testNickname");
        List<Bbs> bbsList = createBbsList(member);

        QueryResults<Bbs> results = bbsService.getBbsListByMember(member.getId(), new CriteriaDto());
        List<Bbs> findBbsList = results.getResults();

        for (Bbs findBbs : findBbsList) {
            Bbs bbs = bbsList.stream().filter(b -> b.getId().equals(findBbs.getId())).findFirst().orElseThrow(EntityNotFoundException::new);

            assertEquals(bbs.getId(), findBbs.getId());
            assertEquals(bbs.getMember().getId(), findBbs.getMember().getId());
            assertEquals(bbs.getTitle(), findBbs.getTitle());
            assertEquals(bbs.getContent(), findBbs.getContent());
        }
    }

    @Test
    void modifyBbs() {
        Member member = getMember("fdafad");
        Bbs bbsSample = getBbsSample(member, BbsStatus.REGISTER);

        String update_title = "update title";
        String update_content = "update content";

        BbsUpdateDto bbsUpdateDto = BbsUpdateDto.builder()
                .title(update_title)
                .content(update_content)
                .build();

        bbsService.modifyBbs(bbsSample.getId(), bbsUpdateDto);

        em.flush();
        em.clear();

        Bbs findBbs = bbsService.getBbs(bbsSample.getId());

        assertEquals(findBbs.getTitle(), update_title);
        assertEquals(findBbs.getContent(), update_content);
    }

    @Test
    @DisplayName("삭제된 게시글을 수정하는 경우")
    public void modify_DeletedBbs() throws Exception {
        //given
        Member member = getMember("fdafad");
        Bbs bbsSample = getBbsSample(member, BbsStatus.DELETE);

        String update_title = "update title";
        String update_content = "update content";

        BbsUpdateDto bbsUpdateDto = BbsUpdateDto.builder()
                .title(update_title)
                .content(update_content)
                .build();
        //when

        //then
        assertThrows(DeletedBbsException.class, () -> bbsService.modifyBbs(bbsSample.getId(), bbsUpdateDto));

    }

    @Test
    void deleteBbs() {
        Member member = getMember("fdsa");
        Bbs bbsSample = getBbsSample(member, BbsStatus.REGISTER);

        bbsService.deleteBbs(bbsSample.getId());

        em.flush();
        em.clear();

        Bbs findBbs = bbsRepository.findById(bbsSample.getId()).get();

        assertEquals(findBbs.getStatus(), BbsStatus.DELETE);
    }

    @Test
    @DisplayName("삭제된 게시글을 다시 삭제하는 경우")
    public void delete_DeletedBbs() throws Exception {
        //given
        Member member = getMember("fdsa");
        Bbs bbsSample = getBbsSample(member, BbsStatus.DELETE);

        //when

        //then
        assertThrows(DeletedBbsException.class, () -> bbsService.deleteBbs(bbsSample.getId()));

    }

    private Member getMember(String nickname) {
        String joinId = "testJoinId";
        return getMember(joinId, nickname);
    }
}