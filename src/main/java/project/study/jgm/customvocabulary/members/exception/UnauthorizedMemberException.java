package project.study.jgm.customvocabulary.members.exception;

public class UnauthorizedMemberException extends RuntimeException {
    public UnauthorizedMemberException() {
        super("해당 사이트에 대한 활동 권한을 가지고 있지 않은 회원입니다.");
    }
}
