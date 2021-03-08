package project.study.jgm.customvocabulary.vocabulary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import project.study.jgm.customvocabulary.common.upload.UploadFileResponseDto;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.vocabulary.Vocabulary;
import project.study.jgm.customvocabulary.vocabulary.VocabularyDivision;
import project.study.jgm.customvocabulary.vocabulary.category.Category;
import project.study.jgm.customvocabulary.vocabulary.upload.VocabularyThumbnailImageFile;
import project.study.jgm.customvocabulary.vocabulary.word.LanguageType;
import project.study.jgm.customvocabulary.vocabulary.word.Word;
import project.study.jgm.customvocabulary.vocabulary.word.dto.WordResponseDto;

import javax.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SharedVocabularyDetailDto {
    private Long id;

    private WriterDto writer;  //개인 단어장 생성 Member, 공유 단어장 공유자

    private CategoryDto category;

    private UploadFileResponseDto thumbnailInfo;

    private String title;

    private LanguageType mainLanguage;

    private LanguageType subLanguage;

    private List<WordResponseDto> wordList = new ArrayList<>();

    private int difficulty;

    private int views;

    private int likeCount;

    private int downloadCount;

    private int totalWordCount; //단어 총 갯수 저장

    private VocabularyDivision division;

    private boolean like;

    private boolean viewLike;

    private boolean permissionToDeleteAndModify;

    private LocalDateTime registerDate;     //division에 따라 다르게 해석 : 개인 단어장{생성 날짜, 복사 날짜 저장} 단어장 공유{공유 날짜 저장}

//.id
//.writer
//.category
//.vocabularyThumbnailImageFile
//.title
//.mainLanguage
//.subLanguage
//.wordList
//.difficulty
//.views
//.likeCount
//.downloadCount
//.totalWordCount
//.registerDate

    public static SharedVocabularyDetailDto sharedVocabularyToDetail(Vocabulary vocabulary, ModelMapper modelMapper) {
        List<Word> wordList = vocabulary.getWordList();
        List<WordResponseDto> wordResponseDtos = new ArrayList<>();
        for (Word word : wordList) {
            final UploadFileResponseDto imageInfo = modelMapper.map(word.getWordImageFile(), UploadFileResponseDto.class);
            final WordResponseDto wordResponseDto = WordResponseDto.builder()
                    .imageInfo(imageInfo)
                    .mainWord(word.getMainWord())
                    .subWord(word.getSubWord())
                    .memorisedCheck(false)
                    .build();
            wordResponseDtos.add(wordResponseDto);
        }

        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabulary.getVocabularyThumbnailImageFile();
        UploadFileResponseDto thumbnailInfo = modelMapper.map(vocabularyThumbnailImageFile, UploadFileResponseDto.class);

        CategoryDto category = null;
        if (vocabulary.getCategory() != null) {
            category = new CategoryDto(vocabulary.getCategory().getId(), vocabulary.getCategory().getName());
        }

        return SharedVocabularyDetailDto.builder()
                .id(vocabulary.getId())
                .writer(new WriterDto(vocabulary.getWriter().getId(), vocabulary.getWriter().getNickname()))
                .category(category)
                .thumbnailInfo(thumbnailInfo)
                .title(vocabulary.getTitle())
                .mainLanguage(vocabulary.getMainLanguage())
                .subLanguage(vocabulary.getSubLanguage())
                .wordList(wordResponseDtos)
                .difficulty(vocabulary.getDifficulty())
                .views(vocabulary.getViews())
                .likeCount(vocabulary.getLikeCount())
                .downloadCount(vocabulary.getDownloadCount())
                .totalWordCount(vocabulary.getTotalWordCount())
                .division(vocabulary.getDivision())
                .viewLike(true)
                .registerDate(vocabulary.getRegisterDate())
                .build();
    }

}
