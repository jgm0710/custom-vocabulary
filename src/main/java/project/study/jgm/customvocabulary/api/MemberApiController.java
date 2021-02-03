package project.study.jgm.customvocabulary.api;

import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import project.study.jgm.customvocabulary.common.LinkToVo;
import project.study.jgm.customvocabulary.common.MessageDto;
import project.study.jgm.customvocabulary.common.EntityModelCreator;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRole;
import project.study.jgm.customvocabulary.members.MemberService;
import project.study.jgm.customvocabulary.members.dto.MemberCreateDto;
import project.study.jgm.customvocabulary.members.dto.MemberDetailDto;
import project.study.jgm.customvocabulary.members.dto.MemberUpdateDto;
import project.study.jgm.customvocabulary.members.dto.PasswordUpdateDto;
import project.study.jgm.customvocabulary.members.exception.MemberNotFoundException;
import project.study.jgm.customvocabulary.security.CurrentUser;
import project.study.jgm.customvocabulary.security.exception.PasswordMismatchException;

import javax.validation.Valid;

import java.awt.print.Pageable;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static project.study.jgm.customvocabulary.common.LinkToVo.*;

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
        Link loginLink = linkToLogin();
        memberDetailEntityModel.add(loginLink);

        return ResponseEntity.created(loginLink.toUri()).body(memberDetailEntityModel);
    }

    @GetMapping("/{memberId}")
    public ResponseEntity getMember(@PathVariable("memberId") Long memberId, @CurrentUser Member member) {
        if (member == null) {
            EntityModel messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(MessageDto.UN_AUTHENTICATION), MemberApiController.class, memberId);
            messageResponse.add(linkToRefresh());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(messageResponse);
        }

        try {
            Member findMember = memberService.getMember(memberId);
            if (!member.getRoles().contains(MemberRole.ADMIN)) {
                if (!findMember.equals(member)) {
                    EntityModel messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(MessageDto.GET_DIFFERENT_MEMBER_INFO), MemberApiController.class, memberId);
                    messageResponse.add(linkToIndex());

                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(messageResponse);
                }
            }

            MemberDetailDto memberDetailDto = MemberDetailDto.memberToDetail(findMember);

            EntityModel memberDetailResponse = EntityModelCreator.createMemberDetailResponse(memberDetailDto, MemberApiController.class, memberId);
            memberDetailResponse.add(linkToIndex());

            return ResponseEntity.ok(memberDetailResponse);

        } catch (MemberNotFoundException e) {
            EntityModel messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(e.getMessage()), MemberApiController.class, memberId);
            messageResponse.add(linkToIndex());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageResponse);
        }
    }

    @PutMapping("/{memberId}")
    public ResponseEntity modifyMember(
            @PathVariable("memberId") Long memberId,
            @RequestParam String password,
            @RequestBody MemberUpdateDto memberUpdateDto,
            @CurrentUser Member member) {

        if (member == null) {
            EntityModel messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(MessageDto.UN_AUTHENTICATION), MemberApiController.class, memberId);
            messageResponse.add(linkToIndex());
            messageResponse.add(linkToRefresh());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(messageResponse);
        }

        try {
            memberService.modifyMember(memberId, password, memberUpdateDto);

            if (!memberId.equals(member.getId())) {
                EntityModel messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(MessageDto.MODIFY_DIFFERENT_MEMBER_INFO), MemberApiController.class, memberId);
                messageResponse.add(linkToIndex());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(messageResponse);
            }

            EntityModel messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(MessageDto.MODIFIED_SUCCESSFULLY), MemberApiController.class, memberId);
            messageResponse.add(linkToIndex());
            messageResponse.add(linkToGetMember(memberId));

            return ResponseEntity.ok(messageResponse);
        } catch (MemberNotFoundException e) {
            EntityModel messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(e.getMessage()), MemberApiController.class, memberId);
            messageResponse.add(linkToIndex());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageResponse);
        } catch (PasswordMismatchException e) {
            EntityModel messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(e.getMessage()), MemberApiController.class, memberId);
            messageResponse.add(linkToIndex());

            return ResponseEntity.badRequest().body(messageResponse);
        }
    }

    @PutMapping("/password/{memberId}")
    public ResponseEntity updatePassword(@PathVariable("memberId") Long memberId,
                                         @RequestBody @Valid PasswordUpdateDto passwordUpdateDto,
                                         @CurrentUser Member member) {

        if (member == null) {
            EntityModel messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(MessageDto.UN_AUTHENTICATION), MemberApiController.class, "password", memberId);
            messageResponse.add(linkToIndex());
            messageResponse.add(linkToRefresh());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(messageResponse);
        }
        try {
            memberService.updatePassword(memberId, passwordUpdateDto.getOldPassword(), passwordUpdateDto.getNewPassword());

            if (!memberId.equals(member.getId())) {
                EntityModel messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(MessageDto.MODIFY_DIFFERENT_MEMBER_INFO), MemberApiController.class, "password", memberId);
                messageResponse.add(linkToIndex());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(messageResponse);
            }

            EntityModel messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(MessageDto.CHANGED_PASSWORD), MemberApiController.class, "password", memberId);
            messageResponse.add(linkToLogin());
            messageResponse.add(linkToIndex());

            return ResponseEntity.ok(messageResponse);
        } catch (MemberNotFoundException e) {
            EntityModel messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(e.getMessage()), MemberApiController.class, "password", memberId);
            messageResponse.add(linkToIndex());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageResponse);
        } catch (PasswordMismatchException e) {
            EntityModel messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(e.getMessage()), MemberApiController.class, "password", memberId);
            messageResponse.add(linkToIndex());

            return ResponseEntity.badRequest().body(messageResponse);
        }
    }

    @PutMapping("/secession/{memberId}")
    public ResponseEntity secession(@PathVariable("memberId") Long memberId,
                                    @CurrentUser Member member) {

        if (member == null) {
            EntityModel messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(MessageDto.UN_AUTHENTICATION), MemberApiController.class, "secession", memberId);
            messageResponse.add(linkToIndex());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(messageResponse);
        }

        try {
            memberService.secession(memberId);
            if (!memberId.equals(member.getId())) {
                EntityModel messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(MessageDto.MODIFY_DIFFERENT_MEMBER_INFO), MemberApiController.class, "secession", memberId);
                messageResponse.add(linkToIndex());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(messageResponse);
            }
            EntityModel messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(MessageDto.SECESSION_SUCCESSFULLY), MemberApiController.class, "secession", memberId);
            messageResponse.add(linkToIndex());

            return ResponseEntity.ok(messageResponse);
        } catch (MemberNotFoundException e) {
            EntityModel messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(e.getMessage()), MemberApiController.class, "secession", memberId);
            messageResponse.add(linkToIndex());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageResponse);
        }
    }

    /**
     * ADMIN
     */

    /*@GetMapping
    public ResponseEntity getMemberList(

    )*/
}
