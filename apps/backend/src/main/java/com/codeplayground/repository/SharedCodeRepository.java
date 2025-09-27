package com.codeplayground.repository;

import com.codeplayground.entity.SharedCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 공유 코드 리포지토리.
 * 코드 공유와 관련된 데이터베이스 작업을 처리합니다.
 */
@Repository
public interface SharedCodeRepository extends JpaRepository<SharedCode, Long> {

    /**
     * 공유 ID로 활성 상태이고 유효한 공유 코드를 조회합니다.
     *
     * @param shareId 공유 ID
     * @return 공유 코드
     */
    @Query("SELECT sc FROM SharedCode sc WHERE sc.shareId = :shareId AND sc.isActive = true AND " +
            "(sc.expiresAt IS NULL OR sc.expiresAt > CURRENT_TIMESTAMP)")
    Optional<SharedCode> findValidByShareId(@Param("shareId") String shareId);

    /**
     * 공유 ID로 공유 코드를 조회합니다 (만료 여부와 관계없이).
     *
     * @param shareId 공유 ID
     * @return 공유 코드
     */
    Optional<SharedCode> findByShareId(String shareId);

    /**
     * 공유 ID가 이미 존재하는지 확인합니다.
     *
     * @param shareId 공유 ID
     * @return 존재하면 true, 아니면 false
     */
    boolean existsByShareId(String shareId);

    /**
     * 코드 스니펫별 활성 상태인 공유 코드를 조회합니다.
     *
     * @param codeSnippetId 코드 스니펫 ID
     * @param pageable      페이징 정보
     * @return 공유 코드 페이지
     */
    Page<SharedCode> findByCodeSnippetIdAndIsActiveTrueOrderByCreatedAtDesc(Long codeSnippetId,
                                                                            Pageable pageable);

    /**
     * 특정 기간 내에 생성된 활성 상태인 공유 코드를 조회합니다.
     *
     * @param startDate 시작 일시
     * @param endDate   종료 일시
     * @param pageable  페이징 정보
     * @return 공유 코드 페이지
     */
    Page<SharedCode> findByIsActiveTrueAndCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate,
                                                                               LocalDateTime endDate,
                                                                               Pageable pageable);

    /**
     * 만료된 공유 코드들을 조회합니다.
     *
     * @param currentTime 현재 시간
     * @param pageable    페이징 정보
     * @return 만료된 공유 코드 페이지
     */
    @Query("SELECT sc FROM SharedCode sc WHERE sc.isActive = true AND " +
            "sc.expiresAt IS NOT NULL AND sc.expiresAt < :currentTime " +
            "ORDER BY sc.expiresAt DESC")
    Page<SharedCode> findExpiredSharedCodes(@Param("currentTime") LocalDateTime currentTime,
                                            Pageable pageable);

    /**
     * 곧 만료될 공유 코드들을 조회합니다.
     *
     * @param warningTime 경고 시간 (이 시간 이후 만료되는 코드들)
     * @param pageable    페이징 정보
     * @return 곧 만료될 공유 코드 페이지
     */
    @Query("SELECT sc FROM SharedCode sc WHERE sc.isActive = true AND " +
            "sc.expiresAt IS NOT NULL AND sc.expiresAt BETWEEN CURRENT_TIMESTAMP AND :warningTime " +
            "ORDER BY sc.expiresAt ASC")
    Page<SharedCode> findSoonToExpireSharedCodes(@Param("warningTime") LocalDateTime warningTime,
                                                 Pageable pageable);

    /**
     * 코드 스니펫별 활성 상태인 공유 코드 개수를 조회합니다.
     *
     * @param codeSnippetId 코드 스니펫 ID
     * @return 공유 코드 개수
     */
    long countByCodeSnippetIdAndIsActiveTrue(Long codeSnippetId);

    /**
     * 전체 활성 상태인 공유 코드 개수를 조회합니다.
     *
     * @return 전체 공유 코드 개수
     */
    long countByIsActiveTrue();

    /**
     * 오늘 생성된 활성 상태인 공유 코드 개수를 조회합니다.
     *
     * @param today 오늘 시작 시간
     * @return 오늘 생성된 공유 코드 개수
     */
    long countByIsActiveTrueAndCreatedAtGreaterThanEqual(LocalDateTime today);

    /**
     * 만료된 공유 코드들을 비활성화합니다.
     *
     * @param currentTime 현재 시간
     * @return 비활성화된 개수
     */
    @Modifying
    @Query("UPDATE SharedCode sc SET sc.isActive = false WHERE sc.isActive = true AND " +
            "sc.expiresAt IS NOT NULL AND sc.expiresAt < :currentTime")
    int deactivateExpiredSharedCodes(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 특정 기간 이전에 생성된 비활성 공유 코드들을 삭제합니다.
     *
     * @param beforeDate 이 날짜 이전의 비활성 공유 코드들을 삭제
     * @return 삭제된 개수
     */
    long deleteByIsActiveFalseAndCreatedAtBefore(LocalDateTime beforeDate);

    /**
     * 최근에 생성된 활성 상태인 공유 코드들을 조회합니다.
     *
     * @param pageable 페이징 정보
     * @return 최근 공유 코드 목록
     */
    Page<SharedCode> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 무기한 공유 코드들을 조회합니다.
     *
     * @param pageable 페이징 정보
     * @return 무기한 공유 코드 페이지
     */
    Page<SharedCode> findByIsActiveTrueAndExpiresAtIsNullOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 기간 제한이 있는 활성 상태 공유 코드들을 조회합니다.
     *
     * @param pageable 페이징 정보
     * @return 기간 제한 공유 코드 페이지
     */
    Page<SharedCode> findByIsActiveTrueAndExpiresAtIsNotNullOrderByExpiresAtAsc(Pageable pageable);

    /**
     * 공유 통계를 조회합니다.
     *
     * @return 공유 통계 (전체, 활성, 만료, 무기한)
     */
    @Query("SELECT " +
            "COUNT(sc) as totalShares, " +
            "SUM(CASE WHEN sc.isActive = true THEN 1 ELSE 0 END) as activeShares, " +
            "SUM(CASE WHEN sc.isActive = true AND sc.expiresAt IS NOT NULL AND sc.expiresAt < CURRENT_TIMESTAMP THEN 1 ELSE 0 END) as expiredShares, " +
            "SUM(CASE WHEN sc.isActive = true AND sc.expiresAt IS NULL THEN 1 ELSE 0 END) as permanentShares " +
            "FROM SharedCode sc")
    Object[] findSharingStatistics();
}