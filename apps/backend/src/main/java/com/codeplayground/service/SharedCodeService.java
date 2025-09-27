package com.codeplayground.service;

import com.codeplayground.dto.PageResponse;
import com.codeplayground.dto.ShareRequest;
import com.codeplayground.dto.ShareResponse;
import com.codeplayground.entity.CodeSnippet;
import com.codeplayground.entity.SharedCode;
import com.codeplayground.exception.ResourceNotFoundException;
import com.codeplayground.repository.CodeSnippetRepository;
import com.codeplayground.repository.SharedCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 코드 공유 서비스.
 * 코드 스니펫의 공유 링크 생성 및 관리를 담당합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SharedCodeService {

    private final SharedCodeRepository sharedCodeRepository;
    private final CodeSnippetRepository codeSnippetRepository;
    private final CodeSnippetService codeSnippetService;

    @Value("${app.base-url:http://localhost:3000}")
    private String baseUrl;

    /**
     * 코드 스니펫에 대한 공유 링크를 생성합니다.
     *
     * @param request 공유 요청
     * @return 공유 응답
     */
    @Transactional
    public ShareResponse createShare(final ShareRequest request) {
        log.info("Creating share for code snippet ID: {}", request.getCodeSnippetId());

        // 코드 스니펫 존재 확인
        final CodeSnippet codeSnippet = codeSnippetRepository.findByIdAndIsActiveTrue(request.getCodeSnippetId())
                .orElseThrow(() -> new ResourceNotFoundException("코드 스니펫을 찾을 수 없습니다: " + request.getCodeSnippetId()));

        // 고유한 공유 ID 생성
        String shareId;
        do {
            shareId = SharedCode.generateShareId();
        } while (sharedCodeRepository.existsByShareId(shareId));

        // 만료 일시 설정
        LocalDateTime expiresAt = null;
        if (request.getExpirationDays() != null && request.getExpirationDays() > 0) {
            expiresAt = LocalDateTime.now().plusDays(request.getExpirationDays());
        }

        // 공유 코드 생성
        final SharedCode sharedCode = new SharedCode(codeSnippet, shareId, expiresAt);
        final SharedCode savedSharedCode = sharedCodeRepository.save(sharedCode);

        log.info("Share created with ID: {} for code snippet: {}", shareId, request.getCodeSnippetId());

        return convertToResponse(savedSharedCode);
    }

    /**
     * 공유 ID로 공유된 코드 스니펫을 조회합니다.
     *
     * @param shareId 공유 ID
     * @return 공유 응답
     */
    public ShareResponse getSharedCode(final String shareId) {
        log.debug("Retrieving shared code with share ID: {}", shareId);

        final SharedCode sharedCode = sharedCodeRepository.findValidByShareId(shareId)
                .orElseThrow(() -> new ResourceNotFoundException("공유된 코드를 찾을 수 없거나 만료되었습니다: " + shareId));

        return convertToResponse(sharedCode);
    }

    /**
     * 공유를 비활성화합니다.
     *
     * @param shareId 공유 ID
     */
    @Transactional
    public void deactivateShare(final String shareId) {
        log.info("Deactivating share with ID: {}", shareId);

        final SharedCode sharedCode = sharedCodeRepository.findByShareId(shareId)
                .orElseThrow(() -> new ResourceNotFoundException("공유를 찾을 수 없습니다: " + shareId));

        sharedCode.deactivate();
        sharedCodeRepository.save(sharedCode);

        log.info("Share deactivated with ID: {}", shareId);
    }

    /**
     * 코드 스니펫의 모든 공유를 조회합니다.
     *
     * @param codeSnippetId 코드 스니펫 ID
     * @param pageable      페이징 정보
     * @return 페이징된 공유 목록
     */
    public PageResponse<ShareResponse> getSharesByCodeSnippet(final Long codeSnippetId,
                                                              final Pageable pageable) {
        log.debug("Retrieving shares for code snippet ID: {} with page: {}, size: {}",
                codeSnippetId, pageable.getPageNumber(), pageable.getPageSize());

        final Page<SharedCode> sharesPage = sharedCodeRepository
                .findByCodeSnippetIdAndIsActiveTrueOrderByCreatedAtDesc(codeSnippetId, pageable);

        return convertToPageResponse(sharesPage);
    }

    /**
     * 최근 생성된 활성 공유들을 조회합니다.
     *
     * @param pageable 페이징 정보
     * @return 페이징된 공유 목록
     */
    public PageResponse<ShareResponse> getRecentShares(final Pageable pageable) {
        log.debug("Retrieving recent shares with page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        final Page<SharedCode> sharesPage = sharedCodeRepository
                .findByIsActiveTrueOrderByCreatedAtDesc(pageable);

        return convertToPageResponse(sharesPage);
    }

    /**
     * 곧 만료될 공유들을 조회합니다.
     *
     * @param pageable 페이징 정보
     * @return 페이징된 공유 목록
     */
    public PageResponse<ShareResponse> getSoonToExpireShares(final Pageable pageable) {
        log.debug("Retrieving soon to expire shares with page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        final LocalDateTime warningTime = LocalDateTime.now().plusHours(24); // 24시간 후 만료 예정
        final Page<SharedCode> sharesPage = sharedCodeRepository
                .findSoonToExpireSharedCodes(warningTime, pageable);

        return convertToPageResponse(sharesPage);
    }

    /**
     * 만료된 공유들을 비활성화합니다.
     *
     * @return 비활성화된 공유 개수
     */
    @Transactional
    public int deactivateExpiredShares() {
        log.info("Deactivating expired shares");

        final int deactivatedCount = sharedCodeRepository.deactivateExpiredSharedCodes(LocalDateTime.now());

        log.info("Deactivated {} expired shares", deactivatedCount);
        return deactivatedCount;
    }

    /**
     * 공유 통계를 조회합니다.
     *
     * @return 공유 통계 배열 [전체, 활성, 만료, 무기한]
     */
    public Object[] getShareStatistics() {
        log.debug("Retrieving share statistics");

        return sharedCodeRepository.findSharingStatistics();
    }

    /**
     * SharedCode 엔티티를 ShareResponse로 변환합니다.
     *
     * @param sharedCode 공유 코드 엔티티
     * @return 공유 응답 DTO
     */
    private ShareResponse convertToResponse(final SharedCode sharedCode) {
        return ShareResponse.builder()
                .id(sharedCode.getId())
                .codeSnippetId(sharedCode.getCodeSnippet().getId())
                .shareId(sharedCode.getShareId())
                .shareUrl(sharedCode.generateShareUrl(baseUrl))
                .expiresAt(sharedCode.getExpiresAt())
                .isActive(sharedCode.getIsActive())
                .createdAt(sharedCode.getCreatedAt())
                .codeSnippet(codeSnippetService.getCodeSnippet(sharedCode.getCodeSnippet().getId()))
                .build();
    }

    /**
     * Page<SharedCode>를 PageResponse<ShareResponse>로 변환합니다.
     *
     * @param sharesPage 공유 페이지
     * @return 페이징된 응답 DTO
     */
    private PageResponse<ShareResponse> convertToPageResponse(final Page<SharedCode> sharesPage) {
        return PageResponse.<ShareResponse>builder()
                .content(sharesPage.getContent().stream()
                        .map(this::convertToResponse)
                        .toList())
                .page(sharesPage.getNumber())
                .size(sharesPage.getSize())
                .totalElements(sharesPage.getTotalElements())
                .totalPages(sharesPage.getTotalPages())
                .first(sharesPage.isFirst())
                .last(sharesPage.isLast())
                .build();
    }
}