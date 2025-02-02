package movie.service;

import lombok.RequiredArgsConstructor;
import movie.common.OcrUtils;
import movie.dto.*;
import movie.mapper.MovieMapper;
import movie.common.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {
    private final MovieMapper movieMapper;
    private final FileUtils fileUtils;
    private final OcrUtils ocrUtils;

    @Override
    public List<MovieDto> selectMovieList() {
        return movieMapper.selectMovieList();
    }

    @Override
    public MovieDto selectMovieDetail(Long movieId) {
        return movieMapper.selectMovieDetail(movieId);
    }

    @Override
    @Transactional
    public List<CommentDto> selectCommentsByMovieId(Long movieId) {
        List<CommentDto> comments = movieMapper.selectCommentsByMovieId(movieId);

        for(CommentDto comment : comments) {
            if (comment.getIsAudience() == 'Y') {
                // DB에서 ticket 조회
                MovieTicketDto ticket = movieMapper.selectMovieTicketByCommentId(comment.getCommentId());
                if (ticket != null) {
                    comment.setTicketUrl(ticket.getTicketUrl());
                }
            }
        }

        return comments;
    }

    @Override
    public void insertComment(CommentDto commentDto) {
        movieMapper.insertComment(commentDto);
    }

    @Override
    public void updateComment(CommentDto commentDto) {
        movieMapper.updateComment(commentDto);
    }

    @Override
    public void deleteComment(Long commentId, String password) {
        CommentDto commentDto = new CommentDto();
        commentDto.setCommentId(commentId);
        commentDto.setPassword(password);
        movieMapper.deleteComment(commentDto);
    }

    @Override
    @Transactional
    public void likeComment(Long commentId, String userIp) {
        CommentLikesDto likesDto = new CommentLikesDto();
        likesDto.setCommentId(commentId);
        likesDto.setUserIp(userIp);

        movieMapper.insertCommentLikes(likesDto);

        movieMapper.increaseLikesCount(commentId);
    }

    @Override
    public MoviePosterDto selectPosterByMovieId(Long movieId) {
        return movieMapper.selectPosterByMovieId(movieId);
    }

    @Override
    public boolean checkMovieTicket(CommentDto commentDto, MultipartHttpServletRequest request) {
        String searchTitle = movieMapper.selectMovieSearchTitle(commentDto.getCommentId());
        String extractText = ocrUtils.extractTextFromImage(request);    // 첨부파일 받아와서 해당 메소드에 이미지 파일 넣기
        return extractText.contains(searchTitle);
    }

    @Override
    @Transactional
    public void insertMovieTicket(CommentDto commentDto, MultipartHttpServletRequest request) {
        movieMapper.updateToAudience(commentDto);

        try {
            // 첨부 파일을 디스크에 저장하고, 첨부 파일 정보를 반환
            List<MovieTicketDto> fileInfoList = fileUtils.parseFileInfo(commentDto.getCommentId(), request);

            // 첨부 파일 정보를 DB에 저장
            if (!CollectionUtils.isEmpty(fileInfoList)) {
                movieMapper.insertMovieTicket(fileInfoList);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
