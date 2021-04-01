package project.study.jgm.customvocabulary.web;

import lombok.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/members")
public class MemberController {

    @GetMapping("/login")
    public String login() {
        return "/member/login";
    }

    @GetMapping("/join")
    public String register(Model model) {
        List<Integer> years = createYears();
        List<DateDto> month = createMonth();
        List<DateDto> days = createDays();

        model.addAttribute("years", years);
        model.addAttribute("month", month);
        model.addAttribute("days", days);

        return "/member/register";
    }

    private List<DateDto> createDays() {
        List<DateDto> days = new ArrayList<>();
        for (int i = 1; i < 32; i++) {
            DateDto dateDto = getDateDto(i);
            days.add(dateDto);
        }
        return days;
    }

    private List<DateDto> createMonth() {
        List<DateDto> month = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            DateDto dateDto = getDateDto(i);
            month.add(dateDto);
        }
        return month;
    }

    private List<Integer> createYears() {
        List<Integer> years = new ArrayList<>();
        int nowYear = LocalDate.now().getYear();
        int j = 0;
        for (int i = 0; i < 150; i++) {
            years.add(nowYear - j);
            j++;
        }
        return years;
    }

    @GetMapping("/profile")
    public String profile() {
        return "/member/profile";
    }

    private DateDto getDateDto(int i) {
        String value = null;

        if (i < 10) {
            value = "0" + i;
        } else {
            value = "" + i;
        }

        DateDto dateDto = DateDto.builder()
                .value(value)
                .text("" + i)
                .build();
        return dateDto;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    static class DateDto {
        String value;
        String text;
    }

}
