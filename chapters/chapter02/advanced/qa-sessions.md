# AI와의 대화로 배우는 동작 파라미터화

> Claude AI와의 실제 대화를 통해 개념을 깊이 이해하는 과정

---

## 📌 이 문서에 대하여

이 문서는 동작 파라미터화를 학습하면서 AI와 나눈 질문과 답변을 정리한 것입니다.
단순한 지식 전달이 아닌, **질문을 통한 깊이 있는 이해**를 목표로 합니다.

---

## 💬 Session 1: 동작 파라미터화의 본질

### Q1: 동작 파라미터화가 정확히 뭔가요?

**A:** 동작 파라미터화는 **메서드의 동작을 파라미터로 전달**하는 기법입니다.

전통적으로는 데이터만 파라미터로 전달할 수 있었습니다:
```java
int add(int a, int b) {
    return a + b;
}
```

하지만 동작 파라미터화를 사용하면 **"무엇을 할지"** 도 전달할 수 있습니다:
```java
void process(List<Data> list, Action action) {
    for (Data d : list) {
        action.execute(d);  // 어떤 동작을 할지는 action이 결정
    }
}
```

### Q2: 왜 이게 필요한가요?

**A:** 요구사항은 끊임없이 변하기 때문입니다.

예를 들어:
1. "녹색 사과만 필터링해주세요"
2. "빨간 사과도 필터링할 수 있어야 해요"
3. "무거운 사과도 필터링하고 싶어요"
4. "녹색이면서 무거운 사과를 찾고 싶어요"

각 요구사항마다 새로운 메서드를 만들면:
- 코드 중복 증가
- 유지보수 어려움
- DRY 원칙 위반

동작 파라미터화를 사용하면 **하나의 메서드로 모든 요구사항 처리**:
```java
filter(apples, a -> a.getColor() == GREEN);
filter(apples, a -> a.getColor() == RED);
filter(apples, a -> a.getWeight() > 150);
filter(apples, a -> a.getColor() == GREEN && a.getWeight() > 150);
```

---

## 💬 Session 2: Predicate 패턴

### Q3: Predicate가 뭔가요?

**A:** Predicate는 **참/거짓을 반환하는 함수**를 추상화한 인터페이스입니다.

```java
@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);  // T를 받아서 boolean 반환
}
```

**사용 예:**
```java
Predicate<Apple> isGreen = apple -> apple.getColor() == GREEN;
Predicate<Apple> isHeavy = apple -> apple.getWeight() > 150;

// test() 메서드로 조건 검사
if (isGreen.test(myApple)) {
    System.out.println("녹색 사과입니다!");
}
```

### Q4: Predicate.and(), or(), negate()는 어떻게 동작하나요?

**A:** Predicate는 **조합 가능**합니다.

```java
Predicate<Apple> isGreen = a -> a.getColor() == GREEN;
Predicate<Apple> isHeavy = a -> a.getWeight() > 150;

// AND: 둘 다 만족
Predicate<Apple> greenAndHeavy = isGreen.and(isHeavy);
// 내부: apple -> isGreen.test(apple) && isHeavy.test(apple)

// OR: 하나라도 만족
Predicate<Apple> greenOrHeavy = isGreen.or(isHeavy);
// 내부: apple -> isGreen.test(apple) || isHeavy.test(apple)

// NOT: 반대
Predicate<Apple> notGreen = isGreen.negate();
// 내부: apple -> !isGreen.test(apple)
```

**체이닝:**
```java
Predicate<Apple> complex = isGreen.and(isHeavy).or(isFresh);
// (녹색 AND 무거운) OR 신선한
```

---

## 💬 Session 3: Consumer와 Function

### Q5: Consumer는 언제 사용하나요?

**A:** **부작용(side effect)** 을 수행할 때 사용합니다.

```java
@FunctionalInterface
public interface Consumer<T> {
    void accept(T t);  // T를 받아서 void (결과 없음)
}
```

**Predicate vs Consumer:**
```java
// Predicate: 조건 검사 (true/false 반환)
Predicate<Apple> isGreen = a -> a.getColor() == GREEN;
boolean result = isGreen.test(apple);

// Consumer: 부작용 (반환값 없음)
Consumer<Apple> print = a -> System.out.println(a);
print.accept(apple);  // 출력만 하고 반환값 없음
```

**forEach 패턴:**
```java
public static <T> void forEach(List<T> list, Consumer<T> consumer) {
    for (T item : list) {
        consumer.accept(item);  // 각 요소에 대해 부작용 수행
    }
}

// 사용
forEach(apples, a -> System.out.println(a));  // 출력
forEach(apples, a -> save(a));                 // 저장
forEach(apples, a -> send(a));                 // 전송
```

