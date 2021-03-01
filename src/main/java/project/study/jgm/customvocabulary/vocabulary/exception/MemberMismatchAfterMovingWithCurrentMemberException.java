package project.study.jgm.customvocabulary.vocabulary.exception;

public class MemberMismatchAfterMovingWithCurrentMemberException extends RuntimeException {
    public MemberMismatchAfterMovingWithCurrentMemberException() {
        super("현재 회원과 이동하려는 카테고리의 회원이 다릅니다.");
    }
}
