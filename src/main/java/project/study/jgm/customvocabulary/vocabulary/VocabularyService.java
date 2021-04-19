package project.study.jgm.customvocabulary.vocabulary;

import com.querydsl.core.QueryResults;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
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
import project.study.jgm.customvocabulary.vocabulary.dto.VocabularyCreateDto;
import project.study.jgm.customvocabulary.vocabulary.dto.VocabularyUpdateDto;
import project.study.jgm.customvocabulary.vocabulary.exception.*;
import project.study.jgm.customvocabulary.vocabulary.upload.VocabularyThumbnailImageFile;
import project.study.jgm.customvocabulary.vocabulary.upload.VocabularyThumbnailImageFileRepository;
import project.study.jgm.customvocabulary.vocabulary.upload.exception.VocabularyThumbnailImageFileNotFoundException;
import project.study.jgm.customvocabulary.vocabulary.word.Word;
import project.study.jgm.customvocabulary.vocabulary.word.WordRepository;
import project.study.jgm.customvocabulary.vocabulary.word.dto.WordRequestDto;
import project.study.jgm.customvocabulary.vocabulary.word.upload.WordImageFile;
import project.study.jgm.customvocabulary.vocabulary.word.upload.WordImageFileRepository;
import project.study.jgm.customvocabulary.vocabulary.word.upload.exception.WordImageFileNotFoundException;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;

    private final VocabularyQueryRepository vocabularyQueryRepository;

    private final MemberRepository memberRepository;

    private final CategoryRepository categoryRepository;

    private final WordRepository wordRepository;

    private final WordImageFileRepository wordImageFileRepository;

    private final VocabularyThumbnailImageFileRepository vocabularyThumbnailImageFileRepository;

    private final ModelMapper modelMapper;

    private final EntityManager em;


    /**
     * personal
     */

    @Transactional
    public Vocabulary addPersonalVocabulary(Long memberId, Long categoryId, VocabularyCreateDto createDto) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);

        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
        }

        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = null;

        if (createDto.getImageFileId() != null) {
            vocabularyThumbnailImageFile = vocabularyThumbnailImageFileRepository
                    .findById(createDto.getImageFileId()).orElseThrow(VocabularyThumbnailImageFileNotFoundException::new);
        }

        Vocabulary personalVocabulary = Vocabulary.createPersonalVocabulary(member, category, createDto, vocabularyThumbnailImageFile);

        if (category != null) {
            if (!category.getDivision().toString().equals(personalVocabulary.getDivision().toString())) {
                throw new DivisionMismatchException();
            }
        }

        vocabularyRepository.save(personalVocabulary);

        return personalVocabulary;

    }

