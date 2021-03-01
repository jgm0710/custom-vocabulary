package project.study.jgm.customvocabulary.vocabulary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.common.upload.OnlyFileIdDto;
import project.study.jgm.customvocabulary.vocabulary.word.LanguageType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VocabularyCreateDto {

    private Long categoryId = null;
    @NotBlank(message = "단어장의 제목을 입력해주세요.")
    private String title;
    @NotNull(message = "단어장의 난이도를 입력해주세요.")
    private Integer difficulty;
    @NotNull(message = "단어장의 MainLanguage 를 입력해주세요.")
    private LanguageType mainLanguage;
    @NotNull(message = "단어장의 SubLanguage 를 입력해주세요.")
    private LanguageType subLanguage;
    private Long imageFileId;

//.title
//.difficulty
//.mainLanguage
//.subLanguage
//.imageFileIdDto
}
