package project.study.jgm.customvocabulary.vocabulary.exception;

public class VocabularyNotFoundException extends RuntimeException {
    public VocabularyNotFoundException() {
        super("요청한 단어장을 찾을 수 없습니다.");
    }
}
