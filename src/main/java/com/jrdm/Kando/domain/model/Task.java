package com.jrdm.Kando.domain.model;

import com.github.f4b6a3.ulid.UlidCreator;
import com.jrdm.Kando.domain.enums.Priority;
import com.jrdm.Kando.domain.enums.TaskStatus;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.*;

@Entity
@Table(
        name = "tasks",
        indexes = {
                @Index(name = "idx_task_board_id",    columnList = "board_id"),
                @Index(name = "idx_task_parent_id",   columnList = "parent_id"),
                @Index(name = "idx_task_path",        columnList = "path"),
                @Index(name = "idx_task_deleted_at",  columnList = "deleted_at"),
                @Index(name = "idx_task_updated_at",  columnList = "updated_at"),
                @Index(name = "idx_task_status",      columnList = "status")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class Task {

    @Id
    @Column(length = 26, updatable = false, nullable = false)
    private String id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Priority priority;

    // LexoRank / Fractional Indexing
    @Column(name = "position_index", nullable = false, length = 50)
    private String positionIndex;

    @Column(name = "due_date")
    private Instant dueDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    // Jerarquía recursiva
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Task parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private Set<Task> subtasks = new LinkedHashSet<>();

    // Materialized Path: "ROOT_ID.CHILD_ID.SUBCHILD_ID."
    @Column(nullable = false, length = 1024)
    private String path;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    // Optimistic Locking: detecta conflictos de edición concurrente
    @Version
    @Column(nullable = false)
    private Long version;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 26)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 26)
    private String updatedBy;

    protected Task() {
        // Requerido por JPA
    }

    public static Task create(
            String title,
            String description,
            Board board,
            Task parent,
            String positionIndex
    ) {
        Objects.requireNonNull(title, "title is required");
        Objects.requireNonNull(board, "board is required");
        Objects.requireNonNull(positionIndex, "positionIndex is required");

        Task task = new Task();
        task.title = title.trim();
        task.description = description;
        task.board = board;
        task.parent = parent;
        task.positionIndex = positionIndex;
        task.status = TaskStatus.TODO;
        task.priority = Priority.MEDIUM;

        return task;
    }

    @PrePersist
    private void prePersist() {
        if (this.id == null) {
            this.id = UlidCreator.getUlid().toString();
        }
        if (this.parent == null) {
            this.path = this.id + ".";
        } else {
            this.path = this.parent.getPath() + this.id + ".";
        }
    }

    // ── Lógica de negocio ──────────────────────────────────────────

    public void moveToStatus(TaskStatus newStatus) {
        Objects.requireNonNull(newStatus);
        this.status = newStatus;
        this.completedAt = (newStatus == TaskStatus.DONE) ? Instant.now() : null;
    }

    public void update(String title, String description, String positionIndex,
                       Priority priority, Instant dueDate, User assignee) {
        if (title != null && !title.isBlank()) this.title = title.trim();
        if (description != null) this.description = description;
        if (positionIndex != null && !positionIndex.isBlank()) this.positionIndex = positionIndex;
        if (priority != null) this.priority = priority;
        this.dueDate = dueDate;
        this.assignee = assignee;
    }

    // Actualiza solo los campos opcionales (útil en flujos de creación y sync)
    public void updateOptionalFields(Priority priority, Instant dueDate, User assignee) {
        if (priority != null) this.priority = priority;
        this.dueDate = dueDate;
        this.assignee = assignee;
    }

    public void updatePath(String newPath) {
        Objects.requireNonNull(newPath, "path is required");
        this.path = newPath;
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TaskStatus getStatus() { return status; }
    public Priority getPriority() { return priority; }
    public String getPositionIndex() { return positionIndex; }
    public Instant getDueDate() { return dueDate; }
    public Board getBoard() { return board; }
    public User getAssignee() { return assignee; }
    public Task getParent() { return parent; }
    public Set<Task> getSubtasks() { return subtasks; }
    public String getPath() { return path; }
    public Instant getCompletedAt() { return completedAt; }
    public Instant getDeletedAt() { return deletedAt; }
    public Long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }
}
