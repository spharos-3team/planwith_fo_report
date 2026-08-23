# Release Note / RM — planwith-fo-report

## 1. 서비스 개요

---

`planwith-fo-report`는 Story 및 Comment 신고를 접수하고, 신고 상태와 누적 데이터를 관리하는 신고 도메인 서비스입니다.

주요 책임은 다음과 같습니다.

- Story 및 Comment 대상 신고 접수
- 신고 단건 조회와 검토 워크플로우 관리
- 댓글 신고 입력값 및 신고 대상 검증
- 동일 회원의 동일 대상 중복 신고 방지
- 댓글별 신고 건수 누적
- 신고 3회 도달 시 댓글 숨김 이벤트 생성
- Outbox 기반 Kafka 이벤트 발행
- Comment Service와의 댓글 존재·작성자·신고 가능 여부 확인

| 항목 | 값 |
|---|---|
| 서비스명 | `planwith-fo-report` |
| 애플리케이션명 | `planwith-fo-report` |
| 기본 포트 | `8092` |
| Java | `17` |
| Spring Boot | `4.0.7` |
| 저장소 | MySQL |
| 메시징 | Kafka |
| 서비스 디스커버리 | Eureka |
| API 문서 | Springdoc OpenAPI |

## 2. 도메인 범위

---

### 일반 신고 도메인

신고 대상은 다음 두 유형을 지원합니다.

- `STORY`
- `COMMENT`

일반 신고 사유는 다음과 같습니다.

- `SPAM`
- `HARASSMENT`
- `HATE`
- `SEXUAL`
- `ILLEGAL`
- `OTHER`

신고 상태는 다음 순서로 전이됩니다.

```text
RECEIVED
   ↓
REVIEWING
   ├─→ APPROVED
   │      ↓
   │   ACTIONED
   │
   └─→ REJECTED
```

잘못된 상태 전이는 도메인 예외로 차단합니다.

### 댓글 신고 도메인

댓글 신고 사유는 다음과 같습니다.

- `SPAM`
- `ABUSE`
- `HATE`
- `SEXUAL`
- `PRIVACY`
- `OTHER`

댓글 신고 처리 흐름은 다음과 같습니다.

```text
신고 요청
  ↓
입력값 검증
  ↓
Comment Service에서 댓글 확인
  ↓
자기 댓글 신고 여부 확인
  ↓
동일 회원 중복 신고 확인
  ↓
story_comment_report 저장
  ↓
commentUuid 기준 신고 건수 집계
  ↓
신고 건수 3회 도달
  ↓
COMMENT_REPORT_THRESHOLD_REACHED Outbox 저장
  ↓
Kafka 발행
  ↓
Comment Service 댓글 숨김
```

임계치는 기본값 `3`이며 환경 변수로 변경할 수 있습니다.

3회를 초과한 추가 신고는 누적되지만 숨김 이벤트는 다시 생성하지 않습니다.

### 데이터 원장

| 테이블 | 책임 |
|---|---|
| `report` | Story 및 Comment 일반 신고와 검토 상태 관리 |
| `story_comment_report` | 댓글별 회원 신고 원장 및 누적 기준 |
| `report_outbox_event` | Kafka 발행 대상 이벤트의 Outbox 저장 |

### 데이터 정합성

- `reporter_uuid + target_type + target_uuid` UNIQUE
- `comment_uuid + member_uuid` UNIQUE
- `report_uuid` UNIQUE
- `comment_report_uuid` UNIQUE
- `event_uuid` UNIQUE
- 댓글 신고 집계를 위한 `comment_uuid` 인덱스 적용
- Outbox 처리를 위한 `status + created_at` 인덱스 적용

## 3. API 그룹

---

### 일반 신고 API

| Method | URL | 설명 |
|---|---|---|
| `POST` | `/api/planwith-fo-report/reports` | Story 또는 Comment 신고 생성 |
| `GET` | `/api/planwith-fo-report/reports/{reportUuid}` | 신고 UUID 기반 단건 조회 |
| `POST` | `/api/planwith-fo-report/reports/{reportUuid}/workflow` | 신고 검토 상태 전이 |

