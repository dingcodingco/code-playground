package com.vibecoding.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 코드 스니펫 응답 DTO.
 */
@Data
@Builder
public class CodeSnippetResponse {

    /**
     * 코드 스니펫 ID.
     */
    private Long id;

    /**
     * 코드 스니펫 제목.
     */
    private String title;

    /**
     * 실행할 코드.
     */
    private String code;

    /**
     * 프로그래밍 언어.
     */
    private String language;

    /**
     * 작성자 이름.
     */
    private String authorName;

    /**
     * 활성 상태.
     */
    private Boolean isActive;

    /**
     * 생성 일시.
     */
    private LocalDateTime createdAt;

    /**
     * 수정 일시.
     */
    private LocalDateTime updatedAt;

    /**
     * 실행 횟수.
     */
    private Integer executionCount;

    /**
     * 공유 횟수.
     */
    private Integer shareCount;
}