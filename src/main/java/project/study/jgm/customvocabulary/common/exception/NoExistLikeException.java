package project.study.jgm.customvocabulary.common.exception;

public class NoExistLikeException extends RuntimeException {
    public NoExistLikeException() {
        super("해당 회원이 이 대상에 등록한 좋아요를 찾을 수 없습니다.");
    }
}
