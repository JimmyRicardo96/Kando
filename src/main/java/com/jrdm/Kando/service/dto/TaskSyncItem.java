package com.jrdm.Kando.service.dto;

import com.jrdm.Kando.domain.enums.Priority;
import com.jrdm.Kando.domain.enums.SyncOperation;
import com.jrdm.Kando.domain.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSyncItem {

    @NotBlank
    private String id;

    @NotNull
    private SyncOperation operation;

    // Versión del cliente al momento de la edición (para optimistic locking)
    private Long version;

    // Campos de la tarea (relevantes según la operación)
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private String positionIndex;
    private Instant dueDate;
    private String parentId;
    private String assigneeId;
    private Instant deletedAt;
    private Instant clientUpdatedAt;
}
