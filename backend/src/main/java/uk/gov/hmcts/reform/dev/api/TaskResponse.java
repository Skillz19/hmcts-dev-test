package uk.gov.hmcts.reform.dev.api;

import java.time.LocalDateTime;
import uk.gov.hmcts.reform.dev.models.TaskStatus;

public record TaskResponse(
        Long id,
        Long version,
        String title,
        String description,
        TaskStatus status,
        LocalDateTime dueDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}