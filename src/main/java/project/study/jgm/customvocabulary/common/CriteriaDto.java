package project.study.jgm.customvocabulary.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CriteriaDto {

    private int pageNum;

    private int limit;

    public int getOffset() {
        return (pageNum - 1) * limit;
    }
}
