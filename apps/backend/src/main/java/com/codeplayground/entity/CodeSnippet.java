package com.codeplayground.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 코드 스니펫 엔티티.
 * 사용자가 작성한 코드와 관련 정보를 저장합니다.
 */
@Entity
@Table(name = "code_snippets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeSnippet extends BaseEntity {

    /**
     * 코드 스니펫의 고유 식별자.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 코드 스니펫의 제목.
     */
    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 255, message = "제목은 255자를 초과할 수 없습니다")
    @Column(name = "title", nullable = false)
    private String title;

    /**
     * 실제 코드 내용.
     * 최대 10KB까지 저장 가능합니다.
     */
    @NotBlank(message = "코드는 필수입니다")
    @Size(max = 10240, message = "코드는 10KB를 초과할 수 없습니다")
    @Column(name = "code", nullable = false, columnDefinition = "TEXT")
    private String code;

    /**
     * 프로그래밍 언어.
     */
    @NotBlank(message = "프로그래밍 언어는 필수입니다")
    @Size(max = 50, message = "프로그래밍 언어는 50자를 초과할 수 없습니다")
    @Column(name = "language", nullable = false)
    private String language;

    /**
     * 작성자 이름.
     * 간단한 식별을 위해 사용됩니다.
     */
    @NotBlank(message = "작성자 이름은 필수입니다")
    @Size(max = 100, message = "작성자 이름은 100자를 초과할 수 없습니다")
    @Column(name = "author_name", nullable = false)
    private String authorName;

    /**
     * 활성 상태.
     * 소프트 삭제를 위해 사용됩니다.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * 이 코드 스니펫의 실행 기록들.
     */
    @OneToMany(mappedBy = "codeSnippet")
    @Setter(AccessLevel.NONE)
    private List<Execution> executions = new ArrayList<>();

    /**
     * 이 코드 스니펫의 공유 정보들.
     */
    @OneToMany(mappedBy = "codeSnippet")
    @Setter(AccessLevel.NONE)
    private List<SharedCode> sharedCodes = new ArrayList<>();

    /**
     * 편의 생성자.
     *
     * @param title      제목
     * @param code       코드 내용
     * @param language   프로그래밍 언어
     * @param authorName 작성자 이름
     */
    public CodeSnippet(final String title, final String code,
                       final String language, final String authorName) {
        this.title = title;
        this.code = code;
        this.language = language;
        this.authorName = authorName;
        this.isActive = true;
    }

    /**
     * 실행 기록을 추가합니다.
     *
     * @param execution 추가할 실행 기록
     */
    public void addExecution(final Execution execution) {
        executions.add(execution);
        execution.setCodeSnippet(this);
    }

    /**
     * 공유 정보를 추가합니다.
     *
     * @param sharedCode 추가할 공유 정보
     */
    public void addSharedCode(final SharedCode sharedCode) {
        sharedCodes.add(sharedCode);
        sharedCode.setCodeSnippet(this);
    }

    /**
     * 코드 스니펫을 비활성화합니다 (소프트 삭제).
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 코드 스니펫을 활성화합니다.
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 가장 최근 실행 기록을 반환합니다.
     *
     * @return 최근 실행 기록, 없으면 null
     */
    public Execution getLatestExecution() {
        return executions.stream()
                .max((e1, e2) -> e1.getCreatedAt().compareTo(e2.getCreatedAt()))
                .orElse(null);
    }
}