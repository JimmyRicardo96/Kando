package com.jrdm.Kando.controller;

import com.jrdm.Kando.common.dto.ApiResponse;
import com.jrdm.Kando.domain.enums.BoardRole;
import com.jrdm.Kando.service.BoardService;
import com.jrdm.Kando.service.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    public ResponseEntity<ApiResponse<BoardResponse>> create(
            @Valid @RequestBody CreateBoardRequest req,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(boardService.createBoard(req, userId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BoardResponse>>> getMyBoards(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.ok(boardService.getMyBoards(userId)));
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<ApiResponse<BoardResponse>> getBoard(
            @PathVariable String boardId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.ok(boardService.getBoard(boardId, userId)));
    }

    @PatchMapping("/{boardId}")
    public ResponseEntity<ApiResponse<BoardResponse>> updateBoard(
            @PathVariable String boardId,
            @Valid @RequestBody UpdateBoardRequest req,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.ok(boardService.updateBoard(boardId, req, userId)));
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<ApiResponse<Void>> deleteBoard(
            @PathVariable String boardId,
            @AuthenticationPrincipal String userId) {
        boardService.deleteBoard(boardId, userId);
        return ResponseEntity.ok(ApiResponse.message("Board deleted"));
    }

    // ── Members ───────────────────────────────────────────────────

    @GetMapping("/{boardId}/members")
    public ResponseEntity<ApiResponse<List<BoardMemberResponse>>> getMembers(
            @PathVariable String boardId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.ok(boardService.getMembers(boardId, userId)));
    }

    @PostMapping("/{boardId}/members")
    public ResponseEntity<ApiResponse<BoardMemberResponse>> inviteMember(
            @PathVariable String boardId,
            @Valid @RequestBody InviteMemberRequest req,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(boardService.inviteMember(boardId, req, userId)));
    }

    @PatchMapping("/{boardId}/members/{memberId}/role")
    public ResponseEntity<ApiResponse<BoardMemberResponse>> changeMemberRole(
            @PathVariable String boardId,
            @PathVariable String memberId,
            @RequestParam BoardRole role,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.ok(boardService.changeMemberRole(boardId, memberId, role, userId)));
    }

    @DeleteMapping("/{boardId}/members/{memberId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable String boardId,
            @PathVariable String memberId,
            @AuthenticationPrincipal String userId) {
        boardService.removeMember(boardId, memberId, userId);
        return ResponseEntity.ok(ApiResponse.message("Member removed"));
    }
}
