package com.jrdm.Kando.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictPayload {

    private String taskId;
    private TaskSyncItem clientItem;
    private TaskResponse serverTask;
    private String reason;
}