//    @Transactional
//    public void addWordListToPersonalVocabulary(Long vocabularyId, List<WordRequestDto> wordRequestDtoList) {
//        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyId).orElseThrow(VocabularyNotFoundException::new);
//
//        if (vocabulary.getDivision() != VocabularyDivision.PERSONAL) {
//            throw new BadRequestByDivision("개인 단어장 이외에는 단어 목록을 추가할 수 없습니다.");
//        }
//
//        List<Word> wordList = new ArrayList<>();
//        for (WordRequestDto wordRequestDto :
//                wordRequestDtoList) {
//            wordRequestDto.setMemorisedCheck(false);
//            WordImageFile wordImageFile = getWordImageFile(wordRequestDto);
//            Word word = Word.createWord(wordRequestDto, wordImageFile);
//            wordList.add(word);
//        }
//
//        vocabulary.addWordList(wordList);
//
//    }

    @Transactional
    public void updateWordListOfPersonalVocabulary(Long vocabularyId, List<WordRequestDto> wordRequestDtoList) {
        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyId).orElseThrow(VocabularyNotFoundException::new);

        if (vocabulary.getDivision() != VocabularyDivision.PERSONAL) {
            throw new BadRequestByDivision("개인 단어장 이외에는 단어 목록을 수정할 수 없습니다. 다운로드 받은 단어장, 삭제된 단어장 또한 단어 목록 수정이 불가능합니다.");
        }

        vocabulary.removeWordList();

        List<Word> wordList = new ArrayList<>();
        for (WordRequestDto wordRequestDto :
                wordRequestDtoList) {
            WordImageFile wordImageFile = getWordImageFile(wordRequestDto);
            Word word = Word.createWord(wordRequestDto, wordImageFile);
            wordList.add(word);
        }

        vocabulary.addWordList(wordList);

        em.flush();
    }

    public void checkMemorise(Long wordId) {
        Word findWord = wordRepository.findById(wordId).orElseThrow(WordNotFoundException::new);
        Vocabulary vocabulary = findWord.getVocabulary();

        if (vocabulary.getDivision() != VocabularyDivision.PERSONAL) {
            if (vocabulary.getDivision() != VocabularyDivision.COPIED) {
                throw new BadRequestByDivision("개인 단어장 이외에는 암기 체크를 할 수 없습니다. 삭제된 단어장 또한 암기 체크를 할 수 없습니다.");
            }
        }

        findWord.checkMemorise();
    }

    public Word getWord(Long wordId) {
        return wordRepository.findById(wordId).orElseThrow(WordNotFoundException::new);
    }

    @Transactional
    public void modifyPersonalVocabulary(Long personalVocabularyId, VocabularyUpdateDto updateDto) {
        Vocabulary vocabulary = vocabularyRepository.findById(personalVocabularyId).orElseThrow(VocabularyNotFoundException::new);
        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyThumbnailImageFileRepository
                .findById(updateDto.getImageFileId()).orElseThrow(VocabularyThumbnailImageFileNotFoundException::new);

        if (vocabulary.getDivision() != VocabularyDivision.PERSONAL) {
            throw new BadRequestByDivision("개인 단어장 외에는 수정할 수 없습니다. 다운로드 받은 단어장, 삭제된 단어장 또한 수정할 수 없습니다.");
        }

        vocabulary.modify(updateDto, vocabularyThumbnailImageFile);
    }

    @Transactional
    public Vocabulary share(Long personalVocabularyId, Long sharedCategoryId) {
        Vocabulary personalVocabulary = vocabularyRepository.findById(personalVocabularyId).orElseThrow(VocabularyNotFoundException::new);
        Category sharedCategory = null;
        if (sharedCategoryId != null) {
            sharedCategory = categoryRepository.findById(sharedCategoryId).orElseThrow(CategoryNotFoundException::new);
        }
        Vocabulary sharedVocabulary = personalVocabulary.personalToShared(sharedCategory);

        if (personalVocabulary.getDivision() != VocabularyDivision.PERSONAL) {
            throw new BadRequestByDivision("자신이 생성한 단어장만 공유할 수 있습니다. 삭제된 단어장, 다운로드 받은 단어장, 이미 공유된 단어장은 공유할 수 없습니다.");
        }

        if (sharedCategory != null) {
            if (sharedCategory.getDivision() != CategoryDivision.SHARED) {
                throw new BadRequestByDivision("단어를 공유할 카테고리가 공유 카테고리가 아닙니다.");
            }
        }

        Vocabulary savedVocabulary = vocabularyRepository.save(sharedVocabulary);

        return savedVocabulary;
    }

    public void deletePersonalVocabulary(Long vocabularyId) {
        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyId).orElseThrow(VocabularyNotFoundException::new);

        if (vocabulary.getDivision() != VocabularyDivision.PERSONAL && vocabulary.getDivision() != VocabularyDivision.COPIED) {
            throw new BadRequestByDivision("개인 단어장에 속한 단어장이 아니면 삭제가 불가능합니다.");
        }

        vocabulary.delete();
    }

    /**
     * Common
     */

    @Transactional
    public void moveCategory(Long vocabularyId, Long newCategoryId) {
        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyId).orElseThrow(VocabularyNotFoundException::new);

        Category newCategory = null;
        if (newCategoryId != null) {
            newCategory = categoryRepository.findById(newCategoryId).orElseThrow(CategoryNotFoundException::new);
            if (vocabulary.getCategory() != null) {
                if (vocabulary.getCategory().getId().equals(newCategoryId)) {
                    throw new DoNotMoveException();
                }
            }
        }

        if (newCategory != null) {
            if (vocabulary.getDivision() == VocabularyDivision.SHARED) {
                if (newCategory.getDivision() != CategoryDivision.SHARED) {
                    throw new DivisionMismatchException("공유단어장은 공유카테고리로만 이동시킬 수 있습니다.");
                }
            } else if (vocabulary.getDivision() == VocabularyDivision.PERSONAL || vocabulary.getDivision() == VocabularyDivision.COPIED) {
                if (newCategory.getDivision() != CategoryDivision.PERSONAL) {
                    throw new DivisionMismatchException("자신이 생성하거나 다운로드 받은 단어장은 개인카테고리로만 이동시킬 수 있습니다.");
                } else {
                    if (!newCategory.getMember().getId().equals(vocabulary.getProprietor().getId())) {
                        throw new MemberMismatchAfterMovingWithCurrentMemberException();
                    }
                }
            }
        }

        if (vocabulary.getDivision() == VocabularyDivision.DELETE || vocabulary.getDivision() == VocabularyDivision.UNSHARED) {
            throw new BadRequestByDivision("삭제되거나 공유가 해제된 단어장은 카테고리 이동이 불가능합니다.");
        }

        vocabulary.moveCategory(newCategory);
    }

    public Vocabulary getVocabulary(Long vocabularyId) {
        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyId).orElseThrow(VocabularyNotFoundException::new);

        if (vocabulary.getDivision() == VocabularyDivision.SHARED) {
            vocabulary.increaseViews();
        }

