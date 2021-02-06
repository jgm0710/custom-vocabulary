package project.study.jgm.customvocabulary.common.exception;

public class NoExistLikeException extends RuntimeException {
    public NoExistLikeException() {
        super("Like not found...");
    }
}
