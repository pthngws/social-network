package com.phithang.mysocialnetwork.service.Impl;

import com.phithang.mysocialnetwork.dto.request.TaskRequestDto;
import com.phithang.mysocialnetwork.dto.response.TaskResponseDto;
import com.phithang.mysocialnetwork.entity.Task;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.exception.AppException;
import com.phithang.mysocialnetwork.exception.ErrorCode;
import com.phithang.mysocialnetwork.repository.TaskRepository;
import com.phithang.mysocialnetwork.repository.UserRepository;
import com.phithang.mysocialnetwork.service.ITaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService implements ITaskService {

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    UserRepository userRepository;

    @Override
    public List<TaskResponseDto> getTasks() {
        Long userId  = getCurrentUserId();
        return mapToTaskResponse(taskRepository.findByUserId(userId));
    }

    @Override
    public TaskResponseDto getTask(Long id) {
        return mapToTaskResponse(taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id)));
    }


    @Override
    public TaskResponseDto addTask(TaskRequestDto task) {
        Long userId  = getCurrentUserId();
        Task taskEntity = new Task();
        taskEntity.setUserId(userId);
        taskEntity.setTitle(task.getTitle());
        taskEntity.setDescription(task.getDescription());
        taskEntity.setCompleted(false);
        taskEntity.setStartDate(task.getStartDate());
        taskEntity.setEndDate(task.getEndDate());
        taskEntity.setPriority(task.getPriority());
        taskEntity.setProgress(task.getProgress());
        return mapToTaskResponse(taskRepository.save(taskEntity));
    }

    @Override
    public TaskResponseDto updateTask(Long taskId, TaskRequestDto task) {
        Task taskEntity = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        taskEntity.setTitle(task.getTitle());
        taskEntity.setDescription(task.getDescription());
        taskEntity.setStartDate(task.getStartDate());
        taskEntity.setEndDate(task.getEndDate());
        taskEntity.setPriority(task.getPriority());
        taskEntity.setProgress(task.getProgress());
        taskEntity.setCompleted(taskEntity.isCompleted());
        return mapToTaskResponse(taskRepository.save(taskEntity));
    }

    @Override
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    @Override
    public TaskResponseDto doneTask(Long taskId) {
        Task taskEntity = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        if(taskEntity.getUserId().equals(getCurrentUserId())) {
            taskEntity.setCompleted(true);
        }
        return mapToTaskResponse(taskRepository.save(taskEntity));
    }
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String email = authentication.getName();
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return userEntity.getId();
    }

    private List<TaskResponseDto> mapToTaskResponse(List<Task> tasks) {
        return tasks.stream()
                .map(this::mapToTaskResponse) // gọi từng task → dto
                .collect(Collectors.toList());
    }


    public TaskResponseDto mapToTaskResponse(Task task) {
        TaskResponseDto response = new TaskResponseDto();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setCompleted(task.isCompleted());
        response.setStartDate(task.getStartDate());
        response.setEndDate(task.getEndDate());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        response.setPriority(task.getPriority());
        response.setProgress(task.getProgress());
        return response;
    }

}
