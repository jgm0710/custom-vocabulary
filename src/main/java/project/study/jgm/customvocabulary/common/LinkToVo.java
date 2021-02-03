package project.study.jgm.customvocabulary.common;

import org.springframework.hateoas.Link;
import project.study.jgm.customvocabulary.api.IndexApiController;
import project.study.jgm.customvocabulary.api.LoginApiController;
import project.study.jgm.customvocabulary.api.MemberApiController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class LinkToVo {
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
}
