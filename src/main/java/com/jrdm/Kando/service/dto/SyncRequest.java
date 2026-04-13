package com.jrdm.Kando.service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncRequest {

    // Timestamp del último sync exitoso (null = primer sync)
    private Instant lastSyncAt;

    @Valid
    @NotNull
    private List<TaskSyncItem> tasks;
}
