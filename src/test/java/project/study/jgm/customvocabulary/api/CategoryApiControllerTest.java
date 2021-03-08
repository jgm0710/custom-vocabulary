package project.study.jgm.customvocabulary.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import project.study.jgm.customvocabulary.common.BaseControllerTest;
import project.study.jgm.customvocabulary.common.dto.MessageVo;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.dto.MemberCreateDto;
import project.study.jgm.customvocabulary.security.dto.OnlyTokenDto;
import project.study.jgm.customvocabulary.security.dto.TokenDto;
import project.study.jgm.customvocabulary.vocabulary.category.Category;
import project.study.jgm.customvocabulary.vocabulary.category.CategoryDivision;
import project.study.jgm.customvocabulary.vocabulary.category.dto.CategoryUpdateDto;
import project.study.jgm.customvocabulary.vocabulary.category.dto.CategoryCreateDto;
import project.study.jgm.customvocabulary.vocabulary.category.exception.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CategoryApiControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("개인용 카테고리 생성")
    public void addPersonalCategory() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        int orders = 1;
        String categoryName = "test category";
        CategoryCreateDto createDto = CategoryCreateDto.builder()
                .name(categoryName)
                .parentId(null)
                .orders(orders)
                .build();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/category")
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isCreated())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.name").value(categoryName))
                .andExpect(jsonPath("data.parentId").isEmpty())
                .andExpect(jsonPath("data.subCategoryList").isEmpty())
                .andExpect(jsonPath("data.vocabularyCount").value(0))
                .andExpect(jsonPath("data.orders").value(orders))
                .andExpect(redirectedUrl("http://localhost:8080/api/vocabulary/category/" + user1.getId()))
                .andExpect(jsonPath("message").value(MessageVo.ADD_PERSONAL_CATEGORY_SUCCESSFULLY))
        ;

    }

    @Test
    @DisplayName("카테고리 생성 시 입력값이 없는 경우")
    public void addPersonalCategory_Empty() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        int orders = 1;
        String categoryName = "test category";
        CategoryCreateDto createDto = CategoryCreateDto.builder()
