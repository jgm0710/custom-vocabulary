package project.study.jgm.customvocabulary.common.upload;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.file.Path;

@Data
@AllArgsConstructor
public
class StoreFileDto {
    private String fileName;
    private Path fileStoredPath;
}
