package project.study.jgm.customvocabulary.api;

import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import project.study.jgm.customvocabulary.common.MessageDto;
import project.study.jgm.customvocabulary.common.EntityModelCreator;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRole;
import project.study.jgm.customvocabulary.members.MemberService;
import project.study.jgm.customvocabulary.members.dto.MemberCreateDto;
import project.study.jgm.customvocabulary.members.dto.MemberDetailDto;
import project.study.jgm.customvocabulary.members.dto.MemberUpdateDto;
import project.study.jgm.customvocabulary.members.dto.PasswordUpdateDto;
import project.study.jgm.customvocabulary.security.CurrentUser;
import project.study.jgm.customvocabulary.security.exception.PasswordMismatchException;

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
        EntityModel memberDetailEntityModel = EntityModelCreator.createMemberDetailResponse(memberDetailDto, MemberApiController.class);
        Link loginLink = linkTo(LoginApiController.class).slash("/login").withRel("login");
        memberDetailEntityModel.add(loginLink);

        return ResponseEntity.created(loginLink.toUri()).body(memberDetailEntityModel);
    }

    @GetMapping("/{memberId}")
    public ResponseEntity getMember(@PathVariable("memberId") Long memberId, @CurrentUser Member member) {
        if (member == null) {
            EntityModel messageResponse = EntityModelCreator.createMessageResponse(new MessageDto("access_token이 유효하지 않습니다."), MemberApiController.class, memberId);
            messageResponse.add(linkTo(LoginApiController.class).slash("/refresh").withRel("refresh"));

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(messageResponse);
        }

        Member findMember = memberService.getMember(memberId);

        if (!member.getRoles().contains(MemberRole.ADMIN)) {
            if (!findMember.equals(member)) {
                EntityModel messageResponse = EntityModelCreator.createMessageResponse(new MessageDto("다른 회원의 정보는 조회가 불가능합니다."), MemberApiController.class, memberId);
                messageResponse.add(linkTo(IndexApiController.class).withRel("index"));

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(messageResponse);
            }
        }

        MemberDetailDto memberDetailDto = MemberDetailDto.memberToDetail(findMember);

        EntityModel memberDetailResponse = EntityModelCreator.createMemberDetailResponse(memberDetailDto, MemberApiController.class, memberId);
        memberDetailResponse.add(linkTo(IndexApiController.class).withRel("index"));

        return ResponseEntity.ok(memberDetailResponse);
    }

    @PutMapping("/{memberId}")
    public ResponseEntity modifyMember(
            @PathVariable("memberId") Long memberId,
            @RequestParam String password,
            @RequestBody MemberUpdateDto memberUpdateDto,
            @CurrentUser Member member) {

        if (member == null) {
            EntityModel messageResponse = EntityModelCreator.createMessageResponse(new MessageDto("access_token이 유효하지 않습니다."), MemberApiController.class, memberId);
            messageResponse.add(linkTo(IndexApiController.class).withRel("index"));
            messageResponse.add(linkTo(LoginApiController.class).slash("refresh").withRel("refresh"));

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(messageResponse);
        }

        if (!memberId.equals(member.getId())) {
            EntityModel messageResponse = EntityModelCreator.createMessageResponse(new MessageDto("다른 회원의 정보는 수정할 수 없습니다."), MemberApiController.class, memberId);
            messageResponse.add(linkTo(IndexApiController.class).withRel("index"));

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(messageResponse);
        }

        try {
            memberService.modifyMember(memberId, password, memberUpdateDto);

            EntityModel messageResponse = EntityModelCreator.createMessageResponse(new MessageDto("회원 정보가 정상적으로 수정되었습니다."), MemberApiController.class, memberId);
            messageResponse.add(linkTo(IndexApiController.class).withRel("index"));
            messageResponse.add(linkTo(MemberApiController.class).slash(memberId).withRel("get-member"));

            return ResponseEntity.ok(messageResponse);
        } catch (PasswordMismatchException e) {
            EntityModel messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(e.getMessage()), MemberApiController.class, memberId);
            messageResponse.add(linkTo(IndexApiController.class).withRel("index"));

            return ResponseEntity.badRequest().body(messageResponse);
        }
    }

    @PutMapping("/password/{memberId}")
    public ResponseEntity updatePassword(@PathVariable("memberId") Long memberId,
                                         @RequestBody @Valid PasswordUpdateDto passwordUpdateDto) {

        return null;
    }

    @PutMapping("/secession/{memberId}")
    public ResponseEntity secession(@PathVariable("memberId") Long memberId,
                                    @CurrentUser Member member) {

        if (member == null) {
            EntityModel messageResponse = EntityModelCreator.createMessageResponse(new MessageDto("access_token이 유효하지 않습니다."), MemberApiController.class, "secession", memberId);
            messageResponse.add(linkTo(IndexApiController.class).withRel("index"));

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(messageResponse);
        }

//        if ()

        memberService.secession(memberId);
        return null;
    }
}