//                .name(categoryName)
//                .parentId(null)
//                .orders(orders)
                .build();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/category")
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].code").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].field").exists())
        ;

    }

    @Test
    @DisplayName("개인용 자식 카테고리 생성")
    public void addPersonalChildCategory() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category parent = createCategory(user1, CategoryDivision.PERSONAL, "parent", null, 0);

        CategoryCreateDto createDto = CategoryCreateDto.builder()
                .name("test category")
                .parentId(parent.getId())
                .orders(1)
                .build();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/category")
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isCreated())
                .andExpect(redirectedUrl("http://localhost:8080/api/vocabulary/category/" + user1.getId()))
                .andExpect(jsonPath("message").value(MessageVo.ADD_PERSONAL_CATEGORY_SUCCESSFULLY))
                .andDo(document("add-personal-category",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type"),
                                headerWithName(X_AUTH_TOKEN).description(X_AUTH_TOKEN_DESCRIPTION)
                        ),
                        requestFields(
                                fieldWithPath("name").description("생성할 개인 카테고리의 이름"),
                                fieldWithPath("parentId").description("생성할 개인 카테고리의 부모 카테고리의 식별 ID"),
                                fieldWithPath("orders").description("생성할 개인 카테고리의 정렬 순서")
                        ),
                        responseFields(
                                fieldWithPath("data.id").description("생성된 개인 카테고리의 식별 ID"),
                                fieldWithPath("data.name").description("생성된 개인 카테고리의 이름"),
                                fieldWithPath("data.parentId").description("생성된 개인 카테고리의 부모 카테고리의 식별 ID"),
                                fieldWithPath("data.subCategoryList").description("생성된 개인 카테고리의 자식 카테고리 수"),
                                fieldWithPath("data.vocabularyCount").description("생성된 개인 카테고리에 등록된 단어장 수"),
                                fieldWithPath("data.orders").description("생성된 개인 카테고리의 정렬 순서"),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;

    }

    @Test
    @DisplayName("개인용 카테고리 생성 시 부모 카테고리가 지정 됐는데 부모 카테고리를 찾을 수 없는 경우")
    public void addPersonalCategory_ParentNotFound() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        CategoryCreateDto createDto = CategoryCreateDto.builder()
                .name("test category")
                .parentId(10000L)
                .orders(1)
                .build();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/category")
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new ParentNotFoundException(10000L).getMessage()))
        ;

    }

    @Test
    @DisplayName("개인용 카테고리 생성 시 해당 순서에 이미 카테고리가 존재하는 경우")
    public void addPersonalCategory_CategoryExistsInTheCorrespondingOrders() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        int orders = 1;
        Category parent = createCategory(user1, CategoryDivision.PERSONAL, "parent", null, orders);

        CategoryCreateDto createDto = CategoryCreateDto.builder()
                .name("test category")
                .parentId(null)
                .orders(orders)
                .build();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/category")
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new CategoryExistsInTheCorrespondingOrdersException(orders).getMessage()));

    }

    @Test
    @DisplayName("개인 카테고리 생성 시 부모 카테고리와 자식 카테고리의 구분이 다른 경우")
    public void addPersonalCategory_DivisionBetweenParentAndChildIsDifferent() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category parent = createCategory(user1, CategoryDivision.SHARED, "parent", null, 0);

        CategoryCreateDto createDto = CategoryCreateDto.builder()
                .name("test category")
                .parentId(parent.getId())
                .orders(1)
                .build();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/category")
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new DivisionBetweenParentAndChildIsDifferentException().getMessage()));

    }

    @Test
    @DisplayName("개인 카테고리 생성 시 부모 카테고리가 다른 회원의 카테고리인 경우")
    public void addPersonalCategory_ParentBelongToOtherMembers() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category parent = createCategory(user1, CategoryDivision.PERSONAL, "parent", null, 0);

        CategoryCreateDto createDto = CategoryCreateDto.builder()
                .name("test category")
                .parentId(parent.getId())
                .orders(1)
                .build();

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        Member user2 = memberService.userJoin(memberCreateDto1);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/category")
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(new ParentBelongToOtherMembersException().getMessage()));

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 개인용 카테고리를 생성하는 경우")
    public void addPersonalCategory_UnAuthentication() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        CategoryCreateDto createDto = CategoryCreateDto.builder()
                .name("test category")
                .parentId(null)
                .orders(1)
                .build();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/category")
//                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("개인 카테고리 목록 조회")
    @Transactional
    public void getCategoryListByMember() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);
        createCategoryList(user1, CategoryDivision.PERSONAL);

        em.flush();
        em.clear();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/vocabulary/category/" + user1.getId())
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data[0].id").exists())
                .andExpect(jsonPath("data[0].name").exists())
                .andExpect(jsonPath("data[0].parentId").isEmpty())
                .andExpect(jsonPath("data[0].subCategoryList").exists())
                .andExpect(jsonPath("data[0].vocabularyCount").exists())
                .andExpect(jsonPath("data[0].orders").exists())
                .andExpect(jsonPath("message").value(MessageVo.GET_PERSONAL_CATEGORY_LIST_SUCCESSFULLY))
                .andDo(document("get-personal-category-list",
                        requestHeaders(
                                headerWithName(X_AUTH_TOKEN).description(X_AUTH_TOKEN_DESCRIPTION)
                        ),
                        relaxedResponseFields(
                                fieldWithPath("data[0].id").description("개인 카테고리 목록 중 첫 번째 카테고리의 식별 ID"),
                                fieldWithPath("data[0].name").description("개인 카테고리 목록 중 첫 번째 카테고리의 이름"),
                                fieldWithPath("data[0].parentId").description("개인 카테고리 목록 중 첫 번째 카테고리의 부모 카테고리 식별 ID"),
                                fieldWithPath("data[0].subCategoryList[]").description("개인 카테고리 목록 중 첫 번째 카테고리의 자식 카테고리 목록"),
                                fieldWithPath("data[0].vocabularyCount").description("개인 카테고리 목록 중 첫 번째 카테고리에 포함된 단어장 개수"),
                                fieldWithPath("data[0].orders").description("개인 카테고리 목록 중 첫 번째 카테고리의 정렬 순서"),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;

    }

    @Test
    @DisplayName("USER가 다른 회원의 카테고리 목록을 조회")
    @Transactional
    public void getCategoryListByMember_Unauthorized() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);
        createCategoryList(user1, CategoryDivision.PERSONAL);

        em.flush();
        em.clear();

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        Member user2 = memberService.userJoin(memberCreateDto1);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/vocabulary/category/" + user1.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.GET_PERSONAL_CATEGORY_LIST_OF_DIFFERENT_MEMBER));

    }

    @Test
    @DisplayName("관리자가 다른 회원의 카테고리 목록을 조회")
    @Transactional
    public void getCategoryListByMember_By_Admin() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);
        createCategoryList(user1, CategoryDivision.PERSONAL);

        em.flush();
        em.clear();

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("adminMember", "adminMember");
        Member admin = memberService.adminJoin(memberCreateDto1);
        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(admin.getLoginInfo().getRefreshToken());
        TokenDto adminTokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/vocabulary/category/" + user1.getId())
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data[0].id").exists())
                .andExpect(jsonPath("data[0].name").exists())
                .andExpect(jsonPath("data[0].parentId").isEmpty())
                .andExpect(jsonPath("data[0].subCategoryList").exists())
                .andExpect(jsonPath("data[0].vocabularyCount").exists())
                .andExpect(jsonPath("data[0].orders").exists())
                .andExpect(jsonPath("message").value(MessageVo.GET_PERSONAL_CATEGORY_LIST_SUCCESSFULLY))
        ;

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 개인용 카테고리 목록 조회")
    @Transactional
    public void getCategoryListByMember_UnAuthentication() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);
        createCategoryList(user1, CategoryDivision.PERSONAL);

        em.flush();
        em.clear();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        get("/api/vocabulary/category/" + user1.getId())
