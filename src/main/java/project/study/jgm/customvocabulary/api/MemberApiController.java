package project.study.jgm.customvocabulary.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import project.study.jgm.customvocabulary.common.dto.ListResponseDto;
import project.study.jgm.customvocabulary.common.dto.PaginationDto;
import project.study.jgm.customvocabulary.common.dto.ResponseDto;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRole;
import project.study.jgm.customvocabulary.members.MemberService;
import project.study.jgm.customvocabulary.members.dto.*;
import project.study.jgm.customvocabulary.members.dto.search.MemberSearchDto;
import project.study.jgm.customvocabulary.members.dto.search.MemberSearchValidator;
import project.study.jgm.customvocabulary.members.exception.ExistDuplicatedMemberException;
import project.study.jgm.customvocabulary.members.exception.MemberAlreadyHasAuthorityException;
import project.study.jgm.customvocabulary.members.exception.MemberNotFoundException;
import project.study.jgm.customvocabulary.security.CurrentUser;
import project.study.jgm.customvocabulary.security.exception.PasswordMismatchException;

import javax.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static project.study.jgm.customvocabulary.common.dto.MessageVo.*;

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

        try {
            Member member = memberService.userJoin(memberCreateDto);
            MemberDetailDto memberDetailDto = MemberDetailDto.memberToDetail(member);

            URI loginUri = linkTo(LoginApiController.class).slash("login").withRel("login").toUri();

            return ResponseEntity.created(loginUri)
                    .body(new ResponseDto<>(memberDetailDto, MEMBER_JOIN_SUCCESSFULLY));
        } catch (ExistDuplicatedMemberException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }
    }

    @GetMapping("/{memberId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity getMember(@PathVariable("memberId") Long memberId, @CurrentUser Member member) {

        try {
            Member findMember = memberService.getMember(memberId);
            if (member.getRoles().contains(MemberRole.ADMIN)) {
                MemberAdminViewDto memberAdminViewDto = MemberAdminViewDto.memberToAdminView(findMember);

                return ResponseEntity
                        .ok(new ResponseDto<>(memberAdminViewDto, GET_MEMBER_BY_ADMIN_SUCCESSFULLY));
            } else {
                if (!findMember.getId().equals(member.getId())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new ResponseDto<>(GET_DIFFERENT_MEMBER_INFO));
                }

                MemberDetailDto memberDetailDto = MemberDetailDto.memberToDetail(findMember);

                return ResponseEntity
                        .ok(new ResponseDto<>(memberDetailDto, GET_MEMBER_SUCCESSFULLY));
            }

        } catch (MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDto<>(e.getMessage()));
        }
    }

    @PutMapping("/{memberId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity modifyMember(
            @PathVariable("memberId") Long memberId,
            @RequestParam String password,
            @RequestBody @Valid MemberUpdateDto memberUpdateDto,
            Errors errors,
            @CurrentUser Member member
    ) {

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }


        if (!memberId.equals(member.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDto<>(MODIFY_DIFFERENT_MEMBER_INFO));
        }

        try {
            memberService.modifyMember(memberId, password, memberUpdateDto);
        } catch (MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDto<>(e.getMessage()));
        } catch (PasswordMismatchException | ExistDuplicatedMemberException e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto<>(e.getMessage()));
        }

        Member findMember = memberService.getMember(memberId);
        MemberDetailDto memberDetailDto = MemberDetailDto.memberToDetail(findMember);

        return ResponseEntity
                .ok(new ResponseDto<>(memberDetailDto, MODIFIED_MEMBER_INFO_SUCCESSFULLY));

    }

    @PutMapping("/password/{memberId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity updatePassword(@PathVariable("memberId") Long memberId,
                                         @RequestBody @Valid PasswordUpdateDto passwordUpdateDto,
                                         Errors errors,
                                         @CurrentUser Member member
    ) {

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        if (!memberId.equals(member.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDto<>(MODIFY_DIFFERENT_MEMBER_INFO));
        }

        try {
            memberService.updatePassword(memberId, passwordUpdateDto.getOldPassword(), passwordUpdateDto.getNewPassword());
        } catch (MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDto<>(e.getMessage()));
        } catch (PasswordMismatchException e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto<>(e.getMessage()));
        }

        return ResponseEntity
                .ok(new ResponseDto<>(CHANGED_PASSWORD_SUCCESSFULLY));
    }

    @PutMapping("/secession/{memberId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity secession(@PathVariable("memberId") Long memberId,
                                    @RequestParam String password,
                                    @CurrentUser Member member) {

        if (!memberId.equals(member.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDto<>(MODIFY_DIFFERENT_MEMBER_INFO));
        }

        try {
            memberService.secession(memberId, password);

        } catch (MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDto<>(e.getMessage()));
        } catch (PasswordMismatchException e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto<>(e.getMessage()));
        }

        return ResponseEntity.ok(new ResponseDto<>(SECESSION_SUCCESSFULLY));
    }

    /**
     * ADMIN
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity getMemberList(
            @ModelAttribute @Valid MemberSearchDto memberSearchDto,
            BindingResult bindingResult
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

        return ResponseEntity.ok(new ResponseDto<>(listResponseDto, GET_MEMBER_LIST_BY_ADMIN_SUCCESSFULLY));
    }

    @PutMapping("/ban/{memberId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity ban(@PathVariable("memberId") Long memberId) {

        try {
            memberService.ban(memberId);
        } catch (MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDto<>(e.getMessage()));
        }

        Member findMember = memberService.getMember(memberId);
        MemberAdminViewDto memberAdminViewDto = MemberAdminViewDto.memberToAdminView(findMember);

        return ResponseEntity.ok(new ResponseDto<>(memberAdminViewDto, BAN_SUCCESSFULLY));
    }

    @PutMapping("/changeToUser/{memberId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity changeMemberRoleToUser(
            @PathVariable("memberId") Long memberId
    ) {

        try {
            memberService.changeMemberRoleToUser(memberId);
        } catch (MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDto<>(e.getMessage()));
        } catch (MemberAlreadyHasAuthorityException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }

        Member findMember = memberService.getMember(memberId);
        MemberAdminViewDto memberAdminViewDto = MemberAdminViewDto.memberToAdminView(findMember);

        return ResponseEntity.ok(new ResponseDto<>(memberAdminViewDto, CHANGE_MEMBER_ROLE_TO_USER_SUCCESSFULLY));
    }

    /**
     * PRIVATE
     */

