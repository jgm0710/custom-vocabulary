package project.study.jgm.customvocabulary.vocabulary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import project.study.jgm.customvocabulary.common.upload.UploadFileResponseDto;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.vocabulary.Vocabulary;
import project.study.jgm.customvocabulary.vocabulary.category.Category;
import project.study.jgm.customvocabulary.vocabulary.upload.VocabularyThumbnailImageFile;
import project.study.jgm.customvocabulary.vocabulary.word.LanguageType;
import project.study.jgm.customvocabulary.vocabulary.word.Word;
import project.study.jgm.customvocabulary.vocabulary.word.dto.WordResponseDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PersonalVocabularyDetailResponseDto {

    private Long id;

    private WriterDto writer;

    private CategoryDto category;

    protected UploadFileResponseDto thumbnailInfo;

    private String title;

    private LanguageType mainLanguage;

    private LanguageType subLanguage;

    @Builder.Default
    private List<WordResponseDto> wordList = new ArrayList<>();

    private int difficulty;

    private int memorisedCount; //단어 암기한 갯수 저장

    private int totalWordCount; //단어 총 갯수 저장

    private LocalDateTime registerDate;

//.id
//.member
//.category
//.vocabularyThumbnailImageFile
//.title
//.mainLanguage
//.subLanguage
//.wordList
//.difficulty
//.memorisedCount
//.totalWordCount

    @Getter
    @AllArgsConstructor
    static class WriterDto {
        private Long id;
        private String nickname;
    }

    @Getter
    @AllArgsConstructor
    static class CategoryDto{
        private Long id;
        private String name;
    }


    public static PersonalVocabularyDetailResponseDto vocabularyToDetail(Vocabulary vocabulary, ModelMapper modelMapper) {
        List<Word> wordList = vocabulary.getWordList();

        List<WordResponseDto> wordResponseDtos = new ArrayList<>();
        for (Word word : wordList) {
            WordResponseDto wordResponseDto = WordResponseDto.wordToResponse(word, modelMapper);
            wordResponseDtos.add(wordResponseDto);
        }

        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabulary.getVocabularyThumbnailImageFile();
        UploadFileResponseDto thumbnailInfo = modelMapper.map(vocabularyThumbnailImageFile, UploadFileResponseDto.class);

        CategoryDto category = null;
        if (vocabulary.getCategory() != null) {
            category = new CategoryDto(vocabulary.getCategory().getId(), vocabulary.getCategory().getName());
        }

        return PersonalVocabularyDetailResponseDto.builder()
                .id(vocabulary.getId())
                .writer(new WriterDto(vocabulary.getMember().getId(), vocabulary.getMember().getNickname()))
                .category(category)
                .thumbnailInfo(thumbnailInfo)
                .title(vocabulary.getTitle())
                .mainLanguage(vocabulary.getMainLanguage())
                .subLanguage(vocabulary.getSubLanguage())
                .wordList(wordResponseDtos)
                .difficulty(vocabulary.getDifficulty())
                .memorisedCount(vocabulary.getMemorisedCount())
                .totalWordCount(vocabulary.getTotalWordCount())
                .registerDate(vocabulary.getRegisterDate())
                .build();
    }
}
