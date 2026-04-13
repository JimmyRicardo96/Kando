package com.jrdm.Kando.service;

import com.jrdm.Kando.common.exception.ForbiddenException;
import com.jrdm.Kando.common.exception.NotFoundException;
import com.jrdm.Kando.domain.enums.BoardRole;
import com.jrdm.Kando.domain.model.Board;
import com.jrdm.Kando.domain.model.BoardUser;
import com.jrdm.Kando.domain.model.User;
import com.jrdm.Kando.repository.BoardRepository;
import com.jrdm.Kando.repository.BoardUserRepository;
import com.jrdm.Kando.repository.UserRepository;
import com.jrdm.Kando.service.dto.*;
import com.jrdm.Kando.service.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final BoardUserRepository boardUserRepository;
    private final UserRepository userRepository;
    private final BoardMapper boardMapper;

    @Override
    @Transactional
    public BoardResponse createBoard(CreateBoardRequest req, String currentUserId) {
        User owner = loadUser(currentUserId);
        Board board = Board.create(req.getName(), req.getDescription(), owner);
        board = boardRepository.save(board);

        BoardUser membership = BoardUser.create(board, owner, BoardRole.OWNER);
        boardUserRepository.save(membership);

        return boardMapper.toResponse(board);
    }

    @Override
    @Transactional(readOnly = true)
    public BoardResponse getBoard(String boardId, String currentUserId) {
        Board board = loadBoardWithAccess(boardId, currentUserId);
        return boardMapper.toResponse(board);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardResponse> getMyBoards(String currentUserId) {
        List<Board> boards = boardRepository.findAllByMemberId(currentUserId);
        return boardMapper.toResponseList(boards);
    }

    @Override
    @Transactional
    public BoardResponse updateBoard(String boardId, UpdateBoardRequest req, String currentUserId) {
        requireRole(boardId, currentUserId, BoardRole.ADMIN);
        Board board = loadBoard(boardId);

        if (req.getName() != null) board.rename(req.getName());

        return boardMapper.toResponse(boardRepository.save(board));
    }

    @Override
    @Transactional
    public void deleteBoard(String boardId, String currentUserId) {
        requireRole(boardId, currentUserId, BoardRole.OWNER);
        Board board = loadBoard(boardId);
        board.softDelete();
        boardRepository.save(board);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardMemberResponse> getMembers(String boardId, String currentUserId) {
        loadBoardWithAccess(boardId, currentUserId);
        List<BoardUser> members = boardUserRepository.findByBoardId(boardId);
        return boardMapper.toMemberResponseList(members);
    }

    @Override
    @Transactional
    public BoardMemberResponse inviteMember(String boardId, InviteMemberRequest req, String currentUserId) {
        requireRole(boardId, currentUserId, BoardRole.ADMIN);

        if (req.getRole() == BoardRole.OWNER) {
            throw new ForbiddenException("Cannot assign OWNER role via invitation");
        }
        if (boardUserRepository.existsByBoardIdAndUserId(boardId, req.getUserId())) {
            throw new IllegalArgumentException("User is already a member of this board");
        }

        Board board = loadBoard(boardId);
        User invitee = loadUser(req.getUserId());
        BoardUser membership = BoardUser.create(board, invitee, req.getRole());
        membership = boardUserRepository.save(membership);

        return boardMapper.toMemberResponse(membership);
    }

    @Override
    @Transactional
    public BoardMemberResponse changeMemberRole(String boardId, String memberId, BoardRole newRole, String currentUserId) {
        requireRole(boardId, currentUserId, BoardRole.ADMIN);

        BoardUser membership = boardUserRepository.findById(memberId)
                .filter(bu -> bu.getBoard().getId().equals(boardId))
                .orElseThrow(() -> new NotFoundException("Membership not found"));

        if (membership.getRole() == BoardRole.OWNER) {
            throw new ForbiddenException("Cannot change role of board OWNER");
        }
        if (newRole == BoardRole.OWNER) {
            throw new ForbiddenException("Cannot assign OWNER role");
        }

        membership.changeRole(newRole);
        return boardMapper.toMemberResponse(boardUserRepository.save(membership));
    }

    @Override
    @Transactional
    public void removeMember(String boardId, String memberId, String currentUserId) {
        requireRole(boardId, currentUserId, BoardRole.ADMIN);

        BoardUser membership = boardUserRepository.findById(memberId)
                .filter(bu -> bu.getBoard().getId().equals(boardId))
                .orElseThrow(() -> new NotFoundException("Membership not found"));

        if (membership.getRole() == BoardRole.OWNER) {
            throw new ForbiddenException("Cannot remove the board OWNER");
        }

        boardUserRepository.delete(membership);
    }

    // ── Helpers ───────────────────────────────────────────────────

    private Board loadBoard(String boardId) {
        return boardRepository.findByIdAndDeletedAtIsNull(boardId)
                .orElseThrow(() -> new NotFoundException("Board not found: " + boardId));
    }

    private Board loadBoardWithAccess(String boardId, String userId) {
        Board board = loadBoard(boardId);
        if (!boardUserRepository.existsByBoardIdAndUserId(boardId, userId)) {
            throw new ForbiddenException("Access denied to board: " + boardId);
        }
        return board;
    }

    private User loadUser(String userId) {
        return userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private void requireRole(String boardId, String userId, BoardRole minimumRole) {
        BoardUser membership = boardUserRepository.findByBoardIdAndUserId(boardId, userId)
                .orElseThrow(() -> new ForbiddenException("Access denied to board: " + boardId));

        if (!hasMinimumRole(membership.getRole(), minimumRole)) {
            throw new ForbiddenException("Insufficient role. Required: " + minimumRole);
        }
    }

    private boolean hasMinimumRole(BoardRole actual, BoardRole minimum) {
        // Orden: OWNER > ADMIN > MEMBER > VIEWER
        int[] rank = {0, 0, 0, 0};
        BoardRole[] ordered = {BoardRole.VIEWER, BoardRole.MEMBER, BoardRole.ADMIN, BoardRole.OWNER};
        int actualRank = 0, minRank = 0;
        for (int i = 0; i < ordered.length; i++) {
            if (ordered[i] == actual) actualRank = i;
            if (ordered[i] == minimum) minRank = i;
        }
        return actualRank >= minRank;
    }
}
