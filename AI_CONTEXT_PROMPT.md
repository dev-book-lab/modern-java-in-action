Dev Book Lab - AI 컨텍스트 복원 프롬프트
새로운 AI 세션에서 프로젝트 컨텍스트를 빠르게 복원하고 챕터 생성을 진행하기 위한 프롬프트
📋 사용 방법
이 문서의 "프롬프트 본문" 섹션을 복사
새 AI 세션에 붙여넣기
학습 자료(정리본, AI 대화 기록)를 첨부
"진행해줘!" 입력
🎯 프롬프트 본문# Dev Book Lab 프로젝트 컨텍스트

안녕! 나는 **Dev Book Lab**이라는 프로젝트를 진행하고 있어.
이 프로젝트는 개발 서적을 AI(너)와 함께 분석하고 정리하는 학습 연구소야.

## 📚 프로젝트 개요

### 핵심 철학
> "AI와 대화하며 얻은 통찰을 기록한다"

단순한 책 요약이 아니라, AI와 대화하면서 **"왜?"** 라는 질문을 통해
개념의 본질을 탐구하고, 그 과정을 문서화하는 프로젝트야.

### 학습 방법론Read (책 읽기)
↓
Analyze (AI와 개념 분석)
↓
Deep Dive (원리 심층 탐구)
↓
Practice (실습 코드 작성)
↓
Document (나만의 언어로 재구성)

---

## 🏗️ 프로젝트 구조

현재 **Modern Java in Action** 책을 진행 중이며,
각 챕터는 다음과 같은 구조로 구성돼:chapter{NN}/
├── README.md                      # 메인 학습 가이드
│   ├── 학습 목표
│   ├── 핵심 개념 설명
│   ├── Before & After 비교
│   ├── 실전 가이드
│   ├── 체크리스트
│   └── 다음 단계 안내
│
├── code/                          # 실습 코드
│   ├── Example1.java              # 핵심 개념 시연
│   ├── Example2.java              # 실전 패턴
│   └── Example3.java              # 고급 활용
│   └── ...
│
└── advanced/                      # 심화 학습 자료
├── cheatsheet.md              # 빠른 참조 가이드
├── deep-dive.md               # 내부 원리와 설계 철학
├── practice-guide.md          # 실전 패턴 (필요시)
├── {topic}-guide.md           # 특정 주제 심화 (필요시)
└── qa-sessions.md             # AI와의 Q&A 대화록

---

## 📝 각 파일의 역할

### 1. README.md (메인 가이드)
**목적:** 챕터의 전체적인 학습 로드맵 제공

**구성:**
- 🎯 학습 목표 (체크리스트 형태)
- 📚 핵심 개념 (상세 설명)
- 🔄 Before & After (자바 7 vs 자바 8 같은 비교)
- 💡 실전 가이드 (사용 예시)
- ✅ 학습 체크리스트
- 🚀 다음 단계 (다음 챕터 연결)

**특징:**
- 마크다운 스타일: 이모지 활용, 박스/표로 시각화
- 코드 예제: 실행 가능한 간단한 스니펫
- 링크: advanced 문서들로 연결

### 2. code/*.java (실습 코드)
**목적:** 개념을 실제로 실행하고 체험

**특징:**
- 모든 코드는 **그대로 복사해서 실행 가능**
- 주석으로 **학습 포인트** 명시
- **출력 결과** 포함 (System.out.println)
- **단계별 발전 과정** 시연 (Before → After)

