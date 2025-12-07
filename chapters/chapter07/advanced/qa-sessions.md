# Chapter 07. 병렬 데이터 처리와 성능 - Q&A Sessions

> 병렬 스트림, Fork/Join, Spliterator 관련 자주 묻는 질문과 답변

---

## 📚 목차

1. [병렬 스트림 일반](#1-병렬-스트림-일반)
2. [Fork/Join 프레임워크](#2-forkjoin-프레임워크)
3. [Spliterator](#3-spliterator)
4. [성능 및 최적화](#4-성능-및-최적화)
5. [디버깅 및 문제 해결](#5-디버깅-및-문제-해결)

---

## 1. 병렬 스트림 일반

### Q1. 병렬 스트림은 언제 사용해야 하나요?

**A:** 다음 조건을 모두 만족할 때 사용하세요:

```java
✅ 1. 데이터가 충분히 많음 (N > 10,000)
✅ 2. 연산이 복잡함 (Q가 큼)
✅ 3. 적절한 자료구조 (ArrayList, 배열)
✅ 4. 공유 가변 상태 없음
✅ 5. 순서 독립적

// 좋은 예
LongStream.rangeClosed(1, 1_000_000)
    .parallel()
    .map(this::complexCalculation)  // 복잡한 연산
    .sum();

// 나쁜 예
Arrays.asList(1, 2, 3)  // 데이터 너무 적음
    .parallelStream()
    .map(x -> x + 1)     // 연산 너무 간단
    .collect(toList());
```

**핵심:** 항상 벤치마크로 측정하세요!

---

### Q2. parallel()과 sequential()을 같이 쓰면 어떻게 되나요?

**A:** **마지막 호출**이 전체 파이프라인에 영향을 미칩니다.

```java
// 순차로 실행됨
stream.parallel()
    .filter(...)
    .sequential()  // ← 마지막 호출
    .map(...)
    .collect(toList());

// 병렬로 실행됨
stream.sequential()
    .filter(...)
    .parallel()    // ← 마지막 호출
    .map(...)
    .collect(toList());
```

**이유:**
```java
// 내부적으로 플래그만 설정
boolean parallel;

stream.parallel() {
    this.parallel = true;
}

stream.sequential() {
    this.parallel = false;
}

// 실행 시점에 플래그 확인
if (parallel) {
    // 병렬 실행
} else {
    // 순차 실행
}
```

---

### Q3. Stream.iterate()는 왜 병렬로 하면 더 느린가요?

**A:** **순차 의존성** 때문입니다.

```java
// ❌ 병렬 불가능
Stream.iterate(1L, i -> i + 1)
    .limit(n)
    .parallel()  // 효과 없음!
    .reduce(0L, Long::sum);

// 왜?
// i = 1 → i = 2 → i = 3 → i = 4 → ...
//   ↑       ↑       ↑       ↑
// 이전 결과에 의존! (병렬 불가)

// ✅ 병렬 가능
LongStream.rangeClosed(1, n)
    .parallel()
    .reduce(0L, Long::sum);

// 왜?
// [1, 2, 3, 4, 5, 6, 7, 8]
//  ↓
// [1,2,3,4] | [5,6,7,8]  (독립적으로 분할 가능!)
```

**성능 차이:**
```
iterate + parallel:  80ms  ❌
rangeClosed + parallel: 1ms  ✅
```

---

### Q4. 병렬 스트림에서 forEach를 쓰면 순서가 보장되나요?

**A:** **forEachOrdered**를 사용하세요.

```java
// ❌ 순서 보장 안 됨
IntStream.range(1, 10)
    .parallel()
    .forEach(System.out::println);

// 출력: 3, 1, 7, 2, 5, 9, 4, 6, 8 (매번 다름)

// ✅ 순서 보장
IntStream.range(1, 10)
    .parallel()
    .forEachOrdered(System.out::println);

// 출력: 1, 2, 3, 4, 5, 6, 7, 8, 9 (항상 같음)
```

**주의:** `forEachOrdered`는 병렬 효과를 감소시킵니다!

---

### Q5. 병렬 스트림은 몇 개의 스레드를 사용하나요?

**A:** **CPU 코어 수 - 1**개입니다.

```java
// 확인 방법
int cores = Runtime.getRuntime().availableProcessors();
System.out.println("코어 수: " + cores);  // 8

int parallelism = ForkJoinPool.commonPool().getParallelism();
System.out.println("병렬도: " + parallelism);  // 7

// 실제 사용되는 스레드 확인
Set<String> threads = Collections.synchronizedSet(new HashSet<>());

IntStream.range(0, 100)
    .parallel()
    .forEach(i -> threads.add(Thread.currentThread().getName()));

threads.forEach(System.out::println);
// 출력:
// ForkJoinPool.commonPool-worker-1
// ForkJoinPool.commonPool-worker-2
// ...
// ForkJoinPool.commonPool-worker-7
// main  ← 메인 스레드도 참여!
```

---

## 2. Fork/Join 프레임워크

### Q6. 왜 한쪽만 fork 해야 하나요?

**A:** **현재 스레드를 활용**하기 위해서입니다.

```java
// ❌ 양쪽 다 fork (비효율)
leftTask.fork();   // Thread-2에 맡김
rightTask.fork();  // Thread-3에 맡김

Long leftResult = leftTask.join();   // Thread-1은 대기만...
Long rightResult = rightTask.join();

// Thread-1: 대기 ⏳⏳⏳ (일 안 함)
// Thread-2: leftTask 실행 ████
// Thread-3: rightTask 실행 ████
// 스레드 활용률: 87.5% (8코어 중 7개만 사용)

// ✅ 한쪽만 fork (효율적)
leftTask.fork();                      // Thread-2에 맡김
Long rightResult = rightTask.compute();  // Thread-1이 직접 처리!
Long leftResult = leftTask.join();       // Thread-2 결과 대기

// Thread-1: rightTask 직접 실행 ████ (일 함!)
// Thread-2: leftTask 실행 ████
// 스레드 활용률: 100% (8코어 모두 사용)
```

**성능 차이:**
```
양쪽 다 fork: 15ms
한쪽만 fork:  12ms
개선: 20% 빠름
```

---

### Q7. RecursiveTask 내부에서 invoke를 쓰면 안 되는 이유는?

**A:** **데드락**이 발생할 수 있습니다.

```java
// ❌ 데드락 발생!
@Override
protected Long compute() {
    ForkJoinTask<Long> leftTask = new MyTask(...);
    ForkJoinTask<Long> rightTask = new MyTask(...);
    
    // invoke 사용
    Long leftResult = ForkJoinPool.commonPool().invoke(leftTask);
    Long rightResult = ForkJoinPool.commonPool().invoke(rightTask);
    
    return leftResult + rightResult;
}

// 데드락 시나리오 (4개 스레드):
// Thread-1: invoke(task1) → 대기
// Thread-2: invoke(task2) → 대기
// Thread-3: invoke(task3) → 대기
// Thread-4: invoke(task4) → 대기
// → 모든 스레드 대기 → 아무도 작업 처리 못함 → 데드락!

// ✅ compute/fork/join 사용
@Override
protected Long compute() {
    leftTask.fork();
    Long rightResult = rightTask.compute();
    Long leftResult = leftTask.join();
    
    return leftResult + rightResult;
}
```

---

### Q8. 임계값(THRESHOLD)은 어떻게 정하나요?

**A:** **작업 특성**에 따라 다릅니다.

```java
// CPU 집약적 작업
private static final long THRESHOLD = 10_000;

// 예: 복잡한 수학 연산
for (int i = start; i < end; i++) {
    result += Math.sqrt(numbers[i]) * Math.sin(numbers[i]);
}

// 메모리 접근이 많은 작업
private static final long THRESHOLD = 100_000;

// 예: 단순 합계
for (int i = start; i < end; i++) {
    result += numbers[i];
}

// 매우 복잡한 작업
private static final long THRESHOLD = 5_000;

// 예: 암호화, 압축
for (int i = start; i < end; i++) {
    result += encrypt(numbers[i]);
}
```

**실험적 결정:**
```java
// 다양한 임계값 테스트
for (int threshold : new int[]{1_000, 10_000, 100_000}) {
    long start = System.nanoTime();
    // 실행...
    long duration = System.nanoTime() - start;
    System.out.printf("THRESHOLD=%d: %dms%n", threshold, duration / 1_000_000);
}

// 출력:
// THRESHOLD=1000:   18ms  (과도한 분할)
// THRESHOLD=10000:  12ms  ← 최적!
// THRESHOLD=100000: 15ms  (불충분한 분할)
```

---

### Q9. fork/join과 병렬 스트림의 차이는?

**A:** **추상화 수준**이 다릅니다.

| 특징 | 병렬 스트림 | Fork/Join |
|------|------------|-----------|
| **추상화** | 높음 (선언적) | 낮음 (명령적) |
| **코드 복잡도** | 간단 | 복잡 |
| **제어력** | 낮음 | 높음 |
| **적합한 경우** | 일반적 병렬 처리 | 커스텀 분할 로직 |

```java
// 병렬 스트림 (간단)
long sum = LongStream.rangeClosed(1, n)
    .parallel()
    .sum();

// Fork/Join (복잡하지만 제어 가능)
class SumTask extends RecursiveTask<Long> {
    @Override
    protected Long compute() {
        // 커스텀 분할 로직
        // 임계값 제어
        // 특수 처리
    }
}

long sum = ForkJoinPool.commonPool()
    .invoke(new SumTask(numbers, 0, numbers.length));
```

**권장:**
- 일반적 경우 → 병렬 스트림
- 특수한 경우 → Fork/Join

---

## 3. Spliterator

### Q10. Spliterator는 언제 직접 구현하나요?

**A:** **커스텀 분할 로직**이 필요할 때입니다.

```java
// 예제 1: 단어 경계에서 분할
// 기본 Spliterator는 임의 위치에서 분할
// → 단어가 잘릴 수 있음
// → 커스텀 Spliterator로 공백에서만 분할

// 예제 2: CSV 파일 파싱
// 기본 Spliterator는 바이트 단위 분할
// → 레코드가 잘릴 수 있음
// → 커스텀 Spliterator로 줄바꿈에서만 분할

// 예제 3: 배치 처리
// 기본 Spliterator는 개별 요소 처리
// → 배치 단위로 처리하고 싶음
// → 커스텀 Spliterator로 배치 생성

// 일반적인 경우는 기본 Spliterator로 충분!
list.spliterator()  // ArrayList는 이미 최적화됨
```

---

### Q11. trySplit()에서 currentChar를 왜 업데이트해야 하나요?

**A:** **중복 처리**를 방지하기 위해서입니다.

```java
@Override
public Spliterator<Character> trySplit() {
    // ...
    
    // 앞부분을 새 Spliterator로
    Spliterator<Character> newSplit = 
        new WordCounterSpliterator(
            string.substring(currentChar, splitPos)
        );
    
    // ⭐ currentChar 업데이트 (필수!)
    currentChar = splitPos;
    
    return newSplit;
}

// ❌ 업데이트 안 하면
// this.string = "Hello World"
// this.currentChar = 0  ← 그대로!

// tryAdvance() 호출 시
action.accept(string.charAt(currentChar++));
// → "Hello World" 처음부터 또 읽음! (중복!)

// ✅ 업데이트 하면
// this.string = "Hello World"
// this.currentChar = 6  ← 업데이트됨!

// tryAdvance() 호출 시
action.accept(string.charAt(currentChar++));
// → "World"만 읽음! (정확!)
```

---

### Q12. 왜 공백에서만 분할해야 하나요?

**A:** 단어가 **잘리지 않게** 하기 위해서입니다.

```java
// ❌ 단어 중간에서 분할
"HelloWorld"
     ↑ 여기서 분할

Thread-1: "Hello"
  - 초기: lastSpace = true
  - 'H' 만남 → 새 단어! counter = 1

Thread-2: "World"
  - 초기: lastSpace = true ← 문제!
  - 'W' 만남 → 새 단어! counter = 1
  
결과: 1 + 1 = 2개 ❌ (실제 1개)

// ✅ 공백에서 분할
"Hello World"
      ↑ 공백에서 분할

Thread-1: "Hello "
  - 'H' → 새 단어! counter = 1
  - ' ' → lastSpace = true

Thread-2: "World"
  - 초기: lastSpace = true ← 맞음!
  - 'W' → 새 단어! counter = 1
  
결과: 1 + 1 = 2개 ✅
```

---

### Q13. characteristics()를 정확히 선언하지 않으면?

**A:** **최적화 기회**를 놓칩니다.

```java
// ❌ 특성 선언 안 함
@Override
public int characteristics() {
    return 0;
}

// 결과:
// - toArray() 비효율 (크기 모름)
// - distinct() 실행 (중복 없는데도)
// - sorted() 실행 (정렬됐는데도)

// ✅ 정확한 특성 선언
@Override
public int characteristics() {
    return ORDERED | SIZED | SUBSIZED | DISTINCT | SORTED;
}

// 결과:
// - toArray() 최적화 (정확한 크기 할당)
// - distinct() 생략 (DISTINCT)
// - sorted() 생략 (SORTED)
```

---

## 4. 성능 및 최적화

### Q14. 병렬 스트림이 순차보다 느린 경우는?

**A:** 다음 경우들입니다:

```java
// 1. 박싱/언박싱
Stream.iterate(1L, i -> i + 1)
    .parallel()
    .reduce(0L, Long::sum);
// Long 객체 생성 → 느림!

// 2. 순차 의존
Stream.iterate(1, i -> i + 1).parallel();
// 이전 결과 필요 → 병렬 불가

// 3. 비효율적 분할
LinkedList<Integer> list = new LinkedList<>();
list.parallelStream().sum();
// O(n) 분할 → 느림!

// 4. 소량 데이터
Arrays.asList(1, 2, 3).parallelStream();
// 오버헤드 > 이득

// 5. 간단한 연산
stream.parallel().map(x -> x + 1);
// 연산 비용 < 스레드 비용

// 6. 순서 의존 연산
stream.parallel().limit(10);
// 순서 맞추느라 느림
```

---

### Q15. LinkedList는 왜 병렬 처리에 안 좋나요?

**A:** **O(n) 분할 비용** 때문입니다.

```java
// ArrayList 분할 (O(1))
class ArrayListSpliterator<E> {
    @Override
    public Spliterator<E> trySplit() {
        int mid = (start + end) >>> 1;  // 중간점 O(1)
        return new ArrayListSpliterator<>(array, start, mid);
    }
}

// 1천만 개 분할: ~1ms

// LinkedList 분할 (O(n))
class LinkedListSpliterator<E> {
    @Override
    public Spliterator<E> trySplit() {
        Node<E> midNode = current;
        for (int i = 0; i < size / 2; i++) {
            midNode = midNode.next;  // 순차 탐색!
        }
        return new LinkedListSpliterator<>(midNode, ...);
    }
}

// 1천만 개 분할: ~500ms (500배 느림!)
```

**성능 비교:**
```
ArrayList + parallel:  1ms  ⭐⭐⭐⭐⭐
LinkedList + parallel: 500ms  ⭐
LinkedList + sequential: 5ms  ⭐⭐⭐⭐

→ LinkedList는 순차가 더 빠름!
```

---

### Q16. 공유 가변 상태는 왜 위험한가요?

**A:** **Race Condition**이 발생합니다.

```java
// ❌ Race Condition 예제
class Accumulator {
    private long total = 0;
    
    public void add(long value) {
        total += value;  // 원자적 연산 아님!
    }
}

Accumulator acc = new Accumulator();

LongStream.rangeClosed(1, 1000)
    .parallel()
    .forEach(acc::add);

System.out.println(acc.total);
// 예상: 500500
// 실제: 483921 ← 틀림!

// 왜?
// total += value는 3단계:
// 1. total 읽기
// 2. value 더하기
// 3. total 쓰기

// Thread-1: total 읽기 (100)
// Thread-2: total 읽기 (100)  ← 같은 값!
// Thread-1: 100 + 50 = 150
// Thread-2: 100 + 30 = 130
// Thread-1: total 쓰기 (150)
// Thread-2: total 쓰기 (130)  ← 덮어씀!
// 결과: 30 손실!

// ✅ 해결: 불변 연산
long sum = LongStream.rangeClosed(1, 1000)
    .parallel()
    .reduce(0L, Long::sum);
// 예상: 500500
// 실제: 500500 ← 정확!
```

---

## 5. 디버깅 및 문제 해결

### Q17. 병렬 스트림 디버깅은 어떻게 하나요?

**A:** 다음 방법들을 사용하세요:

```java
// 1. 로깅 추가
stream.parallel()
    .peek(n -> System.out.printf("[%s] Processing: %d%n",
        Thread.currentThread().getName(), n))
    .forEach(...);

// 2. 순차로 먼저 검증
long sequential = stream.reduce(0L, Long::sum);
long parallel = stream.parallel().reduce(0L, Long::sum);
assert sequential == parallel;

// 3. 작은 데이터로 테스트
List<Integer> small = Arrays.asList(1, 2, 3, 4, 5);
// 10 → 100 → 10,000 → 1,000,000

// 4. 단위 테스트
@Test
public void testParallelSum() {
    List<Integer> list = IntStream.range(1, 101)
        .boxed()
        .collect(toList());
    
    long expected = 5050;
    long actual = ParallelStreamUtil.sum(list);
    
    assertEquals(expected, actual);
}

// 5. JMH 벤치마크
@Benchmark
public long parallelSum() {
    return list.parallelStream().mapToLong(x -> x).sum();
}
```

---

### Q18. ForkJoinPool이 멈춘 것 같아요. 어떻게 확인하나요?

**A:** 다음 정보를 확인하세요:

```java
ForkJoinPool pool = ForkJoinPool.commonPool();

// 1. 활성 스레드 수
int activeThreads = pool.getActiveThreadCount();
System.out.println("활성 스레드: " + activeThreads);
// 0이면 작업 완료, 7이면 모두 바쁨

// 2. 대기 중인 작업 수
long queuedTasks = pool.getQueuedTaskCount();
System.out.println("대기 작업: " + queuedTasks);
// 많으면 병목

// 3. 훔친 작업 수
long stealCount = pool.getStealCount();
System.out.println("훔친 작업: " + stealCount);
// 0이면 부하 불균형

// 4. 병렬도
int parallelism = pool.getParallelism();
System.out.println("병렬도: " + parallelism);

// 5. 스레드 덤프
jstack <PID>
// 각 스레드 상태 확인
```

**데드락 의심:**
```
모든 스레드가 WAITING 상태
→ invoke() 잘못 사용 의심
→ compute/fork/join 사용으로 변경
```

---

### Q19. 성능이 기대보다 안 나와요. 원인은?

**A:** 다음을 체크하세요:

```java
// 1. 박싱 체크
stream.boxed().parallel()  // ❌
IntStream.range(...).parallel()  // ✅

// 2. 자료구조 체크
LinkedList<Integer> list;  // ❌
ArrayList<Integer> list;   // ✅

// 3. 데이터 크기 체크
if (list.size() < 10_000) {
    // 순차 처리
}

// 4. 연산 복잡도 체크
.map(x -> x + 1)  // 간단 → 순차
.map(this::complexCalculation)  // 복잡 → 병렬

// 5. 공유 상태 체크
accumulator.add(...)  // ❌ Race Condition
.reduce(0L, Long::sum)  // ✅

// 6. 순서 의존 체크
.limit(10)  // ❌ 순서 의존
.collect(toSet())  // ✅ 순서 독립

// 7. JVM 워밍업
for (int i = 0; i < 10; i++) {
    // 실행 (JIT 컴파일)
}
// 이후 측정
```

---

### Q20. 병렬 스트림 vs CompletableFuture, 언제 뭘 쓰나요?

**A:** **작업 특성**에 따라 다릅니다.

| 특징 | 병렬 스트림 | CompletableFuture |
|------|------------|-------------------|
| **적합한 경우** | CPU 집약적 | I/O 블로킹 |
| **스레드 풀** | ForkJoinPool | 커스텀 가능 |
| **조합** | 어려움 | 쉬움 (thenCompose 등) |
| **에러 처리** | try-catch | exceptionally |

```java
// 병렬 스트림 (CPU 집약적)
List<Integer> results = numbers.parallelStream()
    .map(this::complexCalculation)  // CPU 작업
    .collect(toList());

// CompletableFuture (I/O 블로킹)
ExecutorService executor = Executors.newFixedThreadPool(20);

List<CompletableFuture<String>> futures = urls.stream()
    .map(url -> CompletableFuture.supplyAsync(
        () -> downloadContent(url),  // I/O 작업
        executor
    ))
    .collect(toList());

List<String> results = futures.stream()
    .map(CompletableFuture::join)
    .collect(toList());
```

---

## 📚 추가 자료

- [📋 CheatSheet](cheatsheet.md) - 빠른 참조
- [📖 Deep Dive](deep-dive.md) - 심화 학습
- [💻 Code](../code/) - 실전 예제

---

**작성일**: 2024년 12월  
**대상**: Modern Java In Action Chapter 7
