package project.study.jgm.customvocabulary.common;

import lombok.Data;

@Data
public class PaginationDto {

    private int totalCount;

    private CriteriaDto criteriaDto;

    private int startPage;

    private int endPage;

    private boolean prev;

    private boolean next;

    private int totalPage;

    private final int DISPLAY_PAGE_NUMBER = 10;

    public PaginationDto(int totalCount, CriteriaDto criteriaDto) {
        this.totalCount = totalCount;
        this.criteriaDto = criteriaDto;

        init();
    }

    private void init() {

        endPage = (int) (Math.ceil(criteriaDto.getPageNum() / (double) DISPLAY_PAGE_NUMBER) * DISPLAY_PAGE_NUMBER);

        startPage = (endPage - DISPLAY_PAGE_NUMBER) + 1;

        int realEndPage = (int) Math.ceil(totalCount / (double) criteriaDto.getLimit());
        if (endPage > realEndPage) {
         endPage = realEndPage;
        }

        prev = startPage == 1 ? false : true;

        next = endPage * criteriaDto.getLimit() > totalCount ? false : true;

        totalPage = (totalCount / criteriaDto.getLimit()) + 1;
    }
}
