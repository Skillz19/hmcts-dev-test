package uk.gov.hmcts.reform.dev.api;

public enum TaskSortBy {
    ID("id"),
    TITLE("title"),
    STATUS("status"),
    DUE_DATE("dueDate");

    private final String entityField;

    TaskSortBy(String entityField) {
        this.entityField = entityField;
    }

    public String entityField() {
        return entityField;
    }

    public static TaskSortBy from(String value) {
        if (value == null) {
            return ID;
        }
        return TaskSortBy.valueOf(value.trim().toUpperCase());
    }
}
