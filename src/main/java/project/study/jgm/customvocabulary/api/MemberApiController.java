package project.study.jgm.customvocabulary.api;

import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import project.study.jgm.customvocabulary.common.EntityModelCreator;
import project.study.jgm.customvocabulary.common.dto.CriteriaDto;
import project.study.jgm.customvocabulary.common.dto.ListResponseDto;
import project.study.jgm.customvocabulary.common.dto.MessageDto;
import project.study.jgm.customvocabulary.common.dto.PaginationDto;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRole;
import project.study.jgm.customvocabulary.members.MemberService;
import project.study.jgm.customvocabulary.members.dto.*;
import project.study.jgm.customvocabulary.members.dto.search.MemberSearchDto;
import project.study.jgm.customvocabulary.members.dto.search.MemberSearchValidator;
import project.study.jgm.customvocabulary.members.exception.MemberAlreadyHasAuthorityException;
import project.study.jgm.customvocabulary.members.exception.MemberNotFoundException;
import project.study.jgm.customvocabulary.security.CurrentUser;
import project.study.jgm.customvocabulary.security.exception.PasswordMismatchException;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

import static project.study.jgm.customvocabulary.common.LinkToCreator.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/members")
public class MemberApiController {

    private final MemberService memberService;

    private final MemberSearchValidator memberSearchValidator;


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
        var memberDetailEntityModel = EntityModelCreator.createMemberDetailResponse(memberDetailDto, MemberApiController.class);
        Link loginLink = linkToLogin();
        memberDetailEntityModel.add(loginLink);

