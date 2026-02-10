package uk.gov.hmcts.reform.dev.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.hmcts.reform.dev.exceptions.InvalidTaskStateException;
import uk.gov.hmcts.reform.dev.exceptions.TaskNotFoundException;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.TaskStatus;
import uk.gov.hmcts.reform.dev.repository.TaskRepository;

@Service
@Transactional
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Optional<Task> getTaskById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Task id must not be null");
        }
        return taskRepository.findById(id);
    }

    public Task createTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task must not be null");
        }
        // Validate task fields
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Task title must not be null or empty");
        }
        if (task.getStatus() == null) {
            throw new IllegalArgumentException("Task status must not be null");
        }
        if (task.getDueDate() == null) {
            throw new IllegalArgumentException("Task due date must not be null");
        }

        return taskRepository.save(task);
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task updateTask(Task updatedTask) {
        if (updatedTask == null || updatedTask.getId() == null) {
            throw new IllegalArgumentException("Task id must not be null");
        }
        Long id = updatedTask.getId();
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        TaskStatus requestedStatus = updatedTask.getStatus();
        if (existing.getStatus() == TaskStatus.COMPLETED
                && requestedStatus != null
                && requestedStatus != TaskStatus.COMPLETED) {
            throw new InvalidTaskStateException("Cannot move task from COMPLETED to another state");
        }

        existing.setTitle(Optional.ofNullable(updatedTask.getTitle())
                .filter(t -> !t.trim().isEmpty())
                .orElse(existing.getTitle()));
        existing.setDescription(Optional.ofNullable(updatedTask.getDescription()).orElse(""));
        existing.setStatus(Optional.ofNullable(requestedStatus).orElse(existing.getStatus()));
        existing.setDueDate(Optional.ofNullable(updatedTask.getDueDate()).orElse(existing.getDueDate()));

        return taskRepository.save(existing);
    }

    public void deleteTask(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Task id must not be null");
        }
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }
}
