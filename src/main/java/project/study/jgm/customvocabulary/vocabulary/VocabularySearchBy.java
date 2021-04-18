package project.study.jgm.customvocabulary.vocabulary;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 전체 검색을 할 것인지, 카테고리별 검색을 할 것인지 지정
 */
@Getter
@RequiredArgsConstructor
public enum VocabularySearchBy {
    BY_TOTAL("전체를 대상으로 목록 조회"),
    BY_CATEGORY("카테고리별 목록 조회");

    private final String description;
}
