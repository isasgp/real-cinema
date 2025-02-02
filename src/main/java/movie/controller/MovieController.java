package movie.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import movie.dto.CommentDto;
import movie.dto.MovieDto;
import movie.dto.MoviePosterDto;
import movie.service.MovieService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @Value("${spring.servlet.multipart.location}")
    private String UPLOAD_ROOT; // "c:\\uploads\\"


    @GetMapping("/movie/openMovieList.do")
    public ModelAndView openMovieList() throws Exception {
        ModelAndView mv = new ModelAndView("/movie/movieList");

        List<MovieDto> list = movieService.selectMovieList();
        mv.addObject("list", list);

        return mv;
    }

    @PostMapping("/movie/insertComment.do")
    public String insertComment(CommentDto commentDto) throws Exception {
        movieService.insertComment(commentDto);
        return "redirect:/movie/openMovieDetail.do?movieId=" + commentDto.getMovieId();
    }

    @PostMapping("/movie/updateComment.do")
    public String updateComment(CommentDto commentDto) throws Exception {
        movieService.updateComment(commentDto);
        return "redirect:/movie/openMovieDetail.do?movieId=" + commentDto.getMovieId();
    }

    @PostMapping("/movie/deleteComment.do")
    public String deleteComment(@RequestParam("commentId") Long commentId,
                                @RequestParam("movieId") Long movieId,
                                @RequestParam("password") String password) throws Exception {
        movieService.deleteComment(commentId, password);
        return "redirect:/movie/openMovieDetail.do?movieId=" + movieId;
    }

    @PostMapping("/movie/likeComment.do")
    public String likeComment(@RequestParam("commentId") Long commentId,
                              @RequestParam("movieId") Long movieId,
                              HttpServletRequest request,
                              RedirectAttributes ra) throws Exception {
        // IP 추출
        String userIp = request.getRemoteAddr();
        try {
            movieService.likeComment(commentId, userIp);
        } catch (DuplicateKeyException e) {
            ra.addAttribute("error", "alreadyLiked");
        }

        return "redirect:/movie/openMovieDetail.do?movieId=" + movieId;
    }

    @PostMapping("/movie/insertMovieTicket.do")
    public String insertMovieTicket(CommentDto commentDto, MultipartHttpServletRequest request) throws Exception {
        movieService.insertMovieTicket(commentDto, request);
        return "redirect:/movie/openMovieDetail.do?movieId=" + commentDto.getMovieId();
    }

    // 상세 조회 요청을 처리하는 메서드
    // /movie/openMovieDetail.do?movieId=1234
    @GetMapping("/movie/openMovieDetail.do")
    public ModelAndView openMovieDetail(@RequestParam("movieId") Long movieId) throws Exception {
        MovieDto movieDto = movieService.selectMovieDetail(movieId);
        List<CommentDto> comments = movieService.selectCommentsByMovieId(movieId);

        MoviePosterDto poster = movieService.selectPosterByMovieId(movieId);

        ModelAndView mv = new ModelAndView("/movie/movieDetail");
        mv.addObject("movie", movieDto);
        mv.addObject("comments", comments);
        mv.addObject("poster", poster );
        return mv;
    }

    @GetMapping("/movie/showTicketImage.do")
    public ResponseEntity<?> showTicketImage(@RequestParam("filename") String filename) {
        try {
            // 파일 경로 정리 및 유효성 검사
            String sanitizedPath = filename.replace("\\", "/");
            String rootNormalized = UPLOAD_ROOT.replace("\\", "/");

            if (sanitizedPath.toLowerCase().startsWith(rootNormalized.toLowerCase())) {
                sanitizedPath = sanitizedPath.substring(rootNormalized.length());
            }

            File file = new File(UPLOAD_ROOT + sanitizedPath);
            if (!file.exists() || !file.isFile()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body("File not found: " + filename);
            }

            // MIME 타입 자동 탐색
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body("Unsupported file type");
            }

            // 파일 스트림을 안전하게 사용
            FileInputStream fis = new FileInputStream(file);
            InputStreamResource resource = new InputStreamResource(fis);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Error processing file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Invalid request: " + e.getMessage());
        }
    }

    @PostMapping("/movie/verifyMovieTicket.do")
    public ResponseEntity<String> verifyMovieTicket(CommentDto commentDto, MultipartHttpServletRequest request) throws Exception {
        boolean isValid = movieService.checkMovieTicket(commentDto, request);

        String statusMessage = isValid ? "✅ 인증 완료" : "❌ 인증 실패";

        return ResponseEntity.ok(statusMessage);
    }


}

