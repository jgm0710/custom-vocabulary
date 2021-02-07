package project.study.jgm.customvocabulary.bbs;

import org.springframework.validation.BindingResult;
import project.study.jgm.customvocabulary.bbs.dto.BbsSearchDto;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRole;
import project.study.jgm.customvocabulary.members.dto.search.MemberSearchDto;

public class BbsSearchValidator {
    public void validate(BbsSearchDto searchDto, Member member, BindingResult bindingResult) {
        //검색 조건이 없는데 검색 내용이 있는 경우
        if (searchDto.getSearchType() == null) {
            if (searchDto.getKeyword() != null) {
                addWrongSearchToBindingResult(bindingResult);
            }
        }

        //관리자 권한이 없는 사용자가 삭제된 게시글 목록을 조회하는 경우
        if (!(searchDto.getBbsStatus() == BbsStatus.REGISTER)) {
            if (member == null) {
                addUnauthorizedSearchToBindingResult(bindingResult);
            } else {
                if (!member.getRoles().contains(MemberRole.ADMIN)) {
                    addUnauthorizedSearchToBindingResult(bindingResult);
                }
            }
        }


    }

    private void addWrongSearchToBindingResult(BindingResult bindingResult) {
        bindingResult.reject("wrongSearch", "If the search condition is null, the content cannot be searched.");
    }

    private void addUnauthorizedSearchToBindingResult(BindingResult bindingResult) {
        bindingResult.rejectValue("UnauthorizedSearch", "Users cannot view the list of deleted posts.");
    }
}
