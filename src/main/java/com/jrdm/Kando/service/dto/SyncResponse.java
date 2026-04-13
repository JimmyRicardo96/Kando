package com.jrdm.Kando.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncResponse {

    // Tareas del servidor modificadas desde lastSyncAt
    private List<TaskResponse> serverTasks;

    // Conflictos detectados (el cliente decide cómo resolverlos)
    private List<ConflictPayload> conflicts;

    // Timestamp del servidor para usar como próximo lastSyncAt
    private Instant syncedAt;
}
