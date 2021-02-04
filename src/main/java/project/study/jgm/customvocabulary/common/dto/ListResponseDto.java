package project.study.jgm.customvocabulary.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListResponseDto<T> {

    private T data;

    private PaginationDto paging;
}
