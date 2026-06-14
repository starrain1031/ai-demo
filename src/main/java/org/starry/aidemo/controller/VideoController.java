package org.starry.aidemo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.starry.aidemo.Repository.ChatHistoryRepository;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Handles video-understanding chat requests through an OpenAI-compatible API.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai/video")
public class VideoController {

    private static final long MAX_VIDEO_SIZE = 100L * 1024 * 1024;

    private final ChatHistoryRepository chatHistoryRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @Value("${spring.ai.openai.base-url}")
    private String openAiBaseUrl;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${app.ai.video.model:qwen3.5-omni-flash}")
    private String videoModel;

    /**
     * Streams a response for a video and prompt pair.
     *
     * @param prompt user prompt for the uploaded video
     * @param chatId conversation identifier recorded in history
     * @param file uploaded video file
     * @return streamed response text from the multimodal model
     */
    @PostMapping(value = "/chat", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = "text/plain;charset=utf-8")
    public Flux<String> chat(@RequestParam("prompt") String prompt,
                             @RequestParam("chatId") String chatId,
                             @RequestParam("file") MultipartFile file) {

        if (!StringUtils.hasText(prompt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "prompt can not be null");
        }
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "video file is required");
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.startsWith("video/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only video files are supported");
        }
        if (file.getSize() > MAX_VIDEO_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Video file is too large");
        }

        chatHistoryRepository.save("video", chatId);

        return Flux.using(
                () -> sendVideoRequest(prompt, file),
                this::readStreamContent,
                response -> response.body().close()
        ).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Builds and sends the OpenAI-compatible video chat completion request.
     */
    private HttpResponse<Stream<String>> sendVideoRequest(String prompt, MultipartFile file)
            throws IOException, InterruptedException {

        String contentType = file.getContentType();
        String base64Video = Base64.getEncoder().encodeToString(file.getBytes());
        String videoDataUrl = "data:" + contentType + ";base64," + base64Video;

        Map<String, Object> textPart = new LinkedHashMap<>();
        textPart.put("type", "text");
        textPart.put("text", prompt);

        Map<String, Object> videoUrl = new LinkedHashMap<>();
        videoUrl.put("url", videoDataUrl);

        Map<String, Object> videoPart = new LinkedHashMap<>();
        videoPart.put("type", "video_url");
        videoPart.put("video_url", videoUrl);

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "user");
        message.put("content", List.of(videoPart, textPart));

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", videoModel);
        requestBody.put("messages", List.of(message));
        requestBody.put("stream", true);
        requestBody.put("modalities", List.of("text"));

        String body = objectMapper.writeValueAsString(requestBody);
        String url = openAiBaseUrl.endsWith("/")
                ? openAiBaseUrl + "chat/completions"
                : openAiBaseUrl + "/chat/completions";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(5))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<Stream<String>> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofLines());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String errorBody;
            try (Stream<String> lines = response.body()) {
                errorBody = String.join("\n", lines.toList());
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "DashScope video request failed: " + errorBody);
        }

        return response;
    }

    /**
     * Converts server-sent-event style response lines into streamed text chunks.
     */
    private Flux<String> readStreamContent(HttpResponse<Stream<String>> response) {
        return Flux.fromStream(response.body())
                .filter(StringUtils::hasText)
                .map(String::trim)
                .filter(line -> line.startsWith("data:"))
                .map(line -> line.substring("data:".length()).trim())
                .takeUntil("[DONE]"::equals)
                .filter(data -> !"[DONE]".equals(data))
                .map(this::extractDeltaContent)
                .filter(StringUtils::hasText);
    }

    /**
     * Extracts the delta content field from one streaming JSON payload.
     */
    private String extractDeltaContent(String data) {
        try {
            JsonNode root = objectMapper.readTree(data);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                return "";
            }
            JsonNode delta = choices.get(0).path("delta");
            JsonNode content = delta.path("content");
            if (content.isString()) {
                return content.asString();
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }
}
