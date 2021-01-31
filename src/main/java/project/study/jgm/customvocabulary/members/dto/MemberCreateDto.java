package project.study.jgm.customvocabulary.members.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.members.Gender;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberCreateDto {

    @NotBlank(message = "ID를 입력해주세요.")
    private String joinId;

    @NotBlank(message = "Email을 입력해주세요.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

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
//password
//name
//nickname
//dateOfBirth
//gender
//simpleAddress

}
