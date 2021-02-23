package project.study.jgm.customvocabulary.vocabulary.word.dto;

import lombok.Data;

import javax.validation.Valid;
import java.util.List;

@Data
public class OnlyWordRequestListDto {

    @Valid
    List<WordRequestDto> wordRequestDtoList;
}
