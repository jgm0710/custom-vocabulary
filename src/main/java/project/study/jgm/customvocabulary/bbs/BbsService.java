package project.study.jgm.customvocabulary.bbs;

import com.querydsl.core.QueryResults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.study.jgm.customvocabulary.bbs.dto.BbsCreateDto;
import project.study.jgm.customvocabulary.bbs.dto.BbsSearchDto;
import project.study.jgm.customvocabulary.bbs.dto.BbsUpdateDto;
import project.study.jgm.customvocabulary.bbs.exception.BbsNotFoundException;
import project.study.jgm.customvocabulary.bbs.exception.DeletedBbsException;
import project.study.jgm.customvocabulary.bbs.upload.BbsFileStorageService;
import project.study.jgm.customvocabulary.bbs.upload.BbsUploadFile;
import project.study.jgm.customvocabulary.bbs.upload.BbsUploadFileRepository;
import project.study.jgm.customvocabulary.common.dto.CriteriaDto;
import project.study.jgm.customvocabulary.common.upload.*;
import project.study.jgm.customvocabulary.common.upload.exception.MyFileNotFoundException;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRepository;
import project.study.jgm.customvocabulary.members.exception.MemberNotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BbsService {

    private final MemberRepository memberRepository;

    private final BbsRepository bbsRepository;

    private final BbsQueryRepository bbsQueryRepository;

    private final BbsFileStorageService bbsFileStorageService;

    private final BbsUploadFileRepository bbsUploadFileRepository;

    @Transactional
    public Bbs addBbs(Long memberId, BbsCreateDto createDto) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        Bbs bbs = Bbs.createBbs(member, createDto);

        Bbs savedBbs = bbsRepository.save(bbs);

        if (createDto.getFileIdList() != null) {
            this.addFiles(savedBbs.getId(), createDto.getFileIdList());
        }

        return savedBbs;
    }

    private void addFiles(Long bbsId, List<OnlyFileIdDto> fileIdList) {
        Bbs findBbs = bbsRepository.findById(bbsId).orElseThrow(BbsNotFoundException::new);

        for (OnlyFileIdDto onlyFileIdDto : fileIdList) {
            BbsUploadFile bbsUploadFile = bbsUploadFileRepository.findById(onlyFileIdDto.getFileId()).orElseThrow(MyFileNotFoundException::new);
            findBbs.addUploadFile(bbsUploadFile);
        }
    }

    public QueryResults<Bbs> getBbsList(BbsSearchDto bbsSearchDto) {
        return bbsQueryRepository.findAll(bbsSearchDto);
    }

    public QueryResults<Bbs> getBbsListByMember(Long memberId, CriteriaDto criteriaDto) {
        return bbsQueryRepository.findAllByMember(criteriaDto, memberId);
    }

    public Bbs getBbs(Long bbsId) {
        Bbs bbs = bbsRepository.findById(bbsId).orElseThrow(BbsNotFoundException::new);
        bbs.increaseViews();

        return bbs;
    }

    @Transactional
    public void modifyBbs(Long bbsId, BbsUpdateDto updateDto) {
        Bbs bbs = bbsRepository.findById(bbsId).orElseThrow(BbsNotFoundException::new);


        if (bbs.getStatus() == BbsStatus.DELETE) {
            throw new DeletedBbsException("삭제된 게시글은 수정이 불가능합니다.");
        }

        List<BbsUploadFile> bbsUploadFileList = new ArrayList<>();
        if (updateDto.getFileIdList() != null) {
            for (OnlyFileIdDto onlyFileIdDto : updateDto.getFileIdList()) {
                BbsUploadFile bbsUploadFile = bbsUploadFileRepository.findById(onlyFileIdDto.getFileId()).orElseThrow(MyFileNotFoundException::new);
                bbsUploadFileList.add(bbsUploadFile);
            }
        }

        bbs.modify(updateDto, bbsUploadFileList);
    }

    @Transactional
    public void deleteBbs(Long bbsId) {
        Bbs bbs = bbsRepository.findById(bbsId).orElseThrow(BbsNotFoundException::new);

        if (bbs.getStatus() == BbsStatus.DELETE) {
            throw new DeletedBbsException("이미 삭제된 게시글은 삭제가 불가능합니다.");
        }

        bbs.delete();
    }

}
