package com.jrdm.Kando.repository;

import com.jrdm.Kando.domain.enums.BoardRole;
import com.jrdm.Kando.domain.model.BoardUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardUserRepository extends JpaRepository<BoardUser, String> {

    Optional<BoardUser> findByBoardIdAndUserId(String boardId, String userId);

    List<BoardUser> findByBoardId(String boardId);

    List<BoardUser> findByUserId(String userId);

    boolean existsByBoardIdAndUserId(String boardId, String userId);

    boolean existsByBoardIdAndUserIdAndRoleIn(String boardId, String userId, List<BoardRole> roles);
}
