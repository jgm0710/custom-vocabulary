package project.study.jgm.customvocabulary.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import project.study.jgm.customvocabulary.members.Member;

import java.util.Collection;
import java.util.stream.Collectors;

public class AccountAdapter extends User {

    private Member member;

    public AccountAdapter(Member member) {
        super(member.getJoinId(), member.getPassword(), memberToAuthorities(member));
        this.member = member;
    }

    private static Collection<? extends GrantedAuthority> memberToAuthorities(Member member) {
        return member.getRoles().stream().map(memberRole -> new SimpleGrantedAuthority(memberRole.getRoleName())).collect(Collectors.toList());
    }

    public Member getMember() {
        return member;
    }
}
