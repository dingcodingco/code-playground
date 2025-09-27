package com.vibecoding.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 코드 공유 요청 DTO.
 */
@Data
public class ShareRequest {

    /**
     * 공유할 코드 스니펫 ID.
     */
    @NotNull(message = "코드 스니펫 ID는 필수입니다")
    private Long codeSnippetId;

    /**
     * 만료 일수 (옵션 - null이면 무기한).
     */
    @Min(value = 1, message = "만료 일수는 1일 이상이어야 합니다")
    private Integer expirationDays;
}