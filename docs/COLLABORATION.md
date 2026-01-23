# 🤝 Collaboration Rules: WhoReads Team

이 문서는 WhoReads 프로젝트의 일관된 코드 품질과 효율적인 협업을 위한 규칙을 담고 있습니다.

## 🌿 Branch Strategy: Git Flow (Simplified)
CI/CD 분리와 안정적인 배포를 위해 **Git Flow 변형**을 채택합니다.

### 브랜치 종류
| 브랜치 | 용도 | 분기 출발점 | 병합 대상 |
|--------|------|-------------|-----------|
| `main` | 프로덕션 배포 | - | - |
| `develop` | 개발 통합 (staging) | main | main |
| `feat/#이슈번호-기능명` | 새 기능 개발 | develop | develop |
| `fix/#이슈번호-버그명` | 버그 수정 | develop | develop |
| `hotfix/#이슈번호-버그명` | 프로덕션 긴급 수정 | main | main + develop |
| `chore/#이슈번호-작업명` | 설정/유지보수 | develop | develop |
| `docs/#이슈번호-작업명` | 문서 작업 | develop | develop |

> **Note**: 연결할 이슈가 없는 경우 `#0`을 사용합니다. (예: `chore/#0-cleanup`)

### 일반 워크플로우
```
feat/*, fix/* ──PR──▶ develop ──merge──▶ main
                     (CI + staging)    (prod 배포)
```

1. `feat/*`, `fix/*`, `chore/*` 브랜치에서 작업 후 `develop`으로 PR
2. CodeRabbit 리뷰 + CI 통과 후 `develop`에 merge → staging 배포
3. 검증 완료 후 `develop` → `main` merge → production 배포

### Hotfix 워크플로우 (긴급 수정)
```
main ◀── hotfix/* ──▶ main + develop
```

1. `main`에서 `hotfix/*` 브랜치 생성
2. 수정 후 `main`으로 PR → production 배포
3. 동일 변경사항을 `develop`에도 merge

## 💬 Commit Convention
나중에 히스토리를 쉽게 추적할 수 있도록 아래 형식을 따릅니다.
`Type: #이슈번호 요약내용` (상세 내용은 본문에 작성)

| Type | Description |
| :--- | :--- |
| **feat** | 새로운 기능 추가 (예: 독서 DNA 테스트 구현) |
| **fix** | 버그 수정 |
| **docs** | 문서 수정 (README, API_SPEC 등) |
| **refactor** | 코드 리팩토링 (기능 변화 없음) |
| **chore** | 빌드 설정, 패키지 매니저 관리 |

(예시)
```
feat: #25 독서 DNA 테스트 매칭 알고리즘 구현

- MBTI 기반의 유저 성향 분석 로직 추가
- 성향 결과에 따른 유명인 롤모델 매칭 엔진(MatchingEngine) 개발
- 테스트 결과 저장 및 조회 API 구현
```

## 📋 PR Title Convention
PR 제목은 작업의 의도를 명확히 전달하기 위해 아래 형식을 준수합니다.

- **형식**: `[Type] 작업 요약 (#이슈번호)`
- **예시**:
    - `[Feat] 독서 DNA 테스트 알고리즘 구현 (#25)`
    - `[Fix] 개인 서재 Drop It 상태 변경 오류 수정 (#32)`
    - `[Docs] API 명세서 최신화 (#10)`

### PR 작성 시 주의사항
1. **이슈 연결**: PR 본문에 `Closes #이슈번호`를 기재하여 머지 시 관련 이슈가 자동으로 닫히도록 합니다.
2. **작업 단위**: 하나의 PR에는 가급적 하나의 이슈(기능)만 포함하여 리뷰의 집중도를 높입니다.
3. **태그 활용**: 리뷰가 급한 경우 제목 앞에 `[URGENT]` 태그를 붙여 팀원들에게 알립니다.

## ✅ PR & Code Review
- **PR 생성**: 모든 작업은 PR을 통해 `develop`으로 합쳐집니다.
- **최소 승인 인원**: 최소 **1명 이상의 Approve**가 있어야 머지가 가능합니다.
- **Merge 방식**: 커밋 히스토리를 깔끔하게 관리하기 위해 **Squash and Merge**를 권장합니다.

## 📁 Domain-based Directory Structure
도메인 기반으로 폴더를 나누어 작업 충돌을 최소화합니다.
- `domain/member`: 회원 관련
- `domain/celebrity`: 유명인 및 인용 데이터
- `domain/library`: 개인 서재 및 **Drop It** 로직
- `domain/dna`: **독서 DNA 테스트** 알고리즘
- `domain/notification`: **스낵 알림** 시스템
