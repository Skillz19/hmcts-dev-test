package uk.gov.hmcts.reform.dev.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import uk.gov.hmcts.reform.dev.api.TaskMapper;
import uk.gov.hmcts.reform.dev.api.TaskRequest;
import uk.gov.hmcts.reform.dev.api.TaskResponse;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.services.TaskService;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(TaskMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        List<TaskResponse> tasks = taskService.getAllTasks()
                .stream()
                .map(TaskMapper::toResponse)
                .toList();
        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        Task created = taskService.createTask(TaskMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskMapper.toResponse(created));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTaskStatus(@PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        Task entity = TaskMapper.toEntity(request);
        entity.setId(id);
        Task updated = taskService.updateTask(entity);
        return ResponseEntity.ok(TaskMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
