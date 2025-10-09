package com.codeplayground.entity.enums;

import lombok.Getter;

/**
 * 코드 실행 상태 열거형.
 */
@Getter
public enum ExecutionStatus {
    /**
     * 성공적으로 실행됨.
     */
    SUCCESS("SUCCESS", "성공"),

    /**
     * 실행 중 에러 발생.
     */
    ERROR("ERROR", "에러"),

    /**
     * 실행 시간 초과.
     */
    TIMEOUT("TIMEOUT", "시간 초과");

    /**
     * 내부 코드.
     */
    private final String code;

    /**
     * 사용자에게 표시되는 메시지.
     */
    private final String message;

    /**
     * ExecutionStatus 생성자.
     *
     * @param code    내부 코드
     * @param message 표시 메시지
     */
    ExecutionStatus(final String code, final String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 현재 상태가 성공인지 확인합니다.
     *
     * @return 성공이면 true, 아니면 false
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }

    /**
     * 현재 상태가 실패인지 확인합니다.
     *
     * @return 실패면 true, 아니면 false
     */
    public boolean isFailure() {
        return this != SUCCESS;
    }
}