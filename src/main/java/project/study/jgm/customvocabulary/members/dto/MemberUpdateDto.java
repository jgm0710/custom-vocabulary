package project.study.jgm.customvocabulary.members.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.members.Gender;
import project.study.jgm.customvocabulary.members.LoginInfo;
import project.study.jgm.customvocabulary.members.MemberRole;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberUpdateDto {

    private String joinId;

    private String email;

    private String name;

    private String nickname;

    private LocalDate dateOfBirth;  //생년월일

    private Gender gender;  //성별 [MALE,FEMALE]

    private String simpleAddress;

//joinId
//email
//name
//nickname
//dateOfBirth
//gender
//simpleAddress

}