### Q6: Function은 뭐가 다른가요?

**A:** Function은 **값을 변환**합니다.

```java
@FunctionalInterface
public interface Function<T, R> {
    R apply(T t);  // T를 받아서 R 반환
}
```

**map 패턴:**
```java
public static <T, R> List<R> map(List<T> list, Function<T, R> function) {
    List<R> result = new ArrayList<>();
    for (T item : list) {
        result.add(function.apply(item));  // 변환
    }
    return result;
}

// 사용
List<Integer> weights = map(apples, a -> a.getWeight());
List<String> colors = map(apples, a -> a.getColor());
```

**비교:**
```java
Predicate<T>:  T → boolean  (필터링)
Consumer<T>:   T → void     (부작용)
Function<T,R>: T → R        (변환)
```

---

## 💬 Session 4: Comparator

### Q7: Comparator.comparing()은 어떻게 작동하나요?

**A:** `comparing()`은 **추출 함수를 받아서 Comparator를 생성**합니다.

```java
// 시그니처
public static <T, U extends Comparable<? super U>> Comparator<T> comparing(
    Function<? super T, ? extends U> keyExtractor
)

// 내부 구현 (간략화)
return (c1, c2) -> {
    U key1 = keyExtractor.apply(c1);  // c1에서 키 추출
    U key2 = keyExtractor.apply(c2);  // c2에서 키 추출
    return key1.compareTo(key2);       // 추출된 키 비교
};
```

**사용 예:**
```java
// 무게로 정렬
Comparator<Apple> byWeight = Comparator.comparing(Apple::getWeight);

// 내부적으로 이렇게 동작
Comparator<Apple> byWeight = (a1, a2) -> {
    Integer w1 = a1.getWeight();
    Integer w2 = a2.getWeight();
    return w1.compareTo(w2);  // Integer의 compareTo()
};
```

### Q8: thenComparing()은 어떻게 체이닝되나요?

**A:** `thenComparing()`은 **첫 번째 비교가 같을 때만 두 번째 비교를 수행**합니다.

```java
// 구현
public Comparator<T> thenComparing(Comparator<? super T> other) {
    return (c1, c2) -> {
        int res = this.compare(c1, c2);  // 먼저 this로 비교
        return (res != 0) ? res : other.compare(c1, c2);  // 같으면 other
    };
}
```

**사용:**
```java
students.sort(
    Comparator.comparingInt(Student::getGrade)        // 1차: 학년
              .thenComparingInt(Student::getScore)    // 2차: 성적
              .thenComparing(Student::getName)         // 3차: 이름
);

// 동작
// 1. 학년이 다르면 학년으로 결정
// 2. 학년이 같으면 성적으로 결정
// 3. 성적도 같으면 이름으로 결정
```

---

## 💬 Session 5: 람다와 익명 클래스

### Q9: 람다와 익명 클래스의 차이는 뭔가요?

**A:** 가장 큰 차이는 **this의 의미**와 **변수 섀도잉**입니다.

#### this의 차이

```java
class Example {
    int value = 1;
    
    void test() {
        // 익명 클래스: this는 익명 클래스 자신
        Runnable r1 = new Runnable() {
            int value = 2;
            public void run() {
                System.out.println(this.value);  // 2 (익명 클래스의 value)
            }
        };
        
        // 람다: this는 Example 인스턴스
        Runnable r2 = () -> {
            System.out.println(this.value);  // 1 (Example의 value)
        };
    }
}
```

**람다는 새로운 스코프를 만들지 않습니다!**

#### 변수 섀도잉

```java
void test() {
    int value = 10;
    
    // ✅ 익명 클래스: 섀도잉 가능
    Runnable r1 = new Runnable() {
        public void run() {
            int value = 20;  // OK! 새 변수 선언
            System.out.println(value);  // 20
        }
    };
    
    // ❌ 람다: 섀도잉 불가
    Runnable r2 = () -> {
        // int value = 30;  // 컴파일 에러!
        System.out.println(value);  // 10 (외부 변수만 접근)
    };
}
```

### Q10: 람다가 성능상 더 좋나요?

**A:** 일반적으로 **람다가 더 효율적**입니다.

```java
// 익명 클래스: 매번 새 객체 생성
for (int i = 0; i < 1000; i++) {
    Runnable r = new Runnable() {  // 1000개 객체
        public void run() { }
    };
}

// 람다: 캐싱 가능
for (int i = 0; i < 1000; i++) {
    Runnable r = () -> { };  // 1개 객체 (최적화)
}
```

**이유:**
- 람다는 **invokedynamic** 바이트코드 사용
- JVM이 런타임에 최적화
- 익명 클래스는 컴파일 시 별도 클래스 파일 생성

