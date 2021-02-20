package project.study.jgm.customvocabulary.common.upload;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("file")
@Getter
@Setter
public class FileStorageProperties {

    private String bbsUploadDir;

    private String thumbnailPrefix;
}
