# Chapter 06 Cheat Sheet 📝

> 스트림 데이터 수집 핵심 요약

---

## 🎯 Collectors 주요 메서드

### 리듀싱과 요약

| 메서드 | 반환 타입 | 설명 | 예시 |
|--------|----------|------|------|
| `counting()` | `Long` | 개수 세기 | `counting()` |
| `summingInt()` | `Integer` | 합계 | `summingInt(Dish::getCalories)` |
| `averagingInt()` | `Double` | 평균 | `averagingInt(Dish::getCalories)` |
| `summarizingInt()` | `IntSummaryStatistics` | 통계(count, sum, min, max, avg) | `summarizingInt(Dish::getCalories)` |
| `maxBy()` | `Optional<T>` | 최대값 | `maxBy(comparingInt(Dish::getCalories))` |
| `minBy()` | `Optional<T>` | 최소값 | `minBy(comparingInt(Dish::getCalories))` |
| `joining()` | `String` | 문자열 연결 | `joining(", ")` |
| `reducing()` | 다양 | 범용 리듀싱 | `reducing(0, Dish::getCalories, Integer::sum)` |

### 그룹화와 분할

| 메서드 | 키 타입 | 설명 | 예시 |
|--------|---------|------|------|
| `groupingBy()` | `K` | 그룹화 | `groupingBy(Dish::getType)` |
| `partitioningBy()` | `Boolean` | 분할 (true/false) | `partitioningBy(Dish::isVegetarian)` |

### 변환

| 메서드 | 설명 | 예시 |
|--------|------|------|
| `collectingAndThen()` | 결과 변환 | `collectingAndThen(maxBy(...), Optional::get)` |
| `mapping()` | 매핑 후 수집 | `mapping(Dish::getName, toList())` |
| `flatMapping()` | 평면화 후 수집 | `flatMapping(dish -> tags.get(dish).stream(), toSet())` |
| `filtering()` | 필터링 후 수집 | `filtering(d -> d.getCalories() > 500, toList())` |

### 컬렉션 생성

| 메서드 | 반환 타입 | 설명 |
|--------|----------|------|
| `toList()` | `List<T>` | 리스트 생성 |
| `toSet()` | `Set<T>` | 집합 생성 |
| `toCollection()` | `Collection<T>` | 특정 컬렉션 생성 |
| `toMap()` | `Map<K, V>` | 맵 생성 |

---

## 📊 그룹화 패턴

### 1. 기본 그룹화
```java
Map<K, List<T>> grouped = stream
    .collect(groupingBy(classifier));
```

### 2. 그룹화 + 개수
```java
Map<K, Long> counts = stream
    .collect(groupingBy(classifier, counting()));
```

### 3. 그룹화 + 합계
```java
Map<K, Integer> sums = stream
    .collect(groupingBy(classifier, summingInt(mapper)));
```

### 4. 그룹화 + 평균
```java
Map<K, Double> averages = stream
    .collect(groupingBy(classifier, averagingInt(mapper)));
```

### 5. 그룹화 + 최대/최소
```java
Map<K, Optional<T>> maxes = stream
    .collect(groupingBy(classifier, maxBy(comparator)));
```

### 6. 그룹화 + Optional 제거
```java
Map<K, T> maxes = stream
    .collect(groupingBy(
        classifier,
        collectingAndThen(maxBy(comparator), Optional::get)
    ));
```

### 7. 그룹화 + 필터링
```java
Map<K, List<T>> filtered = stream
    .collect(groupingBy(
        classifier,
        filtering(predicate, toList())
    ));
```

### 8. 그룹화 + 매핑
```java
Map<K, List<R>> mapped = stream
    .collect(groupingBy(
        classifier,
        mapping(mapper, toList())
    ));
```

### 9. 다수준 그룹화 (2단계)
```java
Map<K1, Map<K2, List<T>>> nested = stream
    .collect(groupingBy(
        classifier1,
        groupingBy(classifier2)
    ));
```

