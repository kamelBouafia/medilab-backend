MediLab Pro - Spring Boot Backend (Java 21)

How to run:
1. Ensure Java 21 and Maven are installed.
2. Create a Postgres database and set environment variables:
   - SPRING_DATASOURCE_URL (e.g. jdbc:postgresql://localhost:5432/medilab)
   - SPRING_DATASOURCE_USERNAME
   - SPRING_DATASOURCE_PASSWORD
   - JWT_SECRET (secure key)
   - GEMINI_API_KEY (optional)
3. mvn spring-boot:run
