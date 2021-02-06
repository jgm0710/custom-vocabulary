package project.study.jgm.customvocabulary.bbs.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BbsCreateDto {
    private String title;
    private String content;
}
