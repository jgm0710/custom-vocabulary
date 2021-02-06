package project.study.jgm.customvocabulary.bbs.reply.exception;

public class DeletedReplyException extends RuntimeException{
    public DeletedReplyException() {
        super("삭제된 댓글 입니다.");
    }

    public DeletedReplyException(String message) {
        super("삭제된 댓글 입니다. : "+message);
    }
}
