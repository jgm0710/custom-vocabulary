package project.study.jgm.customvocabulary.vocabulary;

import com.querydsl.core.QueryResults;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import project.study.jgm.customvocabulary.common.BaseServiceTest;
import project.study.jgm.customvocabulary.common.dto.CriteriaDto;
import project.study.jgm.customvocabulary.members.Gender;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.dto.MemberCreateDto;
import project.study.jgm.customvocabulary.vocabulary.category.Category;
import project.study.jgm.customvocabulary.vocabulary.category.CategoryDivision;
import project.study.jgm.customvocabulary.vocabulary.category.CategoryStatus;
import project.study.jgm.customvocabulary.vocabulary.category.dto.CategoryCreateDto;
import project.study.jgm.customvocabulary.vocabulary.dto.PersonalVocabularyCreateDto;
import project.study.jgm.customvocabulary.vocabulary.dto.PersonalVocabularyUpdateDto;
import project.study.jgm.customvocabulary.vocabulary.upload.VocabularyThumbnailImageFile;
import project.study.jgm.customvocabulary.vocabulary.word.LanguageType;
import project.study.jgm.customvocabulary.vocabulary.word.Word;
import project.study.jgm.customvocabulary.vocabulary.word.dto.WordRequestDto;
import project.study.jgm.customvocabulary.vocabulary.word.upload.WordImageFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VocabularyServiceTest extends BaseServiceTest {

    private final String testImageFilePath = "/static/test/사진1.jpg";

    @Test
    @DisplayName("개인 단어장 생성")
    public void createPersonalVocabulary() throws Exception {
        //given
        Member userMember = createUserMember("user1", "user1");
        Category category = createCategory(userMember, CategoryDivision.PERSONAL, "test category", null, 0, CategoryStatus.REGISTER);

        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);
        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        String title = "test vocabulary";
        int difficulty = 1;
        LanguageType mainLanguage = LanguageType.KOREAN;
        LanguageType subLanguage = LanguageType.ENGLISH;
        Long imageFileId = vocabularyThumbnailImageFile.getId();

        PersonalVocabularyCreateDto createDto = PersonalVocabularyCreateDto.builder()
                .title(title)
                .difficulty(difficulty)
                .mainLanguage(mainLanguage)
                .subLanguage(subLanguage)
                .imageFileId(imageFileId)
                .build();


        //when
        Vocabulary personalVocabulary = vocabularyService.createPersonalVocabulary(userMember.getId(), category.getId(), createDto);

        //then
        assertEquals(title, personalVocabulary.getTitle());
        assertEquals(difficulty, personalVocabulary.getDifficulty());
        assertEquals(mainLanguage, personalVocabulary.getMainLanguage());
        assertEquals(subLanguage, personalVocabulary.getSubLanguage());
        assertEquals(imageFileId, personalVocabulary.getVocabularyThumbnailImageFile().getId());
        assertNotNull(personalVocabulary.getVocabularyThumbnailImageFile().getFileName());
        assertNotNull(personalVocabulary.getVocabularyThumbnailImageFile().getFileDownloadUri());
        assertNotNull(personalVocabulary.getVocabularyThumbnailImageFile().getFileStoredPath());
        assertNotNull(personalVocabulary.getVocabularyThumbnailImageFile().getFileType());


        System.out.println("personalVocabulary.toString() = " + personalVocabulary.toString());
    }

    @Test
    @DisplayName("개인 단어장에 단어 목록 추가")
    public void addWordListToPersonalVocabulary() throws Exception {
        //given
        Member userMember = createUserMember("user1", "user1");
        Category category = createCategory(userMember, CategoryDivision.PERSONAL, "test category", null, 0, CategoryStatus.REGISTER);

        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);
        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        String title = "test vocabulary";
        int difficulty = 1;
        LanguageType mainLanguage = LanguageType.KOREAN;
        LanguageType subLanguage = LanguageType.ENGLISH;
        Long imageFileId = vocabularyThumbnailImageFile.getId();

        PersonalVocabularyCreateDto createDto = PersonalVocabularyCreateDto.builder()
                .title(title)
                .difficulty(difficulty)
                .mainLanguage(mainLanguage)
                .subLanguage(subLanguage)
                .imageFileId(imageFileId)
                .build();

        Vocabulary personalVocabulary = vocabularyService.createPersonalVocabulary(userMember.getId(), category.getId(), createDto);

        List<WordRequestDto> wordRequestDtoList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            WordImageFile wordImageFile = wordFileStorageService.uploadWordImageFile(mockMultipartFile);
            WordRequestDto wordRequestDto = WordRequestDto.builder()
                    .imageFileId(wordImageFile.getId())
                    .mainWord("한국어" + i)
                    .subWord("English" + i)
                    .build();
            wordRequestDtoList.add(wordRequestDto);
        }

        //when
        vocabularyService.addWordListToPersonalVocabulary(personalVocabulary.getId(), wordRequestDtoList);

        em.flush();
        em.clear();

        //then
        Vocabulary findVocabulary = vocabularyService.getVocabulary(personalVocabulary.getId());
        for (Word word : findVocabulary.getWordList()) {
            System.out.println("word = " + word);
        }

    }

    @Test
    @DisplayName("개인 단어장 단어 목록 수정")
    @Transactional(readOnly = true)
    public void updateWordListToPersonalVocabulary() throws Exception {
        //given
        Member user1 = createUser1ByService();

        Category category = createPersonalCategoryByService(user1);

        Vocabulary personalVocabulary = createVocabularyByService(user1, category);

        List<WordRequestDto> wordRequestDtoList1 = getWordRequestDtos1();


        vocabularyService.addWordListToPersonalVocabulary(personalVocabulary.getId(), wordRequestDtoList1);

        Vocabulary tmpFindVocabulary = vocabularyService.getVocabulary(personalVocabulary.getId());
        System.out.println("tmpFindVocabulary = " + tmpFindVocabulary);


        List<WordRequestDto> wordRequestDtoList2 = getWordRequestDtos2();


        //when
        vocabularyService.updateWordListToPersonalVocabulary(personalVocabulary.getId(), wordRequestDtoList2);

        //then
        Vocabulary findVocabulary = vocabularyService.getVocabulary(personalVocabulary.getId());

//        for (Word word : findVocabulary.getWordList()) {
//            boolean noneMatch = wordRequestDtoList1.stream().noneMatch(wordRequestDto ->
//                    wordRequestDto.getMainWord().equals(word.getMainWord()) &&
//                            wordRequestDto.getSubWord().equals(word.getSubWord()) &&
//                            wordRequestDto.getImageFileId().equals(word.getId()));
//
//            boolean anyMatch = wordRequestDtoList2.stream().anyMatch(
//                    wordRequestDto ->
//                            wordRequestDto.getMainWord().equals(word.getMainWord()) &&
//                                    wordRequestDto.getSubWord().equals(word.getSubWord()) &&
//                                    wordRequestDto.getImageFileId().equals(word.getId()));
//
//            assertTrue(anyMatch);
//            assertTrue(noneMatch);
//        }

        System.out.println("findVocabulary = " + findVocabulary);

    }

    private List<WordRequestDto> getWordRequestDtos2() throws IOException {
        List<WordRequestDto> wordRequestDtoList2 = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);
            WordImageFile wordImageFile = wordFileStorageService.uploadWordImageFile(mockMultipartFile);

            WordRequestDto wordRequestDto = WordRequestDto.builder()
                    .imageFileId(wordImageFile.getId())
                    .mainWord("main"+i*2)
                    .subWord("sub"+i*2)
                    .build();
            wordRequestDtoList2.add(wordRequestDto);
        }
        return wordRequestDtoList2;
    }

    private List<WordRequestDto> getWordRequestDtos1() throws IOException {
        List<WordRequestDto> wordRequestDtoList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);
            WordImageFile wordImageFile = wordFileStorageService.uploadWordImageFile(mockMultipartFile);

            WordRequestDto wordRequestDto = WordRequestDto.builder()
                    .imageFileId(wordImageFile.getId())
                    .mainWord("main"+i)
                    .subWord("sub"+i)
                    .build();
            wordRequestDtoList.add(wordRequestDto);
        }
        return wordRequestDtoList;
    }

    private Vocabulary createVocabularyByService(Member user1, Category category, String title) {
        int difficulty = 1;
        LanguageType mainLanguage = LanguageType.KOREAN;
        LanguageType subLanguage = LanguageType.ENGLISH;

        PersonalVocabularyCreateDto vocabularyCreateDto = PersonalVocabularyCreateDto.builder()
                .title(title)
                .difficulty(difficulty)
                .mainLanguage(mainLanguage)
                .subLanguage(subLanguage)
                .build();

        Vocabulary personalVocabulary = vocabularyService.createPersonalVocabulary(user1.getId(), category.getId(), vocabularyCreateDto);
        return personalVocabulary;
    }

    private Vocabulary createVocabularyByService(Member user1, Category category) {
        String title = "test vocabulary";
        int difficulty = 1;
        LanguageType mainLanguage = LanguageType.KOREAN;
        LanguageType subLanguage = LanguageType.ENGLISH;

        PersonalVocabularyCreateDto vocabularyCreateDto = PersonalVocabularyCreateDto.builder()
                .title(title)
                .difficulty(difficulty)
                .mainLanguage(mainLanguage)
                .subLanguage(subLanguage)
                .build();

        Vocabulary personalVocabulary = vocabularyService.createPersonalVocabulary(user1.getId(), category.getId(), vocabularyCreateDto);
        return personalVocabulary;
    }

    private Category createPersonalCategoryByService(Member user1, int orders, String name) {
        CategoryCreateDto categoryCreateDto = CategoryCreateDto.builder()
                .name(name)
                .parentId(null)
                .orders(orders)
                .build();

        Category category = categoryService.addPersonalCategory(user1.getId(), categoryCreateDto);
        return category;
    }

    private Category createPersonalCategoryByService(Member user1) {
        int orders = 1;
        String test_category = "test category";
        CategoryCreateDto categoryCreateDto = CategoryCreateDto.builder()
                .name(test_category)
                .parentId(null)
                .orders(orders)
                .build();

        Category category = categoryService.addPersonalCategory(user1.getId(), categoryCreateDto);
        return category;
    }

    private Member createUser1ByService() {
        MemberCreateDto memberCreateDto = MemberCreateDto.builder()
                .joinId("user1")
                .email("user1@email.com")
                .password("user1")
                .name("user1")
                .nickname("user1")
                .dateOfBirth(LocalDate.now())
                .gender(Gender.MALE)
                .simpleAddress("simple")
                .build();

        Member user1 = memberService.userJoin(memberCreateDto);
        return user1;
    }

    @Test
    @DisplayName("암기 체크")
    public void checkMemorise() throws Exception {
        //given
        Member user1 = createUser1ByService();
        Category category = createPersonalCategoryByService(user1);
        Vocabulary vocabulary = createVocabularyByService(user1, category);

        List<WordRequestDto> wordRequestDtos1 = getWordRequestDtos1();
        vocabularyService.addWordListToPersonalVocabulary(vocabulary.getId(), wordRequestDtos1);

        em.flush();
        em.clear();

        Vocabulary tmpFindVocabulary = vocabularyService.getVocabulary(vocabulary.getId());

        Word word = tmpFindVocabulary.getWordList().get(0);

        //when
        vocabularyService.checkMemorise(word.getId());


        //then
        Vocabulary findVocabulary = vocabularyService.getVocabulary(vocabulary.getId());

        Word findWord = findVocabulary.getWordList().stream().filter(word1 -> word1.getId().equals(word.getId())).findFirst().get();
        assertEquals(findWord.isMemorisedCheck(), true);

        System.out.println("findVocabulary = " + findVocabulary);

    }

    @Test
    @DisplayName("개인 단어장 수정")
    public void modifyPersonalVocabulary() throws Exception {
        //given
        Member user1 = createUser1ByService();
        Category category = createPersonalCategoryByService(user1);
        Vocabulary vocabulary = createVocabularyByService(user1, category);

        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);
        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        String updateTitle = "update vocabulary";
        int updateDifficulty = 5;
        Long imageFileId = vocabularyThumbnailImageFile.getId();
        PersonalVocabularyUpdateDto vocabularyUpdateDto = PersonalVocabularyUpdateDto.builder()
                .title(updateTitle)
                .difficulty(updateDifficulty)
                .imageFileId(imageFileId)
                .build();

        //when
        vocabularyService.modifyPersonalVocabulary(vocabulary.getId(), vocabularyUpdateDto);

        //then
        em.flush();
        em.clear();

        Vocabulary findVocabulary = vocabularyService.getVocabulary(vocabulary.getId());

        assertEquals(updateTitle, findVocabulary.getTitle());
        assertEquals(updateDifficulty, findVocabulary.getDifficulty());
        assertEquals(imageFileId, findVocabulary.getVocabularyThumbnailImageFile().getId());

    }

    @Test
    @DisplayName("개인 단어장 공유 단어장으로 등록")
    public void share() throws Exception {
        //given
        Member user1 = createUser1ByService();
        Category personalCategory = createPersonalCategoryByService(user1);

        MockMultipartFile mockMultipartFile = getMockMultipartFile("file", testImageFilePath);
        VocabularyThumbnailImageFile vocabularyThumbnailImageFile = vocabularyFileStorageService.uploadVocabularyThumbnailImageFile(mockMultipartFile);

        String title = "test vocabulary";
        int difficulty = 1;
        LanguageType mainLanguage = LanguageType.KOREAN;
        LanguageType subLanguage = LanguageType.ENGLISH;
        Long imageFileId = vocabularyThumbnailImageFile.getId();

        PersonalVocabularyCreateDto vocabularyCreateDto = PersonalVocabularyCreateDto.builder()
                .title(title)
                .difficulty(difficulty)
                .mainLanguage(mainLanguage)
                .subLanguage(subLanguage)
                .imageFileId(imageFileId)
                .build();

        Vocabulary personalVocabulary = vocabularyService.createPersonalVocabulary(user1.getId(), personalCategory.getId(), vocabularyCreateDto);


        Category sharedCategory = createSharedCategoryByService();



        //when
        Vocabulary sharedVocabulary = vocabularyService.share(personalVocabulary.getId(), sharedCategory.getId());

        //then
        assertNotEquals(personalVocabulary.getId(), sharedVocabulary.getId());
        assertEquals(VocabularyDivision.SHARED, sharedVocabulary.getDivision());
        assertEquals(personalVocabulary.getMember().getId(), sharedVocabulary.getMember().getId());
        assertEquals(personalVocabulary.getTitle(), sharedVocabulary.getTitle());
        assertEquals(sharedCategory.getId(), sharedVocabulary.getCategory().getId());
        assertEquals(vocabularyThumbnailImageFile.getId(), sharedVocabulary.getVocabularyThumbnailImageFile().getId());
        assertEquals(vocabularyThumbnailImageFile.getFileName(), sharedVocabulary.getVocabularyThumbnailImageFile().getFileName());
        assertEquals(vocabularyThumbnailImageFile.getFileDownloadUri(), sharedVocabulary.getVocabularyThumbnailImageFile().getFileDownloadUri());


    }

    @Test
    @DisplayName("개인 단어장 삭제")
    public void deletePersonalVocabulary() throws Exception {
        //given
        Member user1 = createUser1ByService();
        Category category = createPersonalCategoryByService(user1);
        Vocabulary vocabulary = createVocabularyByService(user1, category);

        //when
        vocabularyService.deletePersonalVocabulary(vocabulary.getId());

        //then
        em.flush();
        em.clear();

        Vocabulary findVocabulary = vocabularyService.getVocabulary(vocabulary.getId());
        assertEquals(VocabularyDivision.DELETE, findVocabulary.getDivision());

    }

    @Test
    @DisplayName("단어장 카테고리 이동")
    public void moveCategory() throws Exception {
        //given
        Member user1 = createUser1ByService();
        Category category1 = createPersonalCategoryByService(user1, 1, "category 1");
        Category category2 = createPersonalCategoryByService(user1, 2, "category 2");
        Vocabulary vocabulary = createVocabularyByService(user1, category1);

        //when
        vocabularyService.moveCategory(vocabulary.getId(), category2.getId());

        //then
        em.flush();
        em.clear();

        Vocabulary findVocabulary = vocabularyService.getVocabulary(vocabulary.getId());

        assertEquals(category2.getId(), findVocabulary.getCategory().getId());

    }

    @Test
    @DisplayName("단어장 조회 테스트")
    public void getVocabulary() throws Exception {
        //given
        Member user1 = createUser1ByService();
        Category category = createPersonalCategoryByService(user1);
        Vocabulary vocabulary = createVocabularyByService(user1, category);

        //when
        Vocabulary findVocabulary = vocabularyService.getVocabulary(vocabulary.getId());

        //then
        assertEquals(vocabulary.getId(), findVocabulary.getId());
        assertEquals(vocabulary.getTitle(), findVocabulary.getTitle());
        assertEquals(vocabulary.getDivision(), findVocabulary.getDivision());
    }

    @Test
    @DisplayName("회원의 개인 단어장 목록 중 특정 카테고리에 소속된 단어장 목록 조회")
    public void getVocabularyListByMember_ByCategory() throws Exception {
        //given
        Member user1 = createUser1ByService();
        Category category1 = createPersonalCategoryByService(user1, 1, "category1");
        Category category2 = createPersonalCategoryByService(user1, 2, "category2");

        for (int i = 0; i < 10; i++) {
            createVocabularyByService(user1, category1, "category1 vocabulary" + i);
        }

        for (int i = 0; i < 10; i++) {
            createVocabularyByService(user1, category2, "category2 vocabulary" + i);
        }

        //when
        QueryResults<Vocabulary> results = vocabularyService.getVocabularyListByMember(new CriteriaDto(), VocabularyDivision.PERSONAL, user1.getId(), category1.getId());
        List<Vocabulary> findVocabularyList = results.getResults();


        //then
        findVocabularyList.forEach(vocabulary -> {
            System.out.println("vocabulary.getMember().getNickname() = " + vocabulary.getMember().getNickname());
            System.out.println("vocabulary.getCategory().getName() = " + vocabulary.getCategory().getName());
            System.out.println("vocabulary.getTitle() = " + vocabulary.getTitle());
        });

    }


    private Category createSharedCategoryByService() {
        CategoryCreateDto categoryCreateDto = CategoryCreateDto.builder()
                .name("shared category")
                .parentId(null)
                .orders(1)
                .build();
        Category sharedCategory = categoryService.addSharedCategory(categoryCreateDto);
        return sharedCategory;
    }

}