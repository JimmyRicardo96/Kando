package com.jrdm.Kando.service.dto;

import com.jrdm.Kando.domain.enums.Priority;
import jakarta.validation.constraints.NotBlank;
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
public class CreateTaskRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 10000)
    private String description;

    @NotBlank
    private String boardId;

    // null = tarea raíz
    private String parentId;

    @NotBlank
    private String positionIndex;

    private Priority priority;

    private Instant dueDate;

    private String assigneeId;
}
