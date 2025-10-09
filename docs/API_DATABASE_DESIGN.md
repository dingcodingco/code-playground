# API 설계 및 데이터베이스 스키마 문서

## 📋 개요

Code Playground 서비스의 REST API 설계와 데이터베이스 스키마를 정의하는 문서입니다.

## 🌐 API 설계

### 기본 정보
- **Base URL**: `https://api.codeplayground.com` (프로덕션)
- **Base URL**: `http://localhost:8080` (로컬 개발)
- **Content-Type**: `application/json`
- **Authentication**: Session-based (간단한 구현)

### API 엔드포인트 구조

#### 1. 코드 실행 API

**POST /api/code/execute**
```json
{
  "description": "코드를 실행하고 결과를 반환",
  "request": {
    "code": "console.log('Hello World');",
    "language": "javascript",
    "authorName": "김개발자"
  },
  "response": {
    "success": true,
    "executionId": "exec_1234567890",
    "output": "Hello World\n",
    "error": null,
    "executionTime": 156,
    "timestamp": "2024-01-01T12:00:00Z"
  },
  "errorResponse": {
    "success": false,
    "error": "Syntax Error: Unexpected token",
    "executionTime": 23,
    "timestamp": "2024-01-01T12:00:00Z"
  }
}
```

**지원 언어**:
- `javascript`: Node.js 환경
- `python`: Python 3.11
- `java`: OpenJDK 21

#### 2. 코드 저장 API

**POST /api/code/save**
```json
{
  "description": "코드 스니펫을 저장",
  "request": {
    "code": "console.log('Hello World');",
    "language": "javascript",
    "title": "첫 번째 프로그램",
    "authorName": "김개발자"
  },
  "response": {
    "success": true,
    "codeId": "code_1234567890",
    "shareUrl": "https://codeplayground.com/share/abc123def",
    "createdAt": "2024-01-01T12:00:00Z"
  }
}
```

**GET /api/code/{codeId}**
```json
{
  "description": "저장된 코드 조회",
  "response": {
    "success": true,
    "data": {
      "id": "code_1234567890",
      "code": "console.log('Hello World');",
      "language": "javascript",
      "title": "첫 번째 프로그램",
      "authorName": "김개발자",
      "createdAt": "2024-01-01T12:00:00Z",
      "lastExecuted": "2024-01-01T12:30:00Z"
    }
  }
}
```

#### 3. 코드 공유 API

**POST /api/share**
```json
{
  "description": "공유 가능한 URL 생성",
  "request": {
    "codeId": "code_1234567890",
    "includeResult": true
  },
  "response": {
    "success": true,
    "shareId": "abc123def",
    "shareUrl": "https://codeplayground.com/share/abc123def",
    "expiresAt": "2024-02-01T12:00:00Z"
  }
}
```

**GET /api/share/{shareId}**
```json
{
  "description": "공유된 코드 및 실행 결과 조회",
  "response": {
    "success": true,
    "data": {
      "shareId": "abc123def",
      "code": "console.log('Hello World');",
      "language": "javascript",
      "title": "첫 번째 프로그램",
      "authorName": "김개발자",
      "lastResult": {
        "output": "Hello World\n",
        "executionTime": 156,
        "timestamp": "2024-01-01T12:30:00Z"
      },
      "createdAt": "2024-01-01T12:00:00Z"
    }
  }
}
```

#### 4. 사용자 히스토리 API

**GET /api/user/{authorName}/codes**
```json
{
  "description": "사용자별 코드 히스토리 조회",
  "queryParams": {
    "page": 1,
    "size": 10,
    "language": "javascript"
  },
  "response": {
    "success": true,
    "data": {
      "content": [
        {
          "id": "code_1234567890",
          "title": "첫 번째 프로그램",
          "language": "javascript",
          "createdAt": "2024-01-01T12:00:00Z",
          "lastExecuted": "2024-01-01T12:30:00Z"
        }
      ],
      "totalElements": 25,
      "totalPages": 3,
      "currentPage": 1,
      "size": 10
    }
  }
}
```

#### 5. 통계 API (관리자/모니터링용)

