package com.codeplayground.service;

import com.codeplayground.dto.CodeSnippetRequest;
import com.codeplayground.dto.CodeSnippetResponse;
import com.codeplayground.dto.PageResponse;
import com.codeplayground.entity.CodeSnippet;
import com.codeplayground.exception.ResourceNotFoundException;
import com.codeplayground.repository.CodeSnippetRepository;
import com.codeplayground.repository.ExecutionRepository;
import com.codeplayground.repository.SharedCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 코드 스니펫 서비스.
 * 코드 스니펫의 생성, 조회, 수정, 삭제 및 비즈니스 로직을 처리합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodeSnippetService {

    private final CodeSnippetRepository codeSnippetRepository;
    private final ExecutionRepository executionRepository;
    private final SharedCodeRepository sharedCodeRepository;

    /**
     * 새로운 코드 스니펫을 생성합니다.
     *
     * @param request 코드 스니펫 생성 요청
     * @return 생성된 코드 스니펫 응답
     */
    @Transactional
    public CodeSnippetResponse createCodeSnippet(final CodeSnippetRequest request) {
        log.info("Creating new code snippet with title: {}", request.getTitle());

        final CodeSnippet codeSnippet = CodeSnippet.builder()
                .title(request.getTitle())
                .code(request.getCode())
                .language(request.getLanguage())
                .authorName(request.getAuthorName())
                .isActive(true)
                .build();

        final CodeSnippet savedCodeSnippet = codeSnippetRepository.save(codeSnippet);

        log.info("Code snippet created with ID: {}", savedCodeSnippet.getId());
        return convertToResponse(savedCodeSnippet);
    }

    /**
     * 코드 스니펫을 ID로 조회합니다.
     *
     * @param id 코드 스니펫 ID
     * @return 코드 스니펫 응답
     */
    public CodeSnippetResponse getCodeSnippet(final Long id) {
        log.debug("Retrieving code snippet with ID: {}", id);

        final CodeSnippet codeSnippet = codeSnippetRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("코드 스니펫을 찾을 수 없습니다: " + id));

        return convertToResponse(codeSnippet);
    }

    /**
     * 코드 스니펫을 수정합니다.
     *
     * @param id      코드 스니펫 ID
     * @param request 수정 요청
     * @return 수정된 코드 스니펫 응답
     */
    @Transactional
    public CodeSnippetResponse updateCodeSnippet(final Long id, final CodeSnippetRequest request) {
        log.info("Updating code snippet with ID: {}", id);

        final CodeSnippet codeSnippet = codeSnippetRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("코드 스니펫을 찾을 수 없습니다: " + id));

        codeSnippet.setTitle(request.getTitle());
        codeSnippet.setCode(request.getCode());
        codeSnippet.setLanguage(request.getLanguage());

        final CodeSnippet updatedCodeSnippet = codeSnippetRepository.save(codeSnippet);

        log.info("Code snippet updated with ID: {}", updatedCodeSnippet.getId());
        return convertToResponse(updatedCodeSnippet);
    }

    /**
     * 코드 스니펫을 삭제합니다 (논리적 삭제).
     *
     * @param id 코드 스니펫 ID
     */
    @Transactional
    public void deleteCodeSnippet(final Long id) {
        log.info("Deleting code snippet with ID: {}", id);

        final CodeSnippet codeSnippet = codeSnippetRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("코드 스니펫을 찾을 수 없습니다: " + id));

        codeSnippet.setIsActive(false);
        codeSnippetRepository.save(codeSnippet);

        log.info("Code snippet deleted with ID: {}", id);
    }

    /**
     * 모든 활성 코드 스니펫을 페이징으로 조회합니다.
     *
     * @param pageable 페이징 정보
     * @return 페이징된 코드 스니펫 목록
     */
    public PageResponse<CodeSnippetResponse> getAllCodeSnippets(final Pageable pageable) {
        log.debug("Retrieving all active code snippets with page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        final Page<CodeSnippet> codeSnippetsPage = codeSnippetRepository
                .findByIsActiveTrueOrderByCreatedAtDesc(pageable);

        return convertToPageResponse(codeSnippetsPage);
    }

    /**
     * 작성자별로 코드 스니펫을 조회합니다.
     *
     * @param authorName 작성자 이름
     * @param pageable   페이징 정보
     * @return 페이징된 코드 스니펫 목록
     */
    public PageResponse<CodeSnippetResponse> getCodeSnippetsByAuthor(final String authorName,
                                                                     final Pageable pageable) {
        log.debug("Retrieving code snippets by author: {} with page: {}, size: {}",
                authorName, pageable.getPageNumber(), pageable.getPageSize());

        final Page<CodeSnippet> codeSnippetsPage = codeSnippetRepository
                .findByAuthorNameAndIsActiveTrueOrderByCreatedAtDesc(authorName, pageable);

        return convertToPageResponse(codeSnippetsPage);
    }

    /**
     * 프로그래밍 언어별로 코드 스니펫을 조회합니다.
     *
     * @param language 프로그래밍 언어
     * @param pageable 페이징 정보
     * @return 페이징된 코드 스니펫 목록
     */
    public PageResponse<CodeSnippetResponse> getCodeSnippetsByLanguage(final String language,
                                                                       final Pageable pageable) {
        log.debug("Retrieving code snippets by language: {} with page: {}, size: {}",
                language, pageable.getPageNumber(), pageable.getPageSize());

        final Page<CodeSnippet> codeSnippetsPage = codeSnippetRepository
                .findByLanguageAndIsActiveTrueOrderByCreatedAtDesc(language, pageable);

        return convertToPageResponse(codeSnippetsPage);
    }

    /**
     * 키워드로 코드 스니펫을 검색합니다.
     *
     * @param keyword  검색 키워드
     * @param pageable 페이징 정보
     * @return 페이징된 코드 스니펫 목록
     */
    public PageResponse<CodeSnippetResponse> searchCodeSnippets(final String keyword,
                                                                final Pageable pageable) {
        log.debug("Searching code snippets with keyword: {} with page: {}, size: {}",
                keyword, pageable.getPageNumber(), pageable.getPageSize());

        final Page<CodeSnippet> codeSnippetsPage = codeSnippetRepository
                .searchByKeyword(keyword, pageable);

        return convertToPageResponse(codeSnippetsPage);
    }

    /**
     * 인기 코드 스니펫을 조회합니다 (실행 횟수 기준).
     *
     * @param pageable 페이징 정보
     * @return 페이징된 인기 코드 스니펫 목록
     */
    public PageResponse<CodeSnippetResponse> getPopularCodeSnippets(final Pageable pageable) {
        log.debug("Retrieving popular code snippets with page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        final Page<CodeSnippet> codeSnippetsPage = codeSnippetRepository
                .findPopularCodeSnippets(pageable);

        return convertToPageResponse(codeSnippetsPage);
    }

    /**
     * CodeSnippet 엔티티를 CodeSnippetResponse로 변환합니다.
     *
     * @param codeSnippet 코드 스니펫 엔티티
     * @return 코드 스니펫 응답 DTO
     */
    private CodeSnippetResponse convertToResponse(final CodeSnippet codeSnippet) {
        // 실행 횟수와 공유 횟수 계산
        final int executionCount = (int) executionRepository.countByCodeSnippetId(codeSnippet.getId());
        final int shareCount = (int) sharedCodeRepository.countByCodeSnippetIdAndIsActiveTrue(codeSnippet.getId());

        return CodeSnippetResponse.builder()
                .id(codeSnippet.getId())
                .title(codeSnippet.getTitle())
                .code(codeSnippet.getCode())
                .language(codeSnippet.getLanguage())
                .authorName(codeSnippet.getAuthorName())
                .isActive(codeSnippet.getIsActive())
                .createdAt(codeSnippet.getCreatedAt())
                .updatedAt(codeSnippet.getUpdatedAt())
                .executionCount(executionCount)
                .shareCount(shareCount)
                .build();
    }

    /**
     * Page<CodeSnippet>을 PageResponse<CodeSnippetResponse>로 변환합니다.
     *
     * @param codeSnippetsPage 코드 스니펫 페이지
     * @return 페이징된 응답 DTO
     */
    private PageResponse<CodeSnippetResponse> convertToPageResponse(final Page<CodeSnippet> codeSnippetsPage) {
        return PageResponse.<CodeSnippetResponse>builder()
                .content(codeSnippetsPage.getContent().stream()
                        .map(this::convertToResponse)
                        .toList())
                .page(codeSnippetsPage.getNumber())
                .size(codeSnippetsPage.getSize())
                .totalElements(codeSnippetsPage.getTotalElements())
                .totalPages(codeSnippetsPage.getTotalPages())
                .first(codeSnippetsPage.isFirst())
                .last(codeSnippetsPage.isLast())
                .build();
    }
}