        return ResponseEntity.created(loginLink.toUri()).body(memberDetailEntityModel);
    }

    @GetMapping("/{memberId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity getMember(@PathVariable("memberId") Long memberId, @CurrentUser Member member) {

        member.getRoles().forEach(memberRole -> System.out.println("memberRole.getRoleName() = " + memberRole.getRoleName()));

        try {
            Member findMember = memberService.getMember(memberId);
            if (member.getRoles().contains(MemberRole.ADMIN)) {
                MemberAdminViewDto memberAdminViewDto = MemberAdminViewDto.memberToAdminView(findMember);

                EntityModel<MemberAdminViewDto> memberAdminViewResponse = EntityModelCreator.createMemberAdminViewResponse(memberAdminViewDto, MemberApiController.class, memberId);
                memberAdminViewResponse.add(linkToIndex());

                return ResponseEntity.ok(memberAdminViewResponse);
            } else {
                if (!findMember.equals(member)) {
                    var messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(MessageDto.GET_DIFFERENT_MEMBER_INFO), MemberApiController.class, memberId);
                    messageResponse.add(linkToIndex());

                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(messageResponse);
                }
                MemberDetailDto memberDetailDto = MemberDetailDto.memberToDetail(findMember);

                var memberDetailResponse = EntityModelCreator.createMemberDetailResponse(memberDetailDto, MemberApiController.class, memberId);
                memberDetailResponse.add(linkToIndex());

                return ResponseEntity.ok(memberDetailResponse);
            }

        } catch (MemberNotFoundException e) {
            var messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(e.getMessage()), MemberApiController.class, memberId);
            messageResponse.add(linkToIndex());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageResponse);
        }
    }

    @PutMapping("/{memberId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity modifyMember(
            @PathVariable("memberId") Long memberId,
            @RequestParam String password,
            @RequestBody @Valid MemberUpdateDto memberUpdateDto,
            @CurrentUser Member member,
            Errors errors) {

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            memberService.modifyMember(memberId, password, memberUpdateDto);

            if (!memberId.equals(member.getId())) {
                var messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(MessageDto.MODIFY_DIFFERENT_MEMBER_INFO), MemberApiController.class, memberId);
                messageResponse.add(linkToIndex());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(messageResponse);
            }

            var messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(MessageDto.MODIFIED_SUCCESSFULLY), MemberApiController.class, memberId);
            messageResponse.add(linkToIndex());
            messageResponse.add(linkToGetMember(memberId));

            return ResponseEntity.ok(messageResponse);
        } catch (MemberNotFoundException e) {
            var messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(e.getMessage()), MemberApiController.class, memberId);
            messageResponse.add(linkToIndex());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageResponse);
        } catch (PasswordMismatchException e) {
            var messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(e.getMessage()), MemberApiController.class, memberId);
            messageResponse.add(linkToIndex());

            return ResponseEntity.badRequest().body(messageResponse);
        }
    }

    @PutMapping("/password/{memberId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity updatePassword(@PathVariable("memberId") Long memberId,
                                         @RequestBody @Valid PasswordUpdateDto passwordUpdateDto,
                                         @CurrentUser Member member,
                                         Errors errors) {

        try {
            memberService.updatePassword(memberId, passwordUpdateDto.getOldPassword(), passwordUpdateDto.getNewPassword());

            if (!memberId.equals(member.getId())) {
                var messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(MessageDto.MODIFY_DIFFERENT_MEMBER_INFO), MemberApiController.class, "password", memberId);
                messageResponse.add(linkToIndex());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(messageResponse);
            }

            var messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(MessageDto.CHANGED_PASSWORD), MemberApiController.class, "password", memberId);
            messageResponse.add(linkToLogin());
            messageResponse.add(linkToIndex());

            return ResponseEntity.ok(messageResponse);
        } catch (MemberNotFoundException e) {
            var messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(e.getMessage()), MemberApiController.class, "password", memberId);
            messageResponse.add(linkToIndex());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageResponse);
        } catch (PasswordMismatchException e) {
            var messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(e.getMessage()), MemberApiController.class, "password", memberId);
            messageResponse.add(linkToIndex());

            return ResponseEntity.badRequest().body(messageResponse);
        }
    }

    @PutMapping("/secession/{memberId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity secession(@PathVariable("memberId") Long memberId,
                                    @CurrentUser Member member) {

        try {
            memberService.secession(memberId);
            if (!memberId.equals(member.getId())) {
                var messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(MessageDto.MODIFY_DIFFERENT_MEMBER_INFO), MemberApiController.class, "secession", memberId);
                messageResponse.add(linkToIndex());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(messageResponse);
            }
            var messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(MessageDto.SECESSION_SUCCESSFULLY), MemberApiController.class, "secession", memberId);
            messageResponse.add(linkToIndex());

            return ResponseEntity.ok(messageResponse);
        } catch (MemberNotFoundException e) {
            var messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(e.getMessage()), MemberApiController.class, "secession", memberId);
            messageResponse.add(linkToIndex());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageResponse);
        }
    }

    /**
     * ADMIN
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity getMemberList(
            @ModelAttribute @Valid MemberSearchDto memberSearchDto,
            BindingResult bindingResult,
            @CurrentUser Member member
    ) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult);
        }

        memberSearchValidator.validate(memberSearchDto, bindingResult);
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult);
        }

        List<MemberAdminViewDto> memberAdminViewDtoList = getMemberAdminViewDtos(memberSearchDto);
        PaginationDto paginationDto = getPaginationDto(memberSearchDto);
        ListResponseDto<Object> listResponseDto = getListResponseDto(memberAdminViewDtoList, paginationDto);

        EntityModel<ListResponseDto> listResponse = EntityModelCreator.createListResponse(listResponseDto);
        listResponse.add(linkToGetMemberList(memberSearchDto));     //selfLink 추가
        addLinkInfoOfPrevAndNextLinkToListResponse(memberSearchDto, paginationDto, listResponse);

        return ResponseEntity.ok(listResponse);
    }

    @PutMapping("/ban/{memberId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity ban(@PathVariable("memberId") Long memberId) {

        try {
            memberService.ban(memberId);
        } catch (MemberNotFoundException e) {
            EntityModel<MessageDto> messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(e.getMessage()), MemberApiController.class, "ban", memberId);
            messageResponse.add(linkToIndex());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageResponse);
        }

        EntityModel<MessageDto> messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(MessageDto.BAN_SUCCESSFULLY), MemberApiController.class, "ban", memberId);
        messageResponse.add(linkToGetMember(memberId));
        messageResponse.add(linkToIndex());

        return ResponseEntity.ok(messageResponse);
    }

    @PutMapping("/changeToUser/{memberId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity changeMemberRoleToUser(
            @PathVariable("memberId") Long memberId
    ) {

        try {
            memberService.changeMemberRoleToUser(memberId);
        } catch (MemberNotFoundException e) {
            EntityModel<MessageDto> messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(e.getMessage()), MemberApiController.class, "changeToUser", memberId);
            messageResponse.add(linkToIndex());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageResponse);
        } catch (MemberAlreadyHasAuthorityException e) {
            EntityModel<MessageDto> messageResponse = EntityModelCreator.createMessageResponse(new MessageDto(e.getMessage()), MemberApiController.class, "changeToUser", memberId);
            messageResponse.add(linkToIndex());

            return ResponseEntity.badRequest().body(messageResponse);
        }

        EntityModel<MessageDto> messageResponse = EntityModelCreator.createMessageResponse(
                new MessageDto(MessageDto.CHANGE_MEMBER_ROLE_TO_USER_SUCCESSFULLY),
                MemberApiController.class,
                "changeToUser", memberId
        );
        messageResponse.add(linkToGetMember(memberId));
        messageResponse.add(linkToIndex());

        return ResponseEntity.ok(messageResponse);
    }

    /**
     * PRIVATE
     */

    private void addLinkInfoOfPrevAndNextLinkToListResponse(MemberSearchDto memberSearchDto, PaginationDto paginationDto, EntityModel<ListResponseDto> listResponse) {
        MemberSearchDto tempSearchDto = MemberSearchDto.builder()
                .searchType(memberSearchDto.getSearchType())
                .keyword(memberSearchDto.getKeyword())
                .criteriaDto(new CriteriaDto(memberSearchDto.getCriteriaDto().getPageNum(), memberSearchDto.getCriteriaDto().getLimit()))
                .sortType(memberSearchDto.getSortType())
                .build();

        if (paginationDto.isPrev()) {
            tempSearchDto.updatePage(paginationDto.getLastPageOfPrevList());
            listResponse.add(linkToGetMemberList(tempSearchDto, "prev-list"));
        }
        if (paginationDto.isNext()) {
            tempSearchDto.updatePage(paginationDto.getFirstPageOfNextList());
            listResponse.add(linkToGetMemberList(tempSearchDto, "next-list"));
        }
    }

    private ListResponseDto<Object> getListResponseDto(List<MemberAdminViewDto> memberAdminViewDtoList, PaginationDto paginationDto) {
        ListResponseDto<Object> listResponseDto = ListResponseDto.builder()
                .data(memberAdminViewDtoList)
                .paging(paginationDto)
                .build();
        return listResponseDto;
    }

    private PaginationDto getPaginationDto(MemberSearchDto memberSearchDto) {
        Long totalCount = memberService.getTotalCount(memberSearchDto);
        PaginationDto paginationDto = new PaginationDto(totalCount, memberSearchDto.getCriteriaDto());
        return paginationDto;
    }

    private List<MemberAdminViewDto> getMemberAdminViewDtos(MemberSearchDto memberSearchDto) {
        List<MemberAdminViewDto> memberAdminViewDtoList = new ArrayList<>();
        List<Member> findMemberList = memberService.getMemberList(memberSearchDto);
        for (Member findMember : findMemberList) {
            MemberAdminViewDto memberAdminViewDto = MemberAdminViewDto.memberToAdminView(findMember);
            memberAdminViewDtoList.add(memberAdminViewDto);
        }
        return memberAdminViewDtoList;
    }

}
