package com.jrdm.Kando.service;

import com.jrdm.Kando.common.exception.ForbiddenException;
import com.jrdm.Kando.common.exception.NotFoundException;
import com.jrdm.Kando.domain.enums.BoardRole;
import com.jrdm.Kando.domain.model.Board;
import com.jrdm.Kando.domain.model.BoardUser;
import com.jrdm.Kando.domain.model.Task;
import com.jrdm.Kando.domain.model.User;
import com.jrdm.Kando.repository.BoardRepository;
import com.jrdm.Kando.repository.BoardUserRepository;
import com.jrdm.Kando.repository.TaskRepository;
import com.jrdm.Kando.repository.UserRepository;
import com.jrdm.Kando.service.dto.ConflictPayload;
import com.jrdm.Kando.service.dto.SyncRequest;
import com.jrdm.Kando.service.dto.SyncResponse;
import com.jrdm.Kando.service.dto.TaskSyncItem;
import com.jrdm.Kando.service.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SyncServiceImpl implements SyncService {

    private final BoardRepository boardRepository;
    private final BoardUserRepository boardUserRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    @Override
    @Transactional
    public SyncResponse sync(String boardId, SyncRequest req, String currentUserId) {
        // 1. Verificar acceso al tablero
        BoardUser membership = boardUserRepository.findByBoardIdAndUserId(boardId, currentUserId)
                .orElseThrow(() -> new ForbiddenException("Access denied to board: " + boardId));

        Board board = boardRepository.findByIdAndDeletedAtIsNull(boardId)
                .orElseThrow(() -> new NotFoundException("Board not found: " + boardId));

        boolean canWrite = hasWriteAccess(membership.getRole());
        Instant syncedAt = Instant.now();

        // 2. Procesar cambios del cliente
        List<ConflictPayload> conflicts = new ArrayList<>();
        if (canWrite && req.getTasks() != null) {
            for (TaskSyncItem item : req.getTasks()) {
                processClientItem(item, board, currentUserId, conflicts);
            }
        }

        // 3. Obtener cambios del servidor desde el último sync
        Instant since = req.getLastSyncAt() != null ? req.getLastSyncAt() : Instant.EPOCH;
        List<Task> serverChanges = taskRepository.findByBoardIdAndUpdatedAtAfter(boardId, since);

        return SyncResponse.builder()
                .serverTasks(taskMapper.toResponseList(serverChanges))
                .conflicts(conflicts)
                .syncedAt(syncedAt)
                .build();
    }

    private void processClientItem(TaskSyncItem item, Board board, String currentUserId,
                                   List<ConflictPayload> conflicts) {
        switch (item.getOperation()) {
            case CREATE -> handleCreate(item, board, currentUserId);
            case UPDATE -> handleUpdate(item, conflicts);
            case DELETE -> handleDelete(item, conflicts);
        }
    }

    private void handleCreate(TaskSyncItem item, Board board, String currentUserId) {
        // Idempotente: si ya existe, lo tratamos como UPDATE
        Optional<Task> existing = taskRepository.findById(item.getId());
        if (existing.isPresent()) {
            // Ya existe: no pisar si el servidor tiene versión más nueva
            return;
        }

        Task parent = null;
        if (item.getParentId() != null) {
            parent = taskRepository.findByIdAndDeletedAtIsNull(item.getParentId()).orElse(null);
        }

        String title = item.getTitle() != null ? item.getTitle() : "Untitled";
        String positionIndex = item.getPositionIndex() != null ? item.getPositionIndex() : "m";

        Task task = Task.create(title, item.getDescription(), board, parent, positionIndex);
        task.update(null, null, null,
                item.getPriority(),
                item.getDueDate(),
                resolveAssignee(item.getAssigneeId()));

        if (item.getStatus() != null) {
            task.moveToStatus(item.getStatus());
        }

        taskRepository.save(task);
    }

    private void handleUpdate(TaskSyncItem item, List<ConflictPayload> conflicts) {
        Optional<Task> opt = taskRepository.findByIdAndDeletedAtIsNull(item.getId());
        if (opt.isEmpty()) return; // Tarea no existe en servidor, ignorar

        Task task = opt.get();

        // Verificar versión para detectar conflicto
        if (item.getVersion() != null && !item.getVersion().equals(task.getVersion())) {
            conflicts.add(ConflictPayload.builder()
                    .taskId(item.getId())
                    .clientItem(item)
                    .serverTask(taskMapper.toResponse(task))
                    .reason("Version mismatch: client=" + item.getVersion() + " server=" + task.getVersion())
                    .build());
            return;
        }

        if (item.getStatus() != null) task.moveToStatus(item.getStatus());
        task.update(item.getTitle(), item.getDescription(), item.getPositionIndex(),
                item.getPriority(), item.getDueDate(), resolveAssignee(item.getAssigneeId()));

        taskRepository.save(task);
    }

    private void handleDelete(TaskSyncItem item, List<ConflictPayload> conflicts) {
        Optional<Task> opt = taskRepository.findByIdAndDeletedAtIsNull(item.getId());
        if (opt.isEmpty()) return; // Ya borrada o no existe

        Task task = opt.get();

        if (item.getVersion() != null && !item.getVersion().equals(task.getVersion())) {
            conflicts.add(ConflictPayload.builder()
                    .taskId(item.getId())
                    .clientItem(item)
                    .serverTask(taskMapper.toResponse(task))
                    .reason("Cannot delete: version mismatch (task was modified after client's last sync)")
                    .build());
            return;
        }

        task.softDelete();
        taskRepository.save(task);
    }

    private boolean hasWriteAccess(BoardRole role) {
        return role == BoardRole.MEMBER || role == BoardRole.ADMIN || role == BoardRole.OWNER;
    }

    private User resolveAssignee(String assigneeId) {
        if (assigneeId == null) return null;
        return userRepository.findById(assigneeId).filter(u -> !u.isDeleted()).orElse(null);
    }
}
