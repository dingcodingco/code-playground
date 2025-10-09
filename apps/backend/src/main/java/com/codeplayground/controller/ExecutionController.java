package com.codeplayground.controller;

import com.codeplayground.dto.ExecutionRequest;
import com.codeplayground.dto.ExecutionResponse;
import com.codeplayground.dto.PageResponse;
import com.codeplayground.entity.enums.ExecutionStatus;
import com.codeplayground.service.ExecutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 코드 실행 REST 컨트롤러.
 * 코드 실행 관련 API 엔드포인트를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/executions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"})
public class ExecutionController {

    private final ExecutionService executionService;

    /**
     * 코드를 실행합니다.
     *
     * @param request 실행 요청
     * @return 실행 결과
     */
    @PostMapping("/execute")
    public ResponseEntity<ExecutionResponse> executeCode(
            @Valid @RequestBody final ExecutionRequest request) {

        log.info("POST /api/v1/executions/execute - Executing code for snippet ID: {}",
                request.getCodeSnippetId());

        final ExecutionResponse response = executionService.executeCode(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 실행 기록을 ID로 조회합니다.
     *
     * @param executionId 실행 기록 ID
     * @return 실행 기록 정보
     */
    @GetMapping("/{executionId}")
    public ResponseEntity<ExecutionResponse> getExecution(@PathVariable final Long executionId) {
        log.debug("GET /api/v1/executions/{} - Retrieving execution", executionId);

        final ExecutionResponse response = executionService.getExecution(executionId);

        return ResponseEntity.ok(response);
    }

    /**
     * 코드 스니펫의 실행 기록을 조회합니다.
     *
     * @param codeSnippetId 코드 스니펫 ID
     * @param page          페이지 번호
     * @param size          페이지 크기
     * @return 페이징된 실행 기록 목록
     */
    @GetMapping("/snippet/{codeSnippetId}")
    public ResponseEntity<PageResponse<ExecutionResponse>> getExecutionHistory(
            @PathVariable final Long codeSnippetId,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size) {

        log.debug("GET /api/v1/executions/snippet/{} - Retrieving execution history", codeSnippetId);

        final Pageable pageable = PageRequest.of(page, size);
        final PageResponse<ExecutionResponse> response = executionService
                .getExecutionHistory(codeSnippetId, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * 코드 스니펫의 가장 최근 실행 기록을 조회합니다.
     *
     * @param codeSnippetId 코드 스니펫 ID
     * @return 최근 실행 기록 (없으면 404)
     */
    @GetMapping("/snippet/{codeSnippetId}/latest")
    public ResponseEntity<ExecutionResponse> getLatestExecution(
            @PathVariable final Long codeSnippetId) {

        log.debug("GET /api/v1/executions/snippet/{}/latest - Retrieving latest execution", codeSnippetId);

        final ExecutionResponse response = executionService.getLatestExecution(codeSnippetId);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 상태의 실행 기록을 조회합니다.
     *
     * @param status 실행 상태
     * @param page   페이지 번호
     * @param size   페이지 크기
     * @return 페이징된 실행 기록 목록
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<PageResponse<ExecutionResponse>> getExecutionsByStatus(
            @PathVariable final ExecutionStatus status,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size) {

        log.debug("GET /api/v1/executions/status/{} - Retrieving executions by status", status);

        final Pageable pageable = PageRequest.of(page, size);
        final PageResponse<ExecutionResponse> response = executionService
                .getExecutionsByStatus(status, pageable);

        return ResponseEntity.ok(response);
    }
}