package com.jrdm.Kando.service.mapper;

import com.jrdm.Kando.domain.model.Board;
import com.jrdm.Kando.domain.model.BoardUser;
import com.jrdm.Kando.service.dto.BoardMemberResponse;
import com.jrdm.Kando.service.dto.BoardResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BoardMapper {

    @Mapping(source = "owner.id",           target = "ownerId")
    @Mapping(source = "owner.displayName",  target = "ownerDisplayName")
    BoardResponse toResponse(Board board);

    List<BoardResponse> toResponseList(List<Board> boards);

    @Mapping(source = "id",                 target = "membershipId")
    @Mapping(source = "user.id",            target = "userId")
    @Mapping(source = "user.displayName",   target = "displayName")
    @Mapping(source = "user.email",         target = "email")
    BoardMemberResponse toMemberResponse(BoardUser boardUser);

    List<BoardMemberResponse> toMemberResponseList(List<BoardUser> members);
}
