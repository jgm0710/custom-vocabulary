package project.study.jgm.customvocabulary;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {

    @GetMapping("/hello")
    public String hello(Model model) {
        model.addAttribute("hello", "hello controller");
        return "hello";
    }

    @GetMapping("/api/hello")
    @ResponseBody
    public ResponseEntity helloApi(HelloDto helloDto) {
        return ResponseEntity.ok(helloDto);
    }
}
