package project.study.jgm.customvocabulary.vocabulary.word.upload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.study.jgm.customvocabulary.common.upload.FileStorage;
import project.study.jgm.customvocabulary.common.upload.FileStorageProperties;
import project.study.jgm.customvocabulary.common.upload.StoreFileDto;
import project.study.jgm.customvocabulary.vocabulary.word.upload.exception.NotImageTypeException;

import java.nio.file.Paths;

@Service
@Transactional(readOnly = true)
public class WordFileStorageService extends FileStorage {


    private final WordImageFileRepository wordImageFileRepository;

    @Autowired
    public WordFileStorageService(WordImageFileRepository wordImageFileRepository, FileStorageProperties fileStorageProperties) {
        super(Paths.get(fileStorageProperties.getWordImageDir())
                .toAbsolutePath().normalize(), fileStorageProperties);

        this.wordImageFileRepository = wordImageFileRepository;
    }



}
