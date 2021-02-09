package project.study.jgm.customvocabulary.api;

import com.querydsl.core.QueryResults;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
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
import project.study.jgm.customvocabulary.bbs.like.BbsLike;
import project.study.jgm.customvocabulary.bbs.like.BbsLikeService;
import project.study.jgm.customvocabulary.common.dto.ListResponseDto;
import project.study.jgm.customvocabulary.common.dto.MessageDto;
import project.study.jgm.customvocabulary.common.dto.PaginationDto;
import project.study.jgm.customvocabulary.common.exception.ExistLikeException;
import project.study.jgm.customvocabulary.common.exception.NoExistLikeException;
import project.study.jgm.customvocabulary.common.exception.SelfLikeException;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRole;
import project.study.jgm.customvocabulary.members.MemberService;
import project.study.jgm.customvocabulary.security.CurrentUser;

import javax.imageio.plugins.jpeg.JPEGImageReadParam;
import javax.persistence.EntityManager;
import javax.validation.Valid;
import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

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
    public ResponseEntity addBbs(
            @RequestBody @Valid BbsCreateDto bbsCreateDto,
            Errors errors,
            @CurrentUser Member member
    ) {

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        Bbs bbs = bbsService.addBbs(member.getId(), bbsCreateDto);
        URI getBbs = linkTo(BbsApiController.class).slash(bbs.getId()).toUri();
        return ResponseEntity.created(getBbs).body(new MessageDto(MessageDto.BBS_REGISTERED_SUCCESSFULLY));
    }

    @GetMapping
    public ResponseEntity getBbsList(
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
                        .data(bbsSimpleAdminViewDtoList)
                        .paging(paginationDto)
                        .build();

                return ResponseEntity.ok(listResponseDto);
            }
        }

        List<BbsSimpleDto> bbsSimpleDtoList = BbsSimpleDto.bbsListToSimpleList(findBbsList);
        ListResponseDto<Object> listResponseDto = ListResponseDto.builder()
                .data(bbsSimpleDtoList)
                .paging(paginationDto)
                .build();

        return ResponseEntity.ok(listResponseDto);
    }

    @GetMapping("/{bbsId}")
    public ResponseEntity getBbs(
            @PathVariable("bbsId") Long bbsId,
            @CurrentUser Member member
    ) {

        try {
            Bbs findBbs = bbsService.getBbs(bbsId);
            BbsDetailDto bbsDetailDto = BbsDetailDto.bbsToDetail(findBbs);

            if (member != null) {
                if (member.getRoles().contains(MemberRole.ADMIN)) {
                    BbsDetailAdminViewDto bbsDetailAdminViewDto = BbsDetailAdminViewDto.bbsToDetailAdminView(findBbs);
                    EntityModel<BbsDetailAdminViewDto> entityModel = EntityModel.of(bbsDetailAdminViewDto);
                    entityModel.add(linkTo(BbsApiController.class).slash(bbsId).withRel("update-bbs"));

                    return ResponseEntity.ok(entityModel);
                } else {
                    if (findBbs.getStatus() == BbsStatus.DELETE) {
                        return ResponseEntity.badRequest().body(new MessageDto(MessageDto.UNAUTHORIZED_USERS_VIEW_DELETED_POSTS));
                    }
                    if (findBbs.getMember().getId().equals(member.getId())) {
                        bbsDetailDto.setViewLike(false);
                        EntityModel<BbsDetailDto> entityModel = EntityModel.of(bbsDetailDto);
                        entityModel.add(linkTo(BbsApiController.class).slash(bbsId).withRel("update-bbs"));

                        return ResponseEntity.ok(entityModel);
                    } else {
                        boolean existLike = bbsLikeService.getExistLike(member.getId(), bbsId);
                        bbsDetailDto.setLike(existLike);
                        bbsDetailDto.setViewLike(true);
                    }
                }
            }

            return ResponseEntity.ok(bbsDetailDto);

        } catch (BbsNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageDto(e.getMessage()));
        }
    }

    @PutMapping("/{bbsId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity modifyBbs(
            @PathVariable("bbsId") Long bbsId,
            @RequestBody @Valid BbsUpdateDto bbsUpdateDto,
            Errors errors,
            @CurrentUser Member member
    ) {

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            Bbs findBbs = bbsService.getBbs(bbsId);
            if (!findBbs.getMember().getId().equals(member.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageDto(MessageDto.MODIFY_BBS_OF_DIFFERENT_MEMBER));
            }
        } catch (BbsNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageDto(e.getMessage()));
        }


        try {
            bbsService.modifyBbs(bbsId, bbsUpdateDto);
        } catch (DeletedBbsException e) {
            return ResponseEntity.badRequest().body(new MessageDto(e.getMessage()));
        }

        EntityModel<MessageDto> entityModel = EntityModel.of(new MessageDto(MessageDto.MODIFIED_BBS_SUCCESSFULLY));
        entityModel.add(linkTo(BbsApiController.class).slash(bbsId).withRel("get-bbs"));

        return ResponseEntity.ok(entityModel);
    }

    @DeleteMapping("/{bbsId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity deleteBbs(
            @PathVariable("bbsId") Long bbsId,
            @CurrentUser Member member
    ) {

        try {
            Bbs findBbs = bbsService.getBbs(bbsId);

            if (!member.getRoles().contains(MemberRole.ADMIN)) {
                if (!findBbs.getMember().getId().equals(member.getId())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageDto(MessageDto.DELETE_BBS_OF_DIFFERENT_MEMBER));
                }
            }

        }catch (BbsNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageDto(e.getMessage()));
        }

        try {
            bbsService.deleteBbs(bbsId);
        } catch (DeletedBbsException e) {
            return ResponseEntity.badRequest().body(new MessageDto(e.getMessage()));
        }

        return ResponseEntity.ok(new MessageDto(MessageDto.DELETE_BBS_SUCCESSFULLY));
    }

    @GetMapping("/like/{bbsId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity addLikeToBbs(
            @PathVariable("bbsId") Long bbsId,
            @CurrentUser Member member
    ) {

        try {
            bbsLikeService.like(member.getId(), bbsId);
        } catch (ExistLikeException e) {
            return ResponseEntity.badRequest().body(new MessageDto(e.getMessage()));
        } catch (BbsNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageDto(e.getMessage()));
        } catch (SelfLikeException e) {
            return ResponseEntity.badRequest().body(new MessageDto(e.getMessage()));
        } catch (DeletedBbsException e) {
            return ResponseEntity.badRequest().body(new MessageDto(e.getMessage()));
        }

        return ResponseEntity.ok(new MessageDto(MessageDto.ADD_LIKE_TO_BBS_SUCCESSFULLY));
    }

    @GetMapping("/unlike/{bbsId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity unLikeBbs(
            @PathVariable("bbsId") Long bbsId,
            @CurrentUser Member member
    ) {

        try {
            bbsLikeService.unLike(member.getId(), bbsId);
        } catch (NoExistLikeException e) {
            return ResponseEntity.badRequest().body(new MessageDto(e.getMessage()));
        }

        return ResponseEntity.ok(new MessageDto(MessageDto.UNLIKE_BBS_SUCCESSFULLY));
    }
}
