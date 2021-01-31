package project.study.jgm.customvocabulary.members.exception;

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException() {
        super("해당 회원을 찾을 수 없습니다.");
    }
}
