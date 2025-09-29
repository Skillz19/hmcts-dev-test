package uk.gov.hmcts.reform.dev.services;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;

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

}