//                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("개인용 카테고리 수정")
    @Transactional
    public void modifyPersonalCategory() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category sampleCategory = createCategory(user1, CategoryDivision.PERSONAL, "user1 category", null, 1);

        Category parent = createCategory(user1, CategoryDivision.PERSONAL, "parent category", null, 2);


        String updateName = "update category";
        int orders = 10;
        Long parentId = parent.getId();
        CategoryUpdateDto categoryUpdateDto = new CategoryUpdateDto(updateName, parentId, orders);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/vocabulary/category/" + sampleCategory.getId())
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(categoryUpdateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").value(sampleCategory.getId()))
                .andExpect(jsonPath("data.name").value(updateName))
                .andExpect(jsonPath("data.parentId").value(parentId))
                .andExpect(jsonPath("data.subCategoryList").isEmpty())
                .andExpect(jsonPath("data.vocabularyCount").value(sampleCategory.getVocabularyCount()))
                .andExpect(jsonPath("data.orders").value(orders))
                .andExpect(jsonPath("message").value(MessageVo.MODIFY_CATEGORY_SUCCESSFULLY))
                .andDo(document("modify-category",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type"),
                                headerWithName(X_AUTH_TOKEN).description(X_AUTH_TOKEN_DESCRIPTION)
                        ),
                        requestFields(
                                fieldWithPath("name").description("수정할 이름"),
                                fieldWithPath("parentId").description("이동할 부모 카테고리의 식별 ID"),
                                fieldWithPath("orders").description("이동할 순서")
                        ),
                        responseFields(
                                fieldWithPath("data.id").description("수정된 카테고리의 식별 ID"),
                                fieldWithPath("data.name").description("수정된 카테고리의 이름"),
                                fieldWithPath("data.parentId").description("수정된 카테고리의 부모 카테고리의 식별 ID"),
                                fieldWithPath("data.subCategoryList[]").description("수정된 카테고리에 포함된 하위 카테고리 목록"),
                                fieldWithPath("data.vocabularyCount").description("수정된 카테고리에 포함된 단어장 개수"),
                                fieldWithPath("data.orders").description("수정된 카테고리의 정렬 순서"),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;

        em.flush();
        em.clear();

        Category findCategory = categoryService.getCategory(sampleCategory.getId());
        assertEquals(findCategory.getName(), updateName);
        assertEquals(findCategory.getParent().getId(), parentId);
        assertEquals(findCategory.getOrders(), orders);


    }

    @Test
    @DisplayName("카테고리 수정 시 부모와 자식 카테고리간의 구분이 다른 경우")
    public void modifyCategory_DivisionBetweenParentAndChildIsDifferent() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        CategoryCreateDto createDto1 = CategoryCreateDto.builder()
                .name("category1")
                .parentId(null)
                .orders(1)
                .build();

        CategoryCreateDto sharedCategoryDto = CategoryCreateDto.builder()
                .name("sharedCategory")
                .parentId(null)
                .orders(2)
                .build();

        Category personalCategory = categoryService.addPersonalCategory(user1.getId(), createDto1);
        Category sharedCategory = categoryService.addSharedCategory(sharedCategoryDto);

        CategoryUpdateDto updateDto = CategoryUpdateDto.builder()
                .name("update category")
                .parentId(sharedCategory.getId())
                .orders(1)
                .build();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/vocabulary/category/" + personalCategory.getId())
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new DivisionBetweenParentAndChildIsDifferentException().getMessage()));

    }

    @Test
    @DisplayName("개인 카테고리 수정 시 부모 카테고리가 다른 회원의 카테고리인 경우")
    public void modifyCategory_ParentBelongToOtherMembers() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        CategoryCreateDto user1CategoryDto = CategoryCreateDto.builder()
                .name("user1 category")
                .parentId(null)
                .orders(1)
                .build();

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        Member user2 = memberService.userJoin(memberCreateDto1);

        CategoryCreateDto user2CategoryDto = CategoryCreateDto.builder()
                .name("user2 category")
                .parentId(null)
                .orders(1)
                .build();

        Category user1Category = categoryService.addPersonalCategory(user1.getId(), user1CategoryDto);
        Category user2Category = categoryService.addPersonalCategory(user2.getId(), user2CategoryDto);

        CategoryUpdateDto updateDto = CategoryUpdateDto.builder()
                .name("update category")
                .parentId(user1Category.getId())
                .orders(1)
                .build();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/vocabulary/category/" + user2Category.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(new ParentBelongToOtherMembersException().getMessage()));

    }

    @Test
    @DisplayName("카테고리 수정 시 입력값이 없는 경우")
    public void modifyCategory_Empty() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category sampleCategory = createCategory(user1, CategoryDivision.PERSONAL, "user1 category", null, 1);

        CategoryUpdateDto categoryUpdateDto = new CategoryUpdateDto();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/vocabulary/category/" + sampleCategory.getId())
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(categoryUpdateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].code").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].field").exists())
        ;

    }

    @Test
    @DisplayName("다른 회원의 카테고리를 수정하는 경우")
    public void modifyCategory_DifferentMember() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category sampleCategory = createCategory(user1, CategoryDivision.PERSONAL, "user1 category", null, 1);

        CategoryUpdateDto categoryUpdateDto = new CategoryUpdateDto("update category", null, 1);

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        Member user2 = memberService.userJoin(memberCreateDto1);
        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/vocabulary/category/" + sampleCategory.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(categoryUpdateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.MODIFY_CATEGORY_OF_DIFFERENT_MEMBER));

    }

    @Test
    @DisplayName("관리자가 다른 회원의 카테고리를 수정하는 경우")
    public void modifyCategory_DifferentMember_By_Admin() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category sampleCategory = createCategory(user1, CategoryDivision.PERSONAL, "user1 category", null, 1);

        CategoryUpdateDto categoryUpdateDto = new CategoryUpdateDto("update category", null, 1);

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("adminMember", "adminMember");
        Member admin = memberService.adminJoin(memberCreateDto1);
        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(admin.getLoginInfo().getRefreshToken());
        TokenDto adminTokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/vocabulary/category/" + sampleCategory.getId())
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(categoryUpdateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.MODIFY_CATEGORY_OF_DIFFERENT_MEMBER));

    }

    @Test
    @DisplayName("카테고리 수정 시 수정할 카테고리를 찾을 수 없는 경우")
    public void modifyCategory_NotFound() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category sampleCategory = createCategory(user1, CategoryDivision.PERSONAL, "user1 category", null, 1);

        CategoryUpdateDto categoryUpdateDto = new CategoryUpdateDto("update category", null, 1);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/vocabulary/category/" + 10000L)
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(categoryUpdateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new CategoryNotFoundException().getMessage()))
        ;

    }

    @Test
    @DisplayName("카테고리 수정 시 이동할 부모 카테고리를 찾을 수 없는 경우")
    public void modifyCategory_ParentNotFound() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category sampleCategory = createCategory(user1, CategoryDivision.PERSONAL, "user1 category", null, 1);

        CategoryUpdateDto categoryUpdateDto = new CategoryUpdateDto("update category", 10000L, 1);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/vocabulary/category/" + sampleCategory.getId())
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(categoryUpdateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new ParentNotFoundException(10000L).getMessage()));

    }

    @Test
    @DisplayName("카테고리 수정 시 이동할 순서에 이미 카테고리가 존재하는 경우")
    public void modifyCategory_ExistOrders() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category sampleCategory = createCategory(user1, CategoryDivision.PERSONAL, "user1 category", null, 1);
        Category sampleCategory2 = createCategory(user1, CategoryDivision.PERSONAL, "user1 category2", null, 2);

        int orders = 2;
        CategoryUpdateDto categoryUpdateDto = new CategoryUpdateDto("update category", null, orders);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/vocabulary/category/" + sampleCategory.getId())
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(categoryUpdateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new CategoryExistsInTheCorrespondingOrdersException(orders).getMessage()));

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 카테고리를 수정하는 경우")
    public void modifyCategory_UnAuthentication() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category sampleCategory = createCategory(user1, CategoryDivision.PERSONAL, "user1 category", null, 1);

        CategoryUpdateDto categoryUpdateDto = new CategoryUpdateDto("update category", null, 1);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/vocabulary/category/" + sampleCategory.getId())
