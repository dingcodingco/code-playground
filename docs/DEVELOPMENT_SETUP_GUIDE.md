# 개발환경 설정 가이드

## 📋 개요

바이브코딩 데모 프로젝트의 로컬 개발환경 설정부터 AWS 배포까지의 전체 가이드입니다.

## 🛠️ 사전 요구사항

### 필수 도구 설치

#### 1. 기본 개발 도구
```bash
# Node.js (v18 이상)
# https://nodejs.org/에서 설치 또는
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
nvm install 18
nvm use 18

# Java JDK 21
# https://adoptium.net/에서 설치 또는
# macOS
brew install openjdk@21

# Windows
choco install openjdk21

# Ubuntu
sudo apt-get install openjdk-21-jdk
```

#### 2. 컨테이너 도구
```bash
# Docker Desktop 설치
# https://www.docker.com/products/docker-desktop

# Docker Compose 설치 (Docker Desktop에 포함)
docker --version
docker-compose --version
```

#### 3. AWS CLI 및 Terraform
```bash
# AWS CLI v2
# https://aws.amazon.com/cli/

# Terraform
# https://terraform.io/downloads

# 설치 확인
aws --version
terraform --version
```

#### 4. IDE 및 확장 프로그램
**VS Code 권장 확장 프로그램**:
- ES7+ React/Redux/React-Native snippets
- Prettier - Code formatter
- ESLint
- GitLens
- Docker
- HashiCorp Terraform
- Thunder Client (API 테스트용)

## 📂 프로젝트 설정

### 1. 저장소 클론 및 초기 설정
```bash
# 프로젝트 클론
git clone https://github.com/instructor/code-playground-demo.git
cd code-playground-demo

# 프로젝트 구조 확인
tree -I 'node_modules|.git|build|.next'
```

### 2. 환경 변수 설정

#### Frontend 환경변수
```bash
# apps/frontend/.env.local 생성
cd apps/frontend
cat > .env.local << EOF
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_APP_NAME=바이브코딩 데모
EOF
```

#### Backend 환경변수
```bash
# apps/backend/src/main/resources/application-local.yml 생성
cd apps/backend/src/main/resources
cat > application-local.yml << 'EOF'
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driverClassName: org.h2.Driver
    username: sa
    password:

  h2:
    console:
      enabled: true
      path: /h2-console

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true

  profiles:
    active: local

server:
  port: 8080

logging:
  level:
    com.codeplayground: DEBUG
    org.springframework.web: DEBUG
EOF
```

### 3. 의존성 설치

#### Frontend 의존성 설치
```bash
cd apps/frontend

# 의존성 설치
npm install

# package.json 확인
cat package.json
```

#### Backend 빌드 테스트
```bash
cd apps/backend

# Gradle 빌드 테스트
./gradlew build --no-daemon

# 빌드 결과 확인
ls -la build/libs/
```

## 🚀 로컬 개발 환경 실행

### 방법 1: Docker Compose (권장)
```bash
# 프로젝트 루트에서 실행
docker-compose up --build

# 백그라운드 실행
docker-compose up -d --build

# 로그 확인
docker-compose logs -f

# 서비스 중단
docker-compose down
```

**접속 URL**:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- H2 Database Console: http://localhost:8080/h2-console

### 방법 2: 개별 서비스 실행

#### Terminal 1 - Backend 실행
```bash
cd apps/backend
./gradlew bootRun

# 또는 IDE에서 VibecodingApplication.java 실행
```

#### Terminal 2 - Frontend 실행
```bash
cd apps/frontend
npm run dev

# 개발 모드 옵션들
npm run dev          # 개발 서버 시작
npm run build        # 프로덕션 빌드
npm run start        # 프로덕션 모드 실행
npm run lint         # ESLint 실행
npm run type-check   # TypeScript 타입 체크
```

### 3. 개발 환경 검증

#### API 테스트
```bash
# 헬스체크 API
curl http://localhost:8080/api/health

# 코드 실행 API 테스트
curl -X POST http://localhost:8080/api/code/execute \
  -H "Content-Type: application/json" \
  -d '{
    "code": "console.log(\"Hello World\");",
    "language": "javascript",
    "authorName": "테스트유저"
  }'
```

