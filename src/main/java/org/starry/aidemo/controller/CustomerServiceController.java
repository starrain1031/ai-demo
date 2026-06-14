package org.starry.aidemo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.starry.aidemo.Repository.ChatHistoryRepository;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

/**
 * Provides course consultation chat backed by Spring AI tool calling.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class CustomerServiceController {
    private final ChatClient serviceChatClient;

    private final ChatHistoryRepository chatHistoryRepository;

    /**
     * Streams a course-service assistant response.
     *
     * @param prompt user question or booking request
     * @param chatId conversation identifier used by chat memory
     * @return streamed response text from the customer-service assistant
     */
    @RequestMapping(value = "/service", produces = "text/plain;charset=utf-8")
    public Flux<String> service(String prompt, String chatId) {
        chatHistoryRepository.save("service", chatId);
        return serviceChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CONVERSATION_ID, chatId))
                .stream()
                .content();
    }
}
