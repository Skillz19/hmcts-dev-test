package uk.gov.hmcts.reform.dev.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.hmcts.reform.dev.models.Task;
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
        Long id = updatedTask.getId();
        return taskRepository.findById(id).map(task -> {
            task.setTitle(updatedTask.getTitle());
            task.setDescription(updatedTask.getDescription());
            task.setStatus(updatedTask.getStatus());
            task.setDueDate(updatedTask.getDueDate());
            return taskRepository.save(task);
        }).orElseThrow(() -> new RuntimeException("Task not found with id " + id));
    }

    public void deleteTask(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Task id must not be null");
        }
        if (!taskRepository.existsById(id)) {
            throw new RuntimeException("Task not found with id " + id);
        }
        taskRepository.deleteById(id);
    }
}
