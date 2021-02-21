package project.study.jgm.customvocabulary.vocabulary.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.common.upload.OnlyFileIdDto;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PersonalVocabularyUpdateDto {
    private String title;
    private int difficulty;
    private OnlyFileIdDto imageFileIdDto;
}
