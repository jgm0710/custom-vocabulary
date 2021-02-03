package project.study.jgm.customvocabulary.members.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.common.CriteriaDto;
import project.study.jgm.customvocabulary.members.Member;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberSearchDto {

    private MemberSearchType searchType;

    @Builder.Default
    private MemberSortType sortType = MemberSortType.LATEST;

    private String searchContent;

    private CriteriaDto criteriaDto;

}
