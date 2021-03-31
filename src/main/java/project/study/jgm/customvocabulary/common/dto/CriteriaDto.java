package project.study.jgm.customvocabulary.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@Builder
@AllArgsConstructor
public class CriteriaDto {

    @Min(value = 1, message = "첫 페이지는 1페이지입니다.")
    private int pageNum;

    @Min(value = 1, message = "1개 미만의 값은 검색할 수 없습니다.")
    @Max(value = 100, message = "100개 초과의 값은 검색할 수 없습니다.")
    private int limit;

    @JsonIgnore
    public int getOffset() {
        return (pageNum - 1) * limit;
    }

    public CriteriaDto() {
        this.pageNum = 1;
        this.limit = 15;
    }
}
