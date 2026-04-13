package com.jrdm.Kando.service.mapper;

import com.jrdm.Kando.domain.model.Task;
import com.jrdm.Kando.service.dto.TaskResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(source = "board.id",           target = "boardId")
    @Mapping(source = "parent.id",          target = "parentId")
    @Mapping(source = "assignee.id",        target = "assigneeId")
    @Mapping(source = "assignee.displayName", target = "assigneeDisplayName")
    TaskResponse toResponse(Task task);

    List<TaskResponse> toResponseList(List<Task> tasks);
}
