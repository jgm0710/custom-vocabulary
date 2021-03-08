package project.study.jgm.customvocabulary.bbs.reply.exception;

public class ReplyNotFoundException extends RuntimeException{
    public ReplyNotFoundException() {
        super("요청한 댓글을 찾을 수 없습니다.");
    }
}
