# Chapter 06 Q&A Sessions ❓

> 스트림 데이터 수집에 대한 자주 묻는 질문

---

## 📚 목차

1. [기본 개념](#1-기본-개념)
2. [그룹화와 분할](#2-그룹화와-분할)
3. [Collector 인터페이스](#3-collector-인터페이스)
4. [성능과 최적화](#4-성능과-최적화)
5. [실무 활용](#5-실무-활용)

---

## 1. 기본 개념

### Q1: collect()와 reduce()는 언제 각각 사용하나요?

**A:** 목적에 따라 선택합니다.

**collect() 사용 (가변 리듀싱):**
```java
// ✅ 컬렉션 생성
List<String> list = stream.collect(toList());
Set<String> set = stream.collect(toSet());
Map<K, V> map = stream.collect(toMap(...));

// ✅ 그룹화/분할
Map<Type, List<Dish>> grouped = stream.collect(groupingBy(...));

// ✅ 문자열 연결 (큰 데이터)
String result = stream.collect(joining(", "));
```

**reduce() 사용 (불변 리듀싱):**
```java
// ✅ 단일 값 계산
int sum = stream.reduce(0, Integer::sum);
Optional<Integer> max = stream.reduce(Integer::max);

// ✅ 불변 객체 결합
BigInteger total = stream.reduce(BigInteger.ZERO, BigInteger::add);
```

**핵심 차이:**
- collect: 가변 컨테이너에 누적 (효율적)
- reduce: 매번 새 객체 생성 (단순)

### Q2: Collector<T, A, R>에서 A(누적자 타입)가 왜 필요한가요?

**A:** 중간 컨테이너를 나타냅니다.

**예시: toList()**
```java
Collector<String, List<String>, List<String>>
//                ↑              ↑
//                누적자          최종 결과
//                (ArrayList)     (List)

supplier() → ArrayList 생성
accumulator() → ArrayList에 추가
finisher() → ArrayList를 그대로 반환
```

**예시: joining()**
```java
Collector<String, StringBuilder, String>
//                ↑                ↑
//                누적자            최종 결과
//                (StringBuilder)   (String)

supplier() → StringBuilder 생성
accumulator() → StringBuilder에 추가
finisher() → StringBuilder.toString()
```

**누적자가 다른 이유:**
- 내부 구현은 효율적인 가변 컨테이너 사용
- 최종 결과는 사용자가 원하는 타입 반환

### Q3: 왜 maxBy/minBy는 Optional을 반환하나요?

**A:** 빈 스트림 가능성 때문입니다.

**문제 상황:**
```java
List<Dish> emptyMenu = new ArrayList<>();

// 빈 스트림에서 최대값?
Dish max = emptyMenu.stream()
    .collect(maxBy(comparingInt(Dish::getCalories)))
    .get();  // NoSuchElementException!
```

**올바른 처리:**
```java
Optional<Dish> max = menu.stream()
    .collect(maxBy(comparingInt(Dish::getCalories)));

// 방법 1: ifPresent
max.ifPresent(dish -> System.out.println(dish));

// 방법 2: orElse
Dish result = max.orElse(defaultDish);

// 방법 3: orElseThrow
Dish result = max.orElseThrow(() -> new RuntimeException("Empty!"));
```

**Optional 제거 (그룹화 시):**
```java
Map<Dish.Type, Dish> mostCaloricByType = menu.stream()
    .collect(groupingBy(
        Dish::getType,
        collectingAndThen(
            maxBy(comparingInt(Dish::getCalories)),
            Optional::get  // 각 그룹이 비어있지 않음을 보장
        )
    ));
```

---

## 2. 그룹화와 분할

### Q4: groupingBy와 partitioningBy의 차이는?

**A:** 키의 타입과 개수가 다릅니다.

**groupingBy - 임의의 키:**
```java
Map<Dish.Type, List<Dish>> grouped = menu.stream()
    .collect(groupingBy(Dish::getType));
// 키: MEAT, FISH, OTHER (3개)

Map<Integer, List<Integer>> byAge = people.stream()
    .collect(groupingBy(Person::getAge));
// 키: 20, 25, 30, ... (n개)
```

**partitioningBy - Boolean 키:**
```java
Map<Boolean, List<Dish>> partitioned = menu.stream()
    .collect(partitioningBy(Dish::isVegetarian));
// 키: true, false (항상 2개)
```

**장단점 비교:**

| 특성 | groupingBy | partitioningBy |
|------|-----------|----------------|
| 키 타입 | 임의 (K) | Boolean |
| 키 개수 | 가변 (0~n) | 고정 (2개) |
| 빈 그룹 | 키 없음 | 항상 true/false |
| 성능 | 일반 | Boolean 최적화 |

**언제 사용?**
```java
// ✅ partitioningBy 사용
Map<Boolean, List<Student>> passedFailed = students.stream()
    .collect(partitioningBy(s -> s.getScore() >= 60));

// ✅ groupingBy 사용
Map<Grade, List<Student>> byGrade = students.stream()
    .collect(groupingBy(s -> {
        if (s.getScore() >= 90) return Grade.A;
        else if (s.getScore() >= 80) return Grade.B;
        else if (s.getScore() >= 70) return Grade.C;
        else return Grade.F;
    }));
```

### Q5: groupingBy의 2인수 vs 3인수는 언제 쓰나요?

**A:** Map 구현체를 지정하려면 3인수 버전을 사용합니다.

**1인수 (기본):**
```java
Map<Dish.Type, List<Dish>> grouped = menu.stream()
    .collect(groupingBy(Dish::getType));
// = groupingBy(Dish::getType, toList())
// HashMap + ArrayList 사용
```

**2인수 (다운스트림 지정):**
```java
Map<Dish.Type, Long> counts = menu.stream()
    .collect(groupingBy(
        Dish::getType,      // 분류 함수
        counting()          // 다운스트림 컬렉터
    ));
// HashMap 사용
```

**3인수 (Map 구현체 지정):**
```java
TreeMap<Dish.Type, List<Dish>> sorted = menu.stream()
    .collect(groupingBy(
        Dish::getType,      // 분류 함수
        TreeMap::new,       // Map 팩토리
        toList()            // 다운스트림 컬렉터
    ));
// TreeMap 사용 → 키가 정렬됨!

LinkedHashMap<Dish.Type, List<Dish>> ordered = menu.stream()
    .collect(groupingBy(
        Dish::getType,
        LinkedHashMap::new, // 삽입 순서 유지
        toList()
    ));
```

**언제 3인수 사용?**
- 정렬된 맵: `TreeMap::new`
- 순서 유지: `LinkedHashMap::new`
- 동시성: `ConcurrentHashMap::new`

### Q6: filter vs filtering 차이는?

**A:** 키 유지 여부가 다릅니다.

**filter 먼저 (키 누락 가능):**
```java
Map<Dish.Type, List<Dish>> caloricDishesByType = menu.stream()
    .filter(dish -> dish.getCalories() > 500)
    .collect(groupingBy(Dish::getType));

// 문제: FISH 타입 요리가 모두 500 이하면?
// 결과: {MEAT=[...], OTHER=[...]}
// FISH 키 자체가 없음!
```

**filtering 컬렉터 (모든 키 유지):**
```java
Map<Dish.Type, List<Dish>> caloricDishesByType = menu.stream()
    .collect(groupingBy(
        Dish::getType,
        filtering(dish -> dish.getCalories() > 500, toList())
    ));

// 결과: {MEAT=[...], FISH=[], OTHER=[...]}
// FISH 키는 빈 리스트로 유지!
```

**언제 사용?**
- 모든 키 필요 → `filtering` 컬렉터
- 조건 만족하는 키만 → `filter` 먼저

---

## 3. Collector 인터페이스

### Q7: Characteristics는 무엇이고 언제 사용하나요?

**A:** 컬렉터의 최적화 힌트입니다.

**IDENTITY_FINISH:**
```java
// finisher가 항등 함수 (변환 불필요)
Collector<String, List<String>, List<String>>
//                ↑              ↑
//                같은 타입!

Set<Characteristics> characteristics() {
    return EnumSet.of(IDENTITY_FINISH);
}
// finisher() 호출 생략 → 성능 향상
```

**UNORDERED:**
```java
// 순서 무관
Set<Characteristics> characteristics() {
    return EnumSet.of(UNORDERED);
}
// 병렬 처리 시 더 효율적인 분할 가능
```

**CONCURRENT:**
```java
// 동시 실행 가능 (스레드 안전한 컨테이너 필요!)
Set<Characteristics> characteristics() {
    return EnumSet.of(CONCURRENT, UNORDERED);
}
// 여러 스레드가 같은 컨테이너에 동시 접근
```

**주의: CONCURRENT 사용 조건:**
```java
// ❌ ArrayList는 스레드 안전하지 않음!
Collector<T, List<T>, List<T>> dangerous = Collector.of(
    ArrayList::new,
    List::add,
    (l1, l2) -> { l1.addAll(l2); return l1; },
    CONCURRENT  // 위험!
);

// ✅ ConcurrentHashMap 같은 스레드 안전한 컨테이너
Collector<T, ConcurrentHashMap<K, V>, ...> safe = ...
```

### Q8: combiner()는 순차 스트림에서도 호출되나요?

**A:** 순차 스트림에서는 호출되지 않습니다.

**순차 스트림:**
```java
List<String> result = Stream.of("a", "b", "c")
    .collect(toList());

// 실행:
// 1. supplier() → container = []
// 2. accumulator(container, "a") → ["a"]
// 3. accumulator(container, "b") → ["a", "b"]
// 4. accumulator(container, "c") → ["a", "b", "c"]
// 5. finisher(container) → ["a", "b", "c"]
// combiner() 호출 안 됨!
```

**병렬 스트림:**
```java
List<String> result = Stream.of("a", "b", "c", "d")
    .parallel()
    .collect(toList());

// 실행:
// 1. supplier() 2번 → container1 = [], container2 = []
// 2. accumulator(container1, "a") → ["a"]
//    accumulator(container1, "b") → ["a", "b"]
//    accumulator(container2, "c") → ["c"]
//    accumulator(container2, "d") → ["c", "d"]
// 3. combiner(container1, container2) → ["a", "b", "c", "d"]
// 4. finisher(...) → ["a", "b", "c", "d"]
```

**따라서:**
- 순차: supplier + accumulator + finisher
- 병렬: supplier + accumulator + combiner + finisher

### Q9: 커스텀 컬렉터는 언제 만들어야 하나요?

**A:** 기본 컬렉터로 불가능하거나 성능 최적화가 필요할 때입니다.

**기본 컬렉터로 충분한 경우:**
```java
// ✅ 이미 있는 메서드 사용
List<String> list = stream.collect(toList());
Map<K, V> map = stream.collect(toMap(...));
Map<K, List<V>> grouped = stream.collect(groupingBy(...));
```

**커스텀 컬렉터가 필요한 경우:**

**1. 특수한 자료구조:**
```java
// 예: ImmutableList 생성
public class ToImmutableListCollector<T> 
        implements Collector<T, List<T>, ImmutableList<T>> {
    // ...
}
```

**2. 성능 최적화:**
```java
// 예: 소수 판별 최적화
public class PrimeNumbersCollector
        implements Collector<Integer, Map<Boolean, List<Integer>>, ...> {
    // 이미 찾은 소수 활용
}
```

**3. 복잡한 집계:**
```java
// 예: 복잡한 통계 수집
public class CustomStatisticsCollector<T>
        implements Collector<T, CustomStats, CustomStats> {
    // 특정 비즈니스 로직
}
```

**성능 비교 (소수 찾기):**
```
n=1,000,000

기본 방법: ~5분
커스텀 컬렉터: ~35초
→ 약 8.5배 빠름!
```

---

## 4. 성능과 최적화

### Q10: groupingBy는 성능이 어떤가요?

**A:** 일반적으로 효율적이지만 주의사항이 있습니다.

**시간 복잡도:**
```
groupingBy: O(n)
- 각 요소를 한 번씩 순회
- HashMap 삽입: O(1) amortized

다수준 groupingBy: O(n × m)
- n: 요소 개수
- m: 그룹화 레벨 수
```

**공간 복잡도:**
```
O(n)
- 모든 요소를 메모리에 유지
- 큰 데이터는 메모리 부족 가능
```

**최적화 팁:**

**1. 불필요한 변환 제거:**
```java
// ❌ 비효율
Map<Type, List<String>> names = menu.stream()
    .collect(groupingBy(Dish::getType, toList()))
    .entrySet().stream()
    .collect(toMap(
        Map.Entry::getKey,
        e -> e.getValue().stream()
                .map(Dish::getName)
                .collect(toList())
    ));

// ✅ 효율
Map<Type, List<String>> names = menu.stream()
    .collect(groupingBy(
        Dish::getType,
        mapping(Dish::getName, toList())
    ));
```

**2. 조기 필터링:**
```java
// ✅ 필요한 데이터만 그룹화
Map<Type, List<Dish>> grouped = menu.stream()
    .filter(dish -> dish.getCalories() > 300)  // 먼저 필터링
    .collect(groupingBy(Dish::getType));
```

**3. 병렬 스트림 활용 (큰 데이터):**
```java
Map<Type, List<Dish>> grouped = menu.parallelStream()
    .collect(groupingBy(Dish::getType));
// 주의: 작은 데이터는 오히려 느릴 수 있음!
```

### Q11: collect()의 병렬 처리는 항상 빠른가요?

**A:** 아닙니다. 데이터 크기와 연산 비용에 따라 다릅니다.

**병렬 처리 오버헤드:**
```
1. 스트림 분할 비용
2. 스레드 생성 및 관리
3. 결과 병합 (combiner)
4. 동기화 오버헤드
```

**언제 병렬이 빠른가?**
```java
// ✅ 큰 데이터 + 간단한 연산
largeList.parallelStream()
    .collect(groupingBy(Item::getCategory, counting()));

// ✅ 계산 비용이 큰 경우
data.parallelStream()
    .collect(groupingBy(d -> expensiveComputation(d)));
```

**언제 순차가 빠른가?**
```java
// ✅ 작은 데이터
smallList.stream()  // parallel 안 씀
    .collect(toList());

// ✅ 순서가 중요한 경우
orderedList.stream()
    .collect(toCollection(LinkedHashSet::new));
```

**벤치마크 예시:**
```
데이터 1,000개:
  순차: 10ms
  병렬: 15ms  ← 오히려 느림!

데이터 1,000,000개:
  순차: 1,000ms
  병렬: 300ms  ← 3.3배 빠름!
```

---

## 5. 실무 활용

### Q12: 실무에서 가장 자주 쓰는 패턴은?

**A:** 그룹화 + 집계 조합이 가장 많습니다.

**패턴 1: 그룹별 개수**
```java
// 카테고리별 상품 개수
Map<Category, Long> productCounts = products.stream()
    .collect(groupingBy(Product::getCategory, counting()));
```

**패턴 2: 그룹별 합계**
```java
// 부서별 총 급여
Map<Department, Integer> totalSalaries = employees.stream()
    .collect(groupingBy(
        Employee::getDepartment,
        summingInt(Employee::getSalary)
    ));
```

**패턴 3: 그룹별 평균**
```java
// 학년별 평균 점수
Map<Grade, Double> avgScores = students.stream()
    .collect(groupingBy(
        Student::getGrade,
        averagingDouble(Student::getScore)
    ));
```

**패턴 4: 그룹별 최대값**
```java
// 카테고리별 가장 비싼 상품
Map<Category, Product> mostExpensive = products.stream()
    .collect(groupingBy(
        Product::getCategory,
        collectingAndThen(
            maxBy(comparingInt(Product::getPrice)),
            Optional::get
        )
    ));
```

**패턴 5: 다수준 그룹화**
```java
// 년도별 → 월별 매출 합계
Map<Integer, Map<Integer, Double>> salesByYearMonth = 
    sales.stream()
        .collect(groupingBy(
            s -> s.getDate().getYear(),
            groupingBy(
                s -> s.getDate().getMonthValue(),
                summingDouble(Sale::getAmount)
            )
        ));
```

### Q13: 메모리가 부족한 큰 데이터는 어떻게 처리하나요?

**A:** 스트리밍 방식이나 청크 단위 처리를 사용합니다.

**문제:**
```java
// ❌ 메모리 부족 가능
Map<K, List<V>> huge = hugeStream
    .collect(groupingBy(...));
// 모든 데이터를 메모리에 유지!
```

**해결책 1: 필터링 먼저**
```java
// ✅ 필요한 데이터만
Map<K, List<V>> filtered = hugeStream
    .filter(필요한_조건)
    .collect(groupingBy(...));
```

**해결책 2: 청크 단위 처리**
```java
// ✅ 배치로 나눠서 처리
List<List<T>> chunks = partition(data, 10000);
for (List<T> chunk : chunks) {
    Map<K, List<V>> result = chunk.stream()
        .collect(groupingBy(...));
    processResult(result);
}
```

**해결책 3: 데이터베이스 활용**
```sql
-- SQL로 그룹화
SELECT category, COUNT(*), SUM(price), AVG(price)
FROM products
GROUP BY category;
```

### Q14: collectingAndThen은 언제 쓰나요?

**A:** 수집 후 결과를 변환할 때 사용합니다.

**사용 사례 1: Optional 제거**
```java
Map<Type, Dish> mostCaloric = menu.stream()
    .collect(groupingBy(
        Dish::getType,
        collectingAndThen(
            maxBy(comparingInt(Dish::getCalories)),
            Optional::get  // Optional<Dish> → Dish
        )
    ));
```

**사용 사례 2: 불변 컬렉션**
```java
List<String> immutable = stream
    .collect(collectingAndThen(
        toList(),
        Collections::unmodifiableList
    ));
```

**사용 사례 3: 정렬**
```java
List<Dish> sorted = menu.stream()
    .collect(collectingAndThen(
        toList(),
        list -> {
            list.sort(comparingInt(Dish::getCalories));
            return list;
        }
    ));
```

**사용 사례 4: 변환**
```java
String summary = menu.stream()
    .collect(collectingAndThen(
        summarizingInt(Dish::getCalories),
        stats -> String.format(
            "Count: %d, Avg: %.2f",
            stats.getCount(),
            stats.getAverage()
        )
    ));
```

### Q15: 디버깅 팁이 있나요?

**A:** 중간 결과를 로깅하거나 단계별로 분리합니다.

**방법 1: peek() 사용**
```java
Map<Type, List<Dish>> result = menu.stream()
    .peek(dish -> System.out.println("Processing: " + dish))
    .collect(groupingBy(Dish::getType));
```

**방법 2: 단계별 분리**
```java
// ❌ 한 번에 (디버깅 어려움)
Map<Type, Dish> result = menu.stream()
    .collect(groupingBy(
        Dish::getType,
        collectingAndThen(maxBy(...), Optional::get)
    ));

// ✅ 단계별 (디버깅 쉬움)
Map<Type, Optional<Dish>> step1 = menu.stream()
    .collect(groupingBy(Dish::getType, maxBy(...)));
System.out.println("Step 1: " + step1);

Map<Type, Dish> step2 = step1.entrySet().stream()
    .collect(toMap(
        Map.Entry::getKey,
        e -> e.getValue().get()
    ));
System.out.println("Step 2: " + step2);
```

**방법 3: 커스텀 컬렉터에 로깅**
```java
@Override
public BiConsumer<A, T> accumulator() {
    return (acc, elem) -> {
        System.out.println("Adding: " + elem + " to " + acc);
        acc.add(elem);
    };
}
```

---

**마지막 업데이트**: 2024년 12월  
**참고**: Modern Java in Action - Chapter 6
