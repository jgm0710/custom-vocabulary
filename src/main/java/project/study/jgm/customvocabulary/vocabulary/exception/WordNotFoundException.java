package project.study.jgm.customvocabulary.vocabulary.exception;

public class WordNotFoundException extends RuntimeException {
    public WordNotFoundException() {
        super("Word not found...");
    }
}
