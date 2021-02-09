package project.study.jgm.customvocabulary.bbs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BbsUpdateDto {

    @NotBlank(message = "수정할 제목을 입력해주세요.")
    private String title;

    @NotBlank(message = "수정할 내용을 입력해주세요.")
    private String content;
}
