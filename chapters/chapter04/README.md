# Chapter 04. 스트림 소개

<div align="center">

**"데이터 처리를 선언적으로 표현하는 강력한 API"**

> *컬렉션 데이터를 SQL 질의처럼 처리하고, 투명하게 병렬화할 수 있는 기능*

[📖 Deep Dive](advanced/deep-dive.md) | [💻 Code](code/) | [📋 CheatSheet](advanced/cheatsheet.md) | [💬 Q&A](advanced/qa-sessions.md)

</div>

---

## 🎯 학습 목표

이 챕터를 마치면 다음을 할 수 있습니다:

- [ ] **스트림이란 무엇인지** 이해하고 컬렉션과 구별한다
- [ ] **외부 반복과 내부 반복**의 차이를 설명한다
- [ ] **중간 연산과 최종 연산**을 구분하고 사용한다
- [ ] **게으른 실행**의 원리와 이점을 이해한다
- [ ] **쇼트서킷**으로 성능을 최적화한다
- [ ] **루프 퓨전**의 메커니즘을 파악한다
- [ ] **스트림 파이프라인**을 설계하고 활용한다

---

## 📚 핵심 개념

### 스트림이란?

**스트림(Stream)** 은 데이터 처리 연산을 지원하도록 소스에서 추출된 연속된 요소입니다.

```java
// Before: Java 7 (명령형)
List<String> lowCaloricDishesName = new ArrayList<>();
for (Dish dish : menu) {
    if (dish.getCalories() < 400) {
        lowCaloricDishes.add(dish);
    }
}
Collections.sort(lowCaloricDishes, ...);
for (Dish dish : lowCaloricDishes) {
    lowCaloricDishesName.add(dish.getName());
}

// After: Java 8 (선언형 - 스트림)
List<String> lowCaloricDishesName = menu.stream()
    .filter(d -> d.getCalories() < 400)
    .sorted(comparing(Dish::getCalories))
    .map(Dish::getName)
    .collect(toList());
```

### 스트림의 3가지 특징

1. **선언형 (Declarative)**: "무엇을(What)"만 명시 → 가독성 향상
2. **조립 가능 (Composable)**: 연산을 파이프라인으로 연결 → 복잡한 질의 표현
3. **병렬화 (Parallelizable)**: `.parallelStream()` 한 줄 → 멀티코어 활용

### 왜 필요한가?

Chapter 02에서 배운 **동작 파라미터화**를 더 강력하게 만들어줍니다:

```java
// Chapter 02: 익명 클래스 사용 (장황함)
List<Dish> vegetarianDishes = filter(menu, new Predicate<Dish>() {
    public boolean test(Dish dish) {
        return dish.isVegetarian();
    }
});

// Chapter 04: 스트림 사용 (간결함)
List<Dish> vegetarianDishes = menu.stream()
    .filter(Dish::isVegetarian)
    .collect(toList());
```

---

## 🔄 스트림 vs 컬렉션

### 핵심 차이점

| 구분 | 컬렉션 | 스트림 |
|------|--------|--------|
| **데이터** | 메모리에 모든 값 저장 | 요청 시 계산 |
| **계산 시점** | Eager (즉시) | Lazy (게으름) |
| **탐색** | 여러 번 가능 | **단 한 번만!** |
| **반복** | 외부 (for, iterator) | 내부 (forEach) |
| **비유** | DVD 📀 | Netflix 📺 |

### 단 한 번만 탐색 가능! ⚠️

```java
Stream<String> stream = list.stream();
stream.forEach(System.out::println);  // ✅ OK
stream.forEach(System.out::println);  // ❌ IllegalStateException!

// 해결: 매번 새로운 스트림 생성
list.stream().forEach(System.out::println);
list.stream().forEach(System.out::println);  // ✅ OK
```

**[→ StreamVsCollection.java 전체 코드 보기](code/StreamVsCollection.java)**

