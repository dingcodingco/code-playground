# Terraform 인프라 설계 문서

## 📋 개요

AWS ECS Fargate 기반의 서버리스 컨테이너 인프라를 Terraform으로 구성하는 설계 문서입니다.

## 🏗️ AWS 아키텍처

### 네트워킹 구조
```
Internet Gateway
      │
      ▼
┌─────────────────────────────────────────────────────────────┐
│                        VPC                                  │
│                   10.0.0.0/16                              │
│                                                             │
│  ┌─────────────────────┐    ┌─────────────────────┐        │
│  │   Public Subnet     │    │   Public Subnet     │        │
│  │   10.0.1.0/24       │    │   10.0.2.0/24       │        │
│  │   AZ-2a             │    │   AZ-2c             │        │
│  │                     │    │                     │        │
│  │  ┌─────────────┐    │    │  ┌─────────────┐    │        │
│  │  │     ALB     │    │    │  │ NAT Gateway │    │        │
│  │  └─────────────┘    │    │  └─────────────┘    │        │
│  └─────────────────────┘    └─────────────────────┘        │
│                                                             │
│  ┌─────────────────────┐    ┌─────────────────────┐        │
│  │  Private Subnet     │    │  Private Subnet     │        │
│  │   10.0.10.0/24      │    │   10.0.20.0/24      │        │
│  │   AZ-2a             │    │   AZ-2c             │        │
│  │                     │    │                     │        │
│  │  ┌─────────────┐    │    │  ┌─────────────┐    │        │
│  │  │ ECS Tasks   │    │    │  │     RDS     │    │        │
│  │  │ (Frontend/  │    │    │  │ PostgreSQL  │    │        │
│  │  │  Backend)   │    │    │  │             │    │        │
│  │  └─────────────┘    │    │  └─────────────┘    │        │
│  └─────────────────────┘    └─────────────────────┘        │
└─────────────────────────────────────────────────────────────┘
```

### 컴포넌트 구성
- **ECS Fargate**: 서버리스 컨테이너 실행 환경
- **Application Load Balancer**: 트래픽 분산 및 SSL 터미네이션
- **RDS PostgreSQL**: 관리형 데이터베이스 (Multi-AZ)
- **VPC**: 격리된 네트워크 환경
- **ECR**: 컨테이너 이미지 저장소
- **CloudWatch**: 로그 및 모니터링

## 📦 Terraform 모듈 구조

```
infrastructure/
├── modules/
│   ├── networking/           # VPC, 서브넷, 라우팅 테이블
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── security/            # 보안 그룹, IAM 역할
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── load-balancer/       # ALB, 타겟 그룹, 리스너
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── ecs/                 # ECS 클러스터, 서비스, 태스크 정의
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── rds/                 # RDS 인스턴스, 서브넷 그룹
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   └── ecr/                 # ECR 리포지토리
│       ├── main.tf
│       ├── variables.tf
│       └── outputs.tf
└── environments/
    ├── dev/
    │   ├── main.tf          # 개발 환경 구성
    │   ├── variables.tf
    │   ├── terraform.tfvars
    │   └── outputs.tf
    ├── staging/
    │   ├── main.tf
    │   ├── variables.tf
    │   ├── terraform.tfvars
    │   └── outputs.tf
    └── prod/
        ├── main.tf          # 프로덕션 환경 구성
        ├── variables.tf
        ├── terraform.tfvars
        └── outputs.tf
```

## 🔧 핵심 모듈 설계

### 1. Networking 모듈

#### VPC 및 서브넷
```hcl
# VPC
resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name        = "${var.environment}-vpc"
    Environment = var.environment
  }
}

# 인터넷 게이트웨이
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name = "${var.environment}-igw"
  }
}

# Public 서브넷 (ALB, NAT Gateway 용)
resource "aws_subnet" "public" {
  count = length(var.public_subnet_cidrs)

  vpc_id                  = aws_vpc.main.id
  cidr_block              = var.public_subnet_cidrs[count.index]
  availability_zone       = var.availability_zones[count.index]
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.environment}-public-${count.index + 1}"
    Type = "Public"
  }
}

# Private 서브넷 (ECS Tasks, RDS 용)
resource "aws_subnet" "private" {
  count = length(var.private_subnet_cidrs)

  vpc_id            = aws_vpc.main.id
  cidr_block        = var.private_subnet_cidrs[count.index]
  availability_zone = var.availability_zones[count.index]

  tags = {
    Name = "${var.environment}-private-${count.index + 1}"
    Type = "Private"
  }
}

# NAT Gateway
resource "aws_eip" "nat" {
  count  = length(aws_subnet.public)
  domain = "vpc"

  tags = {
    Name = "${var.environment}-nat-eip-${count.index + 1}"
  }
}

resource "aws_nat_gateway" "main" {
  count = length(aws_subnet.public)

  allocation_id = aws_eip.nat[count.index].id
  subnet_id     = aws_subnet.public[count.index].id

  tags = {
    Name = "${var.environment}-nat-${count.index + 1}"
  }

  depends_on = [aws_internet_gateway.main]
}
```

