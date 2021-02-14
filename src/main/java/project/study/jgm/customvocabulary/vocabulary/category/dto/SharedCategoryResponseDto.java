package project.study.jgm.customvocabulary.vocabulary.category.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SharedCategoryResponseDto {
    private Long categoryId;
    private String categoryName;
    private Long parentId;
    private List<SharedCategoryResponseDto> subCategories;

    public SharedCategoryResponseDto(Long categoryId, String categoryName, Long parentId) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.parentId = parentId;
    }
}
