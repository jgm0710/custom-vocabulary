package project.study.jgm.customvocabulary.vocabulary.exception;

public class VocabularyNotFoundException extends RuntimeException {
    public VocabularyNotFoundException() {
        super("Vocabulary not found...");
    }
}
