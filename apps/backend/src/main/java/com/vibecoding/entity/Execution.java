package com.vibecoding.entity;

import com.vibecoding.entity.enums.ExecutionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 코드 실행 기록 엔티티.
 * 코드 스니펫의 실행 결과와 관련 정보를 저장합니다.
 */
@Entity
@Table(name = "executions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Execution extends BaseEntity {

    /**
     * 실행 기록의 고유 식별자.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 실행된 코드 스니펫.
     */
    @NotNull(message = "코드 스니펫은 필수입니다")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_snippet_id", nullable = false)
    private CodeSnippet codeSnippet;

    /**
     * 실행 결과 출력.
     * 성공한 경우에만 값이 있습니다.
     */
    @Column(name = "output", columnDefinition = "TEXT")
    private String output;

    /**
     * 에러 메시지.
     * 실행에 실패한 경우에만 값이 있습니다.
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 실행 시간 (밀리초).
     */
    @Min(value = 0, message = "실행 시간은 0 이상이어야 합니다")
    @Column(name = "execution_time", nullable = false)
    private Long executionTime = 0L;

    /**
     * 메모리 사용량 (바이트).
     */
    @Min(value = 0, message = "메모리 사용량은 0 이상이어야 합니다")
    @Column(name = "memory_usage")
    private Long memoryUsage;

    /**
     * 실행 상태.
     */
    @NotNull(message = "실행 상태는 필수입니다")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ExecutionStatus status;

    /**
     * 편의 생성자 - 성공한 실행.
     *
     * @param codeSnippet   실행된 코드 스니펫
     * @param output        실행 결과 출력
     * @param executionTime 실행 시간 (밀리초)
     */
    public Execution(final CodeSnippet codeSnippet, final String output, final Long executionTime) {
        this.codeSnippet = codeSnippet;
        this.output = output;
        this.executionTime = executionTime;
        this.status = ExecutionStatus.SUCCESS;
    }

    /**
     * 편의 생성자 - 실패한 실행.
     *
     * @param codeSnippet   실행된 코드 스니펫
     * @param errorMessage  에러 메시지
     * @param executionTime 실행 시간 (밀리초)
     * @param status        실행 상태 (ERROR 또는 TIMEOUT)
     */
    public Execution(final CodeSnippet codeSnippet, final String errorMessage,
                     final Long executionTime, final ExecutionStatus status) {
        this.codeSnippet = codeSnippet;
        this.errorMessage = errorMessage;
        this.executionTime = executionTime;
        this.status = status;
    }

    /**
     * 실행이 성공했는지 확인합니다.
     *
     * @return 성공했으면 true, 아니면 false
     */
    public boolean isSuccess() {
        return ExecutionStatus.SUCCESS.equals(status);
    }

    /**
     * 실행이 실패했는지 확인합니다.
     *
     * @return 실패했으면 true, 아니면 false
     */
    public boolean isFailure() {
        return !isSuccess();
    }

    /**
     * 실행이 타임아웃되었는지 확인합니다.
     *
     * @return 타임아웃되었으면 true, 아니면 false
     */
    public boolean isTimeout() {
        return ExecutionStatus.TIMEOUT.equals(status);
    }
}