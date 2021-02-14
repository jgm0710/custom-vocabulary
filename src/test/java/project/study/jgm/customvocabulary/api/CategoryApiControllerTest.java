package project.study.jgm.customvocabulary.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.restdocs.RestDocsMockMvcConfigurationCustomizer;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import project.study.jgm.customvocabulary.common.BaseControllerTest;
import project.study.jgm.customvocabulary.common.dto.MessageDto;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.dto.MemberCreateDto;
import project.study.jgm.customvocabulary.security.dto.OnlyTokenDto;
import project.study.jgm.customvocabulary.security.dto.TokenDto;
import project.study.jgm.customvocabulary.vocabulary.category.Category;
import project.study.jgm.customvocabulary.vocabulary.category.CategoryDivision;
import project.study.jgm.customvocabulary.vocabulary.category.CategoryStatus;
import project.study.jgm.customvocabulary.vocabulary.category.dto.PersonalCategoryCreateDto;
import project.study.jgm.customvocabulary.vocabulary.category.exception.CategoryExistsInTheCorrespondingOrdersException;
import project.study.jgm.customvocabulary.vocabulary.category.exception.ParentNotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CategoryApiControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("개인용 카테고리 생성")
    public void addPersonalCategory() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        PersonalCategoryCreateDto createDto = PersonalCategoryCreateDto.builder()
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
                                .header(X_AUTH_TOKEN, user1TokenDto.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto))
                )
                .andDo(print());

        //then
        perform
                .andExpect(status().isCreated())
                .andExpect(redirectedUrl("http://localhost/api/vocabulary/category/" + user1.getId()))
                .andExpect(jsonPath("message").value(MessageDto.ADD_PERSONAL_CATEGORY_SUCCESSFULLY));

    }

    @Test
    @DisplayName("개인용 자식 카테고리 생성")
    public void addPersonalChildCategory() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        Category parent = createCategory(user1, CategoryDivision.PERSONAL, "parent", null, 0, CategoryStatus.REGISTER);

        PersonalCategoryCreateDto createDto = PersonalCategoryCreateDto.builder()
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
                .andExpect(redirectedUrl("http://localhost/api/vocabulary/category/" + user1.getId()))
                .andExpect(jsonPath("message").value(MessageDto.ADD_PERSONAL_CATEGORY_SUCCESSFULLY));

    }

    @Test
    @DisplayName("개인용 카테고리 생성 시 부모 카테고리가 지정 됐는데 부모 카테고리를 찾을 수 없는 경우")
    public void addPersonalCategory_ParentNotFound() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        PersonalCategoryCreateDto createDto = PersonalCategoryCreateDto.builder()
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
        Category parent = createCategory(user1, CategoryDivision.PERSONAL, "parent", null, orders, CategoryStatus.REGISTER);

        PersonalCategoryCreateDto createDto = PersonalCategoryCreateDto.builder()
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
    @DisplayName("인증되지 않은 사용자가 개인용 카테고리를 생성하는 경우")
    public void addPersonalCategory_UnAuthentication() throws Exception {
        //given
        MemberCreateDto memberCreateDto = getMemberCreateDto("user1", "user1");
        Member user1 = memberService.userJoin(memberCreateDto);

        PersonalCategoryCreateDto createDto = PersonalCategoryCreateDto.builder()
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
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].parentId").isEmpty())
                .andExpect(jsonPath("$[0].subCategoryList").exists())
                .andExpect(jsonPath("$[0].vocabularyCount").exists())
                .andExpect(jsonPath("$[0].orders").exists())
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

}