package uk.gov.hmcts.reform.dev.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.hmcts.reform.dev.api.TaskRequest;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.TaskStatus;
import uk.gov.hmcts.reform.dev.services.TaskService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;

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

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getTaskById_shouldReturnTaskDto() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Sample Task");
        task.setDescription("This is a sample task.");
        task.setStatus(TaskStatus.PENDING);
        task.setDueDate(LocalDateTime.now().plusDays(1));

        given(taskService.getTaskById(1L)).willReturn(Optional.of(task));

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Sample Task"))
                .andExpect(jsonPath("$.description").value("This is a sample task."))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.id").value(1L));
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
    void getAllTasks_shouldReturnListOfTasksDtos() throws Exception {
        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Task 1");
        task1.setDescription("Task 1 description");
        task1.setStatus(TaskStatus.PENDING);
        task1.setDueDate(LocalDateTime.now().plusDays(1));

        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");
        task2.setDescription("Task 2 description");
        task2.setStatus(TaskStatus.COMPLETED);
        task2.setDueDate(LocalDateTime.now().plusDays(2));

        given(taskService.getAllTasks()).willReturn(List.of(task1, task2));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].title").value("Task 2"))
                .andExpect(jsonPath("$[1].status").value("COMPLETED"));
    }

    @Test
    void createTask_shouldReturnCreatedTaskDto() throws Exception {
        LocalDateTime dueDate = LocalDateTime.now().plusDays(1);

        TaskRequest request = new TaskRequest("New Task",
                "Description",
                TaskStatus.PENDING,
                dueDate);

        Task created = new Task();
        created.setId(1L);
        created.setTitle("New Task");
        created.setDescription("Description");
        created.setStatus(TaskStatus.PENDING);
        created.setDueDate(dueDate);

        given(taskService.createTask(any(Task.class))).willReturn(created);

        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.description").value("Description"));
    }

    @Test
    void updateTaskStatus_shouldReturnUpdatedTask() throws Exception {
        LocalDateTime dueDate = LocalDateTime.now().plusDays(2);

        TaskRequest request = new TaskRequest("Existing Task",
                "Desc",
                TaskStatus.COMPLETED,
                dueDate);

        Task updated = new Task();
        updated.setId(1L);
        updated.setTitle("Existing Task");
        updated.setDescription("Desc");
        updated.setStatus(TaskStatus.COMPLETED);
        updated.setDueDate(dueDate);

        given(taskService.updateTask(any(Task.class))).willReturn(updated);

        mockMvc.perform(patch("/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Existing Task"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void deleteTask_shouldReturnNoContent() throws Exception {
        Long taskId = 1L;
        willDoNothing().given(taskService).deleteTask(taskId);

        mockMvc.perform(delete("/tasks/{id}", taskId))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(taskId);
    }

}
