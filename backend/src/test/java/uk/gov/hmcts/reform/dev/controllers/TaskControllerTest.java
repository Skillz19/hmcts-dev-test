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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import uk.gov.hmcts.reform.dev.exceptions.InvalidTaskStateException;
import uk.gov.hmcts.reform.dev.exceptions.TaskNotFoundException;

import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import static org.mockito.ArgumentMatchers.argThat;

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
    void getAllTasks_shouldReturnPagedTaskDtos() throws Exception {
        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Task 1");
        task1.setDescription("Desc 1");
        task1.setStatus(TaskStatus.PENDING);
        task1.setDueDate(LocalDateTime.now().plusDays(1));

        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");
        task2.setDescription("Desc 2");
        task2.setStatus(TaskStatus.COMPLETED);
        task2.setDueDate(LocalDateTime.now().plusDays(2));

        PageRequest pageRequest = PageRequest.of(0, 20, Sort.by("id").ascending());
        Page<Task> paged = new PageImpl<>(List.of(task1, task2), pageRequest, 2);

        given(taskService.getAllTasks(argThat(p -> p.getPageNumber() == 0
                && p.getPageSize() == 20
                && p.getSort().getOrderFor("id") != null
                && p.getSort().getOrderFor("id").isAscending()))).willReturn(paged);

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].id").value(1L))
                .andExpect(jsonPath("$.items[1].id").value(2L))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void getAllTasks_shouldReturnBadRequestWhenSizeIsInvalid() throws Exception {
        mockMvc.perform(get("/tasks").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("size must be between 1 and 100"));
    }

    @Test
    void createTask_shouldReturnCreatedTaskDto() throws Exception {
        LocalDateTime dueDate = LocalDateTime.now().plusDays(1);

        Task created = new Task();
        created.setId(1L);
        created.setTitle("New Task");
        created.setDescription("Description");
        created.setStatus(TaskStatus.PENDING);
        created.setDueDate(dueDate);

        TaskRequest request = new TaskRequest("New Task",
                "Description",
                TaskStatus.PENDING,
                dueDate);

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
    void createTask_shouldReturnBadRequestWhenTitleIsBlank() throws Exception {
        TaskRequest invalidRequest = new TaskRequest(
                "   ",
                "Description",
                TaskStatus.PENDING,
                LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.title").value("title must not be blank"));

        verifyNoInteractions(taskService);
    }

    @Test
    void createTask_shouldReturnBadRequestWhenStatusIsNull() throws Exception {
        TaskRequest invalidRequest = new TaskRequest(
                "New Task",
                "Description",
                null,
                LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.status").value("status must not be null"));

        verifyNoInteractions(taskService);
    }

    @Test
    void createTask_shouldReturnBadRequestWhenDueDateIsNull() throws Exception {
        TaskRequest invalidRequest = new TaskRequest(
                "New Task",
                "Description",
                TaskStatus.PENDING,
                null);

        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.dueDate").value("dueDate must not be null"));

        verifyNoInteractions(taskService);
    }

    @Test
    void createTask_shouldReturnBadRequestWhenBodyIsMalformed() throws Exception {
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"x\",\"status\":\"PENDING\",\"dueDate\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body is missing or invalid"));

        verifyNoInteractions(taskService);
    }

    @Test
    void updateTaskStatus_shouldReturnUpdatedTask() throws Exception {
        LocalDateTime dueDate = LocalDateTime.now().plusDays(2);

        Task updated = new Task();
        updated.setId(1L);
        updated.setTitle("Existing Task");
        updated.setDescription("Desc");
        updated.setStatus(TaskStatus.COMPLETED);
        updated.setDueDate(dueDate);

        TaskRequest request = new TaskRequest("Existing Task",
                "Desc",
                TaskStatus.COMPLETED,
                dueDate);

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
    void updateTaskStatus_shouldReturnBadRequestWhenTitleIsBlank() throws Exception {
        TaskRequest invalidRequest = new TaskRequest(
                " ",
                "Desc",
                TaskStatus.PENDING,
                LocalDateTime.now().plusDays(1));

        mockMvc.perform(patch("/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verifyNoInteractions(taskService);
    }

    @Test
    void updateTaskStatus_shouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {
        TaskRequest request = new TaskRequest(
                "Missing Task",
                "Desc",
                TaskStatus.PENDING,
                LocalDateTime.now().plusDays(1));

        given(taskService.updateTask(any(Task.class))).willThrow(new TaskNotFoundException(999L));

        mockMvc.perform(patch("/tasks/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found with id 999"));
    }

    @Test
    void updateTaskStatus_shouldReturnConflictWhenTransitionIsInvalid() throws Exception {
        TaskRequest request = new TaskRequest(
                "Task",
                "Desc",
                TaskStatus.IN_PROGRESS,
                LocalDateTime.now().plusDays(1));

        given(taskService.updateTask(any(Task.class)))
                .willThrow(new InvalidTaskStateException(
                        "Cannot move task from COMPLETED to another state"));

        mockMvc.perform(patch("/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Cannot move task from COMPLETED to another state"));
    }

    @Test
    void deleteTask_shouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {
        willThrow(new TaskNotFoundException(999L)).given(taskService).deleteTask(999L);

        mockMvc.perform(delete("/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found with id 999"));
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
