package project.study.jgm.customvocabulary.vocabulary.exception;

public class DivisionMismatchException extends RuntimeException {
    public DivisionMismatchException() {
        super("Category와 Vocabulary의 구분이 일치하지 않습니다.");
    }

    public DivisionMismatchException(String message) {
        super("Category와 Vocabulary의 구분이 일치하지 않습니다. : " + message);
    }
}
