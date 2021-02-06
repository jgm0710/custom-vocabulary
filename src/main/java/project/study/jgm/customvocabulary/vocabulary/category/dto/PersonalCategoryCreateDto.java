package project.study.jgm.customvocabulary.vocabulary.category.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.vocabulary.category.Category;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PersonalCategoryCreateDto {
    private String name;
    private Category parent;
    private int orders;
}
