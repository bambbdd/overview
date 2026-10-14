1. 기획 및 요구사항 정의
   문제 정의: 거시 경제의 흐름을 파악하기 위해 ECOS, 공공데이터포털, 경제 뉴스 등을 일일이 찾아봐야 하는 번거로움을 해결. 데이터를 직접 파싱하고 적재하는 백엔드(Java/Spring) 파이프라인과, 이를 세련되게 렌더링하는 프론트엔드(Next.js) 풀스택 구현 능력을 증명함.

핵심 기능 (MVP):

한국은행 ECOS API를 통한 '한국 기준금리', '원/달러 환율' 등 지표 수집 및 DB 저장.

네이버 검색 API를 활용한 최신 '경제/금융 뉴스' 수집 및 제공.

수집된 데이터를 바탕으로 최신 지표와 추이 변화를 시각화한 차트 대시보드 제공.

요구사항 명세서:

[BE - 백엔드] @Scheduled 혹은 Spring Batch를 활용해 자정(00:00)마다 ECOS 및 뉴스 API를 호출하고 PostgreSQL DB를 업데이트하는 스케줄러 구현.

[API - 통신] 클라이언트(Next.js)가 1일(1D), 3일(3D), 7일(7D) 등 특정 기간의 데이터를 요청할 수 있도록 파라미터(기간, 지표 종류)를 받는 RESTful API 제공.

[FE - 프론트엔드] 접속 시 가장 최신 지표 값을 상단 위젯에 배치하고, 1D/3D/7D 필터 버튼을 클릭하면 꺾은선 차트가 즉각적으로 리렌더링 되도록 구현. 하단에는 관련 경제 뉴스 리스트 노출.

2. 설계 및 기술 스택 선정
   기술 스택 상세:

Backend: Java (Spring Boot) - 스케줄러, REST API, JPA(Hibernate)를 통한 데이터베이스 제어.

Frontend: Next.js (React), TailwindCSS(스타일링), Chart.js(차트 렌더링).

Database: PostgreSQL (시계열 데이터 및 텍스트 데이터 처리에 강점).

Infra: Docker & docker-compose - Spring Boot 앱과 PostgreSQL을 각각 컨테이너로 띄워 연동.

시스템 아키텍처:

ECOS / Naver API ➔ Spring Boot (API 호출/파싱) ➔ PostgreSQL (데이터 적재)

Next.js (클라이언트) ↔ Spring Boot (REST API) ↔ PostgreSQL (조회)

DB 설계 (ERD 초안):

economic_indicators: id(PK), type(금리, 환율 등), value(수치), base_date(기준일자)

economic_news: id(PK), title(뉴스 제목), link(URL), pub_date(발행일)

화면 설계서 (Wireframe):

Top: '오늘의 환율', '오늘의 기준금리' 강조형 텍스트 위젯

Middle: 꺾은선 차트 영역 (우측 상단에 [1D] [3D] [7D] Toggle 버튼)

Bottom: 최신 경제 뉴스 리스트 (클릭 시 원문 링크 이동)

3. 개발 환경 및 협업 툴 세팅
   버전 관리 세팅:

GitHub 레포지토리 1개 생성: overview-BE(Java), overview-PE(Next.js)

GitHub Flow 브랜치 전략 활용 (main ↔ feature/OOO)

이슈 트래커 설정:

GitHub Projects를 활용해 Backlog를 관리.

개발 컨벤션 & AI(Codex) 활용 룰:

생성된 코드는 반드시 로컬 환경에서 테스트하고, 설정(application.yml 등)과 의존성(build.gradle)이 일치하는지 검증 후 커밋.

4. 일정 계획 및 마일스톤 설정 (예상: 3~4주)
   Sprint 1: 인프라 및 BE 데이터 수집 파이프라인 (Week 1)

Issue 1: docker-compose.yml을 작성하여 PostgreSQL 컨테이너 띄우기

Issue 2: Spring Boot 프로젝트 초기화 및 DB(JPA) 연동

Issue 3: 한국은행 ECOS API 및 네이버 API 연동 (데이터 파싱)

Issue 4: 자정 실행 스케줄러(@Scheduled) 작성 및 DB Insert 로직 구현

Sprint 2: BE REST API 개발 및 FE 뼈대 (Week 2)

Issue 5: 프론트엔드에서 요청할 기간별(1D, 3D, 7D) 데이터 조회 REST API 엔드포인트 구현

Issue 6: Next.js 프로젝트 생성(npx create-next-app) 및 TailwindCSS 기본 세팅

Issue 7: 상단 최신 지표 위젯 및 뉴스 리스트 UI 컴포넌트 마크업

Sprint 3: 데이터 시각화 및 통합 (Week 3)

Issue 8: Next.js에서 Axios/Fetch를 활용해 Spring Boot API와 통신 연동

Issue 9: Chart.js를 활용해 꺾은선 차트 렌더링 및 1D/3D/7D 필터 버튼 상태(State) 연결

Sprint 4: 도커라이징 및 포트폴리오 마무리 (Week 4)

Issue 10: Spring Boot 애플리케이션 Dockerfile 작성 및 docker-compose에 추가

Issue 11: 포트폴리오용 README.md 상세 작성 (아키텍처, ERD, AI 활용 경험 등)