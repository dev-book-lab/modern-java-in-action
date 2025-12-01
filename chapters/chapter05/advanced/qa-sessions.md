# Chapter 05 Q&A Sessions ❓

> 스트림 활용에 대한 자주 묻는 질문들

---

## 📌 필터링과 슬라이싱

### Q1: filter vs takeWhile, 언제 뭘 쓰나요?

**A:** 데이터 정렬 여부로 결정!

```java
// 정렬 안 됨 → filter
list.stream()
    .filter(n -> n > 5)  // 전체 검사

// 정렬됨 → takeWhile (더 빠름!)
sortedList.stream()
    .takeWhile(n -> n > 5)  // 조건 false 나오면 중단
```

**성능 차이:**
- filter: O(n) - 전체 검사
- takeWhile: O(k) - k는 조건 만족하는 개수

---

### Q2: distinct()가 느린데 대안은?

**A:** Set을 직접 사용!

```java
// ❌ 느림
List<Integer> unique = list.stream()
    .distinct()
    .collect(toList());

// ✅ 빠름
Set<Integer> uniqueSet = new HashSet<>(list);
List<Integer> unique = new ArrayList<>(uniqueSet);
```

---

### Q3: limit과 skip을 함께 쓰면 순서는?

**A:** `skip` → `limit` 순서!

```java
stream
    .skip(2)   // 처음 2개 건너뛰고
    .limit(5)  // 그 다음 5개만
```

---

## 📌 매핑

### Q4: map vs flatMap, 언제 뭘 쓰나요?

**A:** 반환 타입으로 결정!

```java
// map: 1:1 변환
list.stream()
    .map(String::length)  // String → Integer

// flatMap: 1:N 변환 후 평면화
list.stream()
    .flatMap(s -> Arrays.stream(s.split("")))  // String → Stream<String>
```

**간단히:**
- 반환이 `단일 값` → map
- 반환이 `스트림/배열/컬렉션` → flatMap

---

### Q5: flatMap이 어려워요. 쉽게 이해하려면?

**A:** 2단계로 생각하세요!

```java
words.stream()
    .flatMap(word -> Arrays.stream(word.split("")))

// 1단계: map
words.stream()
    .map(word -> Arrays.stream(word.split("")))
// → Stream<Stream<String>> (2중 스트림!)

// 2단계: flat (평면화)
// → Stream<String> (1중 스트림)
```

---

## 📌 검색과 매칭

### Q6: findAny vs findFirst, 어떤 차이?

**A:** 병렬 스트림에서 차이 발생!

```java
// 순차 스트림 - 둘 다 같음
list.stream()
    .filter(...)
    .findAny();    // 첫 번째
    .findFirst();  // 첫 번째

// 병렬 스트림 - 차이 발생
list.parallelStream()
    .filter(...)
    .findAny();    // 아무거나 (빠름!)
    .findFirst();  // 첫 번째 (느림)
```

**선택 기준:**
- 순서 중요 → `findFirst`
- 순서 무관 + 성능 중요 → `findAny`

---

### Q7: anyMatch는 왜 Optional이 아니라 boolean인가요?

**A:** 결과가 명확하기 때문!

```java
// anyMatch - boolean 반환
boolean result = stream.anyMatch(...)
// true/false가 명확함

// findAny - Optional 반환
Optional<T> result = stream.findAny()
// 요소가 없을 수도 있음!
```

---

### Q8: allMatch(빈 스트림)이 true인 이유는?

**A:** "공허한 참(vacuous truth)" 개념!

```java
Stream.empty()
    .allMatch(n -> n > 100)  // true!
// "모든 요소가 조건을 만족" (요소가 없으니 만족!)

Stream.empty()
    .anyMatch(n -> n > 100)  // false
// "하나라도 조건 만족" (요소가 없으니 불만족)
```

---

## 📌 리듀싱

### Q9: reduce 초기값은 어떻게 정하나요?

**A:** **항등원(identity)** 을 사용!

```java
// 합계 → 0 (0 + x = x)
reduce(0, Integer::sum)

// 곱셈 → 1 (1 * x = x)
reduce(1, (a, b) -> a * b)

// 문자열 결합 → "" ("" + x = x)
reduce("", (a, b) -> a + b)
```

**❌ 잘못된 초기값:**
```java
// 10은 항등원이 아님!
reduce(10, Integer::sum)  // 결과에 10이 더해짐
```

---

### Q10: reduce vs collect, 차이는?

**A:** 불변 vs 가변!

```java
// reduce - 불변 (새 값 생성)
int sum = stream.reduce(0, Integer::sum);

// collect - 가변 (기존 컨테이너 수정)
List<Integer> list = stream.collect(toList());
```

---

## 📌 기본형 스트림

### Q11: 박싱 비용이 얼마나 차이나나요?

