package uk.gov.hmcts.reform.dev.api;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToSortDirectionConverter implements Converter<String, SortDirection> {
    @Override
    public SortDirection convert(String source) {
        if (source == null || source.isBlank()) {
            return SortDirection.ASC;
        }

        return switch (source.trim().toLowerCase()) {
            case "asc" -> SortDirection.ASC;
            case "desc" -> SortDirection.DESC;
            default -> throw new IllegalArgumentException(
                    "Invalid direction: " + source + ". Allowed: asc, desc");
        };
    }
}
