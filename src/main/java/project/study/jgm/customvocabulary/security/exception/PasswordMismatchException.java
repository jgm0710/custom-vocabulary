package project.study.jgm.customvocabulary.security.exception;

public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException() {
        super("비밀번호가 일치하지 않습니다. 로그인 비밀번호를 다시 확인해주세요.");
    }
}
