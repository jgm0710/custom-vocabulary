package project.study.jgm.customvocabulary.bbs.upload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnailator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import project.study.jgm.customvocabulary.common.upload.FileStorageProperties;
import project.study.jgm.customvocabulary.common.upload.exception.FileStorageException;
import project.study.jgm.customvocabulary.common.upload.exception.MyFileNotFoundException;
import project.study.jgm.customvocabulary.common.upload.exception.OriginalFilenameNotFoundException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BbsFileStorageService {

    private Path fileStorageLocation;

    private final BbsUploadFileRepository bbsUploadFileRepository;

    private final FileStorageProperties fileStorageProperties;

    @Autowired
    public BbsFileStorageService(FileStorageProperties fileStorageProperties, BbsUploadFileRepository bbsUploadFileRepository) {

        this.fileStorageLocation = Paths.get(fileStorageProperties.getBbsUploadDir())
                .toAbsolutePath().normalize();

        this.fileStorageProperties = fileStorageProperties;

        this.bbsUploadFileRepository = bbsUploadFileRepository;

//        try {
//            Files.createDirectories(this.fileStorageLocation);
//        } catch (IOException e) {
//            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", e);
//        }
    }

    public StoreFileDto storeFile(MultipartFile file) {

        String originalFilename;

        if (file.getOriginalFilename() != null) {
            originalFilename = file.getOriginalFilename();
        } else {
            throw new OriginalFilenameNotFoundException();
        }

        String fileName = UUID.randomUUID().toString() + "_" + StringUtils.cleanPath(originalFilename);

        LocalDate now = LocalDate.now();
        String year = Integer.toString(now.getYear());
        String month = Integer.toString(now.getMonthValue());
        String dayOfMonth = Integer.toString(now.getDayOfMonth());
        Path datePath = Paths.get(year, month, dayOfMonth);
        Path finalFileStorageLocation = this.fileStorageLocation.resolve(datePath);

        try {
//            Files.createDirectories(this.fileStorageLocation);
            if (Files.notExists(finalFileStorageLocation)) {
                Files.createDirectories(finalFileStorageLocation);
            }
        } catch (IOException e) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", e);
        }

        try {
            if (fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename invalid path sequence " + fileName);
            }

//            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Path targetLocation = finalFileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            if (checkImageType(file)) {
                String thumbnailPrefix = fileStorageProperties.getThumbnailPrefix();
                targetLocation = finalFileStorageLocation.resolve(thumbnailPrefix + fileName);
                File targetFile = targetLocation.toFile();
                FileOutputStream thumbnail = new FileOutputStream(targetFile);

                Thumbnailator.createThumbnail(file.getInputStream(), thumbnail, 100, 100);

                thumbnail.close();

            }

            return new StoreFileDto(fileName, finalFileStorageLocation);
        } catch (IOException e) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", e);
        }
    }

    private boolean checkImageType(MultipartFile file) {
        if (file.getContentType() != null) {
            String contentType = file.getContentType();
            return contentType.startsWith("image");
        } else {
            return false;
        }
    }

    @Data
    @AllArgsConstructor
    class StoreFileDto {
        private String fileName;
        private Path fileStoredPath;
    }


    public Resource loadBbsFileAsResource(Long bbsFileId) {

        BbsUploadFile bbsUploadFile = bbsUploadFileRepository.findById(bbsFileId).orElseThrow(MyFileNotFoundException::new);

        String fileName = bbsUploadFile.getFileName();
        String fileStoredPath = bbsUploadFile.getFileStoredPath();

        return getResource(fileName, fileStoredPath);

    }

    public Resource loadBbsThumbnailAsResource(Long bbsFileId) {
        BbsUploadFile bbsUploadFile = bbsUploadFileRepository.findById(bbsFileId).orElseThrow(MyFileNotFoundException::new);
        String thumbnailPrefix = fileStorageProperties.getThumbnailPrefix();

        String fileName = thumbnailPrefix + bbsUploadFile.getFileName();
        String fileStoredPath = bbsUploadFile.getFileStoredPath();

        return getResource(fileName, fileStoredPath);
    }

    @Transactional
    public BbsUploadFile uploadBbsFile(MultipartFile file) {

        StoreFileDto storeFileDto = this.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/bbs")
                .path("/downloadFile/")
                .path(storeFileDto.getFileName())
                .toUriString();

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
        return bbsUploadFileRepository.findByFileName(fileName).orElseThrow(MyFileNotFoundException::new);
    }

    public void deleteBbsFile(Long bbsFileId) {
        BbsUploadFile bbsUploadFile = bbsUploadFileRepository.findById(bbsFileId).orElseThrow(MyFileNotFoundException::new);
        Path path = Path.of(bbsUploadFile.getFileStoredPath());
        Path filePath = path.resolve(bbsUploadFile.getFileName()).normalize();

        try {
            Files.delete(filePath);
            bbsUploadFileRepository.delete(bbsUploadFile);
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileStorageException("Could not delete the file.", e);
        }
    }


    private Resource getResource(String fileName, String fileStoredPath) {
        Path path = Path.of(fileStoredPath);
        Path filePath = path.resolve(fileName).normalize();

        try {

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new MyFileNotFoundException("File not found " + fileName, e);
        }
    }
}
