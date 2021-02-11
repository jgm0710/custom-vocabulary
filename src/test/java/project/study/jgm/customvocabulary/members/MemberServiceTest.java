package project.study.jgm.customvocabulary.members;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import project.study.jgm.customvocabulary.common.dto.CriteriaDto;
import project.study.jgm.customvocabulary.common.SecurityProperties;
import project.study.jgm.customvocabulary.members.dto.*;
import project.study.jgm.customvocabulary.members.dto.search.MemberSearchDto;
import project.study.jgm.customvocabulary.members.dto.search.MemberSearchType;
import project.study.jgm.customvocabulary.members.dto.search.MemberSortType;
import project.study.jgm.customvocabulary.members.exception.MemberNotFoundException;
import project.study.jgm.customvocabulary.members.exception.RefreshTokenNotFoundException;
import project.study.jgm.customvocabulary.security.dto.LoginDto;
import project.study.jgm.customvocabulary.security.dto.OnlyTokenDto;
import project.study.jgm.customvocabulary.security.dto.TokenDto;
import project.study.jgm.customvocabulary.security.exception.PasswordMismatchException;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberServiceTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberService memberService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    SecurityProperties securityProperties;

    @Autowired
    EntityManager em;

    @BeforeEach
    public void setUp() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("user 회원 가입")
    void userJoin() {
        MemberCreateDto memberCreateDto = getMemberCreateDto();

        Member joinMember = memberService.userJoin(memberCreateDto);

        assertEquals(memberCreateDto.getJoinId(), joinMember.getJoinId());
        assertEquals(memberCreateDto.getEmail(), joinMember.getEmail());
        assertEquals(memberCreateDto.getName(), joinMember.getName());
        assertEquals(memberCreateDto.getNickname(), joinMember.getNickname());
        assertEquals(memberCreateDto.getDateOfBirth(), joinMember.getDateOfBirth());
        assertEquals(memberCreateDto.getGender(), joinMember.getGender());
        assertEquals(memberCreateDto.getSimpleAddress(), joinMember.getSimpleAddress());

        assertTrue(passwordEncoder.matches(memberCreateDto.getPassword(), joinMember.getPassword()));
        assertTrue(joinMember.getRoles().contains(MemberRole.USER));
        assertTrue(!joinMember.getRoles().contains(MemberRole.ADMIN));
        assertTrue(!joinMember.getRoles().contains(MemberRole.SECESSION));
        assertTrue(!joinMember.getRoles().contains(MemberRole.BAN));

        assertEquals(joinMember.getSharedVocabularyCount(), 0);
        assertEquals(joinMember.getBbsCount(), 0);
        assertNotNull(joinMember.getLoginInfo().getRefreshToken());
        assertNotNull(joinMember.getLoginInfo().getRefreshTokenExpirationPeriodDateTime());

    }

    private MemberCreateDto getMemberCreateDto() {
        MemberCreateDto memberCreateDto = MemberCreateDto.builder()
                .joinId("test")
                .email("test@email.com")
                .password("test")
                .name("test")
                .nickname("test")
                .dateOfBirth(LocalDate.now())
                .gender(Gender.MALE)
                .simpleAddress("test address")
                .build();
        return memberCreateDto;
    }

    @Test
    @DisplayName("로그인")
    void login() {
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member joinMember = memberService.userJoin(memberCreateDto);

        LoginDto loginDto = getLoginDto(memberCreateDto);

        TokenDto tokenDto = memberService.login(loginDto);

        em.flush();
        em.clear();

        Member findMember = memberRepository.findById(joinMember.getId()).orElseThrow(MemberNotFoundException::new);

        assertNotNull(tokenDto.getAccessToken());
        assertEquals(tokenDto.getAccessTokenExpirationSecond(), securityProperties.getTokenValidSecond());
        assertEquals(tokenDto.getRefreshToken(), findMember.getLoginInfo().getRefreshToken());
//        assertEquals(tokenDto.getRefreshTokenExpirationPeriodDateTime(), findMember.getLoginInfo().getRefreshTokenExpirationPeriodDateTime());
    }


    @Test
    @DisplayName("로그인 할 때 회원 정보가 없는 경우")
    public void login_Not_Found() throws Exception{
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        memberService.userJoin(memberCreateDto);
        LoginDto loginDto = getLoginDto(memberCreateDto);
        loginDto.setJoinId("fdafdsa");
        //when

        //then
        assertThrows(UsernameNotFoundException.class, () -> memberService.login(loginDto));
    }

    @Test
    @DisplayName("로그인 할 때 비밀번호가 일치하지 않는 경우")
    public void login_Password_Mismatch() throws Exception{
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        memberService.userJoin(memberCreateDto);
        LoginDto loginDto = getLoginDto(memberCreateDto);
        loginDto.setPassword("jkfldfdajlk");
        //when

        //then
        assertThrows(PasswordMismatchException.class, () -> memberService.login(loginDto));
    }

    private LoginDto getLoginDto(MemberCreateDto memberCreateDto) {
        LoginDto loginDto = new LoginDto();
        loginDto.setJoinId(memberCreateDto.getJoinId());
        loginDto.setPassword(memberCreateDto.getPassword());
        return loginDto;
    }

    @Test
    @DisplayName("refresh token 을 통한 로그인")
    void refresh() {
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member joinMember = memberService.userJoin(memberCreateDto);
        LoginDto loginDto = getLoginDto(memberCreateDto);

        TokenDto tokenDto = memberService.login(loginDto);

        TokenDto tokenDto1 = memberService.refresh(new OnlyTokenDto(tokenDto.getRefreshToken()));

        assertNotNull(tokenDto1.getAccessToken());

        assertNotNull(tokenDto1.getAccessToken());
        assertEquals(tokenDto.getAccessTokenExpirationSecond(), tokenDto1.getAccessTokenExpirationSecond());
        assertEquals(tokenDto.getRefreshToken(), tokenDto1.getRefreshToken());
        assertEquals(tokenDto.getRefreshTokenExpirationPeriodDateTime(), tokenDto1.getRefreshTokenExpirationPeriodDateTime());
    }
    
    @Test
    @DisplayName("refresh token 으로 로그인 할 경우 refresh token 이 유효하지 않을 경우")
    public void refresh_Wrong() throws Exception{
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        memberService.userJoin(memberCreateDto);
        LoginDto loginDto = getLoginDto(memberCreateDto);
        TokenDto tokenDto = memberService.login(loginDto);

        //when

        //then
        assertThrows(RefreshTokenNotFoundException.class, () -> memberService.refresh(new OnlyTokenDto("fdafda")));
    }

    @Test
    @DisplayName("회원 1건 조회")
    void getMember() {
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member joinMember = memberService.userJoin(memberCreateDto);

        em.flush();
        em.clear();

        Member findMember = memberService.getMember(joinMember.getId());

        assertEquals(findMember, joinMember);
        assertEquals(findMember.getId(), joinMember.getId());
        assertEquals(findMember.getJoinId(), joinMember.getJoinId());
        assertEquals(findMember.getEmail(), joinMember.getEmail());
        assertEquals(findMember.getPassword(), joinMember.getPassword());
        assertEquals(findMember.getName(), joinMember.getName());
        assertEquals(findMember.getNickname(), joinMember.getNickname());
        assertEquals(findMember.getDateOfBirth(), joinMember.getDateOfBirth());
        assertEquals(findMember.getGender(), joinMember.getGender());
        assertEquals(findMember.getSimpleAddress(), joinMember.getSimpleAddress());
        assertEquals(findMember.getSharedVocabularyCount(), joinMember.getSharedVocabularyCount());
        assertEquals(findMember.getBbsCount(), joinMember.getBbsCount());
        assertEquals(findMember.getLoginInfo().getRefreshToken(), joinMember.getLoginInfo().getRefreshToken());
//        assertEquals(findMember.getLoginInfo().getRefreshTokenExpirationPeriodDateTime(), joinMember.getLoginInfo().getRefreshTokenExpirationPeriodDateTime());
//        assertEquals(findMember.getRegisterDate(), joinMember.getRegisterDate());
//        assertEquals(findMember.getUpdateDate(), joinMember.getUpdateDate());

        for (MemberRole role : findMember.getRoles()) {
            boolean contains = joinMember.getRoles().contains(role);
            assertTrue(contains);
        }

        for (MemberRole role : joinMember.getRoles()) {
            boolean contains = findMember.getRoles().contains(role);
            assertTrue(contains);
        }
    }

    @Test
    @DisplayName("회원 조회시 조회하려는 회원이 없는 경우")
    public void getMember_Not_Found() throws Exception {
        //given

        //when

        //then
        assertThrows(MemberNotFoundException.class, () -> memberService.getMember(1000L));

    }

    @Test
    @DisplayName("회원 수정")
    void modifyMember() {
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member joinMember = memberService.userJoin(memberCreateDto);

        MemberUpdateDto memberUpdateDto = getMemberUpdateDto();

        memberService.modifyMember(joinMember.getId(), memberCreateDto.getPassword(), memberUpdateDto);

        em.flush();
        em.clear();

        Member findMember = memberService.getMember(joinMember.getId());

        assertNotEquals(findMember.getJoinId(), memberCreateDto.getJoinId());
        assertNotEquals(findMember.getEmail(), memberCreateDto.getEmail());
        assertNotEquals(findMember.getPassword(), memberCreateDto.getPassword());
        assertNotEquals(findMember.getName(), memberCreateDto.getName());
        assertNotEquals(findMember.getNickname(), memberCreateDto.getNickname());
        assertNotEquals(findMember.getDateOfBirth(), memberCreateDto.getDateOfBirth());
        assertNotEquals(findMember.getGender(), memberCreateDto.getGender());
//     dkssudgktpdy ekduddlqslek
        assertNotEquals(findMember.getSimpleAddress(), memberCreateDto.getSimpleAddress());


        assertEquals(findMember.getJoinId(), memberUpdateDto.getJoinId());
        assertEquals(findMember.getEmail(), memberUpdateDto.getEmail());
        assertEquals(findMember.getName(), memberUpdateDto.getName());
        assertEquals(findMember.getNickname(), memberUpdateDto.getNickname());
        assertEquals(findMember.getDateOfBirth(), memberUpdateDto.getDateOfBirth());
        assertEquals(findMember.getGender(), memberUpdateDto.getGender());
        assertEquals(findMember.getSimpleAddress(), memberUpdateDto.getSimpleAddress());
    }

    private MemberUpdateDto getMemberUpdateDto() {
        MemberUpdateDto memberUpdateDto = MemberUpdateDto.builder()
                .joinId("update")
                .email("update")
                .name("update")
                .nickname("update")
                .dateOfBirth(LocalDate.of(1996, 11, 8))
                .gender(Gender.FEMALE)
                .simpleAddress("update AD")
                .build();
        return memberUpdateDto;
    }

    @Test
    @DisplayName("회원 수정 시 수정할 회원이 없는 경우")
    public void modifyMember_Not_Found() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member joinMember = memberService.userJoin(memberCreateDto);

        MemberUpdateDto memberUpdateDto = getMemberUpdateDto();


        //when

        //then
        assertThrows(MemberNotFoundException.class, () -> memberService.modifyMember(3000L, memberCreateDto.getPassword(), memberUpdateDto));
    }

    @Test
    @DisplayName("회원 탈퇴")
    void secession() {
        Member joinMember = memberService.userJoin(getMemberCreateDto());

        memberService.secession(joinMember.getId());

        em.flush();
        em.clear();

        Member findMember = memberService.getMember(joinMember.getId());

        assertTrue(findMember.getRoles().contains(MemberRole.SECESSION));
        assertFalse(findMember.getRoles().contains(MemberRole.USER));
        assertFalse(findMember.getRoles().contains(MemberRole.ADMIN));
        assertFalse(findMember.getRoles().contains(MemberRole.BAN));
    }

    @Test
    @DisplayName("회원 탈퇴 시 탈퇴할 회원이 없는 경우")
    public void secession_Not_Found() throws Exception {
        //given

        //when

        //then
        assertThrows(MemberNotFoundException.class, () -> memberService.secession(3000L));

    }
    /**
     * ADMIN
     */

    @Test
    @DisplayName("관리자 회원 가입")
    void adminJoin() {
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member joinMember = memberService.adminJoin(memberCreateDto);


        assertEquals(memberCreateDto.getJoinId(), joinMember.getJoinId());
        assertEquals(memberCreateDto.getEmail(), joinMember.getEmail());
        assertEquals(memberCreateDto.getName(), joinMember.getName());
        assertEquals(memberCreateDto.getNickname(), joinMember.getNickname());
        assertEquals(memberCreateDto.getDateOfBirth(), joinMember.getDateOfBirth());
        assertEquals(memberCreateDto.getGender(), joinMember.getGender());
        assertEquals(memberCreateDto.getSimpleAddress(), joinMember.getSimpleAddress());

        assertTrue(passwordEncoder.matches(memberCreateDto.getPassword(), joinMember.getPassword()));
        assertTrue(joinMember.getRoles().contains(MemberRole.USER));
        assertTrue(joinMember.getRoles().contains(MemberRole.ADMIN));
        assertFalse(joinMember.getRoles().contains(MemberRole.SECESSION));
        assertFalse(joinMember.getRoles().contains(MemberRole.BAN));

        assertEquals(joinMember.getSharedVocabularyCount(), 0);
        assertEquals(joinMember.getBbsCount(), 0);
        assertNotNull(joinMember.getLoginInfo().getRefreshToken());
        assertNotNull(joinMember.getLoginInfo().getRefreshTokenExpirationPeriodDateTime());


    }

    @ParameterizedTest
    @MethodSource("paramsForGetMemberListTest")
    @DisplayName("회원 목록 조회")
    void getMemberList(MemberSearchType searchType, String content, MemberSortType sortType) {

        //given
        createMemberList();

        em.flush();
        em.clear();


        MemberSearchDto memberSearchDto = MemberSearchDto.builder()
                .criteriaDto(new CriteriaDto(1, 40))
                .keyword(content)
                .searchType(searchType)
                .sortType(sortType)
                .build();

        //when
        List<Member> memberList = memberService.getMemberList(memberSearchDto);

        //then
        if (searchType == MemberSearchType.JOIN_ID) {
            memberList.forEach(member -> {
                assertTrue(member.getJoinId().contains(content));
            });
        } else if (searchType == MemberSearchType.EMAIL) {
            memberList.forEach(member -> {
                assertTrue( member.getEmail().contains(content));
            });
        } else if (searchType == MemberSearchType.NAME) {
            memberList.forEach(member -> {
                assertTrue(member.getName().contains(content));
            });
        } else {    //searchType == MemberSearchType.NICKNAME
            memberList.forEach(member -> {
                assertTrue(member.getNickname().contains(content));
            });
        }

        boolean sortFlag = true;
        if (sortType == MemberSortType.LATEST) {
            var tmp = 1000000L;

            for (Member member : memberList) {
                if (member.getId() <= tmp) {
                    tmp = member.getId();
                } else {
                    sortFlag = false;
                    break;
                }
            }
        }else if (sortType == MemberSortType.OLDEST) {
            var tmp = 0L;

            for (Member member : memberList) {
                if (member.getId() >= tmp) {
                    tmp = member.getId();
                } else {
                    sortFlag = false;
                    break;
                }
            }
        }else if (sortType == MemberSortType.BBS_COUNT_DESC) {
            var tmp = 10000;

            for (Member member : memberList) {
                if (member.getBbsCount() <= tmp) {
                    tmp = member.getBbsCount();
                } else {
                    sortFlag = false;
                    break;
                }
            }
        }else if (sortType == MemberSortType.BBS_COUNT_ASC) {
            var tmp = 0;

            for (Member member : memberList) {
                if (member.getBbsCount() >= tmp) {
                    tmp = member.getBbsCount();
                } else {
                    sortFlag = false;
                    break;
                }
            }
        } else if (sortType == MemberSortType.SHARED_VOCABULARY_COUNT_DESC) {
            var tmp = 10000;

            for (Member member : memberList) {
                if (member.getSharedVocabularyCount() <= tmp) {
                    tmp = member.getSharedVocabularyCount();
                } else {
                    sortFlag = false;
                    break;
                }
            }
        } else {    //sortType == MemberSortType.SHARED_VOCABULARY_COUNT_ASC
            var tmp = 0;

            for (Member member : memberList) {
                if (member.getSharedVocabularyCount() >= tmp) {
                    tmp = member.getSharedVocabularyCount();
                } else {
                    sortFlag = false;
                    break;
                }
            }
        }

        memberList.forEach(member -> System.out.println("member = " + member));
        assertTrue(sortFlag);
    }

    private void createMemberList() {
        for (int i = 0; i < 100; i++) {
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

    static Stream<Arguments> paramsForGetMemberListTest() {
        return Stream.of(
                Arguments.of(MemberSearchType.JOIN_ID, "joinId7", MemberSortType.LATEST),
                Arguments.of(MemberSearchType.EMAIL, "user7", MemberSortType.OLDEST),
                Arguments.of(MemberSearchType.NAME, "name7", MemberSortType.BBS_COUNT_DESC),
                Arguments.of(MemberSearchType.NICKNAME, "nickname7", MemberSortType.BBS_COUNT_ASC),
                Arguments.of(MemberSearchType.JOIN_ID, "joinId7", MemberSortType.SHARED_VOCABULARY_COUNT_DESC),
                Arguments.of(MemberSearchType.JOIN_ID, "joinId7", MemberSortType.SHARED_VOCABULARY_COUNT_ASC)
        );
    }

    @Test
    @DisplayName("회원 활동 금지")
    void ban() {
        Member joinMember = memberService.userJoin(getMemberCreateDto());

        em.flush();
        em.clear();

        memberService.ban(joinMember.getId());

        em.flush();
        em.clear();

        Member findMember = memberService.getMember(joinMember.getId());

        assertTrue(findMember.getRoles().contains(MemberRole.BAN));
        assertFalse(findMember.getRoles().contains(MemberRole.USER));
        assertFalse(findMember.getRoles().contains(MemberRole.ADMIN));
        assertFalse(findMember.getRoles().contains(MemberRole.SECESSION));

        assertNull(findMember.getLoginInfo());
    }

    @Test
    @DisplayName("비밀번호 변경")
    public void updatePassword() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        String newPassword = "newPassword";

        em.flush();
        em.clear();
        //when
        memberService.updatePassword(userMember.getId(), memberCreateDto.getPassword(), newPassword);

        //then
        Member findMember = memberService.getMember(userMember.getId());

        boolean matches = passwordEncoder.matches(newPassword, findMember.getPassword());
        assertTrue(matches);
    }

    @Test
    @DisplayName("비밀번호 변경 시 변경할 회원이 없는 경우")
    public void updatePassword_Not_Found() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        String newPassword = "newPassword";

        em.flush();
        em.clear();
        //when

        //then
        assertThrows(MemberNotFoundException.class, () -> memberService.updatePassword(3000L, memberCreateDto.getPassword(), newPassword));
    }

    @Test
    @DisplayName("비밀번호를 변경할 때 oldPassword 를 틀린 경우")
    public void updatePassword_Password_Mismatch() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto();
        Member userMember = memberService.userJoin(memberCreateDto);

        String newPassword = "newPassword";

        em.flush();
        em.clear();
        //when

        //then
        assertThrows(PasswordMismatchException.class, () -> memberService.updatePassword(userMember.getId(), "fdasfdasfsafdas", newPassword));
    }
}