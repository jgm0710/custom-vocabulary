package project.study.jgm.customvocabulary.bbs.exception;

public class BbsNotFoundException extends RuntimeException {
    public BbsNotFoundException() {
        super("Bbs not found...");
    }
}
