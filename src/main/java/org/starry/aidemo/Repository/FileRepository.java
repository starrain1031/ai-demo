package org.starry.aidemo.Repository;

import org.springframework.core.io.Resource;

public interface FileRepository {

    boolean save(String chatId, Resource resource);

    Resource getFile(String chatId);

    String getFileKey(String chatId);
}