package project.study.jgm.customvocabulary.security.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginDto {

    @NotBlank(message = "로그인 ID를 입력해주세요.")
    private String joinId;

    @NotBlank(message = "로그인 비밀번호를 입력해주세요.")
    private String password;

}
