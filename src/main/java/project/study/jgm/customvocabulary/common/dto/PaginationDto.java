package project.study.jgm.customvocabulary.common.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;

@Data
public class PaginationDto {

    private long totalCount;

    private CriteriaDto criteriaDto;

    private int startPage;

    private int endPage;

    private boolean prev;

    private boolean next;

    private long totalPage;

    private final int NUMBER_OF_PAGES_TO_BE_DISPLAYED = 10;

    public PaginationDto(long totalCount, CriteriaDto criteriaDto) {
        this.totalCount = totalCount;
        this.criteriaDto = criteriaDto;

        init();
    }

    private void init() {

        endPage = (int) (Math.ceil(criteriaDto.getPageNum() / (double) NUMBER_OF_PAGES_TO_BE_DISPLAYED) * NUMBER_OF_PAGES_TO_BE_DISPLAYED);

        startPage = (endPage - NUMBER_OF_PAGES_TO_BE_DISPLAYED) + 1;

        int realEndPage = (int) Math.ceil(totalCount / (double) criteriaDto.getLimit());
        if (endPage > realEndPage) {
         endPage = realEndPage;
        }

        prev = startPage == 1 ? false : true;

        next = endPage * criteriaDto.getLimit() >= totalCount ? false : true;

        totalPage = (totalCount / criteriaDto.getLimit()) + 1;
    }

    @JsonIgnore
    public int getNUMBER_OF_PAGES_TO_BE_DISPLAYED() {
        return NUMBER_OF_PAGES_TO_BE_DISPLAYED;
    }
}
