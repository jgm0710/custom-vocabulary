package project.study.jgm.customvocabulary.bbs.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import project.study.jgm.customvocabulary.bbs.Bbs;
import project.study.jgm.customvocabulary.bbs.BbsStatus;
import project.study.jgm.customvocabulary.members.Member;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BbsSimpleAdminViewDto {

    private Long id;

    private String writer;

    private String title;

    private int views;

    private int likeCount;

    private int replyCount;

    private LocalDateTime registerDate;

    private BbsStatus status;   //Bbs 저장 상태 표시 [REGISTER, DELETE]

//.id
//.writer
//.title
//.views
//.likeCount
//.replyCount
//.registerDate
//.status

    public static List<BbsSimpleAdminViewDto> bbsListToSimpleAdminViewList(List<Bbs> bbsList) {
        List<BbsSimpleAdminViewDto> bbsSimpleAdminViewDtoList = new ArrayList<>();

        for (Bbs bbs : bbsList) {
            BbsSimpleAdminViewDto bbsSimpleAdminViewDto = BbsSimpleAdminViewDto.builder()
                    .id(bbs.getId())
                    .writer(bbs.getMember().getNickname())
                    .title(bbs.getTitle())
                    .views(bbs.getViews())
                    .likeCount(bbs.getLikeCount())
                    .replyCount(bbs.getReplyCount())
                    .registerDate(bbs.getRegisterDate())
                    .status(bbs.getStatus())
                    .build();

            bbsSimpleAdminViewDtoList.add(bbsSimpleAdminViewDto);
        }

        return bbsSimpleAdminViewDtoList;
    }

}
