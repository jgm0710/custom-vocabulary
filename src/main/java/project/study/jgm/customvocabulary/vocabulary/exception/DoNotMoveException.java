package project.study.jgm.customvocabulary.vocabulary.exception;

public class DoNotMoveException extends RuntimeException {
    public DoNotMoveException() {
        super("카테고리를 이동해 주세요.");
    }
}
