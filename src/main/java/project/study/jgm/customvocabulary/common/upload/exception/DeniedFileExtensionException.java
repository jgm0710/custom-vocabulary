package project.study.jgm.customvocabulary.common.upload.exception;

public class DeniedFileExtensionException extends RuntimeException {
    public DeniedFileExtensionException() {
        super("해당 파일의 확장자는 업로드할 수 없습니다.");
    }
}
