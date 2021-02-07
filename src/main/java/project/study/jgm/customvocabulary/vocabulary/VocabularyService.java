package project.study.jgm.customvocabulary.vocabulary;

import com.querydsl.core.QueryResults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.study.jgm.customvocabulary.common.dto.CriteriaDto;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRepository;
import project.study.jgm.customvocabulary.members.exception.MemberNotFoundException;
import project.study.jgm.customvocabulary.vocabulary.category.Category;
import project.study.jgm.customvocabulary.vocabulary.category.CategoryDivision;
import project.study.jgm.customvocabulary.vocabulary.category.CategoryRepository;
import project.study.jgm.customvocabulary.vocabulary.category.exception.CategoryNotFoundException;
import project.study.jgm.customvocabulary.vocabulary.dto.PersonalVocabularyCreateDto;
import project.study.jgm.customvocabulary.vocabulary.dto.PersonalVocabularyUpdateDto;
import project.study.jgm.customvocabulary.vocabulary.exception.*;
import project.study.jgm.customvocabulary.vocabulary.word.Word;
import project.study.jgm.customvocabulary.vocabulary.word.WordRepository;
import project.study.jgm.customvocabulary.vocabulary.word.dto.WordDto;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;

    private final VocabularyQueryRepository vocabularyQueryRepository;

    private final MemberRepository memberRepository;

    private final CategoryRepository categoryRepository;

    private final WordRepository wordRepository;

    /**
     * personal
     */

    @Transactional
    public Vocabulary createPersonalVocabulary(Long memberId, Long categoryId, PersonalVocabularyCreateDto createDto) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        Category category = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
        Vocabulary personalVocabulary = Vocabulary.createPersonalVocabulary(member, category, createDto);

        if (!category.getDivision().toString().equals(personalVocabulary.getDivision().toString())) {
            throw new DivisionMismatchException();
        }

        vocabularyRepository.save(personalVocabulary);

        return personalVocabulary;

    }

    @Transactional
    public void addWordListToPersonalVocabulary(Long vocabularyId, List<WordDto> wordDtoList) {
        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyId).orElseThrow(VocabularyNotFoundException::new);

        if (vocabulary.getDivision() != VocabularyDivision.PERSONAL) {
            throw new BadRequestByDivision();
        }

        for (WordDto wordDto :
                wordDtoList) {
            wordDto.setMemorisedCheck(false);
            Word.addWordToVocabulary(vocabulary, wordDto);
        }
    }

    @Transactional
    public void updateWordListToPersonalVocabulary(Long vocabularyId, List<WordDto> wordDtoList) {
        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyId).orElseThrow(VocabularyNotFoundException::new);

        if (vocabulary.getDivision() != VocabularyDivision.PERSONAL) {
            throw new BadRequestByDivision();
        }

        vocabulary.removeWordList();

        for (WordDto createDto :
                wordDtoList) {
            Word.addWordToVocabulary(vocabulary, createDto);
        }
    }

    public void checkMemorise(Long wordId) {
        Word findWord = wordRepository.findById(wordId).orElseThrow(WordNotFoundException::new);
        Vocabulary vocabulary = findWord.getVocabulary();

        if (vocabulary.getDivision() != VocabularyDivision.PERSONAL) {
            if (vocabulary.getDivision() != VocabularyDivision.COPIED) {
                throw new BadRequestByDivision();
            }
        }

        findWord.checkMemorise();
    }

    @Transactional
    public void modifyPersonalVocabulary(Long vocabularyId, PersonalVocabularyUpdateDto updateDto) {
        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyId).orElseThrow(VocabularyNotFoundException::new);

        if (vocabulary.getDivision() != VocabularyDivision.PERSONAL) {
            throw new BadRequestByDivision();
        }

        vocabulary.modify(updateDto);
    }

    @Transactional
    public Vocabulary share(Long vocabularyId, Long sharedCategoryId) {
        Vocabulary personalVocabulary = vocabularyRepository.findById(vocabularyId).orElseThrow(VocabularyNotFoundException::new);
        Category sharedCategory = categoryRepository.findById(sharedCategoryId).orElseThrow(CategoryNotFoundException::new);
        Vocabulary sharedVocabulary = personalVocabulary.personalToShared(sharedCategory);

        if (personalVocabulary.getDivision() != VocabularyDivision.PERSONAL) {
            throw new BadRequestByDivision();
        }

        if (sharedCategory.getDivision().toString().equals(personalVocabulary.getDivision().toString())) {
            throw new DivisionMismatchException();
        }

        return vocabularyRepository.save(sharedVocabulary);
    }

    public void deletePersonalVocabulary(Long vocabularyId) {
        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyId).orElseThrow(VocabularyNotFoundException::new);
        vocabulary.delete();
    }

    /**
     * Common
     */

    @Transactional
    public void moveCategory(Long vocabularyId, Long newCategoryId) {
        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyId).orElseThrow(VocabularyNotFoundException::new);
        Category newCategory = categoryRepository.findById(newCategoryId).orElseThrow(CategoryNotFoundException::new);

        if (vocabulary.getCategory().getId().equals(newCategoryId)) {
            throw new DoNotMoveException();
        }

        if (vocabulary.getDivision() == VocabularyDivision.SHARED) {
            if (newCategory.getDivision() != CategoryDivision.SHARED) {
                throw new DivisionMismatchException("공유단어장은 공유카테고리로만 이동시킬 수 있습니다.");
            }
        } else if (vocabulary.getDivision() == VocabularyDivision.PERSONAL) {
            if (newCategory.getDivision() != CategoryDivision.PERSONAL) {
                throw new DivisionMismatchException("개인단어장은 개인카테고리로만 이동시킬 수 있습니다.");
            } else {
                if (!newCategory.getMember().getId().equals(vocabulary.getCategory().getMember().getId())) {
                    throw new MemberMismatchAfterMovingWithCurrentMemberException();
                }
            }
        } else if (vocabulary.getDivision() == VocabularyDivision.COPIED) {
            if (newCategory.getDivision() != CategoryDivision.PERSONAL) {
                throw new DivisionMismatchException("다운로드 한 단어장은 개인카테고리로만 이동시킬 수 있습니다.");
            }
        } else {
            throw new BadRequestByDivision("삭제되거나 공유가 해제된 단어장은 카테고리 이동이 불가능합니다.");
        }

        vocabulary.moveCategory(newCategory);
    }

    public Vocabulary getVocabulary(Long vocabularyId) {
        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyId).orElseThrow(VocabularyNotFoundException::new);
        if (vocabulary.getDivision() == VocabularyDivision.SHARED) {
            vocabulary.increaseViews();
        }

        return vocabulary;
    }

    public QueryResults<Vocabulary> getVocabularyListByMember(CriteriaDto criteriaDto, VocabularyDivision division, Long memberId, Long categoryId) {
        return vocabularyQueryRepository.findAllByPersonal(criteriaDto, division, memberId, categoryId);
    }

    /**
     * shared
     */

    public QueryResults<Vocabulary> getVocabularyListByShared(CriteriaDto criteriaDto, Long categoryId, String title, VocabularySortCondition sortCondition) {
        return vocabularyQueryRepository.findAllByShared(criteriaDto, categoryId, title, sortCondition);
    }

    public Vocabulary download(Long vocabularyId, Long memberId, Long categoryId) {
        Vocabulary sharedVocabulary = vocabularyRepository.findById(vocabularyId).orElseThrow(VocabularyNotFoundException::new);
        Category personalCategory = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        Vocabulary personalVocabulary = sharedVocabulary.sharedToPersonal(member, personalCategory);

        if (sharedVocabulary.getDivision() != VocabularyDivision.SHARED) {
            throw new BadRequestByDivision();
        }

        if (personalCategory.getDivision() != CategoryDivision.PERSONAL) {
            throw new BadRequestByDivision();
        }

        return vocabularyRepository.save(personalVocabulary);
    }

    @Transactional
    public void sharedVocabularyToUnshared(Long vocabularyId) {
        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyId).orElseThrow(VocabularyNotFoundException::new);

        if (vocabulary.getDivision() != VocabularyDivision.SHARED) {
            throw new BadRequestByDivision();
        }

        vocabulary.unshared();
    }

}
