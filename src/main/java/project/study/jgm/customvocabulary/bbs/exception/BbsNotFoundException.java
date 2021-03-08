package project.study.jgm.customvocabulary.bbs.exception;

public class BbsNotFoundException extends RuntimeException {
    public BbsNotFoundException() {
        super("요청한 게시글을 찾을 수 없습니다.");
    }
}
