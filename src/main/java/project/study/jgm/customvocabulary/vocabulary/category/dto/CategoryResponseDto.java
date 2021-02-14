package project.study.jgm.customvocabulary.vocabulary.category.dto;

import lombok.*;
import project.study.jgm.customvocabulary.vocabulary.category.Category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponseDto {

    private Long id;

    private String name;

    private Long parentId;

    @Builder.Default
    private List<CategoryResponseDto> subCategoryList = new ArrayList<>();

    private long vocabularyCount;

    private int orders;

    @Override
    public String toString() {
        return "PersonalCategoryResponseDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
//                ", parent=" + parent +
                ", subCategoryList=" + subCategoryList +
                ", vocabularyCount=" + vocabularyCount +
                ", orders=" + orders +
                '}';
    }

//.id
//.name
//.parent
//.children
//.vocabularyCount
//.orders

    public static List<CategoryResponseDto> categoryListToResponseList(List<Category> categoryList) {

        List<CategoryResponseDto> categoryResponseDtoList = new ArrayList<>();
        for (Category category : categoryList) {
            CategoryResponseDto categoryResponseDto = getPersonalCategoryResponseDto(category);
            addSubCategoryList(categoryResponseDto, category.getChildren());

            categoryResponseDtoList.add(categoryResponseDto);
        }

        sortByOrders(categoryResponseDtoList, new CategoryResponseDtoComparator());

        return categoryResponseDtoList;
    }

    private static CategoryResponseDto getPersonalCategoryResponseDto(Category category) {
        Long parentId = null;
        if (category.getParent() != null) {
            parentId = category.getParent().getId();
        }
        CategoryResponseDto categoryResponseDto = CategoryResponseDto.builder()
                .id(category.getId())
                .name(category.getName())
//                .parent(category.getParent())
                .parentId(parentId)
                .vocabularyCount(category.getVocabularyCount())
                .orders(category.getOrders())
                .build();
        return categoryResponseDto;
    }

    private static void addSubCategoryList(CategoryResponseDto categoryResponseDto, List<Category> children) {
        if (children.isEmpty()) {
            return;
        }

        children.forEach(
                c -> {
                    CategoryResponseDto pcrd = getPersonalCategoryResponseDto(c);
                    categoryResponseDto.getSubCategoryList().add(pcrd);
                    addSubCategoryList(pcrd, c.getChildren());
                }
        );
    }

    private static void sortByOrders(List<CategoryResponseDto> categoryResponseDtos, CategoryResponseDtoComparator categoryResponseDtoComparator) {
        if (categoryResponseDtos == null) {
            return;
        }

        Collections.sort(categoryResponseDtos, categoryResponseDtoComparator);

        categoryResponseDtos.forEach(
                categoryResponseDto -> {
                    List<CategoryResponseDto> subCategoryList = categoryResponseDto.getSubCategoryList();
                    sortByOrders(subCategoryList, categoryResponseDtoComparator);
                }
        );
    }

}
