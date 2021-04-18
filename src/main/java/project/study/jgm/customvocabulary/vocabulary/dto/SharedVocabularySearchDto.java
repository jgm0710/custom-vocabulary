package project.study.jgm.customvocabulary.vocabulary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.common.dto.CriteriaDto;
import project.study.jgm.customvocabulary.vocabulary.VocabularySortCondition;

import javax.validation.Valid;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SharedVocabularySearchDto {
    private @Valid CriteriaDto criteria = new CriteriaDto();
    private Long categoryId = null;
    private String title = null;
    private VocabularySortCondition sortCondition = VocabularySortCondition.LATEST_DESC;
}
