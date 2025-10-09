# Code Playground 데모 프로젝트 설계 문서

## 📋 프로젝트 개요

### 목적
수강생들을 위한 Docker 기반 Code Playground 서비스 데모 프로젝트로, 풀스택 개발부터 AWS 배포까지의 전체 과정을 학습할 수 있도록 설계

### 핵심 목표
- Docker 컨테이너화 실습
- 모노레포 구조로 프론트엔드/백엔드 통합 관리
- AWS ECS Fargate를 통한 서버리스 컨테이너 배포
- Terraform을 활용한 Infrastructure as Code
- GitHub Actions를 통한 CI/CD 자동화

## 🏗️ 시스템 아키텍처

### 전체 아키텍처 다이어그램
```
┌─────────────────┐    ┌─────────────────┐
│   사용자        │    │    GitHub       │
│  (브라우저)     │    │   Repository    │
└─────────┬───────┘    └─────────┬───────┘
          │                      │
          │ HTTPS                │ Git Push
          ▼                      ▼
┌─────────────────────────────────────────┐
│            AWS Cloud                    │
│                                         │
│  ┌─────────────────┐                   │
│  │ Application     │                   │
│  │ Load Balancer   │                   │
│  └─────────┬───────┘                   │
│            │                           │
│    ┌───────┴────────┐                 │
│    ▼                ▼                  │
│  ┌─────────┐    ┌─────────┐           │
│  │Frontend │    │Backend  │           │
│  │ECS Task │    │ECS Task │           │
│  │Next.js  │    │Spring   │           │
│  └─────────┘    └────┬────┘           │
│                      │                 │
│                      ▼                 │
│              ┌─────────────┐           │
│              │     RDS     │           │
│              │ PostgreSQL  │           │
│              └─────────────┘           │
└─────────────────────────────────────────┘
```

### 기술 스택

#### Frontend
- **Framework**: Next.js 14 (App Router)
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **Code Editor**: Monaco Editor
- **State Management**: Zustand
- **HTTP Client**: Fetch API

#### Backend
- **Framework**: Spring Boot 3.x
- **Language**: Java 21
- **Database**: H2 (개발) / PostgreSQL (프로덕션)
- **ORM**: JPA/Hibernate
- **Build Tool**: Gradle
- **Authentication**: Session-based (간단한 인증)

#### Infrastructure
- **Containerization**: Docker + Docker Compose
- **Cloud Provider**: AWS
- **Container Orchestration**: ECS Fargate
- **Database**: RDS PostgreSQL
- **Load Balancer**: Application Load Balancer
- **Infrastructure as Code**: Terraform
- **CI/CD**: GitHub Actions
- **Container Registry**: Amazon ECR

## 📁 프로젝트 구조 (모노레포)

```
code-playground-demo/
├── apps/
│   ├── frontend/                 # Next.js 애플리케이션
│   │   ├── app/                 # App Router 구조
│   │   │   ├── layout.tsx
│   │   │   ├── page.tsx         # 메인 코드 에디터 페이지
│   │   │   ├── share/[id]/page.tsx  # 공유 코드 보기
│   │   │   └── api/             # API 라우트
│   │   ├── components/          # 재사용 컴포넌트
│   │   │   ├── CodeEditor.tsx   # Monaco 에디터 래퍼
│   │   │   ├── ExecutionPanel.tsx # 실행 결과 표시
│   │   │   ├── LanguageSelector.tsx
│   │   │   └── ShareButton.tsx
│   │   ├── lib/                 # 유틸리티 및 설정
│   │   │   ├── store.ts         # Zustand 상태 관리
│   │   │   └── api.ts           # API 클라이언트
│   │   ├── Dockerfile
│   │   ├── package.json
│   │   └── next.config.js
│   └── backend/                 # Spring Boot API
│       ├── src/main/java/com/codeplayground/
│       │   ├── controller/      # REST 컨트롤러
│       │   │   ├── CodeController.java
│       │   │   └── ShareController.java
│       │   ├── service/         # 비즈니스 로직
│       │   │   ├── CodeExecutionService.java
│       │   │   └── ShareService.java
│       │   ├── entity/          # JPA 엔티티
│       │   │   ├── CodeSnippet.java
│       │   │   └── ExecutionResult.java
│       │   ├── repository/      # 데이터 액세스
│       │   │   └── CodeSnippetRepository.java
│       │   ├── config/          # 설정 클래스
│       │   │   ├── CorsConfig.java
│       │   │   └── SecurityConfig.java
│       │   └── CodePlaygroundApplication.java
│       ├── src/main/resources/
│       │   ├── application.yml
│       │   ├── application-dev.yml
│       │   └── application-prod.yml
│       ├── Dockerfile
│       └── build.gradle
├── infrastructure/              # Terraform 인프라 코드
│   ├── modules/                # 재사용 가능한 모듈
│   │   ├── networking/         # VPC, 서브넷, 라우팅
│   │   ├── ecs/               # ECS 클러스터, 서비스, 태스크
│   │   ├── load-balancer/     # ALB, 타겟 그룹
│   │   ├── rds/               # RDS 인스턴스
│   │   └── security/          # 보안 그룹, IAM 역할
│   └── environments/          # 환경별 설정
│       ├── dev/
│       ├── staging/
│       └── prod/
├── docker/                     # 로컬 개발용 Docker 설정
│   ├── docker-compose.yml     # 로컬 개발 환경
│   └── nginx.conf             # Nginx 설정
├── scripts/                    # 유틸리티 스크립트
│   ├── dev-start.sh           # 개발 환경 시작
│   ├── build.sh               # 빌드 스크립트
│   └── cleanup-aws.sh         # AWS 리소스 정리
├── .github/workflows/         # GitHub Actions
│   ├── deploy-infrastructure.yml
│   └── deploy-application.yml
├── README.md
├── .gitignore
└── docker-compose.yml         # 루트 레벨 개발용 설정
```

