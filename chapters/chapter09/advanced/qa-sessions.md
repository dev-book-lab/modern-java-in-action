# Chapter 09. 리팩터링, 테스팅, 디버깅 - Q&A Sessions

> 자주 묻는 질문과 답변으로 이해도를 높이자

---

## 목차

1. [리팩터링 관련 질문](#1-리팩터링-관련-질문)
2. [디자인 패턴 관련 질문](#2-디자인-패턴-관련-질문)
3. [테스팅 관련 질문](#3-테스팅-관련-질문)
4. [디버깅 관련 질문](#4-디버깅-관련-질문)
5. [실전 응용 질문](#5-실전-응용-질문)

---

## 1. 리팩터링 관련 질문

### Q1. 모든 익명 클래스를 람다로 바꿔야 하나요?

**A:** 아니요! 상황에 따라 판단해야 합니다.

**람다로 바꾸면 좋은 경우:**
```java
// ✅ 간단한 익명 클래스 (1-5줄)
button.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Clicked");
    }
});

// → 람다로 간결하게
button.addActionListener(e -> System.out.println("Clicked"));
```

**클래스 유지가 나은 경우:**
```java
// ❌ 복잡한 로직 (10줄+)
// ❌ 여러 메서드 구현 필요
// ❌ 상태(필드)를 가짐
// ❌ this를 명시적으로 사용

class ComplexListener implements ActionListener {
    private int clickCount = 0;  // 상태
    
    @Override
    public void actionPerformed(ActionEvent e) {
        clickCount++;
        if (clickCount > 10) {
            // 복잡한 로직...
        }
    }
    
    public int getClickCount() {  // 추가 메서드
        return clickCount;
    }
}
```

**판단 기준:**
- 3줄 이하 → 람다
- 4-10줄 → 상황에 따라
- 10줄 이상 → 클래스 유지

---

### Q2. 람다에서 this를 사용할 수 있나요?

**A:** 네, 가능하지만 익명 클래스와 의미가 다릅니다.

```java
public class MyClass {
    private String name = "MyClass";
    
    public void test() {
        // 익명 클래스: this = 익명 클래스 자신
        Runnable r1 = new Runnable() {
            private String name = "Anonymous";
            
            @Override
            public void run() {
                System.out.println(this.name);  // "Anonymous"
                System.out.println(MyClass.this.name);  // "MyClass"
            }
        };
        
        // 람다: this = 외부 클래스 (MyClass)
        Runnable r2 = () -> {
            System.out.println(this.name);  // "MyClass"
            // "Anonymous"에 접근할 방법 없음
        };
    }
}
```

**핵심:**
- 익명 클래스: 새로운 스코프 생성 → 자신의 this
- 람다: 렉시컬 스코프 → 외부 클래스의 this

---

### Q3. 왜 람다는 섀도잉이 안 되나요?

**A:** 람다는 새로운 스코프를 생성하지 않기 때문입니다.

```java
int x = 1;

// ✅ 익명 클래스: 새로운 스코프
new Runnable() {
    public void run() {
        int x = 2;  // OK - 새로운 변수
    }
}

// ❌ 람다: 같은 스코프
() -> {
    int x = 2;  // 컴파일 에러 - 변수 중복
}
```

**스코프 구조:**
```
메서드 스코프
├─ x = 1
├─ 익명 클래스
│  └─ 새로운 스코프
│     └─ x = 2 (다른 변수)
└─ 람다
   └─ 같은 스코프
      └─ x = ? (중복!)
```

**해결 방법:**
```java
int x = 1;
() -> {
    int y = 2;  // ✅ 다른 이름 사용
    System.out.println(x + y);
}
```

---

### Q4. effectively final이 뭔가요?

**A:** 값이 변경되지 않는 변수를 의미합니다.

```java
// ✅ effectively final
int x = 10;
Runnable r = () -> System.out.println(x);
// x = 20;  // 이 줄을 추가하면 위 람다 컴파일 에러

// ❌ effectively final 아님
int y = 10;
y = 20;  // 값 변경
Runnable r2 = () -> System.out.println(y);  // 컴파일 에러
```

**왜 필요한가?**
1. **스레드 안전성**: 값이 변하면 예측 불가능
2. **구현 단순화**: final이면 값을 복사해서 저장

**해결 방법:**
```java
// 방법 1: 배열/객체 사용 (우회)
int[] counter = {0};
button.addActionListener(e -> counter[0]++);

// 방법 2: AtomicInteger
AtomicInteger counter = new AtomicInteger(0);
button.addActionListener(e -> counter.incrementAndGet());
```

---

### Q5. 언제 메서드 참조를 사용해야 하나요?

**A:** 람다가 단순히 메서드를 호출만 할 때 사용합니다.

```java
// ✅ 메서드 참조 사용
inventory.sort(Comparator.comparing(Apple::getWeight));

// ❌ 람다 사용 (불필요하게 장황)
inventory.sort(Comparator.comparing(a -> a.getWeight()));

// ✅ 람다 사용 (추가 로직 있음)
inventory.sort(Comparator.comparing(a -> {
    System.out.println("Comparing: " + a);
    return a.getWeight();
}));
```

**선택 기준:**
| 상황 | 선택 | 이유 |
|------|------|------|
| 메서드만 호출 | 메서드 참조 | 간결 |
| 추가 로직 있음 | 람다 | 명확 |
| 복잡한 로직 | 별도 메서드 | 재사용 |

---

## 2. 디자인 패턴 관련 질문

### Q6. 모든 디자인 패턴을 람다로 바꿀 수 있나요?

**A:** 아니요. 함수형 인터페이스 기반 패턴만 가능합니다.

**람다로 가능:**
- ✅ 전략 패턴
- ✅ 템플릿 메서드 패턴
- ✅ 옵저버 패턴 (간단한 경우)
- ✅ 의무 체인 패턴
- ✅ 팩토리 패턴 (단순한 경우)

**람다로 불가능 / 부적합:**
- ❌ 싱글톤 패턴 (인스턴스 관리)
- ❌ 빌더 패턴 (복잡한 상태)
- ❌ 어댑터 패턴 (여러 메서드 구현)
- ❌ 데코레이터 패턴 (복잡한 위임)

---

### Q7. 옵저버 패턴에서 언제 람다를 사용하나요?

**A:** 옵저버가 간단하고 상태가 없을 때만 사용합니다.

**람다 적합:**
```java
// ✅ 간단한 알림 처리
feed.registerObserver(tweet -> {
    if (tweet.contains("money")) {
        System.out.println("Breaking news!");
    }
});
```

**클래스 유지:**
```java
// ❌ 상태를 가지는 옵저버
class StatefulObserver implements Observer {
    private int notificationCount = 0;  // 상태
    private Set<String> keywords = new HashSet<>();  // 상태
    
    @Override
    public void notify(String tweet) {
        notificationCount++;
        // 복잡한 로직...
    }
    
    public int getNotificationCount() {  // 추가 메서드
        return notificationCount;
    }
}
```

**판단 기준:**
- 상태 없음 + 간단 → 람다
- 상태 있음 or 복잡 → 클래스

---

### Q8. 팩토리 패턴에서 생성자에 인수가 필요하면?

**A:** 여러 해결 방법이 있습니다.

**문제:**
```java
// ❌ Supplier는 인수 없는 생성자만
Map<String, Supplier<Product>> map = new HashMap<>();
map.put("loan", Loan::new);  // Loan() 가능
// Loan(double, double)는 불가능
```

**해결 방법 1: TriFunction**
```java
@FunctionalInterface
interface TriFunction<T, U, V, R> {
    R apply(T t, U u, V v);
}

Map<String, TriFunction<Double, Double, String, Product>> factories = 
    new HashMap<>();
factories.put("loan", (amount, rate, customer) -> 
    new Loan(amount, rate, customer));

// 사용
Product loan = factories.get("loan").apply(10000.0, 0.05, "Alice");
```

**해결 방법 2: 빌더 패턴 (권장)**
```java
Map<String, Supplier<ProductBuilder>> builders = new HashMap<>();
builders.put("loan", LoanBuilder::new);

// 사용
Product loan = builders.get("loan").get()
    .amount(10000.0)
    .rate(0.05)
    .customer("Alice")
    .build();
```

**해결 방법 3: Map 중첩**
```java
Map<String, Function<Config, Product>> factories = new HashMap<>();
factories.put("loan", config -> new Loan(
    config.getDouble("amount"),
    config.getDouble("rate"),
    config.getString("customer")
));

// 사용
Config config = new Config()
    .set("amount", 10000.0)
    .set("rate", 0.05)
    .set("customer", "Alice");
Product loan = factories.get("loan").apply(config);
```

---

### Q9. 의무 체인 패턴에서 여러 타입을 처리하려면?

**A:** 제네릭과 Function을 조합합니다.

```java
public class GenericChain<T> {
    
    public static <T> Function<T, T> chain(
            Function<T, T>... functions) {
        
        return Arrays.stream(functions)
            .reduce(Function.identity(), Function::andThen);
    }
}

// 사용 1: String 처리
Function<String, String> stringPipeline = GenericChain.chain(
    s -> s.toUpperCase(),
    s -> s.trim(),
    s -> s.replaceAll("\\s+", "_")
);
String result1 = stringPipeline.apply("  hello world  ");
// "HELLO_WORLD"

// 사용 2: Integer 처리
Function<Integer, Integer> intPipeline = GenericChain.chain(
    n -> n * 2,
    n -> n + 10,
    n -> n / 2
);
Integer result2 = intPipeline.apply(5);
// (5 * 2 + 10) / 2 = 10
```

---

## 3. 테스팅 관련 질문

### Q10. 람다를 어떻게 테스트하나요?

**A:** 람다 자체가 아닌 **람다를 사용하는 메서드**를 테스트합니다.

```java
// ❌ 잘못된 접근: 람다를 직접 테스트하려 함
public class MyClass {
    public void process() {
        list.stream()
            .filter(s -> s.length() > 3)  // ← 이 람다를 어떻게 테스트?
            .collect(toList());
    }
}

// ✅ 올바른 접근: 메서드의 동작을 테스트
@Test
public void testProcess() {
    MyClass myClass = new MyClass();
    List<String> input = Arrays.asList("hi", "hello", "a");
    List<String> result = myClass.process(input);
    
    assertEquals(Arrays.asList("hello"), result);
}
```

**예외: public 필드**
```java
// ✅ public 필드는 직접 테스트 가능
public class Point {
    public static final Comparator<Point> compareByX = 
        Comparator.comparing(Point::getX);
}

@Test
public void testComparator() {
    int result = Point.compareByX.compare(
        new Point(5, 10),
        new Point(3, 15)
    );
    assertTrue(result > 0);
}
```

---

### Q11. 복잡한 람다는 어떻게 테스트하나요?

**A:** 메서드로 추출한 후 테스트합니다.

```java
// ❌ Before: 테스트 어려운 복잡한 람다
public List<String> process(List<String> words) {
    return words.stream()
        .filter(s -> {
            // 20줄의 복잡한 검증 로직
            // ...
        })
        .collect(toList());
}

// ✅ After: 메서드로 추출
public List<String> process(List<String> words) {
    return words.stream()
        .filter(this::isValid)  // 메서드 참조
        .collect(toList());
}

// 테스트 가능한 메서드
boolean isValid(String s) {
    // 복잡한 검증 로직
    // ...
}

// 테스트
@Test
public void testIsValid() {
    assertTrue(isValid("validString"));
    assertFalse(isValid("invalid"));
}
```

---

### Q12. 고차원 함수는 어떻게 테스트하나요?

**A:** 다양한 람다/함수로 동작을 검증합니다.

```java
// 고차원 함수
public static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
    return list.stream()
        .filter(predicate)
        .collect(toList());
}

// 테스트: 여러 Predicate로 검증
@Test
public void testFilter() {
    List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
    
    // 짝수 필터
    List<Integer> even = filter(numbers, n -> n % 2 == 0);
    assertEquals(Arrays.asList(2, 4), even);
    
    // 3보다 큰 수
    List<Integer> greaterThan3 = filter(numbers, n -> n > 3);
    assertEquals(Arrays.asList(4, 5), greaterThan3);
    
    // 모든 수
    List<Integer> all = filter(numbers, n -> true);
    assertEquals(numbers, all);
    
    // 빈 결과
    List<Integer> none = filter(numbers, n -> false);
    assertTrue(none.isEmpty());
}
```

---

### Q13. 스트림 체인을 어떻게 테스트하나요?

**A:** 최종 결과를 테스트하거나, 중간 결과가 중요하면 단계별로 분리합니다.

**방법 1: 최종 결과 테스트**
```java
@Test
public void testStreamChain() {
    List<String> words = Arrays.asList("apple", "banana", "cherry");
    
    List<String> result = words.stream()
        .filter(s -> s.length() > 5)
        .map(String::toUpperCase)
        .sorted()
        .collect(toList());
    
    assertEquals(Arrays.asList("BANANA", "CHERRY"), result);
}
```

**방법 2: 단계별 분리 (중간 결과 중요 시)**
```java
@Test
public void testStreamChainStepByStep() {
    List<String> words = Arrays.asList("apple", "banana", "cherry");
    
    // Step 1: 필터링
    List<String> filtered = words.stream()
        .filter(s -> s.length() > 5)
        .collect(toList());
    assertEquals(Arrays.asList("banana", "cherry"), filtered);
    
    // Step 2: 변환
    List<String> mapped = filtered.stream()
        .map(String::toUpperCase)
        .collect(toList());
    assertEquals(Arrays.asList("BANANA", "CHERRY"), mapped);
    
    // Step 3: 정렬
    List<String> sorted = mapped.stream()
        .sorted()
        .collect(toList());
    assertEquals(Arrays.asList("BANANA", "CHERRY"), sorted);
}
```

---

## 4. 디버깅 관련 질문

### Q14. 람다 스택 트레이스를 어떻게 읽나요?

**A:** `lambda$메서드명$번호` 패턴을 이해하면 됩니다.

```java
public class Example {
    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(1, 2, 3);
        
        numbers.stream()
            .map(n -> n * 2)           // lambda$main$0
            .filter(n -> n > 10)       // lambda$main$1
            .map(n -> n / 0)           // lambda$main$2 ← 에러!
            .forEach(System.out::println);
    }
}
```

**스택 트레이스:**
```
Exception in thread "main" java.lang.ArithmeticException: / by zero
    at Example.lambda$main$2(Example.java:8)
                    ↑     ↑
                    |     └─ 세 번째 람다 (0부터 시작)
                    └─ main 메서드
```

**분석:**
- `lambda$main$2`: main 메서드의 세 번째 람다
- 8번 줄 확인: `.map(n -> n / 0)`

---

### Q15. 메서드 참조는 스택 트레이스에 어떻게 나타나나요?

**A:** 같은 클래스 메서드 참조는 **메서드 이름**이 나타납니다.

```java
public class Example {
    
    public static int divideByZero(int n) {
        return n / 0;
    }
    
    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(1, 2, 3);
        
        numbers.stream()
            .map(Example::divideByZero)  // 메서드 참조
            .forEach(System.out::println);
    }
}
```

**스택 트레이스:**
```
Exception in thread "main" java.lang.ArithmeticException: / by zero
    at Example.divideByZero(Example.java:4)
                    ↑ 명확한 메서드 이름!
    at java.util.stream.ReferencePipeline$3$1.accept(...)
```

**장점:**
- ✅ 메서드 이름으로 즉시 파악 가능
- ✅ 디버깅 용이

---

### Q16. peek를 언제 사용하나요?

**A:** 스트림 중간 값을 확인할 때 사용합니다.

```java
List<Integer> result = numbers.stream()
    .peek(n -> System.out.println("Original: " + n))
    .map(n -> n * 2)
    .peek(n -> System.out.println("After map: " + n))
    .filter(n -> n > 10)
    .peek(n -> System.out.println("After filter: " + n))
    .collect(toList());
```

**주의:**
- `peek`는 **중간 연산** (스트림 소비 안 함)
- `forEach`는 **최종 연산** (스트림 소비함)

```java
// ❌ forEach는 최종 연산
numbers.stream()
    .forEach(n -> System.out.println(n))
    .map(n -> n * 2);  // 컴파일 에러! 이미 소비됨

// ✅ peek는 중간 연산
numbers.stream()
    .peek(n -> System.out.println(n))
    .map(n -> n * 2)  // OK
    .collect(toList());
```

---

### Q17. 람다 디버깅이 너무 어려운데 팁이 있나요?

**A:** 여러 전략을 조합해서 사용하세요.

**전략 1: 메서드 참조 사용**
```java
// ❌ 디버깅 어려움
.map(n -> n / 0)

// ✅ 메서드 참조
.map(MyClass::divideByZero)  // 스택 트레이스에 메서드 이름 표시
```

**전략 2: peek 활용**
```java
.map(n -> {
    System.out.println("Processing: " + n);
    return n * 2;
})
```

**전략 3: 단계별 분리**
```java
// ❌ 체이닝
List<String> result = words.stream()
    .filter(...)
    .map(...)
    .sorted()
    .collect(toList());

// ✅ 단계별
Stream<String> stream1 = words.stream();
Stream<String> stream2 = stream1.filter(...);
Stream<String> stream3 = stream2.map(...);
// 각 단계에서 브레이크포인트
```

**전략 4: try-catch**
```java
.map(n -> {
    try {
        return complexOperation(n);
    } catch (Exception e) {
        System.err.println("Error processing " + n + ": " + e);
        return defaultValue;
    }
})
```

---

## 5. 실전 응용 질문

### Q18. 프로덕션 코드에서 람다와 클래스 중 어떤 걸 선택해야 하나요?

**A:** 다음 기준으로 판단하세요.

| 기준 | 람다 | 클래스 |
|------|------|--------|
| **코드 길이** | 1-5줄 | 10줄+ |
| **재사용** | 1-2곳 | 3곳+ |
| **복잡도** | 간단 | 복잡 |
| **상태** | 없음 | 있음 |
| **테스트** | 불필요 | 필요 |
| **디버깅** | 쉬움 | 어려움 시 |

**예시:**
```java
// ✅ 람다: 간단, 재사용 안 함, 상태 없음
button.addActionListener(e -> System.out.println("Clicked"));

// ✅ 클래스: 복잡, 재사용, 상태 있음
class ComplexValidator implements Validator {
    private Set<String> invalidWords = new HashSet<>();
    private int validationCount = 0;
    
    @Override
    public boolean validate(String input) {
        validationCount++;
        // 복잡한 검증 로직...
    }
}
```

---

### Q19. 성능이 중요한 코드에서 람다를 사용해도 되나요?

**A:** 대부분의 경우 문제없지만, 극한 성능이 필요하면 측정 후 결정하세요.

**람다 오버헤드:**
- 첫 호출: invokedynamic으로 구현 생성 (약간의 오버헤드)
- 이후 호출: 거의 무시할 수준

**벤치마크 예시:**
```java
// 1억 번 호출
// 람다: 약 100ms
// 익명 클래스: 약 105ms
// 차이: 5% (무시 가능)

// 단, 람다 안에서 복잡한 작업 시 차이 없음
```

**권장사항:**
- 일반 코드: 람다 사용 (가독성 우선)
- 극한 성능: 측정 후 판단
- 핫스팟: 인라인 가능한 메서드 참조

---

### Q20. 람다와 스트림을 언제 사용하지 말아야 하나요?

**A:** 다음 경우에는 피하는 것이 좋습니다.

**1. 디버깅이 매우 중요한 경우**
```java
// ❌ 복잡한 비즈니스 로직
payments.stream()
    .filter(p -> /* 복잡한 검증 */)
    .map(p -> /* 복잡한 변환 */)
    .reduce(/* 복잡한 집계 */);

// ✅ 명확한 반복문
for (Payment p : payments) {
    if (isValid(p)) {  // 디버깅 쉬움
        transform(p);
    }
}
```

**2. 성능이 극도로 중요한 경우**
```java
// ❌ 스트림 오버헤드
int sum = IntStream.range(0, 1_000_000_000)
    .filter(n -> n % 2 == 0)
    .sum();

// ✅ 전통적 반복문 (약간 빠름)
int sum = 0;
for (int i = 0; i < 1_000_000_000; i++) {
    if (i % 2 == 0) {
        sum += i;
    }
}
```

**3. 팀이 익숙하지 않은 경우**
- 점진적 도입
- 코드 리뷰로 학습
- 간단한 것부터 시작

---

### Q21. 리팩터링 시 어느 순서로 진행해야 하나요?

**A:** 다음 순서를 권장합니다.

**1단계: 익명 클래스 → 람다**
```java
// Before
list.sort(new Comparator<String>() {
    @Override
    public int compare(String s1, String s2) {
        return s1.compareTo(s2);
    }
});

// After
list.sort((s1, s2) -> s1.compareTo(s2));
```

**2단계: 람다 → 메서드 참조**
```java
// After
list.sort(String::compareTo);
```

**3단계: 명령형 → 스트림**
```java
// Before
List<String> result = new ArrayList<>();
for (String s : list) {
    if (s.length() > 3) {
        result.add(s.toUpperCase());
    }
}

// After
List<String> result = list.stream()
    .filter(s -> s.length() > 3)
    .map(String::toUpperCase)
    .collect(toList());
```

**4단계: 복잡한 람다 → 메서드 분리**
```java
// Before
.filter(s -> { /* 10줄 */ })

// After
.filter(this::isValid)
```

---

### Q22. 레거시 코드를 리팩터링할 때 주의사항은?

**A:** 점진적으로 진행하고, 테스트를 작성하세요.

**1. 테스트 먼저**
```java
// 1. 기존 동작 테스트 작성
@Test
public void testExistingBehavior() {
    // 리팩터링 전 동작 확인
}

// 2. 리팩터링

// 3. 테스트로 검증
```

**2. 작은 단위로**
```java
// ❌ 한 번에 모두 변경
public void process() {
    // 100줄을 한 번에 스트림으로...
}

// ✅ 메서드별로 하나씩
public void process() {
    List<User> users = getUsers();  // ← 먼저 리팩터링
    validate(users);                // ← 다음 리팩터링
    save(users);                    // ← 마지막 리팩터링
}
```

**3. 동료 리뷰**
```java
// 리팩터링 후 반드시 리뷰
// - 가독성 향상?
// - 버그 없음?
// - 성능 저하 없음?
```

---

### Q23. 람다를 사용하면서 흔히 하는 실수는?

**A:** 다음을 주의하세요.

**실수 1: 부수 효과**
```java
// ❌ 나쁨: 외부 상태 변경
List<String> result = new ArrayList<>();
stream.forEach(s -> result.add(s.toUpperCase()));

// ✅ 좋음: 수집자 사용
List<String> result = stream
    .map(String::toUpperCase)
    .collect(toList());
```

**실수 2: 과도한 체이닝**
```java
// ❌ 가독성 저하
result = stream
    .filter(...)
    .map(...)
    .flatMap(...)
    .filter(...)
    .map(...)
    .filter(...)
    .collect(...);

// ✅ 적절히 분리
Stream<T> filtered = stream.filter(...).map(...);
Stream<T> processed = filtered.flatMap(...).filter(...);
List<T> result = processed.collect(...);
```

**실수 3: 스트림 재사용**
```java
// ❌ 스트림은 한 번만 사용 가능
Stream<String> stream = list.stream();
stream.forEach(System.out::println);
stream.count();  // IllegalStateException!

// ✅ 새 스트림 생성
list.stream().forEach(System.out::println);
long count = list.stream().count();
```

---

## 핵심 요약

### 리팩터링
```
✅ 간단한 익명 클래스 → 람다
✅ 단순 람다 → 메서드 참조
✅ 복잡한 람다 → 메서드 분리
✅ 명령형 반복 → 스트림
```

### 테스팅
```
✅ 람다 자체가 아닌 메서드 동작 테스트
✅ public 필드는 직접 테스트
✅ 복잡하면 메서드로 추출
✅ 고차원 함수는 다양한 입력으로 검증
```

### 디버깅
```
✅ 메서드 참조로 명확한 스택 트레이스
✅ peek로 중간 값 확인
✅ 단계별 분리로 디버깅 용이화
✅ try-catch로 예외 처리
```

### 실전
```
✅ 3줄 이하 → 람다
✅ 10줄 이상 → 클래스
✅ 테스트 먼저, 리팩터링 나중
✅ 점진적 도입, 팀과 함께
```

---

**작성일**: 2024년 12월  
**대상**: Modern Java In Action Chapter 9  
**난이도**: ⭐⭐⭐⭐ (중급~고급)
