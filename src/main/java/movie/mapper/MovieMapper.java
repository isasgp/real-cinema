package movie.mapper;

import movie.dto.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MovieMapper {
    // 영화 정보
    List<MovieDto> selectMovieList();
    void insertMovie(MovieDto movieDto);
    MovieDto selectMovieDetail(Long movieId);
    void updateAllMovieRankToNull();
    void updateMovieRankAndAudience(MovieDto updateDto);

    // 영화평
    List<CommentDto> selectCommentsByMovieId(Long movieId);
    void insertComment(CommentDto commentDto);
    void updateComment(CommentDto commentDto);
    void deleteComment(CommentDto commentDto);

    // 영화평 좋아요
    void increaseLikesCount(Long commentId);
    void insertCommentLikes(CommentLikesDto commentLikesDto);

    // 영화 포스터
    void insertMoviePoster(MoviePosterDto newPoster);
    MoviePosterDto selectPosterByMovieId(Long movieId);

    // 실관람객 인증
    void updateToAudience(CommentDto commentDto);
    void insertMovieTicket(List<MovieTicketDto> fileInfoList);
    MovieTicketDto selectMovieTicketByCommentId(Long movieId);
    String selectMovieSearchTitle(Long commentId);

}
