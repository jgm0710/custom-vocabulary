package project.study.jgm.customvocabulary.api;

import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.study.jgm.customvocabulary.members.MemberService;
import project.study.jgm.customvocabulary.security.dto.TokenDto;
import project.study.jgm.customvocabulary.security.dto.LoginDto;
import project.study.jgm.customvocabulary.security.dto.OnlyTokenDto;

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

        TokenDto tokenDto = memberService.login(loginDto);

        EntityModel<TokenDto> tokenDtoEntityModel = EntityModel.of(tokenDto);
        tokenDtoEntityModel.add(linkTo(LoginApiController.class).slash("login").withSelfRel());
        tokenDtoEntityModel.add(linkTo(IndexApiController.class).withRel("index"));

        return ResponseEntity.ok(tokenDtoEntityModel);
    }

    @PostMapping("/refresh")
    public ResponseEntity refresh(@RequestBody @Valid OnlyTokenDto onlyTokenDto, Errors errors) {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        TokenDto tokenDto = memberService.refresh(onlyTokenDto);

        EntityModel<TokenDto> tokenDtoEntityModel = EntityModel.of(tokenDto);
        tokenDtoEntityModel.add(linkTo(LoginApiController.class).slash("login").withSelfRel());
        tokenDtoEntityModel.add(linkTo(IndexApiController.class).withRel("index"));

        return ResponseEntity.ok(tokenDtoEntityModel);
    }

}
