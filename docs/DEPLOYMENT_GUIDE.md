# CodePlayground 배포 가이드

## 🐳 Docker 배포 가이드

### 필수 준비사항

1. **Docker 설치 확인**
   ```bash
   docker --version
   docker-compose --version
   ```

2. **Docker 데몬 실행 확인**
   - Docker Desktop 애플리케이션이 실행 중인지 확인
   - 또는 시스템에서 Docker 서비스가 실행 중인지 확인

### 배포 단계

#### 1단계: 전체 스택 배포

```bash
# 프로젝트 루트 디렉토리로 이동
cd /path/to/infra-test

# 전체 서비스 시작 (PostgreSQL + Backend + Frontend)
docker-compose up -d
```

#### 2단계: 개별 서비스 배포

```bash
# PostgreSQL만 먼저 시작
docker-compose up -d postgres

# 백엔드 서비스 시작
docker-compose up -d backend

# 프론트엔드 서비스 시작
docker-compose up -d frontend
```

#### 3단계: 배포 상태 확인

```bash
# 모든 컨테이너 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f

# 개별 서비스 로그 확인
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres
```

### 서비스 접근

- **프론트엔드**: http://localhost:3000
- **백엔드 API**: http://localhost:8080
- **백엔드 Health Check**: http://localhost:8080/actuator/health
- **PostgreSQL**: localhost:5432 (DB명: codeplayground, 사용자: codeplayground)

### 환경 변수 설정

배포용 환경변수는 `docker-compose.yml` 파일에 정의되어 있습니다:

```env
# 데이터베이스 설정
POSTGRES_DB=codeplayground
POSTGRES_USER=codeplayground
POSTGRES_PASSWORD=codeplayground123

# 백엔드 설정
SPRING_PROFILES_ACTIVE=docker
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/codeplayground

# 프론트엔드 설정
NODE_ENV=production
NEXT_PUBLIC_API_BASE_URL=http://backend:8080/api/v1
```

### 헬스체크

모든 서비스는 헬스체크가 구성되어 있습니다:

- **Backend**: `curl -f http://localhost:8080/actuator/health`
- **Frontend**: `node healthcheck.js`
- **PostgreSQL**: `pg_isready -U codeplayground -d codeplayground`

### 배포 검증

#### API 테스트

```bash
# 백엔드 헬스체크
curl http://localhost:8080/actuator/health

# 코드 스니펫 생성 테스트
curl -X POST http://localhost:8080/api/v1/snippets \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Snippet",
    "authorName": "Test User",
    "language": "JAVASCRIPT",
    "content": "console.log(\"Hello World\");"
  }'

# 코드 실행 테스트
curl -X POST http://localhost:8080/api/v1/executions/execute \
  -H "Content-Type: application/json" \
  -d '{
    "codeSnippetId": 1
  }'
```

#### 프론트엔드 테스트

브라우저에서 http://localhost:3000 접속하여:
1. 코드 에디터가 정상 로드되는지 확인
2. 코드 스니펫 작성 및 저장 테스트
3. 코드 실행 기능 테스트

### 문제 해결

#### 1. Docker 데몬 연결 오류
```
Error: Cannot connect to the Docker daemon
```
**해결방법**: Docker Desktop을 시작하거나 Docker 서비스를 재시작

#### 2. 포트 충돌 오류
```
Error: Port is already in use
```
**해결방법**:
- 기존 서비스 종료: `docker-compose down`
- 개별 포트 사용 중인 프로세스 확인 및 종료
- 다른 포트로 변경 (docker-compose.yml 수정)

#### 3. 데이터베이스 연결 오류
```
Error: Connection refused to PostgreSQL
```
**해결방법**:
- PostgreSQL 컨테이너 상태 확인: `docker-compose ps postgres`
- 헬스체크 로그 확인: `docker-compose logs postgres`
- 의존성 순서 확인 (depends_on 설정)

#### 4. 빌드 오류
```
Error: Build failed
```
**해결방법**:
- 빌드 캐시 삭제: `docker-compose build --no-cache`
- 개별 서비스 빌드: `docker-compose build backend`
- Dockerfile 구문 검토

### 배포 중단

```bash
# 모든 서비스 중단
docker-compose down

# 볼륨까지 삭제 (데이터 완전 삭제)
docker-compose down -v

# 이미지까지 삭제
docker-compose down --rmi all
```

### 데이터 백업 및 복원

#### PostgreSQL 데이터 백업
```bash
# 컨테이너에서 덤프 생성
docker-compose exec postgres pg_dump -U codeplayground codeplayground > backup.sql
```

#### PostgreSQL 데이터 복원
```bash
# 덤프 파일에서 복원
docker-compose exec -T postgres psql -U codeplayground codeplayground < backup.sql
```

### 로그 관리

```bash
# 실시간 로그 모니터링
docker-compose logs -f --tail=100

# 로그 파일 저장
docker-compose logs > deployment.log

# 특정 시점 이후 로그
docker-compose logs --since="2024-01-01T00:00:00"
```

## 🚀 프로덕션 배포 고려사항

### 보안 설정

1. **환경변수 보안**
   - 프로덕션용 강력한 패스워드 설정
   - 환경변수 파일을 Git에서 제외 (.gitignore 추가)
   - Docker Secrets 또는 외부 비밀 관리 시스템 사용

2. **네트워크 보안**
   - 역방향 프록시 (Nginx) 설정
   - HTTPS/SSL 인증서 적용
   - 방화벽 규칙 설정

### 성능 최적화

1. **리소스 제한**
   ```yaml
   # docker-compose.yml에 추가
   deploy:
     resources:
       limits:
         memory: 1G
         cpus: '0.5'
   ```

2. **데이터베이스 최적화**
   - 커넥션 풀 설정 최적화
   - 인덱스 최적화
   - 정기적인 VACUUM 작업

### 모니터링 설정

1. **로그 수집**
   - ELK Stack 또는 Fluentd 설정
   - 구조화된 로그 포맷 적용

2. **메트릭 수집**
   - Prometheus + Grafana 설정
   - 애플리케이션 메트릭 노출

### 고가용성 (HA) 설정

1. **로드 밸런서**
   - 다중 인스턴스 배포
   - 로드 밸런서 설정 (HAProxy, Nginx)

2. **데이터베이스 클러스터**
   - PostgreSQL 마스터-슬레이브 설정
   - 자동 페일오버 구성

이 가이드를 참고하여 CodePlayground 애플리케이션을 성공적으로 배포하세요.