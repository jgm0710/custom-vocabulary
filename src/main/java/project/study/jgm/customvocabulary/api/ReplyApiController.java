package project.study.jgm.customvocabulary.api;

import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
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
import project.study.jgm.customvocabulary.common.dto.CriteriaDto;
import project.study.jgm.customvocabulary.common.dto.MessageDto;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRole;
import project.study.jgm.customvocabulary.members.exception.MemberNotFoundException;
import project.study.jgm.customvocabulary.security.CurrentUser;

import javax.validation.Valid;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/bbs/reply")
public class ReplyApiController {

    private final BbsService bbsService;

    private final ReplyService replyService;

    private final ReplyLikeService replyLikeService;

    @PostMapping("/{bbsId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity addReply(
            @PathVariable("bbsId") Long bbsId,
            @RequestBody @Valid ReplyCreateDto replyCreateDto,
            Errors errors,
            @CurrentUser Member member
    ) {

        System.out.println("여기 걸리나?");
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            replyService.addReply(member.getId(), bbsId, replyCreateDto.getContent());
            URI uri = linkTo(ReplyApiController.class).toUri();

            return ResponseEntity.created(uri).body(new MessageDto(MessageDto.REPLY_REGISTER_SUCCESSFULLY));

        } catch (BbsNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageDto(e.getMessage()));
        } catch (DeletedBbsException e) {
            return ResponseEntity.badRequest().body(new MessageDto(e.getMessage()));
        } catch (MemberNotFoundException e) {
            return ResponseEntity.badRequest().body(new MessageDto(e.getMessage()));
        }
    }

    @PostMapping("/reply/{parentId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity addReplyOfReply(
            @PathVariable("parentId") Long parentId,
            @RequestBody @Valid ReplyCreateDto replyCreateDto,
            Errors errors,
            @CurrentUser Member member
    ) {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            replyService.addReplyOfReply(member.getId(), parentId, replyCreateDto.getContent());
            URI uri = linkTo(ReplyApiController.class).toUri();

            return ResponseEntity.created(uri).body(new MessageDto(MessageDto.REPLY_REGISTER_SUCCESSFULLY));

        } catch (ReplyNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageDto(e.getMessage()));
        } catch (MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageDto(e.getMessage()));
        }
    }

    @GetMapping("/{bbsId}")
    public ResponseEntity getReplyParentList(
            @PathVariable("bbsId") Long bbsId,
            @ModelAttribute CriteriaDto criteriaDto,
            @RequestParam ReplySortType sortType,
            @CurrentUser Member member
    ) {

        try {
            List<Reply> replyParentList = replyService.getReplyParentList(bbsId, criteriaDto, sortType);
            List<ReplyParentResponseDto> replyParentResponseDtoList = ReplyParentResponseDto.replyListToParentListResponse(replyParentList, member, replyLikeService);

            return ResponseEntity.ok(replyParentResponseDtoList);
        } catch (BbsNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageDto(e.getMessage()));
        } catch (DeletedBbsException e) {
            return ResponseEntity.badRequest().body(new MessageDto(e.getMessage()));
        }
    }

    @GetMapping("/reply/{parentId}")
    public ResponseEntity getReplyChildList(
            @PathVariable("parentId") Long parentId,
            @ModelAttribute CriteriaDto criteriaDto,
            @CurrentUser Member member
    ) {

        try {
            List<Reply> replyChildList = replyService.getReplyChildList(parentId, criteriaDto);
            List<ReplyChildResponseDto> replyChildResponseDtoList = ReplyChildResponseDto.replyListToChildList(replyChildList, member);

            return ResponseEntity.ok(replyChildResponseDtoList);
        } catch (ReplyNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageDto(e.getMessage()));
        } catch (DeletedReplyException e) {
            return ResponseEntity.badRequest().body(new MessageDto(e.getMessage()));
        }
    }

    @PutMapping("/{replyId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity modifyReply(
            @PathVariable("replyId") Long replyId,
            @RequestBody @Valid ReplyUpdateDto replyUpdateDto,
            Errors errors,
            @CurrentUser Member member
    ) {

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            Reply findReply = replyService.getReply(replyId);
            if (!findReply.getMember().getId().equals(member.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageDto(MessageDto.MODIFY_REPLY_OF_DIFFERENT_MEMBER));
            }
        } catch (ReplyNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageDto(e.getMessage()));
        }

        try {
            replyService.modifyReply(replyId, replyUpdateDto.getContent());
        } catch (DeletedReplyException e) {
            return ResponseEntity.badRequest().body(new MessageDto(e.getMessage()));
        }

        return ResponseEntity.ok(new MessageDto(MessageDto.MODIFY_REPLY_SUCCESSFULLY));
    }

    @DeleteMapping("/{replyId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity deleteReply(
            @PathVariable("replyId") Long replyId,
            @CurrentUser Member member
    ) {

        try {
            Reply findReply = replyService.getReply(replyId);
            if (!member.getRoles().contains(MemberRole.ADMIN)) {
                if (!findReply.getMember().getId().equals(member.getId())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageDto(MessageDto.DELETE_REPLY_OF_DIFFERENT_MEMBER));
                }
            }
        } catch (ReplyNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageDto(e.getMessage()));
        }

        try {
            replyService.deleteReply(replyId);

            return ResponseEntity.ok(new MessageDto(MessageDto.DELETE_REPLY_SUCCESSFULLY));
        } catch (DeletedReplyException e) {
            return ResponseEntity.badRequest().body(new MessageDto(e.getMessage()));
        }
    }
}

























