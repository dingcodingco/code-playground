# Vibe Coding 프로덕션 배포 가이드

## 📋 배포 체크리스트

### 사전 준비사항
- [ ] Docker 및 Docker Compose 설치 확인
- [ ] Kubernetes 클러스터 준비
- [ ] 도메인 및 SSL 인증서 준비
- [ ] 데이터베이스 백업 전략 수립
- [ ] 모니터링 시스템 준비

## 🐳 Docker 배포

### 1. 로컬 테스트
```bash
# Docker Compose로 전체 스택 실행
docker-compose up -d

# 상태 확인
docker-compose ps

# 로그 모니터링
docker-compose logs -f
```

### 2. 이미지 빌드 및 푸시
```bash
# 백엔드 이미지 빌드
docker build -t vibe-coding/backend:latest ./apps/backend

# 프론트엔드 이미지 빌드
docker build -t vibe-coding/frontend:latest ./apps/frontend

# 레지스트리에 푸시 (예: Docker Hub)
docker push vibe-coding/backend:latest
docker push vibe-coding/frontend:latest
```

## ☸️ Kubernetes 배포

### 1. 네임스페이스 생성
```bash
kubectl apply -f k8s/namespace.yaml
```

### 2. 시크릿 및 ConfigMap 배포
```bash
# PostgreSQL 설정
kubectl apply -f k8s/postgres/postgres-configmap.yaml
kubectl apply -f k8s/postgres/postgres-secret.yaml

# 백엔드 설정
kubectl apply -f k8s/backend/backend-configmap.yaml
kubectl apply -f k8s/backend/backend-secret.yaml

# 프론트엔드 설정
kubectl apply -f k8s/frontend/frontend-configmap.yaml
```

### 3. 데이터베이스 배포
```bash
# PV/PVC 생성
kubectl apply -f k8s/postgres/postgres-pv.yaml

# PostgreSQL 배포
kubectl apply -f k8s/postgres/postgres-deployment.yaml
kubectl apply -f k8s/postgres/postgres-service.yaml

# 상태 확인
kubectl get pods -n vibe-coding -l app=postgres
```

### 4. 백엔드 배포
```bash
# 백엔드 배포
kubectl apply -f k8s/backend/backend-deployment.yaml
kubectl apply -f k8s/backend/backend-service.yaml
kubectl apply -f k8s/backend/backend-hpa.yaml

# 상태 확인
kubectl get pods -n vibe-coding -l app=backend
```

### 5. 프론트엔드 배포
```bash
# 프론트엔드 배포
kubectl apply -f k8s/frontend/frontend-deployment.yaml
kubectl apply -f k8s/frontend/frontend-service.yaml
kubectl apply -f k8s/frontend/frontend-hpa.yaml

# 상태 확인
kubectl get pods -n vibe-coding -l app=frontend
```

### 6. Ingress 설정
```bash
# Cert-Manager 설치 (Let's Encrypt SSL)
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml

# ClusterIssuer 생성
kubectl apply -f k8s/ingress/cluster-issuer.yaml

# Ingress 배포
kubectl apply -f k8s/ingress/ingress.yaml
```

### 7. 보안 정책 적용
```bash
# 네트워크 정책
kubectl apply -f k8s/security/network-policy.yaml

# Pod 보안 정책
kubectl apply -f k8s/security/pod-security-policy.yaml

# RBAC 설정
kubectl apply -f k8s/security/rbac.yaml
```

## 📊 모니터링 설정

### Prometheus 설치
```bash
# Prometheus Operator 설치
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install prometheus prometheus-community/kube-prometheus-stack -n vibe-coding

# ConfigMap 적용
kubectl apply -f k8s/monitoring/prometheus-configmap.yaml
```

### Grafana 대시보드 설정
```bash
# Grafana 대시보드 추가
kubectl apply -f k8s/monitoring/grafana-dashboard.yaml

# Grafana 접속 (포트 포워딩)
kubectl port-forward -n vibe-coding svc/prometheus-grafana 3000:80
```

### 로그 수집 (Fluent Bit)
```bash
# Fluent Bit ConfigMap 적용
kubectl apply -f k8s/monitoring/fluent-bit-configmap.yaml

# Fluent Bit DaemonSet 배포
helm repo add fluent https://fluent.github.io/helm-charts
helm install fluent-bit fluent/fluent-bit -n vibe-coding
```

