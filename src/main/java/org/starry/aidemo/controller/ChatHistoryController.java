package org.starry.aidemo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.starry.aidemo.Repository.ChatHistoryRepository;
import org.starry.aidemo.entity.vo.MessageVO;

import java.util.List;

/**
 * Provides conversation id lists and message history for supported chat types.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai/history")
public class ChatHistoryController {
    private final ChatHistoryRepository chatHistoryRepository;

    private final ChatMemory chatMemory;

    /**
     * Lists conversation ids recorded for a chat type.
     *
     * @param type chat type, such as chat, pdf, service, or video
     * @return conversation ids for the requested type
     */
    @RequestMapping("/{type}")
    public List<String> getChatIds(@PathVariable String type) {
        return chatHistoryRepository.getChatIds(type);
    }

    /**
     * Reads the messages stored in chat memory for a conversation.
     *
     * @param type chat type path segment; currently used for route grouping
     * @param chatId conversation identifier
     * @return messages converted to frontend-friendly view objects
     */
    @RequestMapping("/{type}/{chatId}")
    public List<MessageVO> getChatHistory(@PathVariable String type, @PathVariable String chatId) {
        if (!StringUtils.hasText(chatId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "chatId can not be empty");
        }
        List<Message> messages = chatMemory.get(chatId);
        return messages.stream().map(MessageVO::new).toList();
    }

}
