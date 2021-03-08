package project.study.jgm.customvocabulary.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class MessageVo {

    /**
     * Login
     */

    public static final String LOGIN_SUCCESSFULLY = "로그인이 정상적으로 완료되었습니다.";

    public static final String REFRESH_SUCCESSFULLY = "refresh_token 을 통한 로그인이 정상적으로 완료되었습니다.";

    /**
     * Member
     */

    public static final String MEMBER_JOIN_SUCCESSFULLY = "회원가입이 정상적으로 완료되었습니다.";

    public static final String CHANGE_MEMBER_ROLE_TO_USER_SUCCESSFULLY = "회원의 권한이 정상적으로 복구 되었습니다.";

    public static final String BAN_SUCCESSFULLY = "회원의 활동이 정상적으로 금지되었습니다.";

    public static final String SECESSION_SUCCESSFULLY = "회원 탈퇴가 성공적으로 완료 되었습니다.";

    public static final String UN_AUTHENTICATION = "access_token이 유효하지 않습니다.";

    public static final String GET_DIFFERENT_MEMBER_INFO = "다른 회원의 정보는 조회가 불가능합니다.";

    public static final String MODIFY_DIFFERENT_MEMBER_INFO = "다른 회원의 정보는 수정할 수 없습니다.";

    public static final String MODIFIED_MEMBER_INFO_SUCCESSFULLY = "회원 정보가 정상적으로 수정되었습니다.";

    public static final String CHANGED_PASSWORD_SUCCESSFULLY = "비밀번호가 정상적으로 변경되었습니다.";

    public static final String LOGOUT_SUCCESSFULLY = "logout 이 정상적으로 완료되었습니다. 해당 회원의 Refresh Token 을 삭제합니다.";

    public static final String GET_MEMBER_BY_ADMIN_SUCCESSFULLY = "관리자 권한으로 조회하는 회원 정보가 정상적으로 조회되었습니다.";

    public static final String GET_MEMBER_SUCCESSFULLY = "회원 정보가 정상적으로 조회되었습니다.";

    public static final String GET_MEMBER_LIST_BY_ADMIN_SUCCESSFULLY = "관리자 권한으로 회원 목록이 정상적으로 조회되었습니다.";


    /**
     * Bbs
     */

    public static final String BBS_REGISTERED_SUCCESSFULLY = "게시글이 성공적으로 등록되었습니다.";

    public static final String UNAUTHORIZED_USERS_VIEW_DELETED_POSTS = "관리자가 아닌 사용자는 삭제된 게시글을 조회할 수 없습니다.";

    public static final String MODIFY_BBS_OF_DIFFERENT_MEMBER = "다른 회원의 게시글은 수정할 수 없습니다.";

    public static final String MODIFIED_BBS_SUCCESSFULLY = "게시글 수정이 정상적으로 완료되었습니다.";

    public static final String DELETE_BBS_OF_DIFFERENT_MEMBER = "다른 회원의 게시글은 삭제할 수 없습니다.";

    public static final String DELETE_BBS_SUCCESSFULLY = "게시글 삭제가 정상적으로 완료되었습니다.";

    public static final String ADD_LIKE_TO_BBS_SUCCESSFULLY = "게시글에 좋아요가 정상적으로 등록되었습니다.";

    public static final String UNLIKE_BBS_SUCCESSFULLY = "게시글에 등록된 좋아요가 성공적으로 해제되었습니다.";

    public static final String GET_BBS_LIST_BY_ADMIN_SUCCESSFULLY = "관리자 권한으로 게시글 목록을 정상적으로 조회하였습니다.";

    public static final String GET_BBS_LIST_SUCCESSFULLY = "게시글 목록 조회가 성공적으로 완료되었습니다.";

    public static final String GET_BBS_BY_ADMIN_SUCCESSFULLY = "관리자 권한으로 게시글 조회가 정상적으로 완료되었습니다";

    public static final String GET_BBS_SUCCESSFULLY = "게시글 조회가 정상적으로 완료되었습니다.";

    public static final String MODIFY_BBS_BY_ADMIN_SUCCESSFULLY = "관리자 권한으로 게시글이 정상적으로 수정되었습니다.";

    public static final String UPLOAD_BBS_FILE_SUCCESSFULLY = "게시글 파일이 정상적으로 업로드되었습니다.";

    public static final String UPLOAD_BBS_FILE_LIST_SUCCESSFULLY = "게시글 파일 목록이 정상적으로 업로드되었습니다.";


    /**
     * Reply
     */

    public static final String REPLY_REGISTER_SUCCESSFULLY = "댓글이 정상적으로 등록되었습니다.";

    public static final String DELETE_REPLY_OF_DIFFERENT_MEMBER = "다른 회원의 댓글은 삭제할 수 없습니다.";

    public static final String DELETE_REPLY_SUCCESSFULLY = "댓글이 정상적으로 삭제되었습니다.";

    public static final String MODIFY_REPLY_OF_DIFFERENT_MEMBER = "다른 회원의 댓글은 수정할 수 없습니다.";

    public static final String MODIFY_REPLY_SUCCESSFULLY = "댓글이 성공적으로 수정되었습니다.";

    public static final String ADD_LIKE_TO_REPLY_SUCCESSFULLY = "댓글에 좋아요가 정상적으로 등록되었습니다.";

    public static final String UNLIKE_REPLY_SUCCESSFULLY = "댓글에 등록된 좋아요가 성공적으로 해제되었습니다.";

    public static final String GET_PARENT_REPLY_LIST_SUCCESSFULLY = "댓글 목록이 정상적으로 조회되었습니다.";

    public static final String GET_CHILD_REPLY_LIST_SUCCESSFULLY = "댓글에 등록된 댓글 목록이 정상적으로 조회되었습니다.";

    /**
     * Category
     */

    public static final String ADD_PERSONAL_CATEGORY_SUCCESSFULLY = "개인용 카테고리가 정상적으로 추가되었습니다.";

    public static final String GET_PERSONAL_CATEGORY_LIST_OF_DIFFERENT_MEMBER = "다른 회원의 카테고리 목록은 조회할 수 없습니다.";

    public static final String MODIFY_CATEGORY_SUCCESSFULLY = "카테고리 수정이 성공적으로 완료되었습니다.";

    public static final String MODIFY_CATEGORY_OF_DIFFERENT_MEMBER = "다른 회원의 카테고리는 수정할 수 없습니다.";

    public static final String MODIFY_SHARED_CATEGORY_BY_USER = "일반 회원은 공유 카테고리의 수정이 불가능합니다.";

    public static final String DELETE_CATEGORY_SUCCESSFULLY = "카테고리 삭제가 정상적으로 완료되었습니다.";

    public static final String DELETE_CATEGORY_OF_DIFFERENT_MEMBER = "다른 회원의 카테고리는 삭제할 수 없습니다.";

    public static final String GET_PERSONAL_CATEGORY_LIST_SUCCESSFULLY = "개인용 카테고리 목록이 정상적으로 조회되었습니다.";

    public static final String ADD_SHARED_CATEGORY_BY_ADMIN_SUCCESSFULLY = "관리자 권한으로 공유 카테고리가 정상적으로 등록되었습니다.";

    public static final String GET_SHARED_CATEGORY_LIST_SUCCESSFULLY = "공유 카테고리 목록이 정상적으로 조회 되었습니다.";

    /**
     * Vocabulary
     */

    public static final String UPLOAD_WORD_IMAGE_FILE_SUCCESSFULLY = "단어 이미지 파일이 정상적으로 업로드되었습니다.";

    public static final String UPLOAD_VOCABULARY_THUMBNAIL_IMAGE_FILE_SUCCESSFULLY = "단어장 이미지 파일이 정상적으로 업로드되었습니다.";

    public static final String ADD_VOCABULARY_SUCCESSFULLY = "단어장이 성공적으로 추가되었습니다.";

    public static final String UPDATE_WORD_LIST_OF_VOCABULARY_OF_DIFFERENT_MEMBER = "다른 회원의 단어장에는 단어 목록을 변경할 수 없습니다.";

    public static final String UPDATE_WORD_LIST_OF_PERSONAL_VOCABULARY_SUCCESSFULLY = "단어장에 단어 목록이 성공적으로 변경되었습니다.";

    public static final  String CHECK_MEMORIZE_OF_VOCABULARY_OF_DIFFERENT_MEMBER = "다른 회원의 단어에는 암기 체크를 할 수 없습니다.";

    public static final String CHECK_MEMORIZE_SUCCESSFULLY = "암기 체크가 정상적으로 이루어졌습니다.";

    public static final String MODIFY_PERSONAL_VOCABULARY_OF_DIFFERENT_MEMBER = "다른 회원의 단어장은 수정할 수 없습니다.";

    public static final String MODIFY_PERSONAL_VOCABULARY_SUCCESSFULLY = "단어장이 정상적으로 수정되었습니다.";

    public static final String SHARE_VOCABULARY_SUCCESSFULLY = "단어장이 성공적으로 공유되었습니다.";

    public static final String SHARE_VOCABULARY_OF_DIFFERENT_MEMBER = "다른 회원의 단어장은 공유할 수 없습니다.";

    public static final String GET_PERSONAL_VOCABULARY_LIST_OF_DIFFERENT_MEMBER = "다른 회원의 개인 단어장 목록은 조회할 수 없습니다.";

    public static final String GET_PERSONAL_VOCABULARY_LIST_SUCCESSFULLY = "개인 단어장 목록이 정상적으로 조회되었습니다.";

    public static final String MOVE_CATEGORY_OF_VOCABULARY_OF_DIFFERENT_MEMBER = "다른 회원의 단어장의 카테고리는 이동시킬 수 없습니다.";

    public static final String MOVE_CATEGORY_OF_PERSONAL_VOCABULARY_SUCCESSFULLY = "개인 단어장의 카테고리가 정상적으로 변경되었습니다.";

    public static final String MOVE_CATEGORY_OF_SHARED_VOCABULARY_SUCCESSFULLY = "공유 단어장의 카테고리가 정상적으로 변경되었습니다.";

    public static final String GET_PERSONAL_VOCABULARY_OF_DIFFERENT_MEMBER = "다른 회원의 개인 단어장은 조회할 수 없습니다.";

    public static final String GET_PERSONAL_VOCABULARY_SUCCESSFULLY = "개인 단어장이 정상적으로 조회되었습니다.";

    public static final String GET_SHARED_VOCABULARY_SUCCESSFULLY = "공유 단어장이 정상적으로 조회되었습니다.";

    public static final String GET_DELETED_VOCABULARY_BY_USER = "삭제된 단어장은 조회가 불가능합니다.";

    public static final String GET_DELETED_VOCABULARY_BY_ADMIN = "관리자 권한으로 삭제된 단어장이 조회되었습니다.";

    public static final String GET_UNSHARED_VOCABULARY_BY_USER = "공유가 취소된 단어장은 조회가 불가능합니다.";

    public static final String GET_UNSHARED_VOCABULARY_BY_ADMIN = "관리자 권한으로 공유가 취소된 단어장이 조회되었습니다.";

    public static final String ACCESS_TO_SENSITIVE_VOCABULARY_BY_UNAUTHORIZED_USER = "인증되지 않은 사용자는 해당 항목에 접근할 수 없습니다.";

    public static final String GET_SHARED_VOCABULARY_LIST_BY_MEMBER_SUCCESSFULLY = "해당 회원이 공유한 단어 목록이 정상적으로 조회되었습니다.";

    public static final String GET_SHARED_VOCABULARY_LIST_SUCCESSFULLY = "공유 단어장 목록이 정상적으로 조회되었습니다.";

    public static final String DOWNLOAD_SHARED_VOCABULARY_SUCCESSFULLY = "공유 단어장을 성공적으로 다운로드하였습니다.";

    public static final String UNSHARED_SHARED_VOCABULARY_OF_DIFFERENT_MEMBER = "다른 회원의 공유 단어장은 공유 해제가 불가능합니다.";

    public static final String UNSHARED_SHARED_VOCABULARY_SUCCESSFULLY = "단어장의 공유 해제가 정상적으로 완료되었습니다.";

    public static final String ADD_LIKE_TO_SHARED_VOCABULARY_SUCCESSFULLY = "공유 단어장에 좋아요가 성공적으로 등록되었습니다.";

    public static final String UNLIKE_SHARED_VOCABULARY_SUCCESSFULLY = "공유 단어장에 등록된 좋아요가 정상적으로 해제되었습니다.";

    public static final String GET_DELETED_VOCABULARY_LIST_OF_MEMBER_SUCCESSFULLY = "해당 회원의 삭제된 단어장 목록이 정상적으로 조회되었습니다.";

    public static final String GET_UNSHARED_VOCABULARY_LIST_OF_MEMBER_SUCCESSFULLY = "해당 회원의 공유 해제 단어장 목록을 정상적으로 조회하였습니다.";

    public static final String DELETE_PERSONAL_VOCABULARY_OF_DIFFERENT_MEMBER = "다른 회원의 개인 단어장은 삭제할 수 없습니다.";

    public static final String DELETE_PERSONAL_VOCABULARY_SUCCESSFULLY = "개인 단어장이 정상적으로 삭제되었습니다.";

    /**
     * Common
     */

    public static final String GET_DOWNLOAD_FILE_SUCCESSFULLY = "요청하신 파일이 정상적으로 다운로드 되었습니다.";

    public static final String GET_THUMBNAIL_SUCCESSFULLY = "요청하신 썸네일 이미지가 정상적으로 다운로드 되었습니다.";

}
