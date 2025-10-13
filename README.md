
## 개발환경 세팅
1. JDK : 17.0.15 (Amazon Corretto) 
2. MySQL : 8.0.34
3. Gradle : 8.3
4. Spring Boot : 3.5.6
5. OS : Windows 11

---


##  application-dev.yml (로컬 개발용)

- application-dev.yml에 있는 Mysql 비밀번호가 민감하다 판단되어 .gitignore에 포함되어 있어 직접 로컬에 만들어 사용해야 합니다.

-  경로 : `src/main/resources/`
```
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/CoreBoard?allowPublicKeyRetrieval=true&useSSL=false&characterEncoding=utf8&serverTimezone=Asia/Seoul
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver

jwt:
  secret:
    key: ad28a1bb3bad96055ba5345cbe3c6b99
aes:
  secret:
    key: MySuperSecretKey


```

---