워크플로우 액션은 다음 값을 지원합니다.

- `START_REVIEW`
- `APPROVE`
- `REJECT`
- `MARK_ACTIONED`

### 댓글 신고 API

| Method | URL | 설명 |
|---|---|---|
| `POST` | `/api/planwith-fo-report/comment-reports/input-validation` | 댓글 UUID, 신고 사유, 회원 헤더 검증 |
| `POST` | `/api/planwith-fo-report/comment-reports/target-validation` | 댓글 존재·삭제·작성자 및 자기 신고 검증 |
| `POST` | `/api/planwith-fo-report/reports/comments/{commentUuid}` | 댓글 신고 생성 및 누적 처리 |
| `GET` | `/api/planwith-fo-report/reports/comments/{commentUuid}/count` | 댓글 UUID 기준 신고 누적 건수 조회 |

댓글 신고 생성 및 입력 검증 API는 로그인 회원 식별값으로 다음 헤더를 사용합니다.

```http
X-Member-Uuid: {memberUuid}
```

### 운영 확인 API

| Method | URL | 설명 |
|---|---|---|
| `GET` | `/api/planwith-fo-report/deploy-check` | 서비스 배포 상태 및 marker 확인 |
| `POST` | `/api/planwith-fo-report/login` | 배포 확인용 로그인 |
| `GET` | `/actuator/health` | 서비스 상태 확인 |
| `GET` | `/actuator/info` | 서비스 정보 확인 |
| `GET` | `/v3/api-docs` | OpenAPI 문서 조회 |
| `GET` | `/swagger-ui.html` | 로컬 Swagger UI |

## 4. 외부 연동

---

### 외부 공개 API

외부 공개 API 연동은 없습니다.

### Comment Service

댓글 신고 전에 Comment Service 내부 API를 호출합니다.

```http
GET /internal/comments/{commentUuid}/report-context
```

확인 항목은 다음과 같습니다.

- 댓글 존재 여부
- 댓글 작성자 UUID
- 댓글 신고 가능 여부
- 자기 자신이 작성한 댓글인지 여부

기본 서비스 주소는 다음과 같습니다.

```text
http://planwith-fo-comment
```

### Kafka

발행 가능한 Kafka Topic은 다음과 같습니다.

| 이벤트 | 기본 Topic |
|---|---|
| 신고 생성 | `planwith.report.created` |
| 신고 검토 | `planwith.report.reviewed` |
| 운영 조치 요청 | `planwith.report.moderation-action-required` |
| 댓글 신고 임계치 도달 | `planwith.report.comment-report-threshold-reached` |

댓글 신고 3회 도달 시 발행되는 핵심 이벤트는 다음과 같습니다.

```text
COMMENT_REPORT_THRESHOLD_REACHED
```

현재 Kafka Inbound Adapter는 향후 외부 이벤트 구독을 위한 확장 지점만 제공하며, 자동 판정 알고리즘이나 실제 수신 Listener는 개발 범위에 포함되지 않았습니다.

### Eureka 및 Gateway

- Eureka 서비스명: `planwith-fo-report`
- Gateway URI: `lb://planwith-fo-report`
- Gateway API 경로: `/api/planwith-fo-report/**`
- Gateway OpenAPI 경로: `/docs/planwith-fo-report/**`
- 외부 클라이언트는 서비스 포트 `8092`가 아니라 Gateway 포트 `8000`을 사용합니다.

## 5. 비기능 / 품질

---

- Controller, Application Service, Domain, Port, Adapter 책임 분리
- 생성자 주입 적용
- Service 계층 트랜잭션 관리
- Request/Response DTO 분리
- Jakarta Validation 기반 입력값 검증
- 전역 예외 처리와 일관된 오류 응답 제공
- 도메인 객체 내부 상태 전이 규칙 적용
- Application 중복 검사와 DB UNIQUE 제약의 이중 방어
- Outbox 패턴을 통한 저장과 메시지 발행 책임 분리
- Outbox 상태 `PENDING`, `PUBLISHED`, `FAILED` 관리
- JPA `open-in-view=false`
- Hibernate JDBC 시간대 UTC 적용
- Swagger/OpenAPI 제공
- Actuator health/info 제공
- 컴파일 및 null-safety 경고 해소
- 동시 신고 요청에 대한 중복 데이터 방지 검증
- Embedded Kafka 기반 메시지 발행·소비 통합 테스트 제공

