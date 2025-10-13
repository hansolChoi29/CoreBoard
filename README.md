
## 개발환경 세팅
1. JDK : 17 
2. MySQL : 8.0 

---


##  application-dev.yml (로컬 개발용)

- application-dev.yml은 .gitignore에 포함되어 있어 직접 로컬에 만들어 사용해야 합니다.

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


