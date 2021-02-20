package project.study.jgm.customvocabulary.common.upload.exception;

public class OriginalFilenameNotFoundException extends RuntimeException {
    public OriginalFilenameNotFoundException() {
        super("해당 파일의 파일명을 찾을 수 없습니다. 다시 시도해 주세요.");
    }
}
