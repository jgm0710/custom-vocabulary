package project.study.jgm.customvocabulary.vocabulary.exception;

public class DoNotMoveException extends RuntimeException {
    public DoNotMoveException() {
        super("소속 카테고리가 변경되지 않았습니다. 카테고리를 이동해 주세요.");
    }
}
