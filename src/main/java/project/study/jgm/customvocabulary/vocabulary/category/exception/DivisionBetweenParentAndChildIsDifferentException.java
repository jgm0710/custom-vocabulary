package project.study.jgm.customvocabulary.vocabulary.category.exception;

public class DivisionBetweenParentAndChildIsDifferentException extends RuntimeException {
    public DivisionBetweenParentAndChildIsDifferentException() {
        super("부모 카테고리와 자식 카테고리 사이의 구분이 다릅니다.");
    }
}
