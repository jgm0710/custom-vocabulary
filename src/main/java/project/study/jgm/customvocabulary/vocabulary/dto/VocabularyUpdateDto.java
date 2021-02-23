package project.study.jgm.customvocabulary.vocabulary.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import project.study.jgm.customvocabulary.common.upload.OnlyFileIdDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VocabularyUpdateDto {
    @NotBlank(message = "수정할 제목을 입력해주세요.")
    private String title;
    @NotNull(message = "수정할 난이도를 입력해주세요")
    private Integer difficulty;
    private Long imageFileId;

//.title
//.difficulty
//.imageFileId
}
