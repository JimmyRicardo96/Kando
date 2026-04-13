package com.jrdm.Kando.service.mapper;

import com.jrdm.Kando.domain.model.Task;
import com.jrdm.Kando.service.dto.TaskResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(source = "board.id",  target = "boardId")
    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(target = "assigneeId",
             expression = "java(task.getAssignee() != null ? task.getAssignee().getId() : null)")
    @Mapping(target = "assigneeDisplayName",
             expression = "java(task.getAssignee() != null ? task.getAssignee().getDisplayName() : null)")
    TaskResponse toResponse(Task task);

    List<TaskResponse> toResponseList(List<Task> tasks);
}
