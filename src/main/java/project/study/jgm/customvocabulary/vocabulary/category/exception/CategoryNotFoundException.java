package project.study.jgm.customvocabulary.vocabulary.category.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException() {
        super("Category not found...");
    }
}
