package project.study.jgm.customvocabulary.vocabulary.word.upload.exception;

public class WordImageFileNotFoundException extends RuntimeException {
    public WordImageFileNotFoundException() {
        super("단어에 등록된 이미지 파일을 찾을 수 없습니다.");
    }
}
