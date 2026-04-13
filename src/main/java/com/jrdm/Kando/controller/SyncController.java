package com.jrdm.Kando.controller;

import com.jrdm.Kando.common.dto.ApiResponse;
import com.jrdm.Kando.service.SyncService;
import com.jrdm.Kando.service.dto.SyncRequest;
import com.jrdm.Kando.service.dto.SyncResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    /**
     * POST /api/sync/{boardId}
     *
     * El cliente envía sus cambios locales y recibe los cambios del servidor
     * desde su último sync. Los conflictos se devuelven para resolución en cliente.
     */
    @PostMapping("/{boardId}")
    public ResponseEntity<ApiResponse<SyncResponse>> sync(
            @PathVariable String boardId,
            @Valid @RequestBody SyncRequest req,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.ok(syncService.sync(boardId, req, userId)));
    }
}
