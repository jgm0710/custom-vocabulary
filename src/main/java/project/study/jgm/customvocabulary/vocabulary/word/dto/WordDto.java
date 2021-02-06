package project.study.jgm.customvocabulary.vocabulary.word.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.vocabulary.word.LanguageType;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WordDto {
    private String imgUrl;
    private LanguageType mainLanguage;
    private String mainWord;
    private LanguageType subLanguage;
    private String subWord;
    private boolean memorisedCheck;

    public void setMemorisedCheck(boolean memorisedCheck) {
        this.memorisedCheck = memorisedCheck;
    }
}

