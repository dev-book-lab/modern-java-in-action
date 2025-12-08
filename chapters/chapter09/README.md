<div align="center">

# Chapter 09. 리팩터링, 테스팅, 디버깅

**"람다와 스트림으로 코드를 개선하고, 테스트하고, 디버깅하기"**

> *익명 클래스를 람다로, 디자인 패턴을 함수형으로, 그리고 효과적인 테스팅과 디버깅 전략*

[📖 Deep Dive](advanced/deep-dive.md) | [💻 Code](code/) | [📋 CheatSheet](advanced/cheatsheet.md) | [💬 Q&A](advanced/qa-sessions.md)

</div>

---

## 📚 목차

1. [가독성과 유연성을 개선하는 리팩터링](#1-가독성과-유연성을-개선하는-리팩터링)
2. [람다로 객체지향 디자인 패턴 리팩터링하기](#2-람다로-객체지향-디자인-패턴-리팩터링하기)
3. [람다 테스팅](#3-람다-테스팅)
4. [디버깅](#4-디버깅)
5. [핵심 정리](#5-핵심-정리)

---

## 1. 가독성과 유연성을 개선하는 리팩터링

### 1.1 익명 클래스를 람다 표현식으로

#### ❌ Before: 익명 클래스

```java
// 장황한 익명 클래스
Runnable r = new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello World");
    }
};

// Comparator도 마찬가지
inventory.sort(new Comparator<Apple>() {
    @Override
    public int compare(Apple a1, Apple a2) {
        return a1.getWeight().compareTo(a2.getWeight());
    }
});
```

#### ✅ After: 람다 표현식

```java
// 간결한 람다
Runnable r = () -> System.out.println("Hello World");

// 메서드 참조로 더 간결하게
inventory.sort(Comparator.comparing(Apple::getWeight));
```

---

### 1.2 주의사항

#### ⚠️ 1. this의 의미 차이

```java
int a = 10;

// ✅ 익명 클래스: this는 익명 클래스 자신
Runnable r1 = new Runnable() {
    @Override
    public void run() {
        System.out.println(this);  // 익명 클래스 인스턴스
    }
};

// ✅ 람다: this는 감싸는 클래스
Runnable r2 = () -> {
    System.out.println(this);  // 외부 클래스 인스턴스
};
```

#### ⚠️ 2. 변수 섀도잉 (Shadowing)

```java
int a = 10;

// ✅ 익명 클래스: 섀도잉 가능
Runnable r1 = new Runnable() {
    @Override
    public void run() {
        int a = 20;  // ✅ 가능 (새로운 스코프)
        System.out.println(a);  // 20
    }
};

// ❌ 람다: 섀도잉 불가
Runnable r2 = () -> {
    int a = 20;  // ❌ 컴파일 에러! (같은 스코프)
    System.out.println(a);
};
```

#### ⚠️ 3. 오버로딩 모호함

```java
interface Task {
    void execute();
}

public static void doSomething(Runnable r) { r.run(); }
public static void doSomething(Task t) { t.execute(); }

// ✅ 익명 클래스: 타입 명확
doSomething(new Task() {
    @Override
    public void execute() {
        System.out.println("Task");
    }
});

// ❌ 람다: 모호함!
doSomething(() -> System.out.println("?")); // 컴파일 에러

// ✅ 명시적 캐스팅으로 해결
doSomething((Task) () -> System.out.println("Task"));
```

---

### 1.3 람다를 메서드 참조로

#### ❌ Before: 복잡한 람다

```java
Map<CaloricLevel, List<Dish>> dishesByCaloricLevel = 
    menu.stream().collect(
        groupingBy(dish -> {
            if (dish.getCalories() <= 400) return CaloricLevel.DIET;
            else if (dish.getCalories() <= 700) return CaloricLevel.NORMAL;
            else return CaloricLevel.FAT;
        })
    );
```

#### ✅ After: 메서드 참조

```java
// 1. 메서드 추출
public class Dish {
    public CaloricLevel getCaloricLevel() {
        if (this.getCalories() <= 400) return CaloricLevel.DIET;
        else if (this.getCalories() <= 700) return CaloricLevel.NORMAL;
        else return CaloricLevel.FAT;
    }
}

// 2. 메서드 참조 사용
Map<CaloricLevel, List<Dish>> dishesByCaloricLevel = 
    menu.stream().collect(groupingBy(Dish::getCaloricLevel));
```

**장점:**
- ✅ 의도가 명확함
- ✅ 재사용 가능
- ✅ 테스트 용이

---

### 1.4 명령형 → 스트림

#### ❌ Before: 명령형

```java
List<String> dishNames = new ArrayList<>();
for (Dish dish : menu) {
    if (dish.getCalories() > 300) {
        dishNames.add(dish.getName());
    }
}
```

#### ✅ After: 스트림

```java
List<String> dishNames = menu.stream()
    .filter(d -> d.getCalories() > 300)
    .map(Dish::getName)
    .collect(toList());

// 병렬 처리도 쉬움
List<String> dishNames = menu.parallelStream()
    .filter(d -> d.getCalories() > 300)
    .map(Dish::getName)
    .collect(toList());
```

**장점:**
- ✅ 의도가 명확 (무엇을 하는지)
- ✅ 최적화 (쇼트서킷, 게으른 평가)
- ✅ 병렬 처리 간편

---

### 1.5 조건부 연기 실행

#### ❌ Before: 클라이언트 코드에 노출

```java
if (logger.isLoggable(Log.FINER)) {
    logger.finer("Problem: " + generateDiagnostic());
}
```

**문제점:**
- logger 상태가 클라이언트에 노출
- 매번 상태 확인 필요
- 불필요한 평가 (generateDiagnostic 항상 실행)

#### ✅ After: Supplier로 연기

```java
// ✅ 람다 사용
logger.log(Level.FINER, () -> "Problem: " + generateDiagnostic());

// log 메서드 내부
public void log(Level level, Supplier<String> msgSupplier) {
    if (logger.isLoggable(level)) {
        log(level, msgSupplier.get());  // 조건 만족 시에만 실행!
    }
}
```

**장점:**
- ✅ 상태 캡슐화
- ✅ 불필요한 평가 방지
- ✅ 성능 개선

---

### 1.6 실행 어라운드

#### ❌ Before: 중복 코드

```java
// 한 줄 읽기
public String processFile1() throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
        return br.readLine();  // ← 이 부분만 다름
    }
}

// 두 줄 읽기
public String processFile2() throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
        return br.readLine() + br.readLine();  // ← 이 부분만 다름
    }
}
```

#### ✅ After: 람다로 동작 파라미터화

```java
// 1. 함수형 인터페이스 정의
@FunctionalInterface
public interface BufferedReaderProcessor {
    String process(BufferedReader br) throws IOException;
}

// 2. 템플릿 메서드
public String processFile(BufferedReaderProcessor processor) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
        return processor.process(br);  // 동작 파라미터화!
    }
}

// 3. 사용
String oneLine = processFile(br -> br.readLine());
String twoLines = processFile(br -> br.readLine() + br.readLine());
```

**장점:**
- ✅ 중복 제거
- ✅ 유연성
- ✅ 재사용성

---

## 2. 람다로 객체지향 디자인 패턴 리팩터링하기

### 2.1 전략 패턴 (Strategy Pattern)

#### ❌ Before: 클래스 기반

```java
// 전략 인터페이스
public interface ValidationStrategy {
    boolean execute(String s);
}

// 구체 전략 1
public class IsNumeric implements ValidationStrategy {
    @Override
    public boolean execute(String s) {
        return s.matches("\\d+");
    }
}

// 구체 전략 2
public class IsAllLowerCase implements ValidationStrategy {
    @Override
    public boolean execute(String s) {
        return s.matches("[a-z]+");
    }
}

// 사용
Validator numericValidator = new Validator(new IsNumeric());
Validator lowerCaseValidator = new Validator(new IsAllLowerCase());
```

#### ✅ After: 람다 표현식

```java
// ✅ 람다로 전략 전달
Validator numericValidator = new Validator(s -> s.matches("\\d+"));
Validator lowerCaseValidator = new Validator(s -> s.matches("[a-z]+"));

// 클래스 불필요!
```

---

### 2.2 템플릿 메서드 패턴 (Template Method)

#### ❌ Before: 상속 기반

```java
abstract class OnlineBanking {
    public void processCustomer(int id) {
        Customer c = Database.getCustomerWithId(id);
        makeCustomerHappy(c);  // 추상 메서드
    }
    
    abstract void makeCustomerHappy(Customer c);
}

// 구현
class MyBanking extends OnlineBanking {
    @Override
    void makeCustomerHappy(Customer c) {
        System.out.println("Hello " + c.getName());
    }
}
```

#### ✅ After: 람다로 동작 주입

```java
// ✅ Consumer로 동작 파라미터화
public void processCustomer(int id, Consumer<Customer> makeCustomerHappy) {
    Customer c = Database.getCustomerWithId(id);
    makeCustomerHappy.accept(c);
}

// 사용 - 상속 불필요!
new OnlineBanking().processCustomer(1337, 
    c -> System.out.println("Hello " + c.getName())
);
```

---

### 2.3 옵저버 패턴 (Observer Pattern)

#### ❌ Before: 클래스 기반

```java
interface Observer {
    void notify(String tweet);
}

class NYTimes implements Observer {
    @Override
    public void notify(String tweet) {
        if (tweet != null && tweet.contains("money")) {
            System.out.println("Breaking news in NY! " + tweet);
        }
    }
}

class Guardian implements Observer {
    @Override
    public void notify(String tweet) {
        if (tweet != null && tweet.contains("queen")) {
            System.out.println("Yet more news from London... " + tweet);
        }
    }
}

// 사용
Feed feed = new Feed();
feed.registerObserver(new NYTimes());
feed.registerObserver(new Guardian());
```

#### ✅ After: 람다 표현식

```java
// ✅ 람다로 옵저버 등록
Feed feed = new Feed();

feed.registerObserver(tweet -> {
    if (tweet != null && tweet.contains("money")) {
        System.out.println("Breaking news in NY! " + tweet);
    }
});

feed.registerObserver(tweet -> {
    if (tweet != null && tweet.contains("queen")) {
        System.out.println("Yet more news from London... " + tweet);
    }
});
```

**주의:** 옵저버가 상태를 가지거나 복잡하면 클래스 사용 권장

---

### 2.4 의무 체인 패턴 (Chain of Responsibility)

#### ❌ Before: 클래스 기반

```java
abstract class ProcessingObject<T> {
    protected ProcessingObject<T> successor;
    
    public void setSuccessor(ProcessingObject<T> successor) {
        this.successor = successor;
    }
    
    public T handle(T input) {
        T r = handleWork(input);
        if (successor != null) {
            return successor.handle(r);
        }
        return r;
    }
    
    abstract protected T handleWork(T input);
}

class HeaderTextProcessing extends ProcessingObject<String> {
    @Override
    protected String handleWork(String text) {
        return "From Raoul, Mario and Alan: " + text;
    }
}

class SpellCheckerProcessing extends ProcessingObject<String> {
    @Override
    protected String handleWork(String text) {
        return text.replaceAll("labda", "lambda");
    }
}

// 사용
ProcessingObject<String> p1 = new HeaderTextProcessing();
ProcessingObject<String> p2 = new SpellCheckerProcessing();
p1.setSuccessor(p2);
String result = p1.handle("Aren't labdas really sexy?!!");
```

#### ✅ After: Function 조합

```java
// ✅ UnaryOperator와 andThen 사용
UnaryOperator<String> headerProcessing = 
    text -> "From Raoul, Mario and Alan: " + text;
UnaryOperator<String> spellCheckerProcessing = 
    text -> text.replaceAll("labda", "lambda");

// 함수 조합
Function<String, String> pipeline = 
    headerProcessing.andThen(spellCheckerProcessing);

String result = pipeline.apply("Aren't labdas really sexy?!!");
```

---

### 2.5 팩토리 패턴 (Factory Pattern)

#### ❌ Before: switch 문

```java
public class ProductFactory {
    public static Product createProduct(String name) {
        switch (name) {
            case "loan": return new Loan();
            case "stock": return new Stock();
            case "bond": return new Bond();
            default: throw new RuntimeException("No such product: " + name);
        }
    }
}
```

#### ✅ After: Map + Supplier

```java
// ✅ Map으로 팩토리 구현
final static Map<String, Supplier<Product>> map = new HashMap<>();

static {
    map.put("loan", Loan::new);
    map.put("stock", Stock::new);
    map.put("bond", Bond::new);
}

public static Product createProduct(String name) {
    Supplier<Product> p = map.get(name);
    if (p != null) return p.get();
    throw new IllegalArgumentException("No such product: " + name);
}
```

**장점:**
- ✅ switch 문 불필요
- ✅ 새 제품 추가 시 Map에만 추가
- ✅ 확장성

**주의:** 생성자에 인수가 필요하면 별도 처리 필요

---

## 3. 람다 테스팅

### 3.1 보이는 람다 표현식 테스팅

람다는 익명이므로, **public 필드**로 선언하면 테스트 가능합니다.

```java
public class Point {
    // public static 필드
    public static final Comparator<Point> compareByXAndThenY = 
        Comparator.comparing(Point::getX)
                  .thenComparing(Point::getY);
    
    private final int x;
    private final int y;
    
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
}
```

**테스트 코드:**

```java
@Test
public void testComparingTwoPoints() {
    Point p1 = new Point(10, 15);
    Point p2 = new Point(10, 20);
    
    int result = Point.compareByXAndThenY.compare(p1, p2);
    
    assertTrue(result < 0);  // p1 < p2
}
```

---

### 3.2 람다를 사용하는 메서드 테스팅

**핵심 원칙:** 람다 자체가 아닌 **메서드의 동작**을 테스트하라.

```java
public class Point {
    // 람다를 사용하는 메서드
    public static List<Point> moveAllPointsRightBy(List<Point> points, int x) {
        return points.stream()
            .map(p -> new Point(p.getX() + x, p.getY()))  // 람다
            .collect(toList());
    }
}
```

**테스트 코드:**

```java
@Test
public void testMoveAllPointsRightBy() {
    List<Point> points = Arrays.asList(
        new Point(5, 5),
        new Point(10, 5)
    );
    
    List<Point> expected = Arrays.asList(
        new Point(15, 5),
        new Point(20, 5)
    );
    
    List<Point> result = Point.moveAllPointsRightBy(points, 10);
    
    assertEquals(expected, result);
}
```

---

### 3.3 복잡한 람다는 메서드로 분리

#### ❌ 테스트 어려운 복잡한 람다

```java
List<String> result = words.stream()
    .filter(s -> {
        // 복잡한 검증 로직 (10줄)
        boolean lengthCheck = s.length() > 3;
        boolean startsWithJ = s.startsWith("j");
        boolean containsA = s.contains("a");
        return lengthCheck && (startsWithJ || containsA);
    })
    .collect(toList());
```

#### ✅ 메서드로 분리 후 테스트

```java
// 메서드로 추출
private static boolean isValidWord(String s) {
    boolean lengthCheck = s.length() > 3;
    boolean startsWithJ = s.startsWith("j");
    boolean containsA = s.contains("a");
    return lengthCheck && (startsWithJ || containsA);
}

// 메서드 참조 사용
List<String> result = words.stream()
    .filter(MyClass::isValidWord)
    .collect(toList());

// 테스트
@Test
public void testIsValidWord() {
    assertTrue(isValidWord("java"));
    assertTrue(isValidWord("stream"));
    assertFalse(isValidWord("hi"));
}
```

---

### 3.4 고차원 함수 테스팅

**고차원 함수:** 함수를 인수로 받거나 함수를 반환하는 함수

#### 함수를 인수로 받는 경우

```java
public static <T> List<T> filter(List<T> list, Predicate<T> p) {
    return list.stream()
        .filter(p)
        .collect(toList());
}

// 테스트: 다양한 Predicate로 검증
@Test
public void testFilter() {
    List<Integer> numbers = Arrays.asList(1, 2, 3, 4);
    
    List<Integer> even = filter(numbers, i -> i % 2 == 0);
    List<Integer> smallerThanThree = filter(numbers, i -> i < 3);
    
    assertEquals(Arrays.asList(2, 4), even);
    assertEquals(Arrays.asList(1, 2), smallerThanThree);
}
```

#### 함수를 반환하는 경우

```java
public static Predicate<Integer> greaterThan(int threshold) {
    return x -> x > threshold;
}

// 테스트: 반환된 함수를 실행
@Test
public void testGreaterThan() {
    Predicate<Integer> gt5 = greaterThan(5);
    
    assertTrue(gt5.test(6));
    assertTrue(gt5.test(10));
    assertFalse(gt5.test(5));
    assertFalse(gt5.test(3));
}
```

---

## 4. 디버깅

### 4.1 스택 트레이스

#### 람다의 스택 트레이스 문제

```java
List<Point> points = Arrays.asList(new Point(12, 2), null);
points.stream()
    .map(p -> p.getX())  // NullPointerException!
    .forEach(System.out::println);
```

**스택 트레이스:**

```
Exception in thread "main" java.lang.NullPointerException
    at Debugging.lambda$main$0(Debugging.java:6)
                 ↑ 익명! 어떤 람다인지 알기 어려움
    at java.util.stream.ReferencePipeline$3$1.accept(...)
    ...
```

**람다 이름 패턴:**
- `lambda$main$0`: main 메서드의 첫 번째 람다
- `lambda$main$1`: main 메서드의 두 번째 람다
- `lambda$process$0`: process 메서드의 첫 번째 람다

---

#### 메서드 참조의 장점

```java
// 같은 클래스의 메서드 참조
public static int divideByZero(int n) {
    return n / 0;
}

numbers.stream()
    .map(Debugging::divideByZero)  // 메서드 참조
    .forEach(System.out::println);
```

**스택 트레이스:**

```
Exception in thread "main" java.lang.ArithmeticException: / by zero
    at Debugging.divideByZero(Debugging.java:10)
                 ↑ 명확한 메서드 이름!
    ...
```

**장점:**
- ✅ 메서드 이름이 스택 트레이스에 표시
- ✅ 디버깅 용이

---

### 4.2 정보 로깅 (peek)

#### 문제: 스트림 중간 값 확인 어려움

```java
List<Integer> result = numbers.stream()
    .map(x -> x + 17)
    .filter(x -> x % 2 == 0)
    .limit(3)
    .collect(toList());

// 중간에 어떤 값들이 있었는지?
```

#### 해결: peek 사용

```java
List<Integer> result = numbers.stream()
    .peek(x -> System.out.println("from stream: " + x))
    .map(x -> x + 17)
    .peek(x -> System.out.println("after map: " + x))
    .filter(x -> x % 2 == 0)
    .peek(x -> System.out.println("after filter: " + x))
    .limit(3)
    .peek(x -> System.out.println("after limit: " + x))
    .collect(toList());
```

**출력:**

```
from stream: 2
after map: 19
from stream: 3
after map: 20
after filter: 20
after limit: 20
from stream: 4
after map: 21
from stream: 5
after map: 22
after filter: 22
after limit: 22
```

**특징:**
- `forEach`: 최종 연산 (스트림 소비)
- `peek`: 중간 연산 (스트림 소비 안 함)

---

## 5. 핵심 정리

### 5.1 리팩터링

```java
// 익명 클래스 → 람다
Runnable r = () -> System.out.println("Hello");

// 람다 → 메서드 참조
inventory.sort(Comparator.comparing(Apple::getWeight));

// 명령형 → 스트림
menu.stream()
    .filter(d -> d.getCalories() > 300)
    .map(Dish::getName)
    .collect(toList());

// 조건부 연기 실행
logger.log(Level.FINER, () -> "Problem: " + generateDiagnostic());

// 실행 어라운드
processFile(br -> br.readLine());
```

**주의사항:**
- this 의미 차이
- 섀도잉 불가
- 오버로딩 모호함

---

### 5.2 디자인 패턴

| 패턴 | Before | After |
|------|--------|-------|
| **전략** | 전략 클래스 | 람다 전달 |
| **템플릿 메서드** | 상속 | Consumer 전달 |
| **옵저버** | 옵저버 클래스 | 람다 등록 |
| **의무 체인** | setSuccessor | andThen |
| **팩토리** | switch | Map + Supplier |

---

### 5.3 테스팅

```java
// 1. public 필드 → 직접 테스트
Point.compareByXAndThenY.compare(p1, p2);

// 2. 메서드의 동작 테스트
Point.moveAllPointsRightBy(points, 10);

// 3. 복잡한 람다 → 메서드 분리
filter(list, MyClass::isValidWord);

// 4. 고차원 함수 → 다양한 람다로 검증
filter(numbers, i -> i % 2 == 0);
filter(numbers, i -> i < 3);
```

---

### 5.4 디버깅

```java
// 1. 람다 스택 트레이스
lambda$main$0  // main의 첫 번째 람다
lambda$main$1  // main의 두 번째 람다

// 2. 메서드 참조 → 명확한 이름
map(Debugging::divideByZero)  // 스택에 divideByZero 표시

// 3. peek로 중간 값 확인
stream.peek(x -> System.out.println("value: " + x))
```

---

### 5.5 선택 가이드

```
리팩터링:
- 간단한 익명 클래스 → 람다
- 복잡한 람다 → 메서드 참조
- 반복문 → 스트림

디자인 패턴:
- 간단한 전략/옵저버 → 람다
- 복잡한 로직 → 클래스 유지

테스팅:
- 람다 직접 테스트 X
- 메서드 동작 테스트 O
- 복잡하면 메서드로 분리

디버깅:
- 복잡한 람다 → 메서드 참조
- 중간 값 확인 → peek
- 스택 트레이스 → lambda$메서드$번호 확인
```

---

**작성일**: 2024년 12월  
**대상**: Modern Java In Action Chapter 9  
**난이도**: ⭐⭐⭐⭐ (중급)
