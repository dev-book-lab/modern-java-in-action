# Chapter 07. 병렬 데이터 처리와 성능 - CheatSheet

> 병렬 스트림, Fork/Join, Spliterator 핵심 요약

---

## 🚀 병렬 스트림 Quick Reference

### 기본 사용법

```java
// 순차 → 병렬
stream.parallel()

// 병렬 → 순차
stream.sequential()

// 병렬 스트림 생성
collection.parallelStream()
```

### 성능 비교 (1천만 개)

| 방법 | 시간 | 효율 |
|------|------|------|
| **for 루프** | 3ms | ⭐⭐⭐⭐⭐ |
| **Stream.iterate + parallel** | 80ms | ❌ |
| **LongStream.range + parallel** | 1ms | ⭐⭐⭐⭐⭐ |
| **Fork/Join** | 2ms | ⭐⭐⭐⭐⭐ |

---

## ✅ 병렬 스트림 사용 기준

### 언제 사용?

```java
✅ 기본형 스트림 (IntStream, LongStream, DoubleStream)
✅ ArrayList, 배열
✅ 데이터 많음 (N > 10,000)
✅ 복잡한 연산 (Q 큼)
✅ 독립적 연산
```

### 언제 피해야?

```java
❌ LinkedList
❌ Stream.iterate
❌ 소량 데이터 (N < 10,000)
❌ 간단한 연산 (+, -)
❌ 공유 가변 상태
❌ limit, findFirst (순서 의존)
```

---

## 📊 자료구조별 병렬 성능

| 자료구조 | 분할 | 효율 | 비고 |
|---------|------|------|------|
| **ArrayList** | O(1) | 95% | 최고 ⭐⭐⭐⭐⭐ |
| **배열** | O(1) | 95% | 최고 ⭐⭐⭐⭐⭐ |
| **IntStream.range** | O(1) | 95% | 최고 ⭐⭐⭐⭐⭐ |
| **HashSet** | O(1) | 60% | 불균등 ⭐⭐⭐ |
| **TreeSet** | O(log n) | 80% | 좋음 ⭐⭐⭐⭐ |
| **LinkedList** | O(n) | 20% | 나쁨 ⭐ |
| **Stream.iterate** | 불가 | 0% | 최악 ❌ |

---

## 🔧 Fork/Join 프레임워크

### RecursiveTask 템플릿

```java
public class MyTask extends RecursiveTask<Result> {
    
    private static final long THRESHOLD = 10_000;
    
    @Override
    protected Result compute() {
        if (작업 크기 <= THRESHOLD) {
            return 순차_처리();
        }
        
        MyTask left = new MyTask(왼쪽);
        MyTask right = new MyTask(오른쪽);
        
        left.fork();                      // 비동기
        Result rightResult = right.compute();  // 동기
        Result leftResult = left.join();       // 대기
        
        return combine(leftResult, rightResult);
    }
}

// 사용
Result result = ForkJoinPool.commonPool().invoke(task);
```

### 핵심 패턴

```java
✅ 한쪽만 fork
leftTask.fork();
rightResult = rightTask.compute();  // 현재 스레드 활용
leftResult = leftTask.join();

❌ 양쪽 다 fork (비효율)
leftTask.fork();
rightTask.fork();
// 현재 스레드 낭비!

❌ RecursiveTask 내부에서 invoke (데드락)
ForkJoinPool.commonPool().invoke(task);
```

### 임계값 가이드

```java
CPU 집약적:    10,000
메모리 접근:   100,000
간단한 연산:   50,000
복잡한 연산:   5,000
```

---

## 🔀 Spliterator

### 핵심 메서드

```java
// 1. 요소 하나씩 처리
boolean tryAdvance(Consumer<T> action)

// 2. 분할
Spliterator<T> trySplit()

// 3. 남은 개수
long estimateSize()

// 4. 특성
int characteristics()
```

### Characteristics

| 특성 | 의미 | 효과 |
|------|------|------|
| **ORDERED** | 순서 있음 | 순서 유지 |
| **DISTINCT** | 중복 없음 | distinct() 생략 |
| **SORTED** | 정렬됨 | sorted() 생략 |
| **SIZED** | 정확한 크기 | 배열 최적화 |
| **SUBSIZED** | 분할 후 크기 O(1) | 병렬 최적화 |
| **NONNULL** | null 없음 | null 체크 생략 |
| **IMMUTABLE** | 불변 | 동시 수정 안전 |

### 구현 템플릿

