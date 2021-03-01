package project.study.jgm.customvocabulary.api;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import project.study.jgm.customvocabulary.bbs.BbsService;
import project.study.jgm.customvocabulary.bbs.exception.BbsNotFoundException;
import project.study.jgm.customvocabulary.bbs.exception.DeletedBbsException;
import project.study.jgm.customvocabulary.bbs.reply.Reply;
import project.study.jgm.customvocabulary.bbs.reply.ReplyService;
import project.study.jgm.customvocabulary.bbs.reply.ReplySortType;
import project.study.jgm.customvocabulary.bbs.reply.dto.ReplyChildResponseDto;
import project.study.jgm.customvocabulary.bbs.reply.dto.ReplyCreateDto;
import project.study.jgm.customvocabulary.bbs.reply.dto.ReplyParentResponseDto;
import project.study.jgm.customvocabulary.bbs.reply.dto.ReplyUpdateDto;
import project.study.jgm.customvocabulary.bbs.reply.exception.DeletedReplyException;
import project.study.jgm.customvocabulary.bbs.reply.exception.ReplyNotFoundException;
import project.study.jgm.customvocabulary.bbs.reply.like.ReplyLikeService;
import project.study.jgm.customvocabulary.bbs.reply.like.exception.AddLikeToChildReplyException;
import project.study.jgm.customvocabulary.common.dto.CriteriaDto;
import project.study.jgm.customvocabulary.common.dto.ResponseDto;
import project.study.jgm.customvocabulary.common.exception.ExistLikeException;
import project.study.jgm.customvocabulary.common.exception.NoExistLikeException;
import project.study.jgm.customvocabulary.common.exception.SelfLikeException;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRole;
import project.study.jgm.customvocabulary.members.exception.MemberNotFoundException;
import project.study.jgm.customvocabulary.security.CurrentUser;

import javax.validation.Valid;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static project.study.jgm.customvocabulary.common.dto.MessageVo.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/bbs/reply")
public class ReplyApiController {

    private final BbsService bbsService;

    private final ReplyService replyService;

    private final ReplyLikeService replyLikeService;

    private final ModelMapper modelMapper;

