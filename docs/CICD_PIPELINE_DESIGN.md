# CI/CD 파이프라인 설계 문서

## 📋 개요

GitHub Actions를 활용한 바이브코딩 서비스의 CI/CD 파이프라인 설계 문서입니다. Terraform과 AWS ECS Fargate를 통한 완전 자동화된 배포 프로세스를 제공합니다.

## 🔄 CI/CD 전체 플로우

```
개발자 → Git Push → GitHub Actions → Build & Test → Docker Build → ECR Push → Terraform Apply → ECS Deploy
```

### 플로우 상세
1. **개발자 코드 푸시** → GitHub Repository
2. **GitHub Actions 트리거** → 워크플로우 자동 실행
3. **코드 품질 검사** → 린트, 테스트, 보안 스캔
4. **Docker 이미지 빌드** → 프론트엔드 & 백엔드
5. **ECR 이미지 푸시** → AWS Container Registry
6. **Terraform 실행** → 인프라 프로비저닝/업데이트
7. **ECS 서비스 배포** → 롤링 업데이트
8. **배포 검증** → 헬스체크 및 모니터링

## 📂 워크플로우 구조

```
.github/workflows/
├── ci-frontend.yml           # 프론트엔드 CI
├── ci-backend.yml            # 백엔드 CI
├── deploy-infrastructure.yml # 인프라 배포
├── deploy-application.yml    # 애플리케이션 배포
├── cleanup.yml              # 리소스 정리
└── security-scan.yml        # 보안 스캔
```

## 🚀 워크플로우 상세 설계

### 1. Frontend CI 워크플로우

**파일**: `.github/workflows/ci-frontend.yml`

```yaml
name: Frontend CI

on:
  push:
    paths:
      - 'apps/frontend/**'
      - '.github/workflows/ci-frontend.yml'
  pull_request:
    paths:
      - 'apps/frontend/**'

env:
  NODE_VERSION: '18'
  WORKING_DIR: './apps/frontend'

jobs:
  lint-and-test:
    runs-on: ubuntu-latest

    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Setup Node.js
      uses: actions/setup-node@v4
      with:
        node-version: ${{ env.NODE_VERSION }}
        cache: 'npm'
        cache-dependency-path: ${{ env.WORKING_DIR }}/package-lock.json

    - name: Install dependencies
      working-directory: ${{ env.WORKING_DIR }}
      run: npm ci

    - name: Run linting
      working-directory: ${{ env.WORKING_DIR }}
      run: npm run lint

    - name: Run type checking
      working-directory: ${{ env.WORKING_DIR }}
      run: npm run type-check

    - name: Run tests
      working-directory: ${{ env.WORKING_DIR }}
      run: npm run test:ci

    - name: Build application
      working-directory: ${{ env.WORKING_DIR }}
      run: npm run build

    - name: Upload build artifacts
      uses: actions/upload-artifact@v4
      with:
        name: frontend-build
        path: ${{ env.WORKING_DIR }}/.next/
        retention-days: 1
```

### 2. Backend CI 워크플로우

**파일**: `.github/workflows/ci-backend.yml`

```yaml
name: Backend CI

on:
  push:
    paths:
      - 'apps/backend/**'
      - '.github/workflows/ci-backend.yml'
  pull_request:
    paths:
      - 'apps/backend/**'

env:
  JAVA_VERSION: '21'
  WORKING_DIR: './apps/backend'

jobs:
  lint-and-test:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_PASSWORD: testpass
          POSTGRES_DB: testdb
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5432:5432

    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Setup JDK
      uses: actions/setup-java@v4
      with:
        java-version: ${{ env.JAVA_VERSION }}
        distribution: 'temurin'

    - name: Cache Gradle packages
      uses: actions/cache@v4
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-

    - name: Grant execute permission for gradlew
      working-directory: ${{ env.WORKING_DIR }}
      run: chmod +x gradlew

    - name: Run tests
      working-directory: ${{ env.WORKING_DIR }}
      run: ./gradlew test
      env:
        SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/testdb
        SPRING_DATASOURCE_USERNAME: postgres
        SPRING_DATASOURCE_PASSWORD: testpass

    - name: Run linting (Checkstyle)
      working-directory: ${{ env.WORKING_DIR }}
      run: ./gradlew checkstyleMain checkstyleTest

    - name: Build application
      working-directory: ${{ env.WORKING_DIR }}
      run: ./gradlew bootJar

    - name: Upload build artifacts
      uses: actions/upload-artifact@v4
      with:
        name: backend-build
        path: ${{ env.WORKING_DIR }}/build/libs/*.jar
        retention-days: 1
```

