package project.study.jgm.customvocabulary.bbs;

import com.querydsl.core.QueryResults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.study.jgm.customvocabulary.bbs.dto.BbsCreateDto;
import project.study.jgm.customvocabulary.bbs.dto.BbsUpdateDto;
import project.study.jgm.customvocabulary.bbs.exception.BbsNotFoundException;
import project.study.jgm.customvocabulary.bbs.exception.DeletedBbsException;
import project.study.jgm.customvocabulary.common.dto.CriteriaDto;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRepository;
import project.study.jgm.customvocabulary.members.exception.MemberNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BbsService {

    private final MemberRepository memberRepository;

    private final BbsRepository bbsRepository;

    private final BbsQueryRepository bbsQueryRepository;

    public Bbs addBbs(Long memberId, BbsCreateDto createDto) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        Bbs bbs = Bbs.createBbs(member, createDto);

        return bbsRepository.save(bbs);
    }

    public QueryResults<Bbs> getBbsList(CriteriaDto criteriaDto, BbsSortCondition sortCondition) {
        return bbsQueryRepository.findAll(criteriaDto, sortCondition);
    }

    public QueryResults<Bbs> getBbsListByMember(Long memberId, CriteriaDto criteriaDto) {
        return bbsQueryRepository.findAllByMember(criteriaDto, memberId);
    }

    public Bbs getBbs(Long bbsId) {
        Bbs bbs = bbsRepository.findById(bbsId).orElseThrow(BbsNotFoundException::new);

        if (bbs.getStatus() == BbsStatus.DELETE) {
            throw new DeletedBbsException("삭제된 게시글은 조회가 불가능합니다.");
        }

        bbs.increaseViews();

        return bbs;
    }

    public void modifyBbs(Long bbsId, BbsUpdateDto updateDto) {
        Bbs bbs = bbsRepository.findById(bbsId).orElseThrow(BbsNotFoundException::new);

        if (bbs.getStatus() == BbsStatus.DELETE) {
            throw new DeletedBbsException("삭제된 게시글은 수정이 불가능합니다.");
        }

        bbs.modify(updateDto);
    }

    public void deleteBbs(Long bbsId) {
        Bbs bbs = bbsRepository.findById(bbsId).orElseThrow(BbsNotFoundException::new);

        if (bbs.getStatus() == BbsStatus.DELETE) {
            throw new DeletedBbsException("이미 삭제된 게시글은 삭제가 불가능합니다.");
        }

        bbs.delete();
    }
}