#### 프론트엔드 테스트
1. http://localhost:3000 접속
2. 코드 에디터에 간단한 JavaScript 코드 입력
3. "실행" 버튼 클릭
4. 결과 패널에서 출력 확인

## 📁 상세 프로젝트 구조

```
code-playground-demo/
├── apps/
│   ├── frontend/                    # Next.js 애플리케이션
│   │   ├── app/                    # App Router (Next.js 13+)
│   │   │   ├── globals.css         # 전역 스타일
│   │   │   ├── layout.tsx          # 루트 레이아웃
│   │   │   ├── page.tsx            # 메인 페이지
│   │   │   ├── share/[id]/page.tsx # 코드 공유 페이지
│   │   │   └── api/                # API Routes
│   │   │       └── proxy/route.ts  # 백엔드 API 프록시
│   │   ├── components/             # 재사용 컴포넌트
│   │   │   ├── CodeEditor.tsx      # Monaco 에디터
│   │   │   ├── ExecutionPanel.tsx  # 실행 결과 패널
│   │   │   ├── LanguageSelector.tsx # 언어 선택기
│   │   │   └── ShareButton.tsx     # 공유 버튼
│   │   ├── lib/                    # 유틸리티
│   │   │   ├── api.ts              # API 클라이언트
│   │   │   ├── store.ts            # Zustand 스토어
│   │   │   └── utils.ts            # 공통 유틸리티
│   │   ├── public/                 # 정적 파일
│   │   ├── .env.local              # 환경 변수
│   │   ├── .env.example            # 환경 변수 예시
│   │   ├── Dockerfile              # 프론트엔드 Docker 이미지
│   │   ├── next.config.js          # Next.js 설정
│   │   ├── package.json            # NPM 의존성
│   │   ├── tailwind.config.js      # Tailwind CSS 설정
│   │   └── tsconfig.json           # TypeScript 설정
│   └── backend/                    # Spring Boot 애플리케이션
│       ├── src/main/java/com/codeplayground/
│       │   ├── controller/         # REST 컨트롤러
│       │   │   ├── CodeController.java      # 코드 실행 API
│       │   │   ├── ShareController.java     # 공유 API
│       │   │   └── HealthController.java    # 헬스체크 API
│       │   ├── service/            # 비즈니스 로직
│       │   │   ├── CodeExecutionService.java
│       │   │   ├── ShareService.java
│       │   │   └── ExecutionEngineService.java
│       │   ├── entity/             # JPA 엔티티
│       │   │   ├── CodeSnippet.java
│       │   │   ├── Execution.java
│       │   │   └── SharedCode.java
│       │   ├── repository/         # JPA 리포지토리
│       │   │   ├── CodeSnippetRepository.java
│       │   │   ├── ExecutionRepository.java
│       │   │   └── SharedCodeRepository.java
│       │   ├── config/             # Spring 설정
│       │   │   ├── CorsConfig.java
│       │   │   ├── SecurityConfig.java
│       │   │   └── JpaConfig.java
│       │   ├── dto/                # 데이터 전송 객체
│       │   │   ├── request/
│       │   │   └── response/
│       │   ├── exception/          # 예외 처리
│       │   └── VibecodingApplication.java  # 메인 클래스
│       ├── src/main/resources/
│       │   ├── application.yml     # 기본 설정
│       │   ├── application-dev.yml # 개발환경 설정
│       │   ├── application-prod.yml # 프로덕션 설정
│       │   └── data.sql           # 초기 데이터
│       ├── src/test/              # 테스트 코드
│       ├── Dockerfile             # 백엔드 Docker 이미지
│       ├── build.gradle           # Gradle 빌드 스크립트
│       └── gradlew               # Gradle 래퍼
├── infrastructure/               # Terraform 인프라 코드
├── docker/                      # Docker 설정
│   ├── docker-compose.yml       # 로컬 개발용
│   ├── docker-compose.prod.yml  # 프로덕션 테스트용
│   └── nginx.conf              # Nginx 설정
├── scripts/                    # 유틸리티 스크립트
├── .github/workflows/          # GitHub Actions
└── docs/                      # 프로젝트 문서
```

