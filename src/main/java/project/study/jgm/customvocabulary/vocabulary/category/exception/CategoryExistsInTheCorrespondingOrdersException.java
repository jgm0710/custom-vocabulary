package project.study.jgm.customvocabulary.vocabulary.category.exception;

public class CategoryExistsInTheCorrespondingOrdersException extends RuntimeException {
    public CategoryExistsInTheCorrespondingOrdersException(int orders) {
        super("해당 순서에 카테고리가 존재합니다 : " + orders);
    }
}
