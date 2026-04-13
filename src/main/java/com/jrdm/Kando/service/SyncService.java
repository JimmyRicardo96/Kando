package com.jrdm.Kando.service;

import com.jrdm.Kando.service.dto.SyncRequest;
import com.jrdm.Kando.service.dto.SyncResponse;

public interface SyncService {

    SyncResponse sync(String boardId, SyncRequest req, String currentUserId);
}
