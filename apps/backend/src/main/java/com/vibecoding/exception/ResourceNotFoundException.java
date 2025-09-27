package com.vibecoding.exception;

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * 메시지와 함께 예외를 생성합니다.
     *
     * @param message 오류 메시지
     */
    public ResourceNotFoundException(final String message) {
        super(message);
    }

    /**
     * 메시지와 원인과 함께 예외를 생성합니다.
     *
     * @param message 오류 메시지
     * @param cause   원인 예외
     */
    public ResourceNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }
}