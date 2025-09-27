package com.vibecoding.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 공유 코드 엔티티.
 * 코드 스니펫의 공유 정보를 저장합니다.
 */
@Entity
@Table(name = "shared_codes")
@Getter
@Setter
@NoArgsConstructor
public class SharedCode extends BaseEntity {

    /**
     * 공유 코드의 고유 식별자.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 공유될 코드 스니펫.
     */
    @NotNull(message = "코드 스니펫은 필수입니다")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_snippet_id", nullable = false)
    private CodeSnippet codeSnippet;

    /**
     * 공유 식별자.
     * URL에서 사용되는 고유한 문자열입니다.
     */
    @NotBlank(message = "공유 ID는 필수입니다")
    @Pattern(regexp = "^[a-zA-Z0-9]{8,50}$", message = "공유 ID는 8-50자의 영숫자만 가능합니다")
    @Column(name = "share_id", nullable = false, unique = true)
    private String shareId;

    /**
     * 공유 만료 일시.
     * null이면 무기한 공유입니다.
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * 활성 상태.
     * 공유를 중단하거나 재개할 때 사용됩니다.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * 편의 생성자.
     *
     * @param codeSnippet 공유할 코드 스니펫
     * @param shareId     공유 식별자
     * @param expiresAt   만료 일시
     */
    public SharedCode(final CodeSnippet codeSnippet, final String shareId, final LocalDateTime expiresAt) {
        this.codeSnippet = codeSnippet;
        this.shareId = shareId;
        this.expiresAt = expiresAt;
        this.isActive = true;
    }

    /**
     * 편의 생성자 - 무기한 공유.
     *
     * @param codeSnippet 공유할 코드 스니펫
     * @param shareId     공유 식별자
     */
    public SharedCode(final CodeSnippet codeSnippet, final String shareId) {
        this(codeSnippet, shareId, null);
    }

    /**
     * 랜덤한 공유 ID를 생성합니다.
     *
     * @return 생성된 공유 ID
     */
    public static String generateShareId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    /**
     * 공유가 만료되었는지 확인합니다.
     *
     * @return 만료되었으면 true, 아니면 false
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 공유가 유효한지 확인합니다.
     * 활성 상태이고 만료되지 않은 경우에만 유효합니다.
     *
     * @return 유효하면 true, 아니면 false
     */
    public boolean isValid() {
        return isActive && !isExpired();
    }

    /**
     * 공유를 비활성화합니다.
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 공유를 활성화합니다.
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 만료 일시를 설정합니다.
     *
     * @param days 현재로부터 며칠 후 만료할지
     */
    public void setExpiresAfterDays(final int days) {
        this.expiresAt = LocalDateTime.now().plusDays(days);
    }

    /**
     * 공유 URL을 생성합니다.
     *
     * @param baseUrl 기본 URL
     * @return 완전한 공유 URL
     */
    public String generateShareUrl(final String baseUrl) {
        return baseUrl + "/share/" + shareId;
    }
}