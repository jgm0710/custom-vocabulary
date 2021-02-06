package project.study.jgm.customvocabulary.bbs.reply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.bbs.reply.Reply;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReplyCreateDto {
    private Reply parent;
    private String content;
}