## 🎯 핵심 기능 설계

### 1. 코드 실행 서비스
- **실시간 코드 편집**: Monaco Editor 통합
- **다중 언어 지원**: JavaScript, Python, Java
- **안전한 코드 실행**: 별도 컨테이너에서 실행 (보안)
- **실행 결과 표시**: 콘솔 출력, 에러 메시지, 실행 시간

### 2. 코드 공유 시스템
- **고유 URL 생성**: 각 코드 스니펫마다 공유 가능한 URL
- **코드 저장**: 데이터베이스에 코드와 메타데이터 저장
- **실시간 결과 공유**: 코드와 실행 결과 함께 공유

### 3. 사용자 시스템 (간단)
- **세션 기반 인증**: 이름만으로 간단한 사용자 식별
- **코드 히스토리**: 사용자별 코드 작성 이력
- **개인 대시보드**: 저장된 코드 목록

## 🔧 개발 환경 구성

### 로컬 개발 환경
```bash
# 전체 환경 시작
docker-compose up --build

# 개별 서비스 개발 모드
cd apps/frontend && npm run dev
cd apps/backend && ./gradlew bootRun
```

### 환경 변수
```bash
# Frontend: 기본값은 next.config.js에 정의
# NEXT_PUBLIC_API_BASE_URL: http://localhost:8080/api/v1

# Backend (application-dev.yml)
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  profiles:
    active: dev
```

## 🚀 배포 전략

### 개발 → 프로덕션 플로우
1. **로컬 개발**: Docker Compose로 전체 스택 개발
2. **코드 커밋**: GitHub에 푸시
3. **인프라 프로비저닝**: Terraform으로 AWS 리소스 생성
4. **애플리케이션 배포**: GitHub Actions로 자동 빌드 및 배포
5. **모니터링**: CloudWatch로 로그 및 메트릭 모니터링

### 배포 환경별 특성
- **개발**: H2 인메모리 DB, 단일 인스턴스
- **스테이징**: RDS 연결, 로드밸런싱 테스트
- **프로덕션**: Multi-AZ RDS, Auto Scaling, SSL 인증서

## 📚 교육적 가치

### 학습 목표
1. **컨테이너화**: Docker를 활용한 애플리케이션 패키징
2. **마이크로서비스**: 프론트엔드/백엔드 분리 아키텍처
3. **클라우드 네이티브**: AWS 서비스 활용 및 서버리스 컨테이너
4. **자동화**: IaC와 CI/CD를 통한 배포 자동화
5. **모니터링**: 클라우드 환경에서의 로그 및 메트릭 관리

### 실습 시나리오
1. **30분**: 로컬 개발 환경 구축 및 기능 테스트
2. **30분**: Terraform으로 AWS 인프라 구축
3. **15분**: GitHub Actions를 통한 자동 배포
4. **15분**: 모니터링 및 트러블슈팅

## 💰 비용 관리

### 예상 AWS 비용 (월간)
- ECS Fargate: $30-50
- RDS t3.micro: $13-26
- ALB: $23
- NAT Gateway: $32
- 기타 (데이터 전송, CloudWatch): $10
- **총합**: $100-150/월

### 비용 최적화 전략
- 데모 후 자동 정리 스크립트 제공
- Spot 인스턴스 활용 옵션
- 개발 환경은 필요시에만 실행

## 🔒 보안 고려사항

### 네트워크 보안
- Private 서브넷에 백엔드 및 데이터베이스 배치
- 보안 그룹으로 최소 권한 원칙 적용
- ALB를 통한 SSL 터미네이션

### 애플리케이션 보안
- 코드 실행 시 적절한 권한 제한
- 입력 검증 및 SQL 인젝션 방지
- CORS 정책 적용

### 인프라 보안
- IAM 역할 기반 최소 권한
- 보안 그룹 규칙 최적화
- RDS 암호화 적용

이 설계 문서는 프로젝트의 전체적인 방향과 구조를 제시하며, 실제 구현 과정에서 세부 사항이 조정될 수 있습니다.