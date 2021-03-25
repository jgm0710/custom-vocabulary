package project.study.jgm.customvocabulary.vocabulary.exception;

public class WordNotFoundException extends RuntimeException {
    public WordNotFoundException() {
        super("요청한 단어를 찾을 수 없습니다.");
    }
}
