package project.study.jgm.customvocabulary.bbs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.bbs.dto.BbsCreateDto;
import project.study.jgm.customvocabulary.bbs.dto.BbsUpdateDto;
import project.study.jgm.customvocabulary.members.Member;

import javax.persistence.*;
import java.time.LocalDateTime;

import static javax.persistence.FetchType.LAZY;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Bbs {

    @Id
    @GeneratedValue
    @Column(name = "bbs_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    private int views;

    private int likeCount;

    private int replyCount;

    private LocalDateTime registerDate;

    private LocalDateTime updateDate;

    @Enumerated
    private BbsStatus status;   //Bbs 저장 상태 표시 [REGISTER, DELETE]

    @Override
    public String toString() {
        return "Bbs{" +
                "id=" + id +
//                ", member=" + member +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", views=" + views +
                ", likeCount=" + likeCount +
                ", replyCount=" + replyCount +
                ", registerDate=" + registerDate +
                ", updateDate=" + updateDate +
                ", status=" + status +
                '}';
    }

    public static Bbs createBbs(Member member, BbsCreateDto createDto) {
        Bbs bbs = Bbs.builder()
                .member(member)
                .title(createDto.getTitle())
                .content(createDto.getContent())
                .views(0)
                .likeCount(0)
                .replyCount(0)
                .registerDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .status(BbsStatus.REGISTER)
                .build();

        member.addBbs();

        return bbs;
    }

    public void increaseViews() {
        this.views++;
    }

    public void modify(BbsUpdateDto updateDto) {
        this.title = updateDto.getTitle();
        this.content = updateDto.getContent();
        this.updateDate = LocalDateTime.now();
    }

    public void delete() {
        this.status = BbsStatus.DELETE;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        this.likeCount--;
    }

    public void increaseReplyCount() {
        this.replyCount++;
    }

    public void decreaseReplyCount() {
        this.replyCount--;
    }
}