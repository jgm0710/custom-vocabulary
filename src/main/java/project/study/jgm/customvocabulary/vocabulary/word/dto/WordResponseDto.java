package project.study.jgm.customvocabulary.vocabulary.word.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import project.study.jgm.customvocabulary.common.upload.UploadFileResponseDto;
import project.study.jgm.customvocabulary.vocabulary.word.Word;
import project.study.jgm.customvocabulary.vocabulary.word.upload.WordImageFile;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WordResponseDto {

    private Long id;
    private UploadFileResponseDto imageInfo;
    private String mainWord;
    private String subWord;
    private boolean memorisedCheck;

    public static WordResponseDto wordToResponse(Word word, ModelMapper modelMapper) {
        WordImageFile wordImageFile = word.getWordImageFile();
        UploadFileResponseDto uploadFileResponseDto = modelMapper.map(wordImageFile, UploadFileResponseDto.class);

        return WordResponseDto.builder()
                .id(word.getId())
                .imageInfo(uploadFileResponseDto)
                .mainWord(word.getMainWord())
                .subWord(word.getSubWord())
                .memorisedCheck(word.isMemorisedCheck())
                .build();
    }

}
