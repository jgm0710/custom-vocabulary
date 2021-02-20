package project.study.jgm.customvocabulary.bbs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.common.upload.OnlyFileIdDto;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BbsUpdateDto {

    @NotBlank(message = "수정할 제목을 입력해주세요.")
    private String title;

    @NotBlank(message = "수정할 내용을 입력해주세요.")
    private String content;

    private List<OnlyFileIdDto> fileIdList;
}
