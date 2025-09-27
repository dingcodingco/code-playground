package com.vibecoding.dto;

import com.vibecoding.entity.enums.ExecutionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 코드 실행 응답 DTO.
 */
@Data
@Builder
public class ExecutionResponse {

    /**
     * 실행 기록 ID.
     */
    private Long id;

    /**
     * 코드 스니펫 ID.
     */
    private Long codeSnippetId;

    /**
     * 실행 상태.
     */
    private ExecutionStatus status;

    /**
     * 실행 출력 결과.
     */
    private String output;

    /**
     * 에러 메시지 (실행 실패 시).
     */
    private String errorMessage;

    /**
     * 실행 시간 (밀리초).
     */
    private Long executionTime;

    /**
     * 메모리 사용량 (바이트).
     */
    private Long memoryUsage;

    /**
     * 실행 일시.
     */
    private LocalDateTime createdAt;
}