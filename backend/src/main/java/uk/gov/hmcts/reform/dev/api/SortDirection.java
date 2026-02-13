package uk.gov.hmcts.reform.dev.api;

import org.springframework.data.domain.Sort;

public enum SortDirection {
    ASC,
    DESC;

    public static SortDirection from(String value) {
        if (value == null) {
            return ASC;
        }
        return SortDirection.valueOf(value.trim().toUpperCase());
    }

    public Sort.Direction toSpringDirection() {
        return this == DESC ? Sort.Direction.DESC : Sort.Direction.ASC;
    }
}