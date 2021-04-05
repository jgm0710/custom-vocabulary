package project.study.jgm.customvocabulary.members.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OnlyConfirmPasswordDto {

    @NotBlank(message = "본인확인을 위한 비밀번호를 입력해주세요.")
    private String password;

}
