package project.study.jgm.customvocabulary.api;

import com.querydsl.core.QueryResults;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import project.study.jgm.customvocabulary.bbs.Bbs;
import project.study.jgm.customvocabulary.bbs.BbsSearchValidator;
import project.study.jgm.customvocabulary.bbs.BbsService;
import project.study.jgm.customvocabulary.bbs.BbsStatus;
import project.study.jgm.customvocabulary.bbs.dto.*;
import project.study.jgm.customvocabulary.bbs.dto.admin.BbsDetailAdminViewDto;
import project.study.jgm.customvocabulary.bbs.dto.admin.BbsSimpleAdminViewDto;
import project.study.jgm.customvocabulary.bbs.exception.BbsNotFoundException;
import project.study.jgm.customvocabulary.bbs.exception.DeletedBbsException;
import project.study.jgm.customvocabulary.bbs.like.BbsLikeService;
import project.study.jgm.customvocabulary.common.dto.ListResponseDto;
import project.study.jgm.customvocabulary.common.dto.PaginationDto;
import project.study.jgm.customvocabulary.common.dto.ResponseDto;
import project.study.jgm.customvocabulary.common.exception.ExistLikeException;
import project.study.jgm.customvocabulary.common.exception.NoExistLikeException;
import project.study.jgm.customvocabulary.common.exception.SelfLikeException;
import project.study.jgm.customvocabulary.bbs.upload.BbsUploadFile;
import project.study.jgm.customvocabulary.common.upload.UploadFileResponseDto;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRole;
import project.study.jgm.customvocabulary.members.MemberService;
import project.study.jgm.customvocabulary.security.CurrentUser;

import javax.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static project.study.jgm.customvocabulary.common.dto.MessageVo.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/bbs")
public class BbsApiController {

    private final MemberService memberService;

    private final BbsService bbsService;

    private final BbsSearchValidator bbsSearchValidator;

    private final ModelMapper modelMapper;

    private final BbsLikeService bbsLikeService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> addBbs(
            @RequestBody @Valid BbsCreateDto bbsCreateDto,
            Errors errors,
            @CurrentUser Member member
    ) {

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        Bbs savedBbs = bbsService.addBbs(member.getId(), bbsCreateDto);
        URI getBbsUri = linkTo(BbsApiController.class).slash(savedBbs.getId()).toUri();

        BbsDetailDto bbsDetailDto = BbsDetailDto.bbsToDetail(savedBbs, modelMapper);
        bbsDetailDto.setPermissionToDeleteAndModify(true);

        return ResponseEntity.created(getBbsUri).body(new ResponseDto<>(bbsDetailDto, BBS_REGISTERED_SUCCESSFULLY));
    }


