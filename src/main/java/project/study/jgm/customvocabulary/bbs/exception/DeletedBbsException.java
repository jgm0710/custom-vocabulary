package project.study.jgm.customvocabulary.bbs.exception;

public class DeletedBbsException extends RuntimeException {
    public DeletedBbsException() {
        super("삭제된 게시글 입니다.");
    }

    public DeletedBbsException(String message) {
        super("삭제된 게시글 입니다. : " + message);
    }
}
