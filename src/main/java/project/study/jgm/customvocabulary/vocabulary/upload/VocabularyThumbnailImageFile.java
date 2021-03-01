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

    private Long size;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocabulary_id")
    private Vocabulary vocabulary;

    public void setVocabulary(Vocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }

    public VocabularyThumbnailImageFile createCopiedThumbnailImageFile() {

        return VocabularyThumbnailImageFile.builder()
                .fileName(this.fileName)
                .fileStoredPath(this.fileStoredPath)
                .fileDownloadUri(this.fileDownloadUri)
                .fileType(this.fileType)
                .size(this.size)
                .build();
    }
}
