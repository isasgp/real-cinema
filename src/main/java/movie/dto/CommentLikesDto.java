package movie.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentLikesDto {
    private Long likeId;      // auto_increment
    private Long commentId;
    private String userIp;
    private LocalDateTime createdAt;
}
