# VibeCoding 프로젝트 설정 가이드

처음 VibeCoding 프로젝트를 설정하는 개발자를 위한 종합 가이드입니다.

## 📋 목차
1. [프로젝트 개요](#프로젝트-개요)
2. [사전 준비사항](#사전-준비사항)
3. [로컬 개발 환경 설정](#로컬-개발-환경-설정)
4. [AWS 인프라 배포](#aws-인프라-배포)
5. [도커 이미지 빌드 및 배포](#도커-이미지-빌드-및-배포)
6. [문제 해결](#문제-해결)

## 🎯 프로젝트 개요

VibeCoding은 온라인 코드 편집 및 실행 플랫폼입니다.

### 기술 스택
- **Frontend**: Next.js 14, React, TypeScript, Tailwind CSS
- **Backend**: Spring Boot 3, Java 21, PostgreSQL
- **Infrastructure**: AWS ECS Fargate, Application Load Balancer, RDS
- **CI/CD**: Docker, ECR, Terraform

### 아키텍처
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   사용자 브라우저  │    │  Application    │    │   ECS Fargate   │
│    (Frontend)   │───▶│  Load Balancer  │───▶│   (Backend)     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                         │
                                               ┌─────────────────┐
                                               │   RDS PostgreSQL │
                                               │   (Database)     │
                                               └─────────────────┘
```

## 🛠 사전 준비사항

### 1. 필수 소프트웨어 설치

**Node.js & npm**
```bash
# Node.js 20 LTS 설치
curl -fsSL https://nodejs.org/dist/v20.11.1/node-v20.11.1-darwin-x64.tar.gz
```

**Java Development Kit (JDK)**
```bash
# OpenJDK 21 설치 (macOS)
brew install openjdk@21

# Ubuntu/Debian
sudo apt-get install openjdk-21-jdk
```

**Docker**
```bash
# macOS
brew install --cask docker

# Ubuntu
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh
```

**Terraform**
```bash
# macOS
brew install terraform

# Ubuntu
wget -O- https://apt.releases.hashicorp.com/gpg | gpg --dearmor | sudo tee /usr/share/keyrings/hashicorp-archive-keyring.gpg
echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list
sudo apt update && sudo apt install terraform
```

**AWS CLI**
```bash
# macOS
brew install awscli

# Ubuntu
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
```

### 2. AWS 계정 설정

**AWS 계정 생성**
1. [AWS 콘솔](https://aws.amazon.com/ko/)에 접속하여 계정 생성
2. 결제 정보 등록 (프리티어 사용 가능)

**IAM 사용자 생성**
1. AWS 콘솔 → IAM → Users → Create user
2. 사용자 이름: `terraform-user`
3. 권한 정책 첨부: `PowerUserAccess` 또는 다음 정책들:
   - AmazonECS_FullAccess
   - AmazonEC2ContainerRegistryFullAccess
   - AmazonRDSFullAccess
   - AmazonVPCFullAccess
   - ElasticLoadBalancingFullAccess

**Access Key 생성**
1. IAM → Users → terraform-user → Security credentials
2. Create access key → CLI 선택
3. Access Key ID와 Secret Access Key 저장

**AWS CLI 설정**
```bash
aws configure
# AWS Access Key ID: [발급받은 Access Key]
# AWS Secret Access Key: [발급받은 Secret Key]
# Default region name: ap-northeast-2
# Default output format: json
```

## 💻 로컬 개발 환경 설정

### 1. 프로젝트 클론
```bash
git clone [repository-url]
cd code-playground-project
```

### 2. Frontend 개발 환경
```bash
cd apps/frontend

# 의존성 설치
npm install

# 개발 서버 실행 (기본 설정 사용)
npm run dev
# http://localhost:3000 에서 접속
```

**참고:** 프론트엔드는 `next.config.js`에 정의된 기본값을 사용합니다. 필요시 환경 변수를 Docker 빌드 시점에 주입할 수 있습니다.

### 3. Backend 개발 환경
```bash
cd apps/backend

# Gradle Wrapper 권한 설정
chmod +x gradlew

# 개발용 빌드
./gradlew build

# 개발 서버 실행 (H2 데이터베이스 사용)
./gradlew bootRun
# http://localhost:8080 에서 API 접속
# http://localhost:8080/h2-console 에서 데이터베이스 확인
```

**H2 데이터베이스 접속 정보:**
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (비워두기)

## ☁️ AWS 인프라 배포

### 1. Terraform 변수 설정

**terraform.tfvars 파일 생성:**
```bash
cd deploy
cp terraform.tfvars.example terraform.tfvars
```

**terraform.tfvars 편집:**
```hcl
# 기본 설정
aws_region   = "ap-northeast-2"
environment  = "production"
project_name = "code-playground"
domain_name  = "your-domain.com"

# 네트워크 설정
vpc_cidr         = "10.0.0.0/16"
availability_zones = ["ap-northeast-2a", "ap-northeast-2c"]
public_subnets   = ["10.0.1.0/24", "10.0.2.0/24"]
private_subnets  = ["10.0.10.0/24", "10.0.11.0/24"]

# 데이터베이스 설정
database_name     = "codeplayground"
database_username = "codeplayground_admin"
database_password = "ChangeMeInProduction123!"

# ECS 설정 (이미지 URL은 나중에 업데이트)
backend_image        = "851725513597.dkr.ecr.ap-northeast-2.amazonaws.com/code-playground-backend:latest"
frontend_image       = "851725513597.dkr.ecr.ap-northeast-2.amazonaws.com/code-playground-frontend:latest"
backend_cpu          = 512
backend_memory       = 1024
frontend_cpu         = 256
frontend_memory      = 512
```

### 2. 인프라 배포

**Terraform 초기화:**
```bash
cd deploy
terraform init
```

**배포 계획 확인:**
```bash
terraform plan
```

**인프라 배포:**
```bash
terraform apply
# "yes" 입력하여 배포 승인
```

**배포 완료 후 출력값 확인:**
```bash
terraform output
```

중요한 출력값:
- `alb_dns_name`: Application Load Balancer URL
- `backend_ecr_url`: Backend ECR 리포지토리 URL
- `frontend_ecr_url`: Frontend ECR 리포지토리 URL

## 🐳 도커 이미지 빌드 및 배포

### 1. ECR 로그인
```bash
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin 851725513597.dkr.ecr.ap-northeast-2.amazonaws.com
```

### 2. Frontend 이미지 빌드 및 푸시
```bash
cd apps/frontend

# x86_64 아키텍처로 빌드 (ECS Fargate 호환)
docker build --platform linux/amd64 \
  --build-arg NEXT_PUBLIC_API_BASE_URL=http://[ALB_DNS_NAME]/api/v1 \
  -t 851725513597.dkr.ecr.ap-northeast-2.amazonaws.com/code-playground-frontend:latest .

# ECR에 푸시
docker push 851725513597.dkr.ecr.ap-northeast-2.amazonaws.com/code-playground-frontend:latest
```

### 3. Backend 이미지 빌드 및 푸시
```bash
cd apps/backend

# x86_64 아키텍처로 빌드
docker build --platform linux/amd64 \
  -t 851725513597.dkr.ecr.ap-northeast-2.amazonaws.com/code-playground-backend:latest .

# ECR에 푸시
docker push 851725513597.dkr.ecr.ap-northeast-2.amazonaws.com/code-playground-backend:latest
```

### 4. ECS 서비스 업데이트
```bash
# terraform.tfvars에 새 이미지 태그 반영 후
cd deploy
terraform apply
```

## 🔧 문제 해결

### 일반적인 문제들

**1. "exec format error" 오류**
```
원인: ARM64 아키텍처로 빌드된 이미지를 x86_64 환경에서 실행
해결: --platform linux/amd64 플래그 추가하여 빌드
```

**2. ECR 푸시 권한 오류**
```bash
# ECR 로그인 재실행
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin 851725513597.dkr.ecr.ap-northeast-2.amazonaws.com
```

**3. Frontend API 호출 실패**
```
원인: 잘못된 API URL 설정
해결: NEXT_PUBLIC_API_BASE_URL을 올바른 ALB URL로 설정
```

**4. 데이터베이스 연결 오류**
```
확인사항:
- RDS 보안 그룹 설정
- 데이터베이스 자격 증명
- 네트워크 연결성
```

### 로그 확인 방법

**ECS 태스크 로그:**
```bash
# AWS 콘솔 → ECS → Clusters → code-playground-production → Tasks
# 또는 CloudWatch Logs에서 /ecs/code-playground 로그 그룹 확인
```

**로컬 테스트:**
```bash
# Backend 로컬 실행 테스트
cd apps/backend
./gradlew bootRun --args='--spring.profiles.active=local'

# Frontend 로컬 실행 테스트
cd apps/frontend
npm run dev
```

### 유용한 명령어

**인프라 상태 확인:**
```bash
# ECS 서비스 상태
aws ecs describe-services --cluster code-playground-production --services code-playground-frontend code-playground-backend

# ALB 상태
aws elbv2 describe-load-balancers --names code-playground-alb
```

**전체 인프라 삭제:**
```bash
cd deploy
terraform destroy
# "yes" 입력하여 삭제 승인
```

## 📞 지원 및 문의

문제가 발생하면 다음을 확인해주세요:

1. **AWS 계정 및 권한 설정**
2. **필수 소프트웨어 설치 여부**
3. **환경 변수 설정**
4. **네트워크 연결성**
5. **로그 메시지**

더 자세한 도움이 필요하면 개발팀에 문의하시기 바랍니다.

---

*본 문서는 VibeCoding v1.0 기준으로 작성되었습니다. 최신 버전에서는 일부 설정이 다를 수 있습니다.*