package project.study.jgm.customvocabulary.members.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.members.Gender;
import project.study.jgm.customvocabulary.members.LoginInfo;
import project.study.jgm.customvocabulary.members.MemberRole;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberUpdateDto {

    @NotBlank(message = "ID를 입력해주세요.")
    private String joinId;

    @NotBlank(message = "본인인증을 위한 비밀번호를 입력해주세요")
    private String password;

    @NotBlank(message = "Email을 입력해주세요.")
    private String email;

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    @NotBlank(message = "Nickname을 입력해주세요.")
    private String nickname;

    private LocalDate dateOfBirth;  //생년월일

    @NotNull(message = "성별을 입력해주세요.")
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
