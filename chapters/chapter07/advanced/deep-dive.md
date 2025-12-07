# Chapter 07. 병렬 데이터 처리와 성능 - Deep Dive

> 병렬 스트림, Fork/Join, Spliterator의 내부 메커니즘 완벽 분석

---

## 📚 목차

1. [병렬 스트림 내부 동작](#1-병렬-스트림-내부-동작)
2. [ForkJoinPool 메커니즘](#2-forkjoinpool-메커니즘)
3. [Work-Stealing 알고리즘](#3-work-stealing-알고리즘)
4. [Spliterator 내부 구조](#4-spliterator-내부-구조)
5. [성능 최적화 심화](#5-성능-최적화-심화)

---

## 1. 병렬 스트림 내부 동작

### 1.1 parallel() 호출 시 내부 흐름

```java
Stream<T> stream = list.stream().parallel();

// 내부적으로 일어나는 일:

1. parallel 플래그 설정
   → stream.isParallel() = true

2. Spliterator 생성
   → list.spliterator()
   → ArrayListSpliterator, LinkedListSpliterator 등

3. AbstractTask 생성
   → ReduceTask, ForEachTask 등
   → RecursiveTask 상속

4. ForkJoinPool에 제출
   → ForkJoinPool.commonPool().invoke(task)

5. Work-Stealing 실행
   → 각 스레드가 작업 분할 및 처리

6. 결과 병합
   → combiner 함수 실행
   → 최종 결과 반환
```

---

### 1.2 실제 실행 흐름 예제

```java
/**
 * LongStream.range(1, 10).parallel().sum() 실행 과정
 */

// 1단계: 병렬 플래그 설정
LongPipeline pipeline = new LongPipeline(...);
pipeline.parallel = true;

// 2단계: Spliterator 생성
Spliterator.OfLong spliterator = new RangeLongSpliterator(1, 10);

// 3단계: ReduceTask 생성
class ReduceTask extends AbstractTask<Long> {
    Spliterator.OfLong spliterator;
    BinaryOperator<Long> op = Long::sum;
    
    @Override
    public void compute() {
        // Work-Stealing 로직
    }
}

// 4단계: ForkJoinPool에 제출
Long result = ForkJoinPool.commonPool()
    .invoke(new ReduceTask(spliterator, Long::sum));

// 5단계: 병렬 실행
Thread-1: [1, 2, 3]     → sum = 6
Thread-2: [4, 5, 6]     → sum = 15
Thread-3: [7, 8, 9]     → sum = 24

// 6단계: 병합
6 + 15 + 24 = 45
```

---

### 1.3 ArrayList vs LinkedList 분할 차이

#### ArrayList 분할 (O(1))

```java
class ArrayListSpliterator<E> implements Spliterator<E> {
    
    private final Object[] array;
    private int index;      // 현재 위치
    private final int fence; // 끝 위치
    
    @Override
    public Spliterator<E> trySplit() {
        int lo = index;
        int mid = (lo + fence) >>> 1;  // 중간점 계산 O(1)
        
        if (lo >= mid) {
            return null;
        }
        
        // ✅ 인덱스만 조정 (복사 없음)
        Spliterator<E> prefix = 
            new ArrayListSpliterator<>(array, lo, mid);
        
        index = mid;  // 현재 위치 업데이트
        
        return prefix;
    }
}

// 실행 과정:
// 1차 분할: [0-9] → [0-4], [5-9]  (1ns)
// 2차 분할: [0-4] → [0-2], [3-4]  (1ns)
// 3차 분할: [5-9] → [5-7], [8-9]  (1ns)
// 총: 3ns
```

#### LinkedList 분할 (O(n))

```java
class LinkedListSpliterator<E> implements Spliterator<E> {
    
    private Node<E> current;
    private int index;
    private final int fence;
    
    @Override
    public Spliterator<E> trySplit() {
        int lo = index;
        int mid = (lo + fence) >>> 1;
        
        if (lo >= mid) {
            return null;
        }
        
        // ❌ 중간 노드까지 순차 탐색 필요!
        Node<E> midNode = current;
        for (int i = lo; i < mid; i++) {
            midNode = midNode.next;  // O(n/2)
        }
        
        Spliterator<E> prefix = 
            new LinkedListSpliterator<>(current, lo, mid);
        
        current = midNode;
        index = mid;
        
        return prefix;
    }
}

// 실행 과정:
// 1차 분할: [0-9] → 5번 탐색 (500ns)
// 2차 분할: [0-4] → 2번 탐색 (200ns)
// 3차 분할: [5-9] → 2번 탐색 (200ns)
// 총: 900ns (300배 느림!)
```

---

### 1.4 박싱/언박싱 오버헤드 분석

```java
/**
 * 박싱 비용 상세 분석
 */

// ❌ Stream<Long> (박싱)
Stream<Long> boxed = Stream.iterate(1L, i -> i + 1)
    .limit(10_000_000);

// 메모리 사용:
// - Long 객체: 24 bytes (헤더 16 + long 8)
// - 1천만 개: 240MB
// - 추가 오버헤드: 캐시 미스, GC 압력

long sum = boxed.reduce(0L, Long::sum);
// 각 reduce 연산마다:
// 1. Long → long (언박싱)
// 2. long + long
// 3. long → Long (박싱)
// → 1천만 번 반복!

// ✅ LongStream (기본형)
LongStream primitive = LongStream.rangeClosed(1, 10_000_000);

// 메모리 사용:
// - long 배열: 8 bytes
// - 1천만 개: 80MB
// - 오버헤드: 없음

long sum = primitive.reduce(0L, Long::sum);
// 각 reduce 연산:
// 1. long + long (박싱/언박싱 없음!)

// 성능 차이:
// 박싱: 80ms + 240MB + GC 압력
// 기본형: 1ms + 80MB + GC 없음
// → 80배 빠름, 3배 적은 메모리!
```

---

## 2. ForkJoinPool 메커니즘

### 2.1 ForkJoinPool 구조

```java
/**
 * ForkJoinPool 내부 구조
 */
public class ForkJoinPool extends AbstractExecutorService {
    
    // 작업자 스레드 배열
    WorkQueue[] workQueues;
    
    // 병렬도 (스레드 수)
    int parallelism;
    
    // 공용 제출 큐
    WorkQueue submissionQueue;
    
    // 통계
    volatile long stealCount;    // 훔친 작업 수
    volatile int activeCount;    // 활성 스레드 수
}

/**
 * WorkQueue (각 스레드의 작업 큐)
 */
static final class WorkQueue {
    
    // Deque (양방향 큐)
    ForkJoinTask<?>[] array;
    
    // 인덱스
    int top;     // HEAD (자신의 작업)
    int base;    // TAIL (훔칠 작업)
    
    // 소유자 스레드
    ForkJoinWorkerThread owner;
}
```

---

### 2.2 commonPool() 특징

```java
/**
 * 공용 ForkJoinPool
 */
public static ForkJoinPool commonPool() {
    return common;
}

// 특징:
// 1. 싱글톤 (전역 공유)
static final ForkJoinPool common;

// 2. 기본 병렬도
parallelism = Runtime.getRuntime().availableProcessors() - 1;
// 8코어 → 7개 스레드

// 3. 전역 설정 (권장 안 함)
System.setProperty(
    "java.util.concurrent.ForkJoinPool.common.parallelism",
    "12"
);

// 4. 데몬 스레드
// JVM 종료 시 자동 종료

// 5. 스레드 이름
// ForkJoinPool.commonPool-worker-1
// ForkJoinPool.commonPool-worker-2
// ...
```

---

### 2.3 커스텀 ForkJoinPool

```java
/**
 * 블로킹 작업용 전용 풀
 */
public class CustomPoolExample {
    
    public static void main(String[] args) throws Exception {
        List<String> urls = Arrays.asList(/* 많은 URL */);
        
        // ❌ commonPool 사용 (문제)
        List<String> contents = urls.parallelStream()
            .map(url -> {
                // 블로킹 I/O (3초 대기)
                return downloadContent(url);
            })
            .collect(toList());
        
        // → 모든 commonPool 스레드가 블로킹!
        // → 다른 병렬 스트림도 느려짐!
        
        // ✅ 커스텀 풀 사용 (해결)
        ForkJoinPool customPool = new ForkJoinPool(20);
        
        List<String> contents = customPool.submit(() ->
            urls.parallelStream()
                .map(CustomPoolExample::downloadContent)
                .collect(toList())
        ).get();
        
        // → commonPool에 영향 없음!
        // → 다른 병렬 스트림 정상 동작!
    }
    
    static String downloadContent(String url) {
        try {
            Thread.sleep(3000);  // 블로킹
            return "content";
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

---

## 3. Work-Stealing 알고리즘

### 3.1 Work-Stealing 상세 메커니즘

```java
/**
 * WorkQueue의 내부 동작
 */
static final class WorkQueue {
    
    ForkJoinTask<?>[] array;  // 작업 배열
    int top;                   // HEAD
    int base;                  // TAIL
    
    /**
     * push: 작업 추가 (fork 호출 시)
     */
    final void push(ForkJoinTask<?> task) {
        int s = top;
        array[s] = task;
        top = s + 1;  // HEAD 증가
    }
    
    /**
     * pop: 자신의 작업 가져오기 (LIFO, HEAD)
     */
    final ForkJoinTask<?> pop() {
        int s = top - 1;
        
        if (s < base) {
            return null;  // 비었음
        }
        
        top = s;
        ForkJoinTask<?> task = array[s];
        array[s] = null;
        
        return task;
    }
    
    /**
     * poll: 작업 훔치기 (FIFO, TAIL)
     */
    final ForkJoinTask<?> poll() {
        while (true) {
            int b = base;
            int t = top;
            
            if (b >= t) {
                return null;  // 비었음
            }
            
            ForkJoinTask<?> task = array[b];
            
            // CAS (Compare-And-Swap)
            if (casBase(b, b + 1)) {
                return task;  // 성공!
            }
            
            // 실패 → 다른 스레드가 먼저 훔쳐감
            // 재시도
        }
    }
}
```

---

### 3.2 LIFO vs FIFO 이유

```java
/**
 * 왜 자신의 작업은 LIFO, 훔친 작업은 FIFO?
 */

// 시나리오: Thread-1이 큰 작업을 분할
Task parent = new Task(1, 1_000_000);

parent.compute() {
    // 분할
    Task left = new Task(1, 500_000);
    Task right = new Task(500_001, 1_000_000);
    
    left.fork();   // WorkQueue에 push
    right.fork();  // WorkQueue에 push
}

// Thread-1의 WorkQueue:
// [parent] [left] [right]
//   TAIL           HEAD

// 1. Thread-1이 자신의 작업 처리 (LIFO, HEAD)
task = workQueue.pop();  // right 가져옴

// 왜 right (최근 작업)를 먼저?
// → right 관련 데이터가 캐시에 있음!
// → 캐시 히트율 ↑
// → 성능 향상!

// 2. Thread-2가 훔치기 (FIFO, TAIL)
task = workQueue.poll();  // parent 훔침

// 왜 parent (오래된 작업)를 훔침?
// → parent는 아직 분할 안 됨
// → 큰 작업 = 분할 여지 많음
// → Thread-2가 다시 분할 가능
// → 부하 분산 효과 ↑

// 3. HEAD와 충돌 방지
// Thread-1: HEAD에서 pop (right)
// Thread-2: TAIL에서 poll (parent)
// → 서로 다른 끝에서 가져감
// → 충돌 최소화!
```

---

### 3.3 Lock-Free 구현

```java
/**
 * CAS (Compare-And-Swap) 기반 Lock-Free
 */
final boolean casBase(int cmp, int val) {
    return UNSAFE.compareAndSwapInt(this, baseOffset, cmp, val);
}

// 동작 원리:
// 1. base 현재 값 읽기
int currentBase = base;  // 예: 5

// 2. 새 값 계산
int newBase = currentBase + 1;  // 6

// 3. CAS 시도
if (casBase(currentBase, newBase)) {
    // 성공! base가 여전히 5였음
    // base = 6으로 업데이트됨
    return task;
} else {
    // 실패! 다른 스레드가 먼저 변경함
    // base가 이미 6이나 7로 바뀜
    // 재시도
}

// 장점:
// 1. 락 불필요 → 데드락 없음
// 2. 빠름 (CPU 명령어 하나)
// 3. 확장성 좋음 (락 경쟁 없음)
```

---

### 3.4 Work-Stealing 효과 측정

```java
/**
 * 부하 불균형 상황에서 효과
 */
public class WorkStealingEffect {
    
    public static void main(String[] args) {
        long[] numbers = LongStream.rangeClosed(1, 10_000_000)
            .toArray();
        
        // 불균형 작업 시뮬레이션
        // 짝수는 빠름, 홀수는 느림
        long sum = Arrays.stream(numbers)
            .parallel()
            .map(n -> {
                if (n % 2 == 0) {
                    return n;  // 빠름
                } else {
                    // 느림 (복잡한 연산)
                    double result = 0;
                    for (int i = 0; i < 1000; i++) {
                        result += Math.sqrt(n);
                    }
                    return n;
                }
            })
            .sum();
        
        // Work-Stealing 없이:
        // Thread-1: [홀수] 많음 → 16ms (느림)
        // Thread-2: [짝수] 많음 → 4ms
        // Thread-3: [짝수] 많음 → 4ms
        // Thread-4: [짝수] 많음 → 4ms
        // 총 시간: 16ms
        
        // Work-Stealing 사용:
        // Thread-1: [홀수] 처리 중...
        // Thread-2: 자기 작업 끝 → Thread-1에서 훔침!
        // Thread-3: 자기 작업 끝 → Thread-1에서 훔침!
        // Thread-4: 자기 작업 끝 → Thread-1에서 훔침!
        // 총 시간: 8ms (2배 빠름!)
        
        ForkJoinPool pool = ForkJoinPool.commonPool();
        System.out.println("훔친 작업 수: " + pool.getStealCount());
        // 출력: 훔친 작업 수: 127
    }
}
```

---

## 4. Spliterator 내부 구조

### 4.1 Spliterator 인터페이스 분석

```java
/**
 * Spliterator의 핵심 메서드
 */
public interface Spliterator<T> {
    
    /**
     * tryAdvance: 요소 하나씩 처리
     * 
     * @return true면 요소 있음, false면 끝
     */
    boolean tryAdvance(Consumer<? super T> action);
    
    /**
     * trySplit: 분할
     * 
     * @return 분할된 Spliterator, null이면 분할 불가
     */
    Spliterator<T> trySplit();
    
    /**
     * estimateSize: 남은 요소 개수
     * 
     * @return 예상 개수, 무한이면 Long.MAX_VALUE
     */
    long estimateSize();
    
    /**
     * characteristics: 특성
     * 
     * @return 특성 플래그의 비트마스크
     */
    int characteristics();
    
    /**
     * getExactSizeIfKnown: 정확한 크기 (SIZED인 경우)
     */
    default long getExactSizeIfKnown() {
        return (characteristics() & SIZED) == 0 
            ? -1L 
            : estimateSize();
    }
    
    /**
     * hasCharacteristics: 특성 확인
     */
    default boolean hasCharacteristics(int characteristics) {
        return (characteristics() & characteristics) == characteristics;
    }
}
```

---

### 4.2 WordCounterSpliterator 내부 동작

```java
/**
 * WordCounterSpliterator의 상세 분석
 */
public class WordCounterSpliterator implements Spliterator<Character> {
    
    private final String string;
    private int currentChar = 0;
    
    /**
     * trySplit 내부 동작 단계별 분석
     */
    @Override
    public Spliterator<Character> trySplit() {
        // 1. 남은 크기 계산
        int currentSize = string.length() - currentChar;
        // 예: string = "Hello World Good Morning" (23자)
        //     currentChar = 0
        //     currentSize = 23
        
        // 2. 최소 크기 확인
        if (currentSize < 10) {
            return null;
        }
        // 23 >= 10 → 분할 가능
        
        // 3. 중간 지점 계산
        int splitPos = currentSize / 2 + currentChar;
        // splitPos = 23 / 2 + 0 = 11
        //            ↓
        // "Hello World Good Morning"
        //            ^
        //         11번째
        
        // 4. 공백 찾기 (단어 경계)
        for (; splitPos < string.length(); splitPos++) {
            if (Character.isWhitespace(string.charAt(splitPos))) {
                // splitPos = 11 → 'W' (공백 아님)
                // splitPos = 12 → ' ' (공백!) → 여기서 분할
                
                // 5. 앞부분 Spliterator 생성
                Spliterator<Character> newSplit = 
                    new WordCounterSpliterator(
                        string.substring(currentChar, splitPos)
                    );
                // newSplit.string = "Hello World " (0~12)
                // newSplit.currentChar = 0
                
                // 6. ⭐ 현재 위치 업데이트 (중요!)
                currentChar = splitPos;
                // this.currentChar = 12
                // this.string은 여전히 "Hello World Good Morning"
                
                // 왜 업데이트가 필요한가?
                // - 업데이트 안 하면: currentChar = 0
                // - tryAdvance()에서 string.charAt(0)부터 읽음
                // - "Hello World"를 또 읽게 됨! (중복!)
                //
                // - 업데이트 하면: currentChar = 12
                // - tryAdvance()에서 string.charAt(12)부터 읽음
                // - "Good Morning"만 읽음 (정확!)
                
                return newSplit;
            }
        }
        
        return null;
    }
    
    /**
     * tryAdvance가 currentChar를 사용하는 방식
     */
    @Override
    public boolean tryAdvance(Consumer<? super Character> action) {
        if (currentChar < string.length()) {
            // ⭐ currentChar 위치부터 읽음!
            action.accept(string.charAt(currentChar++));
            return true;
        }
        return false;
    }
}

// 실행 흐름:
// 1. 초기 상태
//    Spliterator: "Hello World Good Morning"
//    currentChar: 0
//
// 2. trySplit() 호출
//    newSplit: "Hello World " (0~12)
//    this: "Hello World Good Morning"
//          currentChar: 12 ⭐
//
// 3. Thread-1이 newSplit 처리
//    tryAdvance() → 'H', 'e', 'l', 'l', 'o', ' ', ...
//
// 4. Thread-2가 this 처리
//    tryAdvance() → currentChar=12부터!
//                → 'G', 'o', 'o', 'd', ' ', ...
//    ✅ 중복 없음!
```

---

### 4.3 단어 경계 분할의 중요성

```java
/**
 * 왜 공백에서만 분할해야 하는가?
 */

// ❌ 단어 중간에서 분할하면
String text = "HelloWorld";
//                  ↑ 여기서 분할 (공백 없음)

// Thread-1 처리: "Hello"
class WordCounter {
    int counter = 0;
    boolean lastSpace = true;  // 초기값
    
    void process(char c) {
        // 'H' 처리
        if (!Character.isWhitespace('H')) {
            if (lastSpace) {  // true
                counter++;     // counter = 1
                lastSpace = false;
            }
        }
        // 'e', 'l', 'l', 'o' 처리
        // ...
        // 최종: counter = 1, lastSpace = false
    }
}

// Thread-2 처리: "World"
class WordCounter {
    int counter = 0;
    boolean lastSpace = true;  // ⚠️ 초기값 true!
    
    void process(char c) {
        // 'W' 처리
        if (!Character.isWhitespace('W')) {
            if (lastSpace) {  // true ← 문제!
                counter++;     // counter = 1
                lastSpace = false;
            }
        }
        // 'o', 'r', 'l', 'd' 처리
        // ...
        // 최종: counter = 1, lastSpace = false
    }
}

// combine 실행
WordCounter result = combine(thread1Result, thread2Result);
// result.counter = 1 + 1 = 2 ❌ (실제로는 1개)

// ✅ 공백에서 분할하면
String text = "Hello World";
//                 ↑ 공백에서 분할

// Thread-1 처리: "Hello "
// 'H' → counter = 1
// 'e', 'l', 'l', 'o' → counter = 1
// ' ' → lastSpace = true ⭐
// 최종: counter = 1, lastSpace = true

// Thread-2 처리: "World"
// 초기: lastSpace = true (맞음!)
// 'W' → lastSpace가 true → counter = 1 ✅
// 'o', 'r', 'l', 'd' → counter = 1
// 최종: counter = 1, lastSpace = false

// combine 실행
// result.counter = 1 + 1 = 2 ✅ (정확!)
```

---

## 5. 성능 최적화 심화

### 5.1 캐시 라인과 False Sharing

```java
/**
 * False Sharing 문제
 */
class Counter {
    private long count1;  // 0번째 캐시 라인
    private long count2;  // 0번째 캐시 라인 (같은 라인!)
    
    // Thread-1이 count1 수정
    // → 캐시 라인 무효화
    // → Thread-2의 캐시에서 count2도 무효화!
    // → Thread-2가 다시 로드 (느림!)
}

/**
 * 해결: 패딩
 */
class Counter {
    private long count1;
    private long p1, p2, p3, p4, p5, p6, p7;  // 패딩 (56 bytes)
    private long count2;  // 다른 캐시 라인
    
    // Thread-1이 count1 수정
    // → count1의 캐시 라인만 무효화
    // → Thread-2의 count2는 영향 없음!
}

/**
 * Java 8: @Contended 어노테이션
 */
class Counter {
    @Contended
    private long count1;
    
    @Contended
    private long count2;
    
    // 자동으로 패딩 추가
}

// JVM 옵션 필요:
// -XX:-RestrictContended
```

---

### 5.2 JIT 컴파일 최적화

```java
/**
 * 루프 언롤링 (Loop Unrolling)
 */

// 원본 코드
for (int i = 0; i < array.length; i++) {
    sum += array[i];
}

// JIT 컴파일 후 (자동 최적화)
for (int i = 0; i < array.length; i += 4) {
    sum += array[i];
    sum += array[i + 1];
    sum += array[i + 2];
    sum += array[i + 3];
}

// 효과:
// - 루프 오버헤드 감소 (75%)
// - 파이프라인 효율 증가
// - 분기 예측 실패 감소

/**
 * 벡터화 (Vectorization)
 */
// JIT가 SIMD 명령어로 변환
// 4개 요소를 동시에 처리
sum += _mm_add_ps(array[i:i+4]);
```

---

### 5.3 성능 측정 방법론

```java
/**
 * JMH 벤치마크 베스트 프랙티스
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)    // 워밍업 충분히
@Measurement(iterations = 10, time = 1) // 측정 충분히
@Fork(value = 2, jvmArgs = {          // 2번 실행
    "-Xms4G", "-Xmx4G"                 // 힙 크기 고정
})
@State(Scope.Thread)
public class MyBenchmark {
    
    @Param({"100", "1000", "10000", "100000"})
    int size;
    
    List<Integer> list;
    
    @Setup(Level.Trial)
    public void setup() {
        list = IntStream.range(0, size)
            .boxed()
            .collect(toList());
    }
    
    @Benchmark
    public long sequentialSum() {
        return list.stream()
            .mapToLong(Integer::longValue)
            .sum();
    }
    
    @Benchmark
    public long parallelSum() {
        return list.parallelStream()
            .mapToLong(Integer::longValue)
            .sum();
    }
    
    @TearDown(Level.Invocation)
    public void tearDown() {
        System.gc();  // GC 영향 최소화
    }
}

// 실행:
// java -jar benchmarks.jar -rf json -rff results.json

// 결과 분석:
// 1. Score (평균 시간)
// 2. Error (표준 편차)
// 3. 신뢰 구간
// 4. Throughput (처리량)
```

---

## 📚 추가 자료

- [📋 CheatSheet](cheatsheet.md) - 빠른 참조 가이드
- [💬 Q&A](qa-sessions.md) - 자주 묻는 질문
- [💻 Code](../code/) - 실전 예제

---

**작성일**: 2024년 12월  
**대상**: Modern Java In Action Chapter 7
