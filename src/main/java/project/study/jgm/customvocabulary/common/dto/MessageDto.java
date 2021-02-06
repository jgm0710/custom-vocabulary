package project.study.jgm.customvocabulary.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {

    private String message;

    public static final String CHANGE_MEMBER_ROLE_TO_USER_SUCCESSFULLY = "회원의 권한이 정상적으로 복구 되었습니다.";

    public static final String BAN_SUCCESSFULLY = "회원의 활동이 정상적으로 금지되었습니다.";

    public static final String SECESSION_SUCCESSFULLY = "회원 탈퇴가 성공적으로 완료 되었습니다.";

    public static final String UN_AUTHENTICATION = "access_token이 유효하지 않습니다.";

    public static final String GET_DIFFERENT_MEMBER_INFO = "다른 회원의 정보는 조회가 불가능합니다.";

    public static final String MODIFY_DIFFERENT_MEMBER_INFO = "다른 회원의 정보는 수정할 수 없습니다.";

    public static final String MODIFIED_SUCCESSFULLY = "회원 정보가 정상적으로 수정되었습니다.";

    public static final String CHANGED_PASSWORD = "비밀번호가 변경되었습니다.";

    public static final String LOGOUT_SUCCESSFULLY = "logout 이 정상적으로 완료되었습니다.";

}
