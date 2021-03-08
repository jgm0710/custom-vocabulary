package project.study.jgm.customvocabulary.common.exception;

public class SelfLikeException extends RuntimeException {
    public SelfLikeException() {
        super("자신이 등록한 대상에는 좋아요를 등록할 수 없습니다.");
    }
}
