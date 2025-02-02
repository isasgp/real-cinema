CREATE TABLE movies (
    movie_id BIGINT PRIMARY KEY,       -- 영화 고유 식별자
    movie_rank INT,	-- 영화 박스오피스 순위
    title VARCHAR(255) NOT NULL,                      -- 영화 제목
    search_title VARCHAR(255) GENERATED ALWAYS AS (    -- OCR 검색용 제목 (자동 생성)
        REGEXP_REPLACE(title, '[^가-힣a-zA-Z0-9]', '')
    ) STORED,
    director VARCHAR(255) NOT NULL,                   -- 감독 이름
    release_date DATE,                                -- 개봉일
    genre VARCHAR(100),                               -- 장르
    description TEXT,                                 -- 영화 설명
    total_audience_count BIGINT DEFAULT 0,           -- 누적 관객수
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,   -- 생성 시각
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP -- 수정 시각
);

CREATE TABLE movie_posters (
    poster_id BIGINT AUTO_INCREMENT PRIMARY KEY,      -- 포스터 고유 식별자
    movie_id BIGINT NOT NULL,                         -- 영화 ID (외래 키)
    poster_url VARCHAR(500) NOT NULL,                 -- 포스터 URL
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,   -- 생성 시각
    FOREIGN KEY (movie_id) REFERENCES movies(movie_id) ON DELETE CASCADE -- 영화 삭제 시 포스터도 삭제
);

CREATE TABLE movie_comments (
    comment_id BIGINT AUTO_INCREMENT PRIMARY KEY, -- 코멘트 고유 ID
    movie_id BIGINT NOT NULL,                     -- 영화 ID (외래키)
    nickname VARCHAR(50) NOT NULL,                  -- 사용자 닉네임
    password CHAR(4) NOT NULL,                   -- 비밀번호 (문자 4자리로 제한)
    comment TEXT NOT NULL,                       -- 코멘트 내용
    likes_count INT DEFAULT 0,                   -- 좋아요 수
    rating TINYINT NOT NULL DEFAULT 0,  -- 평점
    is_audience CHAR(1) NOT NULL DEFAULT 'N',                  -- 실관람객 여부
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 생성 시간
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 수정 시간
    FOREIGN KEY (movie_id) REFERENCES movies(movie_id) ON DELETE CASCADE
);

CREATE TABLE comment_likes (
    like_id BIGINT AUTO_INCREMENT PRIMARY KEY, -- 좋아요 고유 ID
    comment_id BIGINT NOT NULL,               -- 코멘트 ID (외래키)
    user_ip VARCHAR(45) NOT NULL,             -- 사용자 IP 주소
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 좋아요 누른 시간
    FOREIGN KEY (comment_id) REFERENCES movie_comments(comment_id) ON DELETE CASCADE,
    UNIQUE (comment_id, user_ip) -- 코멘트와 IP 주소의 조합으로 중복 좋아요 방지
);

CREATE TABLE movie_tickets (
    ticket_id   BIGINT AUTO_INCREMENT PRIMARY KEY,   -- 티켓 고유 ID
    comment_id  BIGINT NOT NULL,                     -- 해당 코멘트의 ID (movie_comments.comment_id 참조)
    ticket_url  VARCHAR(500) NOT NULL,               -- 티켓 이미지 저장 (파일 경로 또는 Base64)
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 생성 시간
    FOREIGN KEY (comment_id) REFERENCES movie_comments(comment_id) ON DELETE CASCADE
);