    @PostMapping("/{bbsId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> addReply(
            @PathVariable("bbsId") Long bbsId,
            @RequestBody @Valid ReplyCreateDto replyCreateDto,
            Errors errors,
            @CurrentUser Member member
    ) {

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            Reply savedReply = replyService.addReply(member.getId(), bbsId, replyCreateDto.getContent());
            ReplyParentResponseDto replyParentResponseDto = ReplyParentResponseDto.replyToParentResponseDto(savedReply, modelMapper);
            URI uri = linkTo(ReplyApiController.class).toUri();

            return ResponseEntity.created(uri).body(new ResponseDto<>(replyParentResponseDto, REPLY_REGISTER_SUCCESSFULLY));

        } catch (BbsNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        } catch (DeletedBbsException | MemberNotFoundException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }
    }

    @PostMapping("/reply/{parentId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> addReplyOfReply(
            @PathVariable("parentId") Long parentId,
            @RequestBody @Valid ReplyCreateDto replyCreateDto,
            Errors errors,
            @CurrentUser Member member
    ) {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            Reply savedReply = replyService.addReplyOfReply(member.getId(), parentId, replyCreateDto.getContent());
            ReplyChildResponseDto replyChildResponseDto = ReplyChildResponseDto.replyToChildResponseDto(savedReply, modelMapper);
            URI uri = linkTo(ReplyApiController.class).toUri();

            return ResponseEntity.created(uri).body(new ResponseDto<>(replyChildResponseDto, REPLY_REGISTER_SUCCESSFULLY));

        } catch (ReplyNotFoundException | MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        }
    }

    @GetMapping("/{bbsId}")
    public ResponseEntity<? extends ResponseDto<?>> getReplyParentList(
            @PathVariable("bbsId") Long bbsId,
            @ModelAttribute CriteriaDto criteriaDto,
            @RequestParam ReplySortType sortType,
            @CurrentUser Member member
    ) {

        try {
            List<Reply> replyParentList = replyService.getReplyParentList(bbsId, criteriaDto, sortType);
            List<ReplyParentResponseDto> replyParentResponseDtoList = ReplyParentResponseDto.replyListToParentListResponse(replyParentList, member, replyLikeService);

            return ResponseEntity.ok(new ResponseDto<>(replyParentResponseDtoList, GET_PARENT_REPLY_LIST_SUCCESSFULLY));
        } catch (BbsNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        } catch (DeletedBbsException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }
    }

    @GetMapping("/reply/{parentId}")
    public ResponseEntity<? extends ResponseDto<?>> getReplyChildList(
            @PathVariable("parentId") Long parentId,
            @ModelAttribute CriteriaDto criteriaDto,
            @CurrentUser Member member
    ) {

        try {
            List<Reply> replyChildList = replyService.getReplyChildList(parentId, criteriaDto);
            List<ReplyChildResponseDto> replyChildResponseDtoList = ReplyChildResponseDto.replyListToChildList(replyChildList, member);

            return ResponseEntity.ok(new ResponseDto<>(replyChildResponseDtoList, GET_CHILD_REPLY_LIST_SUCCESSFULLY));
        } catch (ReplyNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        } catch (DeletedReplyException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }
    }

    @PutMapping("/{replyId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> modifyReply(
            @PathVariable("replyId") Long replyId,
            @RequestBody @Valid ReplyUpdateDto replyUpdateDto,
            Errors errors,
            @CurrentUser Member member
    ) {

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        Reply findReply;

        try {
            findReply = replyService.getReply(replyId);
            if (!findReply.getMember().getId().equals(member.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>(MODIFY_REPLY_OF_DIFFERENT_MEMBER));
            }
        } catch (ReplyNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        }

        try {
            replyService.modifyReply(replyId, replyUpdateDto.getContent());
        } catch (DeletedReplyException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }

        if (findReply.getParent() != null) {
            ReplyChildResponseDto replyChildResponseDto = ReplyChildResponseDto.replyToChildResponseDto(findReply, modelMapper);
            return ResponseEntity.ok(new ResponseDto<>(replyChildResponseDto, MODIFY_REPLY_SUCCESSFULLY));
        } else {
            ReplyParentResponseDto replyParentResponseDto = ReplyParentResponseDto.replyToParentResponseDto(findReply, modelMapper);
            return ResponseEntity.ok(new ResponseDto<>(replyParentResponseDto, MODIFY_REPLY_SUCCESSFULLY));
        }

    }

    @DeleteMapping("/{replyId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ResponseDto<Object>> deleteReply(
            @PathVariable("replyId") Long replyId,
            @CurrentUser Member member
    ) {

        try {
            Reply findReply = replyService.getReply(replyId);
            if (!member.getRoles().contains(MemberRole.ADMIN)) {
                if (!findReply.getMember().getId().equals(member.getId())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>(DELETE_REPLY_OF_DIFFERENT_MEMBER));
                }
            }
        } catch (ReplyNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        }

        try {
            replyService.deleteReply(replyId);
        } catch (DeletedReplyException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }

        return ResponseEntity.ok(new ResponseDto<>(DELETE_REPLY_SUCCESSFULLY));
    }

    @PostMapping("/like/{replyId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ResponseDto<Object>> addLikeToReply(
            @PathVariable("replyId") Long replyId,
            @CurrentUser Member member
    ) {

        try {
            replyLikeService.like(member.getId(), replyId);
        } catch (ReplyNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        } catch (AddLikeToChildReplyException | ExistLikeException | DeletedReplyException | SelfLikeException | MemberNotFoundException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }

        return ResponseEntity.ok(new ResponseDto<>(ADD_LIKE_TO_REPLY_SUCCESSFULLY));
    }

    @DeleteMapping("/like/{replyId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ResponseDto<Object>> unLikeReply(
            @PathVariable("replyId") Long replyId,
            @CurrentUser Member member
    ) {

        try {
            replyLikeService.unLike(member.getId(), replyId);
        } catch (NoExistLikeException e) {
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        }

        return ResponseEntity.ok(new ResponseDto<>(UNLIKE_REPLY_SUCCESSFULLY));
    }
}

























