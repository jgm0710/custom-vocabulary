package project.study.jgm.customvocabulary.common.exception;

public class SelfLikeException extends RuntimeException {
    public SelfLikeException() {
        super("자기 자신은 좋아요를 누를 수 없습니다.");
    }
}
