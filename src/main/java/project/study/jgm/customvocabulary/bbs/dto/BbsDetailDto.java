package project.study.jgm.customvocabulary.bbs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.bbs.Bbs;
import project.study.jgm.customvocabulary.bbs.BbsStatus;
import project.study.jgm.customvocabulary.members.Member;

import javax.persistence.*;
import java.time.LocalDateTime;

import static javax.persistence.FetchType.LAZY;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BbsDetailDto {
    private Long id;

    private String writer;

    private String title;

    private String content;

    private int views;

    private int likeCount;

    private int replyCount;

    private LocalDateTime registerDate;

    private LocalDateTime updateDate;

//id
//writer
//title
//content
//views
//likeCount
//replyCount
//registerDate
//updateDate

    public static BbsDetailDto bbsToDetail(Bbs bbs) {
        return BbsDetailDto.builder()
                .id(bbs.getId())
                .writer(bbs.getMember().getNickname())
                .title(bbs.getTitle())
                .content(bbs.getContent())
                .views(bbs.getViews())
                .likeCount(bbs.getLikeCount())
                .replyCount(bbs.getReplyCount())
                .registerDate(bbs.getRegisterDate())
                .updateDate(bbs.getUpdateDate())
                .build();
    }

}
