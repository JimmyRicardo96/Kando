package com.jrdm.Kando.controller;

import com.jrdm.Kando.common.dto.ApiResponse;
import com.jrdm.Kando.service.TaskService;
import com.jrdm.Kando.service.dto.CreateTaskRequest;
import com.jrdm.Kando.service.dto.TaskResponse;
import com.jrdm.Kando.service.dto.UpdateTaskRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> create(
            @Valid @RequestBody CreateTaskRequest req,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(taskService.createTask(req, userId)));
    }

    @GetMapping("/board/{boardId}")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getByBoard(
            @PathVariable String boardId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.ok(taskService.getTasksByBoard(boardId, userId)));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTask(
            @PathVariable String taskId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.ok(taskService.getTask(taskId, userId)));
    }

    @GetMapping("/{taskId}/subtasks")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getSubtasks(
            @PathVariable String taskId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.ok(taskService.getSubtasks(taskId, userId)));
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> update(
            @PathVariable String taskId,
            @Valid @RequestBody UpdateTaskRequest req,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.ok(taskService.updateTask(taskId, req, userId)));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String taskId,
            @AuthenticationPrincipal String userId) {
        taskService.deleteTask(taskId, userId);
        return ResponseEntity.ok(ApiResponse.message("Task deleted"));
    }
}
