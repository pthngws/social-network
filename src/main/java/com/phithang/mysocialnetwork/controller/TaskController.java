package com.phithang.mysocialnetwork.controller;


import com.phithang.mysocialnetwork.dto.request.TaskRequestDto;
import com.phithang.mysocialnetwork.dto.response.ApiResponse;
import com.phithang.mysocialnetwork.dto.response.TaskResponseDto;
import com.phithang.mysocialnetwork.service.ITaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    @Autowired
    private ITaskService taskService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponseDto>>> getTasks() {
        List<TaskResponseDto> tasks = taskService.getTasks();
        return ResponseEntity.ok(new ApiResponse<>(200,tasks,"Get all tasks successful!"));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponseDto>> getTask(@PathVariable Long taskId) {
        TaskResponseDto task = taskService.getTask(taskId);
        return ResponseEntity.ok(new ApiResponse<>(200,task,"Get task successful!"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponseDto>> addTask(@RequestBody TaskRequestDto taskRequestDto) {
        TaskResponseDto task = taskService.addTask(taskRequestDto);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.CREATED.value(), task,"Add task successful!"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponseDto>> updateTask(
            @PathVariable Long id,
            @RequestBody TaskRequestDto taskRequestDto
    ) {
        TaskResponseDto updatedTask = taskService.updateTask(id, taskRequestDto);
        return ResponseEntity.ok(new ApiResponse<>(200, updatedTask, "Update task successful!"));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<TaskResponseDto>> completeTask(@PathVariable Long id) {
        TaskResponseDto taskResponseDto = taskService.doneTask(id);
        return ResponseEntity.ok(new ApiResponse<>(200,taskResponseDto,"Complete task successful!"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
