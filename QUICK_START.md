# ⚡ CodePlayground - Quick Start Guide

This guide helps you deploy CodePlayground to AWS in less than 10 minutes.

## 📋 Prerequisites

- **AWS Account** with admin access
- **AWS CLI** installed and configured
- **Docker** installed and running
- **Terraform** 1.5+ installed

## 🚀 5-Step Deployment

### Step 1: Clone and Setup
```bash
git clone <repository-url>
cd code-playground
```

### Step 2: Configure AWS
```bash
# Configure AWS CLI (if not already done)
aws configure

# Get your AWS Account ID (needed for configuration)
aws sts get-caller-identity --query Account --output text

# Verify your AWS account
aws sts get-caller-identity
```

**📝 Note:** Save your AWS Account ID (12-digit number) - you'll need it in the next step!

### Step 3: Update Configuration
```bash
# Copy configuration template
cp deploy/terraform.tfvars.example deploy/terraform.tfvars

# Edit with your settings (REQUIRED!)
vim deploy/terraform.tfvars
```

**⚠️ Required Changes in `deploy/terraform.tfvars`:**
- Replace `YOUR_ACCOUNT_ID` with your AWS Account ID (12-digit number from Step 2)
- Replace `YOUR_REGION` with your preferred AWS region (e.g., `us-east-1`, `ap-northeast-2`)
- Change `CHANGE_ME_TO_SECURE_PASSWORD` to a secure database password
- Change `CHANGE_ME_TO_SECURE_JWT_SECRET` to a secure JWT secret

**💡 How to get your AWS Account ID:**
```bash
aws sts get-caller-identity --query Account --output text
```

### Step 4: Deploy to AWS
```bash
# Make deployment script executable
chmod +x deploy.sh

# One-command deployment
./deploy.sh
```

### Step 5: Access Your Application
```bash
# Get your application URL from the output
# Example: http://code-playground-alb-123456789.region.elb.amazonaws.com
```

## 🎉 That's It!

Your CodePlayground platform is now running on AWS with:
- ✅ Auto-scaling containers (ECS)
- ✅ Load balancer (ALB)
- ✅ Managed database (RDS PostgreSQL)
- ✅ Container registry (ECR)
- ✅ Full monitoring and logging

## 🔧 Local Development

For local development:
```bash
# Start all services with Docker Compose
docker-compose up -d

# Access locally
open http://localhost:3000
```

## 🆘 Need Help?

**Common Issues:**

1. **AWS permissions error**
   ```bash
   # Check your AWS credentials
   aws sts get-caller-identity
   ```

2. **Terraform fails**
   ```bash
   # Check Terraform state
   cd deploy && terraform refresh
   ```

3. **Build fails**
   ```bash
   # Clean Docker and rebuild
   docker system prune -af
   ./deploy.sh --build-only
   ```

4. **Application not accessible**
   ```bash
   # Check ECS service status
   aws ecs describe-services --services code-playground-frontend
   ```

**Get Support:**
- 📖 Read the full [README.md](README.md) for detailed documentation
- 🐛 Report issues on GitHub
- 💬 Check troubleshooting section in README

---

**🏷️ Pro Tips:**
- Use `./deploy.sh --build-only` to only build and push images
- Use `./deploy.sh --infra-only` to only deploy infrastructure
- Run `terraform destroy` from the `deploy` directory to tear down everything
- Check AWS costs in your billing dashboard