```java
public class MySpliterator<T> implements Spliterator<T> {
    
    private int currentIndex;
    private final int fence;
    
    @Override
    public boolean tryAdvance(Consumer<T> action) {
        if (currentIndex < fence) {
            action.accept(get(currentIndex++));
            return true;
        }
        return false;
    }
    
    @Override
    public Spliterator<T> trySplit() {
        int remaining = fence - currentIndex;
        
        if (remaining < 10) return null;
        
        int mid = currentIndex + remaining / 2;
        Spliterator<T> prefix = new MySpliterator<>(currentIndex, mid);
        
        currentIndex = mid;  // ⭐ 필수!
        
        return prefix;
    }
    
    @Override
    public long estimateSize() {
        return fence - currentIndex;
    }
    
    @Override
    public int characteristics() {
        return ORDERED | SIZED | SUBSIZED;
    }
}
```

---

## ⚡ 성능 최적화

### 박싱 피하기

```java
❌ Stream<Long>
Stream.iterate(1L, i -> i + 1)
    .limit(n)
    .parallel()
    .reduce(0L, Long::sum);

✅ LongStream
LongStream.rangeClosed(1, n)
    .parallel()
    .reduce(0L, Long::sum);

차이: 80배 빠름!
```

### 공유 가변 상태 피하기

```java
❌ 잘못된 예
Accumulator acc = new Accumulator();
stream.parallel().forEach(acc::add);  // Race Condition!

✅ 올바른 예
long sum = stream.parallel()
    .reduce(0L, Long::sum);  // 불변 연산
```

### ForkJoinPool 설정

```java
// 기본값 (권장)
ForkJoinPool.commonPool()

// 커스텀 풀 (블로킹 작업 시)
ForkJoinPool customPool = new ForkJoinPool(20);
customPool.submit(() -> 
    stream.parallel()...
).get();
```

---

## 🐛 디버깅 팁

### 순차로 먼저 검증

```java
// 1. 순차 테스트
long seq = stream.reduce(0L, Long::sum);

// 2. 병렬 테스트
long par = stream.parallel().reduce(0L, Long::sum);

// 3. 검증
assert seq == par;
```

### 로깅 추가

```java
stream.parallel()
    .peek(n -> System.out.printf("[%s] %d%n",
        Thread.currentThread().getName(), n))
    .forEach(...);
```

### 작은 데이터로 테스트

```java
10 → 100 → 10,000 → 1,000,000
```

---

## 📝 빠른 체크리스트

### 병렬 스트림 체크리스트

```
□ 기본형 스트림 사용?
□ ArrayList/배열 사용?
□ 데이터 10,000개 이상?
□ 복잡한 연산?
□ 공유 가변 상태 없음?
□ 순서 의존 연산 없음?
□ 성능 측정 완료?
```

### Fork/Join 체크리스트

```
□ 한쪽만 fork?
□ 적절한 임계값 (10,000~100,000)?
□ compute/fork/join 사용?
□ invoke 사용 안 함?
□ 균등 분할?
```

### Spliterator 체크리스트

```
□ tryAdvance 구현?
□ trySplit에서 currentIndex 업데이트?
□ characteristics 정확히 선언?
□ estimateSize 구현?
□ 단어 경계에서 분할? (텍스트 처리 시)
```

---

## 🎯 핵심 공식

### 병렬 처리 효과

```
전체 비용 = N * Q

N: 처리할 요소 수
Q: 하나의 요소 처리 비용

N ↑ + Q ↑ → 병렬 처리 효과 ↑
```

### Work-Stealing

```
자신의 작업: HEAD에서 LIFO (캐시 효율)
훔친 작업: TAIL에서 FIFO (충돌 최소화)
```

### 병렬도

```
기본 병렬도 = CPU 코어 수 - 1
```

---

## 🔥 주의사항

### 피해야 할 것

```java
❌ 1. iterate + parallel
Stream.iterate(...).parallel()

❌ 2. LinkedList + parallel
linkedList.parallelStream()

❌ 3. 공유 가변 상태
accumulator.add(...)

❌ 4. 양쪽 다 fork
left.fork(); right.fork();

❌ 5. RecursiveTask에서 invoke
pool.invoke(task)

❌ 6. 블로킹 작업 in commonPool
urls.parallelStream().map(this::download)
```

---

## 📚 추가 자료

- [📖 Deep Dive](deep-dive.md) - 내부 메커니즘 상세 분석
- [💬 Q&A](qa-sessions.md) - 자주 묻는 질문과 답변
- [💻 Code](../code/) - 실전 예제 코드

---

**작성일**: 2024년 12월  
**대상**: Modern Java In Action Chapter 7
