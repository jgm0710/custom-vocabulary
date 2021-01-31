package project.study.jgm.customvocabulary.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OnlyTokenDto {

    @NotBlank(message = "refreshToken이 필요합니다.")
    private String refreshToken;
}
