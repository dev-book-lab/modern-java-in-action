# Lambda Expressions Cheatsheet

> 람다 표현식 빠른 참조 가이드

---

## 🎯 람다 기본 문법

### 기본 형태

```java
// 표현식 스타일
(파라미터) -> 표현식

// 블록 스타일
(파라미터) -> { 문장들; return 값; }
```

### 파라미터 규칙

```java
// 파라미터 없음
() -> 42
() -> System.out.println("Hello")

// 파라미터 하나 (괄호 생략 가능)
x -> x * x
(x) -> x * x  // 동일

// 파라미터 여러 개 (괄호 필수)
(x, y) -> x + y

// 타입 명시
(int x, int y) -> x + y
(String s) -> s.length()
```

---

## 📚 함수형 인터페이스

### java.util.function 핵심 인터페이스

| 인터페이스 | 시그니처 | 추상 메서드 | 용도 |
|-----------|---------|-----------|------|
| **Predicate\<T\>** | `T -> boolean` | `test(T)` | 조건 검사 |
| **Consumer\<T\>** | `T -> void` | `accept(T)` | 값 소비 |
| **Function\<T,R\>** | `T -> R` | `apply(T)` | 변환/매핑 |
| **Supplier\<T\>** | `() -> T` | `get()` | 값 생성 |
| **UnaryOperator\<T\>** | `T -> T` | `apply(T)` | 단항 연산 |
| **BinaryOperator\<T\>** | `(T,T) -> T` | `apply(T,T)` | 이항 연산 |

### 사용 예시

```java
// Predicate
Predicate<String> isEmpty = s -> s.isEmpty();
Predicate<Integer> isPositive = i -> i > 0;

// Consumer
Consumer<String> print = s -> System.out.println(s);
Consumer<List<Integer>> addItem = list -> list.add(1);

// Function
Function<String, Integer> toLength = s -> s.length();
Function<Integer, String> toString = i -> String.valueOf(i);

// Supplier
Supplier<Double> random = () -> Math.random();
Supplier<List<String>> listFactory = () -> new ArrayList<>();

// UnaryOperator
UnaryOperator<Integer> square = x -> x * x;
UnaryOperator<String> toUpper = String::toUpperCase;

// BinaryOperator
BinaryOperator<Integer> add = (a, b) -> a + b;
BinaryOperator<String> concat = (s1, s2) -> s1 + s2;
```

---

## 🔢 기본형 특화 인터페이스

### Int 계열

```java
IntPredicate           // int -> boolean
IntConsumer            // int -> void
IntFunction<R>         // int -> R
IntSupplier            // () -> int
IntUnaryOperator       // int -> int
IntBinaryOperator      // (int, int) -> int
ToIntFunction<T>       // T -> int
IntToDoubleFunction    // int -> double
IntToLongFunction      // int -> long
```

### Double 계열

```java
DoublePredicate        // double -> boolean
DoubleConsumer         // double -> void
DoubleFunction<R>      // double -> R
DoubleSupplier         // () -> double
DoubleUnaryOperator    // double -> double
DoubleBinaryOperator   // (double, double) -> double
ToDoubleFunction<T>    // T -> double
DoubleToIntFunction    // double -> int
DoubleToLongFunction   // double -> long
```

### Long 계열

```java
LongPredicate          // long -> boolean
LongConsumer           // long -> void
LongFunction<R>        // long -> R
LongSupplier           // () -> long
LongUnaryOperator      // long -> long
LongBinaryOperator     // (long, long) -> long
ToLongFunction<T>      // T -> long
LongToDoubleFunction   // long -> double
LongToIntFunction      // long -> int
```

### 사용 예시

```java
// 박싱 발생 (느림)
Predicate<Integer> evenBoxed = i -> i % 2 == 0;

// 박싱 없음 (빠름)
IntPredicate even = i -> i % 2 == 0;

// 성능 차이
IntStream.range(0, 1_000_000)
    .filter(i -> i % 2 == 0)  // IntPredicate (빠름)
    .sum();
```

---

## 🔗 메서드 참조

### 4가지 유형

#### 1. 정적 메서드 참조

```java
// 람다
(String s) -> Integer.parseInt(s)
// 메서드 참조
Integer::parseInt

// 예시
Function<String, Integer> parse = Integer::parseInt;
parse.apply("123");  // 123
```

