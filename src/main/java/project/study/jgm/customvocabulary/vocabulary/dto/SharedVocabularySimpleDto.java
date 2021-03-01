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

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SharedVocabularySimpleDto {

    private Long id;

    private WriterDto writer;  //개인 단어장 생성 Member, 공유 단어장 공유자

    private CategoryDto category;

    private UploadFileResponseDto thumbnailInfo;

    private String title;

    private LanguageType mainLanguage;

    private LanguageType subLanguage;

    private int difficulty;

    private int views;

    private int likeCount;

    private int downloadCount;

    private int totalWordCount; //단어 총 갯수 저장

    private VocabularyDivision division;

    private LocalDateTime registerDate;     //division에 따라 다르게 해석 : 개인 단어장{생성 날짜, 복사 날짜 저장} 단어장 공유{공유 날짜 저장}

//.id
//.writer
//.category
//.thumbnailInfo
//.title
//.mainLanguage
//.subLanguage
//.difficulty
//.views
//.likeCount
//.downloadCount
//.totalWordCount
//.registerDate

    public static SharedVocabularySimpleDto sharedVocabularyToSimple(Vocabulary sharedVocabulary, ModelMapper modelMapper) {

        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = sharedVocabulary.getVocabularyThumbnailImageFile();
        UploadFileResponseDto thumbnailInfo = modelMapper.map(vocabularyThumbnailImageFile, UploadFileResponseDto.class);

        CategoryDto category = null;
        if (sharedVocabulary.getCategory() != null) {
            category = new CategoryDto(sharedVocabulary.getCategory().getId(), sharedVocabulary.getCategory().getName());
        }

        return SharedVocabularySimpleDto.builder()
                .id(sharedVocabulary.getId())
                .writer(new WriterDto(sharedVocabulary.getWriter().getId(), sharedVocabulary.getWriter().getNickname()))
                .category(category)
                .thumbnailInfo(thumbnailInfo)
                .title(sharedVocabulary.getTitle())
                .mainLanguage(sharedVocabulary.getMainLanguage())
                .subLanguage(sharedVocabulary.getSubLanguage())
                .difficulty(sharedVocabulary.getDifficulty())
                .views(sharedVocabulary.getViews())
                .likeCount(sharedVocabulary.getLikeCount())
                .downloadCount(sharedVocabulary.getDownloadCount())
                .totalWordCount(sharedVocabulary.getTotalWordCount())
                .division(sharedVocabulary.getDivision())
                .registerDate(sharedVocabulary.getRegisterDate())
                .build();
    }

}
