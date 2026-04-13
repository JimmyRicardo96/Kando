package com.jrdm.Kando.service.dto;

import com.jrdm.Kando.domain.enums.Priority;
import com.jrdm.Kando.domain.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private String id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private String positionIndex;
    private Instant dueDate;
    private String boardId;
    private String parentId;
    private String path;
    private String assigneeId;
    private String assigneeDisplayName;
    private Instant completedAt;
    private Instant deletedAt;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
}
