package com.jrdm.Kando.service;

import com.jrdm.Kando.service.dto.CreateTaskRequest;
import com.jrdm.Kando.service.dto.TaskResponse;
import com.jrdm.Kando.service.dto.UpdateTaskRequest;

import java.util.List;

public interface TaskService {

    TaskResponse createTask(CreateTaskRequest req, String currentUserId);

    TaskResponse getTask(String taskId, String currentUserId);

    List<TaskResponse> getTasksByBoard(String boardId, String currentUserId);

    List<TaskResponse> getSubtasks(String parentTaskId, String currentUserId);

    TaskResponse updateTask(String taskId, UpdateTaskRequest req, String currentUserId);

    void deleteTask(String taskId, String currentUserId);
}