**[→ Deep Dive에서 상세 비교 보기](advanced/deep-dive.md#1-스트림-vs-컬렉션)**

---

## 🔁 외부 반복 vs 내부 반복

### 외부 반복 (External Iteration)

**개발자가 직접 제어 - "어떻게(How)"**

```java
List<String> names = new ArrayList<>();
for (Dish dish : menu) {              // 명시적 반복
    if (dish.getCalories() < 400) {
        names.add(dish.getName());
    }
}
```

**문제점:**
- ❌ 개발자가 반복 로직 작성
- ❌ 병렬화 어려움 (복잡한 스레드 관리 필요)
- ❌ 에러 가능성 (off-by-one 등)

### 내부 반복 (Internal Iteration)

**라이브러리가 제어 - "무엇을(What)"**

```java
List<String> names = menu.stream()
    .filter(d -> d.getCalories() < 400)
    .map(Dish::getName)
    .collect(toList());
```

**장점:**
- ✅ 선언적이고 간결
- ✅ 자동 최적화 (루프 퓨전 등)
- ✅ 병렬화 쉬움 (`.parallelStream()` 한 줄)

**[→ InternalVsExternalIteration.java 전체 코드 보기](code/InternalVsExternalIteration.java)**

**[→ Deep Dive에서 상세 비교 보기](advanced/deep-dive.md#3-외부-반복-vs-내부-반복)**

---

## ⚙️ 스트림 연산

### 중간 연산 vs 최종 연산

```java
menu.stream()                              // 스트림 생성
    .filter(d -> d.getCalories() > 300)    // 중간 연산
    .map(Dish::getName)                    // 중간 연산
    .limit(3)                              // 중간 연산
    .collect(toList());                    // 최종 연산
```

| 구분 | 중간 연산 | 최종 연산 |
|------|----------|----------|
| **반환** | `Stream<T>` | 구체적 타입 |
| **체이닝** | 가능 | 불가능 |
| **실행** | Lazy (게으름) | Eager (즉시) |
| **횟수** | 여러 번 | 한 번만 |

### 주요 중간 연산

```java
filter(Predicate)       // 조건 필터링
map(Function)           // 요소 변환
flatMap(Function)       // 스트림 평면화
distinct()              // 중복 제거
sorted()                // 정렬
limit(n)                // n개만
skip(n)                 // n개 건너뛰기
peek(Consumer)          // 엿보기 (디버깅)
```

### 주요 최종 연산

```java
forEach(Consumer)       // 각 요소 처리
collect(Collector)      // 결과 수집
reduce(BinaryOperator)  // 축약
count()                 // 개수
anyMatch(Predicate)     // 하나라도?
allMatch(Predicate)     // 모두?
findFirst()             // 첫 번째
findAny()               // 아무거나
min/max(Comparator)     // 최소/최대
```

**[→ StreamOperations.java 전체 코드 보기](code/StreamOperations.java)**

**[→ CheatSheet에서 전체 목록 보기](advanced/cheatsheet.md)**

---

## 😴 게으른 실행 (Lazy Evaluation)

### 핵심 원리

> **중간 연산은 최종 연산이 호출될 때까지 실행되지 않음**

**실험 1: 중간 연산만 있는 경우**
```java
Stream.of(1, 2, 3, 4, 5)
    .filter(n -> {
        System.out.println("filter: " + n);  // 실행 안 됨!
        return n > 2;
    })
    .map(n -> {
        System.out.println("map: " + n);     // 실행 안 됨!
        return n * 2;
    });

// 출력: (없음!) - 최종 연산이 없어서
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

### 게으른 실행의 이점

1. **불필요한 연산 회피** - 필요한 만큼만 처리
2. **무한 스트림 가능** - `Stream.iterate()` 등
3. **메모리 효율** - 중간 컬렉션 생성 안 함
4. **자동 최적화** - 루프 퓨전, 쇼트서킷

**[→ LazyEvaluationDemo.java 전체 코드 보기](code/LazyEvaluationDemo.java)**

**[→ Deep Dive에서 상세 해설 보기](advanced/deep-dive.md#4-게으른-실행-lazy-evaluation)**

---

## ⚡ 쇼트서킷 (Short-circuit)

### 정의

> **모든 요소를 처리하지 않고도 결과를 반환할 수 있는 연산**

### 쇼트서킷 연산

**중간 연산:**
- `limit(n)` - n개만 처리

**최종 연산:**
- `anyMatch()` - 하나라도 찾으면 중단
- `allMatch()` - 하나라도 실패하면 중단
- `noneMatch()` - 하나라도 찾으면 중단
- `findFirst()` - 첫 번째 찾으면 중단
- `findAny()` - 아무거나 찾으면 중단

### 성능 이점

```java
// 100만 개 중 첫 번째 짝수 찾기

// 쇼트서킷 없음 - 100만 번 검사
List<Integer> allEven = numbers.stream()
    .filter(n -> n % 2 == 0)
    .collect(toList());
Integer first = allEven.get(0);

// 쇼트서킷 사용 - 1번 검사!
Optional<Integer> first = numbers.stream()
    .filter(n -> n % 2 == 0)
    .findFirst();

// 속도: 1,000,000배 빠름! 🚀
```

**[→ ShortCircuitDemo.java 전체 코드 보기](code/ShortCircuitDemo.java)**

**[→ Deep Dive에서 상세 해설 보기](advanced/deep-dive.md#5-쇼트서킷-short-circuit)**

---

## 🔗 루프 퓨전 (Loop Fusion)

### 정의

> **여러 개의 중간 연산을 하나의 패스(pass)로 합쳐서 실행**

### 전통적 방식 (3번의 루프)

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

### 스트림 방식 (1번의 루프)

```java
List<String> result = menu.stream()
    .filter(d -> d.getCalories() < 400)  // \
    .map(Dish::getName)                  //  } 한 번의 패스!
    .sorted()                            // /
    .collect(toList());

// 장점: 1번 순회 + 중간 리스트 없음
```

### 실행 과정

```
"pork" → filter(❌) → (버림, 다음 단계 안 감)
"season fruit" → filter(✅) → map → sorted → collect
"rice" → filter(✅) → map → sorted → collect
...

각 요소가 파이프라인 전체를 한 번에 통과!
```

### 성능 비교 (100만 개 데이터)

| 방식 | 시간 | 메모리 |
|------|------|--------|
| 전통적 (3번 루프) | 300ms | 200만 개 |
| 스트림 (루프 퓨전) | 150ms | 100만 개 |

**결과: 2배 빠름 + 메모리 절반! 🚀**

**[→ LoopFusionDemo.java 전체 코드 보기](code/LoopFusionDemo.java)**

**[→ Deep Dive에서 상세 해설 보기](advanced/deep-dive.md#6-루프-퓨전-loop-fusion)**

---

## 🎓 스트림 이용 과정

스트림 사용은 3단계로 이루어집니다:

```java
menu.stream()                           // 1. 데이터 소스
    .filter(d -> d.getCalories() < 400) // 2. 중간 연산 (파이프라인)
    .map(Dish::getName)                 //    여러 개 연결 가능
    .sorted()                           //    게으르게 실행
    .collect(toList());                 // 3. 최종 연산 (실행 트리거)
```

1. **데이터 소스** - 컬렉션, 배열, I/O 등
2. **중간 연산 체인** - filter, map, sorted 등
3. **최종 연산** - collect, forEach, reduce 등

---

## 💡 실전 패턴

### 패턴 1: 조건 필터링 + 변환

```java
List<String> names = products.stream()
    .filter(p -> p.getPrice() < 100)
    .filter(Product::isInStock)
    .map(Product::getName)
    .collect(toList());
```

### 패턴 2: 그룹화

```java
Map<Category, List<Product>> grouped = products.stream()
    .collect(groupingBy(Product::getCategory));
```

### 패턴 3: 통계 계산

```java
IntSummaryStatistics stats = products.stream()
    .mapToInt(Product::getPrice)
    .summaryStatistics();

System.out.println("평균: " + stats.getAverage());
System.out.println("최대: " + stats.getMax());
```

### 패턴 4: 병렬 처리

```java
// 순차 스트림
long count = bigList.stream()
    .filter(condition)
    .count();

// 병렬 스트림 - 코드 한 줄만 변경!
long count = bigList.parallelStream()
    .filter(condition)
    .count();
```

**[→ Deep Dive에서 더 많은 패턴 보기](advanced/deep-dive.md)**

---

## ⚠️ 주의사항

### 1. 스트림 재사용 불가

```java
// ❌ 잘못된 사용
Stream<String> stream = list.stream();
stream.forEach(System.out::println);
stream.forEach(System.out::println);  // IllegalStateException!

// ✅ 올바른 사용
list.stream().forEach(System.out::println);
list.stream().forEach(System.out::println);
```

### 2. 최종 연산 없으면 실행 안 됨

```java
// ❌ 실행 안 됨
menu.stream()
    .filter(d -> d.getCalories() > 300)
    .map(Dish::getName);  // 최종 연산 없음!

// ✅ 실행됨
menu.stream()
    .filter(d -> d.getCalories() > 300)
    .map(Dish::getName)
    .collect(toList());  // 최종 연산!
```

### 3. peek은 디버깅용

```java
// ❌ peek을 최종 연산처럼 사용 (잘못됨)
stream.peek(System.out::println);  // 실행 안 됨!

// ✅ peek은 중간 연산 (디버깅용)
stream
    .peek(System.out::println)
    .collect(toList());
```

### 4. 부작용(Side Effect) 주의

```java
// ❌ 나쁜 예: 외부 상태 변경
List<Integer> collected = new ArrayList<>();
stream.filter(n -> {
    collected.add(n);  // 부작용!
    return n > 2;
});

// ✅ 좋은 예: 최종 연산에서 수집
List<Integer> collected = stream
    .filter(n -> n > 2)
    .collect(toList());
```

**[→ Q&A에서 자주 하는 실수 보기](advanced/qa-sessions.md#q16-자주-하는-실수는)**

---

## 📊 Quick Reference

### 언제 스트림을 사용할까?

**✅ 사용해야 할 때:**
- 복잡한 데이터 처리 파이프라인
- 병렬 처리가 필요한 경우
- 코드 가독성이 중요한 경우
- 대용량 데이터 처리
- 함수형 스타일 선호

**❌ 사용하지 말아야 할 때:**
- 간단한 반복 (3줄 이하)
- 중간에 break/continue 필요
- 반복 중 외부 상태 변경 필요
- 성능이 극도로 중요한 작은 데이터

### 성능 최적화 팁

1. **쇼트서킷 활용** - `anyMatch`, `findFirst`, `limit`
2. **기본형 스트림 사용** - `IntStream`, `LongStream`, `DoubleStream`
3. **병렬 스트림 신중히** - 소량 데이터에서는 오히려 느림
4. **상태 없는 연산 선호** - `sorted`, `distinct` 최소화

**[→ CheatSheet에서 전체 참조 보기](advanced/cheatsheet.md)**

---

## 📁 학습 자료 구조

```
chapter04/
├── README.md (현재 문서)
├── code/
│   ├── Dish.java                        # 도메인 클래스
│   ├── StreamBasic.java                 # Java 7 vs 8 비교
│   ├── StreamVsCollection.java          # 스트림 vs 컬렉션
│   ├── InternalVsExternalIteration.java # 내부/외부 반복
│   ├── LazyEvaluationDemo.java          # 게으른 실행
│   ├── ShortCircuitDemo.java            # 쇼트서킷
│   ├── LoopFusionDemo.java              # 루프 퓨전
│   └── StreamOperations.java            # 연산 종합
└── advanced/
    ├── deep-dive.md                     # 심화 학습 (6개 주제)
    ├── cheatsheet.md                    # 빠른 참조 가이드
    └── qa-sessions.md                   # Q&A (20개 질문)
```

---

## 📚 학습 체크리스트

스트림을 제대로 이해했는지 확인해보세요:

- [ ] 스트림과 컬렉션의 차이를 3가지 이상 설명할 수 있다
- [ ] 외부 반복과 내부 반복의 장단점을 설명할 수 있다
- [ ] 중간 연산과 최종 연산을 구분할 수 있다
- [ ] 게으른 실행이 왜 유용한지 설명할 수 있다
- [ ] 쇼트서킷이 무엇이고 어떤 연산이 있는지 안다
- [ ] 루프 퓨전이 성능을 어떻게 향상시키는지 설명할 수 있다
- [ ] 스트림 파이프라인을 직접 설계할 수 있다
- [ ] 병렬 스트림을 언제 사용해야 하는지 안다

---

## 🔗 더 알아보기

- [Chapter 05: 스트림 활용](../chapter05/) - 필터링, 슬라이싱, 매핑, 검색
- [Chapter 06: 스트림으로 데이터 수집](../chapter06/) - Collectors 활용
- [Chapter 07: 병렬 데이터 처리와 성능](../chapter07/) - 병렬 스트림 심화

---

## 🎯 Key Takeaway

**스트림의 핵심 3가지:**

1. **선언형 코드** - "무엇을(What)" 할지만 명시
2. **자동 최적화** - 게으름 + 쇼트서킷 + 루프 퓨전
3. **쉬운 병렬화** - `.parallelStream()` 한 줄

```java
// 이 한 줄이
menu.parallelStream()
    .filter(d -> d.getCalories() < 400)
    .map(Dish::getName)
    .collect(toList());

// 수십 줄의 병렬 처리 코드를 대체합니다!
```

---

**작성일:** 2024년  
**주제:** Java Stream API 소개  
**난이도:** 중급
