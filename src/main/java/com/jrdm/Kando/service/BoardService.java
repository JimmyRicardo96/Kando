package com.jrdm.Kando.service;

import com.jrdm.Kando.domain.enums.BoardRole;
import com.jrdm.Kando.service.dto.*;

import java.util.List;

public interface BoardService {

    BoardResponse createBoard(CreateBoardRequest req, String currentUserId);

    BoardResponse getBoard(String boardId, String currentUserId);

    List<BoardResponse> getMyBoards(String currentUserId);

    BoardResponse updateBoard(String boardId, UpdateBoardRequest req, String currentUserId);

    void deleteBoard(String boardId, String currentUserId);

    List<BoardMemberResponse> getMembers(String boardId, String currentUserId);

    BoardMemberResponse inviteMember(String boardId, InviteMemberRequest req, String currentUserId);

    BoardMemberResponse changeMemberRole(String boardId, String memberId, BoardRole newRole, String currentUserId);

    void removeMember(String boardId, String memberId, String currentUserId);
}
