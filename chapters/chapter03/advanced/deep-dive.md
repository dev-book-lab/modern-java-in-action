# Lambda Expressions Deep Dive

> 람다 표현식의 내부 동작 원리와 심화 주제

---

## 목차

1. [함수형 인터페이스와 Checked Exception](#1-함수형-인터페이스와-checked-exception)
2. [Optional과 람다](#2-optional과-람다)
3. [람다의 지역변수 참조 제약](#3-람다의-지역변수-참조-제약)
4. [void 호환 람다 안티패턴](#4-void-호환-람다-안티패턴)
5. [reduce의 3가지 형태](#5-reduce의-3가지-형태)
6. [람다를 메서드 참조로 변환](#6-람다를-메서드-참조로-변환)
7. [Function의 andThen vs compose](#7-function의-andthen-vs-compose)
8. [박싱과 기본형 특화](#8-박싱과-기본형-특화)

---

## 1. 함수형 인터페이스와 Checked Exception

### 문제 상황

**핵심 질문:** 왜 함수형 인터페이스는 checked exception을 던지는 동작을 허용하지 않는가?

```java
// ❌ 컴파일 에러!
Function<String, String> readFile = 
    filename -> Files.readString(Path.of(filename));
    // IOException은 checked exception!
```

---

### 근본 원인

#### Function<T,R>의 시그니처

```java
@FunctionalInterface
public interface Function<T, R> {
    R apply(T t);  // throws 없음!
}
```

#### 자바 오버라이드 규칙

```
자식 메서드는 부모보다 많은 checked exception을 던질 수 없음

부모: R apply(T t)  // 0개 예외
자식: R apply(T t) throws IOException  // 1개 예외

0개 < 1개 → 규칙 위반! 💥
```

---

### 왜 이런 설계인가?

#### 1. 타입 안전성

```java
// Function<T,R>의 apply()에 throws 없음
Function<String, String> f = ...;

// 호출자는 예외 처리 불필요
String result = f.apply("input");  // try-catch 불필요!

// 만약 IOException을 던진다면?
// 호출자가 예상 못한 예외 발생 → 타입 안전성 파괴
```

#### 2. 범용성

```java
// Function이 IOException을 선언하면
@FunctionalInterface
public interface Function<T, R> {
    R apply(T t) throws IOException;  // 모든 Function이 선언
}

// 문제: 예외가 필요 없는 경우도 강제됨
Function<Integer, Integer> square = x -> x * x;
// 이 람다는 예외를 안 던지는데 IOException 선언이 강제됨
```

#### 3. 함수형 철학

```
함수형 프로그래밍 원칙:
    - 순수 함수 (Pure Function)
    - 참조 투명성 (Referential Transparency)
    - 부작용 없음 (No Side Effects)

checked exception:
    - 제어 흐름 변경
    - 부작용 발생
    → 함수형 철학과 충돌
```

---

### 해결 방법

#### 방법 1: try-catch로 감싸기 (가장 흔함)

```java
Function<String, String> readFile = filename -> {
    try {
        return Files.readString(Path.of(filename));
    } catch (IOException e) {
        throw new RuntimeException(e);  // unchecked로 변환
    }
};
```

#### 방법 2: 커스텀 함수형 인터페이스

```java
@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {
    R apply(T t) throws E;
}

// 사용
ThrowingFunction<String, String, IOException> readFile = 
    filename -> Files.readString(Path.of(filename));
```

#### 방법 3: 유틸리티 메서드 래핑

```java
public class FunctionUtils {
    public static <T, R> Function<T, R> wrap(
            ThrowingFunction<T, R, Exception> f) {
        return t -> {
            try {
                return f.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}

// 사용
Function<String, String> readFile = 
    wrap(filename -> Files.readString(Path.of(filename)));
```

---

### 실무 예시

```java
// DB 조회 시 SQLException 처리
List<User> users = ids.stream()
    .map(id -> {
        try {
            return findUser(id);  // throws SQLException
        } catch (SQLException e) {
            throw new RuntimeException("DB 조회 실패: " + id, e);
        }
    })
    .collect(Collectors.toList());
```

---

## 2. Optional과 람다

### Optional 기초

**정의:** 값이 있을 수도, 없을 수도 있는 컨테이너

```java
Optional<String> name = Optional.of("Alice");
Optional<String> empty = Optional.empty();
```

---

### 생성 방법

```java
// 1. of: 값이 반드시 있을 때
Optional<String> opt1 = Optional.of("Hello");
Optional<String> opt2 = Optional.of(null);  // NPE!

// 2. ofNullable: null 가능성 있을 때
Optional<String> opt3 = Optional.ofNullable("Hello");
Optional<String> opt4 = Optional.ofNullable(null);  // empty

// 3. empty: 명시적으로 빈 Optional
Optional<String> opt5 = Optional.empty();
```

---

### 값 꺼내기

```java
// get(): 직접 꺼내기 (위험!)
Optional<String> opt = Optional.of("Hello");
String value = opt.get();  // "Hello"

Optional<String> empty = Optional.empty();
String value2 = empty.get();  // NoSuchElementException!

// orElse: 기본값 제공
String value3 = empty.orElse("Default");  // "Default"

// orElseGet: 기본값을 지연 계산
String value4 = empty.orElseGet(() -> "Computed Default");

// orElseThrow: 예외 던지기
String value5 = empty.orElseThrow(() -> 
    new IllegalStateException("값 없음"));
```

---

### orElse vs orElseGet

**핵심 차이: orElse는 항상 실행됨!**

```java
Optional<String> opt = Optional.of("Hello");

// orElse: 값이 있어도 createDefault() 실행!
String result1 = opt.orElse(createDefault());
// 1. createDefault() 실행 (비용 발생!)
// 2. "Hello" 반환 (createDefault() 결과는 버려짐)

// orElseGet: 값이 있으면 createDefault() 실행 안 함!
String result2 = opt.orElseGet(() -> createDefault());
// 1. opt에 값 있음 확인
// 2. "Hello" 반환 (createDefault() 실행 안 함!)
```

**이유: 자바 메서드 호출 시 인수는 메서드 실행 전에 평가됨**

```java
// orElse(T other)
opt.orElse(createDefault())
//         ^^^^^^^^^^^^^^^^
//         메서드 호출 전에 평가!

// orElseGet(Supplier)
opt.orElseGet(() -> createDefault())
//            ^^^^^^^^^^^^^^^^^^^^^
//            함수 객체만 생성, 실행 안 함
```

---

### 값 확인

```java
// isPresent: 값 있는지
if (opt.isPresent()) {
    System.out.println(opt.get());
}

// isEmpty: 값 없는지 (Java 11+)
if (opt.isEmpty()) {
    System.out.println("값 없음");
}

// ifPresent: 값 있으면 실행
opt.ifPresent(value -> System.out.println(value));

// ifPresentOrElse: 값 있으면/없으면 각각 실행 (Java 9+)
opt.ifPresentOrElse(
    value -> System.out.println(value),  // Consumer
    () -> System.out.println("값 없음")   // Runnable
);
```

---

### ifPresentOrElse의 특별한 점

**질문:** 람다 2개면 Consumer 2개인가?

**정답:** Consumer + Runnable!

```java
public void ifPresentOrElse(
    Consumer<? super T> action,  // 값 받음
    Runnable emptyAction         // 아무것도 안 받음
)

// 왜 다른가?
값이 있을 때: value를 사용해야 함 → Consumer<T>
값이 없을 때: 사용할 값이 없음 → Runnable
```

---

### 값 변환

#### map: 값 변환

```java
Optional<String> name = Optional.of("alice");

Optional<String> upper = name.map(String::toUpperCase);
// Optional["ALICE"]

Optional<Integer> length = name.map(String::length);
// Optional[5]
```

#### flatMap: Optional 반환 함수 처리

```java
// map 사용: 이중 중첩!
Optional<Optional<String>> nested = 
    name.map(s -> Optional.of(s.toUpperCase()));
// Optional[Optional["ALICE"]] 💥

// flatMap 사용: 한 겹!
Optional<String> upper = 
    name.flatMap(s -> Optional.of(s.toUpperCase()));
// Optional["ALICE"] ✅
```

**왜 이런 차이가?**

```java
// map의 동작
public <U> Optional<U> map(Function<T, U> mapper) {
    if (value != null) {
        return Optional.ofNullable(mapper.apply(value));
        //     ^^^^^^^^^^^^^^^^^^ map이 감쌈!
    }
    return Optional.empty();
}

// flatMap의 동작
public <U> Optional<U> flatMap(Function<T, Optional<U>> mapper) {
    if (value != null) {
        return mapper.apply(value);  // 그대로 반환! 감싸지 않음!
    }
    return Optional.empty();
}
```

---

### SQLException 처리 예시

```java
List<User> users = ids.stream()
    .map(id -> {
        try {
            return Optional.of(findUser(id));  // 성공
        } catch (SQLException e) {
            return Optional.empty();  // 실패
        }
    })
    .filter(Optional::isPresent)  // 값 있는 것만
    .map(Optional::get)            // Optional → User
    .collect(Collectors.toList());
```

**단계별 흐름:**

```
입력: [1L, 2L, 3L]
    ↓ map (try-catch + Optional)
[Optional[User1], Optional.empty(), Optional[User3]]
    ↓ filter (isPresent)
[Optional[User1], Optional[User3]]
    ↓ map (get)
[User1, User3]
    ↓ collect
최종: List<User> = [User1, User3]
```

---

## 3. 람다의 지역변수 참조 제약

### 규칙

**람다는 외부 지역변수를 참조할 수 있지만, final 또는 effectively final이어야 함**

```java
// ✅ OK
int value = 10;
Runnable r = () -> System.out.println(value);

// ❌ 에러
int value = 10;
Runnable r = () -> System.out.println(value);
value = 20;  // 재할당! effectively final 위반
```

---

### 왜 이런 제약이 있는가?

#### 1. 변수 캡처 (Variable Capture)

```java
public Runnable createRunnable() {
    int value = 10;  // Stack에 저장
    return () -> System.out.println(value);  // 람다가 캡처
}

// 실행 과정:
// 1. createRunnable() 호출
//    Stack에 value = 10 저장
//    람다 생성: value 캡처 (복사본 저장)
//    return 람다
//    Stack Frame 제거 (value 사라짐!)
//
// 2. 람다 실행
//    캡처한 value 사용 (복사본)
```

**문제:**
```
람다는 지역변수의 복사본을 저장함
    ↓
원본이 변경되면?
    ↓
복사본과 원본이 달라짐!
    ↓
혼란과 버그!
```

---

#### 2. 멀티스레드 안전성

```java
int count = 0;

new Thread(() -> {
    for (int i = 0; i < 1000; i++) {
        count++;  // 만약 가능하다면?
    }
}).start();

new Thread(() -> {
    for (int i = 0; i < 1000; i++) {
        count++;
    }
}).start();

// 예상: 2000
// 실제: ? (race condition!)
```

**자바의 해결책:**
```
람다에서 변수 변경 금지
    ↓
final/effectively final만 허용
    ↓
값이 변하지 않으므로 스레드 안전
```

---

#### 3. 함수형 프로그래밍 철학

```
순수 함수: 같은 입력 → 같은 출력, 부작용 없음

람다가 외부 변수 변경 → 부작용 발생 → 순수 함수 아님
```

---

### 해결 방법

#### 방법 1: reduce 사용 (권장)

```java
// ❌ 나쁜 예
int[] sum = {0};
numbers.forEach(n -> sum[0] += n);

// ✅ 좋은 예
int sum = numbers.stream().reduce(0, Integer::sum);
```

#### 방법 2: 인스턴스 변수 사용

```java
class Counter {
    private int count = 0;  // 인스턴스 변수
    
    public void increment(List<Integer> numbers) {
        numbers.forEach(n -> count += n);  // ✅ OK
    }
}
```

**왜 인스턴스 변수는 되는가?**

```
지역변수:
    Stack에 저장
    메서드 종료 시 사라짐
    람다가 복사본 저장
    → final 필요

인스턴스 변수:
    Heap에 저장
    객체가 살아있는 동안 유지
    람다가 참조만 저장
    → final 불필요
```

#### 방법 3: AtomicInteger (멀티스레드)

```java
AtomicInteger count = new AtomicInteger(0);
numbers.forEach(n -> count.addAndGet(n));  // ✅ OK
```

**왜 되는가?**
```
count 변수 자체: 재할당 안 됨 (effectively final)
count.addAndGet(): 메서드 호출일 뿐
```

#### 방법 4: 배열 우회 (비추천!)

```java
int[] count = {0};
numbers.forEach(n -> count[0] += n);  // ✅ 컴파일은 됨
```

**왜 되는가?**
```
count 변수: 배열 주소 (변경 안 됨)
count[0]: 배열 내용 (변경됨)
→ count는 effectively final ✅
```

**왜 비추천?**
- 스레드 안전하지 않음
- 의도 불명확
- 함수형 철학 위배

---

## 4. void 호환 람다 안티패턴

### void 호환 정의

**반환값이 있는 표현식을 void를 기대하는 컨텍스트에서 사용할 때, 자바가 자동으로 반환값을 무시함**

```java
// forEach는 Consumer 기대 (void)
list.forEach(s -> list.add(s.toUpperCase()));
//                ^^^^^^^^^^^^^^^^^^^^^^^^^^^
//                add는 boolean 반환!
//                → 자동으로 무시됨 (void 호환)
```

---

### 작동 원리

```java
list.add(s.toUpperCase())  // boolean 반환
    ↓
Consumer는 void 반환 기대
    ↓
컴파일러가 boolean을 버림! (void 호환)
```

---

### 왜 문제인가?

#### 1. 의도 불명확

```java
list.forEach(s -> list.add(s));
// 읽는 사람: "add의 반환값(boolean)을 쓰는 건가? 버리는 건가?"
```

#### 2. 정보 손실

```java
Set<String> set = new HashSet<>();
set.forEach(s -> set.add(s.toUpperCase()));
// add가 false 반환 (중복) → 무시됨! 중복 감지 못 함!
```

#### 3. 디버깅 어려움

```java
List<String> errors = new ArrayList<>();
users.forEach(user -> errors.add(validate(user)));
// validate가 null 반환 시 add(null)은 true 반환 → 무시됨
// errors = ["error1", null, "error2", null]
// 나중에 e.toUpperCase() 호출 시 NPE!
```

#### 4. 부작용 숨김

```java
original.forEach(s -> result.add(s.toUpperCase()));
// 문제: forEach는 부작용 가짐, result 변경이 명확하지 않음
```

---

### 회피 방법

#### 방법 1: 표현식 → 블록

```java
list.forEach(s -> {
    collection.add(s);  // 세미콜론으로 문장임을 명시
});
```

#### 방법 2: 함수형 API 사용 (권장)

```java
// ❌ forEach + add
List<String> result = new ArrayList<>();
list.forEach(s -> result.add(s.toUpperCase()));

// ✅ map + collect
List<String> result = list.stream()
    .map(String::toUpperCase)
    .collect(Collectors.toList());
```

#### 방법 3: 반환값 확인

```java
set.forEach(s -> {
    boolean added = set.add(s.toUpperCase());
    if (!added) log.warn("Duplicate: " + s);
});
```

---

### 대표적인 void 호환 상황

```java
// 1. forEach + add
list.forEach(s -> collection.add(s));

// 2. forEach + put (Map)
map.forEach((k, v) -> newMap.put(k, v));

// 3. forEach + remove
list.forEach(s -> set.remove(s));

// 4. Predicate 위치에 반환값 있는 람다
list.removeIf(s -> list.add(s));  // add는 boolean 반환
```

---

### Map.put() 상세 해설

**질문:** put이 이전값을 반환한다고?

```java
Map<String, Integer> map = new HashMap<>();

Integer old1 = map.put("A", 100);  // null (처음)
Integer old2 = map.put("A", 200);  // 100 (이전 값!)
Integer old3 = map.put("A", 300);  // 200 (이전 값!)
```

**동작:**
```
초기: {}
put("A", 100) → null 반환, map = {A=100}
put("A", 200) → 100 반환, map = {A=200}
put("A", 300) → 200 반환, map = {A=300}
                ^^^
                이전 값!
```

**왜 이전값을 반환하는가?**
- 중복 체크
- 업데이트 확인

---

### this::validate의 this

**질문:** this는 언제 사용하는가?

**정답:** 인스턴스 메서드 참조할 때!

```java
public class UserService {
    private UserRepository repository;  // 인스턴스 변수
    
    // 인스턴스 메서드 (repository 사용)
    public String validate(User user) {
        // repository 사용
        return null;
    }
    
    public void process(List<User> users) {
        // this::validate 사용
        users.stream()
            .map(this::validate)  // 현재 객체의 validate
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
```

**this가 필요한 이유:**
```
validate() 메서드:
    - 인스턴스 변수 사용 (repository)
    - 특정 객체의 메서드여야 함
    
따라서:
    this::validate (현재 객체의 validate)
```

---

### Function.identity()

**정의:** 입력을 그대로 반환하는 함수

```java
// Function.identity()는 다음과 같음:
Function<String, String> identity = t -> t;

// 사용:
String result = identity.apply("Hello");  // "Hello"
```

**toMap에서 사용:**

```java
List<String> list = Arrays.asList("a", "b", "c");

Map<String, Integer> map = list.stream()
    .collect(Collectors.toMap(
        Function.identity(),  // 키: 원소 그대로
        String::length        // 값: 길이
    ));
// {a=1, b=1, c=1}
```

**실행 과정:**

```
["a", "b", "c"]

Step 1: "a"
    키: Function.identity().apply("a") = "a"
    값: "a".length() = 1
    → put("a", 1)

Step 2: "b"
    키: Function.identity().apply("b") = "b"
    값: "b".length() = 1
    → put("b", 1)

결과: {a=1, b=1, c=1}
```

---

## 5. reduce의 3가지 형태

### 형태 1: identity + accumulator

```java
T reduce(T identity, BinaryOperator<T> accumulator)

// 예시
int sum = stream.reduce(0, (a, b) -> a + b);
```

---

### 형태 2: accumulator만

```java
Optional<T> reduce(BinaryOperator<T> accumulator)

// 예시
Optional<Integer> max = stream.reduce((a, b) -> a > b ? a : b);
```

---

### 형태 3: identity + accumulator + combiner

```java
<U> U reduce(
    U identity,
    BiFunction<U, T, U> accumulator,
    BinaryOperator<U> combiner
)

// 예시
int totalLength = words.parallelStream()
    .reduce(
        0,                              // identity
        (acc, word) -> acc + word.length(),  // accumulator
        (a, b) -> a + b                 // combiner
    );
```

---

### 왜 3개 파라미터가 필요한가?

**타입 변환 때문!**

```java
Stream<String> → int

// 2개 파라미터로는 불가능:
reduce(0, (sum, word) -> sum + word.length())
//         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
//         sum은 int, word는 String → 타입 오류!

// 3개 파라미터로 가능:
reduce(
    0,                              // int (결과 타입)
    (acc, word) -> acc + word.length(),  // (int, String) → int
    (a, b) -> a + b                 // (int, int) → int
)
```

---

### 각 파라미터의 역할

#### identity (초기값)

```java
0  // identity

역할: 각 스레드의 시작값
타입: U (결과 타입)
```

#### accumulator (누적 함수)

```java
(acc, word) -> acc + word.length()
 ^^^  ^^^^     ^^^^^^^^^^^^^^^^^^^
 int  String   int 반환

역할: 각 요소를 처리하여 누적
타입: (U, T) → U

acc = 지금까지 누적값 (int)
word = 현재 요소 (String)
반환 = 새 누적값 (int)
```

#### combiner (결합 함수)

```java
(a, b) -> a + b
 ^  ^     ^^^^^
 int int  int 반환

역할: 병렬 처리 시 부분 결과 합치기
타입: (U, U) → U

a = 스레드1의 결과 (int)
b = 스레드2의 결과 (int)
반환 = 전체 결과 (int)
```

---

### 순차 vs 병렬

**순차:**
```
         0 (identity)
         ↓ accumulator(0, "Hello")
         5
         ↓ accumulator(5, "World")
        10
         ↓ accumulator(10, "Java")
        14

combiner 호출 안 됨!
```

**병렬:**
```
전체: ["Hello", "World", "Java", "Stream"]
         ↓ 분할
┌────────────────┬────────────────┐
│ ["Hello",      │ ["Java",       │
│  "World"]      │  "Stream"]     │
└────────────────┴────────────────┘
       ↓                 ↓
   스레드1            스레드2
       ↓                 ↓
  accumulator      accumulator
       ↓                 ↓
   0 → 5 → 10       0 → 4 → 10
       ↓                 ↓
      10                10
       └────────┬────────┘
                ↓
         combiner(10, 10)
                ↓
               20
```

---

## 6. 람다를 메서드 참조로 변환

### 단계별 변환 과정

```java
BiPredicate<List<String>, String> contains = 
    (list, element) -> list.contains(element);
```

**Step 1: 람다 분석**
```
파라미터: list, element
바디: list.contains(element)
      ^^^^ ^^^^^^^^ ^^^^^^^
      대상  메서드    인수
```

**Step 2: 패턴 파악**
```
(A, B) -> A.method(B)

첫 번째 파라미터가 메서드 호출 대상
두 번째 파라미터가 메서드 인수
```

**Step 3: 타입 확인**
```
list의 타입 = List<String>
    ↓
클래스명 = List
```

**Step 4: 변환!**
```java
List::contains
```

---

### 변환 규칙

#### 패턴 1: 파라미터를 그대로 전달

```java
// 람다
(x) -> someMethod(x)

// 메서드 참조
Class::someMethod
```

#### 패턴 2: 첫 번째 파라미터가 대상

```java
// 람다
(A, B) -> A.method(B)

// 메서드 참조
A타입::method
```

#### 패턴 3: 특정 객체의 메서드

```java
// 람다
(x) -> obj.method(x)

// 메서드 참조
obj::method
```

#### 패턴 4: 정적 메서드

```java
// 람다
(x) -> Class.staticMethod(x)

// 메서드 참조
Class::staticMethod
```

---

### 변환 예시

```java
// 1. String::toUpperCase
(String s) -> s.toUpperCase()
    ↓
String::toUpperCase

// 2. Integer::parseInt
(String s) -> Integer.parseInt(s)
    ↓
Integer::parseInt

// 3. System.out::println
(String s) -> System.out.println(s)
    ↓
System.out::println

// 4. ArrayList::new
() -> new ArrayList<>()
    ↓
ArrayList::new
```

---

## 7. Function의 andThen vs compose

### andThen: 순차 실행

**핵심:** "f 하고 나서(and then) g 해라"

```java
f.andThen(g) = g(f(x))

순서: f 먼저 → g 나중
```

**예시:**

```java
Function<String, Integer> getLength = s -> s.length();
Function<Integer, Integer> doubleIt = n -> n * 2;

Function<String, Integer> lengthThenDouble = 
    getLength.andThen(doubleIt);

lengthThenDouble.apply("Hello");
// 1. getLength("Hello") = 5
// 2. doubleIt(5) = 10
// 결과: 10
```

**시각화:**

```
입력: "Hello"
    ↓
getLength
    ↓
    5
    ↓
doubleIt
    ↓
   10
```

---

### compose: 역순 실행

**핵심:** "g를 먼저 구성(compose)해라"

```java
f.compose(g) = f(g(x))

순서: g 먼저 → f 나중
```

**예시:**

```java
Function<Integer, Integer> doubleIt = n -> n * 2;
Function<String, Integer> getLength = s -> s.length();

Function<String, Integer> lengthThenDouble = 
    doubleIt.compose(getLength);

lengthThenDouble.apply("Hello");
// 1. getLength("Hello") = 5  (먼저!)
// 2. doubleIt(5) = 10
// 결과: 10
```

**시각화:**

```
입력: "Hello"
    ↓
getLength (먼저!)
    ↓
    5
    ↓
doubleIt (나중!)
    ↓
   10
```

---

### 비교

```
andThen:
입력 → [f] → [g] → 출력
       먼저  나중
       
compose:
입력 → [g] → [f] → 출력
       먼저  나중
```

**같은 결과, 다른 표현:**

```java
// andThen
trim.andThen(upper).andThen(length)

// compose (역순!)
length.compose(upper).compose(trim)

// 둘 다 결과 같음!
```

---

## 8. 박싱과 기본형 특화

### 박싱의 비용

**메모리:**
```
기본형 int:
    Stack: [10]  (4 bytes)
    
래퍼 타입 Integer:
    Stack: [주소]  (8 bytes)
    Heap:  [객체]  (16 bytes)
    
총: 24 bytes vs 4 bytes (6배!)
```

**시간:**
```java
// 기본형: 즉시 계산
int sum = a + b;

// 래퍼: 박싱/언박싱 필요
Integer sum = a + b;
// 1. a 언박싱 (객체 → 값)
// 2. b 언박싱 (객체 → 값)
// 3. 계산
// 4. 박싱 (값 → 객체)
```

---

### Function vs DoubleFunction

**Function<Double, Double>:**
```java
Function<Double, Double> square = x -> x * x;

double value = 3.0;
Double result = square.apply(value);

// 내부:
// 1. value 박싱: double → Double
// 2. x 언박싱: Double → double (계산 위해)
// 3. 계산: 3.0 * 3.0 = 9.0
// 4. 결과 박싱: double → Double

총 박싱/언박싱: 4번! 💥
```

**DoubleFunction<Double>:**
```java
DoubleFunction<Double> square = x -> x * x;

double value = 3.0;
Double result = square.apply(value);

// 내부:
// 1. value 그대로 전달 (박싱 없음!)
// 2. 계산: 3.0 * 3.0 = 9.0
// 3. 결과 박싱: double → Double

총 박싱: 1번! ✅
```

**DoubleUnaryOperator:**
```java
DoubleUnaryOperator square = x -> x * x;

double value = 3.0;
double result = square.applyAsDouble(value);

// 내부:
// 1. value 그대로 전달
// 2. 계산: 3.0 * 3.0 = 9.0
// 3. 결과 그대로 반환

총 박싱: 0번! 🎉
```

---

### 성능 차이

```java
// 1,000만 번 반복

Function<Double, Double>:
    약 850ms  💥

DoubleUnaryOperator:
    약 120ms  ✅

차이: 약 7배!
```

---

## 🎯 핵심 정리

### 1. 함수형 인터페이스와 예외

```
Function은 checked exception 못 던짐
    ↓
try-catch로 감싸기
or
커스텀 함수형 인터페이스
```

### 2. Optional

```
orElse: 항상 실행
orElseGet: 필요할 때만 실행

map: 이중 중첩 가능
flatMap: 한 겹으로 펼침
```

### 3. 지역변수 제약

```
지역변수: final/effectively final
인스턴스 변수: 제약 없음

이유: 변수 캡처, 스레드 안전성
```

### 4. void 호환

```
반환값이 있어도 void 컨텍스트 사용 가능
    ↓
정보 손실, 디버깅 어려움
    ↓
함수형 API 사용 권장
```

### 5. reduce

```
2개 파라미터: 같은 타입
3개 파라미터: 타입 변환 가능

identity: 초기값
accumulator: 누적
combiner: 병렬 결과 합치기
```

### 6. 메서드 참조

```
(A, B) -> A.method(B)
    ↓
A타입::method

첫 번째 파라미터가 대상!
```

### 7. andThen vs compose

```
andThen: f → g (g(f(x)))
compose: g → f (f(g(x)))

andThen이 더 직관적
```

### 8. 박싱

```
Function<Double, Double>: 박싱 4번
DoubleFunction<Double>: 박싱 1번
DoubleUnaryOperator: 박싱 0번!

성능: 약 7배 차이
```

---

**이 심화 내용들을 마스터하면 람다를 완벽하게 활용할 수 있습니다!** 🚀