//    private void addLinkInfoOfPrevAndNextLinkToListResponse(MemberSearchDto memberSearchDto, PaginationDto paginationDto, EntityModel<ListResponseDto> listResponse) {
//        MemberSearchDto tempSearchDto = MemberSearchDto.builder()
//                .searchType(memberSearchDto.getSearchType())
//                .keyword(memberSearchDto.getKeyword())
//                .criteriaDto(new CriteriaDto(memberSearchDto.getCriteriaDto().getPageNum(), memberSearchDto.getCriteriaDto().getLimit()))
//                .sortType(memberSearchDto.getSortType())
//                .build();
//
//        if (paginationDto.isPrev()) {
//            tempSearchDto.updatePage(paginationDto.getLastPageOfPrevList());
//            listResponse.add(linkToGetMemberList(tempSearchDto, "prev-list"));
//        }
//        if (paginationDto.isNext()) {
//            tempSearchDto.updatePage(paginationDto.getFirstPageOfNextList());
//            listResponse.add(linkToGetMemberList(tempSearchDto, "next-list"));
//        }
//    }
    private ListResponseDto<Object> getListResponseDto(List<MemberAdminViewDto> memberAdminViewDtoList, PaginationDto paginationDto) {
        ListResponseDto<Object> listResponseDto = ListResponseDto.builder()
                .list(memberAdminViewDtoList)
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
