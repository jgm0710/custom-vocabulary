package project.study.jgm.customvocabulary.bbs.reply.like;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.study.jgm.customvocabulary.bbs.reply.Reply;
import project.study.jgm.customvocabulary.bbs.reply.ReplyRepository;
import project.study.jgm.customvocabulary.bbs.reply.ReplyStatus;
import project.study.jgm.customvocabulary.bbs.reply.exception.DeletedReplyException;
import project.study.jgm.customvocabulary.bbs.reply.exception.ReplyNotFoundException;
import project.study.jgm.customvocabulary.common.exception.ExistLikeException;
import project.study.jgm.customvocabulary.common.exception.NoExistLikeException;
import project.study.jgm.customvocabulary.common.exception.SelfLikeException;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRepository;
import project.study.jgm.customvocabulary.members.exception.MemberNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReplyLikeService {

    private final MemberRepository memberRepository;

    private final ReplyRepository replyRepository;

    private final ReplyLikeRepository replyLikeRepository;

    private final ReplyLikeQueryRepository replyLikeQueryRepository;

    @Transactional
    public ReplyLike like(Long memberId, Long replyId) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        Reply reply = replyRepository.findById(replyId).orElseThrow(ReplyNotFoundException::new);

        if (checkExistLike(memberId, replyId)) {
            throw new ExistLikeException();
        }
        if (reply.getStatus() == ReplyStatus.DELETE) {
            throw new DeletedReplyException("삭제된 댓글에는 좋아요를 누를 수 없습니다.");
        }
        if (reply.getMember().getId().equals(memberId)) {
            throw new SelfLikeException();
        }

        ReplyLike replyLike = ReplyLike.createReplyLike(member, reply);

        return replyLikeRepository.save(replyLike);
    }

    public boolean getExistLike(Long memberId, Long replyId) {
        return checkExistLike(memberId, replyId);
    }

    @Transactional
    public void unLike(Long memberId, Long replyId) {
        if (!checkExistLike(memberId, replyId)) {
            throw new NoExistLikeException();
        }

        ReplyLike replyLike = replyLikeQueryRepository.findByMemberAndReply(memberId, replyId);
        replyLike.delete(replyLikeRepository);
    }

    private boolean checkExistLike(Long memberId, Long replyId) {
        return replyLikeQueryRepository.findByMemberAndReply(memberId, replyId) != null;
    }
}