---

## 💬 Session 6: Runnable vs Callable

### Q11: Runnable과 Callable은 언제 각각 사용하나요?

**A:** **결과가 필요한지**에 따라 선택합니다.

```java
// Runnable: 결과가 필요 없을 때
@FunctionalInterface
public interface Runnable {
    void run();  // () → void
}

// Callable: 결과가 필요할 때
@FunctionalInterface
public interface Callable<V> {
    V call() throws Exception;  // () → V
}
```

**사용 케이스:**

```java
// Runnable: 로깅, 알림 등
new Thread(() -> {
    System.out.println("작업 완료");  // 결과 필요 없음
}).start();

// Callable: 계산 결과 필요
ExecutorService executor = Executors.newSingleThreadExecutor();
Future<Integer> future = executor.submit(() -> {
    return 1 + 2 + 3 + 4 + 5;  // 결과 반환
});
Integer result = future.get();  // 15
```

### Q12: Future는 어떻게 동작하나요?

**A:** Future는 **비동기 계산의 결과를 나타내는 핸들**입니다.

```java
Future<Integer> future = executor.submit(() -> {
    Thread.sleep(1000);  // 1초 걸리는 작업
    return 42;
});

// 여기서 다른 작업 가능
doSomethingElse();

// 필요할 때 결과 받기
Integer result = future.get();  // 블로킹! 완료까지 대기
```

**주요 메서드:**
```java
future.get();             // 결과 받기 (블로킹)
future.get(1, SECONDS);   // 타임아웃 설정
future.isDone();          // 완료 여부
future.cancel(true);      // 취소
```

---

## 💬 Session 7: 전략 패턴

### Q13: 전략 패턴이 정확히 뭔가요?

**A:** 전략 패턴은 **알고리즘을 캡슐화하여 런타임에 선택**하는 패턴입니다.

**구조:**
```java
// 1. 전략 인터페이스
interface PaymentStrategy {
    void pay(int amount);
}

// 2. 구체적 전략들
class CreditCardStrategy implements PaymentStrategy {
    public void pay(int amount) {
        System.out.println("신용카드로 " + amount + "원 결제");
    }
}

class CashStrategy implements PaymentStrategy {
    public void pay(int amount) {
        System.out.println("현금으로 " + amount + "원 결제");
    }
}

// 3. 컨텍스트 (전략 사용)
class ShoppingCart {
    public void checkout(int amount, PaymentStrategy strategy) {
        strategy.pay(amount);  // 전략 실행!
    }
}

// 4. 사용
cart.checkout(10000, new CreditCardStrategy());
cart.checkout(5000, new CashStrategy());

// 람다로 간결하게
cart.checkout(10000, amount -> System.out.println("카드 " + amount));
```

**동작 파라미터화 = 전략 패턴의 함수형 구현!**

---

## 💬 Session 8: 제너릭 일반화

### Q14: 왜 제너릭으로 일반화해야 하나요?

**A:** **재사용성**을 극대화하기 위해서입니다.

```java
// ❌ Apple에만 동작
public static List<Apple> filter(List<Apple> list, ApplePredicate p) {
    // ...
}

// ✅ 모든 타입에 동작
public static <T> List<T> filter(List<T> list, Predicate<T> p) {
    // ...
}

// 사용
List<Apple> apples = filter(appleList, a -> a.getColor() == GREEN);
List<Integer> evens = filter(numbers, n -> n % 2 == 0);
List<String> longs = filter(strings, s -> s.length() > 5);
```

**하나의 메서드로 모든 타입 처리!**

---

## 🎯 핵심 통찰

### 배운 것들

1. **동작 파라미터화** = 코드를 데이터처럼 전달
2. **Predicate** = 조건 검사
3. **Consumer** = 부작용
4. **Function** = 값 변환
5. **Comparator** = 정렬 규칙
6. **Runnable/Callable** = 스레드 동작
7. **전략 패턴** = 동작 파라미터화의 객체지향 구현
8. **제너릭** = 타입 안전한 일반화

### 실전 원칙

```java
// ✅ DO: 간결하고 명확한 람다
filter(list, x -> x.getValue() > 100);

// ❌ DON'T: 복잡한 람다
filter(list, x -> {
    // 10줄의 복잡한 로직
});

// ✅ DO: 메서드 참조
list.sort(Comparator.comparing(Apple::getWeight));

// ❌ DON'T: 불필요한 람다
list.sort((a, b) -> Apple::getWeight);
```

---

<div align="center">

**💡 최종 메시지**

> *"질문하고, 대화하고, 이해하라.*  
> *AI는 선생님이 아니라 대화 상대다."*

**계속 질문하고, 계속 배우세요! 🚀**

</div>
