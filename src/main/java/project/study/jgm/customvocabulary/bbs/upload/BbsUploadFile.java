package project.study.jgm.customvocabulary.bbs.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.bbs.Bbs;

import javax.persistence.*;
import java.nio.file.Path;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BbsUploadFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bbs_upload_file_id")
    private Long id;

    private String fileName;

    private String fileStoredPath;

    @Column(length = 1000)
    private String fileDownloadUri;

    private String fileType;

    private Long size;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bbs_id")
    private Bbs bbs;

    public void setBbs(Bbs bbs) {
        this.bbs = bbs;
    }

    public void setFileDownloadUri(String fileDownloadUri) {
        this.fileDownloadUri = fileDownloadUri;
    }
}
