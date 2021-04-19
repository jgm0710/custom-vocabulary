package project.study.jgm.customvocabulary.bbs.reply;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.study.jgm.customvocabulary.bbs.Bbs;
import project.study.jgm.customvocabulary.bbs.BbsRepository;
import project.study.jgm.customvocabulary.bbs.BbsStatus;
import project.study.jgm.customvocabulary.bbs.exception.BbsNotFoundException;
import project.study.jgm.customvocabulary.bbs.exception.DeletedBbsException;
import project.study.jgm.customvocabulary.bbs.reply.exception.DeletedReplyException;
import project.study.jgm.customvocabulary.bbs.reply.exception.ReplyNotFoundException;
import project.study.jgm.customvocabulary.common.dto.CriteriaDto;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRepository;
import project.study.jgm.customvocabulary.members.exception.MemberNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReplyService {

    private final MemberRepository memberRepository;

    private final BbsRepository bbsRepository;

    private final ReplyRepository replyRepository;

    private final ReplyQueryRepository replyQueryRepository;

    @Transactional
    public Reply addReply(Long memberId, Long bbsId, String content) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        Bbs bbs = bbsRepository.findById(bbsId).orElseThrow(BbsNotFoundException::new);

        if (bbs.getStatus() == BbsStatus.DELETE) {
            throw new DeletedBbsException("삭제된 게시글에는 댓글을 작성할 수 없습니다.");
        }

        Reply reply = Reply.createReply(member, bbs, null, content);

        return replyRepository.save(reply);
    }

    @Transactional
    public Reply addReplyOfReply(Long memberId, Long parentId, String content) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        Reply parent = replyRepository.findById(parentId).orElseThrow(ReplyNotFoundException::new);
        Reply reply = Reply.createReply(member, parent.getBbs(), parent, content);

        return replyRepository.save(reply);
    }

    public Reply getReply(Long replyId) {
        return replyRepository.findById(replyId).orElseThrow(ReplyNotFoundException::new);
    }

    public List<Reply> getReplyParentList(Long bbsId, CriteriaDto criteria, ReplySortType sortType) {
        Bbs findBbs = bbsRepository.findById(bbsId).orElseThrow(BbsNotFoundException::new);
        if (findBbs.getStatus() == BbsStatus.DELETE) {
            throw new DeletedBbsException("삭제된 게시글의 댓글은 조회할 수 없습니다.");
        }
        return replyQueryRepository.findParents(criteria, bbsId, sortType);
    }

    public List<Reply> getReplyChildList(Long parentId, CriteriaDto criteria) {
        Reply parent = replyRepository.findById(parentId).orElseThrow(ReplyNotFoundException::new);
        if (parent.getStatus() == ReplyStatus.DELETE) {
            throw new DeletedReplyException("삭제된 댓글에 등록된 댓글은 조회가 불가능합니다.");
        }

        return replyQueryRepository.findChildren(criteria, parentId);
    }

    @Transactional
    public void modifyReply(Long replyId, String content) {
        Reply reply = replyRepository.findById(replyId).orElseThrow(ReplyNotFoundException::new);

        if (reply.getStatus() == ReplyStatus.DELETE) {
            throw new DeletedReplyException("삭제된 댓글은 수정할 수 없습니다.");
        }

        reply.modify(content);
    }

    @Transactional
    public void deleteReply(Long replyId) {
        Reply reply = replyRepository.findById(replyId).orElseThrow(ReplyNotFoundException::new);

        if (reply.getStatus() == ReplyStatus.DELETE) {
            throw new DeletedReplyException("삭제된 댓글은 삭제할 수 없습니다.");
        }

        reply.delete();
    }
}
