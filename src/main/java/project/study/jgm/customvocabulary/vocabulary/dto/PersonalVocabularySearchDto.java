package project.study.jgm.customvocabulary.vocabulary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.common.dto.CriteriaDto;
import project.study.jgm.customvocabulary.vocabulary.VocabularySearchBy;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PersonalVocabularySearchDto {

    private int pageNum = 1;

    private int limit = 15;

    private Long categoryId = null;

    @NotNull
    private VocabularySearchBy searchBy;

    public CriteriaDto getCriteriaDto() {
        return new @Valid CriteriaDto(this.pageNum, this.limit);
    }
}
