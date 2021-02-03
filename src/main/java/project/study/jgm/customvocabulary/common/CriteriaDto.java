package project.study.jgm.customvocabulary.common;

import lombok.Data;

@Data
public class CriteriaDto {

    private int pageNum;

    private int limit;

    public int getOffset() {
        return (pageNum - 1) * limit;
    }
}
