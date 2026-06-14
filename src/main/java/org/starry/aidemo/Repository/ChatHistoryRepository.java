package org.starry.aidemo.Repository;

import java.util.List;

/**
 * Stores conversation ids grouped by chat type.
 */
public interface ChatHistoryRepository {

    /**
     * Records a conversation id under the given chat type.
     *
     * @param type chat type, such as chat, pdf, service, or video
     * @param chatId conversation identifier
     */
    void save(String type, String chatId);

    /**
     * Returns all known conversation ids for a chat type.
     *
     * @param type chat type
     * @return conversation ids for the type, or an empty list if none exist
     */
    List<String> getChatIds(String type);
}
