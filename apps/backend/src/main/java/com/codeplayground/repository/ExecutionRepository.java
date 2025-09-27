package com.codeplayground.repository;

import com.codeplayground.entity.Execution;
import com.codeplayground.entity.enums.ExecutionStatus;
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
 * 실행 기록 리포지토리.
 * 코드 실행 기록과 관련된 데이터베이스 작업을 처리합니다.
 */
@Repository
public interface ExecutionRepository extends JpaRepository<Execution, Long> {

    /**
     * 코드 스니펫별 실행 기록을 최신순으로 조회합니다.
     *
     * @param codeSnippetId 코드 스니펫 ID
     * @param pageable      페이징 정보
     * @return 실행 기록 페이지
     */
    Page<Execution> findByCodeSnippetIdOrderByCreatedAtDesc(Long codeSnippetId, Pageable pageable);

    /**
     * 코드 스니펫의 가장 최근 실행 기록을 조회합니다.
     *
     * @param codeSnippetId 코드 스니펫 ID
     * @return 최근 실행 기록
     */
    Optional<Execution> findFirstByCodeSnippetIdOrderByCreatedAtDesc(Long codeSnippetId);

    /**
     * 코드 스니펫의 가장 최근 성공한 실행 기록을 조회합니다.
     *
     * @param codeSnippetId 코드 스니펫 ID
     * @return 최근 성공한 실행 기록
     */
    Optional<Execution> findFirstByCodeSnippetIdAndStatusOrderByCreatedAtDesc(Long codeSnippetId,
                                                                              ExecutionStatus status);

    /**
     * 특정 상태의 실행 기록을 조회합니다.
     *
     * @param status   실행 상태
     * @param pageable 페이징 정보
     * @return 실행 기록 페이지
     */
    Page<Execution> findByStatusOrderByCreatedAtDesc(ExecutionStatus status, Pageable pageable);

    /**
     * 특정 기간 내의 실행 기록을 조회합니다.
     *
     * @param startDate 시작 일시
     * @param endDate   종료 일시
     * @param pageable  페이징 정보
     * @return 실행 기록 페이지
     */
    Page<Execution> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate,
                                                               LocalDateTime endDate,
                                                               Pageable pageable);

    /**
     * 코드 스니펫별 실행 횟수를 조회합니다.
     *
     * @param codeSnippetId 코드 스니펫 ID
     * @return 실행 횟수
     */
    long countByCodeSnippetId(Long codeSnippetId);

    /**
     * 특정 상태의 실행 횟수를 조회합니다.
     *
     * @param status 실행 상태
     * @return 실행 횟수
     */
    long countByStatus(ExecutionStatus status);

    /**
     * 특정 기간 내의 실행 횟수를 조회합니다.
     *
     * @param startDate 시작 일시
     * @param endDate   종료 일시
     * @return 실행 횟수
     */
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 평균 실행 시간을 계산합니다.
     *
     * @return 평균 실행 시간 (밀리초)
     */
    @Query("SELECT AVG(e.executionTime) FROM Execution e WHERE e.status = 'SUCCESS'")
    Double findAverageExecutionTime();

    /**
     * 특정 코드 스니펫의 평균 실행 시간을 계산합니다.
     *
     * @param codeSnippetId 코드 스니펫 ID
     * @return 평균 실행 시간 (밀리초)
     */
    @Query("SELECT AVG(e.executionTime) FROM Execution e WHERE e.codeSnippet.id = :codeSnippetId AND e.status = 'SUCCESS'")
    Double findAverageExecutionTimeByCodeSnippetId(@Param("codeSnippetId") Long codeSnippetId);

    /**
     * 실행 시간이 가장 긴 실행 기록들을 조회합니다.
     *
     * @param pageable 페이징 정보
     * @return 실행 기록 목록
     */
    Page<Execution> findByStatusOrderByExecutionTimeDesc(ExecutionStatus status, Pageable pageable);

    /**
     * 오늘의 실행 횟수를 조회합니다.
     *
     * @param today 오늘 시작 시간
     * @return 오늘 실행 횟수
     */
    long countByCreatedAtGreaterThanEqual(LocalDateTime today);

    /**
     * 실행 통계를 조회합니다.
     *
     * @return 실행 통계 (총 실행수, 성공수, 실패수, 타임아웃수)
     */
    @Query("SELECT " +
            "COUNT(e) as totalExecutions, " +
            "SUM(CASE WHEN e.status = 'SUCCESS' THEN 1 ELSE 0 END) as successCount, " +
            "SUM(CASE WHEN e.status = 'ERROR' THEN 1 ELSE 0 END) as errorCount, " +
            "SUM(CASE WHEN e.status = 'TIMEOUT' THEN 1 ELSE 0 END) as timeoutCount " +
            "FROM Execution e")
    Object[] findExecutionStatistics();

    /**
     * 특정 기간의 일별 실행 통계를 조회합니다.
     *
     * @param startDate 시작 일시
     * @param endDate   종료 일시
     * @return 일별 실행 통계
     */
    @Query("SELECT " +
            "DATE(e.createdAt) as date, " +
            "COUNT(e) as totalExecutions, " +
            "SUM(CASE WHEN e.status = 'SUCCESS' THEN 1 ELSE 0 END) as successCount " +
            "FROM Execution e " +
            "WHERE e.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(e.createdAt) " +
            "ORDER BY DATE(e.createdAt)")
    List<Object[]> findDailyExecutionStatistics(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    /**
     * 실행 기록을 일괄 삭제합니다 (특정 기간 이전).
     *
     * @param beforeDate 이 날짜 이전의 기록들을 삭제
     * @return 삭제된 개수
     */
    long deleteByCreatedAtBefore(LocalDateTime beforeDate);
}