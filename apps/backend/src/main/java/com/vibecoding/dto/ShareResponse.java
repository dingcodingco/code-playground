package com.vibecoding.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 코드 공유 응답 DTO.
 */
@Data
@Builder
public class ShareResponse {

    /**
     * 공유 코드 ID.
     */
    private Long id;

    /**
     * 코드 스니펫 ID.
     */
    private Long codeSnippetId;

    /**
     * 공유 식별자.
     */
    private String shareId;

    /**
     * 공유 URL.
     */
    private String shareUrl;

    /**
     * 만료 일시 (null이면 무기한).
     */
    private LocalDateTime expiresAt;

    /**
     * 활성 상태.
     */
    private Boolean isActive;

    /**
     * 생성 일시.
     */
    private LocalDateTime createdAt;

    /**
     * 코드 스니펫 정보.
     */
    private CodeSnippetResponse codeSnippet;
}