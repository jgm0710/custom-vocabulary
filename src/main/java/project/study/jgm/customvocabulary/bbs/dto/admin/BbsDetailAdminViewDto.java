package project.study.jgm.customvocabulary.bbs.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import project.study.jgm.customvocabulary.bbs.Bbs;
import project.study.jgm.customvocabulary.bbs.BbsStatus;
import project.study.jgm.customvocabulary.members.Member;

import javax.persistence.*;
import java.time.LocalDateTime;

import static javax.persistence.FetchType.LAZY;

@Data
@Builder
@AllArgsConstructor
public class BbsDetailAdminViewDto {
    private Long id;

    private String writer;

    private String title;

    private String content;

    private int views;

    private int likeCount;

    private int replyCount;

    private LocalDateTime registerDate;

    private LocalDateTime updateDate;

    private BbsStatus status;   //Bbs 저장 상태 표시 [REGISTER, DELETE]

    private boolean allowModificationAndDeletion;

//.id
//.member
//.title
//.content
//.views
//.likeCount
//.replyCount
//.registerDate
//.updateDate
//.status

    public static BbsDetailAdminViewDto bbsToDetailAdminView(Bbs bbs) {
        return BbsDetailAdminViewDto.builder()
                .id(bbs.getId())
                .writer(bbs.getMember().getNickname())
                .title(bbs.getTitle())
                .content(bbs.getContent())
                .views(bbs.getViews())
                .likeCount(bbs.getLikeCount())
                .replyCount(bbs.getReplyCount())
                .registerDate(bbs.getRegisterDate())
                .updateDate(bbs.getUpdateDate())
                .status(bbs.getStatus())
                .allowModificationAndDeletion(true)
                .build();
    }
}
