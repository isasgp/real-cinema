package movie.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long commentId;        // 댓글 ID
    private Long movieId;          // 영화 ID
    private String nickname;       // 닉네임
    private String password;       // 비밀번호
    private int rating;            // 평점
    private String comment;        // 댓글 내용
    private int likesCount;        // 좋아요 수
    private char isAudience;       // 실관람객 여부 ('Y' or 'N')
    private LocalDateTime createdAt; // 작성 시각

    private String ticketUrl;   // 티켓 이미지 경로
}
