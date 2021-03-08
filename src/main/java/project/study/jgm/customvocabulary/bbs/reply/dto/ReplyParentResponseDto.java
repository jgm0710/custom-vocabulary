package project.study.jgm.customvocabulary.bbs.reply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import project.study.jgm.customvocabulary.bbs.reply.Reply;
import project.study.jgm.customvocabulary.bbs.reply.like.ReplyLikeService;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRole;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReplyParentResponseDto {

    private Long id;

    private String writer;

    private String content;

    private int likeCount;

    private int childrenCount;

    private LocalDateTime registerDate;

    private boolean like;

    private boolean viewLike;

    private boolean permissionToDeleteAndModify;

    public static ReplyParentResponseDto replyToParentResponseDto(Reply reply, ModelMapper modelMapper) {
        ReplyParentResponseDto replyParentResponseDto = modelMapper.map(reply, ReplyParentResponseDto.class);
        replyParentResponseDto.setWriter(reply.getMember().getNickname());
        replyParentResponseDto.setViewLike(false);
        replyParentResponseDto.setPermissionToDeleteAndModify(true);

        return replyParentResponseDto;
    }

    public static List<ReplyParentResponseDto> replyListToParentListResponse(List<Reply> replyList, Member member, ReplyLikeService replyLikeService) {
        List<ReplyParentResponseDto> replyParentResponseDtoList = new ArrayList<>();

        for (Reply reply : replyList) {
            boolean viewLike = true;
            boolean allowModificationAndDeletion = false;
            boolean existLike = false;

            if (member != null) {
                if (member.getRoles().contains(MemberRole.ADMIN)) {
                    viewLike = false;
                    allowModificationAndDeletion = true;
                } else {
                    if (reply.getMember().getId().equals(member.getId())) {
                        viewLike = false;
                        allowModificationAndDeletion = true;
                    }
                }

                existLike = replyLikeService.getExistLike(member.getId(), reply.getId());
            }

            ReplyParentResponseDto replyParentResponseDto = ReplyParentResponseDto.builder()
                    .id(reply.getId())
                    .writer(reply.getMember().getNickname())
                    .content(reply.getContent())
                    .likeCount(reply.getLikeCount())
                    .childrenCount(reply.getChildrenCount())
                    .registerDate(reply.getRegisterDate())
                    .like(existLike)
                    .viewLike(viewLike)
                    .permissionToDeleteAndModify(allowModificationAndDeletion)
                    .build();

            replyParentResponseDtoList.add(replyParentResponseDto);
        }

        return replyParentResponseDtoList;
    }
}
