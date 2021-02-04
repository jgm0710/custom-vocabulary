package project.study.jgm.customvocabulary.members.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.common.dto.CriteriaDto;

import javax.validation.Valid;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberSearchDto {

    @Builder.Default
    private MemberSearchType searchType=null;

    @Builder.Default
    private MemberSortType sortType = MemberSortType.LATEST;

    @Builder.Default
    private String keyword=null;

    @Valid
    @Builder.Default
    private CriteriaDto criteriaDto = new CriteriaDto();
}
