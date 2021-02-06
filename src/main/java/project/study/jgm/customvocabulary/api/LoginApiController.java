package project.study.jgm.customvocabulary.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import project.study.jgm.customvocabulary.common.EntityModelCreator;
import project.study.jgm.customvocabulary.common.dto.MessageDto;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberService;
import project.study.jgm.customvocabulary.members.exception.RefreshTokenExpirationException;
import project.study.jgm.customvocabulary.members.exception.RefreshTokenNotFoundException;
import project.study.jgm.customvocabulary.security.CurrentUser;
import project.study.jgm.customvocabulary.security.dto.LoginDto;
import project.study.jgm.customvocabulary.security.dto.OnlyTokenDto;
import project.study.jgm.customvocabulary.security.dto.TokenDto;
import project.study.jgm.customvocabulary.security.exception.PasswordMismatchException;

import javax.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api")
public class LoginApiController {

    private final MemberService memberService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid LoginDto loginDto, Errors errors) {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            TokenDto tokenDto = memberService.login(loginDto);

            var tokenResponse = EntityModelCreator.createTokenResponse(tokenDto, LoginApiController.class, "login");
            tokenResponse.add(linkTo(IndexApiController.class).withRel("index"));

            return ResponseEntity.ok(tokenResponse);

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body(new MessageDto(e.getMessage()));
        } catch (PasswordMismatchException e) {
            return ResponseEntity.badRequest().body(new MessageDto(e.getMessage()));
        }

    }

    @PostMapping("/refresh")
    public ResponseEntity refresh(@RequestBody @Valid OnlyTokenDto onlyTokenDto, Errors errors) {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            TokenDto tokenDto = memberService.refresh(onlyTokenDto);

            var tokenResponse = EntityModelCreator.createTokenResponse(tokenDto, LoginApiController.class, "refresh");
            tokenResponse.add(linkTo(IndexApiController.class).withRel("index"));

            return ResponseEntity.ok(tokenResponse);
        } catch (RefreshTokenNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageDto(e.getMessage()));
        } catch (RefreshTokenExpirationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageDto(e.getMessage()));
        }
    }

    @GetMapping("/logout")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity logout(@CurrentUser Member member) {
        memberService.logout(member.getId());

        return ResponseEntity.ok(new MessageDto(MessageDto.LOGOUT_SUCCESSFULLY));
    }
}
