package project.study.jgm.customvocabulary.bbs.reply;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.study.jgm.customvocabulary.bbs.Bbs;
import project.study.jgm.customvocabulary.bbs.BbsRepository;
import project.study.jgm.customvocabulary.bbs.BbsStatus;
import project.study.jgm.customvocabulary.bbs.exception.BbsNotFoundException;
import project.study.jgm.customvocabulary.bbs.exception.DeletedBbsException;
import project.study.jgm.customvocabulary.bbs.reply.dto.ReplyCreateDto;
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
    public Reply addReply(Long memberId, Long bbsId, ReplyCreateDto createDto) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        Bbs bbs = bbsRepository.findById(bbsId).orElseThrow(BbsNotFoundException::new);

        if (bbs.getStatus() == BbsStatus.DELETE) {
            throw new DeletedBbsException("삭제된 게시글에는 댓글을 작성할 수 없습니다.");
        }

        Reply reply = Reply.createReply(member, bbs, createDto);

        return replyRepository.save(reply);
    }

    public List<Reply> getReplyList(Long bbsId, CriteriaDto criteriaDto, ReplySortCondition sortCondition) {
        return replyQueryRepository.findAll(criteriaDto, bbsId, sortCondition);
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
