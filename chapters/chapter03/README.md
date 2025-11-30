# Chapter 03. 람다 표현식

> **핵심 주제**: 메서드를 인수로 전달하는 자바 8의 새로운 기능, 람다 표현식

---

## 📚 학습 목표

이 챕터를 마치면 다음을 할 수 있습니다:

- ✅ 람다 표현식이 무엇인지 이해한다
- ✅ 함수형 인터페이스를 사용하여 람다를 활용한다
- ✅ 실행 어라운드 패턴으로 코드를 개선한다
- ✅ java.util.function의 주요 함수형 인터페이스를 사용한다
- ✅ 형식 검사, 형식 추론, 제약을 이해한다
- ✅ 메서드 참조로 람다를 간결하게 표현한다
- ✅ 람다 표현식을 조합하여 복잡한 동작을 만든다

---

## 🎯 핵심 개념

### 람다란?

**람다 표현식 = 메서드로 전달할 수 있는 익명 함수를 단순화한 것**

```java
// Before: 익명 클래스
Comparator<Apple> byWeight = new Comparator<Apple>() {
    public int compare(Apple a1, Apple a2) {
        return a1.getWeight().compareTo(a2.getWeight());
    }
};

// After: 람다 표현식
Comparator<Apple> byWeight = 
    (Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight());
```

**람다의 4가지 특징:**

1. **익명 (Anonymous)**: 이름이 없다
2. **함수 (Function)**: 클래스에 종속되지 않는다
3. **전달 (Passed)**: 메서드 인수나 변수로 전달 가능
4. **간결 (Concise)**: 자질구레한 코드가 줄어든다

---

### 람다 문법

```java
// 기본 형태
(파라미터) -> 표현식

// 블록 형태
(파라미터) -> { 문장들; }
```

**예시:**

```java
// 1. 파라미터 없음, 상수 반환
() -> 42

// 2. 파라미터 하나, 표현식
(String s) -> s.length()

// 3. 파라미터 여러 개, 표현식
(int x, int y) -> x + y

// 4. 파라미터 여러 개, 블록
(int x, int y) -> {
    System.out.println("합계 계산");
    return x + y;
}
```

---

## 🔑 핵심 내용

### 3.1 람다란 무엇인가?

**람다 구조:**
```
(Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight())
 ^^^^^^^^^^^^^^^^      ^^  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 파라미터 리스트      화살표          람다 바디
```

**구성 요소:**
- **파라미터 리스트**: 메서드의 파라미터
- **화살표 (->)**: 파라미터와 바디 구분
- **람다 바디**: 람다의 반환값

---

### 3.2 함수형 인터페이스

**정의:** 정확히 하나의 추상 메서드를 지정하는 인터페이스

```java
@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);  // 추상 메서드 (단 하나!)
}
```

**함수 디스크립터:**
- 함수형 인터페이스의 추상 메서드 시그니처
- 람다 표현식의 시그니처를 서술

```java
Runnable: () -> void
Predicate<T>: (T) -> boolean
Comparator<T>: (T, T) -> int
```

---

### 3.3 실행 어라운드 패턴

**패턴:**
```
초기화/준비 코드
    ↓
실제 작업 (변경 가능)
    ↓
정리/마무리 코드
```

**예시: 파일 읽기**

```java
// 1단계: 동작 파라미터화
String result = processFile((BufferedReader br) -> br.readLine());

// 2단계: 함수형 인터페이스 정의
@FunctionalInterface
public interface BufferedReaderProcessor {
    String process(BufferedReader b) throws IOException;
}

// 3단계: 동작 실행
public String processFile(BufferedReaderProcessor p) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
        return p.process(br);  // 람다 실행!
    }
}

// 4단계: 람다 전달
String oneLine = processFile((BufferedReader br) -> br.readLine());
String twoLines = processFile((BufferedReader br) -> 
    br.readLine() + br.readLine());
```

---

### 3.4 함수형 인터페이스 사용

#### 3.4.1 Predicate<T>

**시그니처:** `(T) -> boolean`