### 3. 인프라 배포 워크플로우

**파일**: `.github/workflows/deploy-infrastructure.yml`

```yaml
name: Deploy Infrastructure

on:
  push:
    branches: [main]
    paths:
      - 'infrastructure/**'
      - '.github/workflows/deploy-infrastructure.yml'
  pull_request:
    paths:
      - 'infrastructure/**'

env:
  TERRAFORM_VERSION: '1.6.0'
  AWS_REGION: 'ap-northeast-2'

jobs:
  terraform-plan:
    runs-on: ubuntu-latest
    if: github.event_name == 'pull_request'

    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Setup Terraform
      uses: hashicorp/setup-terraform@v3
      with:
        terraform_version: ${{ env.TERRAFORM_VERSION }}

    - name: Configure AWS credentials
      uses: aws-actions/configure-aws-credentials@v4
      with:
        aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: ${{ env.AWS_REGION }}

    - name: Terraform Init
      working-directory: ./infrastructure/environments/prod
      run: terraform init

    - name: Terraform Validate
      working-directory: ./infrastructure/environments/prod
      run: terraform validate

    - name: Terraform Plan
      working-directory: ./infrastructure/environments/prod
      run: |
        terraform plan \
          -var="db_password=${{ secrets.DB_PASSWORD }}" \
          -var="ssl_certificate_arn=${{ secrets.SSL_CERTIFICATE_ARN }}" \
          -out=tfplan

    - name: Upload plan
      uses: actions/upload-artifact@v4
      with:
        name: terraform-plan
        path: ./infrastructure/environments/prod/tfplan
        retention-days: 5

  terraform-apply:
    runs-on: ubuntu-latest
    needs: []
    if: github.ref == 'refs/heads/main' && github.event_name == 'push'

    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Setup Terraform
      uses: hashicorp/setup-terraform@v3
      with:
        terraform_version: ${{ env.TERRAFORM_VERSION }}

    - name: Configure AWS credentials
      uses: aws-actions/configure-aws-credentials@v4
      with:
        aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: ${{ env.AWS_REGION }}

    - name: Terraform Init
      working-directory: ./infrastructure/environments/prod
      run: terraform init

    - name: Terraform Apply
      working-directory: ./infrastructure/environments/prod
      run: |
        terraform apply -auto-approve \
          -var="db_password=${{ secrets.DB_PASSWORD }}" \
          -var="ssl_certificate_arn=${{ secrets.SSL_CERTIFICATE_ARN }}"

    - name: Output infrastructure info
      working-directory: ./infrastructure/environments/prod
      run: |
        echo "ALB_DNS_NAME=$(terraform output -raw alb_dns_name)" >> $GITHUB_ENV
        echo "ECS_CLUSTER_NAME=$(terraform output -raw ecs_cluster_name)" >> $GITHUB_ENV
```

### 4. 애플리케이션 배포 워크플로우

**파일**: `.github/workflows/deploy-application.yml`

