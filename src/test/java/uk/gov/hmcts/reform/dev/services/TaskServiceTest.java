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
    void getAllTasks_shouldReturnAllTasks() {
        Task task1 = new Task();
        Task task2 = new Task();
        given(repository.findAll()).willReturn(List.of(task1, task2));

        List<Task> tasks = service.getAllTasks();

        assertThat(tasks).hasSize(2).contains(task1, task2);
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

}