## 6. 배포 설정 요약

---

### Docker

- 빌드 이미지: `eclipse-temurin:17-jdk-alpine`
- 실행 이미지: `eclipse-temurin:17-jre-alpine`
- 애플리케이션 이미지: `planwith/planwith-fo-report:latest`
- 컨테이너명: `planwith-planwith-fo-report`
- 비루트 사용자 `spring:spring`으로 실행
- 서버 로컬 디버그 포트: `127.0.0.1:8092`
- 네트워크: `planwith-net`

### 주요 환경 변수

| 환경 변수 | 기본값 또는 설명 |
|---|---|
| `SERVER_PORT` | `8092` |
| `SERVER_ADDRESS` | `0.0.0.0` |
| `DB_URL` | MySQL JDBC URL |
| `DB_USERNAME` | DB 사용자 |
| `DB_PASSWORD` | DB 비밀번호 |
| `JPA_DDL_AUTO` | `update` |
| `EUREKA_CLIENT_ENABLED` | `true` |
| `EUREKA_DEFAULT_ZONE` | `http://discovery:8761/eureka/` |
| `GATEWAY_PUBLIC_URL` | `/` |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |
| `KAFKA_ENABLED` | `false` |
| `OUTBOX_RELAY_ENABLED` | `false` |
| `OUTBOX_RELAY_INTERVAL_MS` | `5000` |
| `OUTBOX_BATCH_SIZE` | `50` |
| `COMMENT_SERVICE_BASE_URL` | `http://planwith-fo-comment` |
| `COMMENT_SERVICE_REPORT_CONTEXT_PATH` | `/internal/comments/{commentUuid}/report-context` |
| `COMMENT_REPORT_HIDE_THRESHOLD` | `3` |
| `SPRINGDOC_SWAGGER_UI_ENABLED` | 환경별 설정 |
| `DEPLOY_MARKER` | `planwith-fo-report-deploy-v1` |

## 7. 운영 주의사항

---

- 운영 환경에서 댓글 숨김 이벤트를 발행하려면 `KAFKA_ENABLED=true`와 `OUTBOX_RELAY_ENABLED=true`를 함께 설정해야 합니다.
- Outbox Relay가 비활성화되면 임계치 이벤트는 DB에 `PENDING` 상태로 남습니다.
- Kafka가 비활성화된 상태에서는 Outbox Relay가 이벤트 발행을 건너뜁니다.
- Kafka health indicator는 기본 비활성화되어 있으므로 Kafka 연결 상태는 별도 모니터링이 필요합니다.
- Outbox 이벤트는 `KafkaTemplate.send()` 호출 후 `PUBLISHED`로 변경됩니다. Broker 전달 확인 수준이 필요한 경우 발행 ACK 처리 보강을 검토해야 합니다.
- 운영 기본 JPA 설정은 `ddl-auto=update`이며 Flyway/Liquibase Migration은 현재 적용되어 있지 않습니다. 운영 배포 전 스키마 변경 관리 정책을 별도로 확정해야 합니다.
- `X-Member-Uuid` 헤더는 Gateway 또는 인증 서비스가 검증해 전달한다는 전제가 필요합니다.
- `/login`은 배포 확인용 기능이며 실제 운영 인증 수단으로 사용하면 안 됩니다.
- 기본 로그인 ID와 비밀번호는 운영 환경에서 반드시 환경 변수로 변경하거나 기능을 비활성화해야 합니다.
- 일반 신고의 `ReportReason`과 댓글 신고의 `ReportType`은 일부 값이 다릅니다.
  - 일반 신고: `HARASSMENT`, `ILLEGAL`
  - 댓글 신고: `ABUSE`, `PRIVACY`