#### 2. 인스턴스 메서드 참조 (임의 객체)

```java
// 람다
(String s) -> s.toUpperCase()
// 메서드 참조
String::toUpperCase

// 람다
(a1, a2) -> a1.compareTo(a2)
// 메서드 참조
String::compareTo

// 예시
Function<String, String> upper = String::toUpperCase;
upper.apply("hello");  // "HELLO"
```

#### 3. 인스턴스 메서드 참조 (기존 객체)

```java
String str = "Hello";

// 람다
() -> str.length()
// 메서드 참조
str::length

// 예시
Supplier<Integer> lengthGetter = str::length;
lengthGetter.get();  // 5
```

#### 4. 생성자 참조

```java
// 인수 없음
() -> new ArrayList<>()
ArrayList::new

// 인수 하나
(Integer weight) -> new Apple(weight)
Apple::new

// 인수 두 개
(Color c, Integer w) -> new Apple(c, w)
Apple::new

// 예시
Supplier<List<String>> listFactory = ArrayList::new;
Function<Integer, Apple> appleFactory = Apple::new;
BiFunction<Color, Integer, Apple> appleFactory2 = Apple::new;
```

---

## 🎭 람다 조합

### Comparator 조합

```java
Comparator<Apple> c = comparing(Apple::getWeight);

// 역정렬
c.reversed()

// 연결
c.thenComparing(Apple::getCountry)

// 체이닝
comparing(Apple::getWeight)
    .reversed()
    .thenComparing(Apple::getCountry)
```

### Predicate 조합

```java
Predicate<Apple> red = a -> Color.RED.equals(a.getColor());

// NOT
red.negate()

// AND
red.and(a -> a.getWeight() > 150)

// OR
red.or(a -> Color.GREEN.equals(a.getColor()))

// 복합
red.and(a -> a.getWeight() > 150)
   .or(a -> Color.GREEN.equals(a.getColor()))
```

### Function 조합

```java
Function<Integer, Integer> f = x -> x + 1;
Function<Integer, Integer> g = x -> x * 2;

// andThen: f → g
f.andThen(g)   // (x + 1) * 2

// compose: g → f
f.compose(g)   // (x * 2) + 1

// 체이닝
Function<String, String> addHeader = 
    Letter::addHeader;

Function<String, String> process = 
    addHeader
        .andThen(Letter::checkSpelling)
        .andThen(Letter::addFooter);
```

---

## ⚠️ 제약 사항

### 지역 변수 제약

```java
// ✅ OK: final
final int value = 10;
Runnable r = () -> System.out.println(value);

// ✅ OK: effectively final
int value = 10;
Runnable r = () -> System.out.println(value);
// value는 재할당되지 않음

// ❌ 에러: 재할당
int value = 10;
Runnable r = () -> System.out.println(value);
value = 20;  // 컴파일 에러!

// ✅ OK: 인스턴스 변수
class Example {
    private int value = 10;
    
    public Runnable create() {
        return () -> System.out.println(value++);  // OK!
    }
}
```

### 예외 처리

```java
// ❌ Function은 checked exception 던질 수 없음
Function<String, Integer> parse = 
    s -> Integer.parseInt(s);  // NumberFormatException

// ✅ 람다 내부에서 try-catch
Function<String, Integer> parse = s -> {
    try {
        return Integer.parseInt(s);
    } catch (NumberFormatException e) {
        return 0;
    }
};

// ✅ 커스텀 함수형 인터페이스
@FunctionalInterface
interface ThrowingFunction<T, R> {
    R apply(T t) throws Exception;
}
```

---

## 🎯 형식 추론

### 컴파일러가 추론 가능한 것

```java
// 파라미터 타입
Comparator<Apple> c1 = 
    (Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight());

Comparator<Apple> c2 = 
    (a1, a2) -> a1.getWeight().compareTo(a2.getWeight());  // 추론!

// 반환 타입
Function<String, Integer> f = s -> s.length();  // Integer 추론!

// 함수형 인터페이스
filter(list, s -> s.length() > 5);  // Predicate<String> 추론!
```

---

## 💡 실전 패턴

### 실행 어라운드 패턴

