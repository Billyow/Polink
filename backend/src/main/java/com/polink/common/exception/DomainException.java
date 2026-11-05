package com.polink.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DomainException extends RuntimeException {

    private final HttpStatus status;

    public DomainException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public DomainException(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }
}
