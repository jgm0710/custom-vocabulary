package project.study.jgm.customvocabulary.members.exception;

public class MemberAlreadyHasAuthorityException extends RuntimeException {

    public MemberAlreadyHasAuthorityException() {
        super("회원이 이미 권한을 가지고 있습니다.");
    }
}