    @GetMapping
    public ResponseEntity<?> getBbsList(
            @ModelAttribute @Valid BbsSearchDto bbsSearchDto,
            BindingResult bindingResult,
            @CurrentUser Member member
    ) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult);
        }

        bbsSearchValidator.validate(bbsSearchDto, member, bindingResult);
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult);
        }

        QueryResults<Bbs> results = bbsService.getBbsList(bbsSearchDto);
        long total = results.getTotal();
        List<Bbs> findBbsList = results.getResults();

        PaginationDto paginationDto = new PaginationDto(total, bbsSearchDto.getCriteriaDto());

        if (member != null) {
            if (member.getRoles().contains(MemberRole.ADMIN)) {
                List<BbsSimpleAdminViewDto> bbsSimpleAdminViewDtoList = BbsSimpleAdminViewDto.bbsListToSimpleAdminViewList(findBbsList);
                ListResponseDto<Object> listResponseDto = ListResponseDto.builder()
                        .list(bbsSimpleAdminViewDtoList)
                        .paging(paginationDto)
                        .build();

                return ResponseEntity.ok(new ResponseDto<>(listResponseDto, GET_BBS_LIST_BY_ADMIN_SUCCESSFULLY));
            }
        }

        List<BbsSimpleDto> bbsSimpleDtoList = BbsSimpleDto.bbsListToSimpleList(findBbsList);
        ListResponseDto<Object> listResponseDto = ListResponseDto.builder()
                .list(bbsSimpleDtoList)
                .paging(paginationDto)
                .build();

        return ResponseEntity.ok(new ResponseDto<>(listResponseDto, GET_BBS_LIST_SUCCESSFULLY));
    }

    @GetMapping("/{bbsId}")
    public ResponseEntity<? extends ResponseDto<?>> getBbs(
            @PathVariable("bbsId") Long bbsId,
            @CurrentUser Member member
    ) {

        try {
            Bbs findBbs = bbsService.getBbs(bbsId);
            BbsDetailDto bbsDetailDto = BbsDetailDto.bbsToDetail(findBbs,modelMapper);

            if (member != null) {
                if (member.getRoles().contains(MemberRole.ADMIN)) {
                    BbsDetailAdminViewDto bbsDetailAdminViewDto = BbsDetailAdminViewDto.bbsToDetailAdminView(findBbs, modelMapper);

                    return ResponseEntity.ok(new ResponseDto<>(bbsDetailAdminViewDto, GET_BBS_BY_ADMIN_SUCCESSFULLY));
                } else {
                    if (findBbs.getStatus() == BbsStatus.DELETE) {
                        return ResponseEntity.badRequest().body(new ResponseDto<>(UNAUTHORIZED_USERS_VIEW_DELETED_POSTS));
                    }

                    if (findBbs.getMember().getId().equals(member.getId())) {
                        bbsDetailDto.setViewLike(false);
                        bbsDetailDto.setPermissionToDeleteAndModify(true);
                    } else {
                        boolean existLike = bbsLikeService.getExistLike(member.getId(), bbsId);
                        bbsDetailDto.setLike(existLike);
                    }
                }
            }

            return ResponseEntity.ok(new ResponseDto<>(bbsDetailDto, GET_BBS_SUCCESSFULLY));

        } catch (BbsNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        }
    }

    @PutMapping("/{bbsId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> modifyBbs(
            @PathVariable("bbsId") Long bbsId,
            @RequestBody @Valid BbsUpdateDto bbsUpdateDto,
            Errors errors,
            @CurrentUser Member member
    ) {

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        Bbs findBbs;

        try {
            findBbs = bbsService.getBbs(bbsId);
            if (!findBbs.getMember().getId().equals(member.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>(MODIFY_BBS_OF_DIFFERENT_MEMBER));
            }
        } catch (BbsNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        }


        try {
            bbsService.modifyBbs(bbsId, bbsUpdateDto);
        } catch (DeletedBbsException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }

        if (member.getRoles().contains(MemberRole.ADMIN)) {
            BbsDetailAdminViewDto bbsDetailAdminViewDto = BbsDetailAdminViewDto.bbsToDetailAdminView(findBbs, modelMapper);
            return ResponseEntity.ok(new ResponseDto<>(bbsDetailAdminViewDto, MODIFY_BBS_BY_ADMIN_SUCCESSFULLY));
        } else {
            BbsDetailDto bbsDetailDto = BbsDetailDto.bbsToDetail(findBbs,modelMapper);
            bbsDetailDto.setViewLike(false);
            bbsDetailDto.setPermissionToDeleteAndModify(true);
            return ResponseEntity.ok(new ResponseDto<>(bbsDetailDto, MODIFIED_BBS_SUCCESSFULLY));
        }
    }

    @DeleteMapping("/{bbsId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ResponseDto<Object>> deleteBbs(
            @PathVariable("bbsId") Long bbsId,
            @CurrentUser Member member
    ) {

        try {
            Bbs findBbs = bbsService.getBbs(bbsId);

            if (!member.getRoles().contains(MemberRole.ADMIN)) {
                if (!findBbs.getMember().getId().equals(member.getId())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>(DELETE_BBS_OF_DIFFERENT_MEMBER));
                }
            }

        }catch (BbsNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        }

        try {
            bbsService.deleteBbs(bbsId);
        } catch (DeletedBbsException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }

        return ResponseEntity.ok(new ResponseDto<>(DELETE_BBS_SUCCESSFULLY));
    }

    @PostMapping("/like/{bbsId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ResponseDto<Object>> addLikeToBbs(
            @PathVariable("bbsId") Long bbsId,
            @CurrentUser Member member
    ) {

        try {
            bbsLikeService.like(member.getId(), bbsId);
        } catch (ExistLikeException | SelfLikeException | DeletedBbsException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        } catch (BbsNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        }

        return ResponseEntity.ok(new ResponseDto<>(ADD_LIKE_TO_BBS_SUCCESSFULLY));
    }

    @DeleteMapping("/like/{bbsId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ResponseDto<Object>> unLikeBbs(
            @PathVariable("bbsId") Long bbsId,
            @CurrentUser Member member
    ) {

        try {
            bbsLikeService.unLike(member.getId(), bbsId);
        } catch (NoExistLikeException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }

        return ResponseEntity.ok(new ResponseDto<>(UNLIKE_BBS_SUCCESSFULLY));
    }
}
