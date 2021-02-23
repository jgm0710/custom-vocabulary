package project.study.jgm.customvocabulary.vocabulary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.vocabulary.category.Category;
import project.study.jgm.customvocabulary.vocabulary.dto.VocabularyCreateDto;
import project.study.jgm.customvocabulary.vocabulary.dto.VocabularyUpdateDto;
import project.study.jgm.customvocabulary.vocabulary.upload.VocabularyThumbnailImageFile;
import project.study.jgm.customvocabulary.vocabulary.word.LanguageType;
import project.study.jgm.customvocabulary.vocabulary.word.Word;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Vocabulary {

    @Id
    @GeneratedValue
    @Column(name = "vocabulary_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;  //개인 단어장 생성 Member, 공유 단어장 공유자

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToOne(mappedBy = "vocabulary", cascade = CascadeType.ALL)
    private VocabularyThumbnailImageFile vocabularyThumbnailImageFile;

    private String title;

    @Enumerated(EnumType.STRING)
    private LanguageType mainLanguage;

    @Enumerated(EnumType.STRING)
    private LanguageType subLanguage;

    @Builder.Default
    @OneToMany(mappedBy = "vocabulary", cascade = CascadeType.ALL)
    private List<Word> wordList = new ArrayList<>();

    private int difficulty;

    private int views;

    private int likeCount;

    private int downloadCount;

    private int memorisedCount; //단어 암기한 갯수 저장

    private int totalWordCount; //단어 총 갯수 저장

    @Enumerated(EnumType.STRING)
    private VocabularyDivision division;    //단어장 구분 [PERSONAL, DELETE, SHARED, UNSHARED, COPIED]

    private LocalDateTime registerDate;     //division에 따라 다르게 해석 : 개인 단어장{생성 날짜, 복사 날짜 저장} 단어장 공유{공유 날짜 저장}

    public static Vocabulary createPersonalVocabulary(Member member, Category category, VocabularyCreateDto createDto, VocabularyThumbnailImageFile vocabularyThumbnailImageFile) {
        Vocabulary vocabulary = Vocabulary.builder()
                .member(member)
                .category(category)
                .vocabularyThumbnailImageFile(vocabularyThumbnailImageFile)
                .title(createDto.getTitle())
                .mainLanguage(createDto.getMainLanguage())
                .subLanguage(createDto.getSubLanguage())
                .difficulty(createDto.getDifficulty())
                .views(0)
                .likeCount(0)
                .downloadCount(0)
                .memorisedCount(0)
                .totalWordCount(0)
                .division(VocabularyDivision.PERSONAL)
                .registerDate(LocalDateTime.now())
                .build();

        if (vocabularyThumbnailImageFile != null) {
            vocabularyThumbnailImageFile.setVocabulary(vocabulary);
        }

        if (category != null) {
            category.addVocabulary();
        }

        return vocabulary;
    }

    public void setTotalWordCount(int totalWordCount) {
        this.totalWordCount = totalWordCount;
    }

    public void updateTotalWordCount(int totalWordCount) {
        this.totalWordCount = totalWordCount;
    }

    public void removeWordList() {
        this.wordList.clear();
        this.totalWordCount = 0;
        this.memorisedCount = 0;
    }

    public void modify(VocabularyUpdateDto updateDto, VocabularyThumbnailImageFile vocabularyThumbnailImageFile) {
        this.vocabularyThumbnailImageFile = vocabularyThumbnailImageFile;
        this.title = updateDto.getTitle();
        this.difficulty = updateDto.getDifficulty();

        vocabularyThumbnailImageFile.setVocabulary(this);
    }

    public void delete() {
        this.division = VocabularyDivision.DELETE;
    }

    public void moveCategory(Category category) {
        if (this.category != null) {
            this.category.deleteVocabulary();
        }
        if (category != null) {
            this.category = category;
            category.addVocabulary();
        }
    }

    public Vocabulary personalToShared(Category sharedCategory) {
        Vocabulary vocabulary = Vocabulary.builder()
                .member(this.member)
                .category(sharedCategory)
                .vocabularyThumbnailImageFile(this.vocabularyThumbnailImageFile)
                .title(this.title)
                .difficulty(this.difficulty)
                .views(0)
                .likeCount(0)
                .downloadCount(0)
                .memorisedCount(0)
                .totalWordCount(0)
                .division(VocabularyDivision.SHARED)
                .registerDate(LocalDateTime.now())
                .build();

        vocabulary.getMember().addSharedVocabulary();
        if (sharedCategory != null) {
            sharedCategory.addVocabulary();
        }

        return vocabulary;
    }

    public void unshared() {
        this.division = VocabularyDivision.UNSHARED;
        this.member.deleteSharedVocabulary();
    }

    public void increaseMemorisedCount() {
        this.memorisedCount++;
    }

    public void decreaseMemorisedCount() {
        this.memorisedCount--;
    }

    public void increaseViews() {
        this.views++;
    }

    public Vocabulary sharedToPersonal(Member member, Category personalCategory) {
        Vocabulary vocabulary = Vocabulary.builder()
                .member(member)
                .category(personalCategory)
                .vocabularyThumbnailImageFile(this.vocabularyThumbnailImageFile)
                .title(this.title)
                .difficulty(this.difficulty)
                .views(0)
                .likeCount(0)
                .downloadCount(0)
                .memorisedCount(0)
                .totalWordCount(0)
                .division(VocabularyDivision.COPIED)
                .registerDate(LocalDateTime.now())
                .build();

        if (personalCategory != null) {
            personalCategory.addVocabulary();
        }

        return vocabulary;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        this.likeCount--;
    }

    public void deleteCategory() {
        this.category = null;
    }

    @Override
    public String toString() {
        return "Vocabulary{" +
                "id=" + id +
                ", member=" + member +
                ", category=" + category +
                ", vocabularyThumbnailImageFile=" + vocabularyThumbnailImageFile +
                ", title='" + title + '\'' +
                ", mainLanguage=" + mainLanguage +
                ", subLanguage=" + subLanguage +
                ", wordList=" + wordList +
                ", difficulty=" + difficulty +
                ", views=" + views +
                ", likeCount=" + likeCount +
                ", downloadCount=" + downloadCount +
                ", memorisedCount=" + memorisedCount +
                ", totalWordCount=" + totalWordCount +
                ", division=" + division +
                ", registerDate=" + registerDate +
                '}';
    }

    public void addWordList(List<Word> wordList) {
        this.wordList = wordList;
        this.totalWordCount = wordList.size();

        for (Word word : wordList) {
            word.setVocabulary(this);

            if (word.isMemorisedCheck()==true) {
                this.increaseMemorisedCount();
            }
        }
    }
}
