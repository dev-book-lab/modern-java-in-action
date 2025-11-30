# Chapter 03. 람다 표현식

<div align="center">

**"메서드를 값처럼 전달하는 자바 8의 혁신"**

> *익명 함수를 단순하게 표현하여 동작 파라미터화를 더 쉽게*

[📖 Deep Dive](advanced/deep-dive.md) | [💻 Code](code/) | [📋 CheatSheet](advanced/cheatsheet.md) | [💬 Q&A](advanced/qa-sessions.md)

</div>

---

## 🎯 학습 목표

이 챕터를 마치면 다음을 할 수 있습니다:

- [ ] **람다 표현식**의 개념과 문법을 이해한다
- [ ] **함수형 인터페이스**를 사용하여 람다를 활용한다
- [ ] **실행 어라운드 패턴**으로 코드를 개선한다
- [ ] **Predicate, Consumer, Function**을 실전에서 사용한다
- [ ] **형식 검사, 형식 추론, 제약**을 이해한다
- [ ] **메서드 참조** 4가지 유형을 구분하고 사용한다
- [ ] **람다 조합**으로 복잡한 동작을 만든다

---

## 📚 핵심 개념

### 람다란?

**람다 표현식(Lambda Expression)** 은 메서드로 전달할 수 있는 익명 함수를 단순화한 것입니다.

```java
// Before: 익명 클래스 (5줄)
Comparator<Apple> byWeight = new Comparator<Apple>() {
    public int compare(Apple a1, Apple a2) {
        return a1.getWeight().compareTo(a2.getWeight());
    }
};

// After: 람다 표현식 (1줄!)
Comparator<Apple> byWeight = 
    (Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight());
```

### 람다의 4가지 특징

1. **익명 (Anonymous)**: 보통의 메서드와 달리 이름이 없다
2. **함수 (Function)**: 특정 클래스에 종속되지 않는다
3. **전달 (Passed)**: 메서드 인수로 전달하거나 변수로 저장할 수 있다
4. **간결 (Concise)**: 익명 클래스처럼 자질구레한 코드가 없다

### 왜 필요한가?

Chapter 02에서 배운 **동작 파라미터화**를 훨씬 간결하게 만들어줍니다:

```java
// Chapter 02: 익명 클래스 사용
filterApples(inventory, new ApplePredicate() {
    public boolean test(Apple apple) {
        return apple.getColor() == Color.GREEN;
    }
});

// Chapter 03: 람다 사용 - 훨씬 간결!
filterApples(inventory, apple -> apple.getColor() == Color.GREEN);
```

---

## 🔤 람다 문법

### 기본 형태

```java
(파라미터) -> 표현식
(파라미터) -> { 문장들; }
```

### 람다 구조

```
(Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight())
 ^^^^^^^^^^^^^^^^      ^^  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 파라미터 리스트      화살표          람다 바디
```

**구성 요소:**
- **파라미터 리스트**: Comparator의 compare 메서드 파라미터
- **화살표 (->)**: 파라미터와 바디를 구분
- **람다 바디**: 람다의 반환값

### 다양한 형태

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

// 5. 파라미터 타입 생략 (형식 추론)
(a1, a2) -> a1.getWeight().compareTo(a2.getWeight())
```

---

## 🚀 주요 개념

### 3.2 함수형 인터페이스

**정의:** 정확히 **하나의 추상 메서드**를 지정하는 인터페이스

```java
@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);  // 추상 메서드 (딱 하나!)
}
```

**함수 디스크립터 (Function Descriptor):**
- 함수형 인터페이스의 추상 메서드 시그니처
- 람다 표현식의 시그니처를 서술

---

### 3.3 실행 어라운드 패턴 (Execute Around Pattern)

**패턴 구조:**
```
초기화/준비 코드
    ↓
실제 작업 (람다로 전달) ← 변경 가능!
    ↓
