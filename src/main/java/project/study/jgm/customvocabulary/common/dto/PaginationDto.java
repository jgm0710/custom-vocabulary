package project.study.jgm.customvocabulary.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class PaginationDto {

    private long totalCount;

    private CriteriaDto criteria;

    private int startPage;

    private int endPage;

    private boolean prev;

    private boolean next;

    private long totalPage;

    private final int NUMBER_OF_PAGES_TO_BE_DISPLAYED = 10;

    public PaginationDto(long totalCount, CriteriaDto criteria) {
        this.totalCount = totalCount;
        this.criteria = criteria;

        init();
    }

    private void init() {

        endPage = (int) (Math.ceil(criteria.getPageNum() / (double) NUMBER_OF_PAGES_TO_BE_DISPLAYED) * NUMBER_OF_PAGES_TO_BE_DISPLAYED);

        startPage = (endPage - NUMBER_OF_PAGES_TO_BE_DISPLAYED) + 1;

        int realEndPage = (int) Math.ceil(totalCount / (double) criteria.getLimit());
        if (endPage > realEndPage) {
            endPage = realEndPage;
        }

        prev = startPage == 1 ? false : true;

        next = endPage * criteria.getLimit() >= totalCount ? false : true;

        totalPage = (totalCount / criteria.getLimit()) + 1;
    }

    @JsonIgnore
    public int getNUMBER_OF_PAGES_TO_BE_DISPLAYED() {
        return NUMBER_OF_PAGES_TO_BE_DISPLAYED;
    }

    @JsonIgnore
    public int getLastPageOfPrevList() {
        return this.startPage - 1;
    }

    @JsonIgnore
    public int getFirstPageOfNextList() {
        return this.endPage + 1;
    }
}
