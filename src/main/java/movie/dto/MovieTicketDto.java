package movie.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MovieTicketDto {
    private Long ticketId;
    private Long commentId;
    private String ticketUrl; // 파일 경로 or base64
    private char isAudience;    // 'Y' or 'N'
    private LocalDateTime createdAt;
}