정리/마무리 코드
```

**4단계 진화:**

```java
// 1단계: 동작 파라미터화 필요성 파악
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
String oneLine = processFile(br -> br.readLine());
String twoLines = processFile(br -> br.readLine() + br.readLine());
```

[→ ExecuteAroundPattern.java 전체 코드 보기](code/ExecuteAroundPattern.java)

---

### 3.4 java.util.function 패키지

**핵심 함수형 인터페이스 6개:**

| 인터페이스 | 함수 디스크립터 | 사용 예시 |
|-----------|----------------|----------|
| `Predicate<T>` | `T → boolean` | `a -> a.getWeight() > 150` |
| `Consumer<T>` | `T → void` | `a -> System.out.println(a)` |
| `Function<T,R>` | `T → R` | `a -> a.getWeight()` |
| `Supplier<T>` | `() → T` | `() -> new Apple()` |
| `UnaryOperator<T>` | `T → T` | `s -> s.toUpperCase()` |
| `BinaryOperator<T>` | `(T,T) → T` | `(a,b) -> a + b` |

#### Predicate<T> - 조건 검사

```java
Predicate<String> nonEmpty = s -> !s.isEmpty();
List<String> result = filter(strings, nonEmpty);
```

#### Consumer<T> - 값 소비

```java
forEach(Arrays.asList(1,2,3,4,5), 
    i -> System.out.println(i));
```

#### Function<T,R> - 값 변환

```java
List<Integer> lengths = map(
    Arrays.asList("Modern", "Java", "In", "Action"),
    s -> s.length()
);
```

[→ FunctionalInterfaceExamples.java 전체 코드 보기](code/FunctionalInterfaceExamples.java)

---

### 3.4.4 기본형 특화 (Primitive Specialization)

**문제:** 제네릭은 참조형만 사용 → 오토박싱 비용 발생

```java
// ❌ 박싱 발생 (느림)
Predicate<Integer> evenNumbers = i -> i % 2 == 0;

// ✅ 박싱 없음 (빠름, 약 7배!)
IntPredicate evenNumbers = i -> i % 2 == 0;
```

**성능 차이:**
- `Function<Double, Double>`: 박싱 4번 → 850ms
- `DoubleFunction<Double>`: 박싱 1번 → 380ms
- `DoubleUnaryOperator`: 박싱 0번 → 120ms

**주요 기본형 특화 인터페이스:**

| 인터페이스 | 함수 디스크립터 | 예시 |
|-----------|----------------|------|
| `IntPredicate` | `int → boolean` | `i -> i > 0` |
| `LongConsumer` | `long → void` | `l -> System.out.println(l)` |
| `DoubleFunction<R>` | `double → R` | `d -> Double.toString(d)` |
| `IntUnaryOperator` | `int → int` | `i -> i * i` |
| `DoubleBinaryOperator` | `(double,double) → double` | `(d1,d2) -> d1 + d2` |

[→ deep-dive.md에서 박싱 성능 분석 보기](advanced/deep-dive.md#8-박싱과-기본형-특화)

---

### 3.5 형식 검사, 형식 추론, 제약

#### 3.5.1 형식 검사 (Type Checking)

**람다의 형식 = 대상 형식 (Target Type)**

```java
List<Apple> heavyApples = 
    filter(inventory, (Apple a) -> a.getWeight() > 150);
```

**형식 검사 과정:**
```
1. filter 메서드의 시그니처 확인
   → filter(List<Apple>, Predicate<Apple>)
2. Predicate<Apple>의 추상 메서드 확인
   → boolean test(Apple)
3. 람다 시그니처 확인
   → (Apple) -> boolean
4. 일치! ✅
```

---

#### 3.5.2 형식 추론 (Type Inference)

**컴파일러가 대상 형식으로 파라미터 타입 추론**

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
portNumber = 31337;  // 재할당 불가!
```

**이유:**
- 지역 변수는 Stack에 저장
- 람다는 변수의 복사본을 캡처
- 원본이 변경되면 복사본과 불일치
- final만 허용하여 일관성 보장