//        for (int i = 0; i < vocabulary.getWordList().size(); i++) {
//            vocabulary.getWordList().get(i);
//        }

        return vocabulary;
    }

    public QueryResults<Vocabulary> getVocabularyListByMember(CriteriaDto criteria, Long memberId, VocabularySearchBy searchBy, Long categoryId, VocabularyDivision... divisions) {
        if (categoryId != null) {
            final Category findCategory = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
            if (findCategory.getDivision() != CategoryDivision.PERSONAL) {
                throw new BadRequestByDivision("개인 카테고리의 단어 목록만 조회할 수 있습니다.");
            }
            if (!findCategory.getMember().getId().equals(memberId)) {
                throw new MemberAndCategoryMemberDifferentException();
            }
        }
        return vocabularyQueryRepository.findAllByMember(criteria, memberId, searchBy, categoryId, divisions);
    }

    /**
     * shared
     */

    public QueryResults<Vocabulary> getVocabularyListByShared(CriteriaDto criteria, VocabularySearchBy searchBy, Long categoryId, String title, VocabularySortCondition sortCondition) {
        return vocabularyQueryRepository.findAllByShared(criteria, searchBy, categoryId, title, sortCondition);
    }

    public Vocabulary download(Long vocabularyId, Long memberId, Long categoryId) {
        Vocabulary sharedVocabulary = vocabularyRepository.findById(vocabularyId).orElseThrow(VocabularyNotFoundException::new);
        Category personalCategory = null;
        if (categoryId != null) {
            personalCategory = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
        }
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        Vocabulary personalVocabulary = sharedVocabulary.sharedToPersonal(member, personalCategory);

        if (sharedVocabulary.getDivision() != VocabularyDivision.SHARED) {
            throw new BadRequestByDivision("공유 단어장이 아닐 경우 다운로드가 불가능합니다.");
        }

        if (personalCategory != null) {
            if (personalCategory.getDivision() != CategoryDivision.PERSONAL) {
                throw new BadRequestByDivision("공유 카테고리에는 공유 단어장을 다운로드 할 수 없습니다. 카테고리를 다시 확인해주세요.");
            }

            if (!personalCategory.getMember().getId().equals(memberId)) {
                throw new MemberAndCategoryMemberDifferentException();
            }
        }

        return vocabularyRepository.save(personalVocabulary);
    }

    @Transactional
    public void unshared(Long sharedVocabularyId) {
        Vocabulary vocabulary = vocabularyRepository.findById(sharedVocabularyId).orElseThrow(VocabularyNotFoundException::new);

        if (vocabulary.getDivision() != VocabularyDivision.SHARED) {
            throw new BadRequestByDivision("공유 단어장 외에는 공유를 취소할 수 없습니다.");
        }

        vocabulary.unshared();
    }


    private WordImageFile getWordImageFile(WordRequestDto wordRequestDto) {
        if (wordRequestDto.getImageFileId() != null) {
            return wordImageFileRepository
                    .findById(wordRequestDto.getImageFileId())
                    .orElseThrow(WordImageFileNotFoundException::new);
        } else {
            return null;
        }
    }
}
