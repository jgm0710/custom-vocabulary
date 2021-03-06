package project.study.jgm.customvocabulary.bbs.reply.like.exception;

public class AddLikeToChildReplyException extends RuntimeException {
    public AddLikeToChildReplyException() {
        super("댓글에 등록된 하위 댓글에는 좋아요를 등록하는 것이 불가능합니다.");
    }
}
