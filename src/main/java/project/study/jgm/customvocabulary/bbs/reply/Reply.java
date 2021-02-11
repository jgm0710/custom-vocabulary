package project.study.jgm.customvocabulary.bbs.reply;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.bbs.Bbs;
import project.study.jgm.customvocabulary.members.Member;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Reply {

    @Id
    @GeneratedValue
    @Column(name = "reply_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "bbs_id")
    private Bbs bbs;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id")
    private Reply parent;

//    @Builder.Default
//    @OneToMany(mappedBy = "parent")
//    private List<Reply> children = new ArrayList<>();

    @Column(length = 1200)
    private String content; //최대 1200자

    private int likeCount;

    private int childrenCount;

    private ReplyStatus status; //저장, 삭제 구분 [REGISTER, DELETE]

    private LocalDateTime registerDate;

    public static Reply createReply(Member member, Bbs bbs, Reply parent, String content) {
        Reply reply = Reply.builder()
                .member(member)
                .bbs(bbs)
                .parent(parent)
                .content(content)
                .likeCount(0)
                .status(ReplyStatus.REGISTER)
                .registerDate(LocalDateTime.now())
                .build();

        bbs.increaseReplyCount();

        if (parent != null) {
            parent.increaseChildrenCount();
        }

        return reply;
    }

    public void modify(String content) {
        this.content = content;
    }

    public void delete() {
        this.status = ReplyStatus.DELETE;
        this.bbs.decreaseReplyCount();
        if (this.parent != null) {
            this.parent.decreaseChildrenCount();
        }
    }

    public void increaseChildrenCount() {
        this.childrenCount++;
    }

    public void decreaseChildrenCount() {
        this.childrenCount--;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        this.likeCount--;
    }
}
