package org.starry.aidemo.Repository;

import org.springframework.core.io.Resource;

/**
 * Stores and resolves files associated with chat conversations.
 */
public interface FileRepository {

    /**
     * Saves a file resource for a conversation.
     *
     * @param chatId conversation identifier
     * @param resource file resource to persist
     * @return true when the file and related metadata were saved
     */
    boolean save(String chatId, Resource resource);

    /**
     * Resolves the file resource for a conversation.
     *
     * @param chatId conversation identifier
     * @return stored file resource, or a non-existing resource when no file is mapped
     */
    Resource getFile(String chatId);

    /**
     * Returns the vector-store file key for a conversation.
     *
     * @param chatId conversation identifier
     * @return file key used in vector metadata, or null when no file is mapped
     */
    String getFileKey(String chatId);
}
