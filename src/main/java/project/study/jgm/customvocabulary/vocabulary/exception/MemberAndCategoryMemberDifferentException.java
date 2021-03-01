package project.study.jgm.customvocabulary.vocabulary.exception;

public class MemberAndCategoryMemberDifferentException extends RuntimeException {
    public MemberAndCategoryMemberDifferentException() {
        super("해당 기능을 사용하는 회원과 카테고리를 소유한 회원이 다릅니다.");
    }
}