//                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(categoryUpdateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("관리자가 공유 카테고리를 수정")
    public void modifySharedCategory() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("adminMember", "adminMember");
        Member admin = memberService.adminJoin(memberCreateDto);

        Category sampleCategory = createCategory(admin, CategoryDivision.SHARED, "shared category", null, 1);

        String updateName = "update category";
        int orders = 2;
        CategoryUpdateDto categoryUpdateDto = new CategoryUpdateDto(updateName, null, orders);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(admin.getLoginInfo().getRefreshToken());
        TokenDto adminTokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/vocabulary/category/" + sampleCategory.getId())
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(categoryUpdateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").value(sampleCategory.getId()))
                .andExpect(jsonPath("data.name").value(updateName))
                .andExpect(jsonPath("data.parentId").isEmpty())
                .andExpect(jsonPath("data.subCategoryList").isEmpty())
                .andExpect(jsonPath("data.vocabularyCount").value(sampleCategory.getVocabularyCount()))
                .andExpect(jsonPath("data.orders").value(orders))
                .andExpect(jsonPath("message").value(MessageVo.MODIFY_CATEGORY_SUCCESSFULLY));

    }

    @Test
    @DisplayName("일반 회원이 공유 카테고리를 수정하는 경우")
    public void modifySharedCategory_By_User() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("adminMember", "adminMember");
        Member admin = memberService.adminJoin(memberCreateDto);

        Category sampleCategory = createCategory(null, CategoryDivision.SHARED, "shared category", null, 1);

        CategoryUpdateDto categoryUpdateDto = new CategoryUpdateDto("update category", null, 2);

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto1);
        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        put("/api/vocabulary/category/" + sampleCategory.getId())
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(categoryUpdateDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.MODIFY_SHARED_CATEGORY_BY_USER));

    }

    @Test
    @DisplayName("카테고리 삭제")
    public void deleteCategory() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category user1Category = createCategory(user1, CategoryDivision.PERSONAL, "user1 category", null, 1);

        em.flush();
        em.clear();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        delete("/api/vocabulary/category/" + user1Category.getId())
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                )
                .andDo(print());


        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value(MessageVo.DELETE_CATEGORY_SUCCESSFULLY))
                .andDo(document("delete-category",
                        requestHeaders(
                                headerWithName(X_AUTH_TOKEN).description(X_AUTH_TOKEN_DESCRIPTION)
                        ),
                        responseFields(
                                fieldWithPath("data").description("카테고리 삭제는 별도의 data 를 출력하지 않습니다."),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;

        assertThrows(CategoryNotFoundException.class, () -> categoryService.getCategory(user1Category.getId()));
    }

    @Test
    @DisplayName("USER가 다른 회원의 카테고리를 삭제하는 경우")
    public void deleteCategory_Unauthorized() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category user1Category = createCategory(user1, CategoryDivision.PERSONAL, "user1 category", null, 1);

        em.flush();
        em.clear();

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user2", "user2");
        Member user2 = memberService.userJoin(memberCreateDto1);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user2.getLoginInfo().getRefreshToken());
        TokenDto user2TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        delete("/api/vocabulary/category/" + user1Category.getId())
                                .header(X_AUTH_TOKEN, user2TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.DELETE_CATEGORY_OF_DIFFERENT_MEMBER));

    }

    @Test
    @DisplayName("관리자가 다른 회원의 카테고리를 삭제하는 경우")
    public void deleteCategory_By_Admin() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category user1Category = createCategory(user1, CategoryDivision.PERSONAL, "user1 category", null, 1);

        em.flush();
        em.clear();

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("adminMember", "adminMember");
        Member admin = memberService.adminJoin(memberCreateDto1);

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(admin.getLoginInfo().getRefreshToken());
        TokenDto adminTokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        delete("/api/vocabulary/category/" + user1Category.getId())
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(MessageVo.DELETE_CATEGORY_OF_DIFFERENT_MEMBER))
        ;

    }

    @Test
    @DisplayName("카테고리 삭제 시 해당 카테고리를 찾을 수 없는 경우")
    public void deleteCategory_NotFound() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category user1Category = createCategory(user1, CategoryDivision.PERSONAL, "user1 category", null, 1);

        em.flush();
        em.clear();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        delete("/api/vocabulary/category/" + 10000L)
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new CategoryNotFoundException().getMessage()));

    }

    @Test
    @DisplayName("카테고리 삭제 시 자식 카테고리를 가지고 있는 카테고리인 경우")
    public void deleteCategory_Has_SubCategory() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category parent = createCategory(user1, CategoryDivision.PERSONAL, "parent category", null, 1);
        Category child1 = createCategory(user1, CategoryDivision.PERSONAL, "child1 category", parent, 1);
        Category child2 = createCategory(user1, CategoryDivision.PERSONAL, "child2 category", parent, 2);

        em.flush();
        em.clear();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        delete("/api/vocabulary/category/" + parent.getId())
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new ExistSubCategoryException().getMessage()));

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 카테고리를 삭제하는 경우")
    public void deleteCategory_UnAuthentication() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category user1Category = createCategory(user1, CategoryDivision.PERSONAL, "user1 category", null, 1);

        em.flush();
        em.clear();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        delete("/api/vocabulary/category/" + user1Category.getId())
