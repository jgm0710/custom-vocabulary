package project.study.jgm.customvocabulary.vocabulary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.vocabulary.Vocabulary;
import project.study.jgm.customvocabulary.vocabulary.category.Category;
import project.study.jgm.customvocabulary.vocabulary.upload.VocabularyThumbnailImageFile;
import project.study.jgm.customvocabulary.vocabulary.word.LanguageType;
import project.study.jgm.customvocabulary.vocabulary.word.Word;
import project.study.jgm.customvocabulary.vocabulary.word.dto.WordResponseDto;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PersonalVocabularyDetailResponseDto {

    private Long id;

    private Member member;  //개인 단어장 생성 Member, 공유 단어장 공유자

    private Category category;

    private VocabularyThumbnailImageFile vocabularyThumbnailImageFile;

    private String title;

    private LanguageType mainLanguage;

    private LanguageType subLanguage;

    @Builder.Default
    private List<WordResponseDto> wordList = new ArrayList<>();

    private int difficulty;

    private int memorisedCount; //단어 암기한 갯수 저장

    private int totalWordCount; //단어 총 갯수 저장

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


    public static PersonalVocabularyDetailResponseDto vocabularyToDetail(Vocabulary vocabulary, ModelMapper modelMapper) {
        List<Word> wordList = vocabulary.getWordList();

        List<WordResponseDto> wordResponseDtos = new ArrayList<>();
        for (Word word : wordList) {
            WordResponseDto wordResponseDto = WordResponseDto.wordToResponse(word, modelMapper);
            wordResponseDtos.add(wordResponseDto);
        }

       return PersonalVocabularyDetailResponseDto.builder()
                .id(vocabulary.getId())
                .member(vocabulary.getMember())
                .category(vocabulary.getCategory())
                .vocabularyThumbnailImageFile(vocabulary.getVocabularyThumbnailImageFile())
                .title(vocabulary.getTitle())
                .mainLanguage(vocabulary.getMainLanguage())
                .subLanguage(vocabulary.getSubLanguage())
                .wordList(wordResponseDtos)
                .difficulty(vocabulary.getDifficulty())
                .memorisedCount(vocabulary.getMemorisedCount())
                .totalWordCount(vocabulary.getTotalWordCount())
                .build();
    }
}
