package project.study.jgm.customvocabulary.bbs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.study.jgm.customvocabulary.bbs.Bbs;
import project.study.jgm.customvocabulary.bbs.BbsStatus;
import project.study.jgm.customvocabulary.bbs.dto.admin.BbsSimpleAdminViewDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BbsSimpleDto {
    private Long id;

    private String writer;

    private String title;

    private int views;

    private int likeCount;

    private int replyCount;

    private LocalDateTime registerDate;

    public static List<BbsSimpleDto> bbsListToSimpleList(List<Bbs> bbsList) {
        List<BbsSimpleDto> bbsSimpleDtoList = new ArrayList<>();

        for (Bbs bbs : bbsList) {
            BbsSimpleDto bbsSimpleDto = BbsSimpleDto.builder()
                    .id(bbs.getId())
                    .writer(bbs.getMember().getNickname())
                    .title(bbs.getTitle())
                    .views(bbs.getViews())
                    .likeCount(bbs.getLikeCount())
                    .replyCount(bbs.getReplyCount())
                    .registerDate(bbs.getRegisterDate())
                    .build();

            bbsSimpleDtoList.add(bbsSimpleDto);
        }

        return bbsSimpleDtoList;
    }
}
