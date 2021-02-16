package project.study.jgm.customvocabulary.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
public class ResponseDto<T> {

    private T data;

    private String message;

    public ResponseDto() {
        this.data = null;
        this.message = null;
    }

    public ResponseDto(String message) {
        this.message = message;
        this.data = null;
    }
}
