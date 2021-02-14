package project.study.jgm.customvocabulary.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/members")
public class MemberController {

    @GetMapping("/join")
    public String join() {
        return "member/member-join";
    }

    @GetMapping("/login")
    public String login() {
        return "member/loginForm";
    }
}