#### 라우팅 테이블
```hcl
# Public 라우팅 테이블
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = {
    Name = "${var.environment}-public-rt"
  }
}

resource "aws_route_table_association" "public" {
  count = length(aws_subnet.public)

  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

# Private 라우팅 테이블
resource "aws_route_table" "private" {
  count  = length(aws_subnet.private)
  vpc_id = aws_vpc.main.id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.main[count.index].id
  }

  tags = {
    Name = "${var.environment}-private-rt-${count.index + 1}"
  }
}

resource "aws_route_table_association" "private" {
  count = length(aws_subnet.private)

  subnet_id      = aws_subnet.private[count.index].id
  route_table_id = aws_route_table.private[count.index].id
}
```

### 2. Security 모듈

#### 보안 그룹
```hcl
# ALB 보안 그룹
resource "aws_security_group" "alb" {
  name_prefix = "${var.environment}-alb-"
  vpc_id      = var.vpc_id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.environment}-alb-sg"
  }
}

# Frontend ECS 보안 그룹
resource "aws_security_group" "frontend" {
  name_prefix = "${var.environment}-frontend-"
  vpc_id      = var.vpc_id

  ingress {
    from_port       = 3000
    to_port         = 3000
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.environment}-frontend-sg"
  }
}

# Backend ECS 보안 그룹
resource "aws_security_group" "backend" {
  name_prefix = "${var.environment}-backend-"
  vpc_id      = var.vpc_id

  ingress {
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id, aws_security_group.frontend.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.environment}-backend-sg"
  }
}

# RDS 보안 그룹
resource "aws_security_group" "rds" {
  name_prefix = "${var.environment}-rds-"
  vpc_id      = var.vpc_id

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.backend.id]
  }

  tags = {
    Name = "${var.environment}-rds-sg"
  }
}
```

#### IAM 역할
```hcl
# ECS 태스크 실행 역할
resource "aws_iam_role" "ecs_execution_role" {
  name = "${var.environment}-ecs-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_execution_role_policy" {
  role       = aws_iam_role.ecs_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# ECS 태스크 역할
resource "aws_iam_role" "ecs_task_role" {
  name = "${var.environment}-ecs-task-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })
}

# CloudWatch Logs 정책
resource "aws_iam_role_policy" "ecs_logs_policy" {
  name = "${var.environment}-ecs-logs-policy"
  role = aws_iam_role.ecs_task_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ]
        Resource = "*"
      }
    ]
  })
}
```

### 3. ECS 모듈

#### ECS 클러스터 및 서비스
```hcl
# ECS 클러스터
resource "aws_ecs_cluster" "main" {
  name = "${var.environment}-cluster"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = {
    Name = "${var.environment}-ecs-cluster"
  }
}

# Frontend 태스크 정의
resource "aws_ecs_task_definition" "frontend" {
  family                   = "${var.environment}-frontend"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.frontend_cpu
  memory                   = var.frontend_memory
  execution_role_arn       = var.execution_role_arn
  task_role_arn           = var.task_role_arn

  container_definitions = jsonencode([
    {
      name  = "frontend"
      image = var.frontend_image

      environment = [
        {
          name  = "NEXT_PUBLIC_API_URL"
          value = "https://api.${var.domain_name}"
        }
      ]

      portMappings = [
        {
          containerPort = 3000
          protocol      = "tcp"
        }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.frontend.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "ecs"
        }
      }

      essential = true
    }
  ])

  tags = {
    Name = "${var.environment}-frontend-task"
  }
}

# Frontend 서비스
resource "aws_ecs_service" "frontend" {
  name            = "${var.environment}-frontend-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.frontend.arn
  desired_count   = var.desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets         = var.private_subnet_ids
    security_groups = [var.frontend_security_group_id]
  }

  load_balancer {
    target_group_arn = var.frontend_target_group_arn
    container_name   = "frontend"
    container_port   = 3000
  }

  depends_on = [var.frontend_target_group_arn]

  tags = {
    Name = "${var.environment}-frontend-service"
  }
}

# CloudWatch 로그 그룹
resource "aws_cloudwatch_log_group" "frontend" {
  name              = "/ecs/${var.environment}-frontend"
  retention_in_days = 7

  tags = {
    Name = "${var.environment}-frontend-logs"
  }
}

resource "aws_cloudwatch_log_group" "backend" {
  name              = "/ecs/${var.environment}-backend"
  retention_in_days = 7

  tags = {
    Name = "${var.environment}-backend-logs"
  }
}
```

### 4. RDS 모듈

