package project.study.jgm.customvocabulary.bbs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.bbs.BbsStatus;
import project.study.jgm.customvocabulary.common.dto.CriteriaDto;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BbsSearchDto {

    private BbsSearchType searchType;

    private String keyword;

    private BbsSortType bbsSortType;

    private CriteriaDto criteriaDto = new CriteriaDto();

    @Default
    private BbsStatus bbsStatus=BbsStatus.REGISTER;

}
