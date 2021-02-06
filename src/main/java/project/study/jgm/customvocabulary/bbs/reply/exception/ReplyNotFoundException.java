package project.study.jgm.customvocabulary.bbs.reply.exception;

public class ReplyNotFoundException extends RuntimeException{
    public ReplyNotFoundException() {
        super("Reply not found...");
    }
}
