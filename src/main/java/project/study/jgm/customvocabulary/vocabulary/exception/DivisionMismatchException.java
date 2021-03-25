package project.study.jgm.customvocabulary.vocabulary.exception;

public class DivisionMismatchException extends RuntimeException {
    public DivisionMismatchException() {
        super("카테고리와 단어장의 구분이 일치하지 않습니다.");
    }

    public DivisionMismatchException(String message) {
        super("카테고리와 단어장의 구분이 일치하지 않습니다. : " + message);
    }
}
