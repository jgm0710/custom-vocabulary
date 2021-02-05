package project.study.jgm.customvocabulary.members.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.common.dto.CriteriaDto;
import project.study.jgm.customvocabulary.common.dto.PaginationDto;

import javax.validation.Valid;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberSearchDto {

    private MemberSearchType searchType;

    private MemberSortType sortType;

    private String keyword;

    @Valid
    @Builder.Default
    private CriteriaDto criteriaDto = new CriteriaDto();

    public void updatePage(int target) {
        this.criteriaDto.setPageNum(target);
    }
}
