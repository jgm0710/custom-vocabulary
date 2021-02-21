package project.study.jgm.customvocabulary.vocabulary.word.upload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.study.jgm.customvocabulary.common.upload.FileStorage;
import project.study.jgm.customvocabulary.common.upload.FileStorageProperties;
import project.study.jgm.customvocabulary.common.upload.StoreFileDto;
import project.study.jgm.customvocabulary.vocabulary.word.upload.exception.WordImageFileNotFoundException;

import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Transactional(readOnly = true)
public class WordFileStorageService extends FileStorage {


    private final WordImageFileRepository wordImageFileRepository;

    @Autowired
    public WordFileStorageService(WordImageFileRepository wordImageFileRepository, FileStorageProperties fileStorageProperties) {
        super(getFileStorageLocation(fileStorageProperties.getWordImageDir()), fileStorageProperties);
        this.wordImageFileRepository = wordImageFileRepository;
    }

    @Transactional
    public WordImageFile uploadWordImageFile(MultipartFile file) {

        StoreFileDto storeFileDto = super.storeImageFile(file);

        String requestPrefix = "api/vocabulary/word/downloadImageFile";
        String fileName = storeFileDto.getFileName();
        String fileStoredPathValue = storeFileDto.getFileStoredPath().toAbsolutePath().toString();
        String fileDownloadUri = super.getFileDownloadUri(requestPrefix, fileName);
        String contentType = file.getContentType();

        WordImageFile wordImageFile = WordImageFile.builder()
                .fileName(fileName)
                .fileStoredPath(fileStoredPathValue)
                .fileDownloadUri(fileDownloadUri)
                .fileType(contentType)
                .word(null)
                .build();

        return wordImageFileRepository.save(wordImageFile);
    }

    public WordImageFile getWordImageFileByFileName(String fileName) {
        return wordImageFileRepository.findByFileName(fileName).orElseThrow(WordImageFileNotFoundException::new);
    }

    public Resource loadWordImageFileAsResource(Long fileId) {
        WordImageFile wordImageFile = wordImageFileRepository.findById(fileId).orElseThrow(WordImageFileNotFoundException::new);

        String fileName = wordImageFile.getFileName();
        String fileStoredPath = wordImageFile.getFileStoredPath();

        return super.getResource(fileName, fileStoredPath);
    }

    public Resource loadThumbnailOfWordImageFileAsResource(Long fileId) {
        WordImageFile wordImageFile = wordImageFileRepository.findById(fileId).orElseThrow(WordImageFileNotFoundException::new);

        String thumbnailName = super.getThumbnailName(wordImageFile.getFileName());
        String fileStoredPath = wordImageFile.getFileStoredPath();

        return super.getResource(thumbnailName, fileStoredPath);
    }


}
