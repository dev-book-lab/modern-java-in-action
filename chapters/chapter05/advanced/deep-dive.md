# Chapter 05 Deep Dive 🔬

> 스트림 활용의 모든 것을 깊이 있게

---

## 📚 목차

1. [필터링과 슬라이싱](#1-필터링과-슬라이싱)
2. [매핑과 평면화](#2-매핑과-평면화)
3. [검색과 매칭](#3-검색과-매칭)
4. [리듀싱](#4-리듀싱)
5. [기본형 특화 스트림](#5-기본형-특화-스트림)
6. [스트림 생성](#6-스트림-생성)
7. [무한 스트림](#7-무한-스트림)
8. [지연 평가 (Lazy Evaluation)](#8-지연-평가-lazy-evaluation)
9. [실전 쿼리 패턴](#9-실전-쿼리-패턴)

---

## 1. 필터링과 슬라이싱

### 1.1 filter - 프레디케이트 필터링

**동작 원리:**
```
입력: [1, 2, 3, 4, 5]
조건: n % 2 == 0

각 요소 검사:
1 → false → 제외
2 → true → 포함
3 → false → 제외
4 → true → 포함
5 → false → 제외

출력: [2, 4]
```

**예제:**
```java
List<Dish> vegetarianMenu = menu.stream()
    .filter(Dish::isVegetarian)
    .collect(toList());
```

### 1.2 distinct - 고유 요소

`hashCode`와 `equals`로 중복 판단:

```java
List<Integer> numbers = Arrays.asList(1, 2, 1, 3, 3, 2, 4);
numbers.stream()
    .distinct()
    .forEach(System.out::println);
// 출력: 1, 2, 3, 4
```

### 1.3 takeWhile vs dropWhile (Java 9+)

**takeWhile - 조건이 false가 될 때까지:**
```java
// 정렬된 리스트: [120, 300, 350, 400, 530]
specialMenu.stream()
    .takeWhile(dish -> dish.getCalories() < 320)
    .collect(toList());
// 결과: [120, 300] - 350에서 중단!
```

**dropWhile - 조건이 false가 될 때부터:**
```java
specialMenu.stream()
    .dropWhile(dish -> dish.getCalories() < 320)
    .collect(toList());
// 결과: [350, 400, 530]
```

**filter와의 차이:**
```java
// filter - 전체 검사
menu.stream()
    .filter(dish -> dish.getCalories() < 320)
    // 모든 요소를 다 확인!

// takeWhile - 조기 종료
menu.stream()
    .takeWhile(dish -> dish.getCalories() < 320)
    // 조건 false 나오면 즉시 중단!
```

### 1.4 limit과 skip

**limit - 스트림 축소:**
```java
menu.stream()
    .filter(d -> d.getCalories() > 300)
    .limit(3)
    .collect(toList());
// 첫 3개만
```

**skip - 요소 건너뛰기:**
```java
menu.stream()
    .filter(d -> d.getCalories() > 300)
    .skip(2)
    .collect(toList());
// 처음 2개 제외
```

---

## 2. 매핑과 평면화

### 2.1 map - 각 요소에 함수 적용

**1:1 변환:**
```java
// 요리명 추출
List<String> dishNames = menu.stream()
    .map(Dish::getName)
    .collect(toList());

// 문자열 길이
List<Integer> wordLengths = words.stream()
    .map(String::length)
    .collect(toList());
```

### 2.2 flatMap - 스트림 평면화

**문제 상황:**
```java
// ❌ Stream<String[]> 반환!
words.stream()
    .map(word -> word.split(""))
    .distinct()
    .collect(toList());
```

**해결:**
```java
// ✅ Stream<String> 반환
List<String> uniqueCharacters = words.stream()
    .map(word -> word.split(""))
    .flatMap(Arrays::stream)  // 각 배열을 스트림으로, 하나로 합침!
    .distinct()
    .collect(toList());
```

**동작 과정:**
```
["Hello", "World"]
    ↓ map(split)
[["H","e","l","l","o"], ["W","o","r","l","d"]]
    ↓ flatMap(Arrays::stream)
["H","e","l","l","o","W","o","r","l","d"]
    ↓ distinct
["H","e","l","o","W","r","d"]
```

### 2.3 flatMap 실전 예제

**숫자쌍 생성:**
```java
List<Integer> numbers1 = Arrays.asList(1, 2, 3);
List<Integer> numbers2 = Arrays.asList(3, 4);

List<int[]> pairs = numbers1.stream()
    .flatMap(i -> 
        numbers2.stream()
            .map(j -> new int[]{i, j})
    )
    .collect(toList());
// [(1,3), (1,4), (2,3), (2,4), (3,3), (3,4)]
```

---

## 3. 검색과 매칭

### 3.1 쇼트서킷의 이해

**쇼트서킷 연산:**
- 전체를 처리하지 않고 결과 반환
- `&&`, `||` 연산과 동일한 원리

**예제:**
```java
// anyMatch - 하나라도 true면 즉시 종료
boolean hasVegetarian = menu.stream()
    .anyMatch(Dish::isVegetarian);
// 채식 요리 발견하면 더 이상 검사 안 함!
```

### 3.2 anyMatch vs allMatch vs noneMatch

| 연산 | 조건 | 빈 스트림 |
|------|------|----------|
| `anyMatch` | 하나라도 true | false |
| `allMatch` | 모두 true | **true** |
| `noneMatch` | 모두 false | true |

**관계식:**
```java
anyMatch(p) == !noneMatch(p)
allMatch(p) == noneMatch(!p)
```

### 3.3 findAny vs findFirst

**findAny - 임의의 요소:**
```java
Optional<Dish> dish = menu.stream()
    .filter(Dish::isVegetarian)
    .findAny();
```

**findFirst - 첫 번째 요소:**
```java
Optional<Integer> firstSquare = numbers.stream()
    .map(n -> n * n)
    .filter(n -> n % 3 == 0)
    .findFirst();
```

**언제 뭘 쓸까?**
- **순서 중요** → `findFirst`
- **순서 무관 + 병렬** → `findAny` (더 빠름)

---

## 4. 리듀싱

### 4.1 reduce의 동작 원리

**과정:**
```
numbers = [1, 2, 3, 4, 5]
reduce(0, Integer::sum)

단계별:
0 + 1 = 1   (초기값 + 첫 요소)
1 + 2 = 3   (누적값 + 다음 요소)
3 + 3 = 6
6 + 4 = 10
10 + 5 = 15  (최종 결과)
```

### 4.2 reduce 형태

**1. 초기값 있음:**
```java
int sum = numbers.stream()
    .reduce(0, Integer::sum);
// 반환: int
// 빈 스트림: 초기값 반환
```

**2. 초기값 없음:**
```java
Optional<Integer> sum = numbers.stream()
    .reduce(Integer::sum);
// 반환: Optional<Integer>
// 빈 스트림: Optional.empty()
```

### 4.3 실전 예제

**합계:**
```java
int sum = numbers.stream()
    .reduce(0, (a, b) -> a + b);
// 또는
int sum = numbers.stream()
    .reduce(0, Integer::sum);
```

**최대/최소:**
```java
Optional<Integer> max = numbers.stream()
    .reduce(Integer::max);

Optional<Integer> min = numbers.stream()
    .reduce(Integer::min);
```

**문자열 결합:**
```java
String result = words.stream()
    .reduce("", (a, b) -> a + b);
```

---

## 5. 기본형 특화 스트림

### 5.1 박싱 비용 문제

**문제:**
```java
int calories = menu.stream()
    .map(Dish::getCalories)  // Stream<Integer>
    .reduce(0, Integer::sum);
// 내부적으로 Integer → int 언박싱 반복!
```

**해결:**
```java
int calories = menu.stream()
    .mapToInt(Dish::getCalories)  // IntStream
    .sum();
// 박싱 없음!
```

### 5.2 기본형 스트림 종류

- `IntStream` - int 전용
- `LongStream` - long 전용
- `DoubleStream` - double 전용

**전용 메서드:**
```java
sum()               // 합계
average()           // 평균 (OptionalDouble)
max(), min()        // 최대/최소
summaryStatistics() // 통계 (개수, 합, 평균, 최대, 최소)
```

### 5.3 변환

**기본형으로:**
```java
IntStream intStream = menu.stream()
    .mapToInt(Dish::getCalories);
```

**객체로 복원:**
```java
Stream<Integer> stream = intStream.boxed();
```

**기본형 간:**
```java
IntStream → LongStream:   asLongStream()
IntStream → DoubleStream: asDoubleStream()
```

### 5.4 숫자 범위

```java
// range - 끝 제외
IntStream.range(1, 100)  // [1, 100)

// rangeClosed - 끝 포함
IntStream.rangeClosed(1, 100)  // [1, 100]
```

---

## 6. 스트림 생성

### 6.1 값으로 생성

```java
// Stream.of
Stream<String> stream = Stream.of("A", "B", "C");

// Stream.empty
Stream<String> empty = Stream.empty();

// Stream.ofNullable (Java 9+)
Stream<String> stream = Stream.ofNullable(value);
```

### 6.2 배열로 생성

```java
int[] numbers = {2, 3, 5, 7, 11, 13};
IntStream stream = Arrays.stream(numbers);

// 범위 지정
IntStream stream = Arrays.stream(numbers, 1, 4);
// numbers[1]부터 numbers[3]까지
```

### 6.3 파일로 생성

```java
try (Stream<String> lines = Files.lines(Paths.get("data.txt"))) {
    long uniqueWords = lines
        .flatMap(line -> Arrays.stream(line.split(" ")))
        .distinct()
        .count();
}
// try-with-resources로 자동 close!
```

---

## 7. 무한 스트림

### 7.1 iterate - 순차 생성

**기본 형태:**
```java
Stream.iterate(0, n -> n + 1)
    .limit(10)
    .forEach(System.out::println);
// 0, 1, 2, 3, 4, 5, 6, 7, 8, 9
```

**Java 9+ (종료 조건):**
```java
Stream.iterate(0, n -> n < 100, n -> n + 1)
    .forEach(System.out::println);
```

**피보나치 수열:**
```java
Stream.iterate(new int[]{0, 1},
        t -> new int[]{t[1], t[0] + t[1]})
    .limit(10)
    .map(t -> t[0])
    .forEach(System.out::println);
```

### 7.2 generate - 독립 생성

**랜덤 값:**
```java
Stream.generate(Math::random)
    .limit(5)
    .forEach(System.out::println);
```

**고정 값:**
```java
Stream.generate(() -> "Hello")
    .limit(3)
    .forEach(System.out::println);
// Hello, Hello, Hello
```

### 7.3 iterate vs generate

| 특성 | iterate | generate |
|------|---------|----------|
| **의존성** | 이전 값 기반 | 독립적 |
| **순차성** | 순차적 | 무관 |
| **용도** | 수열, 시퀀스 | 랜덤, 독립 값 |

### 7.4 무한 스트림 주의사항

**1. limit 필수:**
```java
// ❌ 무한 루프!
Stream.iterate(0, n -> n + 1)
    .forEach(System.out::println);

// ✅ 제한
Stream.iterate(0, n -> n + 1)
    .limit(10)
    .forEach(System.out::println);
```

**2. sorted 사용 불가:**
```java
// ❌ 무한 루프 (전체 정렬 시도)
Stream.iterate(0, n -> n + 1)
    .sorted()
    .limit(10);

// ✅ limit 먼저
Stream.iterate(0, n -> n + 1)
    .limit(10)
    .sorted();
```

---

## 8. 지연 평가 (Lazy Evaluation)

### 8.1 지연 평가란?

**핵심 개념:**
- 스트림의 중간 연산은 **즉시 실행되지 않음**
- 최종 연산이 호출될 때까지 **기록만** 함
- 최종 연산 시 **한 번에 실행**

**장점:**
1. **성능 최적화** - 필요한 만큼만 처리
2. **쇼트서킷** - 조건 만족하면 즉시 종료
3. **파이프라인 최적화** - 중간 연산 병합 가능

### 8.2 동작 방식

**예제:**
```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);

List<Integer> result = numbers.stream()
    .filter(n -> {
        System.out.println("filtering " + n);
        return n % 2 == 0;
    })
    .map(n -> {
        System.out.println("mapping " + n);
        return n * n;
    })
    .limit(2)
    .collect(toList());
```

**출력 (지연 평가):**
```
filtering 1
filtering 2
mapping 2        ← 2는 통과, 즉시 map
filtering 3
filtering 4
mapping 4        ← 4는 통과, 즉시 map
                 ← limit(2) 달성, 즉시 종료!
```

**만약 즉시 평가라면:**
```
filtering 1
filtering 2
filtering 3
filtering 4
filtering 5
filtering 6
filtering 7
filtering 8      ← 전체 filter 먼저
mapping 2
mapping 4
mapping 6
mapping 8        ← 전체 map 후
                 ← 마지막에 limit
```

### 8.3 지연 평가 vs 즉시 평가

| 특성 | 지연 평가 (Stream) | 즉시 평가 (Collection) |
|------|-------------------|----------------------|
| **실행 시점** | 최종 연산 시 | 즉시 |
| **처리 방식** | 요소별 파이프라인 | 단계별 전체 처리 |
| **메모리** | 효율적 | 중간 결과 저장 |
| **쇼트서킷** | 가능 | 불가능 |

### 8.4 실전 예제

**최종 연산 없으면 실행 안 됨:**
```java
Stream<String> stream = list.stream()
    .filter(s -> {
        System.out.println("filter: " + s);
        return s.length() > 3;
    });
// 아무것도 출력 안 됨!

stream.collect(toList());
// 이제 출력 시작!
```

**쇼트서킷과 지연 평가:**
```java
list.stream()
    .filter(expensiveOperation)
    .findFirst();
// 첫 번째 요소 찾으면 즉시 종료!
// 나머지 요소는 처리하지 않음
```

### 8.5 지연 평가의 이점

**1. 성능 최적화:**
```java
// 100만 개 중 3개만 필요
bigList.stream()
    .filter(...)
    .limit(3)
    .collect(toList());
// 3개 찾으면 즉시 종료! (지연 평가)
```

**2. 무한 스트림 처리:**
```java
Stream.iterate(0, n -> n + 1)
    .filter(n -> n % 2 == 0)
    .limit(10)
    .collect(toList());
// 무한 스트림이지만 limit으로 제한 가능!
```

**3. 파이프라인 최적화:**
```java
// 컴파일러가 최적화 가능
stream
    .filter(a)
    .filter(b)
    .map(c)
// → filter(a && b).map(c) 로 병합 가능
```

---

## 9. 실전 쿼리 패턴

### 9.1 데이터 모델

**Trader (거래자):**
```java
class Trader {
    String name;
    String city;
}
```

**Transaction (거래):**
```java
class Transaction {
    Trader trader;
    int year;
    int value;
}
```

### 9.2 8가지 실전 쿼리

**질의 1: 특정 연도 거래 정렬**
```java
// 2011년 거래를 값 기준 오름차순 정렬
List<Transaction> tr2011 = transactions.stream()
    .filter(t -> t.getYear() == 2011)
    .sorted(comparing(Transaction::getValue))
    .collect(toList());
```

**질의 2: 고유 도시 추출**
```java
// 거래자가 근무하는 모든 고유 도시
List<String> cities = transactions.stream()
    .map(t -> t.getTrader().getCity())
    .distinct()
    .collect(toList());
```

**질의 3: 특정 도시 거래자 정렬**
```java
// Cambridge의 거래자를 이름순 정렬
List<Trader> traders = transactions.stream()
    .map(Transaction::getTrader)
    .filter(trader -> trader.getCity().equals("Cambridge"))
    .distinct()
    .sorted(comparing(Trader::getName))
    .collect(toList());
```

**질의 4: 모든 거래자 이름 정렬**
```java
// 알파벳순 정렬된 거래자 이름 문자열
String traderStr = transactions.stream()
    .map(t -> t.getTrader().getName())
    .distinct()
    .sorted()
    .reduce("", (n1, n2) -> n1 + n2);
```

**질의 5: 특정 도시 거래자 존재 확인**
```java
// Milan 거주 거래자가 있는가?
boolean milanBased = transactions.stream()
    .anyMatch(t -> t.getTrader().getCity().equals("Milan"));
```

**질의 6: 특정 도시 거래 값 출력**
```java
// Cambridge 거래자의 모든 거래 값
transactions.stream()
    .filter(t -> "Cambridge".equals(t.getTrader().getCity()))
    .map(Transaction::getValue)
    .forEach(System.out::println);
```

**질의 7: 최고값 찾기**
```java
// 모든 거래의 최고값
int highestValue = transactions.stream()
    .map(Transaction::getValue)
    .reduce(0, Integer::max);
```

**질의 8: 최소값 거래 찾기**
```java
// 가장 작은 값을 가진 거래
Optional<Transaction> smallest = transactions.stream()
    .min(comparing(Transaction::getValue));

smallest
    .map(String::valueOf)
    .orElse("No transactions found");
```

### 9.3 패턴 분석

**패턴 1: 필터 + 정렬 + 수집**
```java
stream.filter(...).sorted(...).collect(toList());
```

**패턴 2: 매핑 + 중복 제거**
```java
stream.map(...).distinct().collect(toList());
```

**패턴 3: 조건 검사**
```java
stream.anyMatch(...)  // boolean 반환
```

**패턴 4: 집계 연산**
```java
stream.map(...).reduce(0, Integer::max)
stream.min(comparing(...))
```

---

## 💡 핵심 정리

### 필터링과 슬라이싱
- `filter` - 조건 필터
- `distinct` - 중복 제거
- `takeWhile/dropWhile` - 정렬된 데이터 슬라이싱
- `limit/skip` - 개수 제한

### 매핑
- `map` - 1:1 변환
- `flatMap` - 평면화

### 검색과 매칭
- `anyMatch/allMatch/noneMatch` - 조건 검사
- `findAny/findFirst` - 요소 찾기
- 모두 쇼트서킷 연산!

### 리듀싱
- `reduce(초기값, f)` - T 반환
- `reduce(f)` - Optional<T> 반환

### 기본형 스트림
- 박싱 비용 제거
- 전용 메서드 (sum, average, max, min)
- range/rangeClosed

### 스트림 생성
- 값: Stream.of
- 배열: Arrays.stream
- 파일: Files.lines
- 무한: iterate, generate

### 지연 평가
- 중간 연산은 기록만
- 최종 연산 시 실행
- 쇼트서킷 최적화

---

**마지막 업데이트**: 2024년 12월
