# 바이브코딩 데모 프로젝트

Docker 기반 풀스택 개발부터 AWS 배포까지 학습할 수 있는 교육용 프로젝트입니다.

## 📋 프로젝트 개요

- **목적**: Docker 컨테이너화부터 AWS 클라우드 배포까지의 전체 개발 플로우 교육
- **기술스택**: Next.js + Spring Boot + PostgreSQL + Docker + AWS ECS Fargate + Terraform
- **특징**: 모노레포 구조, Infrastructure as Code, CI/CD 자동화

## 🚀 빠른 시작

### 1. 사전 요구사항
- Node.js 18+
- Java JDK 21
- Docker Desktop
- AWS CLI & Terraform (배포용)

### 2. 로컬 실행
```bash
# 저장소 클론
git clone https://github.com/instructor/vibe-coding-demo.git
cd vibe-coding-demo

# Docker Compose로 전체 환경 실행
docker-compose up --build

# 브라우저에서 접속
open http://localhost:3000
```

## 📁 프로젝트 구조

```
vibe-coding-demo/
├── apps/
│   ├── frontend/        # Next.js (포트 3000)
│   └── backend/         # Spring Boot (포트 8080)
├── infrastructure/      # Terraform 코드
├── docker/             # Docker 설정
├── scripts/            # 유틸리티 스크립트
├── .github/workflows/  # CI/CD 파이프라인
└── docs/              # 상세 문서들
```

## 🎯 주요 기능

- ✅ **실시간 코드 에디터**: Monaco Editor 기반
- ✅ **다중 언어 지원**: JavaScript, Python, Java
- ✅ **코드 실행 엔진**: 안전한 샌드박스 환경
- ✅ **코드 공유**: 고유 URL로 코드 및 결과 공유
- ✅ **사용자 히스토리**: 개인별 코드 작성 이력 관리

## 📚 상세 문서

- [📖 프로젝트 설계 문서](./PROJECT_DESIGN.md)
- [🏗️ Terraform 인프라 설계](./TERRAFORM_INFRASTRUCTURE.md)
- [🌐 API 설계 및 데이터베이스](./API_DATABASE_DESIGN.md)
- [🔄 CI/CD 파이프라인 설계](./CICD_PIPELINE_DESIGN.md)
- [⚙️ 개발환경 설정 가이드](./DEVELOPMENT_SETUP_GUIDE.md)

## 🧪 개발 워크플로우

### 로컬 개발
```bash
# 개별 서비스 개발 모드
cd apps/frontend && npm run dev     # http://localhost:3000
cd apps/backend && ./gradlew bootRun # http://localhost:8080

# 또는 Docker Compose
docker-compose up --build
```

### 테스트
```bash
# Frontend 테스트
cd apps/frontend
npm run test
npm run lint
npm run type-check

# Backend 테스트
cd apps/backend
./gradlew test
./gradlew check
```

## ☁️ AWS 배포

### 1. 인프라 프로비저닝
```bash
cd infrastructure/environments/prod
terraform init
terraform plan
terraform apply
```

### 2. 애플리케이션 배포
```bash
# GitHub Actions 자동 배포
git push origin main

# 또는 수동 배포
aws ecs update-service --cluster vibe-coding-cluster --service frontend-service --force-new-deployment
aws ecs update-service --cluster vibe-coding-cluster --service backend-service --force-new-deployment
```

## 📊 아키텍처

### 로컬 개발 환경
```
브라우저 → Next.js (3000) → Spring Boot (8080) → H2 Database
```

### AWS 프로덕션 환경
```
사용자 → ALB → ECS Fargate → RDS PostgreSQL
                ↓
            CloudWatch Logs
```

## 🔧 기술 스택 상세

### Frontend
- **Framework**: Next.js 14 (App Router)
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **Editor**: Monaco Editor
- **State**: Zustand

### Backend
- **Framework**: Spring Boot 3.x
- **Language**: Java 21
- **Database**: H2 (dev) / PostgreSQL (prod)
- **ORM**: JPA/Hibernate

### DevOps
- **Containerization**: Docker + Docker Compose
- **Cloud**: AWS ECS Fargate
- **IaC**: Terraform
- **CI/CD**: GitHub Actions
- **Monitoring**: CloudWatch

## 🎓 교육적 가치

### 학습 목표
1. **컨테이너화**: Docker를 활용한 개발환경 구성
2. **클라우드 네이티브**: AWS 서비스 활용 및 서버리스 아키텍처
3. **Infrastructure as Code**: Terraform을 통한 인프라 관리
4. **CI/CD**: GitHub Actions를 통한 자동화된 배포
5. **모니터링**: 클라우드 환경에서의 로그 및 메트릭 관리

### 데모 시나리오 (총 90분)
1. **로컬 개발** (30분): Docker Compose로 환경 구축 및 기능 테스트
2. **인프라 구축** (30분): Terraform으로 AWS 리소스 생성
3. **자동 배포** (15분): GitHub Actions를 통한 CI/CD
4. **모니터링** (15분): CloudWatch 로그 확인 및 트러블슈팅

## 💰 비용 관리

### 예상 AWS 비용 (월간)
- ECS Fargate: $30-50
- RDS t3.micro: $13-26
- ALB: $23
- 기타: $20-30
- **총합**: ~$100-150/월

### 비용 절약 팁
- 데모 후 리소스 정리 스크립트 실행
- 개발환경은 필요시에만 실행
- AWS Free Tier 활용 가능

## 🔒 보안

- VPC 격리 및 Private 서브넷 사용
- 보안 그룹으로 최소 권한 적용
- IAM 역할 기반 접근 제어
- RDS 암호화 적용
- 코드 실행 샌드박스 격리

## 🐛 트러블슈팅

### 자주 발생하는 문제

1. **포트 충돌**
   ```bash
   lsof -ti:3000
   kill $(lsof -ti:3000)
   ```

2. **Docker 관련**
   ```bash
   docker-compose down
   docker system prune -f
   docker-compose up --build
   ```

3. **의존성 문제**
   ```bash
   # Frontend
   cd apps/frontend && rm -rf node_modules && npm install

   # Backend
   cd apps/backend && ./gradlew clean build
   ```

## 🤝 기여 방법

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 라이선스

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 지원

- 📧 Email: instructor@example.com
- 💬 Slack: #vibe-coding-support
- 🐛 Issues: [GitHub Issues](https://github.com/instructor/vibe-coding-demo/issues)

---

**Made with ❤️ for Education**