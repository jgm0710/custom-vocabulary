package project.study.jgm.customvocabulary.members;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MemberRole {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN"),
    BAN("ROLE_BAN"),
    SECESSION("ROLE_SECESSION");

    private String roleName;
}
