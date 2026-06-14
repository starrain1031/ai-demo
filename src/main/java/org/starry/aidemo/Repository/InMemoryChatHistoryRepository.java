package org.starry.aidemo.Repository;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryChatHistoryRepository implements ChatHistoryRepository{
    private final Map<String, Set<String>> chatHistory = new ConcurrentHashMap<>();

    @Override
    public void save(String type, String chatId) {
        chatHistory
                .computeIfAbsent(type, key -> ConcurrentHashMap.newKeySet())
                .add(chatId);
    }

    @Override
    public List<String> getChatIds(String type) {
        return new ArrayList<>(chatHistory.getOrDefault(type, Set.of()));
    }
}
