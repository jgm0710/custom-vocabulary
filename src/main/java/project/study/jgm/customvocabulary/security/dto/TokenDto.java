package project.study.jgm.customvocabulary.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenDto {

    private Long memberId;

    private String accessToken;

    private long accessTokenExpirationSecond;

    private String refreshToken;

    private LocalDateTime refreshTokenExpirationPeriodDateTime;
}
