package project.study.jgm.customvocabulary.common;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.validation.Errors;
import project.study.jgm.customvocabulary.common.dto.ListResponseDto;
import project.study.jgm.customvocabulary.common.dto.MessageVo;
import project.study.jgm.customvocabulary.members.dto.MemberAdminViewDto;
import project.study.jgm.customvocabulary.members.dto.MemberDetailDto;
import project.study.jgm.customvocabulary.security.dto.TokenDto;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class EntityModelCreator extends EntityModel<Errors> {

    public static <T> EntityModel<MemberDetailDto> createMemberDetailResponse(MemberDetailDto memberDetailDto, Class<T> controller, Object... slashs) {
        Link selfLink = getSelfLink(controller, slashs);
        return EntityModel.of(memberDetailDto, selfLink);
    }

    public static <T> EntityModel<MemberAdminViewDto> createMemberAdminViewResponse(MemberAdminViewDto memberAdminViewDto, Class<T> controller, Object... slashs) {
        Link selfLink = getSelfLink(controller, slashs);
        return EntityModel.of(memberAdminViewDto, selfLink);
    }

    public static <T> EntityModel<MessageVo> createMessageResponse(MessageVo messageVo, Class<T> controller, Object... slashs) {
        Link selfLink = getSelfLink(controller, slashs);
        return EntityModel.of(messageVo, selfLink);
    }

    public static <T> EntityModel<ListResponseDto> createListResponse(ListResponseDto listResponseDto) {
        return EntityModel.of(listResponseDto);
    }

    public static <T> EntityModel<TokenDto> createTokenResponse(TokenDto tokenDto, Class<T> controller, Object... slashs) {
        Link selfLink = getSelfLink(controller, slashs);
        return EntityModel.of(tokenDto, selfLink);
    }

    private static Link getSelfLink(Class<?> controller, Object[] slashs) {
        WebMvcLinkBuilder webMvcLinkBuilder = linkTo(controller);
        for (Object o :
                slashs) {
            webMvcLinkBuilder = addSlash(webMvcLinkBuilder, o);
        }
        Link link = webMvcLinkBuilder.withSelfRel();
        return link;
    }

    private static WebMvcLinkBuilder addSlash(WebMvcLinkBuilder webMvcLinkBuilder, Object slash) {
        return webMvcLinkBuilder.slash(slash);
    }
}
