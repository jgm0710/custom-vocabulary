package project.study.jgm.customvocabulary.bbs.reply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReplyUpdateDto {

    @NotBlank(message = "변경할 내용을 입력해주세요.")
    private String content;

}
