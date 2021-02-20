package project.study.jgm.customvocabulary.common.upload;

import lombok.NoArgsConstructor;
import net.coobird.thumbnailator.Thumbnailator;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import project.study.jgm.customvocabulary.bbs.upload.BbsUploadFile;
import project.study.jgm.customvocabulary.common.upload.exception.FileStorageException;
import project.study.jgm.customvocabulary.common.upload.exception.MyFileNotFoundException;
import project.study.jgm.customvocabulary.common.upload.exception.OriginalFilenameNotFoundException;
import project.study.jgm.customvocabulary.vocabulary.word.upload.exception.NotImageTypeException;

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

public abstract class FileStorage {

    protected final Path fileStorageLocation;

    protected final FileStorageProperties fileStorageProperties;

    public FileStorage(Path fileStorageLocation, FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = fileStorageLocation;
        this.fileStorageProperties = fileStorageProperties;
    }

    protected StoreFileDto storeFile(MultipartFile file) {

        String originalFilename;

        if (file.getOriginalFilename() != null) {
            originalFilename = file.getOriginalFilename();
        } else {
            throw new OriginalFilenameNotFoundException();
        }

        String fileName = UUID.randomUUID().toString() + "_" + StringUtils.cleanPath(originalFilename);

        Path finalFileStorageLocation = createTodayDir(fileStorageLocation);

        try {
            if (fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename invalid path sequence " + fileName);
            }

//            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Path targetLocation = finalFileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            if (checkImageType(file)) {
                createThumbnail(file, fileName, finalFileStorageLocation, fileStorageProperties);

            }

            return new StoreFileDto(fileName, finalFileStorageLocation);
        } catch (IOException e) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", e);
        }
    }

    protected StoreFileDto storeImageFile(MultipartFile file) {

        if (!checkImageType(file)) {
            throw new NotImageTypeException();
        }

        return this.storeFile(file);
    }

    private void createThumbnail(MultipartFile file, String fileName, Path finalFileStorageLocation, FileStorageProperties fileStorageProperties) throws IOException {
        Path targetLocation;
        String thumbnailPrefix = fileStorageProperties.getThumbnailPrefix();
        targetLocation = finalFileStorageLocation.resolve(thumbnailPrefix + fileName);
        File targetFile = targetLocation.toFile();
        FileOutputStream thumbnail = new FileOutputStream(targetFile);

        Thumbnailator.createThumbnail(file.getInputStream(), thumbnail, 100, 100);

        thumbnail.close();
    }

    private Path createTodayDir(Path fileStorageLocation) {
        LocalDate now = LocalDate.now();
        String year = Integer.toString(now.getYear());
        String month = Integer.toString(now.getMonthValue());
        String dayOfMonth = Integer.toString(now.getDayOfMonth());
        Path datePath = Paths.get(year, month, dayOfMonth);
        Path finalFileStorageLocation = fileStorageLocation.resolve(datePath);

        try {
            if (Files.notExists(finalFileStorageLocation)) {
                Files.createDirectories(finalFileStorageLocation);
            }
        } catch (IOException e) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", e);
        }
        return finalFileStorageLocation;
    }

    protected boolean checkImageType(MultipartFile file) {
        if (file.getContentType() != null) {
            String contentType = file.getContentType();
            return contentType.startsWith("image");
        } else {
            return false;
        }
    }

    protected Resource getResource(String fileName, String fileStoredPath) {
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

    protected String getThumbnailName(BbsUploadFile bbsUploadFile) {
        String thumbnailPrefix = fileStorageProperties.getThumbnailPrefix();
        return thumbnailPrefix + bbsUploadFile.getFileName();
    }

    protected String getFileDownloadUri(String requestPrefix, String fileName) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(requestPrefix)
                .path("/" + fileName)
                .toUriString();
    }
}
