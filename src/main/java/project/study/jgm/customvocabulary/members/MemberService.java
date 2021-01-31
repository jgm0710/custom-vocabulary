package project.study.jgm.customvocabulary.members;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.study.jgm.customvocabulary.common.SecurityProperties;
import project.study.jgm.customvocabulary.members.dto.MemberCreateDto;
import project.study.jgm.customvocabulary.members.dto.MemberUpdateDto;
import project.study.jgm.customvocabulary.members.exception.MemberNotFoundException;
import project.study.jgm.customvocabulary.members.exception.RefreshTokenExpirationException;
import project.study.jgm.customvocabulary.members.exception.RefreshTokenNotFoundException;
import project.study.jgm.customvocabulary.security.AccountAdapter;
import project.study.jgm.customvocabulary.security.JwtTokenProvider;
import project.study.jgm.customvocabulary.security.LoginService;
import project.study.jgm.customvocabulary.security.dto.TokenDto;
import project.study.jgm.customvocabulary.security.dto.LoginDto;
import project.study.jgm.customvocabulary.security.dto.OnlyTokenDto;
import project.study.jgm.customvocabulary.security.exception.PasswordMismatchException;

import java.time.LocalDateTime;
import java.util.List;
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
        List<MemberRole> roles = List.of(MemberRole.USER);
        Member member = Member.createMember(memberCreateDto, roles, passwordEncoder, securityProperties);
        return memberRepository.save(member);
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
        if (refreshTokenExpirationPeriodDateTime.isAfter(LocalDateTime.now())) {
            throw new RefreshTokenExpirationException();
        }

        return createToken(findMember);
    }

    public Member getMember(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new MemberNotFoundException());
    }

    @Transactional
    public void modifyMember(Long memberId, MemberUpdateDto memberUpdateDto) {
        Member findMember = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        findMember.update(memberUpdateDto);
    }

    @Transactional
    public void secession(Long memberId) {
        Member findMember = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        findMember.secession();
    }

    /**
     * ADMIN
     */
    @Transactional
    public Member adminJoin(MemberCreateDto memberCreateDto) {
        List<MemberRole> roles = List.of(MemberRole.USER, MemberRole.ADMIN);
        Member member = Member.createMember(memberCreateDto, roles, passwordEncoder, securityProperties);
        return memberRepository.save(member);
    }

    public List<Member> getMemberList(Pageable pageable) {
        Page<Member> page = memberRepository.findAll(pageable);
        return page.getContent();
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
