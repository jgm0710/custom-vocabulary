package project.study.jgm.customvocabulary.bbs.like;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.study.jgm.customvocabulary.bbs.Bbs;
import project.study.jgm.customvocabulary.bbs.BbsRepository;
import project.study.jgm.customvocabulary.bbs.BbsStatus;
import project.study.jgm.customvocabulary.bbs.exception.BbsNotFoundException;
import project.study.jgm.customvocabulary.bbs.exception.DeletedBbsException;
import project.study.jgm.customvocabulary.common.exception.ExistLikeException;
import project.study.jgm.customvocabulary.common.exception.NoExistLikeException;
import project.study.jgm.customvocabulary.common.exception.SelfLikeException;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRepository;
import project.study.jgm.customvocabulary.members.exception.MemberNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BbsLikeService {

    private final BbsLikeRepository bbsLikeRepository;

    private final MemberRepository memberRepository;

    private final BbsRepository bbsRepository;

    private final BbsLikeQueryRepository bbsLikeQueryRepository;

    @Transactional
    public BbsLike like(Long memberId, Long bbsId) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        Bbs bbs = bbsRepository.findById(bbsId).orElseThrow(BbsNotFoundException::new);

        if (checkExistLike(memberId, bbsId)) {
            throw new ExistLikeException();
        }

        if (bbs.getMember().getId().equals(member.getId())) {
            throw new SelfLikeException();
        }

        if (bbs.getStatus() == BbsStatus.DELETE) {
            throw new DeletedBbsException("삭제된 게시글에는 좋아요를 누를 수 없습니다.");
        }

        BbsLike bbsLike = BbsLike.createBbsLike(member, bbs);
        bbs.increaseLikeCount();

        return bbsLikeRepository.save(bbsLike);
    }

    public boolean getExistLike(Long memberId, Long bbsId) {
        return checkExistLike(memberId, bbsId);
    }

    @Transactional
    public void unLike(Long memberId, Long bbsId) {
        if (!checkExistLike(memberId, bbsId)) {
            throw new NoExistLikeException();
        }

        BbsLike bbsLike = bbsLikeQueryRepository.findLikeByMemberAndBbs(memberId, bbsId);


        bbsLikeRepository.delete(bbsLike);
        bbsLike.getBbs().decreaseLikeCount();
    }

    private boolean checkExistLike(Long memberId, Long bbsId) {
        BbsLike bbsLike = bbsLikeQueryRepository.findLikeByMemberAndBbs(memberId, bbsId);

        return bbsLike != null;
    }


}
