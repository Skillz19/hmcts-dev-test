package uk.gov.hmcts.reform.dev.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import uk.gov.hmcts.reform.dev.api.SortDirection;
import uk.gov.hmcts.reform.dev.api.TaskMapper;
import uk.gov.hmcts.reform.dev.api.TaskRequest;
import uk.gov.hmcts.reform.dev.api.TaskResponse;
import uk.gov.hmcts.reform.dev.api.TaskSortBy;
import uk.gov.hmcts.reform.dev.api.TaskUpdateRequest;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.services.TaskService;
import uk.gov.hmcts.reform.dev.api.TaskPageResponse;

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
    public ResponseEntity<TaskPageResponse> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "ID") TaskSortBy sortBy,
            @RequestParam(defaultValue = "ASC") SortDirection direction) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }

        Sort sort = Sort.by(direction.toSpringDirection(), sortBy.entityField());

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TaskResponse> taskPage = taskService.getAllTasks(pageable).map(TaskMapper::toResponse);

        TaskPageResponse response = new TaskPageResponse(
                taskPage.getContent(),
                taskPage.getNumber(),
                taskPage.getSize(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages(),
                taskPage.isFirst(),
                taskPage.isLast());

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        Task created = taskService.createTask(TaskMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskMapper.toResponse(created));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTaskStatus(@PathVariable Long id,
            @Valid @RequestBody TaskUpdateRequest request) {
        if (request.title() == null
                && request.description() == null
                && request.status() == null
                && request.dueDate() == null) {
            throw new IllegalArgumentException("At least one field must be provided for patch update");
        }
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
