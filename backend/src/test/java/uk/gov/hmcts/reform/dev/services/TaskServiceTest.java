package uk.gov.hmcts.reform.dev.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.TaskStatus;
import uk.gov.hmcts.reform.dev.repository.TaskRepository;

import uk.gov.hmcts.reform.dev.exceptions.InvalidTaskStateException;
import uk.gov.hmcts.reform.dev.exceptions.TaskNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository repository;

    @InjectMocks
    private TaskService service;

    @Test
    void getTaskById_shouldReturnTaskWhenExists() {
        // Arrange
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setStatus(TaskStatus.PENDING);
        task.setDueDate(LocalDateTime.now());

        given(repository.findById(1L)).willReturn(Optional.of(task));

        // Act
        Task result = service.getTaskById(1L).orElseThrow();

        // Assert
        assertThat(result.getTitle()).isEqualTo("Test Task");
    }

    @Test
    void createTask_shouldSaveTask() {
        Task task = new Task();
        task.setTitle("New Task");
        task.setStatus(TaskStatus.PENDING);
        task.setDueDate(LocalDateTime.now().plusDays(1));

        given(repository.save(task)).willReturn(task);

        Task result = service.createTask(task);

        assertThat(result).isEqualTo(task);
    }

    @Test
    void createTask_shouldThrowWhenTaskIsNull() {
        Task task = null;

        assertThatThrownBy(() -> service.createTask(task))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task must not be null");
    }

    @Test
    void createTask_shouldThrowWhenTitleIsNull() {
        Task task = new Task();
        task.setTitle("");
        task.setStatus(TaskStatus.PENDING);
        task.setDueDate(LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> service.createTask(task))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task title must not be null or empty");
    }

    @Test
    void createTask_shouldThrowWhenStatusIsNull() {
        Task task = new Task();
        task.setTitle("Valid Title");
        task.setStatus(null);
        task.setDueDate(LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> service.createTask(task))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task status must not be null");
    }

    @Test
    void createTask_shouldThrowWhenDueDateIsNull() {
        Task task = new Task();
        task.setTitle("Valid Title");
        task.setStatus(TaskStatus.PENDING);
        task.setDueDate(null);

        assertThatThrownBy(() -> service.createTask(task))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task due date must not be null");
    }

    @Test
    void getAllTasks_shouldReturnAllTasks() {
        Task task1 = new Task();
        Task task2 = new Task();
        Pageable pageable = PageRequest.of(0, 20);
        Page<Task> page = new PageImpl<>(List.of(task1, task2), pageable, 2);
        given(repository.findAll(pageable)).willReturn(page);

        Page<Task> tasks = service.getAllTasks(pageable);

        assertThat(tasks.getTotalElements()).isEqualTo(2);
        assertThat(tasks.getContent()).containsExactly(task1, task2);
    }

    @Test
    void getAllTasks_shouldReturnPagedTasks() {
        Task task1 = new Task();
        Task task2 = new Task();
        Pageable pageable = PageRequest.of(0, 20);
        Page<Task> page = new PageImpl<>(List.of(task1, task2), pageable, 2);

        given(repository.findAll(pageable)).willReturn(page);

        Page<Task> result = service.getAllTasks(pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).containsExactly(task1, task2);
    }

    @Test
    void getAllTasks_shouldThrowWhenPageableIsNull() {
        assertThatThrownBy(() -> service.getAllTasks(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pageable must not be null");
    }

    @Test
    void updateTask_shouldSaveAndReturnTask() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Original");
        given(repository.findById(1L)).willReturn(Optional.of(task));
        given(repository.save(task)).willReturn(task);

        Task updated = service.updateTask(task);

        assertThat(updated).isEqualTo(task);
    }

    @Test
    void updateTask_shouldUpdateDescriptionOnly() {
        // Arrange
        Task existingTask = new Task();
        existingTask.setId(1L);
        existingTask.setTitle("Original Title");
        existingTask.setDescription("Original Description");
        existingTask.setStatus(TaskStatus.PENDING);
        existingTask.setDueDate(LocalDateTime.now().plusDays(1));

        Task updatedTask = new Task();
        updatedTask.setId(1L);
        updatedTask.setDescription("Updated Description"); // only description changes

        given(repository.findById(1L)).willReturn(Optional.of(existingTask));
        given(repository.save(existingTask)).willReturn(existingTask);

        // Act
        Task result = service.updateTask(updatedTask);

        // Assert
        assertThat(result.getTitle()).isEqualTo("Original Title");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
        assertThat(result.getStatus()).isEqualTo(TaskStatus.PENDING);
        verify(repository).save(existingTask);
    }

    @Test
    void updateTask_shouldKeepDescriptionWhenNotProvided() {
        Task existingTask = new Task();
        existingTask.setId(4L);
        existingTask.setTitle("Original Title");
        existingTask.setDescription("Keep Description");
        existingTask.setStatus(TaskStatus.PENDING);
        existingTask.setDueDate(LocalDateTime.now().plusDays(1));

        Task updatedTask = new Task();
        updatedTask.setId(4L);
        updatedTask.setTitle("Updated Title");
        updatedTask.setDescription(null);

        given(repository.findById(4L)).willReturn(Optional.of(existingTask));
        given(repository.save(existingTask)).willReturn(existingTask);

        Task result = service.updateTask(updatedTask);

        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getDescription()).isEqualTo("Keep Description");
    }

    @Test
    void updateTask_shouldNotOverwriteTitleWithEmptyString() {
        // Arrange
        Task existingTask = new Task();
        existingTask.setId(2L);
        existingTask.setTitle("Keep This Title");
        existingTask.setDescription("Description");

        Task updatedTask = new Task();
        updatedTask.setId(2L);
        updatedTask.setTitle(""); // invalid, should not overwrite

        given(repository.findById(2L)).willReturn(Optional.of(existingTask));
        given(repository.save(existingTask)).willReturn(existingTask);

        // Act
        Task result = service.updateTask(updatedTask);

        // Assert
        assertThat(result.getTitle()).isEqualTo("Keep This Title");
    }

    @Test
    void updateTask_shouldUpdateStatusAndDueDate() {
        // Arrange
        Task existingTask = new Task();
        existingTask.setId(3L);
        existingTask.setStatus(TaskStatus.PENDING);
        existingTask.setDueDate(LocalDateTime.now().plusDays(3));

        Task updatedTask = new Task();
        updatedTask.setId(3L);
        updatedTask.setStatus(TaskStatus.COMPLETED);
        LocalDateTime newDueDate = LocalDateTime.now().plusDays(10);
        updatedTask.setDueDate(newDueDate);

        given(repository.findById(3L)).willReturn(Optional.of(existingTask));
        given(repository.save(existingTask)).willReturn(existingTask);

        // Act
        Task result = service.updateTask(updatedTask);

        // Assert
        assertThat(result.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(result.getDueDate()).isEqualTo(newDueDate);
    }

    @Test
    void updateTask_shouldThrowTaskNotFoundWhenIdNotFound() {
        Task updatedTask = new Task();
        updatedTask.setId(999L);

        given(repository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateTask(updatedTask))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("Task not found with id 999");
    }

    @Test
    void updateTask_shouldThrowInvalidTaskStateWhenReopeningCompletedTask() {
        Task existing = new Task();
        existing.setId(1L);
        existing.setTitle("Done task");
        existing.setStatus(TaskStatus.COMPLETED);
        existing.setDueDate(LocalDateTime.now().plusDays(1));

        Task update = new Task();
        update.setId(1L);
        update.setStatus(TaskStatus.IN_PROGRESS);

        given(repository.findById(1L)).willReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.updateTask(update))
                .isInstanceOf(InvalidTaskStateException.class)
                .hasMessageContaining("Cannot move task from COMPLETED");
    }

    @Test
    void deleteTask_shouldCallRepositoryDelete() {
        Long id = 1L;
        given(repository.existsById(id)).willReturn(true);
        service.deleteTask(id);
        verify(repository).deleteById(id);
    }

    @Test
    void deleteTask_shouldThrowWhenIdNotFound() {
        Long id = 999L;

        assertThatThrownBy(() -> service.deleteTask(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Task not found with id " + id);
    }

    @Test
    void deleteTask_shouldThrowWhenIdIsNull() {
        Long id = null;

        assertThatThrownBy(() -> service.deleteTask(id))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
