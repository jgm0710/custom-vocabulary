package project.study.jgm.customvocabulary.vocabulary.word.upload.exception;

public class NotImageTypeException extends RuntimeException {
    public NotImageTypeException() {
        super("단어장에는 Image file 만 등록할 수 있습니다.");
    }
}
