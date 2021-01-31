package project.study.jgm.customvocabulary.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import project.study.jgm.customvocabulary.api.LoginApiController;
import project.study.jgm.customvocabulary.common.SecurityProperties;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRepository;
import project.study.jgm.customvocabulary.security.dto.LoginDto;
import project.study.jgm.customvocabulary.security.exception.PasswordMismatchException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoginService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String joinId) throws UsernameNotFoundException {
        Member member = memberRepository.findByJoinId(joinId).orElseThrow(() -> new UsernameNotFoundException("해당 아이디의 사용자가 없습니다. ID : " + joinId));
        return new AccountAdapter(member);
    }
}
