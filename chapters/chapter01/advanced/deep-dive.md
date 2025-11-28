# 자바 8 Deep Dive

> 개념의 본질과 내부 원리를 깊이 있게 탐구

---

## 목차

1. [왜 자바는 변화했는가](#1-왜-자바는-변화했는가)
2. [스트림 API의 내부 구조](#2-스트림-api의-내부-구조)
3. [람다의 비밀](#3-람다의-비밀)
4. [동작 파라미터화의 철학](#4-동작-파라미터화의-철학)
5. [디폴트 메서드의 탄생 배경](#5-디폴트-메서드의-탄생-배경)
6. [함수형 프로그래밍과 병렬성](#6-함수형-프로그래밍과-병렬성)

---

## 1. 왜 자바는 변화했는가

### 1.1 언어 생태계의 진화

**프로그래밍 언어 = 생태계**
```
진화하는 언어 → 생존
정체된 언어 → 도태
```

**자바의 역사:**
```
1995: 탄생 (Applet, Write Once Run Anywhere)
    ↓
2004: 제네릭 (Java 5)
    ↓
2011: 포크/조인 프레임워크 (Java 7)
    ↓
2014: 람다, 스트림 (Java 8) ← 가장 큰 변화!
```

### 1.2 하드웨어의 변화

```
단일 코어 시대 (~ 2000년대 초)
CPU 클럭 속도 향상 (1GHz → 3GHz)
    ↓
멀티 코어 시대 (2000년대 중반 ~)
듀얼/쿼드/옥타 코어
    ↓
자바의 선택:
기존 방식(Thread, synchronized) → 복잡하고 에러 많음
새로운 방식(Stream, parallelStream) → 간단하고 안전
```

**무어의 법칙의 종말:**
- CPU 클럭 속도 증가 정체
- 코어 개수 증가로 성능 향상
- → **병렬 프로그래밍이 필수!**

### 1.3 경쟁 언어들의 압박

```
C#: LINQ (2007)
    - SQL처럼 데이터 질의
    - 람다 표현식

Scala: 함수형 프로그래밍 (2004)
    - 패턴 매칭
    - 강력한 타입 추론

JavaScript: 함수형 스타일 (ES6, 2015)
    - Arrow functions
    - map, filter, reduce

→ 자바도 변해야 했다!
```

---

## 2. 스트림 API의 내부 구조

### 2.1 스트림의 본질

**스트림 ≠ 컬렉션**
```
컬렉션: 데이터를 저장하는 자료구조
    - ArrayList, HashMap 등
    - 메모리에 모든 데이터 보유

스트림: 데이터 처리 연산의 파이프라인
    - 데이터를 계산하는 과정
    - 필요할 때만 계산 (lazy evaluation)
```

### 2.2 스트림 파이프라인의 3단계

```java
list.stream()                              // 1. 생성
    .filter(x -> x > 10)                   // 2. 중간 연산 (lazy)
    .map(x -> x * 2)                       // 2. 중간 연산 (lazy)
    .collect(Collectors.toList());         // 3. 최종 연산 (실행!)
```

**중간 연산 vs 최종 연산:**
```
중간 연산 (Intermediate Operations):
- filter, map, sorted, distinct
- 스트림을 반환
- 실행되지 않음 (lazy!)

최종 연산 (Terminal Operations):
- collect, forEach, count, reduce
- 스트림이 아닌 값을 반환
- 파이프라인 실행 시작!
```

### 2.3 Lazy Evaluation (지연 평가)

```java
// 이 코드는 실제로 아무것도 실행하지 않음!
Stream<Apple> stream = inventory.stream()
    .filter(a -> {
        System.out.println("Filtering: " + a);
        return a.getWeight() > 150;
    });

// 여기서 비로소 실행!
List<Apple> result = stream.collect(Collectors.toList());
```

**왜 지연 평가?**
```
장점:
1. 최적화 가능
   - filter와 map을 하나의 과정으로 합침 (fusion)
2. 불필요한 계산 방지
   - limit(10)이 있으면 10개만 처리
3. 무한 스트림 가능
   - Stream.iterate(0, n -> n + 1).limit(100)
```

### 2.4 병렬 스트림의 내부

**Fork/Join 프레임워크:**
```
데이터
  ↓ 
포크 (분할)
  ├─ Thread 1 → 처리
  ├─ Thread 2 → 처리
  └─ Thread 3 → 처리
  ↓
조인 (합침)
  ↓
결과
```

**실제 동작:**
```java
inventory.parallelStream()
    .filter(a -> a.getWeight() > 150)
    .collect(Collectors.toList());

// 내부적으로:
// 1. inventory를 서브 리스트로 분할
// 2. 각 서브 리스트를 다른 스레드에서 filter
// 3. 결과를 하나로 합침
```

**공통 포크/조인 풀:**
```java
// 모든 parallel stream이 공유하는 풀
ForkJoinPool commonPool = ForkJoinPool.commonPool();

// 풀 크기 = 프로세서 개수 - 1
int poolSize = Runtime.getRuntime().availableProcessors() - 1;
```

---

## 3. 람다의 비밀

### 3.1 람다 ≠ 익명 클래스

**익명 클래스:**
```java
// 컴파일 시 별도의 .class 파일 생성
Runnable r1 = new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello");
    }
};
// → FilteringApples$1.class 파일 생성
```

**람다:**
```java
// .class 파일을 생성하지 않음!
Runnable r2 = () -> System.out.println("Hello");
// → invokedynamic 바이트코드 사용
```

### 3.2 invokedynamic의 마법

**바이트코드 비교:**
```
익명 클래스:
1. 새 클래스 정의 생성
2. 클래스 로딩
3. 인스턴스 생성

람다:
1. invokedynamic 명령
2. 런타임에 LambdaMetafactory가 처리
3. 필요시 최적화 (같은 람다는 재사용)
```

**invokedynamic이 하는 일:**
```java
// 람다 표현식
Predicate<Apple> p = apple -> apple.getWeight() > 150;

// invokedynamic이 내부적으로 생성 (개념적)
// 실제로는 더 최적화됨
Predicate<Apple> p = new Predicate<Apple>() {
    @Override
    public boolean test(Apple apple) {
        return lambda$main$0(apple);
    }
    
    private static boolean lambda$main$0(Apple apple) {
        return apple.getWeight() > 150;
    }
};
```

### 3.3 변수 캡처 (Variable Capture)

**람다는 final 또는 effectively final 변수만 캡처:**
```java
int threshold = 150;  // effectively final

Predicate<Apple> p = apple -> apple.getWeight() > threshold;  // ✅ OK

threshold = 200;  // ❌ 컴파일 에러!
// 람다가 캡처한 변수는 변경 불가
```

**왜 final만?**
```
람다는 다른 스레드에서 실행될 수 있음
    ↓
변수가 변경되면 동기화 문제 발생
    ↓
불변 변수만 허용 → 스레드 안전
```

### 3.4 this의 의미

```java
class Example {
    int value = 1;
    
    void test() {
        // 익명 클래스: this = 익명 클래스 자신
        Runnable r1 = new Runnable() {
            int value = 2;
            public void run() {
                System.out.println(this.value);  // 2
            }
        };
        
        // 람다: this = Example 인스턴스
        Runnable r2 = () -> {
            System.out.println(this.value);  // 1
            // 람다는 새로운 스코프를 만들지 않음!
        };
    }
}
```

---

## 4. 동작 파라미터화의 철학

### 4.1 전략 패턴 (Strategy Pattern)

**GoF 디자인 패턴:**
```
알고리즘을 캡슐화하여 런타임에 선택
```

**전통적 구현:**
```java
// 전략 인터페이스
interface Strategy {
    void execute();
}

// 구체적 전략들
class StrategyA implements Strategy {
    public void execute() { ... }
}

class StrategyB implements Strategy {
    public void execute() { ... }
}

// 컨텍스트
class Context {
    private Strategy strategy;
    
    void doSomething(Strategy strategy) {
        strategy.execute();
    }
}
```

**람다를 사용한 구현:**
```java
// 인터페이스만 정의
@FunctionalInterface
interface Strategy {
    void execute();
}

// 사용
context.doSomething(() -> System.out.println("Strategy A"));
context.doSomething(() -> System.out.println("Strategy B"));
// 클래스 정의 불필요!
```

### 4.2 템플릿 메서드 패턴

**전통적:**
```java
abstract class Algorithm {
    final void process() {
        step1();
        step2();  // 서브클래스에서 오버라이드
        step3();
    }
    
    abstract void step2();
}
```

**동작 파라미터화:**
```java
class Algorithm {
    void process(Runnable step2) {
        step1();
        step2.run();  // 동작을 파라미터로 전달!
        step3();
    }
}
```

### 4.3 실행 어라운드 (Execute Around)

**리소스 처리 패턴:**
```java
// 전통적
BufferedReader br = null;
try {
    br = new BufferedReader(new FileReader("file.txt"));
    // 실제 로직
    String line = br.readLine();
} finally {
    if (br != null) br.close();
}

// 람다 사용
public static String processFile(
        BufferedReaderProcessor p) throws IOException {
    
    try (BufferedReader br = new BufferedReader(
            new FileReader("file.txt"))) {
        return p.process(br);  // 핵심 로직만 전달!
    }
}

// 사용
String line = processFile(br -> br.readLine());
String twoLines = processFile(br -> br.readLine() + br.readLine());
```

---

## 5. 디폴트 메서드의 탄생 배경

### 5.1 진화하는 인터페이스의 딜레마

**문제:**
```java
// 자바 7
public interface List<E> {
    boolean add(E e);
    E get(int index);
    // ... 기존 메서드들
}

// 자바 8에서 sort()를 추가하고 싶다면?
public interface List<E> {
    void sort(Comparator<? super E> c);  // 추가!
}

// 💥 모든 List 구현체가 깨짐!
// - ArrayList
// - LinkedList
// - Vector
// - 전 세계의 커스텀 List 구현체들...
```

### 5.2 해결책: 디폴트 메서드

```java
public interface List<E> {
    // 디폴트 구현 제공!
    default void sort(Comparator<? super E> c) {
        Object[] a = this.toArray();
        Arrays.sort(a, (Comparator) c);
        ListIterator<E> i = this.listIterator();
        for (Object e : a) {
            i.next();
            i.set((E) e);
        }
    }
}

// ✅ 기존 구현체들은 수정 없이 사용 가능!
```

### 5.3 다이아몬드 문제

**문제:**
```java
interface A {
    default void hello() {
        System.out.println("Hello from A");
    }
}

interface B {
    default void hello() {
        System.out.println("Hello from B");
    }
}

class C implements A, B {
    // 어떤 hello()를 사용? 💥
}
```

**해결 규칙:**
```
1. 클래스가 항상 우선
   - 클래스의 메서드 > 디폴트 메서드

2. 서브인터페이스가 우선
   - B extends A일 때, B의 디폴트 > A의 디폴트

3. 명시적으로 선택
   - 충돌 시 명시적으로 선택해야 함
```

**명시적 선택:**
```java
class C implements A, B {
    @Override
    public void hello() {
        A.super.hello();  // A의 hello() 명시적 호출
    }
}
```

---

## 6. 함수형 프로그래밍과 병렬성

### 6.1 순수 함수 (Pure Function)

**정의:**
```
1. 같은 입력 → 항상 같은 출력
2. 부작용 없음 (외부 상태 변경 안 함)
```

**순수 함수 예:**
```java
// ✅ 순수 함수
public static int add(int a, int b) {
    return a + b;
}

// ✅ 순수 함수
public static boolean isHeavy(Apple apple) {
    return apple.getWeight() > 150;
}
```

**비순수 함수 예:**
```java
// ❌ 비순수: 외부 상태에 의존
int threshold = 150;
public static boolean isHeavy(Apple apple) {
    return apple.getWeight() > threshold;
}

// ❌ 비순수: 부작용 있음
List<Apple> result = new ArrayList<>();
public static boolean process(Apple apple) {
    result.add(apple);  // 외부 상태 변경!
    return true;
}
```

### 6.2 참조 투명성 (Referential Transparency)

**개념:**
```
표현식을 그 결과값으로 대체해도 프로그램 동작이 변하지 않음
```

**예:**
```java
// 참조 투명
int x = add(2, 3);
int y = 5;  // add(2, 3)을 5로 대체 가능

// 참조 불투명
int x = random();
int y = random();  // 결과가 다를 수 있음!
```

### 6.3 왜 함수형 프로그래밍이 병렬성에 좋은가?

**공유 가변 상태의 위험:**
```java
// 문제 상황
class Counter {
    private int count = 0;
    
    public void increment() {
        count++;  // 스레드 안전하지 않음!
    }
}

// 두 스레드가 동시에 increment() 호출
// Thread 1: count 읽기 (0) → +1 → 쓰기 (1)
// Thread 2: count 읽기 (0) → +1 → 쓰기 (1)
// 결과: 1 (기대값: 2) 💥
```

**함수형 해결:**
```java
// 불변 상태
class Counter {
    private final int count;
    
    public Counter(int count) {
        this.count = count;
    }
    
    public Counter increment() {
        return new Counter(count + 1);  // 새 객체 반환!
    }
}

// 스레드 안전! (각 스레드가 독립적인 객체)
```

### 6.4 스트림과 함수형의 만남

```java
// 순수 함수 사용 → 안전한 병렬화
list.parallelStream()
    .filter(x -> x > 10)     // 순수 함수
    .map(x -> x * 2)          // 순수 함수
    .collect(Collectors.toList());

// 비순수 함수 사용 → 위험!
List<Integer> result = new ArrayList<>();
list.parallelStream()
    .filter(x -> x > 10)
    .forEach(result::add);   // 💥 공유 가변 상태!
```

---

## 7. 성능과 최적화

### 7.1 스트림 vs 반복문

**언제 스트림이 느릴까?**
```java
// 작은 데이터셋: 반복문이 더 빠름
List<Integer> small = Arrays.asList(1, 2, 3, 4, 5);

// 반복문: ~100ns
for (int i : small) {
    if (i > 3) { ... }
}

// 스트림: ~500ns (오버헤드)
small.stream().filter(i -> i > 3).forEach(...);

// 큰 데이터셋: 스트림이 더 빠름 (병렬화 가능)
List<Integer> large = IntStream.range(0, 1_000_000).boxed().collect(toList());
```

### 7.2 박싱 비용

```java
// ❌ 비효율: Integer 박싱/언박싱
int sum = list.stream()
    .map(x -> x.getValue())  // Integer 반환
    .reduce(0, (a, b) -> a + b);  // 언박싱 후 계산

// ✅ 효율적: 기본형 스트림
int sum = list.stream()
    .mapToInt(X::getValue)   // IntStream 반환
    .sum();  // 박싱 없음!
```

### 7.3 병렬 스트림 사용 시 주의사항

**좋은 경우:**
```
1. 계산 집약적
   - 각 요소의 처리 시간이 긺

2. 독립적인 연산
   - 요소 간 의존성 없음

3. 큰 데이터셋
   - 분할/합치기 비용 < 병렬 이득
```

**나쁜 경우:**
```
1. 작은 데이터셋
   - 오버헤드 > 이득

2. 순서 의존적
   - sorted(), findFirst() 등

3. 공유 상태
   - synchronized 필요 → 병렬 이득 감소
```

---

## 8. 핵심 정리

### 자바 8의 설계 철학

```
1. 선언형 프로그래밍
   - How → What
   
2. 함수형 프로그래밍
   - 순수 함수, 불변성
   
3. 조합 가능성
   - 작은 함수들을 조합
   
4. 병렬성
   - 안전하고 쉬운 병렬 처리
```

### 변화의 본질

```
객체지향 + 함수형 = 멀티 패러다임
    ↓
더 표현력 있고
더 안전하고
더 빠른 코드
```

---

<div align="center">

**💡 최종 통찰**

> *"자바 8은 언어의 진화가 아니라  
> 프로그래밍 패러다임의 전환이다."*

**이 변화를 이해하면,  
더 나은 개발자가 될 수 있습니다.**

</div>
