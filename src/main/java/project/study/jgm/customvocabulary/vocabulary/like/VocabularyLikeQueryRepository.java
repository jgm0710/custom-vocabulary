package project.study.jgm.customvocabulary.vocabulary.like;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.study.jgm.customvocabulary.common.dto.CriteriaDto;
import project.study.jgm.customvocabulary.vocabulary.Vocabulary;
import project.study.jgm.customvocabulary.vocabulary.VocabularyDivision;

import javax.persistence.EntityManager;
import java.util.List;

import static project.study.jgm.customvocabulary.vocabulary.QVocabulary.vocabulary;
import static project.study.jgm.customvocabulary.vocabulary.like.QVocabularyLike.vocabularyLike;

@Repository
@RequiredArgsConstructor
public class VocabularyLikeQueryRepository {

    private final EntityManager em;

    private final JPAQueryFactory queryFactory;

    public List<Vocabulary> findVocabularyListByLikeByMember(Long memberId, CriteriaDto criteriaDto) {
        return queryFactory
                .select(vocabulary)
                .from(vocabularyLike)
                .join(vocabularyLike.vocabulary, vocabulary)
                .where(
                        vocabularyLike.member.id.eq(memberId),
                        vocabulary.division.eq(VocabularyDivision.SHARED)
                )
                .orderBy(vocabularyLike.registerDate.desc(), vocabulary.id.desc())
                .offset(criteriaDto.getOffset())
                .limit(criteriaDto.getLimit())
                .fetch();
    }

    public VocabularyLike findLikeByMemberAndVocabulary(Long memberId, Long vocabularyId) {
        return queryFactory
                .select(vocabularyLike)
                .from(vocabularyLike)
                .where(
                        vocabularyLike.member.id.eq(memberId),
                        vocabularyLike.vocabulary.id.eq(vocabularyId)
                )
                .fetchOne();
    }

}
