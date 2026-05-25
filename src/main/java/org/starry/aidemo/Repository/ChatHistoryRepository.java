package org.starry.aidemo.Repository;

import java.util.List;

public interface ChatHistoryRepository {
    void save(String type, String chatId);

    List<String> getChatIds(String type);


}
