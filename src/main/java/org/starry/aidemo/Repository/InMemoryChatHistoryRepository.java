package org.starry.aidemo.Repository;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryChatHistoryRepository implements ChatHistoryRepository{
    private final Map<String, List<String>> chatHistory = new HashMap<>();

    @Override
    public void save(String type, String chatId) {
        if(!chatHistory.containsKey(type)){
            List<String> newList = new ArrayList<>();
            newList.add(chatId);
            chatHistory.put(type, newList);
        }
        else {
            List<String> chatIds = chatHistory.get(type);
            if(!chatIds.contains(chatId)){
                chatIds.add(chatId);
            }
        }
    }

    @Override
    public List<String> getChatIds(String type) {
        return chatHistory.containsKey(type) ? chatHistory.get(type) : List.of();

    }
}
