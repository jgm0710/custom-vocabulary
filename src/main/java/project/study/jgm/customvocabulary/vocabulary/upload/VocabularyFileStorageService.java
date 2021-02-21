package project.study.jgm.customvocabulary.vocabulary.upload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.study.jgm.customvocabulary.common.upload.FileStorage;
import project.study.jgm.customvocabulary.common.upload.FileStorageProperties;
import project.study.jgm.customvocabulary.common.upload.StoreFileDto;
import project.study.jgm.customvocabulary.vocabulary.upload.exception.VocabularyThumbnailImageFileNotFoundException;

@Service
@Transactional(readOnly = true)
public class VocabularyFileStorageService extends FileStorage {

    private final VocabularyThumbnailImageFileRepository vocabularyThumbnailImageFileRepository;

    @Autowired
    public VocabularyFileStorageService(VocabularyThumbnailImageFileRepository vocabularyThumbnailImageFileRepository, FileStorageProperties fileStorageProperties) {
        super(getFileStorageLocation(fileStorageProperties.getVocabularyThumbnailImageDir()), fileStorageProperties);
        this.vocabularyThumbnailImageFileRepository = vocabularyThumbnailImageFileRepository;
    }

    @Transactional
    public VocabularyThumbnailImageFile uploadVocabularyThumbnailImageFile(MultipartFile file) {
        StoreFileDto storeFileDto = super.storeImageFile(file);

        String requestPrefix = "api/vocabulary/downloadImageFile";
        String fileName = storeFileDto.getFileName();
        String fileStoredPathValue = storeFileDto.getFileStoredPath().toAbsolutePath().toString();
        String fileDownloadUri = super.getFileDownloadUri(requestPrefix, fileName);
        String contentType = file.getContentType();

        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = VocabularyThumbnailImageFile.builder()
                .fileName(fileName)
                .fileStoredPath(fileStoredPathValue)
                .fileDownloadUri(fileDownloadUri)
                .fileType(contentType)
                .vocabulary(null)
                .build();

        return vocabularyThumbnailImageFileRepository.save(vocabularyThumbnailImageFile);
    }

    public VocabularyThumbnailImageFile getVocabularyThumbnailImageFileByFileName(String fileName) {
        return vocabularyThumbnailImageFileRepository.findByFileName(fileName).orElseThrow(VocabularyThumbnailImageFileNotFoundException::new);
    }

    public Resource loadVocabularyThumbnailImageFileAsResource(Long fileId) {
        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyThumbnailImageFileRepository
                .findById(fileId).orElseThrow(VocabularyThumbnailImageFileNotFoundException::new);

        String fileName = vocabularyThumbnailImageFile.getFileName();
        String fileStoredPath = vocabularyThumbnailImageFile.getFileStoredPath();

        return super.getResource(fileName, fileStoredPath);
    }

    public Resource loadThumbnailOfVocabularyThumbnailImageFileAsResource(Long fileId) {
        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyThumbnailImageFileRepository
                .findById(fileId).orElseThrow(VocabularyThumbnailImageFileNotFoundException::new);

        String thumbnailName = super.getThumbnailName(vocabularyThumbnailImageFile.getFileName());
        String fileStoredPath = vocabularyThumbnailImageFile.getFileStoredPath();

        return super.getResource(thumbnailName, fileStoredPath);
    }
}
