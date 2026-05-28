FROM eclipse-temurin:21-jre

WORKDIR /app

# 이미 GitHub Actions 호스트에서 고속 빌드 완료된 JAR 파일을 복사하여 담기만 함
COPY build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
