package project.study.jgm.customvocabulary.common.exception;

public class ExistLikeException extends RuntimeException {
    public ExistLikeException() {
        super("이미 좋아요를 등록한 대상입니다.");
    }
}