```yaml
name: Deploy Application

on:
  push:
    branches: [main]
    paths:
      - 'apps/**'
      - '.github/workflows/deploy-application.yml'

env:
  AWS_REGION: ap-northeast-2
  ECR_REGISTRY: ${{ secrets.AWS_ACCOUNT_ID }}.dkr.ecr.ap-northeast-2.amazonaws.com
  FRONTEND_REPOSITORY: vibe-coding-frontend
  BACKEND_REPOSITORY: vibe-coding-backend

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest

    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Configure AWS credentials
      uses: aws-actions/configure-aws-credentials@v4
      with:
        aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: ${{ env.AWS_REGION }}

    - name: Login to Amazon ECR
      id: login-ecr
      uses: aws-actions/amazon-ecr-login@v2

    - name: Build and push Frontend image
      env:
        IMAGE_TAG: ${{ github.sha }}
      run: |
        cd apps/frontend
        docker build -t $ECR_REGISTRY/$FRONTEND_REPOSITORY:$IMAGE_TAG .
        docker push $ECR_REGISTRY/$FRONTEND_REPOSITORY:$IMAGE_TAG
        docker tag $ECR_REGISTRY/$FRONTEND_REPOSITORY:$IMAGE_TAG $ECR_REGISTRY/$FRONTEND_REPOSITORY:latest
        docker push $ECR_REGISTRY/$FRONTEND_REPOSITORY:latest

    - name: Build and push Backend image
      env:
        IMAGE_TAG: ${{ github.sha }}
      run: |
        cd apps/backend
        docker build -t $ECR_REGISTRY/$BACKEND_REPOSITORY:$IMAGE_TAG .
        docker push $ECR_REGISTRY/$BACKEND_REPOSITORY:$IMAGE_TAG
        docker tag $ECR_REGISTRY/$BACKEND_REPOSITORY:$IMAGE_TAG $ECR_REGISTRY/$BACKEND_REPOSITORY:latest
        docker push $ECR_REGISTRY/$BACKEND_REPOSITORY:latest

    - name: Update ECS services
      run: |
        # Frontend 서비스 업데이트
        aws ecs update-service \
          --cluster vibe-coding-cluster \
          --service frontend-service \
          --force-new-deployment \
          --region $AWS_REGION

        # Backend 서비스 업데이트
        aws ecs update-service \
          --cluster vibe-coding-cluster \
          --service backend-service \
          --force-new-deployment \
          --region $AWS_REGION

    - name: Wait for deployment completion
      run: |
        echo "Waiting for frontend deployment..."
        aws ecs wait services-stable \
          --cluster vibe-coding-cluster \
          --services frontend-service \
          --region $AWS_REGION

        echo "Waiting for backend deployment..."
        aws ecs wait services-stable \
          --cluster vibe-coding-cluster \
          --services backend-service \
          --region $AWS_REGION

        echo "✅ Deployment completed successfully!"

    - name: Run deployment tests
      run: |
        # 헬스체크 API 호출
        sleep 30  # 서비스 안정화 대기

        ALB_DNS=$(aws elbv2 describe-load-balancers \
          --names vibe-coding-alb \
          --query 'LoadBalancers[0].DNSName' \
          --output text \
          --region $AWS_REGION)

        echo "Testing health check..."
        curl -f "http://$ALB_DNS/api/health" || exit 1

        echo "Testing frontend..."
        curl -f "http://$ALB_DNS/" || exit 1

        echo "✅ All health checks passed!"
```

### 5. 보안 스캔 워크플로우

**파일**: `.github/workflows/security-scan.yml`

```yaml
name: Security Scan

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]
  schedule:
    - cron: '0 2 * * 1'  # 매주 월요일 오전 2시

jobs:
  security-scan:
    runs-on: ubuntu-latest

    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Run Trivy vulnerability scanner
      uses: aquasecurity/trivy-action@master
      with:
        scan-type: 'fs'
        scan-ref: '.'
        format: 'sarif'
        output: 'trivy-results.sarif'

    - name: Upload Trivy scan results to GitHub Security
      uses: github/codeql-action/upload-sarif@v3
      if: always()
      with:
        sarif_file: 'trivy-results.sarif'

    - name: Scan Docker images
      run: |
        # Frontend 이미지 스캔
        docker build -t frontend-scan apps/frontend/
        trivy image --exit-code 1 --severity HIGH,CRITICAL frontend-scan

        # Backend 이미지 스캔
        docker build -t backend-scan apps/backend/
        trivy image --exit-code 1 --severity HIGH,CRITICAL backend-scan
```

### 6. 리소스 정리 워크플로우

**파일**: `.github/workflows/cleanup.yml`

```yaml
name: Cleanup Resources

on:
  workflow_dispatch:
    inputs:
      environment:
        description: 'Environment to cleanup'
        required: true
        default: 'dev'
        type: choice
        options:
        - dev
        - staging
      confirm:
        description: 'Type "DESTROY" to confirm'
        required: true
        type: string

env:
  TERRAFORM_VERSION: '1.6.0'
  AWS_REGION: 'ap-northeast-2'

jobs:
  cleanup:
    runs-on: ubuntu-latest
    if: github.event.inputs.confirm == 'DESTROY'

    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Setup Terraform
      uses: hashicorp/setup-terraform@v3
      with:
        terraform_version: ${{ env.TERRAFORM_VERSION }}

    - name: Configure AWS credentials
      uses: aws-actions/configure-aws-credentials@v4
      with:
        aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: ${{ env.AWS_REGION }}

    - name: Terraform Destroy
      working-directory: ./infrastructure/environments/${{ github.event.inputs.environment }}
      run: |
        terraform init
        terraform destroy -auto-approve \
          -var="db_password=${{ secrets.DB_PASSWORD }}" \
          -var="ssl_certificate_arn=${{ secrets.SSL_CERTIFICATE_ARN }}"

    - name: Cleanup ECR images
      run: |
        aws ecr batch-delete-image \
          --repository-name ${{ env.FRONTEND_REPOSITORY }} \
          --image-ids imageTag=latest \
          --region ${{ env.AWS_REGION }} || true

        aws ecr batch-delete-image \
          --repository-name ${{ env.BACKEND_REPOSITORY }} \
          --image-ids imageTag=latest \
          --region ${{ env.AWS_REGION }} || true
```

