package project.study.jgm.customvocabulary.bbs.reply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import project.study.jgm.customvocabulary.bbs.reply.Reply;
import project.study.jgm.customvocabulary.members.Member;
import project.study.jgm.customvocabulary.members.MemberRole;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReplyChildResponseDto {
    private Long id;
    private String writer;
    private String content;
    private LocalDateTime registerDate;
    private boolean permissionToDeleteAndModify;

    public static ReplyChildResponseDto replyToChildResponseDto(Reply reply, ModelMapper modelMapper) {
        ReplyChildResponseDto replyChildResponseDto = modelMapper.map(reply, ReplyChildResponseDto.class);
        replyChildResponseDto.setWriter(reply.getMember().getNickname());
        replyChildResponseDto.setPermissionToDeleteAndModify(true);

        return replyChildResponseDto;
    }

    public static List<ReplyChildResponseDto> replyListToChildList(List<Reply> replyList, Member member) {
        List<ReplyChildResponseDto> replyChildResponseDtoList = new ArrayList<>();

        for (Reply reply : replyList) {
            boolean allowModificationAndDeletion = false;

            if (member != null) {
                if (member.getRoles().contains(MemberRole.ADMIN)) {
                    allowModificationAndDeletion = true;
                }
                else {
                    if (reply.getMember().getId().equals(member.getId())) {
                        allowModificationAndDeletion = true;
                    }
                }
            }

            ReplyChildResponseDto replyChildResponseDto = ReplyChildResponseDto.builder()
                    .id(reply.getId())
                    .writer(reply.getMember().getNickname())
                    .content(reply.getContent())
                    .registerDate(reply.getRegisterDate())
                    .permissionToDeleteAndModify(allowModificationAndDeletion)
                    .build();
            replyChildResponseDtoList.add(replyChildResponseDto);
        }

        return replyChildResponseDtoList;
    }
}
