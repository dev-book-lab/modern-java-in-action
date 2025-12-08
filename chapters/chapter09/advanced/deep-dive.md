# Chapter 09. 리팩터링, 테스팅, 디버깅 - Deep Dive

> 람다 표현식 리팩터링의 내부 메커니즘과 고급 테스팅/디버깅 기법

---

## 목차

1. [람다와 익명 클래스의 내부 구조](#1-람다와-익명-클래스의-내부-구조)
2. [리팩터링 심화](#2-리팩터링-심화)
3. [디자인 패턴 심층 분석](#3-디자인-패턴-심층-분석)
4. [테스팅 전략 심화](#4-테스팅-전략-심화)
5. [디버깅 고급 기법](#5-디버깅-고급-기법)

---

## 1. 람다와 익명 클래스의 내부 구조

### 1.1 컴파일러가 생성하는 코드

#### 익명 클래스 컴파일

```java
// 소스 코드
Runnable r = new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello");
    }
};
```

**컴파일러 생성:**
```java
// MyClass$1.class 파일 생성
class MyClass$1 implements Runnable {
    MyClass$1() {}
    
    @Override
    public void run() {
        System.out.println("Hello");
    }
}

// 원본 코드는 다음과 같이 변환
Runnable r = new MyClass$1();
```

**특징:**
- ✅ 각 익명 클래스마다 `.class` 파일 생성
- ✅ 클래스 로딩 시 메모리 사용
- ✅ 명시적인 this 참조

---

#### 람다 컴파일

```java
// 소스 코드
Runnable r = () -> System.out.println("Hello");
```

**컴파일러 생성 (invokedynamic 사용):**
```java
// 1. private static 메서드 생성
private static void lambda$main$0() {
    System.out.println("Hello");
}

// 2. invokedynamic 명령어로 람다 팩토리 호출
// LambdaMetafactory를 통해 런타임에 구현 생성
Runnable r = <invokedynamic bootstrap=LambdaMetafactory>(lambda$main$0);
```

**특징:**
- ✅ `.class` 파일 생성 안 함
- ✅ 런타임에 동적으로 구현 생성
- ✅ 메모리 효율적
- ✅ 지연 초기화 (처음 사용 시)

---

### 1.2 this 바인딩 차이

#### 익명 클래스의 this

```java
public class OuterClass {
    private String name = "Outer";
    
    public void test() {
        Runnable r = new Runnable() {
            private String name = "Inner";
            
            @Override
            public void run() {
                System.out.println(this.name);  // "Inner"
                System.out.println(OuterClass.this.name);  // "Outer"
            }
        };
        r.run();
    }
}
```

**메모리 구조:**
```
OuterClass 인스턴스
├─ name = "Outer"
└─ Runnable 익명 클래스 인스턴스
   ├─ name = "Inner"
   └─ this$0 (외부 클래스 참조)
```

---

#### 람다의 this

```java
public class OuterClass {
    private String name = "Outer";
    
    public void test() {
        Runnable r = () -> {
            // 람다는 자신의 this를 가지지 않음
            System.out.println(this.name);  // "Outer" (외부 클래스)
            // System.out.println(OuterClass.this.name);  // 불필요
        };
        r.run();
    }
}
```

**메모리 구조:**
```
OuterClass 인스턴스
└─ name = "Outer"
   (람다는 별도 인스턴스 생성 안 함)
```

**핵심 차이:**
- 익명 클래스: 새로운 스코프 생성 (자신의 this)
- 람다: 외부 스코프 공유 (렉시컬 스코프)

---

### 1.3 변수 캡처 메커니즘

#### Effectively Final

```java
public void test() {
    int localVar = 10;  // effectively final
    
    // ✅ 람다는 effectively final 변수만 캡처 가능
    Runnable r = () -> System.out.println(localVar);
    
    // localVar = 20;  // ❌ 이렇게 하면 위 람다 컴파일 에러
}
```

**이유:**
1. **스레드 안전성**: 변수가 변경되면 다른 스레드에서 예측 불가능
2. **구현 단순화**: final이면 값을 복사해서 저장 가능

---

#### 컴파일러 변환

```java
// 소스 코드
int x = 10;
Runnable r = () -> System.out.println(x);

// 컴파일러 생성 (개념적)
private static void lambda$main$0(int x_copy) {
    System.out.println(x_copy);
}

Runnable r = <invokedynamic>(10);  // x 값 전달
```

---

### 1.4 섀도잉 (Shadowing) 상세

#### 익명 클래스: 섀도잉 가능

```java
public class ShadowTest {
    public void test() {
        int x = 1;
        
        Runnable r = new Runnable() {
            @Override
            public void run() {
                int x = 2;  // ✅ 새로운 스코프, 섀도잉 가능
                System.out.println(x);  // 2
            }
        };
        
        System.out.println(x);  // 1
    }
}
```

**스코프 구조:**
```
test() 메서드 스코프
└─ x = 1
   └─ run() 메서드 스코프 (새로운 스코프!)
      └─ x = 2
```

---

#### 람다: 섀도잉 불가

```java
public class ShadowTest {
    public void test() {
        int x = 1;
        
        Runnable r = () -> {
            // int x = 2;  // ❌ 컴파일 에러!
            // 같은 스코프에서 중복 선언 불가
            System.out.println(x);  // 1
        };
    }
}
```

**스코프 구조:**
```
test() 메서드 스코프
├─ x = 1
└─ 람다 (같은 스코프!)
   └─ x 참조 (위의 x)
```

**핵심:** 람다는 새로운 스코프를 생성하지 않음 (렉시컬 스코프)

---

### 1.5 오버로딩 해결

#### 문제 상황

```java
interface Task {
    void execute();
}

public static void doSomething(Runnable r) { 
    System.out.println("Runnable");
    r.run(); 
}

public static void doSomething(Task t) { 
    System.out.println("Task");
    t.execute(); 
}

// ❌ 모호함!
doSomething(() -> System.out.println("Hello"));
// Error: reference to doSomething is ambiguous
```

---

#### 해결 방법

**방법 1: 명시적 캐스팅**
```java
doSomething((Runnable) () -> System.out.println("Hello"));  // "Runnable"
doSomething((Task) () -> System.out.println("Hello"));      // "Task"
```

**방법 2: 타입 명시 (변수 사용)**
```java
Runnable r = () -> System.out.println("Hello");
doSomething(r);  // "Runnable"

Task t = () -> System.out.println("Hello");
doSomething(t);  // "Task"
```

**방법 3: 메서드 이름 변경**
```java
public static void doSomethingRunnable(Runnable r) { ... }
public static void doSomethingTask(Task t) { ... }
```

---

#### 타입 추론 과정

```java
// 컴파일러 타입 추론
() -> System.out.println("Hello")
  ↓
어떤 함수형 인터페이스?
  ↓
Runnable? Task? (둘 다 가능)
  ↓
모호함! (Ambiguous)
```

**명시적 캐스팅 시:**
```java
(Runnable) () -> System.out.println("Hello")
  ↓
타입이 명확: Runnable
  ↓
public void run() 메서드 구현
```

---

## 2. 리팩터링 심화

### 2.1 조건부 연기 실행 패턴

#### 내부 동작 원리

```java
// Before: 즉시 평가
logger.log(Level.FINER, "Problem: " + generateDiagnostic());
//                       ↑ 항상 실행됨 (logger 레벨 무관)

// After: 지연 평가
logger.log(Level.FINER, () -> "Problem: " + generateDiagnostic());
//                       ↑ Supplier - 필요 시에만 실행
```

---

#### 성능 비교

```java
public class PerformanceTest {
    
    private static Logger logger = Logger.getLogger("Test");
    
    // 비용이 큰 작업
    private static String generateDiagnostic() {
        // 1초 소요
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        return "Diagnostic data";
    }
    
    public static void main(String[] args) {
        logger.setLevel(Level.INFO);  // FINER보다 높음 → 로그 안 남음
        
        // ❌ 즉시 평가: 1초 소요 (의미 없는 작업)
        long start1 = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            logger.log(Level.FINER, "Problem: " + generateDiagnostic());
        }
        System.out.println("즉시 평가: " + (System.currentTimeMillis() - start1) + "ms");
        // 출력: 100초
        
        // ✅ 지연 평가: 거의 즉시 (조건 불만족 시 실행 안 함)
        long start2 = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            logger.log(Level.FINER, () -> "Problem: " + generateDiagnostic());
        }
        System.out.println("지연 평가: " + (System.currentTimeMillis() - start2) + "ms");
        // 출력: ~10ms
    }
}
```

**성능 향상: 10,000배!**

---

#### 실전 응용: Optional

```java
public class OptionalExample {
    
    public User getUserById(String id) {
        // ❌ 즉시 평가: 항상 기본 사용자 생성 (비효율)
        return userRepository.findById(id)
            .orElse(createDefaultUser());  // 항상 실행됨
        
        // ✅ 지연 평가: 필요할 때만 생성
        return userRepository.findById(id)
            .orElseGet(() -> createDefaultUser());  // Optional이 empty일 때만 실행
    }
    
    private User createDefaultUser() {
        // 데이터베이스 접근, 복잡한 초기화 등
        System.out.println("Creating default user...");
        return new User("default");
    }
}
```

---

### 2.2 실행 어라운드 패턴 심화

#### 복잡한 예제: 트랜잭션

```java
@FunctionalInterface
public interface TransactionCallback<T> {
    T doInTransaction(Connection conn) throws SQLException;
}

public class TransactionTemplate {
    
    private DataSource dataSource;
    
    public <T> T execute(TransactionCallback<T> action) throws SQLException {
        Connection conn = null;
        try {
            // 1. 준비: 연결 얻기, 트랜잭션 시작
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            
            // 2. 실행: 사용자 정의 작업
            T result = action.doInTransaction(conn);
            
            // 3. 성공: 커밋
            conn.commit();
            return result;
            
        } catch (SQLException e) {
            // 4. 실패: 롤백
            if (conn != null) {
                conn.rollback();
            }
            throw e;
            
        } finally {
            // 5. 정리: 연결 닫기
            if (conn != null) {
                conn.close();
            }
        }
    }
}

// 사용
TransactionTemplate template = new TransactionTemplate(dataSource);

User user = template.execute(conn -> {
    // 트랜잭션 내에서 실행
    PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (?, ?)");
    stmt.setString(1, "alice");
    stmt.setInt(2, 25);
    stmt.executeUpdate();
    
    return new User("alice", 25);
});
```

**장점:**
- ✅ 트랜잭션 관리 자동화
- ✅ 예외 처리 일관성
- ✅ 리소스 누수 방지

---

#### 재시도 패턴

```java
@FunctionalInterface
public interface RetryableTask<T> {
    T execute() throws Exception;
}

public class RetryTemplate {
    
    public <T> T executeWithRetry(
            RetryableTask<T> task,
            int maxAttempts,
            long delayMs) throws Exception {
        
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                System.out.println("Attempt " + attempt);
                return task.execute();  // 성공 시 즉시 반환
                
            } catch (Exception e) {
                lastException = e;
                System.out.println("Failed: " + e.getMessage());
                
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }
        
        throw lastException;  // 모든 시도 실패
    }
}

// 사용
RetryTemplate retry = new RetryTemplate();

String result = retry.executeWithRetry(
    () -> {
        // 불안정한 외부 API 호출
        return httpClient.get("https://api.example.com/data");
    },
    3,      // 최대 3번 시도
    1000    // 1초 대기
);
```

---

## 3. 디자인 패턴 심층 분석

### 3.1 전략 패턴 고급 활용

#### 전략 조합

```java
public class ValidationStrategies {
    
    // 기본 전략들
    public static final Predicate<String> IS_NUMERIC = 
        s -> s.matches("\\d+");
    
    public static final Predicate<String> IS_LOWERCASE = 
        s -> s.matches("[a-z]+");
    
    public static final Predicate<String> MIN_LENGTH_5 = 
        s -> s.length() >= 5;
    
    // 전략 조합
    public static Predicate<String> combine(Predicate<String>... strategies) {
        return Arrays.stream(strategies)
            .reduce(s -> true, Predicate::and);
    }
}

// 사용
Predicate<String> complexValidation = ValidationStrategies.combine(
    ValidationStrategies.IS_NUMERIC,
    ValidationStrategies.MIN_LENGTH_5
);

Validator validator = new Validator(complexValidation);
System.out.println(validator.validate("12345"));  // true
System.out.println(validator.validate("123"));    // false (길이 부족)
System.out.println(validator.validate("abcde"));  // false (숫자 아님)
```

---

#### 실시간 전략 선택

```java
public class PaymentProcessor {
    
    private Map<String, Function<Payment, Result>> strategies;
    
    public PaymentProcessor() {
        strategies = new HashMap<>();
        strategies.put("CREDIT_CARD", this::processCreditCard);
        strategies.put("PAYPAL", this::processPaypal);
        strategies.put("BITCOIN", this::processBitcoin);
    }
    
    public Result process(Payment payment) {
        Function<Payment, Result> strategy = 
            strategies.get(payment.getMethod());
        
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown payment method");
        }
        
        return strategy.apply(payment);
    }
    
    private Result processCreditCard(Payment payment) {
        System.out.println("Processing credit card: " + payment.getAmount());
        return new Result("success");
    }
    
    private Result processPaypal(Payment payment) {
        System.out.println("Processing PayPal: " + payment.getAmount());
        return new Result("success");
    }
    
    private Result processBitcoin(Payment payment) {
        System.out.println("Processing Bitcoin: " + payment.getAmount());
        return new Result("success");
    }
}
```

---

### 3.2 옵저버 패턴 고급

#### 필터링 옵저버

```java
public class FilteredFeed implements Subject {
    
    private List<Observer> observers = new ArrayList<>();
    private Map<Observer, Predicate<String>> filters = new HashMap<>();
    
    @Override
    public void registerObserver(Observer o) {
        registerObserver(o, tweet -> true);  // 모든 트윗
    }
    
    // 필터와 함께 등록
    public void registerObserver(Observer o, Predicate<String> filter) {
        observers.add(o);
        filters.put(o, filter);
    }
    
    @Override
    public void notifyObservers(String tweet) {
        observers.forEach(observer -> {
            Predicate<String> filter = filters.get(observer);
            if (filter.test(tweet)) {
                observer.notify(tweet);
            }
        });
    }
}

// 사용
FilteredFeed feed = new FilteredFeed();

// 금융 뉴스만 받는 옵저버
feed.registerObserver(
    tweet -> System.out.println("Financial: " + tweet),
    tweet -> tweet.contains("stock") || tweet.contains("market")
);

// 스포츠 뉴스만 받는 옵저버
feed.registerObserver(
    tweet -> System.out.println("Sports: " + tweet),
    tweet -> tweet.contains("goal") || tweet.contains("win")
);
```

---

### 3.3 팩토리 패턴 - 다중 파라미터 문제

#### 문제

```java
// ❌ Supplier는 인수 없는 생성자만 가능
Map<String, Supplier<Product>> map = new HashMap<>();
map.put("loan", Loan::new);  // Loan() 생성자

// ❌ 인수가 있는 생성자는?
// Loan(double amount, double rate)
```

---

#### 해결 방법

**방법 1: TriFunction 정의**

```java
@FunctionalInterface
public interface TriFunction<T, U, V, R> {
    R apply(T t, U u, V v);
}

Map<String, TriFunction<Double, Double, String, Product>> map = new HashMap<>();
map.put("loan", (amount, rate, customer) -> new Loan(amount, rate, customer));

// 사용
Product loan = map.get("loan").apply(10000.0, 0.05, "Alice");
```

**방법 2: 빌더 패턴**

```java
public class ProductFactory {
    
    public static LoanBuilder loanBuilder() {
        return new LoanBuilder();
    }
    
    public static class LoanBuilder {
        private double amount;
        private double rate;
        private String customer;
        
        public LoanBuilder amount(double amount) {
            this.amount = amount;
            return this;
        }
        
        public LoanBuilder rate(double rate) {
            this.rate = rate;
            return this;
        }
        
        public LoanBuilder customer(String customer) {
            this.customer = customer;
            return this;
        }
        
        public Loan build() {
            return new Loan(amount, rate, customer);
        }
    }
}

// 사용
Loan loan = ProductFactory.loanBuilder()
    .amount(10000.0)
    .rate(0.05)
    .customer("Alice")
    .build();
```

---

## 4. 테스팅 전략 심화

### 4.1 Mockito와 람다

```java
public class UserServiceTest {
    
    @Mock
    private UserRepository repository;
    
    @InjectMocks
    private UserService service;
    
    @Test
    public void testProcessUsers() {
        // Mock 설정
        when(repository.findAll()).thenReturn(Arrays.asList(
            new User("Alice", 25),
            new User("Bob", 30)
        ));
        
        // 람다를 사용하는 메서드 테스트
        List<String> result = service.processUsers(
            user -> user.getName().toUpperCase()
        );
        
        // 검증
        assertEquals(Arrays.asList("ALICE", "BOB"), result);
        verify(repository, times(1)).findAll();
    }
}
```

---

### 4.2 AssertJ와 람다

```java
import static org.assertj.core.api.Assertions.*;

public class StreamTest {
    
    @Test
    public void testFiltering() {
        List<String> words = Arrays.asList("apple", "banana", "cherry");
        
        List<String> result = words.stream()
            .filter(s -> s.length() > 5)
            .collect(toList());
        
        // AssertJ 람다 매칭
        assertThat(result)
            .hasSize(2)
            .allMatch(s -> s.length() > 5)
            .anyMatch(s -> s.startsWith("b"))
            .noneMatch(s -> s.contains("x"));
    }
}
```

---

### 4.3 예외 테스팅

```java
public class ExceptionTest {
    
    @Test
    public void testLambdaException() {
        List<String> words = Arrays.asList("123", "abc", "456");
        
        // assertThrows (JUnit 5)
        assertThrows(NumberFormatException.class, () -> {
            words.stream()
                .map(Integer::parseInt)
                .collect(toList());
        });
        
        // 예외 메시지 검증
        NumberFormatException exception = assertThrows(
            NumberFormatException.class,
            () -> Integer.parseInt("abc")
        );
        
        assertTrue(exception.getMessage().contains("abc"));
    }
}
```

---

## 5. 디버깅 고급 기법

### 5.1 람다 스택 트레이스 분석

#### 복잡한 체인

```java
List<String> words = Arrays.asList("hello", "world", null, "java");

words.stream()
    .map(s -> s.toUpperCase())           // lambda$main$0
    .filter(s -> s.length() > 4)         // lambda$main$1
    .map(s -> s + "!")                   // lambda$main$2
    .forEach(System.out::println);       // 메서드 참조
```

**스택 트레이스:**
```
Exception in thread "main" java.lang.NullPointerException
    at Example.lambda$main$0(Example.java:8)
                    ↑     ↑
                    |     └─ 첫 번째 람다 (map)
                    └─ main 메서드
    at java.util.stream.ReferencePipeline$3$1.accept(...)
    ...
```

**분석:**
- `lambda$main$0`: 첫 번째 람다 (map) → null.toUpperCase() 에러
- 8번 줄 확인 → `.map(s -> s.toUpperCase())`

---

### 5.2 디버깅용 래퍼

```java
public class DebugUtils {
    
    // 람다 래핑 - 실행 전후 로깅
    public static <T, R> Function<T, R> debug(
            String name, 
            Function<T, R> function) {
        
        return input -> {
            System.out.println("[" + name + "] Input: " + input);
            try {
                R result = function.apply(input);
                System.out.println("[" + name + "] Output: " + result);
                return result;
            } catch (Exception e) {
                System.err.println("[" + name + "] Error: " + e.getMessage());
                throw e;
            }
        };
    }
}

// 사용
List<String> result = words.stream()
    .map(debug("uppercase", String::toUpperCase))
    .filter(debug("length check", s -> s.length() > 4))
    .map(debug("add exclamation", s -> s + "!"))
    .collect(toList());
```

**출력:**
```
[uppercase] Input: hello
[uppercase] Output: HELLO
[length check] Input: HELLO
[length check] Output: true
[add exclamation] Input: HELLO
[add exclamation] Output: HELLO!
...
```

---

### 5.3 성능 측정

```java
public class PerformanceUtils {
    
    public static <T, R> Function<T, R> measure(
            String name, 
            Function<T, R> function) {
        
        return input -> {
            long start = System.nanoTime();
            try {
                return function.apply(input);
            } finally {
                long duration = System.nanoTime() - start;
                System.out.printf("[%s] Duration: %.2f ms%n", 
                    name, duration / 1_000_000.0);
            }
        };
    }
}

// 사용
List<String> result = words.stream()
    .map(measure("uppercase", String::toUpperCase))
    .map(measure("trim", String::trim))
    .map(measure("toLowerCase", String::toLowerCase))
    .collect(toList());
```

---

### 5.4 IntelliJ IDEA 람다 디버깅

#### 브레이크포인트

```java
// 람다 내부에 브레이크포인트 설정 가능
List<String> result = words.stream()
    .map(s -> {
        // ← 여기 브레이크포인트
        return s.toUpperCase();
    })
    .collect(toList());
```

#### Stream Trace

IntelliJ IDEA의 "Trace Current Stream Chain" 기능:
```java
words.stream()
    .map(String::toUpperCase)     // [hello, world, java]
    .filter(s -> s.length() > 4)  // [HELLO, WORLD]
    .collect(toList());           // [HELLO, WORLD]
```

---

### 5.5 로깅 프레임워크 통합

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingExample {
    
    private static final Logger log = LoggerFactory.getLogger(LoggingExample.class);
    
    public List<User> processUsers(List<User> users) {
        return users.stream()
            .peek(u -> log.debug("Processing user: {}", u))
            .filter(u -> {
                boolean valid = u.getAge() >= 18;
                log.debug("User {} is {}", u.getName(), valid ? "valid" : "invalid");
                return valid;
            })
            .map(u -> {
                User transformed = transform(u);
                log.debug("Transformed {} to {}", u, transformed);
                return transformed;
            })
            .peek(u -> log.info("Final user: {}", u))
            .collect(toList());
    }
}
```

---

## 핵심 정리

### 1. 람다 내부 구조

```
✅ invokedynamic으로 런타임 생성
✅ .class 파일 생성 안 함
✅ 렉시컬 스코프 (this는 외부 클래스)
✅ effectively final 변수만 캡처
✅ 섀도잉 불가
```

### 2. 리팩터링 고급

```
✅ 조건부 연기: Supplier로 10,000배 성능 향상
✅ 실행 어라운드: 트랜잭션, 재시도 패턴
✅ 전략 조합: Predicate.and/or
✅ 팩토리: 빌더 패턴으로 다중 파라미터 해결
```

### 3. 테스팅 고급

```
✅ Mockito와 람다 조합
✅ AssertJ 람다 매칭
✅ 예외 테스팅 (assertThrows)
✅ 고차원 함수 철저한 검증
```

### 4. 디버깅 고급

```
✅ 람다 이름 패턴 이해
✅ 디버깅 래퍼 사용
✅ 성능 측정 래퍼
✅ IDE 도구 활용 (Stream Trace)
✅ 로깅 프레임워크 통합
```

---

**작성일**: 2024년 12월  
**대상**: Modern Java In Action Chapter 9  
**난이도**: ⭐⭐⭐⭐⭐ (고급)
