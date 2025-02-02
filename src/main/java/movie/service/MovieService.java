package movie.service;

import movie.dto.CommentDto;
import movie.dto.MovieDto;
import movie.dto.MoviePosterDto;
import movie.dto.MovieTicketDto;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;

public interface MovieService {
    List<MovieDto> selectMovieList();
    MovieDto selectMovieDetail(Long movieId);

    List<CommentDto> selectCommentsByMovieId(Long movieId);
    void insertComment(CommentDto commentDto);
    void updateComment(CommentDto commentDto);
    void deleteComment(Long commentId, String password);

    void likeComment(Long commentId, String userIp);

    MoviePosterDto selectPosterByMovieId(Long movieId);

    boolean checkMovieTicket(CommentDto commentDto, MultipartHttpServletRequest request);
    void insertMovieTicket(CommentDto commentDto, MultipartHttpServletRequest request);
}
