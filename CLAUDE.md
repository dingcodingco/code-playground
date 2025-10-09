# CLAUDE.md - Project-Specific Guidelines for Code Playground

## Flyway Migration Rules

### ⚠️ CRITICAL: Flyway Migration Immutability

**NEVER MODIFY EXISTING FLYWAY MIGRATION FILES!**

Once a Flyway migration file has been created and deployed, it becomes immutable. Any changes to existing migration files will cause deployment failures and database inconsistencies.

### Rules for Flyway Migrations:

1. **Never Edit Existing Migration Files**
   - Once a migration file (V1__, V2__, etc.) is created and deployed, it must NEVER be modified
   - This includes changing SQL statements, comments, or even formatting
   - Flyway tracks migrations by checksum - any change will cause validation failures

2. **Always Create New Migration Files**
   - To make database changes, always create a new migration with the next version number
   - Example: If V2__ exists, create V3__ for new changes

3. **Version Numbering**
   - Use sequential version numbers: V1__, V2__, V3__, etc.
   - Never reuse version numbers
   - Never skip version numbers

4. **Migration File Naming Convention**
   ```
   V{version}__{Description}.sql
   ```
   Example: `V3__Add_user_authentication_fields.sql`

5. **Testing Migrations**
   - Test migrations locally before deploying to production
   - Once deployed to ANY environment, consider the migration immutable

6. **Handling Migration Errors**
   - If a migration fails, DO NOT modify the failed migration
   - Create a new migration to fix the issue
   - Example: If V2__ fails, create V3__ to correct the problem

### Common Mistakes to Avoid:
- ❌ Editing SQL in existing migration files
- ❌ Renaming migration files
- ❌ Deleting migration files
- ❌ Changing version numbers
- ❌ Modifying migrations after deployment

### Correct Approach:
- ✅ Always create new migration files
- ✅ Test thoroughly before deployment
- ✅ Use descriptive names for migrations
- ✅ Document the purpose of each migration

## Project Architecture

### Backend
- **Framework**: Spring Boot 3.2.0
- **Java Version**: 21
- **Database**: PostgreSQL 15
- **Migration Tool**: Flyway
- **Build Tool**: Gradle

### Frontend
- **Framework**: Next.js
- **Language**: TypeScript
- **Styling**: Tailwind CSS

### Infrastructure
- **Cloud Provider**: AWS
- **Container Orchestration**: ECS Fargate
- **IaC Tool**: Terraform
- **Container Registry**: ECR
- **Database**: RDS PostgreSQL

## Database Schema Changes

### Current State (After V2 Migration)
- `language` field in `code_snippets` table is now VARCHAR(50) without enum constraints
- All enum constraints have been removed to allow any programming language string

### Important Notes
- The system no longer uses enum types for programming languages
- Language validation is now handled at the application layer
- Any language string up to 50 characters is accepted

## Deployment Process

1. Build Docker image with platform flag: `--platform linux/amd64`
2. Tag with descriptive version: `{ecr-url}:{version}`
3. Push to ECR
4. Update `terraform.tfvars` with new image tag
5. Run `terraform apply` to deploy

## Environment Variables

Required environment variables for backend:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`

## Testing Commands

### Local Testing
```bash
./gradlew bootRun  # Run Spring Boot locally
```

### API Testing
```bash
curl "http://{alb-dns}/api/v1/snippets?page=0&size=5"
```

## Monitoring

- CloudWatch Logs: `/ecs/code-playground`
- ECS Service: `code-playground-backend`, `code-playground-frontend`
- RDS Instance: `code-playground-db`