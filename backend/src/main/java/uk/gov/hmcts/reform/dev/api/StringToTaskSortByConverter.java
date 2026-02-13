package uk.gov.hmcts.reform.dev.api;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToTaskSortByConverter implements Converter<String, TaskSortBy> {
    @Override
    public TaskSortBy convert(String source) {
        if (source == null || source.isBlank()) {
            return TaskSortBy.ID;
        }

        return switch (source.trim().toLowerCase()) {
            case "id" -> TaskSortBy.ID;
            case "title" -> TaskSortBy.TITLE;
            case "status" -> TaskSortBy.STATUS;
            case "due_date", "duedate" -> TaskSortBy.DUE_DATE;
            default -> throw new IllegalArgumentException(
                    "Invalid sortBy: " + source + ". Allowed: id, title, status, due_date");
        };
    }
}