**패턴:**
```java/**

Chapter XX: {주제}


학습 목표 1




학습 목표 2




학습 목표 3
*/
public class ExampleClass {
public static void main(String[] args) {
System.out.println("=== 1. 첫 번째 패턴 ===");
// 코드 + 설명 주석


    System.out.println("\n=== 2. 두 번째 패턴 ===");
    // 코드 + 설명 주석    // ... 반복
}
}

### 3. advanced/cheatsheet.md (빠른 참조)
**목적:** 한눈에 보는 핵심 요약

**구성:**
- 핵심 개념 1줄 요약
- 주요 API 표 (메서드, 시그니처, 용도, 예시)
- 자주 사용하는 패턴 10가지
- 실수하기 쉬운 부분
- 코드 스니펫 모음

**특징:**
- 간결함 (1-2페이지 분량)
- 표와 코드 블록 위주
- 실무에서 바로 참조 가능

### 4. advanced/deep-dive.md (심화 학습)
**목적:** 개념의 본질과 내부 원리 탐구

**구성:**
- 왜 이 기능이 필요한가? (역사적 배경)
- 내부 구조와 동작 원리
- 설계 철학과 트레이드오프
- 성능과 최적화
- 고급 활용 패턴

**특징:**
- 깊이 있는 설명
- 다이어그램/플로우차트 (텍스트로 표현)
- "왜?"에 대한 답변

### 5. advanced/practice-guide.md (실전 패턴)
**목적:** 10가지 실전 패턴을 단계별로 학습

**구성:**패턴 1️⃣: {패턴명}
상황: 이런 경우에 사용
❌ 틀린 방법: 왜 안 되는지
✅ 올바른 방법: 어떻게 해야 하는지
🎯 핵심 규칙: 기억할 점패턴 2️⃣: ...

**특징:**
- 문제 → 해결 과정
- 실무 시나리오 기반
- 단계별 개선 과정

### 6. advanced/{topic}-guide.md (특정 주제 심화)
**목적:** 특정 주제를 완벽하게 마스터

**예시:**
- `comparator-guide.md` (Comparator 완벽 가이드)
- `stream-api-guide.md` (스트림 API 완벽 가이드)

**구성:**
- 해당 주제의 모든 것
- 7가지 질문과 답변 또는 패턴
- 내부 구현 원리

### 7. advanced/qa-sessions.md (AI 대화록)
**목적:** AI와 나눈 질문과 답변 정리

**구성:**Session 1: {주제}Q1: {질문}
A: {답변}Q2: {질문}
A: {답변}...핵심 통찰: 배운 것 정리

**특징:**
- 실제 대화 내용 재구성
- "왜?"를 통한 깊이 있는 이해
- 개념의 연결고리 발견

---

## 🎨 문서 작성 스타일 가이드

### 마크다운 스타일
- **이모지 활용**: 섹션 구분, 시각적 강조
  - 🎯 목표, 📚 개념, 💡 팁, ✅ 체크, ❌ 잘못된 예, ✅ 올바른 예
- **코드 블록**: 언어 명시 (```java, ```bash)
- **표**: 비교, API 레퍼런스
- **박스**: 중요 개념 강조

### 코드 스타일
```java// ❌ 잘못된 방법
badCode();// ✅ 올바른 방법
goodCode();// 💡 더 나은 방법
betterCode();

### 설명 스타일
- **선언적**: "~는 ~이다" (명확한 정의)
- **비교**: Before & After, vs 패턴
- **예시**: 실제 코드와 함께
- **이유**: "왜?"에 대한 답변

---

## 📊 지금까지 완료된 챕터

### ✅ Chapter 01: 자바 8, 9, 10, 11 - 무슨 일이 일어나고 있는가?
**핵심 주제:**
- 스트림 API (내부 반복, 병렬 처리)
- 동작 파라미터화 (메서드를 값으로)
- 메서드 참조와 람다
- 디폴트 메서드

**파일:**
- README.md
- code/FilteringApples.java
- code/MethodReferenceExample.java
- code/StreamExample.java
- code/ParallelStreamExample.java
- code/DefaultMethodExample.java
- advanced/cheatsheet.md
- advanced/deep-dive.md
- advanced/qa-sessions.md

### ✅ Chapter 02: 동작 파라미터화 코드 전달하기
**핵심 주제:**
- 7단계 발전 과정 (녹색 사과 → 제너릭)
- Predicate 패턴과 합성
- Consumer, Function
- Comparator 완벽 가이드
- Runnable, Callable

**파일:**
- README.md
- code/FilteringApples.java
- code/PredicateExamples.java
- code/ComparatorExamples.java
- code/ThreadExamples.java
- advanced/cheatsheet.md
- advanced/deep-dive.md
- advanced/practice-guide.md
- advanced/comparator-guide.md
- advanced/qa-sessions.md

---

## 🎯 너에게 요청하는 작업

