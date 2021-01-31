package project.study.jgm.customvocabulary.members.exception;

public class RefreshTokenNotFoundException extends RuntimeException {
    public RefreshTokenNotFoundException() {
        super("해당 refresh_token 으로는 회원을 찾을 수 없습니다.");
    }
}
