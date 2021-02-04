package project.study.jgm.customvocabulary.members.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.members.Gender;
import project.study.jgm.customvocabulary.members.LoginInfo;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRole;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberAdminViewDto {
    private Long id;

    private String joinId;

    private String email;

    private String name;

    private String nickname;

    private LocalDate dateOfBirth;  //생년월일

    private Gender gender;  //성별 [MALE,FEMALE]

    private String simpleAddress;

    private int sharedVocabularyCount; //몇개의 공유 단어장을 가지고 있는지 저장

    private int bbsCount;   //몇개의 게시글을 장성했는지 저장

    @Builder.Default
    private List<MemberRole> roles = new ArrayList<>();    //회원 상태 [MEMBER, ADMIN, BAN, SECESSION]

    private LocalDateTime registerDate; //가입 일시

    private LocalDateTime updateDate;   //업데이트 일시

//id
//joinId
//email
//name
//nickname
//dateOfBirth
//gender
//simpleAddress
//sharedVocabularyCount
//bbsCount
//roles
//registerDate
//updateDate

    public static MemberAdminViewDto memberToAdminView(Member member) {
        return MemberAdminViewDto.builder()
                .id(member.getId())
                .joinId(member.getJoinId())
                .email(member.getEmail())
                .name(member.getName())
                .nickname(member.getNickname())
                .dateOfBirth(member.getDateOfBirth())
                .gender(member.getGender())
                .simpleAddress(member.getSimpleAddress())
                .sharedVocabularyCount(member.getSharedVocabularyCount())
                .bbsCount(member.getBbsCount())
                .roles(member.getRoles())
                .registerDate(member.getRegisterDate())
                .updateDate(member.getUpdateDate())
                .build();
    }
}
