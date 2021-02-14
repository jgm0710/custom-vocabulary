package project.study.jgm.customvocabulary.vocabulary.category.exception;

public class ExistSubCategoryException extends RuntimeException {
    public ExistSubCategoryException() {
        super("하위 카테고리가 있는 경우 삭제가 불가능합니다.");
    }
}