//                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("관리자에 의해 공유 카테고리 생성")
    public void addSharedCategory_By_Admin() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("adminMember", "adminMember");
        Member admin = memberService.adminJoin(memberCreateDto);

        String categoryName = "sample category";
        int orders = 1;
        Long parentId = null;
        CategoryCreateDto createDto = CategoryCreateDto.builder()
                .name(categoryName)
                .orders(orders)
                .parentId(parentId)
                .build();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(admin.getLoginInfo().getRefreshToken());
        TokenDto adminTokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/category/shared")
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isCreated())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.name").value(categoryName))
                .andExpect(jsonPath("data.parentId").value(parentId))
                .andExpect(jsonPath("data.subCategoryList").isEmpty())
                .andExpect(jsonPath("data.vocabularyCount").value(0))
                .andExpect(jsonPath("data.orders").value(orders))
                .andExpect(jsonPath("message").value(MessageVo.ADD_SHARED_CATEGORY_BY_ADMIN_SUCCESSFULLY))
        ;

    }

    @Test
    @DisplayName("관리자에 의해 자식 공유 카테고리 생성")
    public void addSharedChildCategory_By_Admin() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("adminMember", "adminMember");
        Member admin = memberService.adminJoin(memberCreateDto);

        CategoryCreateDto parentCategoryDto = CategoryCreateDto.builder()
                .name("parent category")
                .orders(1)
                .parentId(null)
                .build();

        Category parent = categoryService.addSharedCategory(parentCategoryDto);

        String categoryName = "child category";
        int orders = 1;
        Long parentId = parent.getId();
        CategoryCreateDto childCategoryDto = CategoryCreateDto.builder()
                .name(categoryName)
                .orders(orders)
                .parentId(parentId)
                .build();


        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(admin.getLoginInfo().getRefreshToken());
        TokenDto adminTokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/category/shared")
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(childCategoryDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isCreated())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.name").value(categoryName))
                .andExpect(jsonPath("data.parentId").value(parentId))
                .andExpect(jsonPath("data.subCategoryList").isEmpty())
                .andExpect(jsonPath("data.vocabularyCount").value(0))
                .andExpect(jsonPath("data.orders").value(orders))
                .andExpect(jsonPath("message").value(MessageVo.ADD_SHARED_CATEGORY_BY_ADMIN_SUCCESSFULLY))
                .andDo(document("add-shared-category",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type"),
                                headerWithName(X_AUTH_TOKEN).description(X_AUTH_TOKEN_DESCRIPTION)
                        ),
                        requestFields(
                                fieldWithPath("name").description("생성할 공유 카테고리의 이름"),
                                fieldWithPath("parentId").description("생성할 공유 카테고리의 부모 카테고리의 식별 ID"),
                                fieldWithPath("orders").description("생성할 공유 카테고리의 정렬 순서")
                        ),
                        responseFields(
                                fieldWithPath("data.id").description("생성된 공유 카테고리의 식별 ID"),
                                fieldWithPath("data.name").description("생성된 공유 카테고리의 이름"),
                                fieldWithPath("data.parentId").description("생성된 공유 카테고리의 부모 카테고리의 식별 ID"),
                                fieldWithPath("data.subCategoryList").description("생성된 공유 카테고리가 가지고 있는 자식 카테고리 목록"),
                                fieldWithPath("data.vocabularyCount").description("생성된 공유 카테고리에 포함된 공유 단어장의 개수"),
                                fieldWithPath("data.orders").description("생성된 공유 카테고리의 정렬 순서"),
                                fieldWithPath("message").description(MESSAGE_DESCRIPTION)
                        )
                ))
        ;

        em.flush();
        em.clear();

        Category findParentCategory = categoryService.getCategory(parentId);
        assertFalse(findParentCategory.getChildren().isEmpty());

    }

    @Test
    @DisplayName("공유 카테고리 생성 시 입력값이 없는 경우")
    public void addSharedCategory_Empty() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("adminMember", "adminMember");
        Member admin = memberService.adminJoin(memberCreateDto);

        String categoryName = "sample category";
        int orders = 1;
        Long parentId = null;
        CategoryCreateDto createDto = CategoryCreateDto.builder()
