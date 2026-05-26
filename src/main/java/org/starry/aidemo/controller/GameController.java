package org.starry.aidemo.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class GameController {

    private final ChatClient gameChatClient;

    @RequestMapping(value = "/game", produces = "text/plain;charset=utf-8")
    public Flux<String> chat(String prompt, String chatId) {
        if (!StringUtils.hasText(prompt)) {
            throw new ResponseStatusException
                    (HttpStatus.BAD_REQUEST, "Sorry, the prompt can not be null");
        }

        return gameChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CONVERSATION_ID, chatId))
                .stream()
                .content();
    }
}
