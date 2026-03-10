package com.jrdm.Kando.domain.model;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import com.jrdm.Kando.domain.enums.BoardRole;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "board_users",
        indexes = {
                @Index(name = "idx_board_user_board_id", columnList = "board_id"),
                @Index(name = "idx_board_user_user_id", columnList = "user_id")
        },
        // Un usuario solo puede estar una vez en un tablero
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_board_user",
                        columnNames = {"board_id", "user_id"}
                )
        }
)
@EntityListeners(AuditingEntityListener.class)
public class BoardUser {

    @Id
    @Column(length = 26, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false, updatable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    // OWNER, ADMIN, MEMBER, VIEWER
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BoardRole role;

    @CreatedDate
    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    protected BoardUser() {
        // Requerido por JPA
    }

    public static BoardUser create(Board board, User user, BoardRole role) {
        Objects.requireNonNull(board, "board is required");
        Objects.requireNonNull(user, "user is required");
        Objects.requireNonNull(role, "role is required");

        BoardUser bu = new BoardUser();
        bu.board = board;
        bu.user = user;
        bu.role = role;

        return bu;
    }

    @PrePersist
    private void prePersist() {
        if (this.id == null) {
            this.id = UlidCreator.getUlid().toString();
        }
    }

    public void changeRole(BoardRole newRole) {
        Objects.requireNonNull(newRole, "role is required");
        this.role = newRole;
    }

    public boolean isOwner() {
        return this.role == BoardRole.OWNER;
    }

    public String getId() {
        return id;
    }

    public Board getBoard() {
        return board;
    }

    public User getUser() {
        return user;
    }

    public BoardRole getRole() {
        return role;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

}