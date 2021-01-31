package project.study.jgm.customvocabulary.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@ConfigurationProperties(prefix = "jwt-security")
@Getter
@Setter
public class SecurityProperties {

    private String secretKey;

    private long TokenValidSecond;

    private int expirationCycleDays;

    public long getTokenValidTime() {
        return TokenValidSecond * 1000;
    }
}
