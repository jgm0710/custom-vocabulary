package project.study.jgm.customvocabulary.common.upload.exception;

public class NotImageTypeException extends RuntimeException {
    public NotImageTypeException() {
        super("단어장과 단어에는 Image file 만 등록할 수 있습니다.");
    }
}
