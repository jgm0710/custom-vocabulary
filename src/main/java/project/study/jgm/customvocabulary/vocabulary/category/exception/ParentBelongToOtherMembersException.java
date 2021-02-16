package project.study.jgm.customvocabulary.vocabulary.category.exception;

public class ParentBelongToOtherMembersException extends RuntimeException {
    public ParentBelongToOtherMembersException() {
        super("부모 카테고리가 다른 회원이 카테고리입니다. 해당 카테고리에는 당신이 하위 카테고리를 생성할 수 없습니다.");
    }
}
