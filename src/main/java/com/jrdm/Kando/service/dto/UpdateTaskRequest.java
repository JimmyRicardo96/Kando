package com.jrdm.Kando.service.dto;

import com.jrdm.Kando.domain.enums.Priority;
import com.jrdm.Kando.domain.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {

    @Size(min = 1, max = 200)
    private String title;

    @Size(max = 10000)
    private String description;

    private TaskStatus status;

    private String positionIndex;

    private Priority priority;

    private Instant dueDate;

    private String assigneeId;

    // Requerido para optimistic locking
    @NotNull
    private Long version;
}
