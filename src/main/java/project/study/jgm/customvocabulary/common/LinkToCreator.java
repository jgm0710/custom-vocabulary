package project.study.jgm.customvocabulary.common;

import com.sun.istack.Nullable;
import org.springframework.hateoas.Link;
import project.study.jgm.customvocabulary.api.IndexApiController;
import project.study.jgm.customvocabulary.api.LoginApiController;
import project.study.jgm.customvocabulary.api.MemberApiController;
import project.study.jgm.customvocabulary.members.dto.search.MemberSearchDto;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class LinkToCreator {
    public static Link linkToRefresh() {
        return linkTo(LoginApiController.class).slash("/refresh").withRel("refresh");
    }

    public static Link linkToLogin() {
        return linkTo(LoginApiController.class).slash("login").withRel("login");
    }

    public static Link linkToIndex() {
        return linkTo(IndexApiController.class).withRel("index");
    }

    public static Link linkToGetMember(Long memberId) {
        return linkTo(MemberApiController.class).slash(memberId).withRel("get-member");
    }

    public static Link linkToGetMemberList(MemberSearchDto memberSearchDto) {
        Link link = getMemberListLink(memberSearchDto);
        return link.withSelfRel();
    }

    public static Link linkToGetMemberList(MemberSearchDto memberSearchDto, String rel) {
        Link link = getMemberListLink(memberSearchDto);
        return link.withRel(rel);
    }

    private static Link getMemberListLink(MemberSearchDto memberSearchDto) {
        String parameter = "";
        boolean firstFlag = true;


        if (memberSearchDto.getSearchType() != null) {
            parameter += getParameterConnector(firstFlag);
            firstFlag = false;
            parameter += "searchType=" + memberSearchDto.getSearchType().name();
        }

        if (memberSearchDto.getKeyword() != null) {
            parameter += getParameterConnector(firstFlag);
            firstFlag = false;
            parameter += "keyword=" + memberSearchDto.getKeyword();
        }

        parameter += getParameterConnector(firstFlag);
        firstFlag = false;
        parameter += "criteriaDto.pageNum=" + memberSearchDto.getCriteriaDto().getPageNum();
        parameter += getParameterConnector(firstFlag);
        parameter += "criteriaDto.limit=" + memberSearchDto.getCriteriaDto().getLimit();
        if (memberSearchDto.getSortType() != null) {
            parameter += getParameterConnector(firstFlag);
            parameter += "sortType=" + memberSearchDto.getSortType().name();
        }


        String linkUri = linkTo(MemberApiController.class).toUri().toString() + parameter;

        Link link = Link.of(linkUri);
        return link;
    }

    private static String getParameterConnector(boolean firstFlag) {
        if (firstFlag) {
            return "?";
        } else {
            return "&";
        }
    }
}