### 10. 다수준 그룹화 (3단계)
```java
Map<K1, Map<K2, Map<K3, List<T>>>> nested = stream
    .collect(groupingBy(
        classifier1,
        groupingBy(
            classifier2,
            groupingBy(classifier3)
        )
    ));
```

---

## 🔀 분할 패턴

### 1. 기본 분할
```java
Map<Boolean, List<T>> partitioned = stream
    .collect(partitioningBy(predicate));
```

### 2. 분할 + 개수
```java
Map<Boolean, Long> counts = stream
    .collect(partitioningBy(predicate, counting()));
```

### 3. 분할 + 그룹화
```java
Map<Boolean, Map<K, List<T>>> partitionedGroups = stream
    .collect(partitioningBy(
        predicate,
        groupingBy(classifier)
    ));
```

### 4. 분할 + 최대/최소
```java
Map<Boolean, T> maxes = stream
    .collect(partitioningBy(
        predicate,
        collectingAndThen(maxBy(comparator), Optional::get)
    ));
```

---

## ⚖️ collect() vs reduce()

| 특성 | collect() | reduce() |
|------|-----------|----------|
| **목적** | 가변 컨테이너에 수집 | 두 값을 하나로 결합 |
| **방식** | 가변(Mutable) | 불변(Immutable) |
| **객체 생성** | 1개 컨테이너 재사용 | 매번 새 객체 생성 |
| **병렬 처리** | 안전하고 효율적 | 동기화 필요 |
| **사용 사례** | List, Set, Map 생성 | 숫자 합계, 최대/최소 |

### collect() 사용
```java
✅ List<T> list = stream.collect(toList());
✅ Set<T> set = stream.collect(toSet());
✅ Map<K, V> map = stream.collect(toMap(...));
✅ String str = stream.collect(joining(", "));
✅ Map<K, List<T>> grouped = stream.collect(groupingBy(...));
```

### reduce() 사용
```java
✅ int sum = stream.reduce(0, Integer::sum);
✅ Optional<Integer> max = stream.reduce(Integer::max);
✅ String concat = stream.reduce("", (s1, s2) -> s1 + s2);
```

### 잘못된 사용
```java
❌ List<T> list = stream.reduce(new ArrayList<>(), ...);  // collect 사용!
❌ StringBuilder sb = stream.reduce(new StringBuilder(), ...);  // collect(joining())!
```

---

## 🔧 Collector 인터페이스

### 타입 파라미터
```java
Collector<T, A, R>
//        ↑  ↑  ↑
//        |  |  최종 결과 타입
//        |  누적자 타입
//        입력 요소 타입
```

### 5가지 메서드
```java
Supplier<A> supplier()              // 컨테이너 생성
BiConsumer<A, T> accumulator()      // 요소 추가
BinaryOperator<A> combiner()        // 병합 (병렬용)
Function<A, R> finisher()           // 최종 변환
Set<Characteristics> characteristics()  // 특성
```

### Characteristics
```
IDENTITY_FINISH  - finisher가 항등 함수 (A == R)
UNORDERED        - 순서 무관
CONCURRENT       - 동시 실행 가능 (스레드 안전 필요)
```

---

## 💡 빠른 참조

### 개수 세기
```java
long count = stream.collect(counting());
// 또는
long count = stream.count();
```

### 합계
```java
int sum = stream.collect(summingInt(mapper));
// 또는
int sum = stream.mapToInt(mapper).sum();
```

### 평균
```java
double avg = stream.collect(averagingInt(mapper));
// 또는
OptionalDouble avg = stream.mapToInt(mapper).average();
```

### 최대/최소
```java
Optional<T> max = stream.collect(maxBy(comparator));
// 또는
Optional<T> max = stream.max(comparator);
```

### 문자열 연결
```java
String result = stream.map(mapper).collect(joining(", "));
```

