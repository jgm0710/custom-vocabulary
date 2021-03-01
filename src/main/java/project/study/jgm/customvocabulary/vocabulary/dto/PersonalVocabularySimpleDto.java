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
public class PersonalVocabularySimpleDto {
    private Long id;

    private WriterDto writer;  //개인 단어장 생성 Member, 공유 단어장 공유자

    private CategoryDto category;

    private UploadFileResponseDto thumbnailInfo;

    private String title;

    private LanguageType mainLanguage;

    private LanguageType subLanguage;

    private int difficulty;

    private int memorisedCount; //단어 암기한 갯수 저장

    private int totalWordCount; //단어 총 갯수 저장

    private VocabularyDivision division;

    private LocalDateTime registerDate;     //division에 따라 다르게 해석 : 개인 단어장{생성 날짜, 복사 날짜 저장} 단어장 공유{공유 날짜 저장}

    @Override
    public String toString() {
        return "PersonalVocabularySimpleDto{" +
                "id=" + id +
                ", writer=" + writer +
                ", category=" + category +
                ", thumbnailInfo=" + thumbnailInfo +
                ", title='" + title + '\'' +
                ", mainLanguage=" + mainLanguage +
                ", subLanguage=" + subLanguage +
                ", difficulty=" + difficulty +
                ", memorisedCount=" + memorisedCount +
                ", totalWordCount=" + totalWordCount +
                ", registerDate=" + registerDate +
                '}';
    }

    public static PersonalVocabularySimpleDto personalVocabularyToSimple(Vocabulary vocabulary, ModelMapper modelMapper) {

        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabulary.getVocabularyThumbnailImageFile();
        UploadFileResponseDto thumbnailInfo = modelMapper.map(vocabularyThumbnailImageFile, UploadFileResponseDto.class);

        CategoryDto category = null;
        if (vocabulary.getCategory() != null) {
            category = new CategoryDto(vocabulary.getCategory().getId(), vocabulary.getCategory().getName());
        }

        return PersonalVocabularySimpleDto.builder()
                .id(vocabulary.getId())
                .writer(new WriterDto(vocabulary.getWriter().getId(), vocabulary.getWriter().getNickname()))
                .category(category)
                .thumbnailInfo(thumbnailInfo)
                .title(vocabulary.getTitle())
                .mainLanguage(vocabulary.getMainLanguage())
                .subLanguage(vocabulary.getSubLanguage())
                .difficulty(vocabulary.getDifficulty())
                .memorisedCount(vocabulary.getMemorisedCount())
                .totalWordCount(vocabulary.getTotalWordCount())
                .division(vocabulary.getDivision())
                .registerDate(vocabulary.getRegisterDate())
                .build();
    }

}
