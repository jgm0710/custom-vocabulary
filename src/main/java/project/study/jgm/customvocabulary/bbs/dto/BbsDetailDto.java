package project.study.jgm.customvocabulary.bbs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import project.study.jgm.customvocabulary.bbs.Bbs;
import project.study.jgm.customvocabulary.bbs.upload.BbsUploadFile;
import project.study.jgm.customvocabulary.common.upload.UploadFileResponseDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BbsDetailDto {
    private Long id;

    private String writer;

    private String title;

    private String content;

    private int views;

    private int likeCount;

    private int replyCount;

    private LocalDateTime registerDate;

    private LocalDateTime updateDate;

    private boolean like;

    private boolean viewLike;

    private boolean permissionToDeleteAndModify;

    @Builder.Default
    private List<UploadFileResponseDto> uploadFiles = new ArrayList<>();

//id
//writer
//title
//content
//views
//likeCount
//replyCount
//registerDate
//updateDate

    public static BbsDetailDto bbsToDetail(Bbs bbs, ModelMapper modelMapper) {

        BbsDetailDto bbsDetailDto = BbsDetailDto.builder()
                .id(bbs.getId())
                .writer(bbs.getMember().getNickname())
                .title(bbs.getTitle())
                .content(bbs.getContent())
                .views(bbs.getViews())
                .likeCount(bbs.getLikeCount())
                .replyCount(bbs.getReplyCount())
                .registerDate(bbs.getRegisterDate())
                .updateDate(bbs.getUpdateDate())
                .viewLike(true)
                .permissionToDeleteAndModify(false)
                .build();

        List<BbsUploadFile> bbsUploadFileList = bbs.getBbsUploadFileList();
        for (BbsUploadFile bbsUploadFile : bbsUploadFileList) {
            UploadFileResponseDto uploadFileResponseDto = modelMapper.map(bbsUploadFile, UploadFileResponseDto.class);
            bbsDetailDto.addUploadFileResponseDto(uploadFileResponseDto);
        }

        return bbsDetailDto;
    }

    public void addUploadFileResponseDto(UploadFileResponseDto uploadFileResponseDto) {
        this.uploadFiles.add(uploadFileResponseDto);
    }

}
