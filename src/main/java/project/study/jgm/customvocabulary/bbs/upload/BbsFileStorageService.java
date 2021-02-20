package project.study.jgm.customvocabulary.bbs.upload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.study.jgm.customvocabulary.bbs.upload.exception.BbsUploadFileNotFoundException;
import project.study.jgm.customvocabulary.common.upload.FileStorage;
import project.study.jgm.customvocabulary.common.upload.FileStorageProperties;
import project.study.jgm.customvocabulary.common.upload.StoreFileDto;

import java.nio.file.Paths;

@Service
@Transactional(readOnly = true)
public class BbsFileStorageService extends FileStorage {

    private final BbsUploadFileRepository bbsUploadFileRepository;

    @Autowired
    public BbsFileStorageService(FileStorageProperties fileStorageProperties, BbsUploadFileRepository bbsUploadFileRepository) {
        super(Paths.get(fileStorageProperties.getBbsUploadDir())
                .toAbsolutePath().normalize(), fileStorageProperties);

        this.bbsUploadFileRepository = bbsUploadFileRepository;

    }

    @Transactional
    public BbsUploadFile uploadBbsFile(MultipartFile file) {

        StoreFileDto storeFileDto = super.storeFile(file);

        String requestPrefix = "/api/bbs/downloadFile";
        String fileName = storeFileDto.getFileName();
        String fileDownloadUri = super.getFileDownloadUri(requestPrefix, fileName);

        BbsUploadFile bbsUploadFile = BbsUploadFile.builder()
                .fileName(storeFileDto.getFileName())
                .fileStoredPath(storeFileDto.getFileStoredPath().toAbsolutePath().toString())
                .fileDownloadUri(fileDownloadUri)
                .fileType(file.getContentType())
                .size(file.getSize())
                .bbs(null)
                .build();

        return bbsUploadFileRepository.save(bbsUploadFile);

    }

    public BbsUploadFile getBbsUploadFileByFileName(String fileName) {
        return bbsUploadFileRepository.findByFileName(fileName).orElseThrow(BbsUploadFileNotFoundException::new);
    }

    public Resource loadBbsFileAsResource(Long bbsFileId) {

        BbsUploadFile bbsUploadFile = bbsUploadFileRepository.findById(bbsFileId).orElseThrow(BbsUploadFileNotFoundException::new);

        String fileName = bbsUploadFile.getFileName();
        String fileStoredPath = bbsUploadFile.getFileStoredPath();

        return super.getResource(fileName, fileStoredPath);

    }

    public Resource loadBbsThumbnailAsResource(Long bbsFileId) {
        BbsUploadFile bbsUploadFile = bbsUploadFileRepository.findById(bbsFileId).orElseThrow(BbsUploadFileNotFoundException::new);

        String thumbnailName = super.getThumbnailName(bbsUploadFile);
        String fileStoredPath = bbsUploadFile.getFileStoredPath();

        return super.getResource(thumbnailName, fileStoredPath);
    }

}