## 🔐 GitHub Secrets 설정

### 필수 Secrets
```yaml
# AWS 관련
AWS_ACCESS_KEY_ID: AKIA...           # AWS Access Key
AWS_SECRET_ACCESS_KEY: xxx...        # AWS Secret Key
AWS_ACCOUNT_ID: 123456789012         # AWS Account ID

# 데이터베이스
DB_PASSWORD: your-secure-password    # RDS 패스워드

# SSL 인증서
SSL_CERTIFICATE_ARN: arn:aws:acm:... # ACM 인증서 ARN

# 기타
DOMAIN_NAME: vibecoding.com          # 도메인 이름 (옵션)
```

### Secrets 보안 설정
- **환경별 분리**: Production과 Development Secrets 분리
- **최소 권한**: IAM 사용자에 최소한의 권한만 부여
- **정기 로테이션**: 주기적인 Secrets 갱신

## 🚦 브랜치 전략

### GitFlow 기반 전략
```
main (production)
  ├── develop (development)
  │   ├── feature/new-feature
  │   ├── feature/bug-fix
  │   └── hotfix/critical-fix
  └── release/v1.0.0
```

### 배포 트리거
- **main 브랜치**: 프로덕션 자동 배포
- **develop 브랜치**: 개발 환경 자동 배포
- **Pull Request**: CI 검사만 실행
- **release 브랜치**: 스테이징 환경 배포

## 📊 모니터링 및 알림

### 배포 상태 모니터링
```yaml
# 슬랙 알림 예시
- name: Notify Slack on success
  if: success()
  uses: 8398a7/action-slack@v3
  with:
    status: success
    text: '✅ 배포가 성공적으로 완료되었습니다!'
  env:
    SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}

- name: Notify Slack on failure
  if: failure()
  uses: 8398a7/action-slack@v3
  with:
    status: failure
    text: '❌ 배포 중 오류가 발생했습니다!'
  env:
    SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
```

### 메트릭 수집
- **배포 시간**: 각 단계별 소요 시간 측정
- **성공률**: 배포 성공/실패 비율
- **롤백 빈도**: 롤백 발생 횟수
- **테스트 커버리지**: 코드 커버리지 추적

## 🔄 롤백 전략

### 자동 롤백
```yaml
- name: Health check and rollback
  run: |
    # 헬스체크 실행
    if ! curl -f "$HEALTH_CHECK_URL"; then
      echo "Health check failed, rolling back..."

      # 이전 버전으로 롤백
      aws ecs update-service \
        --cluster $CLUSTER_NAME \
        --service $SERVICE_NAME \
        --task-definition $PREVIOUS_TASK_DEFINITION

      exit 1
    fi
```

### 수동 롤백
- GitHub Actions의 `workflow_dispatch`를 통한 수동 롤백
- 특정 이미지 태그로 서비스 업데이트
- 데이터베이스 마이그레이션 롤백 스크립트

## 📈 성능 최적화

### 빌드 최적화
- **Docker 레이어 캐싱**: 멀티 스테이지 빌드 활용
- **병렬 빌드**: Frontend/Backend 동시 빌드
- **의존성 캐싱**: npm/Gradle 의존성 캐시 활용
- **빌드 아티팩트 재사용**: 동일 커밋의 빌드 결과 재사용

### 배포 최적화
- **롤링 업데이트**: 무중단 배포
- **헬스체크**: 배포 후 서비스 상태 검증
- **단계적 배포**: Blue-Green 배포 (고급)

## 🧪 테스트 전략

### 테스트 레벨
1. **Unit Tests**: 개별 함수/클래스 테스트
2. **Integration Tests**: API 엔드포인트 테스트
3. **E2E Tests**: 전체 워크플로우 테스트
4. **Security Tests**: 보안 취약점 스캔
5. **Performance Tests**: 성능 테스트 (부하 테스트)

### 테스트 자동화
- PR 생성 시 자동 테스트 실행
- 배포 전 필수 테스트 통과 검증
- 테스트 실패 시 배포 중단
- 테스트 커버리지 리포트 생성

이 CI/CD 파이프라인 설계는 현대적인 DevOps 프랙티스를 적용하여 안정적이고 효율적인 배포 프로세스를 제공합니다.