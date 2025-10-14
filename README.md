
## 개발환경 세팅
1. JDK : 17.0.15 (Amazon Corretto) 
2. MySQL : 8.0.34
3. Gradle : 8.14.3
4. Spring Boot : 3.5.6

---

## Docker에서 MySQL 컨테이너 실행

### Windows (PowerShell/CMD)
```
docker run -d --name coreboard-mysql `
  -e MYSQL_ROOT_PASSWORD=password `
  -e MYSQL_DATABASE=CoreBoard `
  -p 3307:3306 `
  -v "${PWD}\docker\mysql\data:/var/lib/mysql" `
  -v "${PWD}\docker\mysql\init:/docker-entrypoint-initdb.d" `
  --restart=always mysql:8.0.34
```
  - 컨테이너 상태 확인

```
docker ps
docker exec -it coreboard-mysql mysql -u root -p
# 비밀번호: password
SHOW DATABASES;
```



### macOS / Linux

```
docker run -d \
  --name coreboard-mysql \
  -e MYSQL_ROOT_PASSWORD=password \
  -e MYSQL_DATABASE=CoreBoard \
  -p 3306:3306 \
  -v "${PWD}/docker/mysql/data:/var/lib/mysql" \
  -v "${PWD}/docker/mysql/init:/docker-entrypoint-initdb.d" \
  --restart always \
  mysql:8.0.34
```

---

## IntelliJ Run/Debug 설정
실행 시 로컬 환경(application-local.yml)을 사용하려면
Run ▸ Edit Configurations ▸ Active Profiles에 local 입력

