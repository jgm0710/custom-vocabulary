package project.study.jgm.customvocabulary.vocabulary.like;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.study.jgm.customvocabulary.common.dto.CriteriaDto;
import project.study.jgm.customvocabulary.common.exception.ExistLikeException;
import project.study.jgm.customvocabulary.common.exception.NoExistLikeException;
import project.study.jgm.customvocabulary.common.exception.SelfLikeException;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRepository;
import project.study.jgm.customvocabulary.members.exception.MemberNotFoundException;
import project.study.jgm.customvocabulary.vocabulary.Vocabulary;
import project.study.jgm.customvocabulary.vocabulary.VocabularyDivision;
import project.study.jgm.customvocabulary.vocabulary.VocabularyRepository;
import project.study.jgm.customvocabulary.vocabulary.exception.BadRequestByDivision;
import project.study.jgm.customvocabulary.vocabulary.exception.VocabularyNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VocabularyLikeService {

    private final VocabularyLikeRepository vocabularyLikeRepository;

    private final VocabularyLikeQueryRepository vocabularyLikeQueryRepository;

    private final MemberRepository memberRepository;

    private final VocabularyRepository vocabularyRepository;

    @Transactional
    public VocabularyLike like(Long memberId, Long vocabularyId) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyId).orElseThrow(VocabularyNotFoundException::new);

        if (vocabulary.getDivision() != VocabularyDivision.SHARED) {
            throw new BadRequestByDivision();
        }

        if (checkExistLike(memberId, vocabularyId)) {
            throw new ExistLikeException();
        }

        if (vocabulary.getMember().getId().equals(memberId)) {
            throw new SelfLikeException();
        }

        VocabularyLike vocabularyLike = VocabularyLike.createVocabularyLike(member, vocabulary);
        vocabulary.increaseLikeCount();

        return vocabularyLikeRepository.save(vocabularyLike);
    }

    public List<Vocabulary> getVocabularyListByLikeByMember(Long memberId, CriteriaDto criteriaDto) {
        return vocabularyLikeQueryRepository.findVocabularyListByLikeByMember(memberId, criteriaDto);
    }

    public boolean getExistLike(Long memberId, Long vocabularyId) {
        return checkExistLike(memberId, vocabularyId);
    }

    @Transactional
    public void unLike(Long memberId, Long vocabularyId) {
        if (!checkExistLike(memberId, vocabularyId)) {
            throw new NoExistLikeException();
        }

        VocabularyLike vocabularyLike = vocabularyLikeQueryRepository.findLikeByMemberAndVocabulary(memberId, vocabularyId);

        vocabularyLikeRepository.delete(vocabularyLike);
        vocabularyLike.getVocabulary().decreaseLikeCount();
    }

    private boolean checkExistLike(Long memberId, Long vocabularyId) {
        VocabularyLike vocabularyLike = vocabularyLikeQueryRepository.findLikeByMemberAndVocabulary(memberId, vocabularyId);

        return vocabularyLike!=null;
    }
}
