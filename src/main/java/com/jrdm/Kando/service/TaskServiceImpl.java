package com.jrdm.Kando.service;

import com.jrdm.Kando.common.exception.ConflictException;
import com.jrdm.Kando.common.exception.ForbiddenException;
import com.jrdm.Kando.common.exception.NotFoundException;
import com.jrdm.Kando.domain.enums.BoardRole;
import com.jrdm.Kando.domain.model.Board;
import com.jrdm.Kando.domain.model.Task;
import com.jrdm.Kando.domain.model.User;
import com.jrdm.Kando.repository.BoardRepository;
import com.jrdm.Kando.repository.BoardUserRepository;
import com.jrdm.Kando.repository.TaskRepository;
import com.jrdm.Kando.repository.UserRepository;
import com.jrdm.Kando.service.dto.CreateTaskRequest;
import com.jrdm.Kando.service.dto.TaskResponse;
import com.jrdm.Kando.service.dto.UpdateTaskRequest;
import com.jrdm.Kando.service.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final BoardRepository boardRepository;
    private final BoardUserRepository boardUserRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    @Override
    @Transactional
    public TaskResponse createTask(CreateTaskRequest req, String currentUserId) {
        requireMinRole(req.getBoardId(), currentUserId, BoardRole.MEMBER);

        Board board = boardRepository.findByIdAndDeletedAtIsNull(req.getBoardId())
                .orElseThrow(() -> new NotFoundException("Board not found: " + req.getBoardId()));

        Task parent = null;
        if (req.getParentId() != null) {
            parent = taskRepository.findByIdAndDeletedAtIsNull(req.getParentId())
                    .orElseThrow(() -> new NotFoundException("Parent task not found: " + req.getParentId()));
            if (!parent.getBoard().getId().equals(board.getId())) {
                throw new IllegalArgumentException("Parent task belongs to a different board");
            }
        }

        Task task = Task.create(req.getTitle(), req.getDescription(), board, parent, req.getPositionIndex());

        if (req.getPriority() != null) {
            task.update(null, null, null, req.getPriority(), req.getDueDate(), resolveAssignee(req.getAssigneeId()));
        } else if (req.getDueDate() != null || req.getAssigneeId() != null) {
            task.update(null, null, null, null, req.getDueDate(), resolveAssignee(req.getAssigneeId()));
        }

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTask(String taskId, String currentUserId) {
        Task task = loadTask(taskId);
        requireMinRole(task.getBoard().getId(), currentUserId, BoardRole.VIEWER);
        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByBoard(String boardId, String currentUserId) {
        requireMinRole(boardId, currentUserId, BoardRole.VIEWER);
        List<Task> tasks = taskRepository.findByBoardIdAndDeletedAtIsNullOrderByPositionIndex(boardId);
        return taskMapper.toResponseList(tasks);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getSubtasks(String parentTaskId, String currentUserId) {
        Task parent = loadTask(parentTaskId);
        requireMinRole(parent.getBoard().getId(), currentUserId, BoardRole.VIEWER);
        List<Task> subtasks = taskRepository.findByParentIdAndDeletedAtIsNullOrderByPositionIndex(parentTaskId);
        return taskMapper.toResponseList(subtasks);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(String taskId, UpdateTaskRequest req, String currentUserId) {
        Task task = loadTask(taskId);
        requireMinRole(task.getBoard().getId(), currentUserId, BoardRole.MEMBER);

        // Optimistic locking check a nivel de aplicación (antes del @Version de JPA)
        if (!task.getVersion().equals(req.getVersion())) {
            throw new ConflictException(
                    "Task was modified by another client. Please refresh and try again.",
                    taskMapper.toResponse(task)
            );
        }

        if (req.getStatus() != null) {
            task.moveToStatus(req.getStatus());
        }

        User assignee = req.getAssigneeId() != null ? resolveAssignee(req.getAssigneeId()) : null;
        task.update(req.getTitle(), req.getDescription(), req.getPositionIndex(),
                req.getPriority(), req.getDueDate(), assignee);

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public void deleteTask(String taskId, String currentUserId) {
        Task task = loadTask(taskId);
        requireMinRole(task.getBoard().getId(), currentUserId, BoardRole.MEMBER);
        task.softDelete();
        taskRepository.save(task);
    }

    // ── Helpers ───────────────────────────────────────────────────

    private Task loadTask(String taskId) {
        return taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found: " + taskId));
    }

    private User resolveAssignee(String assigneeId) {
        if (assigneeId == null) return null;
        return userRepository.findById(assigneeId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new NotFoundException("Assignee not found: " + assigneeId));
    }

    private void requireMinRole(String boardId, String userId, BoardRole minimum) {
        List<BoardRole> allowedRoles = switch (minimum) {
            case VIEWER -> List.of(BoardRole.VIEWER, BoardRole.MEMBER, BoardRole.ADMIN, BoardRole.OWNER);
            case MEMBER -> List.of(BoardRole.MEMBER, BoardRole.ADMIN, BoardRole.OWNER);
            case ADMIN  -> List.of(BoardRole.ADMIN, BoardRole.OWNER);
            case OWNER  -> List.of(BoardRole.OWNER);
        };
        boolean hasRole = boardUserRepository.existsByBoardIdAndUserIdAndRoleIn(boardId, userId, allowedRoles);
        if (!hasRole) {
            throw new ForbiddenException("Insufficient permissions on board: " + boardId);
        }
    }
}
