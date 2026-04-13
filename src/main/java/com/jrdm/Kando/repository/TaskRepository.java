package com.jrdm.Kando.repository;

import com.jrdm.Kando.domain.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {

    Optional<Task> findByIdAndDeletedAtIsNull(String id);

    List<Task> findByBoardIdAndDeletedAtIsNullOrderByPositionIndex(String boardId);

    // Solo tareas raíz (sin padre)
    List<Task> findByBoardIdAndParentIsNullAndDeletedAtIsNullOrderByPositionIndex(String boardId);

    // Subtareas directas
    List<Task> findByParentIdAndDeletedAtIsNullOrderByPositionIndex(String parentId);

    // Delta sync: incluye soft-deleted para que el cliente pueda eliminar localmente
    @Query("""
            SELECT t FROM Task t
            WHERE t.board.id = :boardId
            AND t.updatedAt > :since
            """)
    List<Task> findByBoardIdAndUpdatedAtAfter(@Param("boardId") String boardId,
                                              @Param("since") Instant since);

    // Subtree por materialized path (todos los descendientes)
    @Query("SELECT t FROM Task t WHERE t.path LIKE :pathPrefix% AND t.deletedAt IS NULL")
    List<Task> findSubtree(@Param("pathPrefix") String pathPrefix);

    boolean existsByIdAndDeletedAtIsNull(String id);
}
