package com.codeplayground.service;

import com.codeplayground.dto.ExecutionRequest;
import com.codeplayground.dto.ExecutionResponse;
import com.codeplayground.dto.PageResponse;
import com.codeplayground.entity.CodeSnippet;
import com.codeplayground.entity.Execution;
import com.codeplayground.entity.enums.ExecutionStatus;
import com.codeplayground.exception.ResourceNotFoundException;
import com.codeplayground.repository.CodeSnippetRepository;
import com.codeplayground.repository.ExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 코드 실행 서비스.
 * 코드 실행 요청 처리 및 실행 기록 관리를 담당합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExecutionService {

    private final ExecutionRepository executionRepository;
    private final CodeSnippetRepository codeSnippetRepository;
    private final CodeExecutionService codeExecutionService;

    /**
     * 코드를 실행합니다.
     *
     * @param request 실행 요청
     * @return 실행 결과
     */
    @Transactional
    public ExecutionResponse executeCode(final ExecutionRequest request) {
        log.info("Executing code for snippet ID: {}", request.getCodeSnippetId());

        // 코드 스니펫 조회
        final CodeSnippet codeSnippet = codeSnippetRepository.findByIdAndIsActiveTrue(request.getCodeSnippetId())
                .orElseThrow(() -> new ResourceNotFoundException("코드 스니펫을 찾을 수 없습니다: " + request.getCodeSnippetId()));

        // 코드 실행 (우리의 새로운 CodeExecutionService 사용)
        final Execution execution = codeExecutionService.executeCode(
                codeSnippet,
                request.getCustomCode(),
                request.getInput(),
                request.getTimeoutSeconds()
        );

        return convertToResponse(execution);
    }

    /**
     * 코드 스니펫의 실행 기록을 조회합니다.
     *
     * @param codeSnippetId 코드 스니펫 ID
     * @param pageable      페이징 정보
     * @return 페이징된 실행 기록 목록
     */
    public PageResponse<ExecutionResponse> getExecutionHistory(final Long codeSnippetId,
                                                               final Pageable pageable) {
        log.debug("Retrieving execution history for snippet ID: {} with page: {}, size: {}",
                codeSnippetId, pageable.getPageNumber(), pageable.getPageSize());

        final Page<Execution> executionsPage = executionRepository
                .findByCodeSnippetIdOrderByCreatedAtDesc(codeSnippetId, pageable);

        return convertToPageResponse(executionsPage);
    }

    /**
     * 특정 상태의 실행 기록을 조회합니다.
     *
     * @param status   실행 상태
     * @param pageable 페이징 정보
     * @return 페이징된 실행 기록 목록
     */
    public PageResponse<ExecutionResponse> getExecutionsByStatus(final ExecutionStatus status,
                                                                 final Pageable pageable) {
        log.debug("Retrieving executions by status: {} with page: {}, size: {}",
                status, pageable.getPageNumber(), pageable.getPageSize());

        final Page<Execution> executionsPage = executionRepository
                .findByStatusOrderByCreatedAtDesc(status, pageable);

        return convertToPageResponse(executionsPage);
    }

    /**
     * 실행 기록을 ID로 조회합니다.
     *
     * @param executionId 실행 기록 ID
     * @return 실행 기록 응답
     */
    public ExecutionResponse getExecution(final Long executionId) {
        log.debug("Retrieving execution with ID: {}", executionId);

        final Execution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("실행 기록을 찾을 수 없습니다: " + executionId));

        return convertToResponse(execution);
    }

    /**
     * 코드 스니펫의 가장 최근 실행 기록을 조회합니다.
     *
     * @param codeSnippetId 코드 스니펫 ID
     * @return 최근 실행 기록 (없으면 null)
     */
    public ExecutionResponse getLatestExecution(final Long codeSnippetId) {
        log.debug("Retrieving latest execution for snippet ID: {}", codeSnippetId);

        return executionRepository.findFirstByCodeSnippetIdOrderByCreatedAtDesc(codeSnippetId)
                .map(this::convertToResponse)
                .orElse(null);
    }

    /**
     * 예외 유형에 따라 실행 상태를 결정합니다.
     *
     * @param exception 발생한 예외
     * @return 실행 상태
     */
    private ExecutionStatus determineErrorStatus(final Exception exception) {
        // 타임아웃 관련 예외인지 확인
        if (exception.getMessage() != null &&
            (exception.getMessage().contains("timeout") ||
             exception.getMessage().contains("시간 초과"))) {
            return ExecutionStatus.TIMEOUT;
        }
        return ExecutionStatus.ERROR;
    }

    /**
     * Execution 엔티티를 ExecutionResponse로 변환합니다.
     *
     * @param execution 실행 기록 엔티티
     * @return 실행 응답 DTO
     */
    private ExecutionResponse convertToResponse(final Execution execution) {
        return ExecutionResponse.builder()
                .id(execution.getId())
                .codeSnippetId(execution.getCodeSnippet().getId())
                .status(execution.getStatus())
                .output(execution.getOutput())
                .errorMessage(execution.getErrorMessage())
                .executionTime(execution.getExecutionTime())
                .memoryUsage(execution.getMemoryUsage())
                .createdAt(execution.getCreatedAt())
                .build();
    }

    /**
     * Page<Execution>을 PageResponse<ExecutionResponse>로 변환합니다.
     *
     * @param executionsPage 실행 기록 페이지
     * @return 페이징된 응답 DTO
     */
    private PageResponse<ExecutionResponse> convertToPageResponse(final Page<Execution> executionsPage) {
        return PageResponse.<ExecutionResponse>builder()
                .content(executionsPage.getContent().stream()
                        .map(this::convertToResponse)
                        .toList())
                .page(executionsPage.getNumber())
                .size(executionsPage.getSize())
                .totalElements(executionsPage.getTotalElements())
                .totalPages(executionsPage.getTotalPages())
                .first(executionsPage.isFirst())
                .last(executionsPage.isLast())
                .build();
    }
}