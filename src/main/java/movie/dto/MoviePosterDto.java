package movie.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MoviePosterDto {
    private Long posterId;
    private Long movieId;
    private String posterUrl;
    private LocalDateTime createdAt;
}
