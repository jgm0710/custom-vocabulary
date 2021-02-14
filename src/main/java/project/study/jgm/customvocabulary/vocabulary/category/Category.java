package project.study.jgm.customvocabulary.vocabulary.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.vocabulary.category.dto.CategoryUpdateDto;
import project.study.jgm.customvocabulary.vocabulary.category.dto.PersonalCategoryCreateDto;
import project.study.jgm.customvocabulary.vocabulary.category.dto.SharedCategoryCreateDto;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Category {

    @Id
    @GeneratedValue
    @Column(name = "category_id")
    private Long id;
    
    private String name;

    @ManyToOne(fetch = LAZY)
    private Member member;  //personal Category 의 경우 특정 Member 가 만든 카테고리이기 때문에 Member 에 대한 정보가 필요
                            //null 이면 sharedCategory

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Builder.Default
    @OneToMany(mappedBy = "parent")
    private List<Category> children = new ArrayList<>();

    private int vocabularyCount;

    @Enumerated(EnumType.STRING)
    private CategoryDivision division;  //카테고리 구분 [PERSONAL, SHARED]

    private int orders;

    @Enumerated(EnumType.STRING)
    private CategoryStatus status;  //삭제 시 category 를 삭제하지 않고 상태만 바꾸기 위해 사용//  [REGISTER, DELETE]

//.name
//.member
//.parent
//.vocabularyCount
//.division
//.orders
//.status


    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
//                ", member=" + member +
//                ", parent=" + parent +
                ", children=" + children +
                ", vocabularyCount=" + vocabularyCount +
                ", division=" + division +
                ", orders=" + orders +
                ", status=" + status +
                '}';
    }

    public static Category createPersonalCategory(Member member, String name, Category parent, int orders) {
        return Category.builder()
                .name(name)
                .member(member)
                .parent(parent)
                .division(CategoryDivision.PERSONAL)
                .orders(orders)
                .status(CategoryStatus.REGISTER)
                .vocabularyCount(0)
                .build();
    }

    public static Category createSharedCategory(String name, Category parent, int orders) {
        return Category.builder()
                .name(name)
                .parent(parent)
                .division(CategoryDivision.SHARED)
                .orders(orders)
                .status(CategoryStatus.REGISTER)
                .vocabularyCount(0)
                .build();
    }

    public void updateCategory(String name, Category parent, int orders) {
        this.name = name;
        this.parent = parent;
        this.orders = orders;
    }

    public void addVocabulary() {
        this.vocabularyCount++;
    }

    public void deleteVocabulary() {
        this.vocabularyCount--;
    }
}
