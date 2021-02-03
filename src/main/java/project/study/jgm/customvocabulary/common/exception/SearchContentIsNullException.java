package project.study.jgm.customvocabulary.common.exception;

public class SearchContentIsNullException extends RuntimeException {
    public SearchContentIsNullException() {
        super("검색 내용은 Null일 수 없습니다.");
    }
}
