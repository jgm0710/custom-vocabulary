package project.study.jgm.customvocabulary.vocabulary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.common.upload.OnlyFileIdDto;
import project.study.jgm.customvocabulary.vocabulary.word.LanguageType;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PersonalVocabularyCreateDto {
    private String title;
    private int difficulty;
    private LanguageType mainLanguage;
    private LanguageType subLanguage;
    private OnlyFileIdDto imageFileIdDto;
}
