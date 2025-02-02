package movie.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import movie.dto.MovieDto;
import movie.dto.MoviePosterDto;
import movie.mapper.MovieMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class DailyBoxOfficeService {
    private final MovieMapper movieMapper;

    private final ObjectMapper objectMapper;

    private final String KOBIS_API_KEY;
    private final OkHttpClient client;

    public DailyBoxOfficeService(MovieMapper movieMapper,
                                 @Value("${box-office.api.key}") String kobisApiKey) {
        this.movieMapper = movieMapper;
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.KOBIS_API_KEY = kobisApiKey;
    }

    /**
     * 매일 자정에 박스오피스 데이터를 업데이트하는 스케줄러
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateBoxOfficeDaily() {
        String targetDt = getYesterdayString();
        try {
            updateMoviesFromBoxOffice(targetDt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getYesterdayString() {
        return LocalDate.now().minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private String sendAPIRequest(String targetDt) throws IOException {
        String url = "http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/"
                + "searchDailyBoxOfficeList.json?key=" + KOBIS_API_KEY
                + "&targetDt=" + targetDt;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return null; // 요청 실패 시 null 반환
            }
            return response.body().string();
        }
    }

    /**
     * 특정 날짜의 박스오피스 데이터를 업데이트하는 메소드
     */
    @Transactional
    public void updateMoviesFromBoxOffice(String targetDt) throws IOException {
        String jsonStr = sendAPIRequest(targetDt);
        if (jsonStr == null) {
            return;
        }

        JsonNode root = objectMapper.readTree(jsonStr);
        JsonNode dailyList = root.path("boxOfficeResult").path("dailyBoxOfficeList");
        if (dailyList.isMissingNode() || !dailyList.isArray()) {
            return;
        }

//        movieMapper.updateAllMovieRankToNull();

        for (JsonNode item : dailyList) {
            Long movieId = item.path("movieCd").asLong();
            Integer rank = item.path("rank").asInt();
            long audiAcc = item.path("audiAcc").asLong();

            MovieDto existing = movieMapper.selectMovieDetail(movieId);

            if (existing == null) {
                // 영화 정보 삽입
                MovieDto newMovie = new MovieDto();
                newMovie.setMovieId(movieId);
                newMovie.setTitle(item.path("movieNm").asText());
                newMovie.setDirector("");   // kmdb api
                newMovie.setMovieRank(rank);
                newMovie.setGenre("");  // kmdb api
                newMovie.setDescription("");    // kmdb api
                newMovie.setTotalAudienceCount(audiAcc);

//                movieMapper.insertMovie(newMovie);

                // 영화 포스터 정보 삽입
                MoviePosterDto newPoster = new MoviePosterDto();
                newPoster.setMovieId(movieId);
                newPoster.setPosterUrl("");     // kmdb api

//                movieMapper.insertMoviePoster(newPoster);
            } else {
                MovieDto updateDto = new MovieDto();
                updateDto.setMovieId(movieId);
                updateDto.setMovieRank(rank);
                updateDto.setTotalAudienceCount(audiAcc);

//                movieMapper.updateMovieRankAndAudience(updateDto);
            }
        }
    }
}

