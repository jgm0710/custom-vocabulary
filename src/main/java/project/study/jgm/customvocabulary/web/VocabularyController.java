package project.study.jgm.customvocabulary.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/vocabulary")
public class VocabularyController {

    @GetMapping("/personal")
    public String getPersonalVocabularyPage() {
        return "/vocabulary/personalVocabularyForm";
    }

}
