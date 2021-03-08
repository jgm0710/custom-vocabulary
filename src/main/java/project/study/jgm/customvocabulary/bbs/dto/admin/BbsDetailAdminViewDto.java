package project.study.jgm.customvocabulary.bbs.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.modelmapper.ModelMapper;
import project.study.jgm.customvocabulary.bbs.Bbs;
import project.study.jgm.customvocabulary.bbs.BbsStatus;
import project.study.jgm.customvocabulary.bbs.upload.BbsUploadFile;
import project.study.jgm.customvocabulary.common.upload.UploadFileResponseDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class BbsDetailAdminViewDto {
    private Long id;

    private String writer;

    private String title;

    private String content;

    private int views;

    private int likeCount;

    private int replyCount;

    private LocalDateTime registerDate;

    private LocalDateTime updateDate;

    private BbsStatus status;   //Bbs 저장 상태 표시 [REGISTER, DELETE]

    private boolean permissionToDeleteAndModify;

    @Builder.Default
    private List<UploadFileResponseDto> uploadFiles =new ArrayList<>();

//.id
//.member
//.title
//.content
//.views
//.likeCount
//.replyCount
//.registerDate
//.updateDate
//.status

    public static BbsDetailAdminViewDto bbsToDetailAdminView(Bbs bbs, ModelMapper modelMapper) {

        BbsDetailAdminViewDto bbsDetailAdminViewDto = BbsDetailAdminViewDto.builder()
                .id(bbs.getId())
                .writer(bbs.getMember().getNickname())
                .title(bbs.getTitle())
                .content(bbs.getContent())
                .views(bbs.getViews())
                .likeCount(bbs.getLikeCount())
                .replyCount(bbs.getReplyCount())
                .registerDate(bbs.getRegisterDate())
                .updateDate(bbs.getUpdateDate())
                .status(bbs.getStatus())
                .permissionToDeleteAndModify(true)
                .build();

        List<BbsUploadFile> bbsUploadFileList = bbs.getBbsUploadFileList();
        for (BbsUploadFile bbsUploadFile : bbsUploadFileList) {
            UploadFileResponseDto uploadFileResponseDto = modelMapper.map(bbsUploadFile, UploadFileResponseDto.class);
            bbsDetailAdminViewDto.addUploadFileResponseDto(uploadFileResponseDto);
        }

        return bbsDetailAdminViewDto;
    }

    public void addUploadFileResponseDto(UploadFileResponseDto uploadFileResponseDto) {
        this.uploadFiles.add(uploadFileResponseDto);
    }
}
