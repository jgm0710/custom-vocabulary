package project.study.jgm.customvocabulary.vocabulary.word;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.vocabulary.Vocabulary;
import project.study.jgm.customvocabulary.vocabulary.word.dto.WordDto;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Word {

    @Id
    @GeneratedValue
    @Column(name = "word_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "vocabulary_id")
    private Vocabulary vocabulary;

    private String imgUrl;

    @Enumerated(EnumType.STRING)
    private LanguageType mainLanguage;

    private String mainWord;

    @Enumerated(EnumType.STRING)
    private LanguageType subLanguage;

    private String subWord;

    private boolean memorisedCheck; //암기 했는지 체크

    public static void addWordToVocabulary(Vocabulary vocabulary, WordDto wordDto) {
        Word word = Word.builder()
                .vocabulary(vocabulary)
                .imgUrl(wordDto.getImgUrl())
                .mainLanguage(wordDto.getMainLanguage())
                .mainWord(wordDto.getMainWord())
                .subLanguage(wordDto.getSubLanguage())
                .subWord(wordDto.getSubWord())
                .memorisedCheck(wordDto.isMemorisedCheck())
                .build();

        word.getVocabulary().addWord(word);
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
}
