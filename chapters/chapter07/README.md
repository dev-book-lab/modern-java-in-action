<div align="center">

# Chapter 07. 병렬 데이터 처리와 성능

**"멀티코어를 활용한 효율적인 병렬 처리와 성능 최적화"**

> *병렬 스트림, Fork/Join 프레임워크, Spliterator를 마스터하여 고성능 병렬 프로그래밍 구현*

[📖 Deep Dive](advanced/deep-dive.md) | [💻 Code](code/) | [📋 CheatSheet](advanced/cheatsheet.md) | [💬 Q&A](advanced/qa-sessions.md)

</div>

---

## 📚 목차

1. [병렬 스트림](#1-병렬-스트림)
2. [포크/조인 프레임워크](#2-포크조인-프레임워크)
3. [Spliterator 인터페이스](#3-spliterator-인터페이스)
4. [실전 예제](#4-실전-예제)
5. [핵심 정리](#5-핵심-정리)

---

## 1. 병렬 스트림

### 1.1 병렬 스트림이란?

병렬 스트림은 각각의 스레드에서 처리할 수 있도록 **스트림 요소를 여러 청크로 분할한 스트림**이다. 멀티코어 프로세서가 각 청크를 처리하도록 할당할 수 있다.

```java
// 순차 스트림
public long sequentialSum(long n) {
    return Stream.iterate(1L, i -> i + 1)
        .limit(n)
        .reduce(0L, Long::sum);
}

// 병렬 스트림
public long parallelSum(long n) {
    return Stream.iterate(1L, i -> i + 1)
        .limit(n)
        .parallel()  // 병렬 스트림으로 변환
        .reduce(0L, Long::sum);
}
```

### 1.2 순차 스트림 ↔ 병렬 스트림 변환

```java
// 순차 → 병렬
stream.parallel()

// 병렬 → 순차
stream.sequential()

// ⚠️ 마지막 호출이 전체 파이프라인에 영향
stream.parallel()
    .filter(...)
    .sequential()  // 순차로 실행됨
    .map(...)
```

**핵심:**
- `parallel()`과 `sequential()` 중 **최종적으로 호출된 메서드**가 전체 파이프라인에 영향을 미친다.

---

### 1.3 병렬 스트림의 성능

#### ❌ 나쁜 예: Stream.iterate

```java
// 느림! (순차보다 5배 느림)
public long parallelSum(long n) {
    return Stream.iterate(1L, i -> i + 1)
        .limit(n)
        .parallel()
        .reduce(0L, Long::sum);
}
```

**문제점:**
1. **박싱/언박싱 오버헤드**: `Long` 객체 생성
2. **순차적 특성**: `iterate`는 이전 연산 결과에 의존 → 병렬화 어려움
3. **분할 불가**: 전체 리스트가 준비되지 않아 청크 분할 불가

#### ✅ 좋은 예: LongStream.rangeClosed

```java
// 빠름! (순차보다 4배 빠름)
public long parallelRangedSum(long n) {
    return LongStream.rangeClosed(1, n)
        .parallel()
        .reduce(0L, Long::sum);
}
```

**장점:**
1. **기본형 사용**: 박싱/언박싱 없음
2. **쉬운 분할**: 숫자 범위를 균등하게 청크로 분할 가능
3. **독립적**: 각 요소가 독립적이라 병렬 처리 효율적

---

### 1.4 병렬 스트림 사용 가이드

#### ✅ 병렬 스트림이 효과적인 경우

```java
// 1. 기본형 스트림 (IntStream, LongStream, DoubleStream)
LongStream.rangeClosed(1, n).parallel().sum();

// 2. ArrayList, 배열 (분할 쉬움)
List<Integer> list = new ArrayList<>();
list.parallelStream().filter(...).collect(toList());

// 3. Q가 큰 경우 (하나의 요소 처리 비용이 높음)
expensiveData.parallelStream()
    .map(this::complexCalculation)  // 복잡한 연산
    .collect(toList());
```

#### ❌ 병렬 스트림이 비효율적인 경우

```java
// 1. LinkedList (분할 어려움 - O(n) 순차 탐색 필요)
LinkedList<Integer> linkedList = new LinkedList<>();
linkedList.parallelStream().sum();  // 느림!

// 2. iterate (순차 의존)
Stream.iterate(1, i -> i + 1).parallel().sum();  // 느림!

// 3. 소량의 데이터
List<Integer> small = Arrays.asList(1, 2, 3);
small.parallelStream().sum();  // 오버헤드만 발생

// 4. limit, findFirst (순서 의존)
stream.parallel().limit(10);  // 비효율적

// 5. 공유 가변 상태
Accumulator acc = new Accumulator();
LongStream.rangeClosed(1, n)
    .parallel()
    .forEach(acc::add);  // Race Condition! 틀린 결과!
```

---

### 1.5 병렬 스트림의 스레드 풀

```java
// 기본값: Runtime.getRuntime().availableProcessors() - 1
ForkJoinPool.commonPool()

// 전역 설정 (권장하지 않음)
System.setProperty(
    "java.util.concurrent.ForkJoinPool.common.parallelism", 
    "12"
);

// 커스텀 풀 사용 (권장)
ForkJoinPool customPool = new ForkJoinPool(4);
customPool.submit(() -> 
    stream.parallel().reduce(...)
).get();
```

---

### 1.6 자료구조별 병렬 성능

| 자료구조 | 분할 성능 | 병렬 효율 | 비고 |
|---------|----------|----------|------|
| **ArrayList** | O(1) | ⭐⭐⭐⭐⭐ | 최고 |
| **배열** | O(1) | ⭐⭐⭐⭐⭐ | 최고 |
| **IntStream.range** | O(1) | ⭐⭐⭐⭐⭐ | 최고 |
| **HashSet** | O(1) | ⭐⭐⭐ | 불균등 분할 |
| **TreeSet** | O(log n) | ⭐⭐⭐⭐ | 균등 분할 |
| **LinkedList** | O(n) | ⭐ | 매우 느림 |
| **Stream.iterate** | 불가능 | ❌ | 병렬 불가 |

---

## 2. 포크/조인 프레임워크

### 2.1 포크/조인이란?

**병렬화할 수 있는 작업을 재귀적으로 작은 작업으로 분할**한 다음, 서브태스크 각각의 결과를 합쳐서 전체 결과를 만드는 프레임워크.

```
분할 정복 알고리즘의 병렬화 버전

         [1~100]
        /        \
    [1~50]      [51~100]
    /    \       /     \
[1~25][26~50][51~75][76~100]
```

---

### 2.2 RecursiveTask 사용

```java
public class ForkJoinSumCalculator extends RecursiveTask<Long> {
    
    private static final long THRESHOLD = 10_000;  // 임계값
    
    private final long[] numbers;
    private final int start;
    private final int end;
    
    public ForkJoinSumCalculator(long[] numbers, int start, int end) {
        this.numbers = numbers;
        this.start = start;
        this.end = end;
    }
    
    @Override
    protected Long compute() {
        int length = end - start;
        
        // 임계값 이하면 순차 처리
        if (length <= THRESHOLD) {
            return computeSequentially();
        }
        
        // 분할
        ForkJoinSumCalculator leftTask = 
            new ForkJoinSumCalculator(numbers, start, start + length / 2);
        
        leftTask.fork();  // 비동기 실행
        
        ForkJoinSumCalculator rightTask = 
            new ForkJoinSumCalculator(numbers, start + length / 2, end);
        
        Long rightResult = rightTask.compute();  // 동기 실행
        Long leftResult = leftTask.join();       // 결과 대기
        
        return leftResult + rightResult;
    }
    
    private long computeSequentially() {
        long sum = 0;
        for (int i = start; i < end; i++) {
            sum += numbers[i];
        }
        return sum;
    }
}

// 사용
long[] numbers = LongStream.rangeClosed(1, n).toArray();
ForkJoinTask<Long> task = new ForkJoinSumCalculator(numbers, 0, numbers.length);
long result = ForkJoinPool.commonPool().invoke(task);
```

---

### 2.3 포크/조인 프레임워크 사용법

#### ✅ 올바른 사용

```java
@Override
protected Long compute() {
    // 1. 한쪽만 fork (효율적)
    leftTask.fork();
    Long rightResult = rightTask.compute();  // 현재 스레드 활용
    Long leftResult = leftTask.join();
    
    return leftResult + rightResult;
}
```

#### ❌ 잘못된 사용

```java
@Override
protected Long compute() {
    // 1. 양쪽 다 fork (비효율적 - 현재 스레드 낭비)
    leftTask.fork();
    rightTask.fork();
    Long leftResult = leftTask.join();
    Long rightResult = rightTask.join();
    
    // 2. RecursiveTask 내부에서 invoke 사용 (데드락 위험)
    ForkJoinPool.commonPool().invoke(task);  // ❌
    
    // 3. join 전에 fork 안 함 (순차 실행됨)
    Long leftResult = leftTask.compute();
    Long rightResult = rightTask.compute();
    
    return leftResult + rightResult;
}
```

---

### 2.4 Work-Stealing 알고리즘

**핵심:** 유휴 스레드가 바쁜 스레드의 작업을 훔쳐옴.

```
각 스레드는 Deque(덱) 보유:

Thread-1의 Deque:
┌─────────────────────────────┐
│ [T1] [T2] [T3] [T4] [T5]    │
└─────────────────────────────┘
   ↑                        ↑
  TAIL                    HEAD
   ↑                        ↑
훔침(FIFO)               자신(LIFO)

동작:
1. 자신의 작업: HEAD에서 LIFO (캐시 효율)
2. 훔친 작업: TAIL에서 FIFO (충돌 최소화)
3. Lock-Free (CAS 사용)
```

**장점:**
- ✅ 모든 스레드가 바쁘게 유지 → CPU 100% 활용
- ✅ 동적 부하 분산 (자동)
- ✅ 데드락 없음 (Lock-Free)

---

## 3. Spliterator 인터페이스

### 3.1 Spliterator란?

**Spliterator = Splitable Iterator (분할 가능한 반복자)**

병렬 처리를 위한 요소 탐색 및 분할 인터페이스.

```java
public interface Spliterator<T> {
    boolean tryAdvance(Consumer<? super T> action);  // 요소 하나씩 처리
    Spliterator<T> trySplit();                       // 분할
    long estimateSize();                             // 남은 요소 개수
    int characteristics();                           // 특성
}
```

---

### 3.2 핵심 메서드

#### tryAdvance - 요소 하나씩 처리

```java
Spliterator<String> spliterator = list.spliterator();

while (spliterator.tryAdvance(System.out::println)) {
    // 다음 요소 처리
}
```

#### trySplit - 분할

```java
Spliterator<Integer> spliterator = list.spliterator();

Spliterator<Integer> split1 = spliterator.trySplit();  // 절반
Spliterator<Integer> split2 = spliterator;              // 나머지

// 각각 다른 스레드에서 처리
Thread-1: split1.forEachRemaining(this::process);
Thread-2: split2.forEachRemaining(this::process);
```

---

### 3.3 Characteristics (특성)

| 특성 | 의미 | 영향 |
|------|------|------|
| **ORDERED** | 순서 있음 | 순서 유지 |
| **DISTINCT** | 중복 없음 | `distinct()` 생략 가능 |
| **SORTED** | 정렬됨 | `sorted()` 생략 가능 |
| **SIZED** | 정확한 크기 | 배열 크기 최적화 |
| **SUBSIZED** | 분할 후에도 크기 O(1) | 병렬 처리 최적화 |
| **NONNULL** | null 없음 | null 체크 생략 |
| **IMMUTABLE** | 불변 | 동시 수정 안전 |
| **CONCURRENT** | 동시 수정 안전 | 락 없이 안전 |

---

### 3.4 커스텀 Spliterator 구현

#### 단어 카운터 예제

```java
public class WordCounterSpliterator implements Spliterator<Character> {
    
    private final String string;
    private int currentChar = 0;
    
    public WordCounterSpliterator(String string) {
        this.string = string;
    }
    
    @Override
    public boolean tryAdvance(Consumer<? super Character> action) {
        if (currentChar < string.length()) {
            action.accept(string.charAt(currentChar++));
            return true;
        }
        return false;
    }
    
    @Override
    public Spliterator<Character> trySplit() {
        int currentSize = string.length() - currentChar;
        
        // 너무 작으면 분할 안 함
        if (currentSize < 10) {
            return null;
        }
        
        // 공백(단어 경계)에서만 분할
        for (int splitPos = currentSize / 2 + currentChar; 
             splitPos < string.length(); 
             splitPos++) {
            
            if (Character.isWhitespace(string.charAt(splitPos))) {
                Spliterator<Character> newSplit = 
                    new WordCounterSpliterator(
                        string.substring(currentChar, splitPos)
                    );
                
                currentChar = splitPos;
                return newSplit;
            }
        }
        
        return null;
    }
    
    @Override
    public long estimateSize() {
        return string.length() - currentChar;
    }
    
    @Override
    public int characteristics() {
        return ORDERED | SIZED | SUBSIZED | NONNULL | IMMUTABLE;
    }
}

// 사용
String text = "The quick brown fox jumps over the lazy dog";
Spliterator<Character> spliterator = new WordCounterSpliterator(text);

Stream<Character> stream = StreamSupport.stream(spliterator, true);  // parallel

int wordCount = stream.reduce(
    new WordCounter(0, true),
    WordCounter::accumulate,
    WordCounter::combine
).getCounter();

System.out.println("단어 수: " + wordCount);  // 9
```

**핵심:**
- 공백에서만 분할 → 단어가 안 잘림
- `currentChar` 업데이트 → 중복 처리 방지
- 특성 정확히 선언 → 최적화

---

## 4. 실전 예제

### 4.1 성능 비교 (1천만 개 합계)

```java
// JMH 벤치마크 결과
Benchmark                          Mode  Score
iterativeSum                       avgt   3 ms  ⭐⭐⭐⭐⭐
sequentialSum (iterate)            avgt  50 ms  ⭐
parallelSum (iterate)              avgt  80 ms  ❌ (더 느림!)
rangedSum                          avgt   5 ms  ⭐⭐⭐⭐
parallelRangedSum                  avgt   1 ms  ⭐⭐⭐⭐⭐
forkJoinSum                        avgt   2 ms  ⭐⭐⭐⭐⭐
```

**결론:**
- 기본형 스트림 + 병렬: **최고 성능** ⭐⭐⭐⭐⭐
- iterate + 병렬: **최악** (순차보다 느림!) ❌
- 전통적 for문: **여전히 빠름** ⭐⭐⭐⭐⭐

---

### 4.2 공유 가변 상태의 위험

```java
// ❌ 잘못된 예
public class Accumulator {
    private long total = 0;
    
    public void add(long value) {
        total += value;  // Race Condition!
    }
}

long result = LongStream.rangeClosed(1, n)
    .parallel()
    .forEach(accumulator::add);

// 결과: 매번 다름! (5000050000이 아닌 이상한 값)
// 실행 1: 3829583942
// 실행 2: 4281394820
// 실행 3: 2983729384
```

**해결:**
```java
// ✅ 올바른 예 (불변 객체)
long result = LongStream.rangeClosed(1, n)
    .parallel()
    .reduce(0L, Long::sum);  // 불변 연산
```

---

## 5. 핵심 정리

### 5.1 병렬 스트림 사용 기준

```
✅ 병렬 스트림이 좋은 경우:
1. 기본형 스트림 (IntStream, LongStream)
2. ArrayList, 배열 (분할 O(1))
3. 연산이 복잡함 (Q가 큼)
4. 데이터가 많음 (N이 큼)
5. 독립적 연산 (상태 공유 없음)

❌ 병렬 스트림이 나쁜 경우:
1. LinkedList (분할 O(n))
2. iterate (순차 의존)
3. 소량 데이터
4. limit, findFirst (순서 의존)
5. 공유 가변 상태
```

---

### 5.2 포크/조인 프레임워크

```java
핵심 패턴:

@Override
protected Result compute() {
    if (작업이 충분히 작으면) {
        순차 처리
    } else {
        좌측 서브태스크 생성
        우측 서브태스크 생성
        
        leftTask.fork();                    // 비동기
        Result rightResult = rightTask.compute();  // 동기
        Result leftResult = leftTask.join();       // 대기
        
        return combine(leftResult, rightResult);
    }
}

주의사항:
✅ 한쪽만 fork (효율적)
✅ compute나 fork 사용
❌ 양쪽 다 fork (비효율적)
❌ RecursiveTask 내에서 invoke (데드락)
```

---

### 5.3 Spliterator

```java
구현 단계:

1. tryAdvance 구현
   - 다음 요소 반환
   - Consumer 호출

2. trySplit 구현 (병렬용)
   - 분할 가능 여부 확인
   - 절반으로 나누기
   - currentChar 업데이트 중요!

3. estimateSize 구현
   - 남은 요소 개수

4. characteristics 구현
   - 정확한 특성 선언
   - 최적화 힌트
```

---

### 5.4 성능 최적화 체크리스트

```
□ 기본형 스트림 사용 (박싱 방지)
□ 자료구조 선택 (ArrayList > LinkedList)
□ 병렬화 효과 측정 (JMH 벤치마크)
□ 공유 가변 상태 제거
□ 적절한 임계값 설정 (THRESHOLD)
□ 순서 의존 연산 피하기 (limit, findFirst)
□ Work-Stealing 활용 (한쪽만 fork)
□ 특성 정확히 선언 (Spliterator)
```

---

**작성일**: 2024년 12월  
**대상**: Modern Java In Action Chapter 7  
**난이도**: ⭐⭐⭐⭐ (중급~고급)
