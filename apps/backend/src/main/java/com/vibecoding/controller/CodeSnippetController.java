package com.vibecoding.controller;

import com.vibecoding.dto.CodeSnippetRequest;
import com.vibecoding.dto.CodeSnippetResponse;
import com.vibecoding.dto.PageResponse;
import com.vibecoding.service.CodeSnippetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 코드 스니펫 REST 컨트롤러.
 * 코드 스니펫 관련 API 엔드포인트를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/snippets")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"})
public class CodeSnippetController {

    private final CodeSnippetService codeSnippetService;

    /**
     * 새로운 코드 스니펫을 생성합니다.
     *
     * @param request 코드 스니펫 생성 요청
     * @return 생성된 코드 스니펫
     */
    @PostMapping
    public ResponseEntity<CodeSnippetResponse> createCodeSnippet(
            @Valid @RequestBody final CodeSnippetRequest request) {

        log.info("POST /api/v1/snippets - Creating code snippet: {}", request.getTitle());

        final CodeSnippetResponse response = codeSnippetService.createCodeSnippet(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 코드 스니펫을 ID로 조회합니다.
     *
     * @param id 코드 스니펫 ID
     * @return 코드 스니펫 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<CodeSnippetResponse> getCodeSnippet(@PathVariable final Long id) {
        log.debug("GET /api/v1/snippets/{} - Retrieving code snippet", id);

        final CodeSnippetResponse response = codeSnippetService.getCodeSnippet(id);

        return ResponseEntity.ok(response);
    }

    /**
     * 코드 스니펫을 수정합니다.
     *
     * @param id      코드 스니펫 ID
     * @param request 수정 요청
     * @return 수정된 코드 스니펫
     */
    @PutMapping("/{id}")
    public ResponseEntity<CodeSnippetResponse> updateCodeSnippet(
            @PathVariable final Long id,
            @Valid @RequestBody final CodeSnippetRequest request) {

        log.info("PUT /api/v1/snippets/{} - Updating code snippet", id);

        final CodeSnippetResponse response = codeSnippetService.updateCodeSnippet(id, request);

        return ResponseEntity.ok(response);
    }

    /**
     * 코드 스니펫을 삭제합니다.
     *
     * @param id 코드 스니펫 ID
     * @return 삭제 완료 응답
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCodeSnippet(@PathVariable final Long id) {
        log.info("DELETE /api/v1/snippets/{} - Deleting code snippet", id);

        codeSnippetService.deleteCodeSnippet(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * 모든 활성 코드 스니펫을 페이징으로 조회합니다.
     *
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 페이지 크기 (기본값: 20)
     * @return 페이징된 코드 스니펫 목록
     */
    @GetMapping
    public ResponseEntity<PageResponse<CodeSnippetResponse>> getAllCodeSnippets(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size) {

        log.debug("GET /api/v1/snippets - Retrieving all snippets (page: {}, size: {})", page, size);

        final Pageable pageable = PageRequest.of(page, size);
        final PageResponse<CodeSnippetResponse> response = codeSnippetService.getAllCodeSnippets(pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * 작성자별로 코드 스니펫을 조회합니다.
     *
     * @param authorName 작성자 이름
     * @param page       페이지 번호
     * @param size       페이지 크기
     * @return 페이징된 코드 스니펫 목록
     */
    @GetMapping("/author/{authorName}")
    public ResponseEntity<PageResponse<CodeSnippetResponse>> getCodeSnippetsByAuthor(
            @PathVariable final String authorName,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size) {

        log.debug("GET /api/v1/snippets/author/{} - Retrieving snippets by author", authorName);

        final Pageable pageable = PageRequest.of(page, size);
        final PageResponse<CodeSnippetResponse> response = codeSnippetService
                .getCodeSnippetsByAuthor(authorName, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * 프로그래밍 언어별로 코드 스니펫을 조회합니다.
     *
     * @param language 프로그래밍 언어
     * @param page     페이지 번호
     * @param size     페이지 크기
     * @return 페이징된 코드 스니펫 목록
     */
    @GetMapping("/language/{language}")
    public ResponseEntity<PageResponse<CodeSnippetResponse>> getCodeSnippetsByLanguage(
            @PathVariable final String language,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size) {

        log.debug("GET /api/v1/snippets/language/{} - Retrieving snippets by language", language);

        final Pageable pageable = PageRequest.of(page, size);
        final PageResponse<CodeSnippetResponse> response = codeSnippetService
                .getCodeSnippetsByLanguage(language, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * 키워드로 코드 스니펫을 검색합니다.
     *
     * @param keyword 검색 키워드
     * @param page    페이지 번호
     * @param size    페이지 크기
     * @return 페이징된 코드 스니펫 목록
     */
    @GetMapping("/search")
    public ResponseEntity<PageResponse<CodeSnippetResponse>> searchCodeSnippets(
            @RequestParam final String keyword,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size) {

        log.debug("GET /api/v1/snippets/search - Searching snippets with keyword: {}", keyword);

        final Pageable pageable = PageRequest.of(page, size);
        final PageResponse<CodeSnippetResponse> response = codeSnippetService
                .searchCodeSnippets(keyword, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * 인기 코드 스니펫을 조회합니다 (실행 횟수 기준).
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 페이징된 인기 코드 스니펫 목록
     */
    @GetMapping("/popular")
    public ResponseEntity<PageResponse<CodeSnippetResponse>> getPopularCodeSnippets(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size) {

        log.debug("GET /api/v1/snippets/popular - Retrieving popular snippets");

        final Pageable pageable = PageRequest.of(page, size);
        final PageResponse<CodeSnippetResponse> response = codeSnippetService
                .getPopularCodeSnippets(pageable);

        return ResponseEntity.ok(response);
    }
}