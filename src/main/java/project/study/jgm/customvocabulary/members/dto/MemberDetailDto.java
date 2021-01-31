package project.study.jgm.customvocabulary.members.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.members.Gender;
import project.study.jgm.customvocabulary.members.LoginInfo;
import project.study.jgm.customvocabulary.members.Member;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberDetailDto {
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

    private LoginInfo loginInfo;

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
//loginInfo
//registerDate
//updateDate


    public static MemberDetailDto memberToDetail(Member member) {
        return MemberDetailDto.builder()
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
                .loginInfo(member.getLoginInfo())
                .registerDate(member.getRegisterDate())
                .updateDate(member.getUpdateDate())
                .build();
    }

}