## 🔄 CI/CD 파이프라인 설정

### GitHub Actions 설정
1. GitHub 저장소 Settings → Secrets and variables → Actions
2. 다음 시크릿 추가:
   - `KUBECONFIG`: Kubernetes 설정 (base64 인코딩)
   - `DOCKER_USERNAME`: Docker Hub 사용자명
   - `DOCKER_PASSWORD`: Docker Hub 패스워드

### 자동 배포 트리거
```bash
# main 브랜치에 푸시 시 자동 배포
git push origin main

# 수동 배포 트리거
gh workflow run ci-cd.yml
```

## 🧪 성능 테스트

### K6 설치
```bash
# macOS
brew install k6

# Linux
snap install k6
```

### 부하 테스트 실행
```bash
# 로컬 환경 테스트
./performance/run-performance-tests.sh load local

# 스테이징 환경 테스트
./performance/run-performance-tests.sh load staging

# 스트레스 테스트
./performance/run-performance-tests.sh stress local
```

### 성능 목표
- **응답 시간**: p95 < 500ms, p99 < 1000ms
- **에러율**: < 1%
- **동시 사용자**: 1000명 이상 지원
- **처리량**: 1000 req/s 이상

## 🔐 보안 체크리스트

### 애플리케이션 보안
- [x] HTTPS/TLS 적용
- [x] CORS 정책 설정
- [x] SQL Injection 방지
- [x] XSS 방지
- [x] CSRF 토큰 구현

### 인프라 보안
- [x] 네트워크 정책 적용
- [x] Pod 보안 정책
- [x] RBAC 권한 최소화
- [x] 시크릿 암호화
- [x] 이미지 취약점 스캔

### 운영 보안
- [ ] 정기 백업 설정
- [ ] 재해 복구 계획
- [ ] 접근 로그 모니터링
- [ ] 보안 패치 자동화

## 📈 스케일링 전략

### Horizontal Pod Autoscaler
```yaml
# CPU 기반 스케일링
- 백엔드: 2-10 replicas (CPU 70%)
- 프론트엔드: 3-20 replicas (CPU 60%)

# 메모리 기반 스케일링
- 백엔드: Memory 80%
- 프론트엔드: Memory 70%
```

### Cluster Autoscaler
```bash
# EKS/GKE/AKS에서 노드 자동 스케일링 설정
kubectl autoscale nodes --min=3 --max=10
```

## 🔄 롤백 전략

### 배포 실패 시 롤백
```bash
# 이전 버전으로 롤백
kubectl rollout undo deployment/backend -n vibe-coding
kubectl rollout undo deployment/frontend -n vibe-coding

# 특정 버전으로 롤백
kubectl rollout undo deployment/backend --to-revision=2 -n vibe-coding
```

### 데이터베이스 롤백
```bash
# 백업에서 복원
kubectl exec -it postgres-pod -n vibe-coding -- psql -U vibecoding < backup.sql
```

## 📝 운영 매뉴얼

### 일일 점검사항
1. 서비스 헬스체크 확인
2. 리소스 사용량 모니터링
3. 에러 로그 검토
4. 백업 상태 확인

### 주간 작업
1. 보안 패치 적용
2. 성능 메트릭 분석
3. 용량 계획 검토

### 월간 작업
1. 재해 복구 테스트
2. 보안 감사
3. 비용 최적화 검토

## 🚨 장애 대응

### 장애 레벨 정의
- **P1 (Critical)**: 전체 서비스 중단
- **P2 (Major)**: 주요 기능 장애
- **P3 (Minor)**: 일부 기능 제한
- **P4 (Low)**: 사용자 경험 저하

### 대응 프로세스
1. 장애 감지 및 알림
2. 영향도 평가
3. 즉시 조치 (롤백/스케일링)
4. 근본 원인 분석
5. 영구 수정 및 배포
6. 사후 분석 문서화

## 📞 연락처

### 담당자 정보
- **개발팀**: dev@vibecoding.com
- **운영팀**: ops@vibecoding.com
- **보안팀**: security@vibecoding.com

### 긴급 연락망
- **On-Call Engineer**: +82-10-XXXX-XXXX
- **팀 리더**: +82-10-YYYY-YYYY
- **CTO**: +82-10-ZZZZ-ZZZZ

---

이 가이드는 지속적으로 업데이트됩니다.
최신 버전은 항상 main 브랜치를 참고하세요.