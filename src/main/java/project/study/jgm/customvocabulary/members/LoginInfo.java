package project.study.jgm.customvocabulary.members;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.common.SecurityProperties;

import javax.persistence.Embeddable;
import java.time.LocalDateTime;
import java.util.UUID;

@Embeddable
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginInfo {

    private String refreshToken;

    private LocalDateTime refreshTokenExpirationPeriodDateTime;

    public static LoginInfo login(SecurityProperties securityProperties) {
        return LoginInfo.builder()
                .refreshToken(UUID.randomUUID().toString())
                .refreshTokenExpirationPeriodDateTime(LocalDateTime.now().plusDays(securityProperties.getExpirationCycleDays()))
                .build();
    }

    public static LoginInfo initialize(SecurityProperties securityProperties) {
        return LoginInfo.builder()
                .refreshToken(UUID.randomUUID().toString())
                .refreshTokenExpirationPeriodDateTime(LocalDateTime.now().plusDays(securityProperties.getExpirationCycleDays()))
                .build();
    }

    public static LoginInfo deleteInfo() {
        return LoginInfo.builder()
                .refreshToken(null)
                .refreshTokenExpirationPeriodDateTime(null)
                .build();
    }
}
