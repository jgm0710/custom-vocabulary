package project.study.jgm.customvocabulary.members.exception;

public class ExistDuplicatedMemberException extends RuntimeException {
    public ExistDuplicatedMemberException() {
        super("ID 나 Nickname 중 중복된 회원이 있습니다.");
    }
}