이제 **Chapter {NN}: {챕터 제목}** 을 위의 구조와 스타일로 생성해줘.

### 📎 첨부 파일
1. **책의 해당 챕터 정리본** (마크다운 또는 텍스트)
2. **AI와 나눈 대화 기록** (있다면)
3. **기존 코드 예제** (있다면)

### 📝 생성해야 할 파일
1. `README.md` - 메인 학습 가이드
2. `code/*.java` - 실습 코드 (3-5개)
3. `advanced/cheatsheet.md` - 빠른 참조
4. `advanced/deep-dive.md` - 심화 학습
5. `advanced/qa-sessions.md` - Q&A 세션
6. (선택) `advanced/{topic}-guide.md` - 특정 주제 심화

### ✨ 작성 원칙
1. **실행 가능성**: 모든 코드는 복사-붙여넣기로 실행 가능해야 함
2. **점진적 발전**: Before → After, 단계별 개선 과정 시연
3. **깊이 있는 이해**: "왜?"에 대한 답변 포함
4. **실전 중심**: 실무에서 바로 쓸 수 있는 패턴
5. **시각적**: 이모지, 표, 박스로 가독성 향상
6. **연결성**: 이전/다음 챕터와의 연결 명시

### 🔗 참고할 이전 챕터
- Chapter 01과 02의 스타일과 구조를 참고
- 특히 README.md의 구성과 톤앤매너
- code 파일의 주석 스타일
- advanced 문서들의 깊이

---

진행해줘!📝 사용 예시예시 1: Chapter 03 (람다 표현식) 생성프롬프트:
[위의 "프롬프트 본문" 전체 복사]

+ 첨부 파일:
- chapter03_summary.md (책 정리본)
- chapter03_discussion.md (AI 대화 기록)예시 2: Chapter 04 (스트림 API) 생성프롬프트:
[위의 "프롬프트 본문" 전체 복사]

+ 첨부 파일:
- modern_java_chapter04.md (책 요약)
- existing_stream_examples.java (기존 코드)
- ai_conversation_log.txt (대화 기록)🎯 AI가 즉시 이해할 수 있는 정보위 프롬프트를 사용하면 AI는 다음을 즉시 파악합니다:✅ 프로젝트 목적: 책 요약이 아닌 AI와의 대화를 통한 깊이 있는 학습
✅ 구조: chapter{NN} 폴더 구조와 각 파일의 역할
✅ 스타일: 마크다운, 코드 주석, 설명 방식
✅ 원칙: 실행 가능성, 점진적 발전, 실전 중심
✅ 참고: 이전 챕터들의 스타일 계승
✅ 입력: 정리본과 대화 기록을 바탕으로 재구성💡 팁더 나은 결과를 위한 팁
정리본은 상세할수록 좋음

책의 핵심 개념
코드 예제
저자의 설명



AI 대화 기록 포함 시

개념에 대한 질문
"왜?"라는 질문들
이해한 내용 정리



기존 코드가 있다면

함께 첨부
AI가 개선하거나 재구성할 수 있음



특정 강조점이 있다면

프롬프트 마지막에 추가
"특히 {주제}를 중점적으로 다뤄줘"


🔄 지속적 개선AI의 첫 결과물이 마음에 들지 않으면:좋은데, {부분}을 좀 더 {방향}으로 수정해줘.
Chapter 01의 {파일}처럼 {스타일}로 작성해줘.예시:
좋은데, README를 좀 더 초보자 친화적으로 수정해줘.
Chapter 02의 practice-guide.md처럼 단계별로 자세히 작성해줘.
코드 예제에 더 많은 주석을 추가해줘.📚 완성 후 확인사항생성 완료 후 다음을 확인:
 README.md의 학습 목표가 명확한가?
 코드가 실제로 실행 가능한가?
 Before & After 비교가 있는가?
 advanced 문서들이 충실한가?
 이전/다음 챕터와 연결되는가?
 "왜?"에 대한 답변이 있는가?
 실전에서 쓸 수 있는 패턴인가?
<div align="center">🎉 이제 어떤 챕터든 빠르게 생성할 수 있습니다!AI와 함께, 더 깊이 있는 학습을! 🚀</div>