### 리스트 생성
```java
List<T> list = stream.collect(toList());
// 또는
List<T> list = stream.collect(Collectors.toList());
```

### 집합 생성
```java
Set<T> set = stream.collect(toSet());
```

### 맵 생성
```java
Map<K, V> map = stream.collect(toMap(keyMapper, valueMapper));
```

---

## ⚠️ 주의사항

### 1. groupingBy 필터링
```java
❌ stream.filter(...).collect(groupingBy(...))  // 키 누락 가능
✅ stream.collect(groupingBy(..., filtering(..., toList())))  // 모든 키 유지
```

### 2. Optional 처리
```java
❌ Map<K, Optional<T>> map = stream.collect(groupingBy(..., maxBy(...)));
✅ Map<K, T> map = stream.collect(groupingBy(..., collectingAndThen(maxBy(...), Optional::get)));
```

### 3. collect vs reduce
```java
❌ List<T> list = stream.reduce(new ArrayList<>(), ...);  // 비효율!
✅ List<T> list = stream.collect(toList());
```

### 4. 문자열 연결
```java
❌ String result = stream.reduce("", (s1, s2) -> s1 + s2);  // 큰 데이터면 느림
✅ String result = stream.collect(joining());
```

---

## 🚀 성능 팁

### 1. 기본형 특화 스트림 사용
```java
// ❌ 박싱 비용
int sum = stream.collect(summingInt(T::intValue));

// ✅ 기본형 스트림
int sum = stream.mapToInt(T::intValue).sum();
```

### 2. 불필요한 변환 제거
```java
// ❌ 두 번 순회
List<String> names = stream.map(T::getName).collect(toList());
Set<String> uniqueNames = names.stream().collect(toSet());

// ✅ 한 번에
Set<String> uniqueNames = stream.map(T::getName).collect(toSet());
```

### 3. 조기 종료 활용
```java
// ✅ limit으로 조기 종료
List<T> top10 = stream
    .sorted(comparator)
    .limit(10)
    .collect(toList());
```

### 4. 병렬 스트림 주의
```java
// 작은 데이터는 순차가 빠름
stream.parallel().collect(...)  // 오버헤드 > 이득

// 큰 데이터는 병렬이 빠름
largeStream.parallel().collect(...)  // 이득 > 오버헤드
```

---

## 📖 자주 쓰는 조합

### 타입별 요리 개수
```java
Map<Dish.Type, Long> counts = menu.stream()
    .collect(groupingBy(Dish::getType, counting()));
```

### 타입별 총 칼로리
```java
Map<Dish.Type, Integer> totalCalories = menu.stream()
    .collect(groupingBy(Dish::getType, summingInt(Dish::getCalories)));
```

### 타입별 평균 칼로리
```java
Map<Dish.Type, Double> avgCalories = menu.stream()
    .collect(groupingBy(Dish::getType, averagingInt(Dish::getCalories)));
```

### 타입별 가장 칼로리 높은 요리
```java
Map<Dish.Type, Dish> mostCaloric = menu.stream()
    .collect(groupingBy(
        Dish::getType,
        collectingAndThen(
            maxBy(comparingInt(Dish::getCalories)),
            Optional::get
        )
    ));
```

### 타입별 요리명 리스트
```java
Map<Dish.Type, List<String>> dishNames = menu.stream()
    .collect(groupingBy(
        Dish::getType,
        mapping(Dish::getName, toList())
    ));
```

### 채식/비채식 분할 + 타입별 그룹화
```java
Map<Boolean, Map<Dish.Type, List<Dish>>> partitionedByTypeAndVeg = menu.stream()
    .collect(partitioningBy(
        Dish::isVegetarian,
        groupingBy(Dish::getType)
    ));
```

---

**마지막 업데이트**: 2024년 12월  
**참고**: Modern Java in Action - Chapter 6
