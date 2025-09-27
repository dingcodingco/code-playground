# 다단계 빌드를 사용한 Spring Boot 애플리케이션 Dockerfile

# 1단계: 빌드 환경
FROM eclipse-temurin:21-jdk AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 래퍼와 빌드 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY checkstyle.xml .

# 실행 권한 부여
RUN chmod +x ./gradlew

# 의존성 캐싱을 위한 빈 빌드 실행
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드
RUN ./gradlew bootJar --no-daemon

# 빌드 결과 확인
RUN ls -la build/libs/

# 2단계: 실행 환경
FROM eclipse-temurin:21-jre

# 메타데이터 라벨
LABEL maintainer="Vibe Coding Team <team@vibecoding.com>"
LABEL version="1.0.0"
LABEL description="Vibe Coding Backend Service"

# 시스템 업데이트 및 필요 패키지 설치
RUN apt-get update && apt-get install -y \
    curl \
    python3 \
    python3-pip \
    nodejs \
    npm \
    default-jdk \
    && rm -rf /var/lib/apt/lists/*

# Node.js 및 Python 실행 환경 설정
RUN npm install -g node
RUN python3 --version
RUN java --version

# 애플리케이션 사용자 생성
RUN groupadd -r appuser && useradd -r -g appuser appuser

# 작업 디렉토리 설정
WORKDIR /app

# 코드 실행을 위한 임시 디렉토리 생성
RUN mkdir -p /tmp/vibe-coding-exec && chown -R appuser:appuser /tmp/vibe-coding-exec

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 파일 권한 설정
RUN chown -R appuser:appuser /app

# 사용자 전환
USER appuser

# 포트 노출
EXPOSE 8080

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM 옵션 설정
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]