package project.study.jgm.customvocabulary.vocabulary.word;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.vocabulary.Vocabulary;
import project.study.jgm.customvocabulary.vocabulary.word.dto.WordRequestDto;
import project.study.jgm.customvocabulary.vocabulary.word.upload.WordImageFile;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "word_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "vocabulary_id")
    private Vocabulary vocabulary;

    @OneToOne(mappedBy = "word", cascade = CascadeType.ALL)
    private WordImageFile wordImageFile;

    private String mainWord;

    private String subWord;

    private boolean memorisedCheck; //암기 했는지 체크

    @Override
    public String toString() {
        return "Word{" +
                "id=" + id +
//                ", vocabulary=" + vocabulary +
                ", wordImageFile=" + wordImageFile +
                ", mainWord='" + mainWord + '\'' +
                ", subWord='" + subWord + '\'' +
                ", memorisedCheck=" + memorisedCheck +
                '}';
    }

    public static Word createWord(WordRequestDto wordRequestDto, WordImageFile wordImageFile) {
        Word word = Word.builder()
                .wordImageFile(wordImageFile)
                .mainWord(wordRequestDto.getMainWord())
                .subWord(wordRequestDto.getSubWord())
                .memorisedCheck(wordRequestDto.isMemorisedCheck())
                .build();

        wordImageFile.setWord(word);

        return word;
    }

    public Word createCopiedWord() {

        final WordImageFile copiedWordImageFile = this.wordImageFile.createCopiedWordImageFile();

        final Word copiedWord = Word.builder()
                .wordImageFile(copiedWordImageFile)
                .mainWord(this.getMainWord())
                .subWord(this.getSubWord())
                .memorisedCheck(false)
                .build();

        copiedWordImageFile.setWord(copiedWord);

        return copiedWord;
    }

    public void checkMemorise() {
        if (this.memorisedCheck == false) {
            this.memorisedCheck = true;
            this.vocabulary.increaseMemorisedCount();
        } else {
            this.memorisedCheck = false;
            this.vocabulary.decreaseMemorisedCount();
        }
    }

    public void setVocabulary(Vocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
