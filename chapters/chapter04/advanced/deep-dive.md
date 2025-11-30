# Chapter 04 스트림 Deep Dive 🔬

> 스트림의 핵심 개념과 내부 동작 원리를 깊이 있게 탐구합니다.

---

## 📑 목차

1. [스트림 vs 컬렉션](#1-스트림-vs-컬렉션)
2. [용어 정리](#2-용어-정리)
3. [외부 반복 vs 내부 반복](#3-외부-반복-vs-내부-반복)
4. [게으른 실행 (Lazy Evaluation)](#4-게으른-실행-lazy-evaluation)
5. [쇼트서킷 (Short-circuit)](#5-쇼트서킷-short-circuit)
6. [루프 퓨전 (Loop Fusion)](#6-루프-퓨전-loop-fusion)

---

## 1. 스트림 vs 컬렉션

### 1.1 핵심 차이점

| 구분 | 컬렉션 (Collection) | 스트림 (Stream) |
|------|-------------------|----------------|
| **데이터** | 메모리에 모든 값 저장 | 요청할 때만 계산 |
| **반복** | 외부 반복 (for, iterator) | 내부 반복 (forEach) |
| **탐색** | 여러 번 탐색 가능 | **단 한 번만** 탐색 가능 |
| **시점** | 적극적 생성 (Eager) | 게으른 생성 (Lazy) |
| **목적** | 데이터 저장 | 데이터 처리 (계산) |

### 1.2 비유: DVD vs Netflix

**컬렉션 = DVD 📀**
```java
List<String> movies = Arrays.asList("The Matrix", "Inception", "Interstellar");
```
- 모든 영화가 DVD에 저장됨
- 언제든지 다시 볼 수 있음
- DVD를 먼저 다 구워야 함

**스트림 = Netflix 📺**
```java
Stream<String> movieStream = Stream.of("The Matrix", "Inception", "Interstellar");
```
- 보는 장면만 전송됨
- 즉시 시청 시작 가능
- 한 번 보면 되감기 불가!

### 1.3 탐색 횟수의 결정적 차이

**컬렉션: 여러 번 탐색 가능 ✅**
```java
List<String> names = Arrays.asList("Alice", "Bob", "Charlie");

// 첫 번째 탐색
for (String name : names) {
    System.out.println(name);
}

// 두 번째 탐색 - OK!
for (String name : names) {
    System.out.println(name);
}
```

**스트림: 단 한 번만 탐색 가능! ⚠️**
```java
Stream<String> stream = Stream.of("Alice", "Bob", "Charlie");

stream.forEach(System.out::println);  // ✅ OK
stream.forEach(System.out::println);  // ❌ IllegalStateException!
```

**해결 방법:**
```java
// 매번 새로운 스트림 생성
list.stream().forEach(System.out::println);
list.stream().forEach(System.out::println);  // ✅ OK
```

### 1.4 메모리 관점 비교

**컬렉션: 모든 데이터 저장**
```java
// 10GB 파일의 모든 라인을 메모리에!
List<String> lines = Files.readAllLines(Paths.get("huge-file.txt"));
// OOM 위험!
```

**스트림: 필요한 것만 계산**
```java
// 라인별로 처리 (메모리 효율적)
try (Stream<String> lines = Files.lines(Paths.get("huge-file.txt"))) {
    lines.filter(line -> line.contains("ERROR"))
         .forEach(System.out::println);
}
```

---

## 2. 용어 정리

### 2.1 스트림 (Stream)

**정의:**
> 데이터 처리 연산을 지원하도록 소스에서 추출된 연속된 요소

**구성 요소:**
- **연속된 요소**: 특정 요소 형식의 값 시퀀스
- **소스**: 컬렉션, 배열, I/O 자원 등
- **데이터 처리 연산**: filter, map, reduce 등

### 2.2 중간 연산 (Intermediate Operation)

**특징:**
- 스트림을 반환 → 체이닝 가능
- 게으르다 (Lazy) → 최종 연산 전까지 실행 안 됨
- 📝 "할 일"만 기록

**주요 중간 연산:**
```java
filter()      // 필터링
map()         // 변환
flatMap()     // 평면화
distinct()    // 중복 제거
sorted()      // 정렬
limit()       // 개수 제한
skip()        // 건너뛰기
peek()        // 엿보기 (디버깅)
```

### 2.3 최종 연산 (Terminal Operation)

**특징:**
- 결과를 반환 (스트림 아님)
- 즉시 실행 → 모든 중간 연산 실행됨
- 🎯 파이프라인을 "실행"시킴

**주요 최종 연산:**
```java
forEach()     // 각 요소 처리
collect()     // 결과 수집
count()       // 개수
reduce()      // 축약
anyMatch()    // 조건 만족 확인
findFirst()   // 첫 번째 요소
min/max()     // 최소/최대
```

### 2.4 스트림 파이프라인

**구조:**
```
소스 → 중간 연산1 → 중간 연산2 → 최종 연산 → 결과
```

**예제:**
```java
List<String> result = menu.stream()              // 소스
    .filter(d -> d.getCalories() < 400)          // 중간 연산 1
    .map(Dish::getName)                          // 중간 연산 2
    .sorted()                                    // 중간 연산 3
    .collect(Collectors.toList());               // 최종 연산
```

### 2.5 Short-circuit (쇼트서킷)

**정의:**
> 모든 요소를 처리하지 않고도 결과를 반환할 수 있는 연산

**종류:**
- 중간 연산: `limit(n)`
- 최종 연산: `anyMatch()`, `findFirst()` 등

**효과:**
- 불필요한 연산 회피
- 무한 스트림 처리 가능

### 2.6 상태 없는 연산 vs 상태 있는 연산

**상태 없는 연산 (Stateless):**
```java
filter(n -> n > 5)    // 각 요소 독립적
map(n -> n * 2)       // 각 요소 독립적
```

**상태 있는 연산 (Stateful):**
```java
distinct()    // 이전 요소 기억 필요
sorted()      // 모든 요소 필요
```

---

## 3. 외부 반복 vs 내부 반복

### 3.1 외부 반복 (External Iteration)

**정의:**
> 개발자가 명시적으로 컬렉션의 요소를 하나씩 가져와서 처리

**형태 1: for 루프**
```java
for (int i = 0; i < list.size(); i++) {
    String name = list.get(i);
    System.out.println(name);
}
```

**형태 2: for-each**
```java
for (String name : list) {
    System.out.println(name);
}
```

**형태 3: Iterator**
```java
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    String name = it.next();
    System.out.println(name);
}
```

**문제점:**
- ❌ 개발자가 "어떻게(How)" 반복할지 명시
- ❌ 병렬화 어려움
- ❌ 에러 가능성 (off-by-one 등)

### 3.2 내부 반복 (Internal Iteration)

**정의:**
> 라이브러리가 반복을 관리하고, 개발자는 각 요소에 적용할 동작만 제공

**예제:**
```java
list.stream()
    .filter(name -> name.length() > 3)
    .forEach(System.out::println);
```

**장점:**
- ✅ 개발자는 "무엇을(What)" 할지만 명시
- ✅ 자동 최적화 (루프 퓨전 등)
- ✅ 병렬화 쉬움 (`.parallelStream()`)

### 3.3 비교 예제

**시나리오: 400 칼로리 미만 & 채식 메뉴**

**외부 반복:**
```java
List<String> names = new ArrayList<>();
for (Dish dish : menu) {
    if (dish.getCalories() < 400 && dish.isVegetarian()) {
        names.add(dish.getName());
    }
}
// 5줄, 가독성 ★★☆☆☆
```

**내부 반복:**
```java
List<String> names = menu.stream()
    .filter(d -> d.getCalories() < 400)
    .filter(Dish::isVegetarian)
    .map(Dish::getName)
    .collect(toList());
// 5줄, 가독성 ★★★★★
```

### 3.4 병렬 처리 비교

**외부 반복으로 병렬화:**
```java
// 50줄 이상의 복잡한 스레드 관리 코드 필요...
ExecutorService executor = Executors.newFixedThreadPool(4);
// ... 데이터 분할
// ... Future 관리
// ... 결과 합치기
executor.shutdown();
```

**내부 반복으로 병렬화:**
```java
List<String> result = menu.parallelStream()  // 이것만 변경!
    .filter(d -> d.getCalories() < 400)
    .map(Dish::getName)
    .collect(toList());
```

---

## 4. 게으른 실행 (Lazy Evaluation)

### 4.1 핵심 원리

> **중간 연산은 최종 연산이 호출될 때까지 실행되지 않음**

**실험 1: 중간 연산만**
```java
Stream.of(1, 2, 3, 4, 5)
    .filter(n -> {
        System.out.println("filter: " + n);
        return n > 2;
    })
    .map(n -> {
        System.out.println("map: " + n);
        return n * 2;
    });

// 출력: (없음!) - 최종 연산이 없어서 실행 안 됨
```

**실험 2: 최종 연산 추가**
```java
Stream.of(1, 2, 3, 4, 5)
    .filter(n -> {
        System.out.println("filter: " + n);
        return n > 2;
    })
    .map(n -> {
        System.out.println("map: " + n);
        return n * 2;
    })
    .forEach(System.out::println);  // 최종 연산!

// 출력:
// filter: 1
// filter: 2
// filter: 3
// map: 3
// 6
// filter: 4
// map: 4
// 8
// filter: 5
// map: 5
// 10
```

### 4.2 게으른 실행의 이점

**1. 불필요한 연산 회피**
```java
// 100만 개 중 첫 번째 짝수만 찾으면 됨
Optional<Integer> first = numbers.stream()
    .filter(n -> n % 2 == 0)
    .findFirst();
// 1개만 검사하고 중단!
```

**2. 무한 스트림 처리**
```java
Stream<Integer> infinite = Stream.iterate(0, n -> n + 1);

List<Integer> first10 = infinite
    .filter(n -> n % 2 == 0)
    .limit(10)
    .collect(toList());
// 무한이지만 10개만 생성하고 중단!
```

**3. 메모리 효율**
```java
// 대용량 파일을 한 줄씩만 메모리에
Files.lines(path)
    .filter(line -> line.contains("ERROR"))
    .forEach(System.out::println);
```

**4. 루프 퓨전**
- 여러 중간 연산을 하나의 패스로 합침
- 중간 컬렉션 생성 안 함

---

## 5. 쇼트서킷 (Short-circuit)

### 5.1 정의

> **모든 요소를 처리하지 않고도 결과를 반환할 수 있는 연산**

### 5.2 쇼트서킷 연산

**중간 연산:**
- `limit(n)` - n개만 처리

**최종 연산:**
- `anyMatch()` - 하나라도 찾으면 중단
- `allMatch()` - 하나라도 실패하면 중단
- `noneMatch()` - 하나라도 찾으면 중단
- `findFirst()` - 첫 번째 찾으면 중단
- `findAny()` - 아무거나 찾으면 중단

### 5.3 성능 이점

**시나리오: 100만 개 중 첫 번째 짝수**

**쇼트서킷 없음:**
```java
List<Integer> allEven = numbers.stream()
    .filter(n -> n % 2 == 0)
    .collect(toList());
Integer first = allEven.get(0);
// 100만 번 검사!
```

**쇼트서킷 사용:**
```java
Optional<Integer> first = numbers.stream()
    .filter(n -> n % 2 == 0)
    .findFirst();
// 1번만 검사! (0이 짝수이므로)
// 속도: 1,000,000배 빠름! 🚀
```

### 5.4 각 연산 상세

**anyMatch - 하나라도?**
```java
boolean hasVegetarian = menu.stream()
    .peek(d -> System.out.println("검사: " + d.getName()))
    .anyMatch(Dish::isVegetarian);

// 출력:
// 검사: pork
// 검사: beef
// 검사: chicken
// 검사: french fries  ← 채식 발견! 중단
```

**allMatch - 모두?**
```java
boolean allLow = menu.stream()
    .peek(d -> System.out.println("검사: " + d.getName()))
    .allMatch(d -> d.getCalories() < 500);

// 출력:
// 검사: pork  ← 800칼로리, 실패! 즉시 false 반환
```

**findFirst - 첫 번째**
```java
Optional<Dish> first = menu.stream()
    .filter(Dish::isVegetarian)
    .findFirst();
// french fries 발견 후 즉시 중단
```

### 5.5 무한 스트림 예제

```java
// limit이 없으면 무한 루프!
List<Integer> first10Even = Stream.iterate(0, n -> n + 1)
    .filter(n -> n % 2 == 0)
    .limit(10)  // 쇼트서킷!
    .collect(toList());
// [0, 2, 4, 6, 8, 10, 12, 14, 16, 18]
```

---

## 6. 루프 퓨전 (Loop Fusion)

### 6.1 정의

> **여러 개의 중간 연산을 하나의 패스(pass)로 합쳐서 실행하는 최적화 기법**

### 6.2 전통적 방식 (3번의 루프)

```java
// 루프 1: 필터링
List<Dish> filtered = new ArrayList<>();
for (Dish d : menu) {
    if (d.getCalories() < 400) filtered.add(d);
}

// 루프 2: 변환
List<String> mapped = new ArrayList<>();
for (Dish d : filtered) {
    mapped.add(d.getName());
}

// 루프 3: 정렬
Collections.sort(mapped);

// 문제: 3번 순회 + 2개 중간 리스트
```

### 6.3 스트림 방식 (1번의 루프)

```java
List<String> result = menu.stream()
    .filter(d -> d.getCalories() < 400)  // \
    .map(Dish::getName)                  //  } 한 번의 패스!
    .sorted()                            // /
    .collect(toList());

// 장점: 1번 순회 + 중간 리스트 없음
```

### 6.4 실행 과정

```
"pork" → filter(❌) → (버림)
"season fruit" → filter(✅) → map → sorted → collect
"rice" → filter(✅) → map → sorted → collect

각 요소가 파이프라인 전체를 한 번에 통과!
```

### 6.5 성능 비교 (100만 개 데이터)

**전통적 방식:**
```java
// 시간: ~300ms
// 메모리: 200만 개 (원본 + 중간1 + 중간2)
```

**스트림 방식:**
```java
// 시간: ~150ms (2배 빠름!)
// 메모리: 100만 개 (원본만)
```

### 6.6 루프 퓨전의 한계

**상태 있는 연산에서는 퓨전 불가능:**

```java
stream
    .filter(n -> n > 50)
    .sorted()              // ← 여기서 끊김! (모든 요소 필요)
    .map(n -> n * 2)
    .collect(toList());
```

**실행 단계:**
1. filter 실행 → 중간 결과 생성
2. sorted 실행 → 중간 결과 정렬
3. map + collect 실행 (다시 퓨전)

**상태 없는 연산은 완벽한 퓨전:**

```java
stream
    .filter(n -> n > 50)      // 상태 없음
    .map(n -> n * 2)          // 상태 없음
    .filter(n -> n < 200)     // 상태 없음
    .collect(toList());

// 한 번의 패스로 모든 연산 수행!
```

### 6.7 쇼트서킷 + 루프 퓨전 조합

```java
Optional<Integer> result = numbers.stream()
    .filter(n -> n > 100)     // \
    .filter(n -> n % 2 == 0)  //  } 루프 퓨전
    .findFirst();             // 쇼트서킷

// 두 filter가 하나의 패스로 합쳐지고,
// findFirst로 조건 만족 시 즉시 중단!
```

---

## 💡 핵심 요약

### 스트림의 3대 최적화 기법

1. **게으른 실행 (Lazy Evaluation)**
   - 중간 연산은 최종 연산 전까지 실행 안 됨
   - 필요한 만큼만 계산

2. **쇼트서킷 (Short-circuit)**
   - 조건 만족 시 즉시 중단
   - 최대 1,000,000배 빠름

3. **루프 퓨전 (Loop Fusion)**
   - 여러 연산을 하나의 패스로 합침
   - 2배 빠름 + 메모리 절반

### 성능 최적화 원칙

1. **쇼트서킷 활용** - `anyMatch`, `findFirst`, `limit`
2. **빠른 조건을 먼저** - filter 순서 최적화
3. **상태 없는 연산 선호** - `sorted`, `distinct` 최소화
4. **기본형 스트림 사용** - `IntStream`, `LongStream`

---

**작성일:** 2024년  
**주제:** Java Stream Deep Dive  
**난이도:** 중급~고급
