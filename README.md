#Custom Vocabulary Server Api

## 1. 프로젝트 설명

- 자신이 원하는 단어장을 생성하여 보관하고 언제 어디서나 학습할 수 있도록 하기 위한 Custom 단어장 앱 구현에 필요한 API 를 제공
- 회원가입 이후 자신이 생성한 단어장은 자신의 계정에 귀속되어 개인 단어장 보관함에서 학습하기 위한 기능 제공
- 단어장을 카테고리별 분류하기 위한 카테고리 생성 기능 제공
- 자신의 단어장을 다른 사용자와 공유할 수 있도록 공유 기능 제공
- 여러 사용자가 간단하게 의견을 주고 받을 수 있도록 게시판 기능 제공

## 2. 프로젝트 문서 관리

Rest Docs 를 사용하여 프로젝트 문서화 진행
  
**[API 문서 열람 링크](https://34.123.255.2/docs/index.html)**
 
    
## 3. 기술 스택

- Spring boot
    - version : 2.4.2
    - Spring framework 설정을 보다 편하게 하기 위한 의존성
    - Spring Boot 를 활용한 실행 환경 profile 지정
      
        - application.yml
          
          - 기본적인 설정 등록
          - 로컬 컴퓨터의 MariaDB 와 커넥션 연결
          - 8080 port 에서 실행
          - jwt token security properties 설정
          - file upload properties 설정
              - 로컬 컴퓨터에 파일 업로드 경로 설정
          - 해당 프로젝트 logging level debug 로 설정
            
        - application-test.yml
          
          - Test 설정 등록
          - inMemory h2 Database 와 커넥션 연결
          - flyway disable
          
        - application-production.yml
          
          - 배포 환경 설정 등록
          - AWS RDS 의 MariaDB 와 커넥션 연결
          - file upload properties 설정
              - GCP ubuntu 파일 업로드 경로로 설정
          - 해당 프로젝트 logging level info 로 설정
          - 443 port 에서 실행
          - ssl 설정

- Spring Data Jpa
    - ORM(Object Relational Mapping)
        - 객체와 관계형 데이터베이스의 데이터를 자동으로 매핑해주는 기능
        - Class 와 Table 간의 Paradigm 불일치 문제를 해결하기 위해 사용


- Spring Rest Docs
    - Rest API 문서화를 위해 사용
    - MockMvc 를 통해 테스트된 코드를 문서 조각(snippet)으로 생성
    - 생성된 문서 조각을 .adoc 파일에 끼워 넣어서 문서 생성
    

- Spring Security
    - Application 의 보안(인증, 권한, 인가 등)을 담당하는 스프링 하위 프레임워크


- Querydsl
    - Jpa 를 사용한 동적 쿼리를 보다 쉽게 사용하기 위한 의존성

    
- flyway
    - flyway 를 활용한 Database 버전 관리

    
- JWT Token
    - JWT Token 을 활용한 회원 식별    
