package movie.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import movie.mapper.MovieMapper;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class OcrUtils {
    // CLOVA OCR API 관련 설정 (향후 @Value로 주입 받아도 좋음)
    private final String OCR_URL;
    private final String OCR_API_KEY;

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;


    public OcrUtils(@Value("${ocr.api.url}") String ocrUrl,
                    @Value("${ocr.api.key}") String ocrApiKey) {
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.OCR_URL = ocrUrl;
        this.OCR_API_KEY = ocrApiKey;
    }


    public String extractTextFromImage(MultipartHttpServletRequest request) {
        if (ObjectUtils.isEmpty(request)) {
            return null;
        }
        MultipartFile multipartFile = request.getFile("ticketFile");
        try {
            String jsonResponse = sendOCRRequest(multipartFile);
            return extractTextFromOCRResponse(jsonResponse);
        } catch (IOException e) {
            throw new RuntimeException("OCR 요청 실패", e);
        }
    }

    /**
     * CLOVA OCR API로 이미지 전송
     */
    private String sendOCRRequest(MultipartFile multipartFile) throws IOException {
        byte[] fileBytes = multipartFile.getBytes();
        String fileName = multipartFile.getOriginalFilename();

        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("파일명이 존재하지 않습니다.");
        }

        // 파일 확장자 추출 및 확인 (jpg, jpeg, png 만 허용)
        String fileExtension = getFileExtension(fileName);
        if (!isValidImageExtension(fileExtension)) {
            throw new IllegalArgumentException("지원되지 않는 파일 형식입니다. (지원 가능: jpg, jpeg, png)");
        }

        // OCR 요청에 사용할 JSON message
        String messageJson = createJsonRequest(fileExtension);

        // 멀티파트 폼 구성
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", multipartFile.getOriginalFilename(),
                        RequestBody.create(fileBytes, MediaType.parse("image/jpeg")))
                .addFormDataPart("message", messageJson)
                .build();

        // HTTP 요청 생성
        Request request = new Request.Builder()
                .url(OCR_URL)
                .header("X-OCR-SECRET", OCR_API_KEY)
                .post(requestBody)
                .build();

        // 요청 실행
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("OCR 요청 실패: " + response);
            }
            return response.body().string();
        }
    }

    /**
     * OCR API 요청에 사용할 JSON (동적 생성)
     */
    private String createJsonRequest(String fileFormat) throws IOException {
        Map<String, Object> requestJson = new HashMap<>();
        requestJson.put("version", "v2");
        requestJson.put("requestId", "1234");
        requestJson.put("timestamp", System.currentTimeMillis());
        requestJson.put("lang", "ko");
        requestJson.put("images", Collections.singletonList(
                Map.of("format", fileFormat, "name", "test2")
        ));

        return objectMapper.writeValueAsString(requestJson);
    }

    /**
     * OCR 응답 JSON에서 inferText 추출 → 특수문자 제거
     */
    private String extractTextFromOCRResponse(String jsonResponse) {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray imagesArray = jsonObject.getJSONArray("images");

        StringBuilder result = new StringBuilder();
        Pattern nonAlphanumeric = Pattern.compile("[^a-zA-Z0-9가-힣]");

        for (int i = 0; i < imagesArray.length(); i++) {
            JSONObject image = imagesArray.getJSONObject(i);
            JSONArray fieldsArray = image.getJSONArray("fields");

            for (int j = 0; j < fieldsArray.length(); j++) {
                JSONObject field = fieldsArray.getJSONObject(j);
                String text = field.getString("inferText");
                // 특수문자/공백 제거
                String cleanedText = nonAlphanumeric.matcher(text).replaceAll("");
                result.append(cleanedText);
            }
        }
        return result.toString();
    }

    private String getFileExtension(String filename) {
        int lastIndex = filename.lastIndexOf(".");
        if (lastIndex == -1) {
            return ""; // 확장자가 없는 경우
        }
        return filename.substring(lastIndex + 1).toLowerCase(); // 확장자를 소문자로 변환
    }

    /**
     * 지원되는 이미지 확장자인지 확인 (jpg, jpeg, png)
     */
    private boolean isValidImageExtension(String extension) {
        return extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png");
    }
}
