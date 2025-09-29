package uk.gov.hmcts.reform.dev.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import jakarta.transaction.Transactional;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.TaskStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@DataJpaTest // Spins up in-memory persistence for testing repositories
class TaskRepositoryTest {

    @Autowired
    private TaskRepository repository;

    @Test
    void saveAndRetrieveTask() {
        // given
        Task task = new Task();
        task.setTitle("First TDD Task");
        task.setStatus(TaskStatus.PENDING);
        task.setDueDate(LocalDateTime.now());
        task.setStatus(TaskStatus.PENDING);

        // when
        Task saved = repository.save(task);

        // then
        assertThat(saved.getId()).isNotNull();

        Task found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getTitle()).isEqualTo("First TDD Task");
        assertThat(found.getStatus()).isEqualTo(TaskStatus.PENDING);
    }

    @Test
    void savingTaskWithoutStatus_shouldThrowException() {
        // Arrange
        Task task = new Task();
        task.setTitle("Task without status");
        task.setDueDate(LocalDateTime.now());

        // Act & Assert
        assertThatThrownBy(() -> repository.saveAndFlush(task))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void savingTaskWithoutDueDate_shouldThrowException() {
        // Arrange
        Task task = new Task();
        task.setTitle("Task without due date");
        task.setStatus(TaskStatus.PENDING);

        // Act & Assert
        assertThatThrownBy(() -> repository.saveAndFlush(task))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void findAllTasks_shouldReturnAllSavedTasks() {
        // Arrange
        Task task1 = new Task();
        task1.setTitle("Task 1");
        task1.setStatus(TaskStatus.PENDING);
        task1.setDueDate(LocalDateTime.now());
        repository.saveAndFlush(task1);

        Task task2 = new Task();
        task2.setTitle("Task 2");
        task2.setStatus(TaskStatus.COMPLETED);
        task2.setDueDate(LocalDateTime.now());
        repository.saveAndFlush(task2);

        // Act
        List<Task> tasks = repository.findAll();

        // Assert
        assertThat(tasks)
                .hasSize(2)
                .extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Task 1", "Task 2");
    }

}