//                .name(categoryName)
//                .orders(orders)
//                .parentId(parentId)
                .build();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(admin.getLoginInfo().getRefreshToken());
        TokenDto adminTokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/category/shared")
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].code").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].field").exists())
        ;

    }

    @Test
    @DisplayName("공유 카테고리 생성 시 순서가 0인 경우")
    public void addSharedCategory_OrdersIsZero() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("adminMember", "adminMember");
        Member admin = memberService.adminJoin(memberCreateDto);

        String categoryName = "sample category";
        int orders = 0;
        Long parentId = null;
        CategoryCreateDto createDto = CategoryCreateDto.builder()
                .name(categoryName)
                .orders(orders)
                .parentId(parentId)
                .build();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(admin.getLoginInfo().getRefreshToken());
        TokenDto adminTokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/category/shared")
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].code").exists())
                .andExpect(jsonPath("$[0].defaultMessage").value("지정할 수 있는 순서는 1보다 작을 수 없습니다."))
                .andExpect(jsonPath("$[0].field").exists())
        ;

    }

    @Test
    @DisplayName("인증되지 않은 사용자가 공유 카테고리를 생성")
    public void addSharedCategory_UnAuthentication() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("adminMember", "adminMember");
        Member admin = memberService.adminJoin(memberCreateDto);

        String categoryName = "sample category";
        int orders = 1;
        Long parentId = null;
        CategoryCreateDto createDto = CategoryCreateDto.builder()
                .name(categoryName)
                .orders(orders)
                .parentId(parentId)
                .build();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(admin.getLoginInfo().getRefreshToken());
        TokenDto adminTokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/category/shared")
