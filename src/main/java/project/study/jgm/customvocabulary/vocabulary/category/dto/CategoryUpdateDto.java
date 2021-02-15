package project.study.jgm.customvocabulary.vocabulary.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.vocabulary.category.Category;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryUpdateDto {
    @NotBlank(message = "수정할 이름을 입력해주세요.")
    private String name;

    private Long parentId;

    @NotNull(message = "수정할 순서를 입력해주세요.")
    private Integer orders;
}
