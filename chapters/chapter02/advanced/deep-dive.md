# 동작 파라미터화 Deep Dive

> 개념의 본질과 내부 원리를 깊이 있게 탐구

---

## 목차

1. [동작 파라미터화의 철학](#1-동작-파라미터화의-철학)
2. [전략 패턴의 이해](#2-전략-패턴의-이해)
3. [함수형 인터페이스의 원리](#3-함수형-인터페이스의-원리)
4. [람다와 익명 클래스의 차이](#4-람다와-익명-클래스의-차이)
5. [Comparator의 내부 구현](#5-comparator의-내부-구현)
6. [스레드와 동작 파라미터화](#6-스레드와-동작-파라미터화)

---

## 1. 동작 파라미터화의 철학

### 1.1 문제의 본질

전통적인 프로그래밍에서는 **데이터**만 전달할 수 있었습니다:

```java
// 데이터만 전달
int sum(int a, int b) {
    return a + b;
}
```

하지만 현대 프로그래밍에서는 **동작(behavior)** 도 전달하고 싶습니다:

```java
// 동작을 전달하고 싶다!
List<T> process(List<T> data, ??? whatToDo) {
    // whatToDo를 어떻게 표현?
}
```

### 1.2 해결책: 동작을 객체로

자바는 객체 지향 언어이므로, **동작을 객체로 감싸서** 전달합니다:

```java
// 동작을 인터페이스로 정의
interface Behavior<T> {
    void execute(T item);
}

// 동작을 전달
void process(List<T> data, Behavior<T> behavior) {
    for (T item : data) {
        behavior.execute(item);  // 전달받은 동작 실행!
    }
}
```

이것이 **동작 파라미터화**의 핵심입니다.

### 1.3 왜 이것이 혁명적인가?

```java
// ❌ 전통적 방식: 동작마다 메서드 작성
printAllItems(list);
saveAllItems(list);
sendAllItems(list);

// ✅ 동작 파라미터화: 하나의 메서드로 모든 동작
process(list, item -> System.out.println(item));  // 출력
process(list, item -> save(item));                // 저장
process(list, item -> send(item));                // 전송
```

**코드 중복이 사라지고, 유연성이 극대화됩니다.**

---

## 2. 전략 패턴의 이해

### 2.1 전략 패턴이란?

**전략(Strategy) 패턴**은 알고리즘을 캡슐화하여 런타임에 선택할 수 있게 하는 디자인 패턴입니다.

```
┌──────────────┐
│   Context    │ ─────▶ Strategy Interface
└──────────────┘              │
                               ├─▶ ConcreteStrategyA
                               ├─▶ ConcreteStrategyB
                               └─▶ ConcreteStrategyC
```

### 2.2 구조

```java
// 1. Strategy Interface (전략 인터페이스)
interface ApplePredicate {
    boolean test(Apple apple);
}

// 2. Concrete Strategies (구체적 전략들)
class GreenApplePredicate implements ApplePredicate {
    public boolean test(Apple apple) {
        return apple.getColor() == GREEN;
    }
}

class HeavyApplePredicate implements ApplePredicate {
    public boolean test(Apple apple) {
        return apple.getWeight() > 150;
    }
}

// 3. Context (전략을 사용하는 곳)
class AppleFilter {
    public List<Apple> filter(List<Apple> inventory, ApplePredicate strategy) {
        List<Apple> result = new ArrayList<>();
        for (Apple apple : inventory) {
            if (strategy.test(apple)) {  // 전략 실행!
                result.add(apple);
            }
        }
        return result;
    }
}
```

### 2.3 전략 패턴의 장점

1. **개방-폐쇄 원칙(OCP)**: 기존 코드 수정 없이 새로운 전략 추가
2. **단일 책임 원칙(SRP)**: 각 전략은 하나의 책임만
3. **런타임 선택**: 실행 시점에 동작 결정

```java
// 런타임에 전략 선택
ApplePredicate strategy;
if (userChoice.equals("green")) {
    strategy = new GreenApplePredicate();
} else {
    strategy = new HeavyApplePredicate();
}

List<Apple> result = filter(inventory, strategy);
```

---

## 3. 함수형 인터페이스의 원리

### 3.1 함수형 인터페이스란?

**정확히 하나의 추상 메서드**를 가진 인터페이스입니다.

```java
@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);  // 유일한 추상 메서드
    
    // default 메서드는 여러 개 가능
    default Predicate<T> and(Predicate<? super T> other) { ... }
    default Predicate<T> or(Predicate<? super T> other) { ... }
    default Predicate<T> negate() { ... }
}
```

### 3.2 왜 "하나의" 추상 메서드인가?

람다 표현식이 **어떤 메서드를 구현하는지 명확히** 하기 위해서입니다:

```java
// 추상 메서드가 1개 → 람다가 이 메서드를 구현한다는 것이 명확!
Predicate<Apple> p = apple -> apple.getWeight() > 150;
// test() 메서드를 구현

// 만약 추상 메서드가 2개라면?
interface TwoMethods {
    void method1();
    void method2();
}

// ❌ 컴파일 에러! 람다가 어떤 메서드를 구현하는지 모호함
TwoMethods tm = () -> System.out.println("?");
```

### 3.3 Java의 주요 함수형 인터페이스

```java
// java.util.function 패키지

@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);  // T → boolean
}

@FunctionalInterface
public interface Consumer<T> {
    void accept(T t);   // T → void
}

@FunctionalInterface
public interface Function<T, R> {
    R apply(T t);       // T → R
}

@FunctionalInterface
public interface Supplier<T> {
    T get();            // () → T
}
```

### 3.4 내부 동작 원리

람다는 컴파일 시 **익명 클래스가 아닌 invokedynamic** 으로 변환됩니다:

```java
// 소스 코드
Predicate<Apple> p = apple -> apple.getWeight() > 150;

// 컴파일러가 내부적으로 생성 (실제는 invokedynamic)
// 개념적으로 이해하면:
Predicate<Apple> p = new Predicate<Apple>() {
    @Override
    public boolean test(Apple apple) {
        return apple.getWeight() > 150;
    }
};
```

**invokedynamic의 장점:**
- 익명 클래스보다 성능 우수
- 메모리 효율적
- JVM이 최적화 가능

---

## 4. 람다와 익명 클래스의 차이

### 4.1 문법 차이

```java
// 익명 클래스
Comparator<Apple> c1 = new Comparator<Apple>() {
    @Override
    public int compare(Apple a1, Apple a2) {
        return Integer.compare(a1.getWeight(), a2.getWeight());
    }
};

// 람다
Comparator<Apple> c2 = (a1, a2) -> 
    Integer.compare(a1.getWeight(), a2.getWeight());
```

### 4.2 this의 의미

```java
class Example {
    int value = 1;
    
    void test() {
        // 익명 클래스: this는 익명 클래스 자신
        Runnable r1 = new Runnable() {
            int value = 2;
            @Override
            public void run() {
                System.out.println(this.value);  // 2 출력
            }
        };
        
        // 람다: this는 Example 인스턴스
        Runnable r2 = () -> {
            System.out.println(this.value);  // 1 출력
        };
    }
}
```

**람다는 새로운 스코프를 생성하지 않습니다!**

### 4.3 변수 섀도잉

```java
void test() {
    int value = 10;
    
    // ✅ 익명 클래스: 섀도잉 가능
    Runnable r1 = new Runnable() {
        public void run() {
            int value = 20;  // OK! 새로운 스코프
            System.out.println(value);  // 20
        }
    };
    
    // ❌ 람다: 섀도잉 불가
    Runnable r2 = () -> {
        // int value = 30;  // 컴파일 에러!
        System.out.println(value);  // 10 (외부 변수)
    };
}
```

### 4.4 성능 차이

```java
// 익명 클래스: 매번 새 객체 생성
for (int i = 0; i < 1000; i++) {
    Runnable r = new Runnable() {  // 1000개 객체 생성
        public void run() { }
    };
}

// 람다: 한 번만 생성 (캐싱)
for (int i = 0; i < 1000; i++) {
    Runnable r = () -> { };  // 1개 객체만 생성 (최적화)
}
```

---

## 5. Comparator의 내부 구현

### 5.1 comparing()의 원리

```java
// Comparator.comparing()의 실제 구현
public static <T, U extends Comparable<? super U>> Comparator<T> comparing(
        Function<? super T, ? extends U> keyExtractor) {
    
    Objects.requireNonNull(keyExtractor);
    
    // 핵심: 람다로 Comparator 구현을 반환
    return (c1, c2) -> {
        U key1 = keyExtractor.apply(c1);  // c1에서 키 추출
        U key2 = keyExtractor.apply(c2);  // c2에서 키 추출
        return key1.compareTo(key2);       // 키 비교
    };
}
```

### 5.2 사용 예제 분석

```java
// 코드
Comparator<Apple> byWeight = Comparator.comparing(Apple::getWeight);

// 내부 동작
Comparator<Apple> byWeight = (a1, a2) -> {
    Integer weight1 = a1.getWeight();  // keyExtractor 적용
    Integer weight2 = a2.getWeight();
    return weight1.compareTo(weight2); // Comparable.compareTo()
};
```

### 5.3 thenComparing()의 체이닝

```java
// thenComparing() 구현
public Comparator<T> thenComparing(Comparator<? super T> other) {
    Objects.requireNonNull(other);
    
    return (c1, c2) -> {
        int res = this.compare(c1, c2);  // 먼저 this로 비교
        return (res != 0) ? res : other.compare(c1, c2);  // 같으면 other로
    };
}
```

**사용:**

```java
Comparator<Apple> comp = Comparator.comparing(Apple::getColor)
                                   .thenComparing(Apple::getWeight);

// 내부 동작
Comparator<Apple> comp = (a1, a2) -> {
    int colorCmp = a1.getColor().compareTo(a2.getColor());
    if (colorCmp != 0) {
        return colorCmp;  // 색상이 다르면 색상 기준
    }
    return Integer.compare(a1.getWeight(), a2.getWeight());  // 같으면 무게
};
```

---

## 6. 스레드와 동작 파라미터화

### 6.1 Runnable의 본질

```java
@FunctionalInterface
public interface Runnable {
    void run();  // () → void
}
```

**Runnable은 "파라미터 없고 반환값 없는 동작"을 추상화합니다.**

### 6.2 Callable의 본질

```java
@FunctionalInterface
public interface Callable<V> {
    V call() throws Exception;  // () → V
}
```

**Callable은 "파라미터 없고 V를 반환하는 동작"을 추상화합니다.**

### 6.3 왜 분리되어 있는가?

```java
// Runnable: 결과가 필요 없을 때
new Thread(() -> {
    System.out.println("작업 실행");  // 결과 반환 불필요
}).start();

// Callable: 결과가 필요할 때
Future<Integer> future = executor.submit(() -> {
    return 42;  // 결과 반환 필요
});
Integer result = future.get();
```

### 6.4 ExecutorService의 원리

```java
public interface ExecutorService {
    // Runnable 제출 - 결과 없음
    void execute(Runnable command);
    
    // Callable 제출 - 결과 반환
    <T> Future<T> submit(Callable<T> task);
    
    // Runnable을 Future로 감싸기 (결과는 null)
    Future<?> submit(Runnable task);
}
```

**내부 구조:**

```
User Code  →  ExecutorService  →  Thread Pool
   ↓                ↓                  ↓
람다/동작     작업 큐에 추가      워커 스레드가 실행
```

---

## 7. 핵심 정리

### 7.1 동작 파라미터화의 본질

```
동작 파라미터화 = 1급 시민으로서의 함수

1급 시민(First-class citizen):
- 변수에 할당 가능
- 파라미터로 전달 가능
- 반환값으로 사용 가능
```

### 7.2 전략 패턴 = 동작 파라미터화

```java
// 전략 패턴
interface Strategy {
    void execute();
}

void doSomething(Strategy strategy) {
    strategy.execute();  // 전략 실행
}

// 이것이 바로 동작 파라미터화!
doSomething(() -> System.out.println("Action!"));
```

### 7.3 함수형 프로그래밍으로의 다리

동작 파라미터화는 **객체 지향에서 함수형 프로그래밍으로 가는 다리**입니다:

```java
// OOP: 객체로 동작 캡슐화
class PrintAction {
    void execute() {
        System.out.println("Print");
    }
}

// FP: 동작 자체를 값으로
Runnable action = () -> System.out.println("Print");
```

---

## 8. 실전 적용 원칙

### 8.1 언제 사용할까?

✅ **사용해야 할 때:**
- 메서드 동작이 자주 변경될 때
- 여러 전략 중 하나를 선택해야 할 때
- 코드 중복이 발생할 때

❌ **사용하지 말아야 할 때:**
- 동작이 한 가지만 있을 때
- 성능이 매우 중요한 핫스팟 코드
- 단순한 로직

### 8.2 설계 원칙

```java
// ✅ 좋은 설계
interface Processor<T> {
    void process(T item);  // 명확한 목적
}

// ❌ 나쁜 설계
interface DoSomething {
    void doIt(Object obj);  // 모호함
}
```

---

<div align="center">

**💡 최종 통찰**

> *"동작 파라미터화는 코드를 데이터처럼 다루는 기법이다.*  
> *이를 통해 유연하고 확장 가능한 시스템을 만들 수 있다."*

**동작 파라미터화를 마스터하면,  
함수형 프로그래밍의 세계로 들어갈 문이 열립니다.**

</div>