**GET /api/stats/overview**
```json
{
  "description": "전체 서비스 통계",
  "response": {
    "success": true,
    "data": {
      "totalCodes": 1500,
      "totalExecutions": 5200,
      "activeUsers": 120,
      "languageStats": {
        "javascript": 850,
        "python": 450,
        "java": 200
      },
      "dailyExecutions": 340
    }
  }
}
```

### 에러 응답 형식

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "코드가 너무 길습니다 (최대 10KB)",
    "details": {
      "field": "code",
      "maxSize": "10KB",
      "currentSize": "15KB"
    }
  },
  "timestamp": "2024-01-01T12:00:00Z"
}
```

**에러 코드 목록**:
- `VALIDATION_ERROR`: 입력 데이터 검증 실패
- `EXECUTION_TIMEOUT`: 코드 실행 시간 초과
- `EXECUTION_ERROR`: 코드 실행 중 오류 발생
- `RESOURCE_NOT_FOUND`: 요청한 리소스가 없음
- `RATE_LIMIT_EXCEEDED`: API 호출 제한 초과
- `INTERNAL_SERVER_ERROR`: 서버 내부 오류

## 🗄️ 데이터베이스 스키마

### ERD (Entity Relationship Diagram)
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   code_snippets │    │  executions     │    │  shared_codes   │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ id (PK)         │───▶│ id (PK)         │    │ id (PK)         │
│ title           │    │ code_snippet_id │    │ code_snippet_id │─┐
│ code            │    │ output          │    │ share_id        │ │
│ language        │    │ error_message   │    │ expires_at      │ │
│ author_name     │    │ execution_time  │    │ created_at      │ │
│ created_at      │    │ status          │    │ is_active       │ │
│ updated_at      │    │ created_at      │    └─────────────────┘ │
│ is_active       │    └─────────────────┘                      │
└─────────────────┘                                             │
         ▲                                                      │
         └──────────────────────────────────────────────────────┘
```

### 테이블 상세 정의

#### 1. code_snippets (코드 스니펫)
```sql
CREATE TABLE code_snippets (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    code TEXT NOT NULL,
    language VARCHAR(50) NOT NULL CHECK (language IN ('javascript', 'python', 'java')),
    author_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,

    -- 인덱스
    CONSTRAINT chk_code_length CHECK (LENGTH(code) <= 10240), -- 10KB 제한
    CONSTRAINT chk_title_length CHECK (LENGTH(title) <= 255)
);

-- 인덱스 생성
CREATE INDEX idx_code_snippets_author_name ON code_snippets(author_name);
CREATE INDEX idx_code_snippets_language ON code_snippets(language);
CREATE INDEX idx_code_snippets_created_at ON code_snippets(created_at);
CREATE INDEX idx_code_snippets_active ON code_snippets(is_active) WHERE is_active = TRUE;
```

**필드 설명**:
- `id`: 기본 키, 자동 증가
- `title`: 코드 스니펫 제목
- `code`: 실제 소스 코드 (최대 10KB)
- `language`: 프로그래밍 언어 (javascript, python, java)
- `author_name`: 작성자 이름 (간단한 식별자)
- `created_at`: 생성 일시
- `updated_at`: 수정 일시
- `is_active`: 활성 상태 (소프트 삭제용)

#### 2. executions (실행 기록)
```sql
CREATE TABLE executions (
    id BIGSERIAL PRIMARY KEY,
    code_snippet_id BIGINT REFERENCES code_snippets(id) ON DELETE CASCADE,
    output TEXT,
    error_message TEXT,
    execution_time INTEGER NOT NULL DEFAULT 0, -- 밀리초 단위
    status VARCHAR(20) NOT NULL CHECK (status IN ('SUCCESS', 'ERROR', 'TIMEOUT')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    -- 제약 조건
    CONSTRAINT chk_execution_time CHECK (execution_time >= 0),
    CONSTRAINT chk_output_or_error CHECK (
        (status = 'SUCCESS' AND output IS NOT NULL) OR
        (status IN ('ERROR', 'TIMEOUT') AND error_message IS NOT NULL)
    )
);

-- 인덱스 생성
CREATE INDEX idx_executions_code_snippet_id ON executions(code_snippet_id);
CREATE INDEX idx_executions_status ON executions(status);
CREATE INDEX idx_executions_created_at ON executions(created_at);

-- 최근 실행 결과 조회를 위한 복합 인덱스
CREATE INDEX idx_executions_snippet_created ON executions(code_snippet_id, created_at DESC);
```

