package project.study.jgm.customvocabulary.vocabulary.exception;

public class BadRequestByDivision extends RuntimeException {
    public BadRequestByDivision() {
        super("구분이 잘못된 요청입니다.");
    }

    public BadRequestByDivision(String message) {
        super("구분이 잘못된 요청입니다. : " + message);
    }
}
