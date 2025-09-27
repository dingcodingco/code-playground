package com.vibecoding.controller;

import com.vibecoding.dto.PageResponse;
import com.vibecoding.dto.ShareRequest;
import com.vibecoding.dto.ShareResponse;
import com.vibecoding.service.SharedCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 코드 공유 REST 컨트롤러.
 * 코드 공유 관련 API 엔드포인트를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/shares")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"})
public class SharedCodeController {

    private final SharedCodeService sharedCodeService;

    /**
     * 코드 스니펫에 대한 공유 링크를 생성합니다.
     *
     * @param request 공유 요청
     * @return 생성된 공유 정보
     */
    @PostMapping
    public ResponseEntity<ShareResponse> createShare(@Valid @RequestBody final ShareRequest request) {
        log.info("POST /api/v1/shares - Creating share for snippet ID: {}", request.getCodeSnippetId());

        final ShareResponse response = sharedCodeService.createShare(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 공유 ID로 공유된 코드 스니펫을 조회합니다.
     *
     * @param shareId 공유 ID
     * @return 공유된 코드 정보
     */
    @GetMapping("/{shareId}")
    public ResponseEntity<ShareResponse> getSharedCode(@PathVariable final String shareId) {
        log.debug("GET /api/v1/shares/{} - Retrieving shared code", shareId);

        final ShareResponse response = sharedCodeService.getSharedCode(shareId);

        return ResponseEntity.ok(response);
    }

    /**
     * 공유를 비활성화합니다.
     *
     * @param shareId 공유 ID
     * @return 비활성화 완료 응답
     */
    @DeleteMapping("/{shareId}")
    public ResponseEntity<Void> deactivateShare(@PathVariable final String shareId) {
        log.info("DELETE /api/v1/shares/{} - Deactivating share", shareId);

        sharedCodeService.deactivateShare(shareId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 코드 스니펫의 모든 공유를 조회합니다.
     *
     * @param codeSnippetId 코드 스니펫 ID
     * @param page          페이지 번호
     * @param size          페이지 크기
     * @return 페이징된 공유 목록
     */
    @GetMapping("/snippet/{codeSnippetId}")
    public ResponseEntity<PageResponse<ShareResponse>> getSharesByCodeSnippet(
            @PathVariable final Long codeSnippetId,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size) {

        log.debug("GET /api/v1/shares/snippet/{} - Retrieving shares for snippet", codeSnippetId);

        final Pageable pageable = PageRequest.of(page, size);
        final PageResponse<ShareResponse> response = sharedCodeService
                .getSharesByCodeSnippet(codeSnippetId, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * 최근 생성된 활성 공유들을 조회합니다.
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 페이징된 공유 목록
     */
    @GetMapping("/recent")
    public ResponseEntity<PageResponse<ShareResponse>> getRecentShares(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size) {

        log.debug("GET /api/v1/shares/recent - Retrieving recent shares");

        final Pageable pageable = PageRequest.of(page, size);
        final PageResponse<ShareResponse> response = sharedCodeService.getRecentShares(pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * 곧 만료될 공유들을 조회합니다.
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 페이징된 공유 목록
     */
    @GetMapping("/expiring-soon")
    public ResponseEntity<PageResponse<ShareResponse>> getSoonToExpireShares(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size) {

        log.debug("GET /api/v1/shares/expiring-soon - Retrieving soon to expire shares");

        final Pageable pageable = PageRequest.of(page, size);
        final PageResponse<ShareResponse> response = sharedCodeService.getSoonToExpireShares(pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * 만료된 공유들을 비활성화합니다.
     *
     * @return 비활성화된 공유 개수
     */
    @PostMapping("/cleanup-expired")
    public ResponseEntity<Map<String, Object>> deactivateExpiredShares() {
        log.info("POST /api/v1/shares/cleanup-expired - Deactivating expired shares");

        final int deactivatedCount = sharedCodeService.deactivateExpiredShares();

        final Map<String, Object> response = Map.of(
                "deactivatedCount", deactivatedCount,
                "message", "만료된 공유 " + deactivatedCount + "개를 비활성화했습니다."
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 공유 통계를 조회합니다.
     *
     * @return 공유 통계
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getShareStatistics() {
        log.debug("GET /api/v1/shares/statistics - Retrieving share statistics");

        final Object[] statistics = sharedCodeService.getShareStatistics();

        final Map<String, Object> response = Map.of(
                "totalShares", statistics[0],
                "activeShares", statistics[1],
                "expiredShares", statistics[2],
                "permanentShares", statistics[3]
        );

        return ResponseEntity.ok(response);
    }
}