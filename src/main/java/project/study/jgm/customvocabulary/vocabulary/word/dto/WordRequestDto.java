package project.study.jgm.customvocabulary.vocabulary.word.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.common.upload.OnlyFileIdDto;
import project.study.jgm.customvocabulary.vocabulary.word.LanguageType;

import javax.validation.constraints.NotBlank;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WordRequestDto {
    private Long imageFileId;
    @NotBlank(message = "Main word 를 입력해주세요.")
    private String mainWord;
    @NotBlank(message = "Sub word 를 입력해주세요.")
    private String subWord;
    private boolean memorisedCheck = false;

//.imageFileId
//.mainWord
//.subWord
//.memorisedCheck

    public void setMemorisedCheck(boolean memorisedCheck) {
        this.memorisedCheck = memorisedCheck;
    }
}

