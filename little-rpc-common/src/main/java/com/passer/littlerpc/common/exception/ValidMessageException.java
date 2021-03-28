package com.passer.littlerpc.common.exception;

/**
 * @author passer
 * @time 2021/3/27 10:40 上午
 */
public class ValidMessageException extends RuntimeException {
    public ValidMessageException() {
    }

    public ValidMessageException(String message) {
        super(message);
    }

    public ValidMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
