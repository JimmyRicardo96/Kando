package com.jrdm.Kando.domain.model;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "boards",
        indexes = {
                @Index(name = "idx_board_deleted_at", columnList = "deleted_at"),
                @Index(name = "idx_board_updated_at", columnList = "updated_at")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class Board {

    @Id
    @Column(length = 26, nullable = false, updatable = false)
    private String id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    //(referencia directa al User)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, updatable = false)
    private User owner;

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

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected Board() {
        // Requerido por JPA
    }

    public static Board create(String name, String description, User owner) {
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(owner, "owner is required");

        Board board = new Board();
        board.name = name.trim();
        board.description = description;
        board.owner = owner;

        return board;
    }

    @PrePersist
    private void prePersist() {
        if (this.id == null) {
            this.id = UlidCreator.getUlid().toString();
        }
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public void rename(String newName) {
        Objects.requireNonNull(newName, "name is required");
        this.name = newName.trim();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public User getOwner() {
        return owner;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
