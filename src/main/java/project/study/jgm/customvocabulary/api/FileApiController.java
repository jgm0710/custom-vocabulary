package project.study.jgm.customvocabulary.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.study.jgm.customvocabulary.bbs.upload.BbsFileStorageService;
import project.study.jgm.customvocabulary.bbs.upload.BbsUploadFile;
import project.study.jgm.customvocabulary.bbs.upload.exception.BbsUploadFileNotFoundException;
import project.study.jgm.customvocabulary.common.dto.ResponseDto;
import project.study.jgm.customvocabulary.common.upload.*;
import project.study.jgm.customvocabulary.common.upload.exception.FileStorageException;
import project.study.jgm.customvocabulary.common.upload.exception.MyFileNotFoundException;
import project.study.jgm.customvocabulary.common.upload.exception.OriginalFilenameNotFoundException;
import project.study.jgm.customvocabulary.vocabulary.word.upload.WordFileStorageService;
import project.study.jgm.customvocabulary.vocabulary.word.upload.WordImageFile;
import project.study.jgm.customvocabulary.vocabulary.word.upload.exception.NotImageTypeException;
import project.study.jgm.customvocabulary.vocabulary.word.upload.exception.WordImageFileNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static project.study.jgm.customvocabulary.common.dto.MessageVo.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class FileApiController {

    private final ModelMapper modelMapper;

    private final BbsFileStorageService bbsFileStorageService;

    private final WordFileStorageService wordFileStorageService;


    private final String AAA = "AAA";

    /**
     * Bbs
     */

    @CrossOrigin
    @PostMapping(value = "/bbs/uploadFile")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ResponseDto<UploadFileResponseDto>> uploadBbsFile(
            @RequestParam("file") MultipartFile file
    ) {
        BbsUploadFile bbsUploadFile;
        try {
            bbsUploadFile = bbsFileStorageService.uploadBbsFile(file);
        } catch (OriginalFilenameNotFoundException | FileStorageException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }

        UploadFileResponseDto uploadFileResponseDto = modelMapper.map(bbsUploadFile, UploadFileResponseDto.class);

        return ResponseEntity.ok(new ResponseDto<>(uploadFileResponseDto, UPLOAD_BBS_FILE_SUCCESSFULLY));
    }


    @CrossOrigin
    @PostMapping(value = "/bbs/uploadMultipleFiles")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ResponseDto<List<UploadFileResponseDto>>> uploadBbsMultipleFiles(
            @RequestParam("files") MultipartFile[] files
    ) {

        List<UploadFileResponseDto> uploadFileResponseDtos = Arrays.stream(files)
                .map(file -> {
                    ResponseEntity<ResponseDto<UploadFileResponseDto>> responseDtoResponseEntity = uploadBbsFile(file);
                    ResponseDto<UploadFileResponseDto> responseDto = responseDtoResponseEntity.getBody();
                    return responseDto != null ? responseDto.getData() != null ? responseDto.getData() : null : null;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ResponseDto<>(uploadFileResponseDtos, UPLOAD_BBS_FILE_LIST_SUCCESSFULLY));
    }

    @GetMapping(value = "/bbs/downloadFile/{fileName:.+}")
    public ResponseEntity<?> downloadBbsFile(
            @PathVariable String fileName
    ) {

        BbsUploadFile bbsUploadFile;
        Resource resource;

        try {
            bbsUploadFile = bbsFileStorageService.getBbsUploadFileByFileName(fileName);
            resource = bbsFileStorageService.loadBbsUploadFileAsResource(bbsUploadFile.getId());
        } catch (BbsUploadFileNotFoundException | MyFileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        }

        MediaType parseContentType = getParseContentType(bbsUploadFile.getFileType());

        return ResponseEntity.ok()
                .contentType(parseContentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, getContentDispositionString(resource))
                .body(resource);
    }

    @GetMapping(value = "/bbs/displayThumbnail/{fileName:.+}")
    public ResponseEntity<?> displayThumbnailOfBbsImage(
            @PathVariable String fileName
    ) {
        BbsUploadFile bbsUploadFile;
        Resource resource;

        try {
            bbsUploadFile = bbsFileStorageService.getBbsUploadFileByFileName(fileName);
            resource = bbsFileStorageService.loadThumbnailOfBbsUploadFileAsResource(bbsUploadFile.getId());
        } catch (BbsUploadFileNotFoundException | MyFileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        }

        MediaType parseContentType = getParseContentType(bbsUploadFile.getFileType());

        return ResponseEntity.ok()
                .contentType(parseContentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, getContentDispositionString(resource))
                .body(resource);
    }


    @CrossOrigin
    @PostMapping(value = "/vocabulary/word/uploadImageFile")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<? extends ResponseDto<?>> uploadWordImageFile(
            @RequestParam("file") MultipartFile file
    ) {

        WordImageFile wordImageFile;
        try {
            wordImageFile = wordFileStorageService.uploadWordImageFile(file);
        } catch (NotImageTypeException | OriginalFilenameNotFoundException | FileStorageException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }

        UploadFileResponseDto uploadFileResponseDto = modelMapper.map(wordImageFile, UploadFileResponseDto.class);

        return ResponseEntity.ok(new ResponseDto<>(uploadFileResponseDto, "단어 이미지 파일이 정상적으로 업로드되었습니다.."));
    }

    @GetMapping(value = "/vocabulary/word/downloadImageFile/{fileName:.+}")
    public ResponseEntity<?> downloadWordFile(
            @PathVariable String fileName
    ) {

        WordImageFile wordImageFile;
        Resource resource;

        try {
            wordImageFile = wordFileStorageService.getWordImageFileByFileName(fileName);
            resource = wordFileStorageService.loadWordImageFileAsResource(wordImageFile.getId());
        } catch (WordImageFileNotFoundException | MyFileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

        String contentType = wordImageFile.getFileType();
        MediaType parseContentType = getParseContentType(wordImageFile.getFileType());

        return ResponseEntity.ok()
                .contentType(parseContentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, getContentDispositionString(resource))
                .body(resource);
    }

    @GetMapping(value = "/vocabulary/word/displayThumbnail/{fileName:.+}")
    public ResponseEntity<?> displayThumbnailOfWordImage(
            @PathVariable String fileName
    ) {
        WordImageFile wordImageFile;
        Resource resource;

        try {
            wordImageFile = wordFileStorageService.getWordImageFileByFileName(fileName);
            resource = wordFileStorageService.loadThumbnailOfWordImageFileAsResource(wordImageFile.getId());
        } catch (WordImageFileNotFoundException | MyFileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        }

        MediaType parseContentType = getParseContentType(wordImageFile.getFileType());

        return ResponseEntity.ok()
                .contentType(parseContentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, getContentDispositionString(resource))
                .body(resource);
    }

    private String getContentDispositionString(Resource resource) {
        return "attachment; filename=\"" + resource.getFilename() + "\"";
    }

    private MediaType getParseContentType(String fileType) {
        return MediaType.parseMediaType(fileType);
    }
}