```java
@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);
}

// 사용
Predicate<String> nonEmpty = (String s) -> !s.isEmpty();
List<String> nonEmptyStrings = filter(strings, nonEmpty);
```

---

#### 3.4.2 Consumer<T>

**시그니처:** `(T) -> void`

```java
@FunctionalInterface
public interface Consumer<T> {
    void accept(T t);
}

// 사용
forEach(Arrays.asList(1,2,3,4,5), 
    (Integer i) -> System.out.println(i));
```

---

#### 3.4.3 Function<T, R>

**시그니처:** `(T) -> R`

```java
@FunctionalInterface
public interface Function<T, R> {
    R apply(T t);
}

// 사용
List<Integer> lengths = map(
    Arrays.asList("Modern", "Java", "In", "Action"),
    (String s) -> s.length()
);
```

---

#### 3.4.4 기본형 특화

**문제:** 제네릭은 참조형만 사용 가능 → 박싱 비용

**해결:** 기본형 특화 함수형 인터페이스

```java
// 박싱 발생 (느림)
Predicate<Integer> evenNumbers = (Integer i) -> i % 2 == 0;

// 박싱 없음 (빠름)
IntPredicate evenNumbers = (int i) -> i % 2 == 0;
```

**주요 기본형 특화 인터페이스:**

| 인터페이스 | 함수 디스크립터 | 예시 |
|-----------|----------------|------|
| `IntPredicate` | `int -> boolean` | `i -> i > 0` |
| `LongConsumer` | `long -> void` | `l -> System.out.println(l)` |
| `DoubleFunction<R>` | `double -> R` | `d -> Double.toString(d)` |
| `IntUnaryOperator` | `int -> int` | `i -> i * i` |
| `DoubleBinaryOperator` | `(double, double) -> double` | `(d1, d2) -> d1 + d2` |

---

### 3.5 형식 검사, 형식 추론, 제약

#### 3.5.1 형식 검사

**람다의 형식 = 대상 형식 (target type)**

```java
List<Apple> heavyApples = 
    filter(inventory, (Apple a) -> a.getWeight() > 150);
```

**형식 검사 과정:**
```
1. filter 메서드 확인
   ↓
2. 두 번째 파라미터 타입 확인: Predicate<Apple>
   ↓
3. Predicate<Apple>의 추상 메서드: test(Apple) -> boolean
   ↓
4. 람다 시그니처 확인: (Apple) -> boolean
   ↓
5. 일치! ✅
```

---

#### 3.5.2 형식 추론

**컴파일러가 대상 형식으로 람다 파라미터 타입 추론**

```java
// 형식 명시
Comparator<Apple> c = 
    (Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight());

// 형식 추론 (권장)
Comparator<Apple> c = 
    (a1, a2) -> a1.getWeight().compareTo(a2.getWeight());
```

---

#### 3.5.3 지역 변수 제약

**규칙:** 람다는 `final` 또는 `effectively final` 지역 변수만 캡처 가능

```java
// ✅ OK
int portNumber = 1337;
Runnable r = () -> System.out.println(portNumber);

// ❌ 컴파일 에러
int portNumber = 1337;
Runnable r = () -> System.out.println(portNumber);
portNumber = 31337;  // 재할당!
```

**이유:**
```
지역 변수: Stack에 저장
    ↓
람다: 변수의 복사본 캡처
    ↓
원본이 변경되면 복사본과 불일치
    ↓
final만 허용하여 일관성 보장
```

---

### 3.6 메서드 참조

**람다를 더 간결하게!**

```java
// 람다
inventory.sort((Apple a1, Apple a2) -> 
    a1.getWeight().compareTo(a2.getWeight()));

// 메서드 참조
inventory.sort(comparing(Apple::getWeight));
```

#### 3.6.1 메서드 참조 유형

**1. 정적 메서드 참조**
```java
(String s) -> Integer.parseInt(s)
    ↓
Integer::parseInt
```

**2. 인스턴스 메서드 참조 (임의 객체)**
```java
(String s) -> s.toUpperCase()
    ↓
String::toUpperCase
```

