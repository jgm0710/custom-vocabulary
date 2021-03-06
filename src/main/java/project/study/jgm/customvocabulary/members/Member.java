package project.study.jgm.customvocabulary.members;

import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import project.study.jgm.customvocabulary.common.SecurityProperties;
import project.study.jgm.customvocabulary.members.dto.MemberCreateDto;
import project.study.jgm.customvocabulary.members.dto.MemberUpdateDto;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(unique = true)
    private String joinId;

    private String email;

    private String password;

    private String name;

    @Column(unique = true)
    private String nickname;

    private LocalDate dateOfBirth;  //생년월일

    @Enumerated(EnumType.STRING)
    private Gender gender;  //성별 [MALE,FEMALE]

    private String simpleAddress;

    private int sharedVocabularyCount; //몇개의 공유 단어장을 가지고 있는지 저장

    private int bbsCount;   //몇개의 게시글을 장성했는지 저장

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private List<MemberRole> roles = new ArrayList<>();    //회원 상태 [MEMBER, ADMIN, BAN, SECESSION]

    @Embedded
    private LoginInfo loginInfo;

    private LocalDateTime registerDate; //가입 일시

    private LocalDateTime updateDate;   //업데이트 일시

//id
//joinId
//email
//password
//name
//nickname
//dateOfBirth
//gender
//simpleAddress
//sharedVocabularyCount
//bbsCount
//roles
//loginInfo
//registerDate
//updateDate

    public static Member createMember(MemberCreateDto memberCreateDto, List<MemberRole> roles, PasswordEncoder passwordEncoder, SecurityProperties securityProperties) {
        String encodedPassword = passwordEncoder.encode(memberCreateDto.getPassword());

        return Member.builder()
                .joinId(memberCreateDto.getJoinId())
                .email(memberCreateDto.getEmail())
                .password(encodedPassword)
                .name(memberCreateDto.getName())
                .nickname(memberCreateDto.getNickname())
                .dateOfBirth(memberCreateDto.getDateOfBirth())
                .gender(memberCreateDto.getGender())
                .simpleAddress(memberCreateDto.getSimpleAddress())
                .sharedVocabularyCount(0)
                .bbsCount(0)
                .roles(roles)
                .loginInfo(LoginInfo.initialize(securityProperties))
                .registerDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
    }

    public void login(SecurityProperties securityProperties) {
        this.loginInfo = LoginInfo.login(securityProperties);
    }

    public void update(MemberUpdateDto memberUpdateDto) {
        this.joinId = memberUpdateDto.getJoinId();
        this.email = memberUpdateDto.getEmail();
        this.name = memberUpdateDto.getName();
        this.nickname = memberUpdateDto.getNickname();
        this.dateOfBirth = memberUpdateDto.getDateOfBirth();
        this.gender = memberUpdateDto.getGender();
        this.simpleAddress = memberUpdateDto.getSimpleAddress();

        this.updateDate = LocalDateTime.now();
    }

    public void secession() {
        this.roles = List.of(MemberRole.SECESSION);

        this.loginInfo = LoginInfo.deleteInfo();
    }

    public void ban() {
        this.roles = List.of(MemberRole.BAN);

        this.loginInfo = LoginInfo.deleteInfo();
    }

    public boolean matches(String password, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(password, this.password);
    }

    public void updatePassword(String newPassword, PasswordEncoder passwordEncoder) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        this.password = encodedPassword;
        this.updateDate = LocalDateTime.now();

        this.loginInfo = LoginInfo.deleteInfo();
    }

    public void changeMemberRoleToUser() {
        this.roles = List.of(MemberRole.USER);
    }

    public void logout() {
        this.loginInfo = LoginInfo.deleteInfo();
    }

    public void addSharedVocabulary() {
        this.sharedVocabularyCount++;
    }

    public void deleteSharedVocabulary() {
        this.sharedVocabularyCount--;
    }

    public void addBbs() {
        this.bbsCount++;
    }

    public void deleteBbs() {
        this.bbsCount--;
    }

    public boolean checkAuthorized() {
        return !this.roles.contains(MemberRole.SECESSION) && !this.roles.contains(MemberRole.BAN);
    }
}
