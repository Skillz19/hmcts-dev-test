package uk.gov.hmcts.reform.dev.api;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import uk.gov.hmcts.reform.dev.models.TaskStatus;

public record TaskRequest(
        @NotBlank(message = "title must not be blank") String title,

        String description,

        @NotNull(message = "status must not be null") TaskStatus status,

        @NotNull(message = "dueDate must not be null") LocalDateTime dueDate) {
}