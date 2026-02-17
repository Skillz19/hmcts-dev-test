package uk.gov.hmcts.reform.dev.api;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Pattern;
import uk.gov.hmcts.reform.dev.models.TaskStatus;

public record TaskUpdateRequest(
        @Pattern(regexp = ".*\\S.*", message = "title must not be blank") String title,
        String description,
        TaskStatus status,
        LocalDateTime dueDate) {
}
