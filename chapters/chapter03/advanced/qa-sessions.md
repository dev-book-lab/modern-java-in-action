# Q&A Sessions - Lambda Expressions

> 실제 학습 과정에서 나온 질문과 답변 모음

---

## 📋 목차

1. [함수형 인터페이스와 예외](#q1-함수형-인터페이스와-예외)
2. [Optional 관련](#q2-optional-관련)
3. [람다 제약사항](#q3-람다-제약사항)
4. [void 호환 규칙](#q4-void-호환-규칙)
5. [reduce의 동작](#q5-reduce의-동작)
6. [메서드 참조 변환](#q6-메서드-참조-변환)
7. [Function 조합](#q7-function-조합)
8. [박싱과 성능](#q8-박싱과-성능)

---

## Q1. 함수형 인터페이스와 예외

### Q: 왜 Function은 checked exception을 던질 수 없나요?

**A:** Function의 apply() 메서드에 throws가 없기 때문입니다.

```java
@FunctionalInterface
public interface Function<T, R> {
    R apply(T t);  // throws 없음!
}
```

자바의 오버라이드 규칙:
- 자식 메서드는 부모보다 많은 checked exception을 던질 수 없음
- 부모 apply(): 0개 예외
- 자식 apply(): IOException 던지려 함 → 규칙 위반!

**해결책:**

```java
// 1. try-catch로 감싸기
Function<String, String> readFile = filename -> {
    try {
        return Files.readString(Path.of(filename));
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
};

// 2. 커스텀 함수형 인터페이스
@FunctionalInterface
interface ThrowingFunction<T, R> {
    R apply(T t) throws IOException;
}
```

---

## Q2. Optional 관련

### Q2-1: add(null)하면 null도 추가되나요?

**A:** 네, 맞습니다!

```java
List<String> list = new ArrayList<>();
list.add(null);  // ✅ null 추가됨!

System.out.println(list);  // [null]
System.out.println(list.size());  // 1
```

**문제 상황:**

```java
users.forEach(user -> errors.add(validate(user)));
// validate()가 null 반환 → errors에 null 추가됨!

// 나중에:
errors.forEach(error -> {
    System.out.println(error.toUpperCase());  // NPE!
});
```

**해결:**

```java
List<String> errors = users.stream()
    .map(this::validate)
    .filter(Objects::nonNull)  // null 제거!
    .collect(Collectors.toList());
```

---

### Q2-2: orElse vs orElseGet, 뭐가 다른가요?

**A:** orElse는 항상 실행되고, orElseGet은 필요할 때만 실행됩니다!

```java
// orElse: 값이 있어도 createDefault() 실행!
Optional<String> opt = Optional.of("Hello");
String result = opt.orElse(createDefault());
// 1. createDefault() 실행 (비용 발생!)
// 2. "Hello" 반환

// orElseGet: 값이 있으면 실행 안 함!
String result = opt.orElseGet(() -> createDefault());
// 1. opt에 값 있음 확인
// 2. "Hello" 반환 (createDefault() 실행 안 함!)
```

**이유:** 자바 메서드 호출 시 인수는 메서드 실행 전에 평가됨

---

### Q2-3: ifPresentOrElse에서 람다 2개면 Consumer 2개 아닌가요?

**A:** 아닙니다! Consumer + Runnable입니다!

```java
public void ifPresentOrElse(
    Consumer<? super T> action,  // 값 받음
    Runnable emptyAction         // 아무것도 안 받음
)
```

**이유:**
```
값이 있을 때: value를 사용해야 함 → Consumer<T>
값이 없을 때: 사용할 값이 없음 → Runnable
```

만약 두 번째도 Consumer였다면:
```java
if (value != null) {
    action.accept(value);
} else {
    emptyAction.accept(???);  // 💥 뭘 넘겨줘? null? NPE!
}
```

---

### Q2-4: map vs flatMap, 이중 중첩이 뭔가요?

**A:** map에서 Optional을 반환하면 Optional[Optional[...]]이 됩니다!

```java
Optional<String> name = Optional.of("john");

// map 사용: 이중 중첩!
Optional<Optional<String>> nested = 
    name.map(s -> Optional.of(s.toUpperCase()));
// Optional[Optional["JOHN"]] 💥

// flatMap 사용: 한 겹!
Optional<String> upper = 
    name.flatMap(s -> Optional.of(s.toUpperCase()));
// Optional["JOHN"] ✅
```

**핵심:**
```java
// map: 결과를 자동으로 감쌈
map(s -> Optional.of(...))
    ↓
Optional[Optional[...]]  // 이중 중첩!

// flatMap: 그대로 반환
flatMap(s -> Optional.of(...))
    ↓
Optional[...]  // 한 겹!
```

---

## Q3. 람다 제약사항

### Q3-1: 왜 배열 우회는 되는 건가요?

**A:** count 변수 자체는 재할당 안 되기 때문입니다!

```java
int[] count = {0};
list.forEach(item -> count[0]++);  // ✅ OK
```

**메모리 관점:**

```
Stack:
  count → [주소 0x1234]  (변수는 그대로!)
  
Heap:
  0x1234 → [0]  (내용만 변경)
  
count[0]++:
  count 변수: 주소는 그대로 (재할당 안 됨)
  count[0]: 배열 내용만 변경
  → count는 effectively final ✅
```

**핵심:**
```
count = ...  ← 이런 코드 있나? 없음!
    ↓
count는 effectively final
    ↓
람다에서 사용 가능!
```

---

### Q3-2: reduce는 어떻게 동작하나요?

**A:** 초기값부터 시작해서 각 요소를 누적합니다!

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

int sum = numbers.stream()
    .reduce(0, (a, b) -> a + b);
```

**실행 과정:**

```
Step 0: accumulator = 0 (identity)

Step 1: (0, 1) → 0 + 1 = 1
Step 2: (1, 2) → 1 + 2 = 3
Step 3: (3, 3) → 3 + 3 = 6
Step 4: (6, 4) → 6 + 4 = 10
Step 5: (10, 5) → 10 + 5 = 15

결과: 15
```

**시각화:**

```
         0 (초기값)
         ↓ + 1
         1
         ↓ + 2
         3
         ↓ + 3
         6
         ↓ + 4
        10
         ↓ + 5
        15 ← 최종
```

---

### Q3-3: AtomicInteger는 뭔가요?

**A:** 스레드 안전한 정수 클래스입니다!

```java
AtomicInteger count = new AtomicInteger(0);

list.forEach(item -> count.incrementAndGet());  // ✅ OK
```

**왜 람다에서 가능한가?**
```
count 변수 자체: 재할당 안 됨 (effectively final)
count.incrementAndGet(): 메서드 호출일 뿐
```

**스레드 안전성:**

```java
// ❌ 일반 int: race condition
int[] count = {0};
threads.forEach(t -> count[0]++);  // 예상: 1000, 실제: 523

// ✅ AtomicInteger: 안전
AtomicInteger count = new AtomicInteger(0);
threads.forEach(t -> count.incrementAndGet());  // 1000 (정확!)
```

**원리:** CAS (Compare-And-Swap) 알고리즘 사용

---

## Q4. void 호환 규칙

### Q4-1: Map.put()이 이전값을 반환한다고요?

**A:** 네, 맞습니다!

```java
Map<String, Integer> map = new HashMap<>();

Integer old1 = map.put("A", 100);  // null (처음)
Integer old2 = map.put("A", 200);  // 100 (이전 값!)
Integer old3 = map.put("A", 300);  // 200 (이전 값!)
```

**과정:**

```
초기: {}
put("A", 100) → null 반환, map = {A=100}
put("A", 200) → 100 반환, map = {A=200}
               ^^^
               이전 값!
```

**이전 값 기준:**
```
이전 값 = put() 호출 직전에 그 키에 매핑된 값

map = {A=100}
put("A", 200) 호출
→ 이전 값 = 100
→ 100 반환
→ map = {A=200}
```

---

### Q4-2: this::validate에서 this는 언제 쓰나요?

**A:** 인스턴스 메서드를 참조할 때 씁니다!

```java
public class UserService {
    private UserRepository repository;  // 인스턴스 변수
    
    // 인스턴스 메서드
    public String validate(User user) {
        // repository 사용
        return null;
    }
    
    public void process(List<User> users) {
        users.stream()
            .map(this::validate)  // ← 현재 객체의 validate
            .collect(toList());
    }
}
```

**this가 필요한 이유:**
```
validate() 메서드:
    - 인스턴스 변수 사용
    - 특정 객체의 메서드여야 함
    
따라서:
    this::validate (현재 객체의 메서드)
```

**대조:**
```java
// static 메서드
Integer::parseInt

// 파라미터의 메서드
String::toUpperCase

// 특정 객체의 메서드
this::validate
str::concat
```

---

### Q4-3: Function.identity()가 뭔가요?

**A:** 입력을 그대로 반환하는 함수입니다!

```java
// Function.identity()는 이것과 같음:
Function<String, String> identity = t -> t;

// 동작:
String result = identity.apply("Hello");  // "Hello"
```

**toMap에서 사용:**

```java
List<String> words = Arrays.asList("a", "b", "c");

Map<String, Integer> map = words.stream()
    .collect(Collectors.toMap(
        Function.identity(),  // 키: 단어 그대로
        String::length        // 값: 길이
    ));
// {a=1, b=1, c=1}
```

**왜 좋은가?**
- 의도 명확: "항등 함수"
- 재사용 가능: 같은 Function 객체
- 타입 안전: 컴파일러가 타입 추론 쉬움

---

## Q5. reduce의 동작

### Q5-1: reduce의 3가지 파라미터가 뭔가요?

**A:** identity, accumulator, combiner입니다!

```java
<U> U reduce(
    U identity,
    BiFunction<U, T, U> accumulator,
    BinaryOperator<U> combiner
)
```

**예시:**

```java
int totalLength = words.parallelStream()
    .reduce(
        0,                              // identity
        (acc, word) -> acc + word.length(),  // accumulator
        (a, b) -> a + b                 // combiner
    );
```

---

### Q5-2: 왜 3개가 필요한가요?

**A:** 타입 변환 때문입니다!

```java
Stream<String> → int

// 2개로는 불가능:
reduce(0, (sum, word) -> sum + word.length())
//         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
//         sum은 int, word는 String → 타입 오류!

// 3개로 가능:
reduce(
    0,                              // int (결과 타입)
    (acc, word) -> acc + word.length(),  // (int, String) → int
    (a, b) -> a + b                 // (int, int) → int
)
```

---

### Q5-3: combiner는 언제 사용되나요?

**A:** 병렬 스트림에서만 사용됩니다!

**순차 스트림:**
```
         0
         ↓ accumulator
         5
         ↓ accumulator
        10

combiner 호출 안 됨!
```

**병렬 스트림:**
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
      10                10
       └────────┬────────┘
                ↓
         combiner(10, 10)
                ↓
               20
```

---

## Q6. 메서드 참조 변환

### Q6-1: BiPredicate를 List::contains로 어떻게 변환하나요?

**A:** 단계별로 분석하면 됩니다!

```java
BiPredicate<List<String>, String> contains = 
    (list, element) -> list.contains(element);
```

**Step 1: 람다 분석**
```
파라미터: list (List), element (String)
바디: list.contains(element)
      ^^^^ ^^^^^^^^ ^^^^^^^
      대상  메서드    인수
```

**Step 2: 패턴 파악**
```
(A, B) -> A.method(B)

첫 번째 파라미터 (list) → 메서드 호출 대상
두 번째 파라미터 (element) → 메서드 인수
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

### Q6-2: 변환 패턴은 몇 가지인가요?

**A:** 4가지 주요 패턴이 있습니다!

**1. 정적 메서드:**
```java
(String s) -> Integer.parseInt(s)
    ↓
Integer::parseInt
```

**2. 인스턴스 메서드 (임의 객체):**
```java
(String s) -> s.toUpperCase()
    ↓
String::toUpperCase
```

**3. 인스턴스 메서드 (기존 객체):**
```java
() -> str.length()
    ↓
str::length
```

**4. 생성자:**
```java
() -> new ArrayList<>()
    ↓
ArrayList::new
```

---

## Q7. Function 조합

### Q7-1: andThen과 compose의 차이가 뭔가요?

**A:** 실행 순서가 반대입니다!

**andThen: f → g**
```java
f.andThen(g) = g(f(x))

Function<String, Integer> getLength = s -> s.length();
Function<Integer, Integer> doubleIt = n -> n * 2;

Function<String, Integer> result = getLength.andThen(doubleIt);

result.apply("Hello");
// 1. getLength("Hello") = 5
// 2. doubleIt(5) = 10
```

**compose: g → f**
```java
f.compose(g) = f(g(x))

Function<Integer, Integer> doubleIt = n -> n * 2;
Function<String, Integer> getLength = s -> s.length();

Function<String, Integer> result = doubleIt.compose(getLength);

result.apply("Hello");
// 1. getLength("Hello") = 5  (먼저!)
// 2. doubleIt(5) = 10
```

---

### Q7-2: 어떤 걸 사용해야 하나요?

**A:** 일반적으로 andThen이 더 직관적입니다!

```java
// ✅ andThen: 읽기 쉬움
user
    .getName()
    .andThen(String::toUpperCase)
    .andThen(this::greet);
// 순서: 이름 → 대문자 → 인사

// compose: 읽기 어려움
greet
    .compose(String::toUpperCase)
    .compose(getName);
// 역순으로 읽어야 함
```

---

## Q8. 박싱과 성능

### Q8-1: DoubleFunction은 왜 박싱하지 않나요?

**A:** 파라미터가 기본형이기 때문입니다!

```java
// Function<Double, Double>
Double apply(Double t)
       ^^^^^^ ^^^^^^
       둘 다 객체! → 박싱 필요

// DoubleFunction<R>
R apply(double value)
        ^^^^^^
        기본형! → 박싱 불필요
```

**동작:**

```java
// Function<Double, Double>
Function<Double, Double> square = x -> x * x;

double value = 3.0;
Double result = square.apply(value);
// 1. value 박싱: double → Double
// 2. x 언박싱: Double → double (계산)
// 3. 계산: 3.0 * 3.0
// 4. 결과 박싱: double → Double
// 총 박싱: 4번!

// DoubleFunction<Double>
DoubleFunction<Double> square = x -> x * x;

double value = 3.0;
Double result = square.apply(value);
// 1. value 그대로 전달 (박싱 없음!)
// 2. 계산: 3.0 * 3.0
// 3. 결과 박싱: double → Double
// 총 박싱: 1번!
```

---

### Q8-2: 얼마나 빠른가요?

**A:** 약 7배 빠릅니다!

```java
// 1,000만 번 반복

Function<Double, Double>:
    약 850ms  💥

DoubleUnaryOperator:
    약 120ms  ✅

차이: 약 7배!
```

**메모리도 절약:**
```
Function: 1000개 람다 + 수많은 Double 객체
DoubleUnaryOperator: 1000개 람다만
```

---

### Q8-3: 언제 기본형 특화를 사용하나요?

**A:** 다음 경우에 사용하세요!

**✅ 사용:**
```java
// 1. 대량 데이터
IntStream.range(0, 1_000_000)
    .map(i -> i * i)
    .sum();

// 2. 수학 연산
DoubleUnaryOperator sqrt = Math::sqrt;

// 3. 반복 연산
IntUnaryOperator increment = n -> n + 1;
for (int i = 0; i < 1000; i++) {
    value = increment.applyAsInt(value);
}
```

**❌ 불필요:**
```java
// 소량 데이터
List<Integer> small = Arrays.asList(1, 2, 3);
small.stream().map(n -> n * n);  // OK

// 객체와 함께
users.stream()
    .map(User::getAge)  // 어차피 객체
```

---

## 💡 자주 하는 실수

### 1. 지역변수 재할당

```java
// ❌ 에러
int count = 0;
list.forEach(item -> count++);
count = 10;  // 재할당!

// ✅ 해결
int count = list.stream().count();
```

---

### 2. Optional.get() 직접 호출

```java
// ❌ 위험
Optional<String> opt = ...;
String value = opt.get();  // NoSuchElementException!

// ✅ 안전
String value = opt.orElse("Default");
```

---

### 3. void 호환 남용

```java
// ❌ 정보 손실
list.forEach(s -> map.put(s, s.length()));

// ✅ 명확
Map<String, Integer> map = list.stream()
    .collect(toMap(identity(), String::length));
```

---

### 4. 박싱 오버헤드 무시

```java
// ❌ 느림
List<Integer> numbers = ...;
numbers.stream()
    .map(n -> n * n)
    .reduce(0, Integer::sum);

// ✅ 빠름
int[] numbers = ...;
IntStream.of(numbers)
    .map(n -> n * n)
    .sum();
```

---

## ✅ 체크리스트

학습 완료 후 확인하세요:

- [ ] 람다에서 checked exception 처리 방법을 안다
- [ ] Optional의 orElse와 orElseGet 차이를 안다
- [ ] 지역변수 제약의 이유를 이해한다
- [ ] void 호환의 함정을 안다
- [ ] reduce의 3가지 파라미터 역할을 안다
- [ ] 람다를 메서드 참조로 변환할 수 있다
- [ ] andThen과 compose의 차이를 안다
- [ ] 기본형 특화의 성능 이점을 안다

---

**이 Q&A 세션으로 람다의 모든 궁금증이 해결되었기를 바랍니다!** 🎉
