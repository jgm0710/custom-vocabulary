package project.study.jgm.customvocabulary.members.dto.search;

import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;

@Component
public class MemberSearchValidator {

    public void validate(MemberSearchDto searchDto, BindingResult bindingResult) {
        //검색 조건이 없는데 검색 내용이 있는 경우
        if (searchDto.getSearchType() == null) {
            if (searchDto.getKeyword() != null) {
                bindingResult.reject("wrongSearch", "If the search condition is null, the content cannot be searched.");
            }
        }
    }
}
