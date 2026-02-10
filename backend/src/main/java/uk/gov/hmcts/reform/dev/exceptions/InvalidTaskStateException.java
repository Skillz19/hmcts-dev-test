package uk.gov.hmcts.reform.dev.exceptions;

public class InvalidTaskStateException extends RuntimeException {
    public InvalidTaskStateException(String message) {
        super(message);
    }
}