**3. 인스턴스 메서드 참조 (기존 객체)**
```java
() -> expensiveTransaction.getValue()
    ↓
expensiveTransaction::getValue
```

---

#### 3.6.2 생성자 참조

```java
// 인수 없는 생성자
Supplier<Apple> c1 = () -> new Apple();
    ↓
Supplier<Apple> c1 = Apple::new;

// 인수 하나
Function<Integer, Apple> c2 = (weight) -> new Apple(weight);
    ↓
Function<Integer, Apple> c2 = Apple::new;

// 인수 두 개
BiFunction<Color, Integer, Apple> c3 = 
    (color, weight) -> new Apple(color, weight);
    ↓
BiFunction<Color, Integer, Apple> c3 = Apple::new;
```

---

### 3.7 람다, 메서드 참조 활용

**진화 과정:**

```java
// 1단계: 코드 전달
inventory.sort(new AppleComparator());

// 2단계: 익명 클래스
inventory.sort(new Comparator<Apple>() {
    public int compare(Apple a1, Apple a2) {
        return a1.getWeight().compareTo(a2.getWeight());
    }
});

// 3단계: 람다
inventory.sort((Apple a1, Apple a2) -> 
    a1.getWeight().compareTo(a2.getWeight()));

// 3-1단계: 형식 추론
inventory.sort((a1, a2) -> 
    a1.getWeight().compareTo(a2.getWeight()));

// 4단계: 메서드 참조
inventory.sort(comparing(Apple::getWeight));
```

---

### 3.8 람다 표현식 조합

#### 3.8.1 Comparator 조합

```java
// 역정렬
inventory.sort(comparing(Apple::getWeight).reversed());

// Comparator 연결
inventory.sort(comparing(Apple::getWeight)
    .reversed()
    .thenComparing(Apple::getCountry));
```

---

#### 3.8.2 Predicate 조합

```java
Predicate<Apple> redApple = a -> Color.RED.equals(a.getColor());

// NOT
Predicate<Apple> notRed = redApple.negate();

// AND
Predicate<Apple> redAndHeavy = 
    redApple.and(a -> a.getWeight() > 150);

// OR
Predicate<Apple> redAndHeavyOrGreen = 
    redApple
        .and(a -> a.getWeight() > 150)
        .or(a -> Color.GREEN.equals(a.getColor()));
```

---

#### 3.8.3 Function 조합

```java
Function<Integer, Integer> f = x -> x + 1;
Function<Integer, Integer> g = x -> x * 2;

// andThen: f 실행 → g 실행
Function<Integer, Integer> h = f.andThen(g);
h.apply(1);  // (1 + 1) * 2 = 4

// compose: g 실행 → f 실행
Function<Integer, Integer> h = f.compose(g);
h.apply(1);  // (1 * 2) + 1 = 3
```

---

## 📊 함수형 인터페이스 치트시트

### java.util.function 주요 인터페이스

| 인터페이스 | 함수 디스크립터 | 추상 메서드 | 사용 예시 |
|-----------|----------------|-----------|----------|
| `Predicate<T>` | `T -> boolean` | `boolean test(T t)` | 필터링 |
| `Consumer<T>` | `T -> void` | `void accept(T t)` | 출력, 저장 |
| `Function<T,R>` | `T -> R` | `R apply(T t)` | 변환, 매핑 |
| `Supplier<T>` | `() -> T` | `T get()` | 팩토리 |
| `UnaryOperator<T>` | `T -> T` | `T apply(T t)` | 단항 연산 |
| `BinaryOperator<T>` | `(T,T) -> T` | `T apply(T t1, T t2)` | 이항 연산 |
| `BiPredicate<T,U>` | `(T,U) -> boolean` | `boolean test(T t, U u)` | 두 값 비교 |
| `BiConsumer<T,U>` | `(T,U) -> void` | `void accept(T t, U u)` | 두 값 처리 |
| `BiFunction<T,U,R>` | `(T,U) -> R` | `R apply(T t, U u)` | 두 값 변환 |

---

