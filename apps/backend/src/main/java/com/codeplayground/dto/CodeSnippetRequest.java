package com.codeplayground.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 코드 스니펫 생성/수정 요청 DTO.
 */
@Data
public class CodeSnippetRequest {

    /**
     * 코드 스니펫 제목.
     */
    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 255, message = "제목은 255자를 초과할 수 없습니다")
    private String title;

    /**
     * 실행할 코드.
     */
    @NotBlank(message = "코드는 필수입니다")
    @Size(max = 10000, message = "코드는 10,000자를 초과할 수 없습니다")
    private String code;

    /**
     * 프로그래밍 언어.
     */
    @NotBlank(message = "프로그래밍 언어는 필수입니다")
    @Size(max = 50, message = "프로그래밍 언어는 50자를 초과할 수 없습니다")
    private String language;

    /**
     * 작성자 이름.
     */
    @NotBlank(message = "작성자명은 필수입니다")
    @Size(max = 100, message = "작성자명은 100자를 초과할 수 없습니다")
    private String authorName;
}