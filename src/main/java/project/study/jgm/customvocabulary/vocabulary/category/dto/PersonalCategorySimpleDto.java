package project.study.jgm.customvocabulary.vocabulary.category.dto;

import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.vocabulary.category.Category;

import java.util.ArrayList;
import java.util.List;

public class PersonalCategorySimpleDto {
    private Long id;

    private String name;

    private Member member;

    private Category parent;

    private List<Category> children = new ArrayList<>();

    private int orders;
}
