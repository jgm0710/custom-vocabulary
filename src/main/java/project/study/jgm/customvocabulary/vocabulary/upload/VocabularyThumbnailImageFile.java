package project.study.jgm.customvocabulary.vocabulary.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.vocabulary.Vocabulary;

import javax.persistence.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VocabularyThumbnailImageFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vocabulary_thumbnail_image_file_id")
    private Long id;

    private String fileName;

    private String fileStoredPath;

    @Column(length = 1000)
    private String fileDownloadUri;

    private String fileType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocabulary_id")
    private Vocabulary vocabulary;

    @Override
    public String toString() {
        return "VocabularyThumbnailImageFile{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", fileStoredPath='" + fileStoredPath + '\'' +
                ", fileDownloadUri='" + fileDownloadUri + '\'' +
                ", fileType='" + fileType + '\'' +
//                ", vocabulary=" + vocabulary +
                '}';
    }

    public void setVocabulary(Vocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }
}
