package project.study.jgm.customvocabulary.members.exception;

public class ExistDuplicatedMemberException extends RuntimeException {
    public ExistDuplicatedMemberException() {
        super("ID, Email, Nickname 중 중복된 회원이 있습니다.");
    }
}