**A:** 약 **3~5배** 차이!

```java
// 100만 개 합계
// Stream<Integer>: ~150ms (박싱)
// IntStream: ~50ms (박싱 없음)
```

---

### Q12: 언제 boxed()를 써야 하나요?

**A:** 컬렉션으로 수집할 때!

```java
// ❌ IntStream에는 collect(toList()) 없음
IntStream.range(1, 10)
    .collect(toList());  // 컴파일 에러!

// ✅ boxed() 후 수집
List<Integer> list = IntStream.range(1, 10)
    .boxed()
    .collect(toList());
```

---

### Q13: range vs rangeClosed, 어떤 차이?

**A:** 끝 값 포함 여부!

```java
IntStream.range(1, 5)        // [1, 5) → 1,2,3,4
IntStream.rangeClosed(1, 5)  // [1, 5] → 1,2,3,4,5
```

**선택 기준:**
- 인덱스 사용 → `range` (0부터 length까지)
- 범위 사용 → `rangeClosed` (1부터 100까지)

---

## 📌 스트림 생성

### Q14: Stream.of vs Arrays.stream, 차이는?

**A:** 기본형 배열 처리!

```java
int[] numbers = {1, 2, 3};

// Stream.of - Stream<int[]> 생성 (주의!)
Stream.of(numbers)  // 배열 자체가 하나의 요소

// Arrays.stream - IntStream 생성 (올바름!)
Arrays.stream(numbers)  // 각 요소가 개별 요소
```

---

### Q15: 파일 스트림을 close 안 하면?

**A:** 리소스 누수 발생!

```java
// ❌ close 안 함
Stream<String> lines = Files.lines(path);
lines.forEach(System.out::println);
// 파일 핸들이 닫히지 않음!

// ✅ try-with-resources
try (Stream<String> lines = Files.lines(path)) {
    lines.forEach(System.out::println);
}  // 자동 close
```

---

## 📌 무한 스트림

### Q16: iterate vs generate, 언제 뭘 쓰나요?

**A:** 이전 값 의존 여부!

```java
// iterate - 이전 값 기반 (순차)
Stream.iterate(0, n -> n + 1)  // 0, 1, 2, 3, ...

// generate - 독립적 (랜덤)
Stream.generate(Math::random)  // 0.123, 0.456, ...
```

**선택 기준:**
- 규칙적 수열 → `iterate`
- 랜덤/독립적 → `generate`

---

### Q17: 무한 스트림에 sorted()를 쓰면?

**A:** 무한 루프!

```java
// ❌ 무한 루프 (전체 정렬 시도)
Stream.iterate(0, n -> n + 1)
    .sorted()    // 끝이 없으니 정렬 불가!
    .limit(10);

// ✅ limit 먼저
Stream.iterate(0, n -> n + 1)
    .limit(10)
    .sorted();
```

---

### Q18: generate에서 상태를 유지하면?

**A:** 병렬 스트림에서 위험!

```java
// ❌ 병렬 스트림에서 문제
class Counter {
    int count = 0;
    int getNext() { return count++; }
}
Counter counter = new Counter();
stream.parallel()
    .generate(counter::getNext)  // 동시성 문제!

// ✅ 상태 없는 Supplier
stream.generate(Math::random)  // 안전!
```

---

## 📌 성능과 최적화

### Q19: 스트림이 for문보다 느린가요?

**A:** 경우에 따라 다름!

**스트림이 빠른 경우:**
```java
// 병렬 처리
list.parallelStream()
    .filter(...)
    .map(...)

// 쇼트서킷
list.stream()
    .anyMatch(...)  // 찾으면 즉시 종료
```

**for문이 빠른 경우:**
```java
// 단순 반복
for (int i = 0; i < n; i++) {
    sum += i;
}
```

---

### Q20: 스트림 최적화 팁은?

**A:** 5가지 원칙!

1. **filter 먼저** (데이터 줄이기)
2. **기본형 스트림** (박싱 피하기)
3. **limit 활용** (조기 종료)
4. **병렬 신중히** (작은 데이터는 오히려 느림)
5. **상태 없는 연산** (상태 있는 연산 최소화)

---

## 💡 실전 팁

### 스트림 디버깅

```java
list.stream()
    .peek(n -> System.out.println("filter 전: " + n))
    .filter(n -> n > 5)
    .peek(n -> System.out.println("filter 후: " + n))
    .map(n -> n * 2)
    .peek(n -> System.out.println("map 후: " + n))
    .collect(toList());
```

### Optional 안전하게 처리

```java
stream.findAny()
    .ifPresent(System.out::println)  // 있으면 출력
    .orElse(defaultValue)            // 없으면 기본값
    .orElseThrow()                   // 없으면 예외
```

---

**마지막 업데이트**: 2024년 12월
