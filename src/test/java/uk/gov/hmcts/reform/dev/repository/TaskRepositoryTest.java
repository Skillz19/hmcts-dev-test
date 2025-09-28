package uk.gov.hmcts.reform.dev.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.TaskStatus;

import static org.assertj.core.api.Assertions.assertThat;

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

        // when
        Task saved = repository.save(task);

        // then
        assertThat(saved.getId()).isNotNull();

        Task found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getTitle()).isEqualTo("First TDD Task");
        assertThat(found.getStatus()).isEqualTo(TaskStatus.PENDING);
    }
}