```java
@FunctionalInterface
interface ResourceProcessor<T> {
    T process(Resource r) throws IOException;
}

public <T> T execute(ResourceProcessor<T> p) throws IOException {
    try (Resource r = new Resource()) {
        return p.process(r);
    }
}

// 사용
String result = execute(r -> r.readLine());
```

### 템플릿 메서드 패턴

```java
abstract class OnlineBanking {
    public void processCustomer(int id) {
        Customer c = Database.getCustomerWithId(id);
        makeCustomerHappy(c);
    }
    
    abstract void makeCustomerHappy(Customer c);
}

// 람다로 대체
void processCustomer(int id, Consumer<Customer> makeCustomerHappy) {
    Customer c = Database.getCustomerWithId(id);
    makeCustomerHappy.accept(c);
}
```

### 전략 패턴

```java
interface ValidationStrategy {
    boolean execute(String s);
}

class Validator {
    private final ValidationStrategy strategy;
    
    public Validator(ValidationStrategy v) {
        this.strategy = v;
    }
    
    public boolean validate(String s) {
        return strategy.execute(s);
    }
}

// 사용
Validator numericValidator = 
    new Validator(s -> s.matches("\\d+"));

Validator lowerCaseValidator = 
    new Validator(s -> s.matches("[a-z]+"));
```

---

## 🔍 디버깅

### 람다 스택 트레이스

```java
// 람다에서 예외 발생 시
List<Point> points = Arrays.asList(new Point(12, 2), null);
points.stream()
    .map(p -> p.getX())  // NPE 발생!
    .forEach(System.out::println);

// 스택 트레이스:
// at Debugging.lambda$main$0(Debugging.java:6)
// at java.util.stream.ReferencePipeline...
```

### 디버깅 팁

```java
// 1. peek 사용
list.stream()
    .peek(x -> System.out.println("처리 전: " + x))
    .map(x -> x + 10)
    .peek(x -> System.out.println("처리 후: " + x))
    .collect(toList());

// 2. 메서드로 추출
// Before
list.stream().map(s -> s.toUpperCase()).collect(toList());

// After
list.stream()
    .map(this::toUpperCase)  // 디버깅 가능!
    .collect(toList());

private String toUpperCase(String s) {
    return s.toUpperCase();
}
```

---

## 📊 성능 고려사항

### 박싱 오버헤드

```java
// ❌ 박싱 발생 (느림)
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
numbers.stream()
    .reduce(0, (a, b) -> a + b);

// ✅ 기본형 스트림 (빠름)
IntStream.rangeClosed(1, 5)
    .sum();
```

### 병렬 처리

```java
// 순차
list.stream()
    .map(x -> x * x)
    .reduce(0, Integer::sum);

// 병렬
list.parallelStream()
    .map(x -> x * x)
    .reduce(0, Integer::sum);
```

---

## ✅ 체크리스트

### 람다 작성 시 확인사항

- [ ] 함수형 인터페이스를 사용하는가?
- [ ] 파라미터 타입 추론이 가능한가?
- [ ] 지역 변수가 effectively final인가?
- [ ] 예외 처리가 필요한가?
- [ ] 메서드 참조로 간결하게 가능한가?
- [ ] 기본형 특화 인터페이스를 사용할 수 있는가?

---

## 🎓 빠른 변환 가이드

### 익명 클래스 → 람다

```java
// Before
new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello");
    }
}

// After
() -> System.out.println("Hello")
```

### 람다 → 메서드 참조

```java
// Before
(Apple a) -> a.getWeight()
// After
Apple::getWeight

// Before
() -> Thread.currentThread().dumpStack()
// After
Thread.currentThread()::dumpStack

// Before
(str, i) -> str.substring(i)
// After
String::substring
```

---

## 💻 자주 사용하는 패턴

```java
// 필터링
list.stream()
    .filter(s -> s.startsWith("A"))
    .collect(toList());

// 변환
list.stream()
    .map(String::toUpperCase)
    .collect(toList());

// 정렬
list.sort(comparing(String::length));

// 그룹화
Map<Integer, List<String>> groups = 
    list.stream()
        .collect(groupingBy(String::length));

// 조건 검사
boolean allMatch = list.stream()
    .allMatch(s -> s.length() > 3);

// 축약
int sum = numbers.stream()
    .reduce(0, Integer::sum);
```

---

**이 치트시트를 참조하여 람다 표현식을 효과적으로 사용하세요!** 🚀