## 💡 핵심 정리

### 람다 사용 시 기억할 것

**1. 함수형 인터페이스에서만 사용**
```java
// ✅ OK: Runnable은 함수형 인터페이스
Runnable r = () -> System.out.println("Hello");

// ❌ 에러: List는 함수형 인터페이스 아님
List<String> list = () -> new ArrayList<>();  // 컴파일 에러!
```

**2. 예외 처리**
```java
// 람다 내부에서 try-catch
Function<String, Integer> parse = (String s) -> {
    try {
        return Integer.parseInt(s);
    } catch (NumberFormatException e) {
        return 0;
    }
};
```

**3. 지역 변수는 final**
```java
int value = 10;
// value는 effectively final이어야 함
Supplier<Integer> s = () -> value;
// value = 20;  // 이러면 컴파일 에러!
```

**4. void 호환**
```java
// add는 boolean 반환하지만 Consumer는 void
Consumer<String> c = s -> list.add(s);  // OK!
```

---

## 🎯 실전 패턴

### 패턴 1: 전략 패턴

```java
// 전략 인터페이스
interface ValidationStrategy {
    boolean execute(String s);
}

// 람다로 전략 전달
Validator numericValidator = new Validator(
    (String s) -> s.matches("\\d+")
);

Validator lowerCaseValidator = new Validator(
    (String s) -> s.matches("[a-z]+")
);
```

---

### 패턴 2: 템플릿 메서드 패턴

```java
// 추상 클래스 대신 람다
void processCustomer(int id, Consumer<Customer> makeCustomerHappy) {
    Customer c = Database.getCustomerWithId(id);
    makeCustomerHappy.accept(c);
}

// 사용
processCustomer(1337, (Customer c) -> 
    System.out.println("Hello " + c.getName()));
```

---

### 패턴 3: 옵저버 패턴

```java
interface Observer {
    void notify(String tweet);
}

// 람다로 옵저버 등록
feed.registerObserver((String tweet) -> {
    if (tweet != null && tweet.contains("money")) {
        System.out.println("Breaking news: " + tweet);
    }
});
```

---

## 🔍 디버깅 팁

### 스택 트레이스 읽기

```java
// 람다에서 예외 발생 시
list.stream()
    .map(s -> s.toUpperCase())
    .forEach(System.out::println);

// 스택 트레이스:
// at Lambda$1.apply(Unknown Source)
// at java.util.stream.ReferencePipeline...
```

**해결:** 
- 람다를 메서드로 추출
- 디버거의 람다 브레이크포인트 사용

---

## 📁 코드 예시

이 챕터의 모든 예제 코드는 `code/` 디렉토리에서 확인할 수 있습니다:

- `ExecuteAroundPattern.java` - 실행 어라운드 패턴
- `FunctionalInterfaceExamples.java` - 함수형 인터페이스 사용법
- `MethodReferenceExamples.java` - 메서드 참조
- `LambdaCompositionExamples.java` - 람다 조합
- `TypeInferenceExamples.java` - 형식 추론과 검사

---

## 📖 더 깊이 학습하기

`advanced/` 디렉토리에서 심화 학습 자료를 확인하세요:

- `cheatsheet.md` - 빠른 참조 가이드
- `deep-dive.md` - 내부 동작 원리
- `qa-sessions.md` - 자주 묻는 질문

---

## ✅ 체크리스트

이 챕터를 완료한 후 확인하세요:

- [ ] 람다 표현식을 작성할 수 있다
- [ ] 함수형 인터페이스를 이해하고 사용할 수 있다
- [ ] Predicate, Consumer, Function을 사용할 수 있다
- [ ] 메서드 참조를 사용할 수 있다
- [ ] 람다의 형식 검사 과정을 이해한다
- [ ] 지역 변수 제약의 이유를 안다
- [ ] Comparator, Predicate, Function 조합을 사용할 수 있다

---

## 🎓 다음 단계

**Chapter 04: 스트림 소개**에서는 람다를 활용하여 컬렉션을 선언형으로 처리하는 방법을 배웁니다!
