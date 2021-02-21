package project.study.jgm.customvocabulary.vocabulary.exception;

public class VocabularyThumbnailImageFileNotFoundException extends RuntimeException{
    public VocabularyThumbnailImageFileNotFoundException() {
        super("단어장에 등록된 파일을 찾을 수 없습니다.");
    }
}
