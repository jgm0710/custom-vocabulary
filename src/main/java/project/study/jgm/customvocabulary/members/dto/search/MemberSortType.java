package project.study.jgm.customvocabulary.members.dto.search;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MemberSortType {
    LATEST,
    OLDEST,
    BBS_COUNT_DESC,
    BBS_COUNT_ASC,
    SHARED_VOCABULARY_COUNT_DESC,
    SHARED_VOCABULARY_COUNT_ASC;
}
