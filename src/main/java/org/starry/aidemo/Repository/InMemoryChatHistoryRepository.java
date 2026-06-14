package org.starry.aidemo.Repository;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory implementation of {@link ChatHistoryRepository}.
 */
@Component
public class InMemoryChatHistoryRepository implements ChatHistoryRepository{
    private final Map<String, Set<String>> chatHistory = new ConcurrentHashMap<>();

    /**
     * Records a chat id in a concurrent set for the given type.
     */
    @Override
    public void save(String type, String chatId) {
        chatHistory
                .computeIfAbsent(type, key -> ConcurrentHashMap.newKeySet())
                .add(chatId);
    }

    /**
     * Returns a snapshot of known chat ids for the given type.
     */
    @Override
    public List<String> getChatIds(String type) {
        return new ArrayList<>(chatHistory.getOrDefault(type, Set.of()));
    }
}
