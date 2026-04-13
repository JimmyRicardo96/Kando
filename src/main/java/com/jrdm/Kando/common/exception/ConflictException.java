package com.jrdm.Kando.common.exception;

import lombok.Getter;

@Getter
public class ConflictException extends RuntimeException {

    private final Object conflictPayload;

    public ConflictException(String message) {
        super(message);
        this.conflictPayload = null;
    }

    public ConflictException(String message, Object conflictPayload) {
        super(message);
        this.conflictPayload = conflictPayload;
    }
}
