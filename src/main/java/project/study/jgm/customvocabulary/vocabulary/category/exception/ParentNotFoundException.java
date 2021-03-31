package project.study.jgm.customvocabulary.vocabulary.category.exception;

public class ParentNotFoundException extends RuntimeException {
    public ParentNotFoundException(Long parentId) {
        super("부모 카테고리를 찾을 수 없습니다 : " + parentId);
    }
}
