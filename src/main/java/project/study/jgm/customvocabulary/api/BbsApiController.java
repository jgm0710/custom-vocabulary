package project.study.jgm.customvocabulary.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import project.study.jgm.customvocabulary.bbs.Bbs;
import project.study.jgm.customvocabulary.bbs.BbsService;
import project.study.jgm.customvocabulary.bbs.dto.BbsCreateDto;
import project.study.jgm.customvocabulary.bbs.dto.BbsSearchDto;
import project.study.jgm.customvocabulary.bbs.like.BbsLike;
import project.study.jgm.customvocabulary.common.dto.MessageDto;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberService;
import project.study.jgm.customvocabulary.security.CurrentUser;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/bbs")
public class BbsApiController {

    private final MemberService memberService;

    private final BbsService bbsService;

    private final BbsLike bbsLike;


    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity addBbs(
            @RequestBody @Valid BbsCreateDto bbsCreateDto,
            @CurrentUser Member member,
            Errors errors
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


        return null;
    }
}