```hcl
# RDS 서브넷 그룹
resource "aws_db_subnet_group" "main" {
  name       = "${var.environment}-db-subnet-group"
  subnet_ids = var.private_subnet_ids

  tags = {
    Name = "${var.environment}-db-subnet-group"
  }
}

# RDS 인스턴스
resource "aws_db_instance" "main" {
  identifier     = "${var.environment}-database"
  engine         = "postgres"
  engine_version = "15.4"
  instance_class = var.instance_class

  allocated_storage     = var.allocated_storage
  max_allocated_storage = var.max_allocated_storage
  storage_encrypted     = true
  storage_type          = "gp2"

  db_name  = var.database_name
  username = var.username
  password = var.password

  vpc_security_group_ids = [var.rds_security_group_id]
  db_subnet_group_name   = aws_db_subnet_group.main.name

  multi_az               = var.multi_az
  publicly_accessible    = false
  backup_retention_period = var.backup_retention_period
  backup_window          = "03:00-04:00"
  maintenance_window     = "Sun:04:00-Sun:05:00"

  skip_final_snapshot = var.environment != "prod"
  final_snapshot_identifier = var.environment == "prod" ? "${var.environment}-final-snapshot-${formatdate("YYYY-MM-DD-hhmm", timestamp())}" : null

  tags = {
    Name        = "${var.environment}-database"
    Environment = var.environment
  }
}
```

## 🌍 환경별 구성

### 개발 환경 (dev)
```hcl
# infrastructure/environments/dev/main.tf
terraform {
  backend "s3" {
    bucket = "code-playground-terraform-state-dev"
    key    = "dev/terraform.tfstate"
    region = "ap-northeast-2"
  }
}

module "networking" {
  source = "../../modules/networking"

  environment = "dev"
  vpc_cidr    = "10.0.0.0/16"

  public_subnet_cidrs  = ["10.0.1.0/24", "10.0.2.0/24"]
  private_subnet_cidrs = ["10.0.10.0/24", "10.0.20.0/24"]

  availability_zones = ["ap-northeast-2a", "ap-northeast-2c"]
}

module "rds" {
  source = "../../modules/rds"

  environment     = "dev"
  vpc_id         = module.networking.vpc_id
  private_subnet_ids = module.networking.private_subnet_ids

  instance_class    = "db.t3.micro"
  allocated_storage = 10
  multi_az         = false

  database_name = "codeplayground_dev"
  username      = "admin"
  password      = var.db_password

  rds_security_group_id = module.security.rds_security_group_id
}

# 개발환경 특화 설정
# - 작은 인스턴스 크기
# - Single-AZ RDS
# - 짧은 로그 보존 기간
```

### 프로덕션 환경 (prod)
```hcl
# infrastructure/environments/prod/main.tf
terraform {
  backend "s3" {
    bucket = "code-playground-terraform-state-prod"
    key    = "prod/terraform.tfstate"
    region = "ap-northeast-2"
  }
}

module "rds" {
  source = "../../modules/rds"

  environment     = "prod"
  vpc_id         = module.networking.vpc_id
  private_subnet_ids = module.networking.private_subnet_ids

  instance_class    = "db.t3.small"
  allocated_storage = 20
  multi_az         = true

  database_name = "codeplayground"
  username      = "admin"
  password      = var.db_password

  backup_retention_period = 7
  rds_security_group_id = module.security.rds_security_group_id
}

# 프로덕션 환경 특화 설정
# - 더 큰 인스턴스 크기
# - Multi-AZ RDS
# - 긴 로그 보존 기간
# - 백업 정책 강화
```

## 📊 비용 최적화 전략

### 리소스 크기 조정
- **개발환경**: 최소 사양으로 비용 절감
- **프로덕션**: 성능과 가용성 우선

### 자동 정리 스크립트
```bash
#!/bin/bash
# scripts/cleanup-aws.sh

echo "🧹 AWS 리소스 정리 시작..."

# Terraform destroy
cd infrastructure/environments/$1
terraform destroy -auto-approve

echo "✅ $1 환경의 모든 리소스가 제거되었습니다."
```

### 비용 모니터링
- CloudWatch 비용 알람 설정
- 리소스 태깅으로 비용 추적
- 사용하지 않는 리소스 자동 식별

## 🔒 보안 및 모니터링

### 보안 기능
- VPC 격리
- Private 서브넷 배치
- 보안 그룹 최소 권한 원칙
- RDS 암호화
- IAM 역할 기반 액세스

### 모니터링 설정
- CloudWatch 로그 집중화
- 메트릭 대시보드
- 알람 및 알림 설정
- X-Ray 분산 추적 (옵션)

이 Terraform 설계는 확장 가능하고 유지보수가 용이한 인프라를 제공하며, 교육 목적에 적합한 구조로 설계되었습니다.