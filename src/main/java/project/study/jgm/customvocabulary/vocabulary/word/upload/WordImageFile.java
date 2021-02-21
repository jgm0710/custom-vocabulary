package project.study.jgm.customvocabulary.vocabulary.word.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import project.study.jgm.customvocabulary.vocabulary.word.Word;

import javax.persistence.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WordImageFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "word_image_file_id")
    private Long id;

    private String fileName;

    private String fileStoredPath;

    @Column(length = 1000)
    private String fileDownloadUri;

    private String fileType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id")
    private Word word;

//.id
//.fileName
//.fileStoredPath
//.fileDownloadUri
//.fileType
//.word

}