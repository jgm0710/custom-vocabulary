package project.study.jgm.customvocabulary.common;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.validation.Errors;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.dto.MemberDetailDto;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class EntityModelCreater extends EntityModel<Errors>{

    public static <T> EntityModel createMemberDetailResponse(MemberDetailDto memberDetailDto, Class<T> controller, Object... slashs) {
        Link selfLink = getSelfLink(controller, slashs);
        return EntityModel.of(memberDetailDto, selfLink);
    }

//    public static <T> EntityModel

    private static Link getSelfLink(Class<?> controller, Object[] slashs) {
        WebMvcLinkBuilder webMvcLinkBuilder = linkTo(controller);
        for (Object o :
                slashs) {
            webMvcLinkBuilder.slash(o);
        }
        Link link = webMvcLinkBuilder.withSelfRel();
        return link;
    }

    private static WebMvcLinkBuilder addSlash(WebMvcLinkBuilder webMvcLinkBuilder, Object[] slash) {
        return webMvcLinkBuilder.slash(slash);
    }
}
