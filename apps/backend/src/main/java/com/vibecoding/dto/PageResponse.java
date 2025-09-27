package com.vibecoding.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 페이징된 응답 DTO.
 *
 * @param <T> 응답 데이터 타입
 */
@Data
@Builder
public class PageResponse<T> {

    /**
     * 응답 데이터 목록.
     */
    private List<T> content;

    /**
     * 현재 페이지 번호 (0부터 시작).
     */
    private Integer page;

    /**
     * 페이지 크기.
     */
    private Integer size;

    /**
     * 전체 요소 개수.
     */
    private Long totalElements;

    /**
     * 전체 페이지 개수.
     */
    private Integer totalPages;

    /**
     * 첫 번째 페이지 여부.
     */
    private Boolean first;

    /**
     * 마지막 페이지 여부.
     */
    private Boolean last;
}