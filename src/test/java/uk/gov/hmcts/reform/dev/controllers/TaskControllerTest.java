package uk.gov.hmcts.reform.dev.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.TaskStatus;
import uk.gov.hmcts.reform.dev.services.TaskService;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    void getTaskById_shouldReturnTask() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Sample Task");
        task.setStatus(TaskStatus.PENDING);
        task.setDueDate(LocalDateTime.now().plusDays(1));

        given(taskService.getTaskById(1L)).willReturn(Optional.of(task));

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Sample Task"));
    }

    @Test
    void getTaskById_shouldReturnNotFoundForNonExistingTask() throws Exception {
        given(taskService.getTaskById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/tasks/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTaskById_shouldReturnBadRequestForInvalidId() throws Exception {
        mockMvc.perform(get("/tasks/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllTasks_shouldReturnListOfTasks() throws Exception {
        // Arrange
        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Task 1");
        task1.setStatus(TaskStatus.PENDING);
        task1.setDueDate(LocalDateTime.now().plusDays(1));

        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");
        task2.setStatus(TaskStatus.PENDING);
        task2.setDueDate(LocalDateTime.now().plusDays(2));

        given(taskService.getAllTasks()).willReturn(List.of(task1, task2));

        // Act & Assert
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[1].title").value("Task 2"));
    }

}