**필드 설명**:
- `id`: 기본 키, 자동 증가
- `code_snippet_id`: 실행된 코드 스니펫 참조
- `output`: 실행 결과 출력
- `error_message`: 에러 메시지 (실패 시)
- `execution_time`: 실행 시간 (밀리초)
- `status`: 실행 상태 (SUCCESS, ERROR, TIMEOUT)
- `created_at`: 실행 일시

#### 3. shared_codes (공유 코드)
```sql
CREATE TABLE shared_codes (
    id BIGSERIAL PRIMARY KEY,
    code_snippet_id BIGINT REFERENCES code_snippets(id) ON DELETE CASCADE,
    share_id VARCHAR(50) UNIQUE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,

    -- 제약 조건
    CONSTRAINT chk_share_id_format CHECK (share_id ~ '^[a-zA-Z0-9]{8,50}$'),
    CONSTRAINT chk_expires_future CHECK (expires_at IS NULL OR expires_at > created_at)
);

-- 인덱스 생성
CREATE UNIQUE INDEX idx_shared_codes_share_id ON shared_codes(share_id);
CREATE INDEX idx_shared_codes_code_snippet_id ON shared_codes(code_snippet_id);
CREATE INDEX idx_shared_codes_expires_at ON shared_codes(expires_at) WHERE expires_at IS NOT NULL;
CREATE INDEX idx_shared_codes_active ON shared_codes(is_active) WHERE is_active = TRUE;
```

**필드 설명**:
- `id`: 기본 키, 자동 증가
- `code_snippet_id`: 공유할 코드 스니펫 참조
- `share_id`: 공유용 고유 식별자 (URL에 사용)
- `expires_at`: 공유 만료 시간 (NULL이면 무기한)
- `created_at`: 공유 생성 일시
- `is_active`: 활성 상태

### 데이터베이스 설정

#### 연결 설정
```yaml
# application-dev.yml (개발환경)
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

# application-prod.yml (프로덕션)
spring:
  datasource:
    url: ${DATABASE_URL}
    driver-class-name: org.postgresql.Driver
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

#### 커넥션 풀 설정
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
```

## 🔄 데이터 플로우

### 1. 코드 실행 플로우
```
사용자 요청 → Controller → Service → 코드 실행 → 결과 저장 → 응답
```

### 2. 코드 공유 플로우
```
코드 저장 → 공유 ID 생성 → shared_codes 테이블 저장 → 공유 URL 반환
```

### 3. 히스토리 조회 플로우
```
사용자 요청 → Repository → JPA 쿼리 → 페이징 처리 → 응답
```

## 📊 성능 최적화

### 데이터베이스 최적화
- **인덱스 전략**: 자주 조회되는 컬럼에 인덱스 생성
- **페이징**: 대용량 데이터 조회 시 페이징 처리
- **캐싱**: 자주 조회되는 데이터 Redis 캐싱
- **연결 풀**: HikariCP로 연결 풀 최적화

### API 최적화
- **응답 압축**: Gzip 압축 활성화
- **캐시 헤더**: 정적 데이터에 캐시 헤더 설정
- **비동기 처리**: 코드 실행은 비동기로 처리
- **레이트 리미팅**: API 호출 제한 설정

## 🔒 보안 고려사항

### API 보안
- **입력 검증**: 모든 입력 데이터 검증
- **코드 실행 격리**: 샌드박스 환경에서 코드 실행
- **레이트 리미팅**: DoS 공격 방지
- **CORS 설정**: 적절한 CORS 정책 설정

### 데이터베이스 보안
- **SQL 인젝션 방지**: Prepared Statement 사용
- **접근 권한**: 최소 권한 원칙 적용
- **데이터 암호화**: 중요 데이터 암호화 저장
- **백업**: 정기적인 데이터베이스 백업

이 API 설계와 데이터베이스 스키마는 확장성과 유지보수성을 고려하여 설계되었으며, 교육 목적에 적합한 구조를 제공합니다.