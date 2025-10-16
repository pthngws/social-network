package com.phithang.mysocialnetwork.service;

import com.phithang.mysocialnetwork.dto.request.TaskRequestDto;
import com.phithang.mysocialnetwork.dto.response.TaskResponseDto;
import com.phithang.mysocialnetwork.entity.Task;

import java.util.List;

public interface ITaskService {
    public List<TaskResponseDto> getTasks();
    public TaskResponseDto getTask(Long id);
    public TaskResponseDto addTask(TaskRequestDto task);
    public TaskResponseDto updateTask(Long taskId, TaskRequestDto task);
    public void deleteTask(Long id);
    public TaskResponseDto doneTask(Long taskId);

}
