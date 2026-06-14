package org.starry.aidemo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.http.HttpStatus;
import org.springframework.util.MimeType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.starry.aidemo.Repository.ChatHistoryRepository;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

/**
 * Handles general chat requests, including optional multimodal file input.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class ChatController {

    private final ChatClient chatClient;
    private final ChatHistoryRepository chatHistoryRepository;

    /**
     * Streams a chat response for the given conversation.
     *
     * @param prompt user prompt text
     * @param chatId conversation identifier used by chat memory
     * @param files optional files to send as multimodal media
     * @return streamed response text from the chat model
     */
    @RequestMapping(value = "/chat", produces = "text/plain;charset=utf-8")
//    @RequestMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestParam("prompt") String prompt, @RequestParam("chatId") String chatId,
                             @RequestParam(value = "files", required = false) List<MultipartFile> files) {

        if (!StringUtils.hasText(prompt)) {
            throw new ResponseStatusException
                    (HttpStatus.BAD_REQUEST, "Sorry, the prompt can not be null");
        }

        chatHistoryRepository.save("chat", chatId);

        ChatClient.ChatClientRequestSpec request = chatClient.prompt();

        if (files != null && !files.isEmpty()) {
            List<Media> medias = files.stream()
                    .map(file -> new Media(
                                    MimeType.valueOf(Objects.requireNonNull(file.getContentType())),
                                    file.getResource()
                            )
                    )
                    .toList();
            request.user(u -> u
                    .text(prompt)
                    .media(medias.toArray(Media[]::new)));
        } else {
            request.user(prompt);
        }

        return request
                .advisors(a -> a.param(CONVERSATION_ID, chatId))
                .stream()
                .content();
    }
}
