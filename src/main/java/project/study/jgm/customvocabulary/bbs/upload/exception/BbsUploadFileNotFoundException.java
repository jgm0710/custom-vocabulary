package project.study.jgm.customvocabulary.bbs.upload.exception;

public class BbsUploadFileNotFoundException extends RuntimeException {
    public BbsUploadFileNotFoundException() {
        super("게시글에 업로드된 파일을 찾을 수 없습니다.");
    }
}
