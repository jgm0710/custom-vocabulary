package project.study.jgm.customvocabulary.members.exception;

public class RefreshTokenExpirationException extends RuntimeException {
    public RefreshTokenExpirationException() {
        super("refresh_token 의 기간이 만료되었습니다.");
    }
}
