package project.study.jgm.customvocabulary.common.exception;

public class ExistLikeException extends RuntimeException {
    public ExistLikeException() {
        super("이미 존재하는 좋아요");
    }
}
