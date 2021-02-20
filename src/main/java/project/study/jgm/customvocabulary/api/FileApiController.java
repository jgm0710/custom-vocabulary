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
import project.study.jgm.customvocabulary.common.dto.ResponseDto;
import project.study.jgm.customvocabulary.common.upload.*;
import project.study.jgm.customvocabulary.common.upload.exception.MyFileNotFoundException;
import project.study.jgm.customvocabulary.common.upload.exception.OriginalFilenameNotFoundException;

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


    @CrossOrigin
    @PostMapping("/bbs/uploadFile")
    @PreAuthorize("hasRole('ROLE_USER')")

    public ResponseEntity<ResponseDto<UploadFileResponseDto>> uploadBbsFile(
            @RequestParam("file") MultipartFile file
    ) {
        BbsUploadFile bbsUploadFile;
        try {
            bbsUploadFile = bbsFileStorageService.uploadBbsFile(file);
        } catch (OriginalFilenameNotFoundException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }
        UploadFileResponseDto uploadFileResponseDto = modelMapper.map(bbsUploadFile, UploadFileResponseDto.class);

        return ResponseEntity.ok(new ResponseDto<>(uploadFileResponseDto, ADD_FILE_TO_BBS_SUCCESSFULLY));
    }

    @CrossOrigin
    @PostMapping("/bbs/uploadMultipleFiles")
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

        return ResponseEntity.ok(new ResponseDto<>(uploadFileResponseDtos, ADD_FILE_LIST_TO_BBS_SUCCESSFULLY));
    }

    @CrossOrigin
    @GetMapping("/bbs/downloadFile/{fileName:.+}")
    public ResponseEntity<?> downloadBbsFile(
            @PathVariable String fileName
    ) {

        BbsUploadFile bbsUploadFile;
        Resource resource;

        try {
            bbsUploadFile = bbsFileStorageService.getBbsUploadFileByFileName(fileName);
            resource = bbsFileStorageService.loadBbsFileAsResource(bbsUploadFile.getId());
        } catch (MyFileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        }

        String contentType = bbsUploadFile.getFileType();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/bbs/displayThumbnail/{fileName:.+}")
    public ResponseEntity<?> displayThumbnailOfBbsImage(
            @PathVariable String fileName
    ) {
        BbsUploadFile bbsUploadFile;
        Resource resource;

        try {
            bbsUploadFile = bbsFileStorageService.getBbsUploadFileByFileName(fileName);
            resource = bbsFileStorageService.loadBbsThumbnailAsResource(bbsUploadFile.getId());
        } catch (MyFileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        }

        String contentType = bbsUploadFile.getFileType();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

}
