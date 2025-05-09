package com.eviden.cine.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(hidden = true)
public class CustomException extends RuntimeException {

    private final String code;

    public CustomException() {
        super("Excepción personalizada");
        this.code = "CUSTOM_ERROR";
    }

    public CustomException(String message) {
        super(message);
        this.code = "CUSTOM_ERROR";
    }

    public CustomException(String message, String code) {
        super(message);
        this.code = code;
    }

    public CustomException(String message, String code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
