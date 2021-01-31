package project.study.jgm.customvocabulary.api;

import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import project.study.jgm.customvocabulary.common.AuthMessageDto;
import project.study.jgm.customvocabulary.common.EntityModelCreater;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRole;
import project.study.jgm.customvocabulary.members.MemberService;
import project.study.jgm.customvocabulary.members.dto.MemberCreateDto;
import project.study.jgm.customvocabulary.members.dto.MemberDetailDto;
import project.study.jgm.customvocabulary.members.dto.MemberUpdateDto;
import project.study.jgm.customvocabulary.security.CurrentUser;

import javax.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/members")
public class MemberApiController {

    private final MemberService memberService;

    /**
     * USER
     */
    @PostMapping
    public ResponseEntity join(@RequestBody @Valid MemberCreateDto memberCreateDto, Errors errors) {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        Member member = memberService.userJoin(memberCreateDto);
        MemberDetailDto memberDetailDto = MemberDetailDto.memberToDetail(member);

//        EntityModel<Member> memberEntityModel = MemberEntityModel.createDetailEntityModel(member);
        EntityModel memberDetailEntityModel = EntityModelCreater.createMemberDetailResponse(memberDetailDto, MemberApiController.class);

        Link loginLink = linkTo(LoginApiController.class).slash("/login").withRel("login");
        memberDetailEntityModel.add(loginLink);

        return ResponseEntity.created(loginLink.toUri()).body(memberDetailEntityModel);
    }

    @GetMapping("/{memberId}")
    public ResponseEntity getMember(@PathVariable("memberId") Long memberId, @CurrentUser Member member) {
        if (member == null) {
            EntityModel entityModel = AuthMessageDto.createEntityModel("access_token이 유효하지 않습니다.");
            entityModel.add(linkTo(MemberApiController.class).slash(memberId).withSelfRel());
            entityModel.add(linkTo(LoginApiController.class).slash("/refresh").withRel("refresh"));

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(entityModel);
        }

        Member findMember = memberService.getMember(memberId);

        if (!member.getRoles().contains(MemberRole.ADMIN)) {
            if (!findMember.equals(member)) {
                EntityModel entityModel = AuthMessageDto.createEntityModel("다른 회원의 정보는 조회가 불가능합니다.");
                entityModel.add(linkTo(MemberApiController.class).slash(memberId).withSelfRel());
                entityModel.add(linkTo(IndexApiController.class).withRel("index"));

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(entityModel);
            }
        }

        MemberDetailDto memberDetailDto = MemberDetailDto.memberToDetail(findMember);

        EntityModel memberDetailResponse = EntityModelCreater.createMemberDetailResponse(memberDetailDto, MemberApiController.class, memberId);
        memberDetailResponse.add(linkTo(IndexApiController.class).withRel("index"));

        return ResponseEntity.ok(memberDetailResponse);
    }

    @PutMapping("/{memberId}")
    public ResponseEntity modifyMember(
            @PathVariable("memberId") Long memberId,
            @RequestParam String password,
            @RequestBody MemberUpdateDto memberUpdateDto,
            @CurrentUser Member member) {

        memberService.modifyMember(memberId, memberUpdateDto);
        Member findMember = memberService.getMember(memberId);

//        EntityModel detailEntityModel = MemberEntityModel.createDetailEntityModel(findMember);
        MemberDetailDto memberDetailDto = MemberDetailDto.memberToDetail(findMember);
        EntityModel memberDetailResponse = EntityModelCreater.createMemberDetailResponse(memberDetailDto, MemberApiController.class, memberId);
        return ResponseEntity.ok(memberDetailResponse);
    }

    @PutMapping("/secession/{memberId}")
    public ResponseEntity secession(@PathVariable("memberId") Long memberId,
                                    @CurrentUser Member member) {

        if (member == null) {
            EntityModel entityModel = AuthMessageDto.createEntityModel("access_token이 유효하지 않습니다.");
            entityModel.add(linkTo(MemberApiController.class).slash("secession").slash(memberId).withSelfRel());
            entityModel.add(linkTo(IndexApiController.class).withRel("index"));

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(entityModel);
        }

//        if ()

        memberService.secession(memberId);
        return null;
    }
}
