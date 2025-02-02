package movie.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MovieDto {
    private Long movieId;              // 영화 고유 식별자
    private Integer movieRank;
    private String title;              // 영화 제목
    private String director;           // 감독 이름
    private LocalDate releaseDate;     // 개봉일
    private String genre;              // 장르
    private String description;        // 영화 설명
    private Double rating;             // 네티즌 평점
    private Double audienceRating;     // 실관람객 평점
    private Long totalAudienceCount;   // 누적 관객수
    private LocalDateTime createdAt;   // 생성 시각
    private LocalDateTime updatedAt;   // 수정 시각
}