//                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("USER 권한의 사용자가 공유 카테고리를 생성")
    public void addSharedCategory_By_User() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        String categoryName = "sample category";
        int orders = 1;
        Long parentId = null;
        CategoryCreateDto createDto = CategoryCreateDto.builder()
                .name(categoryName)
                .orders(orders)
                .parentId(parentId)
                .build();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(user1.getLoginInfo().getRefreshToken());
        TokenDto user1TokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/category/shared")
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("자식 공유 카테고리 생성 시 부모 카테고리를 찾을 수 없는 경우")
    public void addSharedCategory_ParentNotFound() throws Exception {
          //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("adminMember", "adminMember");
        Member admin = memberService.adminJoin(memberCreateDto);

        String categoryName = "sample category";
        int orders = 1;
        Long parentId = 100000L;
        CategoryCreateDto createDto = CategoryCreateDto.builder()
                .name(categoryName)
                .orders(orders)
                .parentId(parentId)
                .build();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(admin.getLoginInfo().getRefreshToken());
        TokenDto adminTokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/category/shared")
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto))
                )
                .andDo(print());


        //then
        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(new ParentNotFoundException(parentId).getMessage()));

    }

    @Test
    @DisplayName("공유 카테고리 생성 시 해당 순서에 카테고리가 존재하는 경우")
    public void addSharedCategory_CategoryExistsInTheCorrespondingOrdersException() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("adminMember", "adminMember");
        Member admin = memberService.adminJoin(memberCreateDto);

        CategoryCreateDto sampleCreateDto = CategoryCreateDto.builder()
                .name("sample Category")
                .orders(1)
                .parentId(null)
                .build();

        categoryService.addSharedCategory(sampleCreateDto);

        String categoryName = "sample category";
        int orders = 1;
        Long parentId = null;
        CategoryCreateDto createDto = CategoryCreateDto.builder()
                .name(categoryName)
                .orders(orders)
                .parentId(parentId)
                .build();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(admin.getLoginInfo().getRefreshToken());
        TokenDto adminTokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/category/shared")
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto))
                )
                .andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new CategoryExistsInTheCorrespondingOrdersException(orders).getMessage()));

    }

    @Test
    @DisplayName("공유 자식 카테고리 생성 시 부모 카테고리가 개인 카테고리일 경우")
    public void addSharedCategory_DivisionBetweenParentAndChildIsDifferentException() throws Exception {
          //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("adminMember", "adminMember");
        Member admin = memberService.adminJoin(memberCreateDto);

        MemberCreateDto memberCreateDto1 = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto1);
        CategoryCreateDto personalCreateDto = CategoryCreateDto.builder()
                .name("personal category")
                .orders(1)
                .parentId(null)
                .build();

        Category personalCategory = categoryService.addPersonalCategory(user1.getId(), personalCreateDto);

        String categoryName = "sample category";
        int orders = 1;
        Long parentId = personalCategory.getId();
        CategoryCreateDto createDto = CategoryCreateDto.builder()
                .name(categoryName)
                .orders(orders)
                .parentId(parentId)
                .build();

        OnlyTokenDto onlyTokenDto = new OnlyTokenDto(admin.getLoginInfo().getRefreshToken());
        TokenDto adminTokenDto = memberService.refresh(onlyTokenDto);

        //when
        ResultActions perform = mockMvc
                .perform(
                        post("/api/vocabulary/category/shared")
                                .header(X_AUTH_TOKEN, adminTokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto))
                )
                .andDo(print());


        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(new DivisionBetweenParentAndChildIsDifferentException().getMessage()))
        ;

    }

    @Test
    @DisplayName("공유 카테고리 목록 조회")
    public void getSharedCategoryList() throws Exception {
        //given
        createCategoryList(null, CategoryDivision.SHARED);
        em.flush();
        em.clear();
        //when

        //then
        mockMvc
                .perform(
                        get("/api/vocabulary/category/shared")
                )
                .andDo(print())
                .andExpect(status().isOk())
        ;

    }

}