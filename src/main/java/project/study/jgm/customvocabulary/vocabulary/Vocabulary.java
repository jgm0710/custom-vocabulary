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
    private Member proprietor;  //개인 단어장 생성 Member, 공유 단어장 공유자

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "writer_id")
    private Member writer;

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
                .proprietor(member)
                .writer(member)
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

        this.category = category;
        if (category != null) {
            category.addVocabulary();
        }
    }

    public Vocabulary personalToShared(Category sharedCategory) {

        VocabularyThumbnailImageFile copiedThumbnailImageFile = createCopiedThumbnailImageFile();

        Vocabulary sharedVocabulary = Vocabulary.builder()
                .proprietor(this.proprietor)
                .writer(this.writer)
                .category(sharedCategory)
                .vocabularyThumbnailImageFile(copiedThumbnailImageFile)
                .title(this.title)
                .mainLanguage(this.mainLanguage)
                .subLanguage(this.subLanguage)
                .difficulty(this.difficulty)
                .views(0)
                .likeCount(0)
                .downloadCount(0)
                .memorisedCount(0)
                .totalWordCount(this.totalWordCount)
                .division(VocabularyDivision.SHARED)
                .registerDate(LocalDateTime.now())
                .build();

        if (copiedThumbnailImageFile != null) {
            copiedThumbnailImageFile.setVocabulary(sharedVocabulary);
        }

        List<Word> copiedWordList = createCopiedWordList();

        sharedVocabulary.addWordList(copiedWordList);

        this.proprietor.addSharedVocabulary();

        if (sharedCategory != null) {
            sharedCategory.addVocabulary();
        }

        return sharedVocabulary;
    }

    private List<Word> createCopiedWordList() {
        List<Word> copiedWordList = new ArrayList<>();
        for (Word word : this.wordList) {
            final Word copiedWord = word.createCopiedWord();
            copiedWordList.add(copiedWord);
        }
        return copiedWordList;
    }

    private VocabularyThumbnailImageFile createCopiedThumbnailImageFile() {
        VocabularyThumbnailImageFile copiedThumbnailImageFile = null;
        if (this.vocabularyThumbnailImageFile != null) {
            copiedThumbnailImageFile = this.vocabularyThumbnailImageFile.createCopiedThumbnailImageFile();
        }
        return copiedThumbnailImageFile;
    }

    public void unshared() {
        this.division = VocabularyDivision.UNSHARED;
        this.writer.deleteSharedVocabulary();
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

    public void decreaseViews() {
        this.views--;
    }

    public Vocabulary sharedToPersonal(Member proprietor, Category personalCategory) {
        final VocabularyThumbnailImageFile copiedThumbnailImageFile = createCopiedThumbnailImageFile();
        final List<Word> copiedWordList = createCopiedWordList();

        Vocabulary copiedVocabulary = Vocabulary.builder()
                .proprietor(proprietor)
                .writer(this.writer)
                .category(personalCategory)
                .vocabularyThumbnailImageFile(copiedThumbnailImageFile)
                .title(this.title)
                .mainLanguage(this.mainLanguage)
                .subLanguage(this.subLanguage)
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

        copiedVocabulary.addWordList(copiedWordList);

        return copiedVocabulary;
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
                ", member=" + proprietor +
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
