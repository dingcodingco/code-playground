package com.vibecoding.repository;

import com.vibecoding.entity.CodeSnippet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 코드 스니펫 리포지토리.
 * 코드 스니펫과 관련된 데이터베이스 작업을 처리합니다.
 */
@Repository
public interface CodeSnippetRepository extends JpaRepository<CodeSnippet, Long> {

    /**
     * 활성 상태인 코드 스니펫을 ID로 조회합니다.
     *
     * @param id 코드 스니펫 ID
     * @return 활성 상태인 코드 스니펫
     */
    Optional<CodeSnippet> findByIdAndIsActiveTrue(Long id);

    /**
     * 작성자별로 활성 상태인 코드 스니펫을 조회합니다.
     *
     * @param authorName 작성자 이름
     * @param pageable   페이징 정보
     * @return 코드 스니펫 페이지
     */
    Page<CodeSnippet> findByAuthorNameAndIsActiveTrueOrderByCreatedAtDesc(String authorName, Pageable pageable);

    /**
     * 프로그래밍 언어별로 활성 상태인 코드 스니펫을 조회합니다.
     *
     * @param language 프로그래밍 언어
     * @param pageable 페이징 정보
     * @return 코드 스니펫 페이지
     */
    Page<CodeSnippet> findByLanguageAndIsActiveTrueOrderByCreatedAtDesc(String language,
                                                                        Pageable pageable);

    /**
     * 작성자와 프로그래밍 언어로 활성 상태인 코드 스니펫을 조회합니다.
     *
     * @param authorName 작성자 이름
     * @param language   프로그래밍 언어
     * @param pageable   페이징 정보
     * @return 코드 스니펫 페이지
     */
    Page<CodeSnippet> findByAuthorNameAndLanguageAndIsActiveTrueOrderByCreatedAtDesc(String authorName,
                                                                                     String language,
                                                                                     Pageable pageable);

    /**
     * 제목이나 코드 내용에 키워드가 포함된 활성 상태인 코드 스니펫을 검색합니다.
     *
     * @param keyword  검색 키워드
     * @param pageable 페이징 정보
     * @return 코드 스니펫 페이지
     */
    @Query("SELECT cs FROM CodeSnippet cs WHERE cs.isActive = true AND " +
            "(LOWER(cs.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(cs.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY cs.createdAt DESC")
    Page<CodeSnippet> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 작성자별로 키워드가 포함된 활성 상태인 코드 스니펫을 검색합니다.
     *
     * @param authorName 작성자 이름
     * @param keyword    검색 키워드
     * @param pageable   페이징 정보
     * @return 코드 스니펫 페이지
     */
    @Query("SELECT cs FROM CodeSnippet cs WHERE cs.isActive = true AND cs.authorName = :authorName AND " +
            "(LOWER(cs.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(cs.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY cs.createdAt DESC")
    Page<CodeSnippet> searchByAuthorAndKeyword(@Param("authorName") String authorName,
                                               @Param("keyword") String keyword,
                                               Pageable pageable);

    /**
     * 특정 기간 내에 생성된 활성 상태인 코드 스니펫을 조회합니다.
     *
     * @param startDate 시작 일시
     * @param endDate   종료 일시
     * @param pageable  페이징 정보
     * @return 코드 스니펫 페이지
     */
    Page<CodeSnippet> findByIsActiveTrueAndCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate,
                                                                                LocalDateTime endDate,
                                                                                Pageable pageable);

    /**
     * 작성자별 활성 상태인 코드 스니펫 개수를 조회합니다.
     *
     * @param authorName 작성자 이름
     * @return 코드 스니펫 개수
     */
    long countByAuthorNameAndIsActiveTrue(String authorName);

    /**
     * 프로그래밍 언어별 활성 상태인 코드 스니펫 개수를 조회합니다.
     *
     * @param language 프로그래밍 언어
     * @return 코드 스니펫 개수
     */
    long countByLanguageAndIsActiveTrue(String language);

    /**
     * 최근 생성된 활성 상태인 코드 스니펫을 조회합니다.
     *
     * @param limit 조회할 개수
     * @return 최근 코드 스니펫 목록
     */
    @Query("SELECT cs FROM CodeSnippet cs WHERE cs.isActive = true ORDER BY cs.createdAt DESC")
    List<CodeSnippet> findRecentCodeSnippets(Pageable pageable);

    /**
     * 인기있는 코드 스니펫을 조회합니다 (실행 횟수 기준).
     *
     * @param pageable 페이징 정보
     * @return 코드 스니펫 페이지
     */
    @Query("SELECT cs FROM CodeSnippet cs WHERE cs.isActive = true " +
            "ORDER BY SIZE(cs.executions) DESC, cs.createdAt DESC")
    Page<CodeSnippet> findPopularCodeSnippets(Pageable pageable);

    /**
     * 전체 활성 상태인 코드 스니펫 개수를 조회합니다.
     *
     * @return 전체 코드 스니펫 개수
     */
    long countByIsActiveTrue();

    /**
     * 오늘 생성된 활성 상태인 코드 스니펫 개수를 조회합니다.
     *
     * @param today 오늘 시작 시간
     * @return 오늘 생성된 코드 스니펫 개수
     */
    long countByIsActiveTrueAndCreatedAtGreaterThanEqual(LocalDateTime today);

    /**
     * 활성 상태인 모든 코드 스니펫을 최신순으로 조회합니다.
     *
     * @param pageable 페이징 정보
     * @return 코드 스니펫 페이지
     */
    Page<CodeSnippet> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
}