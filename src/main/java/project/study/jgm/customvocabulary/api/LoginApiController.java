package project.study.jgm.customvocabulary.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import project.study.jgm.customvocabulary.common.dto.ResponseDto;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberService;
import project.study.jgm.customvocabulary.members.exception.RefreshTokenExpirationException;
import project.study.jgm.customvocabulary.members.exception.RefreshTokenNotFoundException;
import project.study.jgm.customvocabulary.members.exception.UnauthorizedMemberException;
import project.study.jgm.customvocabulary.security.CurrentUser;
import project.study.jgm.customvocabulary.security.dto.LoginDto;
import project.study.jgm.customvocabulary.security.dto.OnlyTokenDto;
import project.study.jgm.customvocabulary.security.dto.TokenDto;
import project.study.jgm.customvocabulary.security.exception.PasswordMismatchException;

import javax.validation.Valid;

import static project.study.jgm.customvocabulary.common.dto.MessageVo.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api")
@Slf4j
public class LoginApiController {

    private final MemberService memberService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid LoginDto loginDto, Errors errors) {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            TokenDto tokenDto = memberService.login(loginDto);
            log.info("Login success!");

            return ResponseEntity.ok(new ResponseDto<>(tokenDto, LOGIN_SUCCESSFULLY));

        } catch (UsernameNotFoundException e) {
            log.info("User not found -> login fail...");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(e.getMessage()));
        } catch (PasswordMismatchException e) {
            log.info("Password mismatch -> login fail...");
            return ResponseEntity.badRequest().body(new ResponseDto<>(e.getMessage()));
        } catch (UnauthorizedMemberException e) {
            log.info("A member who does not have activity rights attempts to log in.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>(e.getMessage()));
        }

    }

    @PostMapping("/refresh")
    public ResponseEntity refresh(@RequestBody @Valid OnlyTokenDto onlyTokenDto, Errors errors) {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            TokenDto tokenDto = memberService.refresh(onlyTokenDto);

            return ResponseEntity.ok(new ResponseDto<>(tokenDto, REFRESH_SUCCESSFULLY));

        } catch (RefreshTokenNotFoundException e) {
            log.info("Refresh token not found -> refresh login fail...");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>(e.getMessage()));
        } catch (RefreshTokenExpirationException e) {
            log.info("Refresh token expiration -> refresh login fail...");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>(e.getMessage()));
        }
    }

    @GetMapping("/logout")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity logout(@CurrentUser Member member) {
        memberService.logout(member.getId());

        return ResponseEntity.ok(new ResponseDto<>(LOGOUT_SUCCESSFULLY));
    }
}
