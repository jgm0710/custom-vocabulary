package project.study.jgm.customvocabulary.api;

import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class IndexApiController {

    @GetMapping
    public ResponseEntity index() {
        RepresentationModel representationModel = new RepresentationModel();
        Link joinLink = linkTo(MemberApiController.class).withRel("join");
        Link loginLink = linkTo(LoginApiController.class).slash("login").withRel("login");

        representationModel.add(joinLink);
        representationModel.add(loginLink);

        return ResponseEntity.ok(representationModel);
    }
}
