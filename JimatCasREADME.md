Jimat CAS Configuration Guidelines
----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

### Install Docker

- [Download Docker Desktop](https://www.docker.com/products/docker-desktop) and Install it.

### Install MySQL Sever and MySQL Workbench (Only For Running in Local)

- [Download MySQL Server](https://dev.mysql.com/downloads/installer) and Install It.
- [Download MySQL Workbench](https://dev.mysql.com/downloads/workbench) and Install It.
- Add new local connection.
- Add new schema inside that connection.

### Add Keystore File (If not Exist)
- Check thekeystore file is present on path `src/main/resources/etc/cas`
- If thekeystore is not present then add keystore as per [CAS Overlay Baeldung](https://www.baeldung.com/spring-security-cas-sso).

### Basic Settings in application.yml
- SET `jdbc_url, username and password` for all DB related configurations.
- SET `Key-store` and `key-store-password` if thekestore file is stored at different path.

### DB Setup
- MySQL DB schema must has `user table` as per query in `cas.authn.jdbc.query[0].sql` property mentioned in application.yml.

### Run Jimat CAS Server
- Go To Project Directory.
- Run Command `docker compose up`.
- Server will start on port mentioned in application.yml

### Features of Jimat CAS server
- Register a service as example below and Basic Authentication user must be registered and has authorities ROLE_SERVICE_ADMIN mentioned in application.yml and user-attributes.json.

```
curl --location 'https://localhost:8443/cas/v1/services' \
--header 'Content-Type: application/json' \
--header 'Authorization: Basic XXXXX' \
--data-raw '{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "https://test-service.com",
  "name" : "TestService",
  "description": "Test Description",
  "redirectUrl": "http://test-service.com/login/callback"
}'
```

- Login via CAS using service

```
Sample URL: https://localhost:8443/cas?service=https://test-service.com
```

```
On Login Success user would be redirected to register service :
http://test-service.com/login/callback?ST={{SERVICE_TICKET}}
```

- Verify ticket status

```
curl -X 'GET' \
  'https://localhost:8443/cas/v1/tickets/{{SERVICE_TICKET}}' \
  -H 'accept: */*'
```

- Logout using service ticket

```
curl -X 'DELETE' \
  'https://localhost:8443/cas/v1/tickets/{{SERVICE_TICKET}}' \
  -H 'accept: application/json'
```

- Swagger URL to check supported APIs

```
https://localhost:8443/cas/swagger-ui/index.html
```