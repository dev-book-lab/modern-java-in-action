# Chapter 04 스트림 빠른 참조 가이드 📋

## 목차
- [스트림 생성](#스트림-생성)
- [중간 연산](#중간-연산)
- [최종 연산](#최종-연산)
- [쇼트서킷 연산](#쇼트서킷-연산)
- [실전 패턴](#실전-패턴)
- [성능 최적화](#성능-최적화)

---

## 스트림 생성

```java
// 컬렉션에서
List<String> list = Arrays.asList("a", "b", "c");
Stream<String> stream = list.stream();

// 배열에서
String[] array = {"a", "b", "c"};
Stream<String> stream = Arrays.stream(array);

// 값에서
Stream<String> stream = Stream.of("a", "b", "c");

// 빈 스트림
Stream<String> empty = Stream.empty();

// 무한 스트림
Stream<Integer> infinite = Stream.iterate(0, n -> n + 1);
Stream<Double> random = Stream.generate(Math::random);

// 파일에서
try (Stream<String> lines = Files.lines(Paths.get("file.txt"))) {
    // 처리
}
```

---

## 중간 연산

### filter - 조건 필터링

```java
// 짝수만
stream.filter(n -> n % 2 == 0)

// 양수만
stream.filter(n -> n > 0)

// null 아닌 것만
stream.filter(Objects::nonNull)

// 여러 조건 (AND)
stream
    .filter(d -> d.getCalories() < 400)
    .filter(Dish::isVegetarian)

// 여러 조건 (OR)
stream.filter(d -> d.getCalories() < 400 || d.isVegetarian())
```

### map - 요소 변환

```java
// 이름만 추출
stream.map(Dish::getName)

// 대문자로 변환
stream.map(String::toUpperCase)

// 길이로 변환
stream.map(String::length)

// 객체 생성
stream.map(name -> new User(name))
```

### flatMap - 스트림 평면화

```java
// 단어를 문자로
List<String> words = Arrays.asList("Hello", "World");
words.stream()
     .flatMap(word -> Arrays.stream(word.split("")))
     .distinct()
     // ["H", "e", "l", "o", "W", "r", "d"]

// 리스트의 리스트를 평면화
List<List<Integer>> nested = Arrays.asList(
    Arrays.asList(1, 2),
    Arrays.asList(3, 4)
);
nested.stream()
      .flatMap(List::stream)
      // [1, 2, 3, 4]
```

### distinct - 중복 제거

```java
// 중복 제거 (equals 기준)
stream.distinct()

// 특정 속성 기준 중복 제거
stream.filter(distinctByKey(User::getName))

private static <T> Predicate<T> distinctByKey(
    Function<? super T, ?> keyExtractor) {
    Set<Object> seen = ConcurrentHashMap.newKeySet();
    return t -> seen.add(keyExtractor.apply(t));
}
```

### sorted - 정렬

```java
// 자연 순서
stream.sorted()

// 역순
stream.sorted(Comparator.reverseOrder())

// 속성 기준
stream.sorted(Comparator.comparing(Dish::getCalories))

// 역순 속성 기준
stream.sorted(Comparator.comparing(Dish::getCalories).reversed())

// 다중 조건
stream.sorted(
    Comparator.comparing(Dish::getType)
              .thenComparing(Dish::getCalories)
)
```

### limit / skip

```java
// 처음 10개
stream.limit(10)

// 처음 5개 건너뛰기
stream.skip(5)

// 페이징 (2페이지, 페이지당 10개)
int page = 2;
int pageSize = 10;
stream.skip((page - 1) * pageSize)
      .limit(pageSize)
```

### peek - 엿보기 (디버깅)

```java
// 디버깅
stream
    .peek(d -> System.out.println("Original: " + d))
    .filter(d -> d.getCalories() < 400)
    .peek(d -> System.out.println("Filtered: " + d))
    .map(Dish::getName)
    .peek(name -> System.out.println("Mapped: " + name))
    .collect(toList());

// 로깅
stream.peek(user -> logger.info("Processing: " + user))
```

---

## 최종 연산

### forEach - 각 요소 처리

```java
// 출력
stream.forEach(System.out::println)

// 순서 보장 (병렬에서도)
stream.forEachOrdered(System.out::println)

// 부작용 주의!
// ❌ 나쁜 예
List<String> result = new ArrayList<>();
stream.forEach(result::add);  // 스레드 안전하지 않음

// ✅ 좋은 예
List<String> result = stream.collect(toList());
```

### collect - 결과 수집

```java
// 리스트로
List<String> list = stream.collect(toList());

// 셋으로
Set<String> set = stream.collect(toSet());

// 맵으로
Map<String, Integer> map = stream.collect(
    toMap(User::getName, User::getAge)
);

// 문자열로 합치기
String joined = stream.collect(joining(", "));

// 그룹화
Map<Type, List<Dish>> grouped = stream.collect(
    groupingBy(Dish::getType)
);

// 파티셔닝
Map<Boolean, List<Dish>> partitioned = stream.collect(
    partitioningBy(Dish::isVegetarian)
);
```

### reduce - 축약

```java
// 합계
int sum = stream.reduce(0, (a, b) -> a + b);
int sum = stream.reduce(0, Integer::sum);

// 최대값
Optional<Integer> max = stream.reduce(Integer::max);

// 최소값
Optional<Integer> min = stream.reduce(Integer::min);

// 문자열 연결
String concat = stream.reduce("", (a, b) -> a + b);

// 곱셈
int product = stream.reduce(1, (a, b) -> a * b);
```

### count - 개수

```java
// 개수 세기
long count = stream.count();

// 조건 만족 개수
long count = stream.filter(d -> d.getCalories() < 400).count();
```

### anyMatch / allMatch / noneMatch

```java
// 하나라도 만족?
boolean any = stream.anyMatch(d -> d.getCalories() > 300);

// 모두 만족?
boolean all = stream.allMatch(d -> d.getCalories() < 1000);

// 하나도 만족 안 함?
boolean none = stream.noneMatch(d -> d.getCalories() > 1000);
```

### findFirst / findAny

```java
// 첫 번째 요소
Optional<Dish> first = stream.findFirst();

// 아무 요소 (병렬에서 더 빠름)
Optional<Dish> any = stream.findAny();

// 조건을 만족하는 첫 번째
Optional<Dish> firstVegetarian = stream
    .filter(Dish::isVegetarian)
    .findFirst();
```

### min / max

```java
// 최소값
Optional<Dish> min = stream.min(
    Comparator.comparing(Dish::getCalories)
);

// 최대값
Optional<Dish> max = stream.max(
    Comparator.comparing(Dish::getCalories)
);
```

---

## 쇼트서킷 연산

### Short-circuit 중간 연산

```java
// limit - n개만
stream.limit(10)

// takeWhile (Java 9+) - 조건 만족하는 동안
stream.takeWhile(d -> d.getCalories() < 400)

// dropWhile (Java 9+) - 조건 만족하는 동안 버리기
stream.dropWhile(d -> d.getCalories() < 400)
```

### Short-circuit 최종 연산

```java
// anyMatch - 하나라도 찾으면 중단
boolean exists = stream.anyMatch(condition);

// allMatch - 하나라도 실패하면 중단
boolean all = stream.allMatch(condition);

// noneMatch - 하나라도 찾으면 중단
boolean none = stream.noneMatch(condition);

// findFirst - 첫 번째 찾으면 중단
Optional<T> first = stream.findFirst();

// findAny - 아무거나 찾으면 중단
Optional<T> any = stream.findAny();
```

---

## 실전 패턴

### 패턴 1: 필터링 + 변환

```java
// 가격 100 미만, 재고 있는 상품 이름
List<String> names = products.stream()
    .filter(p -> p.getPrice() < 100)
    .filter(Product::isInStock)
    .map(Product::getName)
    .collect(toList());
```

### 패턴 2: 그룹화

```java
// 타입별 그룹화
Map<Type, List<Dish>> byType = menu.stream()
    .collect(groupingBy(Dish::getType));

// 타입별 개수
Map<Type, Long> countByType = menu.stream()
    .collect(groupingBy(Dish::getType, counting()));

// 타입별 평균 칼로리
Map<Type, Double> avgCalories = menu.stream()
    .collect(groupingBy(
        Dish::getType,
        averagingInt(Dish::getCalories)
    ));
```

### 패턴 3: 통계

```java
// 기본 통계
IntSummaryStatistics stats = menu.stream()
    .mapToInt(Dish::getCalories)
    .summaryStatistics();

System.out.println("개수: " + stats.getCount());
System.out.println("합계: " + stats.getSum());
System.out.println("평균: " + stats.getAverage());
System.out.println("최소: " + stats.getMin());
System.out.println("최대: " + stats.getMax());
```

### 패턴 4: Top N

```java
// 칼로리 높은 순 Top 3
List<Dish> top3 = menu.stream()
    .sorted(Comparator.comparing(Dish::getCalories).reversed())
    .limit(3)
    .collect(toList());
```

### 패턴 5: 파티셔닝

```java
// 채식/비채식 분할
Map<Boolean, List<Dish>> partitioned = menu.stream()
    .collect(partitioningBy(Dish::isVegetarian));

List<Dish> vegetarian = partitioned.get(true);
List<Dish> meat = partitioned.get(false);
```

### 패턴 6: 조인

```java
// 문자열로 합치기
String names = menu.stream()
    .map(Dish::getName)
    .collect(joining(", "));
// "pork, beef, chicken, ..."

// 접두사/접미사 추가
String names = menu.stream()
    .map(Dish::getName)
    .collect(joining(", ", "[", "]"));
// "[pork, beef, chicken, ...]"
```

### 패턴 7: flatMap 활용

```java
// 모든 단어의 고유 문자
List<String> uniqueChars = words.stream()
    .flatMap(word -> Arrays.stream(word.split("")))
    .distinct()
    .collect(toList());

// 두 리스트의 모든 조합
List<Integer> numbers1 = Arrays.asList(1, 2, 3);
List<Integer> numbers2 = Arrays.asList(3, 4);

List<int[]> pairs = numbers1.stream()
    .flatMap(i -> numbers2.stream()
                          .map(j -> new int[]{i, j}))
    .collect(toList());
// [(1,3), (1,4), (2,3), (2,4), (3,3), (3,4)]
```

---

## 성능 최적화

### 1. 기본형 스트림 사용

```java
// ❌ 박싱/언박싱 오버헤드
int sum = list.stream()
    .map(Integer::intValue)
    .reduce(0, Integer::sum);

// ✅ 기본형 스트림
int sum = list.stream()
    .mapToInt(Integer::intValue)
    .sum();

// IntStream, LongStream, DoubleStream
IntStream intStream = IntStream.range(1, 100);
LongStream longStream = LongStream.rangeClosed(1, 100);
DoubleStream doubleStream = DoubleStream.of(1.0, 2.0, 3.0);
```

### 2. 병렬 스트림

```java
// 순차 스트림
long count = list.stream()
    .filter(condition)
    .count();

// 병렬 스트림
long count = list.parallelStream()
    .filter(condition)
    .count();

// 병렬 처리 주의사항
// - 소량 데이터: 오버헤드로 더 느림
// - 상태 변경: 스레드 안전성 문제
// - 순서 중요: forEachOrdered 사용
```

### 3. 쇼트서킷 활용

```java
// ❌ 비효율
boolean exists = list.stream()
    .filter(condition)
    .collect(toList())
    .size() > 0;

// ✅ 효율
boolean exists = list.stream()
    .anyMatch(condition);
```

### 4. 조건 순서 최적화

```java
// ❌ 비효율 (느린 조건 먼저)
stream
    .filter(expensiveCheck)  // 느림
    .filter(cheapCheck)      // 빠름

// ✅ 효율 (빠른 조건 먼저)
stream
    .filter(cheapCheck)      // 빠름 (대부분 거름)
    .filter(expensiveCheck)  // 느림 (적은 양만)
```

### 5. 상태 없는 연산 선호

```java
// ✅ 상태 없음 (완벽한 루프 퓨전)
stream
    .filter(condition)
    .map(transformation)
    .collect(toList());

// ⚠️ 상태 있음 (퓨전 제한)
stream
    .filter(condition)
    .sorted()           // 모든 요소 필요
    .map(transformation)
    .collect(toList());
```

---

## 자주 하는 실수

### 1. 스트림 재사용

```java
// ❌ 에러!
Stream<String> stream = list.stream();
stream.forEach(System.out::println);
stream.forEach(System.out::println);  // IllegalStateException!

// ✅ 매번 새로 생성
list.stream().forEach(System.out::println);
list.stream().forEach(System.out::println);
```

### 2. 최종 연산 누락

```java
// ❌ 실행 안 됨
list.stream()
    .filter(condition)
    .map(transformation);  // 최종 연산 없음!

// ✅ 최종 연산 추가
list.stream()
    .filter(condition)
    .map(transformation)
    .collect(toList());
```

### 3. peek을 최종 연산으로 오해

```java
// ❌ 실행 안 됨
list.stream()
    .peek(System.out::println);  // 최종 연산 아님!

// ✅ 최종 연산 추가
list.stream()
    .peek(System.out::println)
    .collect(toList());
```

### 4. 부작용 있는 연산

```java
// ❌ 위험! (스레드 안전하지 않음)
List<String> result = new ArrayList<>();
stream.forEach(result::add);

// ✅ 안전
List<String> result = stream.collect(toList());
```

---

## Quick Reference

### 중간 연산 (Lazy)

| 연산 | 설명 | 예시 |
|------|------|------|
| `filter` | 조건 필터링 | `filter(n -> n > 0)` |
| `map` | 요소 변환 | `map(String::toUpperCase)` |
| `flatMap` | 스트림 평면화 | `flatMap(List::stream)` |
| `distinct` | 중복 제거 | `distinct()` |
| `sorted` | 정렬 | `sorted()` |
| `peek` | 엿보기 | `peek(System.out::println)` |
| `limit` | 개수 제한 | `limit(10)` |
| `skip` | 건너뛰기 | `skip(5)` |

### 최종 연산 (Eager)

| 연산 | 설명 | 예시 |
|------|------|------|
| `forEach` | 각 요소 처리 | `forEach(System.out::println)` |
| `collect` | 결과 수집 | `collect(toList())` |
| `reduce` | 축약 | `reduce(0, Integer::sum)` |
| `count` | 개수 | `count()` |
| `anyMatch` | 하나라도? | `anyMatch(n -> n > 0)` |
| `allMatch` | 모두? | `allMatch(n -> n > 0)` |
| `noneMatch` | 하나도 안? | `noneMatch(n -> n < 0)` |
| `findFirst` | 첫 번째 | `findFirst()` |
| `findAny` | 아무거나 | `findAny()` |
| `min` | 최소값 | `min(Comparator.naturalOrder())` |
| `max` | 최대값 | `max(Comparator.naturalOrder())` |

---

**작성일:** 2024년  
**주제:** Java Stream API CheatSheet  
**난이도:** 중급