- Comment Service 장애 시 댓글 신고 대상 검증은 `COMMENT_SERVICE_UNAVAILABLE`로 실패합니다.
- 댓글 숨김 이후에도 Comment Service의 `reportable` 응답이 `true`라면 추가 신고는 누적됩니다.
- 외부 클라이언트는 서비스 포트에 직접 접근하지 않고 Gateway를 통해 호출해야 합니다.
- Docker 배포 시 MySQL과 Eureka가 준비되어 있어야 합니다.

## 8. 개발 완료 범위 (단계 요약)

---

### STEP 01. 댓글 신고 Domain 구현

- `StoryCommentReport`
- `ReportType`
- Repository Port 및 Persistence Adapter

### STEP 02. 신고 대상 댓글 검증

- Comment Service 내부 API 연동
- 댓글 존재 여부 확인
- 댓글 작성자 확인
- 삭제·신고 불가 댓글 차단
- 자기 댓글 신고 방지

### STEP 03. 신고 입력 Validation

- `commentUuid` 검증
- `memberUuid` 검증
- `reportType` 검증
- 댓글 신고 사유 6종 지원

### STEP 04. 댓글 신고 생성

- 댓글 신고 생성 API
- 댓글 신고 UUID 생성
- `story_comment_report` 저장

### STEP 05. 중복 신고 방지

- `commentUuid + memberUuid` Application 검사
- DB UNIQUE 제약을 통한 최종 중복 방지
- 중복 신고 도메인 예외 처리

### STEP 06. 신고 누적 집계

- `commentUuid` 기준 신고 COUNT
- 서로 다른 회원의 동일 댓글 신고 누적

### STEP 07. 신고 임계치 판단

- 신고 1회·2회·3회 누적 처리
- 설정 기반 숨김 임계치 판단
- 최초 임계치 도달 시에만 이벤트 생성

### STEP 08. 댓글 숨김 이벤트

- `COMMENT_REPORT_THRESHOLD_REACHED` 이벤트
- Outbox 저장
- Kafka Topic 발행
- Comment Service 댓글 숨김 계약 검증

### STEP 09. 예외 및 동시성 처리

- 미인증 회원
- 자기 신고
- 중복 신고
- 존재하지 않는 댓글
- 삭제 또는 신고 불가 댓글
- Comment Service 장애
- 동시 중복 신고 요청

### STEP 10. 전체 통합 테스트

- HTTP 신고 요청
- Comment Service 내부 API
- H2 영속성
- 신고 누적
- 3회 임계치
- Outbox
- Embedded Kafka
- 댓글 숨김 상태
- 추가 신고
- 동시성 전체 흐름 검증

## 9. 검증 상태

---

현재 `develop` 브랜치는 통합 테스트 PR까지 병합된 상태입니다.

```text
전체 테스트: 115
성공: 115
실패: 0
오류: 0
스킵: 0
```

검증 명령:

```powershell
.\gradlew.bat clean test --warning-mode all --console plain
```

검증된 주요 항목은 다음과 같습니다.

- 일반 신고 생성·조회·워크플로우
- 댓글 신고 사유 6종
- 댓글 신고 입력 및 대상 검증
- 중복·자기·없는 댓글·삭제 댓글 신고 차단
- 서로 다른 회원의 신고 누적
- 1회·2회·3회·4회 누적 결과
- 3회 도달 시 Outbox 생성
- Embedded Kafka 이벤트 발행 및 소비
- Comment Service 댓글 숨김 상태 전환
- 임계치 이후 이벤트 재발행 방지
- 동일 회원 동시 요청 시 단일 데이터 보장
- OpenAPI 서버 설정
- Controller·Service·Repository·Domain 단위 및 통합 검증

**RM 결론:**

`planwith-fo-report`는 계획된 STEP 01~10 범위의 신고 접수, 댓글 검증, 중복 방지, 누적 집계, 3회 임계치 판단, Outbox/Kafka 댓글 숨김 이벤트 및 전체 통합 테스트 개발을 완료했습니다.

운영 배포 전에는 Kafka와 Outbox Relay 활성화, 실제 인증 헤더 신뢰 경계, Comment Service 내부 API 연결, DB Migration 정책 및 Kafka 발행 ACK 처리 정책을 최종 확인해야 합니다.
```