## 🔧 개발 도구 설정

### VS Code 설정
`.vscode/settings.json`:
```json
{
  "editor.formatOnSave": true,
  "editor.codeActionsOnSave": {
    "source.fixAll.eslint": true
  },
  "typescript.preferences.importModuleSpecifier": "relative",
  "files.exclude": {
    "**/node_modules": true,
    "**/build": true,
    "**/.next": true
  }
}
```

`.vscode/launch.json` (디버깅 설정):
```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "name": "Next.js: debug server-side",
      "type": "node-terminal",
      "request": "launch",
      "command": "npm run dev",
      "cwd": "${workspaceFolder}/apps/frontend"
    },
    {
      "name": "Spring Boot Debug",
      "type": "java",
      "request": "launch",
      "mainClass": "com.codeplayground.VibecodingApplication",
      "projectName": "backend"
    }
  ]
}
```

### Git 설정
`.gitignore` (이미 포함됨):
```gitignore
# Dependencies
node_modules/
npm-debug.log*
yarn-debug.log*
yarn-error.log*

# Production builds
.next/
build/
dist/

# Environment variables
.env.local
.env.development.local
.env.test.local
.env.production.local

# IDE
.vscode/
.idea/
*.swp
*.swo
*~

# OS
.DS_Store
Thumbs.db

# Logs
logs
*.log

# Runtime data
pids
*.pid
*.seed
*.pid.lock

# Terraform
*.tfstate
*.tfstate.*
.terraform/
.terraform.lock.hcl
```

## 🐛 트러블슈팅

### 자주 발생하는 문제들

#### 1. 포트 충돌
```bash
# 포트 사용 중인 프로세스 확인
lsof -ti:3000
lsof -ti:8080

# 프로세스 종료
kill $(lsof -ti:3000)
kill $(lsof -ti:8080)
```

#### 2. Docker 관련 문제
```bash
# Docker 컨테이너 정리
docker-compose down
docker system prune -f

# 이미지 재빌드
docker-compose build --no-cache
docker-compose up --build
```

#### 3. 의존성 문제
```bash
# Frontend 의존성 재설치
cd apps/frontend
rm -rf node_modules package-lock.json
npm install

# Backend 의존성 정리
cd apps/backend
./gradlew clean build
```

#### 4. 데이터베이스 연결 문제
```bash
# H2 Console 접속 정보
JDBC URL: jdbc:h2:mem:testdb
User Name: sa
Password: (비어있음)
```

### 로그 확인 방법

#### Docker Compose 로그
```bash
# 전체 로그
docker-compose logs

# 특정 서비스 로그
docker-compose logs frontend
docker-compose logs backend

# 실시간 로그 모니터링
docker-compose logs -f
```

#### 개별 서비스 로그
```bash
# Frontend 개발 서버 로그
cd apps/frontend
npm run dev

# Backend 애플리케이션 로그
cd apps/backend
./gradlew bootRun
```

## 📚 다음 단계

개발환경 설정이 완료되면:

1. **기능 구현**: 코드 에디터, 실행 엔진, 공유 기능 구현
2. **테스트 작성**: 단위 테스트 및 통합 테스트 작성
3. **AWS 배포**: Terraform으로 인프라 구축 및 애플리케이션 배포
4. **CI/CD 설정**: GitHub Actions 워크플로우 설정
5. **모니터링**: CloudWatch 로그 및 메트릭 설정

각 단계별 상세 가이드는 별도 문서를 참조하세요.

## 🔗 유용한 링크

- [Next.js 문서](https://nextjs.org/docs)
- [Spring Boot 문서](https://spring.io/projects/spring-boot)
- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [Docker Compose 문서](https://docs.docker.com/compose/)
- [GitHub Actions 문서](https://docs.github.com/en/actions)