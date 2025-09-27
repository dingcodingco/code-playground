package com.vibecoding.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 코드 실행 요청 DTO.
 */
@Data
public class ExecutionRequest {

    /**
     * 실행할 코드 스니펫 ID.
     */
    @NotNull(message = "코드 스니펫 ID는 필수입니다")
    private Long codeSnippetId;

    /**
     * 실행할 코드 (옵션 - 코드 스니펫을 수정해서 실행할 경우).
     */
    private String customCode;

    /**
     * 입력 데이터 (옵션 - stdin 입력).
     */
    private String input;

    /**
     * 실행 타임아웃 (초 단위, 기본값: 10초).
     */
    private Integer timeoutSeconds = 10;
}