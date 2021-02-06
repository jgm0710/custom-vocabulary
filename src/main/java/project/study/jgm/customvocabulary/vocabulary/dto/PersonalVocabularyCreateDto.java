package project.study.jgm.customvocabulary.vocabulary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PersonalVocabularyCreateDto {
    private String thumbnailImgUrl;
    private String title;
    private int difficulty;
}
