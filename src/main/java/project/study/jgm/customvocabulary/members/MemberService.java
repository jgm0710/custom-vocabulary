package project.study.jgm.customvocabulary.members;

import javassist.bytecode.DuplicateMemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.study.jgm.customvocabulary.common.SecurityProperties;
import project.study.jgm.customvocabulary.members.dto.MemberCreateDto;
import project.study.jgm.customvocabulary.members.dto.search.MemberSearchDto;
import project.study.jgm.customvocabulary.members.dto.MemberUpdateDto;
import project.study.jgm.customvocabulary.members.exception.*;
import project.study.jgm.customvocabulary.security.AccountAdapter;
import project.study.jgm.customvocabulary.security.JwtTokenProvider;
import project.study.jgm.customvocabulary.security.LoginService;
import project.study.jgm.customvocabulary.security.dto.TokenDto;
import project.study.jgm.customvocabulary.security.dto.LoginDto;
import project.study.jgm.customvocabulary.security.dto.OnlyTokenDto;
import project.study.jgm.customvocabulary.security.exception.PasswordMismatchException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    private final SecurityProperties securityProperties;

    private final JwtTokenProvider jwtTokenProvider;

    private final LoginService loginService;

    private final MemberQueryRepository memberQueryRepository;

    /**
     * USER
     */
    @Transactional
    public Member userJoin(MemberCreateDto memberCreateDto) {
        checkForDuplicateMembers(memberCreateDto.getJoinId(), memberCreateDto.getNickname());

        List<MemberRole> roles = List.of(MemberRole.USER);
        Member member = Member.createMember(memberCreateDto, roles, passwordEncoder, securityProperties);
        return memberRepository.save(member);
    }

    private void checkForDuplicateMembers(String joinId, String nickname) {
        if (checkExistJoinId(joinId) || checkExistNickname(nickname)) {
            throw new ExistDuplicatedMemberException();
        }
    }

    @Transactional
    public TokenDto login(LoginDto loginDto) {
        AccountAdapter accountAdapter = (AccountAdapter) loginService.loadUserByUsername(loginDto.getJoinId());
        if (!passwordEncoder.matches(loginDto.getPassword(), accountAdapter.getPassword())) {
            throw new PasswordMismatchException();
        }

        Member loginMember = accountAdapter.getMember();
        loginMember.login(securityProperties);

        return createToken(loginMember);
    }

    @Transactional
    public TokenDto refresh(OnlyTokenDto onlyTokenDto) {
        Member findMember = memberQueryRepository.findByRefreshToken(onlyTokenDto.getRefreshToken());
        if (findMember == null) {
            throw new RefreshTokenNotFoundException();
        }

        LocalDateTime refreshTokenExpirationPeriodDateTime = findMember.getLoginInfo().getRefreshTokenExpirationPeriodDateTime();
        if (refreshTokenExpirationPeriodDateTime.isBefore(LocalDateTime.now())) {
            throw new RefreshTokenExpirationException();
        }

        return createToken(findMember);
    }

    @Transactional
    public void logout(Long memberId) {
        Member findMember = memberRepository.findById(memberId).get();
        findMember.logout();
    }

    public Member getMember(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new MemberNotFoundException());
    }

    @Transactional
    public void modifyMember(Long memberId, String password, MemberUpdateDto memberUpdateDto) {
        Member findMember = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        if (!findMember.matches(password, passwordEncoder)) {
            throw new PasswordMismatchException();
        }

        if (findMember.getJoinId() != memberUpdateDto.getJoinId()) {
            if (checkExistJoinId(memberUpdateDto.getJoinId())) {
                throw new ExistDuplicatedMemberException();
            }
        }

        if (findMember.getNickname() != memberUpdateDto.getNickname()) {
            if (checkExistNickname(memberUpdateDto.getNickname())) {
                throw new ExistDuplicatedMemberException();
            }
        }

        findMember.update(memberUpdateDto);
    }

    @Transactional
    public void updatePassword(Long memberId, String oldPassword, String newPassword) {
        Member findMember = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        if (!findMember.matches(oldPassword, passwordEncoder)) {
            throw new PasswordMismatchException();
        }

        findMember.updatePassword(newPassword, passwordEncoder);
    }

    @Transactional
    public void secession(Long memberId, String password) {
        Member findMember = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        boolean matches = findMember.matches(password, passwordEncoder);
        if (matches == false) {
            throw new PasswordMismatchException();
        }
        findMember.secession();
    }

    /**
     * Common
     */
    public boolean checkExistJoinId(String joinId) {
        Member member = memberRepository.findByJoinId(joinId).orElse(null);
        return member != null;
    }

    public boolean checkExistNickname(String nickname) {
        Member member = memberRepository.findByNickname(nickname).orElse(null);
        return member != null;
    }

    /**
     * ADMIN
     */
    @Transactional
    public Member adminJoin(MemberCreateDto memberCreateDto) {
        checkForDuplicateMembers(memberCreateDto.getJoinId(), memberCreateDto.getNickname());

        List<MemberRole> roles = List.of(MemberRole.USER, MemberRole.ADMIN);
        Member member = Member.createMember(memberCreateDto, roles, passwordEncoder, securityProperties);
        return memberRepository.save(member);
    }

    public List<Member> getMemberList(MemberSearchDto memberSearchDto) {
        return memberQueryRepository.findAll(memberSearchDto);
    }

    public Long getTotalCount(MemberSearchDto memberSearchDto) {
        return memberQueryRepository.findTotalCount(memberSearchDto);
    }

    @Transactional
    public void ban(Long memberId) {
        Member findMember = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        findMember.ban();
    }


    @Transactional
    public void changeMemberRoleToUser(Long memberId) {
        Member findMember = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);

        if (!findMember.getRoles().contains(MemberRole.SECESSION) && !findMember.getRoles().contains(MemberRole.BAN)) {
            throw new MemberAlreadyHasAuthorityException();
        }

        findMember.changeMemberRoleToUser();
    }


    /**
     * PRIVATE
     */

    private TokenDto createToken(Member findMember) {
        List<String> roles = findMember.getRoles().stream().map(memberRole -> memberRole.getRoleName()).collect(Collectors.toList());
        String accessToken = jwtTokenProvider.createToken(findMember.getJoinId(), roles);
        return TokenDto.builder()
                .accessToken(accessToken)
                .accessTokenExpirationSecond(securityProperties.getTokenValidSecond())
                .refreshToken(findMember.getLoginInfo().getRefreshToken())
                .refreshTokenExpirationPeriodDateTime(findMember.getLoginInfo().getRefreshTokenExpirationPeriodDateTime())
                .build();
    }
}
