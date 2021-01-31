package project.study.jgm.customvocabulary.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.EntityModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthMessageDto {

    private String message;

    public static EntityModel createEntityModel(String message) {
        return EntityModel.of(new AuthMessageDto(message));
    }
}
