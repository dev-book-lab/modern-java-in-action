# Chapter 04 스트림 Q&A 세션 💬

> 스트림 학습 중 자주 묻는 질문과 심화 질문에 대한 답변 모음

---

## 📑 목차

### 기본 개념
1. [스트림과 컬렉션의 가장 큰 차이는?](#q1-스트림과-컬렉션의-가장-큰-차이는)
2. [스트림은 왜 단 한 번만 소비할 수 있나요?](#q2-스트림은-왜-단-한-번만-소비할-수-있나요)
3. [스트림을 언제 사용해야 하나요?](#q3-스트림을-언제-사용해야-하나요)

### 연산 관련
4. [중간 연산과 최종 연산을 어떻게 구분하나요?](#q4-중간-연산과-최종-연산을-어떻게-구분하나요)
5. [peek()과 forEach()의 차이는?](#q5-peek과-foreach의-차이는)
6. [map()과 flatMap()의 차이는?](#q6-map과-flatmap의-차이는)

### 성능 관련
7. [게으른 실행이 왜 빠른가요?](#q7-게으른-실행이-왜-빠른가요)
8. [쇼트서킷은 얼마나 빠른가요?](#q8-쇼트서킷은-얼마나-빠른가요)
9. [병렬 스트림은 항상 빠른가요?](#q9-병렬-스트림은-항상-빠른가요)

### 실전 활용
10. [스트림에서 인덱스를 어떻게 사용하나요?](#q10-스트림에서-인덱스를-어떻게-사용하나요)
11. [스트림을 List로 변환하는 방법은?](#q11-스트림을-list로-변환하는-방법은)
12. [null을 안전하게 처리하는 방법은?](#q12-null을-안전하게-처리하는-방법은)

### 고급 주제
13. [무한 스트림은 어떻게 만드나요?](#q13-무한-스트림은-어떻게-만드나요)
14. [스트림은 어떻게 최적화되나요?](#q14-스트림은-어떻게-최적화되나요)
15. [스트림의 메모리 사용량은?](#q15-스트림의-메모리-사용량은)

### 실수 방지
16. [자주 하는 실수는?](#q16-자주-하는-실수는)
17. [디버깅은 어떻게 하나요?](#q17-디버깅은-어떻게-하나요)
18. [성능 문제는 어떻게 찾나요?](#q18-성능-문제는-어떻게-찾나요)

### 심화 질문
19. [스트림의 내부 구현은?](#q19-스트림의-내부-구현은)
20. [함수형 프로그래밍과의 관계는?](#q20-함수형-프로그래밍과의-관계는)

---

## 기본 개념

### Q1. 스트림과 컬렉션의 가장 큰 차이는?

**A:** 데이터를 **언제 계산하느냐**입니다.

**컬렉션 (Eager - 즉시 계산):**
```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
// 이미 모든 값이 메모리에 존재
```

**스트림 (Lazy - 필요할 때 계산):**
```java
Stream<Integer> stream = numbers.stream()
    .filter(n -> n > 2)    // 아직 실행 안 됨!
    .map(n -> n * 2);      // 아직 실행 안 됨!

stream.forEach(System.out::println);  // 이제 실행!
```

**비유:**
- 컬렉션 = DVD (모든 장면이 저장됨)
- 스트림 = Netflix (보는 장면만 전송)

---

### Q2. 스트림은 왜 단 한 번만 소비할 수 있나요?

**A:** 스트림은 **데이터의 흐름**을 나타내기 때문입니다.

**개념:**
```java
Stream<String> stream = list.stream();
stream.forEach(System.out::println);  // ✅ OK
stream.forEach(System.out::println);  // ❌ IllegalStateException!
```

**이유:**
1. **효율성**: 데이터를 한 번만 순회하여 처리
2. **설계 철학**: 데이터의 "흐름"을 나타냄 (물이 흐르면 다시 안 옴)
3. **최적화**: 한 번의 패스로 모든 연산 수행

**해결책:**
```java
// 매번 새로운 스트림 생성
list.stream().forEach(System.out::println);
list.stream().forEach(System.out::println);  // ✅ OK

// 또는 Supplier 사용
Supplier<Stream<String>> streamSupplier = () -> list.stream();
streamSupplier.get().forEach(System.out::println);
streamSupplier.get().forEach(System.out::println);
```

---

### Q3. 스트림을 언제 사용해야 하나요?

**A:** 다음 경우에 스트림을 사용하세요:

**✅ 사용해야 할 때:**
```java
// 1. 복잡한 데이터 처리 파이프라인
List<String> result = users.stream()
    .filter(u -> u.isActive())
    .filter(u -> u.getAge() > 18)
    .map(User::getName)
    .sorted()
    .collect(toList());

// 2. 병렬 처리
long count = bigList.parallelStream()
    .filter(condition)
    .count();

// 3. 대용량 데이터
Files.lines(path)
    .filter(line -> line.contains("ERROR"))
    .forEach(System.out::println);

// 4. 함수형 스타일 선호
Optional<User> found = users.stream()
    .filter(u -> u.getId() == targetId)
    .findFirst();
```

**❌ 사용하지 말아야 할 때:**
```java
// 1. 간단한 반복 (3줄 이하)
for (String name : names) {
    System.out.println(name);  // 이게 더 명확
}

// 2. 인덱스가 필요한 경우
for (int i = 0; i < list.size(); i++) {
    process(list.get(i), i);  // 인덱스 필요
}

// 3. 반복 중 break/continue 필요
for (Item item : items) {
    if (item.isTarget()) break;  // 스트림에서는 복잡
}
```

---

## 연산 관련

### Q4. 중간 연산과 최종 연산을 어떻게 구분하나요?

**A:** **반환 타입**으로 구분합니다.

**중간 연산 - Stream을 반환:**
```java
Stream<T> filter(Predicate<T> p)     // Stream<T> 반환
Stream<T> map(Function<T, R> f)      // Stream<R> 반환
Stream<T> sorted()                    // Stream<T> 반환
Stream<T> distinct()                  // Stream<T> 반환
```

**최종 연산 - 구체적인 타입 반환:**
```java
void forEach(Consumer<T> c)           // void 반환
List<T> collect(Collector<T, ?, R>)   // List<T> 반환
long count()                          // long 반환
Optional<T> findFirst()               // Optional<T> 반환
```

**실전 예제:**
```java
list.stream()                    // Stream<String>
    .filter(s -> s.length() > 3) // Stream<String> (중간)
    .map(String::toUpperCase)    // Stream<String> (중간)
    .sorted()                    // Stream<String> (중간)
    .collect(toList());          // List<String> (최종)
```

---

### Q5. peek()과 forEach()의 차이는?

**A:** `peek()`은 **중간 연산**, `forEach()`는 **최종 연산**입니다.

**peek() - 디버깅용 중간 연산:**
```java
list.stream()
    .peek(x -> System.out.println("Before: " + x))
    .filter(x -> x > 10)
    .peek(x -> System.out.println("After: " + x))
    .collect(toList());  // 최종 연산 필요!
```

**forEach() - 최종 연산:**
```java
list.stream()
    .filter(x -> x > 10)
    .forEach(System.out::println);  // 스트림 닫힘
```

**주의사항:**
```java
// ❌ 잘못된 사용
list.stream()
    .peek(System.out::println);  // 실행 안 됨! (최종 연산 없음)

// ✅ 올바른 사용
list.stream()
    .peek(System.out::println)
    .collect(toList());  // 최종 연산 추가
```

---

### Q6. map()과 flatMap()의 차이는?

**A:** `map()`은 **1:1 변환**, `flatMap()`은 **1:N 변환 후 평면화**입니다.

**map() - 각 요소를 변환:**
```java
List<String> words = Arrays.asList("Hello", "World");

words.stream()
     .map(String::toUpperCase)
     .collect(toList());
// ["HELLO", "WORLD"]

words.stream()
     .map(String::length)
     .collect(toList());
// [5, 5]
```

**flatMap() - 스트림을 평면화:**
```java
// 각 단어를 문자로 분리
words.stream()
     .map(word -> word.split(""))        // Stream<String[]>
     .collect(toList());
// [["H","e","l","l","o"], ["W","o","r","l","d"]]  ← 2차원!

words.stream()
     .flatMap(word -> Arrays.stream(word.split("")))  // Stream<String>
     .distinct()
     .collect(toList());
// ["H", "e", "l", "o", "W", "r", "d"]  ← 1차원!
```

**시각화:**
```
map:
["Hello", "World"]
   ↓ map(word -> word.split(""))
[["H","e","l","l","o"], ["W","o","r","l","d"]]  ← 2차원 배열

flatMap:
["Hello", "World"]
   ↓ flatMap(word -> Arrays.stream(word.split("")))
["H", "e", "l", "o", "W", "r", "l", "d"]  ← 평면화됨!
```

---

## 성능 관련

### Q7. 게으른 실행이 왜 빠른가요?

**A:** **필요한 만큼만 계산**하기 때문입니다.

**예제: 첫 번째 짝수 찾기**

**즉시 실행 (가정):**
```java
// 1단계: 모든 짝수 찾기 (100만 번)
List<Integer> allEven = new ArrayList<>();
for (int i = 0; i < 1_000_000; i++) {
    if (i % 2 == 0) allEven.add(i);
}

// 2단계: 첫 번째 선택
Integer first = allEven.get(0);

// 처리: 100만 번!
```

**게으른 실행 (스트림):**
```java
Optional<Integer> first = Stream.iterate(0, n -> n + 1)
    .filter(n -> n % 2 == 0)
    .findFirst();

// 처리: 1번! (0 검사 → 짝수 → 반환)
```

**이점:**
1. **불필요한 연산 회피**: 필요한 것만 계산
2. **무한 스트림 가능**: 무한이어도 처리 가능
3. **메모리 효율**: 중간 컬렉션 생성 안 함

---

### Q8. 쇼트서킷은 얼마나 빠른가요?

**A:** 경우에 따라 **수천~수백만 배** 빠를 수 있습니다.

**실험: 100만 개 중 첫 번째 찾기**

```java
List<Integer> numbers = IntStream.range(0, 1_000_000)
    .boxed()
    .collect(toList());

// 쇼트서킷 없음
long start1 = System.nanoTime();
List<Integer> all = numbers.stream()
    .filter(n -> n % 2 == 0)
    .collect(toList());
Integer first1 = all.get(0);
long time1 = System.nanoTime() - start1;
// 시간: ~200ms (100만 번 검사)

// 쇼트서킷 사용
long start2 = System.nanoTime();
Optional<Integer> first2 = numbers.stream()
    .filter(n -> n % 2 == 0)
    .findFirst();
long time2 = System.nanoTime() - start2;
// 시간: ~0.001ms (1번 검사)

// 속도 차이: 200,000배!
```

**쇼트서킷 연산:**
- `anyMatch()`, `allMatch()`, `noneMatch()`
- `findFirst()`, `findAny()`
- `limit(n)`

---

### Q9. 병렬 스트림은 항상 빠른가요?

**A:** **아니오!** 다음 경우에만 빠릅니다.

**✅ 병렬 스트림이 빠른 경우:**
```java
// 1. 대용량 데이터 (수만~수백만 개)
bigList.parallelStream()
    .filter(complexCondition)
    .collect(toList());

// 2. CPU 집약적 작업
numbers.parallelStream()
    .map(n -> expensiveComputation(n))
    .collect(toList());

// 3. 분할 가능한 소스 (ArrayList, 배열 등)
array.parallelStream()
    .filter(condition)
    .collect(toList());
```

**❌ 병렬 스트림이 느린 경우:**
```java
// 1. 소량 데이터 (수백~수천 개)
smallList.parallelStream()  // 오버헤드 > 이득
    .filter(condition)
    .collect(toList());

// 2. I/O 작업
files.parallelStream()
    .map(f -> readFile(f))  // 병목은 I/O, CPU 아님
    .collect(toList());

// 3. 분할 어려운 소스 (LinkedList 등)
linkedList.parallelStream()  // 분할 비용이 큼
    .filter(condition)
    .collect(toList());

// 4. 순서가 중요한 경우
list.parallelStream()
    .sorted()  // 병렬 정렬 후 합치기 필요
    .collect(toList());
```

**벤치마크 예제:**
```java
// 소량 데이터 (1,000개)
List<Integer> small = IntStream.range(0, 1_000).boxed().collect(toList());

// 순차
small.stream().filter(n -> n % 2 == 0).count();
// 시간: 1ms

// 병렬
small.parallelStream().filter(n -> n % 2 == 0).count();
// 시간: 5ms (오히려 느림!)

// 대량 데이터 (1,000,000개)
List<Integer> large = IntStream.range(0, 1_000_000).boxed().collect(toList());

// 순차
large.stream().filter(n -> isPrime(n)).count();
// 시간: 1000ms

// 병렬
large.parallelStream().filter(n -> isPrime(n)).count();
// 시간: 250ms (4배 빠름!)
```

---

## 실전 활용

### Q10. 스트림에서 인덱스를 어떻게 사용하나요?

**A:** 여러 방법이 있습니다.

**방법 1: IntStream.range() 사용**
```java
List<String> names = Arrays.asList("Alice", "Bob", "Charlie");

IntStream.range(0, names.size())
    .forEach(i -> System.out.println(i + ": " + names.get(i)));

// 출력:
// 0: Alice
// 1: Bob
// 2: Charlie
```

**방법 2: AtomicInteger 사용**
```java
AtomicInteger index = new AtomicInteger();

names.stream()
    .forEach(name -> System.out.println(
        index.getAndIncrement() + ": " + name
    ));
```

**방법 3: 인덱스 포함 클래스 만들기**
```java
class Indexed<T> {
    final int index;
    final T value;
    
    Indexed(int index, T value) {
        this.index = index;
        this.value = value;
    }
}

IntStream.range(0, names.size())
    .mapToObj(i -> new Indexed<>(i, names.get(i)))
    .filter(indexed -> indexed.value.length() > 3)
    .forEach(indexed -> 
        System.out.println(indexed.index + ": " + indexed.value)
    );
```

**주의:** 인덱스가 자주 필요하면 for 루프가 더 나을 수 있습니다.

---

### Q11. 스트림을 List로 변환하는 방법은?

**A:** `collect(Collectors.toList())`를 사용합니다.

**기본 방법:**
```java
List<String> result = stream.collect(Collectors.toList());

// 또는 static import
import static java.util.stream.Collectors.toList;

List<String> result = stream.collect(toList());
```

**특정 List 구현체로:**
```java
// ArrayList로
List<String> arrayList = stream.collect(
    Collectors.toCollection(ArrayList::new)
);

// LinkedList로
List<String> linkedList = stream.collect(
    Collectors.toCollection(LinkedList::new)
);
```

**불변 리스트로 (Java 10+):**
```java
List<String> immutable = stream.collect(
    Collectors.toUnmodifiableList()
);
```

**배열로:**
```java
String[] array = stream.toArray(String[]::new);
```

---

### Q12. null을 안전하게 처리하는 방법은?

**A:** `Objects.nonNull()` 또는 `Optional`을 사용합니다.

**방법 1: null 필터링**
```java
List<String> names = Arrays.asList("Alice", null, "Bob", null, "Charlie");

List<String> nonNull = names.stream()
    .filter(Objects::nonNull)
    .collect(toList());
// ["Alice", "Bob", "Charlie"]
```

**방법 2: Optional 활용**
```java
Optional<String> result = Optional.ofNullable(nullableValue)
    .map(String::toUpperCase)
    .filter(s -> s.length() > 3);
```

**방법 3: flatMap + Optional**
```java
List<String> names = Arrays.asList("Alice", null, "Bob");

List<String> processed = names.stream()
    .flatMap(name -> Optional.ofNullable(name).stream())
    .map(String::toUpperCase)
    .collect(toList());
```

**주의사항:**
```java
// ❌ NPE 위험
stream.map(s -> s.toUpperCase())  // s가 null이면 NPE!

// ✅ 안전
stream.filter(Objects::nonNull)
      .map(String::toUpperCase)
```

---

## 고급 주제

### Q13. 무한 스트림은 어떻게 만드나요?

**A:** `Stream.iterate()` 또는 `Stream.generate()`를 사용합니다.

**iterate() - 수열 생성:**
```java
// 0, 1, 2, 3, 4, ...
Stream.iterate(0, n -> n + 1)
    .limit(10)
    .forEach(System.out::println);

// 피보나치 수열
Stream.iterate(new int[]{0, 1}, f -> new int[]{f[1], f[0] + f[1]})
    .limit(10)
    .map(f -> f[0])
    .forEach(System.out::println);
// 0, 1, 1, 2, 3, 5, 8, 13, 21, 34
```

**generate() - 임의 값 생성:**
```java
// 랜덤 숫자
Stream.generate(Math::random)
    .limit(5)
    .forEach(System.out::println);

// 상수
Stream.generate(() -> "Hello")
    .limit(3)
    .forEach(System.out::println);
// Hello
// Hello
// Hello
```

**주의:** 무한 스트림은 **반드시 limit()** 또는 **쇼트서킷 연산** 필요!

---

### Q14. 스트림은 어떻게 최적화되나요?

**A:** 3가지 주요 최적화 기법이 있습니다.

**1. 게으른 실행 (Lazy Evaluation)**
```java
stream
    .filter(...)   // 저장만 함
    .map(...)      // 저장만 함
    .collect(...); // 이제 실행!
```

**2. 쇼트서킷 (Short-circuit)**
```java
stream
    .filter(...)
    .findFirst();  // 찾으면 즉시 중단!
```

**3. 루프 퓨전 (Loop Fusion)**
```java
// 여러 연산을 하나의 패스로 합침
stream
    .filter(...)  // \
    .map(...)     //  } 한 번의 루프!
    .filter(...)  // /
```

**내부 최적화 예시:**
```java
// 개발자 코드
stream.filter(a).map(b).filter(c).collect(toList())

// 내부적으로 실행 (개념적)
for (element : source) {
    if (a(element)) {           // filter
        temp = b(element);       // map
        if (c(temp)) {           // filter
            result.add(temp);    // collect
        }
    }
}
```

---

### Q15. 스트림의 메모리 사용량은?

**A:** **매우 적습니다!** 중간 컬렉션을 만들지 않기 때문입니다.

**전통적 방식:**
```java
List<Integer> temp1 = new ArrayList<>();  // 메모리 사용
for (int n : numbers) {
    if (n > 10) temp1.add(n);
}

List<Integer> temp2 = new ArrayList<>();  // 메모리 사용
for (int n : temp1) {
    temp2.add(n * 2);
}

// 메모리: 원본 + temp1 + temp2 = 3배
```

**스트림 방식:**
```java
List<Integer> result = numbers.stream()
    .filter(n -> n > 10)
    .map(n -> n * 2)
    .collect(toList());

// 메모리: 원본 + 결과만 = 2배
// (중간 컬렉션 temp1, temp2 없음!)
```

**대용량 파일 처리:**
```java
// 전통적 방식 - OOM 위험!
List<String> lines = Files.readAllLines(path);  // 전체를 메모리에!

// 스트림 방식 - 안전
Files.lines(path)
    .filter(line -> line.contains("ERROR"))
    .forEach(System.out::println);
// 한 줄씩만 메모리에!
```

---

## 실수 방지

### Q16. 자주 하는 실수는?

**A:** 다음 4가지 실수가 가장 흔합니다.

**실수 1: 스트림 재사용**
```java
// ❌ 에러!
Stream<String> stream = list.stream();
stream.forEach(System.out::println);
stream.forEach(System.out::println);  // IllegalStateException!

// ✅ 수정
list.stream().forEach(System.out::println);
list.stream().forEach(System.out::println);
```

**실수 2: 최종 연산 누락**
```java
// ❌ 실행 안 됨
list.stream()
    .filter(s -> s.length() > 3)
    .map(String::toUpperCase);  // 최종 연산 없음!

// ✅ 수정
list.stream()
    .filter(s -> s.length() > 3)
    .map(String::toUpperCase)
    .collect(toList());  // 최종 연산 추가
```

**실수 3: peek을 최종 연산으로 오해**
```java
// ❌ 실행 안 됨
list.stream()
    .peek(System.out::println);  // peek은 중간 연산!

// ✅ 수정
list.stream()
    .peek(System.out::println)
    .collect(toList());  // 최종 연산 필요
```

**실수 4: 외부 상태 변경**
```java
// ❌ 위험! (스레드 안전하지 않음)
List<String> result = new ArrayList<>();
stream.forEach(result::add);  // 병렬 스트림에서 문제!

// ✅ 수정
List<String> result = stream.collect(toList());
```

---

### Q17. 디버깅은 어떻게 하나요?

**A:** `peek()`을 활용합니다.

**방법 1: peek()으로 중간 확인**
```java
List<String> result = list.stream()
    .peek(s -> System.out.println("Original: " + s))
    .filter(s -> s.length() > 3)
    .peek(s -> System.out.println("  Filtered: " + s))
    .map(String::toUpperCase)
    .peek(s -> System.out.println("    Mapped: " + s))
    .collect(toList());
```

**방법 2: 실행 순서 확인**
```java
Stream.of("a", "bb", "ccc", "dddd")
    .peek(s -> System.out.println("1. 소스: " + s))
    .filter(s -> s.length() > 2)
    .peek(s -> System.out.println("  2. 필터 통과: " + s))
    .map(String::toUpperCase)
    .peek(s -> System.out.println("    3. 변환 완료: " + s))
    .forEach(s -> System.out.println("      4. 최종: " + s));

// 출력:
// 1. 소스: a
// 1. 소스: bb
// 1. 소스: ccc
//   2. 필터 통과: ccc
//     3. 변환 완료: CCC
//       4. 최종: CCC
// 1. 소스: dddd
//   2. 필터 통과: dddd
//     3. 변환 완료: DDDD
//       4. 최종: DDDD
```

**방법 3: IntelliJ의 Stream Trace 사용**
- IntelliJ에서 스트림 디버깅 시 "Trace Current Stream Chain" 기능 활용

---

### Q18. 성능 문제는 어떻게 찾나요?

**A:** 다음을 확인하세요.

**체크리스트:**

1. **불필요한 박싱/언박싱?**
```java
// ❌ 느림
int sum = list.stream()
    .map(Integer::intValue)
    .reduce(0, Integer::sum);

// ✅ 빠름
int sum = list.stream()
    .mapToInt(Integer::intValue)
    .sum();
```

2. **쇼트서킷 미활용?**
```java
// ❌ 느림
boolean exists = list.stream()
    .filter(condition)
    .collect(toList())
    .size() > 0;

// ✅ 빠름
boolean exists = list.stream()
    .anyMatch(condition);
```

3. **상태 있는 연산 남용?**
```java
// ❌ 느림 (여러 정렬)
stream
    .sorted()
    .filter(...)
    .sorted()  // 불필요한 정렬!

// ✅ 빠름
stream
    .filter(...)
    .sorted()  // 한 번만
```

4. **병렬 스트림 오용?**
```java
// ❌ 소량 데이터에 병렬
smallList.parallelStream()  // 오버헤드 > 이득

// ✅ 대량 데이터에 병렬
largeList.parallelStream()
```

---

## 심화 질문

### Q19. 스트림의 내부 구현은?

**A:** 주요 구성 요소는 다음과 같습니다.

**1. Pipeline 구조**
```
Head (소스) → Stage1 (중간) → Stage2 (중간) → Tail (최종)
```

**2. Spliterator**
- 스트림의 요소 순회 담당
- 병렬 처리 시 데이터 분할

**3. Sink**
- 각 연산의 실행 담당
- 체이닝으로 연결됨

**간단한 개념도:**
```java
// 개발자 코드
stream.filter(a).map(b).collect(toList())

// 내부 구조 (단순화)
class FilterStage {
    Predicate<T> predicate;
    Stage next;
    
    void process(T item) {
        if (predicate.test(item)) {
            next.process(item);
        }
    }
}

class MapStage {
    Function<T, R> mapper;
    Stage next;
    
    void process(T item) {
        R result = mapper.apply(item);
        next.process(result);
    }
}

class CollectStage {
    Collector collector;
    
    void process(T item) {
        collector.accumulate(item);
    }
}
```

---

### Q20. 함수형 프로그래밍과의 관계는?

**A:** 스트림은 Java의 **함수형 프로그래밍 도구**입니다.

**함수형 프로그래밍 원칙:**

1. **불변성 (Immutability)**
```java
// 원본을 변경하지 않음
List<String> original = Arrays.asList("a", "b", "c");
List<String> upper = original.stream()
    .map(String::toUpperCase)
    .collect(toList());

System.out.println(original);  // [a, b, c] (변경 안 됨!)
System.out.println(upper);     // [A, B, C]
```

2. **순수 함수 (Pure Functions)**
```java
// ✅ 순수 함수 (부작용 없음)
stream.map(s -> s.toUpperCase())

// ❌ 비순수 함수 (외부 상태 변경)
List<String> result = new ArrayList<>();
stream.forEach(s -> result.add(s))  // 부작용!
```

3. **선언형 프로그래밍**
```java
// 명령형: "어떻게(How)" 할지 명시
List<String> result = new ArrayList<>();
for (String s : list) {
    if (s.length() > 3) {
        result.add(s.toUpperCase());
    }
}

// 선언형: "무엇을(What)" 할지 명시
List<String> result = list.stream()
    .filter(s -> s.length() > 3)
    .map(String::toUpperCase)
    .collect(toList());
```

4. **고차 함수 (Higher-Order Functions)**
```java
// 함수를 인수로 받음
stream.filter(predicate)  // Predicate를 받음
      .map(function)      // Function을 받음
```

---

## 💡 핵심 정리

### 가장 중요한 5가지

1. **스트림은 단 한 번만 소비 가능**
2. **중간 연산은 게으르다 (Lazy)**
3. **쇼트서킷으로 성능 향상**
4. **루프 퓨전으로 최적화**
5. **병렬 스트림은 대량 데이터에만**

### 실수 방지 체크리스트

- [ ] 스트림 재사용하지 않았나?
- [ ] 최종 연산 있나?
- [ ] peek을 최종 연산으로 쓰지 않았나?
- [ ] 외부 상태 변경하지 않았나?
- [ ] 병렬 스트림을 적절히 사용했나?

---

**작성일:** 2024년  
**주제:** Java Stream Q&A  
**난이도:** 중급~고급
