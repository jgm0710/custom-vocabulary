package project.study.jgm.customvocabulary.vocabulary.category.dto;

import java.util.Comparator;

public class CategoryResponseDtoComparator implements Comparator<CategoryResponseDto> {

    @Override
    public int compare(CategoryResponseDto first, CategoryResponseDto second) {
        int firstOrders = first.getOrders();
        int secondOrders = second.getOrders();

        if (firstOrders > secondOrders) {
            return 1;
        } else if (firstOrders < secondOrders) {
            return -1;
        } else {
            return 0;
        }
    }
}
