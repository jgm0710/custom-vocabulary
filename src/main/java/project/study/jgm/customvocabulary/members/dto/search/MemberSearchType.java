package project.study.jgm.customvocabulary.members.dto.search;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MemberSearchType {
    JOIN_ID,
    EMAIL,
    NAME,
    NICKNAME;
}