[→ deep-dive.md에서 메모리 분석 보기](advanced/deep-dive.md#3-람다의-지역변수-참조-제약)

---

### 3.6 메서드 참조 (Method Reference)

**람다를 더 간결하게!**

```java
// 람다
inventory.sort((a1, a2) -> a1.getWeight().compareTo(a2.getWeight()));

// 메서드 참조
inventory.sort(comparing(Apple::getWeight));
```

#### 메서드 참조 4가지 유형

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

(list, element) -> list.contains(element)
    ↓
List::contains  // 첫 번째 파라미터가 대상!
```

**3. 인스턴스 메서드 참조 (기존 객체)**
```java
() -> expensiveTransaction.getValue()
    ↓
expensiveTransaction::getValue
```

**4. 생성자 참조**
```java
() -> new Apple()                          → Apple::new
(weight) -> new Apple(weight)              → Apple::new
(color, weight) -> new Apple(color, weight) → Apple::new
```

[→ MethodReferenceExamples.java 전체 코드 보기](code/MethodReferenceExamples.java)

---

### 3.7 람다 활용: 코드의 진화

**6단계 진화 과정:**

```java
// 1단계: Comparator 구현 클래스
inventory.sort(new AppleComparator());

// 2단계: 익명 클래스
inventory.sort(new Comparator<Apple>() {
    public int compare(Apple a1, Apple a2) {
        return a1.getWeight().compareTo(a2.getWeight());
    }
});

// 3단계: 람다 (타입 명시)
inventory.sort((Apple a1, Apple a2) -> 
    a1.getWeight().compareTo(a2.getWeight()));

// 4단계: 람다 (타입 추론)
inventory.sort((a1, a2) -> 
    a1.getWeight().compareTo(a2.getWeight()));

// 5단계: Comparator.comparing 사용
inventory.sort(comparing(a -> a.getWeight()));

// 6단계: 메서드 참조 (최종!)
inventory.sort(comparing(Apple::getWeight));
```

[→ SortingEvolution.java 전체 코드 보기](code/SortingEvolution.java)

---

### 3.8 람다 표현식 조합

#### 3.8.1 Comparator 조합

```java
// 역정렬
inventory.sort(comparing(Apple::getWeight).reversed());

// Comparator 연결
inventory.sort(comparing(Apple::getWeight)
    .reversed()
    .thenComparing(Apple::getColor));
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

// OR - 왼쪽부터 오른쪽으로 결합
Predicate<Apple> redAndHeavyOrGreen = 
    redApple
        .and(a -> a.getWeight() > 150)
        .or(a -> Color.GREEN.equals(a.getColor()));
// → (빨강 AND 무거움) OR 초록
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

**시각화:**
```
andThen: 입력 → [f] → [g] → 출력
compose: 입력 → [g] → [f] → 출력
```

[→ LambdaComposition.java 전체 코드 보기](code/LambdaComposition.java)  
[→ deep-dive.md에서 andThen vs compose 상세 비교](advanced/deep-dive.md#7-function의-andthen-vs-compose)

---

## 📊 함수형 인터페이스 치트시트

### java.util.function 전체 목록

| 인터페이스 | 함수 디스크립터 | 추상 메서드 | 사용 예시 |
|-----------|----------------|-----------|----------|
| `Predicate<T>` | `T → boolean` | `boolean test(T t)` | 필터링 |
| `Consumer<T>` | `T → void` | `void accept(T t)` | 출력, 저장 |
| `Function<T,R>` | `T → R` | `R apply(T t)` | 변환, 매핑 |
| `Supplier<T>` | `() → T` | `T get()` | 팩토리 |
| `UnaryOperator<T>` | `T → T` | `T apply(T t)` | 단항 연산 |
| `BinaryOperator<T>` | `(T,T) → T` | `T apply(T t1, T t2)` | 이항 연산 |
| `BiPredicate<T,U>` | `(T,U) → boolean` | `boolean test(T t, U u)` | 두 값 비교 |
| `BiConsumer<T,U>` | `(T,U) → void` | `void accept(T t, U u)` | 두 값 처리 |
| `BiFunction<T,U,R>` | `(T,U) → R` | `R apply(T t, U u)` | 두 값 변환 |

[→ cheatsheet.md에서 전체 목록 보기](advanced/cheatsheet.md)

---

## 💡 핵심 정리

### 람다 사용 시 주의사항

**1. 함수형 인터페이스에서만 사용**
```java
// ✅ OK: Runnable은 함수형 인터페이스
Runnable r = () -> System.out.println("Hello");

// ❌ 에러: List는 함수형 인터페이스 아님
List<String> list = () -> new ArrayList<>();
```

**2. 예외 처리**
```java
Function<String, Integer> parse = s -> {
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
Supplier<Integer> s = () -> value;  // OK
// value = 20;  // 이러면 컴파일 에러!
```

**4. void 호환**
```java
// add는 boolean 반환하지만 Consumer는 void
Consumer<String> c = s -> list.add(s);  // OK!
```

[→ qa-sessions.md에서 자주 묻는 질문 보기](advanced/qa-sessions.md)

---

## 🎯 실전 패턴

### Pattern 1: 전략 패턴

```java
interface ValidationStrategy {
    boolean execute(String s);
}

Validator numericValidator = new Validator(s -> s.matches("\\d+"));
Validator lowerCaseValidator = new Validator(s -> s.matches("[a-z]+"));
```

### Pattern 2: 템플릿 메서드 패턴

```java
void processCustomer(int id, Consumer<Customer> makeCustomerHappy) {
    Customer c = Database.getCustomerWithId(id);
    makeCustomerHappy.accept(c);
}

processCustomer(1337, c -> System.out.println("Hello " + c.getName()));
```

### Pattern 3: 옵저버 패턴

```java
feed.registerObserver(tweet -> {
    if (tweet != null && tweet.contains("money")) {
        System.out.println("Breaking news: " + tweet);
    }
});
```

---

## 📂 학습 자료 구조

```
chapter03/
├── README.md                        # 👈 현재 문서
├── code/                            # 실습 코드
│   ├── Apple.java                   # 도메인 클래스
│   ├── Color.java                   # Enum
│   ├── ExecuteAroundPattern.java    # 실행 어라운드 패턴
│   ├── FunctionalInterfaceExamples.java # 함수형 인터페이스
│   ├── LambdaBasics.java            # 람다 기초
│   ├── LambdaComposition.java       # 람다 조합
│   ├── MethodReferenceExamples.java # 메서드 참조
│   └── SortingEvolution.java        # 정렬의 진화
└── advanced/                        # 심화 학습
    ├── deep-dive.md                 # 상세 원리 (8개 심화 주제)
    ├── cheatsheet.md                # 빠른 참조 가이드
    └── qa-sessions.md               # Q&A 세션 (20+ 질문)
```

---

## 🔍 디버깅 팁

### 스택 트레이스 읽기

```java
list.stream()
    .map(s -> s.toUpperCase())
    .forEach(System.out::println);

// 스택 트레이스:
// at Lambda$1.apply(Unknown Source)
// at java.util.stream.ReferencePipeline...
```

**해결책:**
- 람다를 메서드로 추출
- 디버거의 람다 브레이크포인트 사용
- `peek()`으로 중간값 확인

---

## ⚡ Quick Reference

### 가장 자주 사용하는 10가지 패턴

```java
// 1. 필터링
filter(list, x -> condition)

// 2. Predicate 조합
filter(list, pred1.and(pred2).or(pred3))

// 3. 각 요소 처리
forEach(list, x -> System.out.println(x))

// 4. 값 변환
map(list, x -> x.getValue())

// 5. 정렬
list.sort(comparing(X::getValue))

// 6. 역순 정렬
list.sort(comparing(X::getValue).reversed())

// 7. 다중 정렬
list.sort(comparing(X::getFirst).thenComparing(X::getSecond))

// 8. 생성자 참조
map(list, Apple::new)

// 9. Function 조합
f.andThen(g).apply(x)

// 10. 메서드 참조 4가지
Integer::parseInt           // 정적 메서드
String::toUpperCase         // 임의 객체
obj::getValue               // 기존 객체
Apple::new                  // 생성자
```

---

## 🎯 학습 체크리스트

- [ ] 람다 표현식을 **자신의 언어로** 설명할 수 있다
- [ ] 함수형 인터페이스의 **조건**을 말할 수 있다
- [ ] **Predicate, Consumer, Function**의 차이를 구분할 수 있다
- [ ] 메서드 참조 **4가지 유형**을 사용할 수 있다
- [ ] 형식 검사 과정을 **단계별로** 설명할 수 있다
- [ ] 지역 변수 제약의 **이유**를 메모리 관점에서 설명할 수 있다
- [ ] **Comparator, Predicate, Function** 조합을 사용할 수 있다
- [ ] 기본형 특화의 **성능 이점**을 설명할 수 있다

---

## 📖 더 알아보기

- [Deep Dive](advanced/deep-dive.md) - 8개 심화 주제 (예외 처리, Optional, reduce, 박싱 등)
- [CheatSheet](advanced/cheatsheet.md) - 빠른 참조 가이드 (함수형 인터페이스 전체 목록)
- [Q&A Sessions](advanced/qa-sessions.md) - 20+ 질문 답변

---

## 🚀 다음 단계

이제 **Chapter 4: 스트림 소개**로 넘어갈 준비가 되었습니다!

Chapter 4에서는:
- **스트림 API**의 개념과 특징
- **내부 반복 vs 외부 반복**
- **중간 연산과 최종 연산**
- **스트림과 컬렉션**의 차이
- **람다를 활용한 선언형 데이터 처리**

를 학습합니다.

---

<div align="center">

**💡 Key Takeaway**

> *"람다 표현식은 동작 파라미터화를 간결하게 만든다.*  
> *메서드 참조로 더욱 간결하게, 조합으로 더욱 강력하게!"*

**🌟 람다를 마스터하면, 함수형 프로그래밍의 진정한 힘을 경험할 수 있습니다!**

</div>
