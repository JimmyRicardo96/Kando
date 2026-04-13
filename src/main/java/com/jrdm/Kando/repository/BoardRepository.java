package com.jrdm.Kando.repository;

import com.jrdm.Kando.domain.model.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, String> {

    Optional<Board> findByIdAndDeletedAtIsNull(String id);

    // Tableros donde el usuario es miembro (via board_users)
    @Query("""
            SELECT b FROM Board b
            JOIN BoardUser bu ON bu.board = b
            WHERE bu.user.id = :userId
            AND b.deletedAt IS NULL
            """)
    List<Board> findAllByMemberId(@Param("userId") String userId);

    // Delta sync: tableros modificados desde lastSyncAt
    @Query("""
            SELECT b FROM Board b
            JOIN BoardUser bu ON bu.board = b
            WHERE bu.user.id = :userId
            AND b.updatedAt > :since
            """)
    List<Board> findByMemberAndUpdatedAtAfter(@Param("userId") String userId,
                                              @Param("since") Instant since);
}
