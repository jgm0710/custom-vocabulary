package project.study.jgm.customvocabulary.vocabulary.category.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException() {
        super("요청한 카테고리를 찾을 수 없습니다.");
    }
}
