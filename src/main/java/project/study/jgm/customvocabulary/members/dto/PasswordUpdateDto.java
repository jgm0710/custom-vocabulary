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
public class PasswordUpdateDto {

    @NotBlank
    private String oldPassword;

    @NotBlank
    private String newPassword;

}
