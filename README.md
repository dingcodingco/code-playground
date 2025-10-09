# 🚀 CodePlayground - Cloud-Native Code Execution Platform

![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Node.js](https://img.shields.io/badge/Node.js-20+-green.svg)
![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![AWS](https://img.shields.io/badge/AWS-ECS-orange.svg)
![Terraform](https://img.shields.io/badge/Terraform-1.5+-purple.svg)

CodePlayground is a modern, cloud-native code execution platform that enables developers to write, execute, and share code snippets in real-time. Built with Next.js, Spring Boot, and deployed on AWS infrastructure.

## ✨ Features

- 🖥️ **Real-time Code Editor** - Monaco-based editor with syntax highlighting
- ⚡ **Code Execution** - Execute JavaScript, Python, and Java code safely
- 📝 **Code Management** - Save, organize, and manage code snippets
- 🔗 **Code Sharing** - Share code snippets with unique URLs
- 🌐 **Multi-language Support** - JavaScript, Python, Java support
- 📱 **Responsive Design** - Works on desktop and mobile devices
- 🔒 **Secure Execution** - Containerized code execution environment
- ☁️ **Cloud-Native** - Deployed on AWS with auto-scaling

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Next.js App  │    │  Spring Boot    │    │   PostgreSQL    │
│   (Frontend)    │────│   (Backend)     │────│   (Database)    │
│   Port: 3000    │    │   Port: 8080    │    │   Port: 5432    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
        │                       │                       │
        └───────────────────────┼───────────────────────┘
                                │
                        ┌───────────────┐
                        │ AWS Load      │
                        │ Balancer      │
                        └───────────────┘
```

## 🛠️ Tech Stack

### Frontend
- **Next.js 14** - React framework with App Router
- **TypeScript** - Type-safe JavaScript
- **Tailwind CSS** - Utility-first CSS framework
- **Monaco Editor** - VSCode-like code editor
- **Zustand** - Lightweight state management

### Backend
- **Spring Boot 3** - Java enterprise framework
- **Spring Data JPA** - Data persistence layer
- **PostgreSQL** - Relational database
- **Docker** - Containerization

### Infrastructure
- **AWS ECS** - Container orchestration
- **AWS ALB** - Application Load Balancer
- **AWS RDS** - Managed PostgreSQL
- **AWS ECR** - Container registry
- **Terraform** - Infrastructure as Code

## 🚀 Quick Start

### Prerequisites

- **AWS CLI** configured with appropriate permissions
- **Docker** installed and running
- **Terraform** 1.5+ installed
- **Node.js** 20+ and npm
- **Java** 17+ and Gradle

### 1. Clone the Repository

```bash
git clone <repository-url>
cd code-playground
```

### 2. Configure AWS Deployment

1. **Update configuration file:**
   ```bash
   cp deploy/terraform.tfvars.example deploy/terraform.tfvars
   # Edit deploy/terraform.tfvars with your settings
   ```

2. **Required changes in `deploy/terraform.tfvars`:**
   - Replace `YOUR_ACCOUNT_ID` with your AWS account ID
   - Replace `YOUR_REGION` with your preferred AWS region
   - Change `CHANGE_ME_TO_SECURE_PASSWORD` to a secure database password
   - Change `CHANGE_ME_TO_SECURE_JWT_SECRET` to a secure JWT secret

### 3. Deploy to AWS

**Option A: One-click Deployment (Recommended)**
```bash
./deploy.sh
```

**Option B: Step-by-Step Deployment**
```bash
# 1. Build and push images
./deploy.sh --build-only

# 2. Deploy infrastructure
./deploy.sh --infra-only
```

### 4. Local Development

```bash
# Start all services with Docker Compose
docker-compose up -d

# Access the application
open http://localhost:3000
```

## 📦 Project Structure

```
code-playground/
├── apps/
│   ├── frontend/           # Next.js application
│   │   ├── src/
│   │   │   ├── app/       # App Router pages
│   │   │   ├── components/ # React components
│   │   │   ├── store/     # State management
│   │   │   └── types/     # TypeScript types
│   │   ├── Dockerfile     # Frontend container
│   │   └── package.json   # Dependencies
│   │
│   └── backend/            # Spring Boot application
│       ├── src/main/java/ # Java source code
│       │   ├── controller/ # REST controllers
│       │   ├── service/   # Business logic
│       │   ├── entity/    # JPA entities
│       │   └── repository/ # Data repositories
│       ├── Dockerfile     # Backend container
│       └── build.gradle   # Dependencies
│
├── deploy/                 # Infrastructure code
│   ├── terraform.tfvars   # Configuration file
│   ├── main.tf           # Main Terraform configuration
│   ├── variables.tf      # Variable definitions
│   └── ecs-resources.tf  # ECS resources
│
├── k8s/                   # Kubernetes manifests (alternative)
├── monitoring/            # Monitoring configurations
├── docker-compose.yml     # Local development
├── deploy.sh             # Deployment script
└── README.md             # This file
```

## 🔧 Configuration

### Environment Variables

**Frontend:**
Environment variables are configured in `next.config.js` with default values:
- `NEXT_PUBLIC_API_BASE_URL`: Default `http://localhost:8080/api/v1`
- `NEXT_PUBLIC_APP_NAME`: Default `Code Playground`

Override these during Docker build if needed using `--build-arg`.

**Backend (`apps/backend/application.yml`):**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/codeplayground
    username: codeplayground_admin
    password: your-password
  jpa:
    hibernate:
      ddl-auto: update
```

### Infrastructure Configuration

Key settings in `deploy/terraform.tfvars`:

- **`aws_region`** - AWS deployment region
- **`backend_image`** - Backend Docker image URL
- **`frontend_image`** - Frontend Docker image URL
- **`database_password`** - PostgreSQL password
- **`jwt_secret`** - JWT signing secret

## 📊 Monitoring & Observability

The platform includes comprehensive monitoring:

- **Application Metrics** - Custom Spring Boot metrics
- **Infrastructure Monitoring** - AWS CloudWatch integration
- **Log Aggregation** - Centralized logging with ELK stack
- **Health Checks** - Application and infrastructure health monitoring

Access monitoring dashboards:
```bash
# Grafana Dashboard
open http://<alb-dns>/grafana

# Application Logs
aws logs tail /ecs/code-playground --follow
```

## 🧪 Testing

### Run Tests Locally
```bash
# Frontend tests
cd apps/frontend
npm test

# Backend tests
cd apps/backend
./gradlew test

# E2E tests
cd e2e
npm test
```

### Performance Testing
```bash
# Load testing with k6
cd performance
./run-performance-tests.sh
```

## 🔐 Security

- **Authentication** - JWT-based authentication
- **Authorization** - Role-based access control
- **Code Execution** - Sandboxed execution environment
- **Data Protection** - Encrypted data in transit and at rest
- **Network Security** - VPC with private subnets
- **Compliance** - Follows AWS security best practices

## 🚦 Deployment Strategies

### Blue-Green Deployment
```bash
# Deploy new version alongside current
terraform apply -var="deployment_strategy=blue-green"

# Switch traffic to new version
terraform apply -var="active_deployment=green"
```

### Rolling Updates
```bash
# Gradual deployment with zero downtime
aws ecs update-service --service code-playground-frontend --force-new-deployment
```

## 📈 Scaling

### Auto Scaling Configuration
```terraform
# ECS Service Auto Scaling
resource "aws_appautoscaling_target" "ecs_target" {
  max_capacity       = 10
  min_capacity       = 2
  resource_id        = "service/${aws_ecs_cluster.main.name}/${aws_ecs_service.app.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}
```

### Manual Scaling
```bash
# Scale frontend service
aws ecs update-service --service code-playground-frontend --desired-count 3

# Scale backend service
aws ecs update-service --service code-playground-backend --desired-count 5
```

## 🐛 Troubleshooting

### Common Issues

**1. Deployment Fails**
```bash
# Check Terraform state
terraform refresh
terraform plan

# Verify AWS credentials
aws sts get-caller-identity
```

**2. Application Not Accessible**
```bash
# Check ECS service status
aws ecs describe-services --services code-playground-frontend

# Check load balancer health
aws elbv2 describe-target-health --target-group-arn <target-group-arn>
```

**3. Database Connection Issues**
```bash
# Check RDS instance status
aws rds describe-db-instances --db-instance-identifier code-playground-db

# Test database connectivity
psql -h <rds-endpoint> -U codeplayground_admin -d codeplayground
```

### Logs and Debugging
```bash
# Application logs
aws logs tail /ecs/code-playground --follow

# ECS service events
aws ecs describe-services --services code-playground-frontend --query 'services[0].events'

# CloudFormation stack events (if using)
aws cloudformation describe-stack-events --stack-name code-playground
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow existing code style and conventions
- Add tests for new features
- Update documentation as needed
- Ensure all tests pass before submitting PR

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Documentation** - Check this README and inline documentation
- **Issues** - Report bugs and request features via GitHub issues
- **Community** - Join our community discussions

## 🏷️ Changelog

### v2.0.1 (Latest)
- ✅ Fixed JavaScript TypeError in AWS environment
- ✅ Added defensive programming for language configuration
- ✅ Improved error handling and fallback values
- ✅ Enhanced deployment scripts and documentation

### v2.0.0
- 🆕 Complete AWS infrastructure setup
- 🆕 ECS-based container orchestration
- 🆕 Auto-scaling and load balancing
- 🆕 Comprehensive monitoring setup

### v1.0.0
- 🎉 Initial release
- ✨ Core code editing and execution features
- ✨ Spring Boot backend with PostgreSQL
- ✨ Next.js frontend with real-time updates

---

**Built with ❤️ by the CodePlayground Team**