package project.study.jgm.customvocabulary.vocabulary.category.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.vocabulary.category.Category;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryCreateDto {

    @NotBlank(message = "생성할 카테고리 이름을 입력해주세요.")
    private String name;

    private Long parentId;

    @NotNull(message = "카테고리를 생성할 순서를 지정해주세요.")
    @Min(value = 1, message = "지정할 수 있는 순서는 1보다 작을 수 없습니다.")
    private Integer orders;

//.name
//.parentId
//.orders
}
