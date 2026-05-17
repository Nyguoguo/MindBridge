package com.mindbridge.common.exception;

public class MindBridgeException extends RuntimeException {

    public MindBridgeException(String message) {
        super(message);
    }

    public MindBridgeException(String message, Throwable cause) {
        super(message, cause);
    }
}
