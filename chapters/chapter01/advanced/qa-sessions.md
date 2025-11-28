# AI와의 대화로 배우는 자바 8

> Claude AI와의 실제 대화를 통해 개념을 깊이 이해하는 과정

---

## 📌 이 문서에 대하여

이 문서는 자바 8을 학습하면서 AI와 나눈 질문과 답변을 정리한 것입니다.
**"왜?"라는 질문을 통한 깊이 있는 이해**를 목표로 합니다.

---

## 💬 Session 1: 자바 8의 본질

### Q1: 자바 8이 왜 이렇게 중요한가요?

**A:** 자바 8은 **프로그래밍 패러다임의 전환**을 가져왔기 때문입니다.

**하드웨어의 변화:**
```
단일 코어 시대 → 멀티 코어 시대
CPU 클럭 향상 → 코어 개수 증가

기존 자바의 문제:
- Thread, synchronized로 병렬 처리
- 복잡하고 에러 발생 가능성 높음

자바 8의 해법:
- parallelStream() 한 줄로 병렬 처리
- 안전하고 간단함
```

**소프트웨어 트렌드:**
```
빅데이터, 실시간 처리 요구 증가
    ↓
효율적인 병렬 처리 필수
    ↓
함수형 프로그래밍 도입
```

### Q2: 스트림이 정확히 뭔가요? 컬렉션과 뭐가 다른가요?

**A:** 스트림은 **데이터 처리 연산의 파이프라인**입니다.

**컬렉션 vs 스트림:**
```
컬렉션 (Collection):
- 데이터를 저장하는 자료구조
- 메모리에 모든 요소 보유
- 공간 중심 (Space-oriented)

스트림 (Stream):
- 데이터를 처리하는 파이프라인
- 요소를 필요할 때 계산
- 시간 중심 (Time-oriented)
```

**예시로 이해하기:**
```java
List<Apple> apples = ...;  // 컬렉션: 모든 사과를 메모리에

Stream<Apple> stream = apples.stream();  // 스트림: 처리 파이프라인
    .filter(a -> a.getWeight() > 150)    // 연산 정의
    .map(Apple::getColor);                // 연산 정의
// 아직 실행 안 됨!

List<String> colors = stream.collect(Collectors.toList());  // 여기서 실행!
```

---

## 💬 Session 2: 동작 파라미터화

### Q3: 동작 파라미터화가 왜 필요한가요?

**A:** **변화하는 요구사항에 유연하게 대응**하기 위해서입니다.

**실무 시나리오:**
```
1일차: "녹색 사과만 필터링해주세요"
    → filterGreenApples() 작성

2일차: "빨간 사과도 필터링할 수 있어야 해요"
    → filterRedApples() 작성

3일차: "무거운 사과도 필터링하고 싶어요"
    → filterHeavyApples() 작성

4일차: "녹색이면서 무거운 사과를..."
    → filterGreenAndHeavyApples() ... 😰
```

**동작 파라미터화로 해결:**
```java
// 하나의 메서드로 모든 요구사항 처리!
filterApples(inventory, apple -> "green".equals(apple.getColor()));
filterApples(inventory, apple -> "red".equals(apple.getColor()));
filterApples(inventory, apple -> apple.getWeight() > 150);
filterApples(inventory, apple -> 
    "green".equals(apple.getColor()) && apple.getWeight() > 150);
```

### Q4: Predicate가 정확히 뭔가요? 어떻게 동작하나요?

**A:** Predicate는 **조건을 표현하는 함수형 인터페이스**입니다.

**구조:**
```java
@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);  // 유일한 추상 메서드
}
```

**동작 원리:**
```java
// 1. 람다 작성
Predicate<Apple> p = apple -> apple.getWeight() > 150;

// 2. 컴파일러가 내부적으로 변환 (개념적)
Predicate<Apple> p = new Predicate<Apple>() {
    @Override
    public boolean test(Apple apple) {
        return apple.getWeight() > 150;  // 람다 본문이 여기로!
    }
};

// 3. 사용
if (p.test(myApple)) {  // test() 메서드 호출
    System.out.println("무거운 사과!");
}
```

**왜 함수형 인터페이스인가?**
```
함수형 인터페이스 = 추상 메서드가 정확히 1개

왜 1개?
→ 람다가 어떤 메서드를 구현하는지 명확해야 함

만약 추상 메서드가 2개라면?
→ 람다가 어떤 걸 구현하는지 모호함!
```

---

## 💬 Session 3: 람다와 메서드 참조

### Q5: 람다와 익명 클래스는 어떻게 다른가요?

**A:** 가장 큰 차이는 **구현 방식**과 **this의 의미**입니다.

**구현 방식:**
```java
// 익명 클래스: 컴파일 시 .class 파일 생성
Runnable r1 = new Runnable() {
    public void run() { ... }
};
// → MyClass$1.class 생성

// 람다: invokedynamic 바이트코드 사용
Runnable r2 = () -> { ... };
// → .class 파일 생성 안 함, 런타임에 처리
```

**this의 의미:**
```java
class Example {
    int value = 1;
    
    void test() {
        // 익명 클래스: this = 익명 클래스 자신
        Runnable r1 = new Runnable() {
            int value = 2;
            public void run() {
                System.out.println(this.value);  // 2
            }
        };
        
        // 람다: this = Example 인스턴스
        Runnable r2 = () -> {
            System.out.println(this.value);  // 1
        };
    }
}
```

### Q6: 메서드 참조는 언제 사용하나요?

**A:** **의미를 명확히 표현하고 싶을 때** 사용합니다.

**선택 기준:**
```java
// 1. 간단하고 한 번만 사용 → 람다
list.stream()
    .filter(x -> x > 10)
    .collect(Collectors.toList());

// 2. 재사용되거나 의미가 명확해야 함 → 메서드 참조
public static boolean isHeavy(Apple apple) {
    return apple.getWeight() > 150;
}

list.stream()
    .filter(FilteringApples::isHeavy)  // 의미 명확!
    .collect(Collectors.toList());

// 3. 복잡한 로직 → 메서드로 추출 후 참조
public static boolean isHeavyGreenApple(Apple apple) {
    return apple.getWeight() > 150 && 
           "green".equals(apple.getColor());
}

list.stream()
    .filter(FilteringApples::isHeavyGreenApple)
    .collect(Collectors.toList());
```

---

## 💬 Session 4: 스트림 API

### Q7: 외부 반복과 내부 반복이 뭔가요?

**A:** **누가 반복을 제어하느냐**의 차이입니다.

**외부 반복 (External Iteration):**
```java
// 개발자가 직접 반복 제어
List<Apple> result = new ArrayList<>();
for (Apple apple : inventory) {  // 명시적 반복
    if (apple.getWeight() > 150) {
        result.add(apple);
    }
}

문제점:
- 병렬화 어려움
- 최적화 어려움
- 가독성 낮음
```

**내부 반복 (Internal Iteration):**
```java
// 라이브러리가 반복 제어
List<Apple> result = inventory.stream()
    .filter(apple -> apple.getWeight() > 150)
    .collect(Collectors.toList());

장점:
- 병렬화 쉬움 (parallelStream()만 바꾸면 됨)
- 최적화 가능 (라이브러리가 알아서)
- 가독성 높음 (무엇을 할지만 표현)
```

### Q8: parallelStream()은 어떻게 동작하나요?

**A:** **Fork/Join 프레임워크**를 사용합니다.

**동작 원리:**
```
1. 포크 (Fork) - 데이터 분할
   inventory (100개)
        ↓
   ├─ 25개 (Thread 1)
   ├─ 25개 (Thread 2)
   ├─ 25개 (Thread 3)
   └─ 25개 (Thread 4)

2. 처리 (Process)
   각 스레드가 독립적으로 filter 수행

3. 조인 (Join) - 결과 합치기
   Thread 1 결과: [a1, a2]
   Thread 2 결과: [a3]
   Thread 3 결과: [a4, a5, a6]
   Thread 4 결과: []
        ↓
   최종 결과: [a1, a2, a3, a4, a5, a6]
```

**언제 사용하면 좋을까?**
```
✅ 좋은 경우:
- 큰 데이터셋 (10,000개 이상)
- 계산 집약적 (각 요소 처리 시간 ≥ 1ms)
- 독립적 연산 (요소 간 의존성 없음)

❌ 나쁜 경우:
- 작은 데이터셋 (오버헤드 > 이득)
- 순서 의존적 (sorted 등)
- 공유 상태 접근 (synchronized 필요)
```

---

## 💬 Session 5: 디폴트 메서드

### Q9: 디폴트 메서드가 왜 필요한가요?

**A:** **인터페이스를 진화**시키기 위해서입니다.

**문제 상황:**
```java
// 자바 7: List 인터페이스
public interface List<E> {
    boolean add(E e);
    E get(int index);
    // ...
}

// 자바 8에서 sort()를 추가하고 싶다
public interface List<E> {
    void sort(Comparator<? super E> c);  // 💥
}

// 문제: 모든 List 구현체가 깨짐!
// - ArrayList, LinkedList, Vector
// - 전 세계의 커스텀 List 구현체들
```

**해결책:**
```java
public interface List<E> {
    // 디폴트 구현 제공!
    default void sort(Comparator<? super E> c) {
        Object[] a = this.toArray();
        Arrays.sort(a, (Comparator) c);
        // ...
    }
}

// ✅ 기존 구현체들은 수정 없이 사용 가능!
```

### Q10: 와일드카드 <? super E>는 왜 사용하나요?

**A:** **유연성**을 위해서입니다.

**시나리오:**
```java
class Fruit { }
class Apple extends Fruit { }

List<Apple> apples = new ArrayList<>();

// Fruit Comparator (상위 타입)
Comparator<Fruit> fruitComp = Comparator.comparing(Fruit::getName);

// ✅ Apple 리스트를 Fruit Comparator로 정렬 가능!
apples.sort(fruitComp);
// Apple은 Fruit이므로 안전!
```

**와일드카드 종류:**
```java
// 1. <? extends T> - 상한 제한 (읽기)
List<? extends Fruit> fruits;
// Apple, Orange 등 Fruit의 하위 타입

// 2. <? super T> - 하한 제한 (쓰기)
Comparator<? super Apple> comp;
// Apple, Fruit, Object 등 Apple의 상위 타입

// 3. <?> - 무제한 (읽기 전용)
List<?> unknowns;
```

---

## 💬 Session 6: 함수형 프로그래밍

### Q11: 순수 함수가 정확히 뭔가요?

**A:** **같은 입력에 항상 같은 출력**을 반환하고 **부작용이 없는** 함수입니다.

**순수 함수:**
```java
// ✅ 순수 함수
public static int add(int a, int b) {
    return a + b;
}
// - 입력: (2, 3) → 출력: 항상 5
// - 외부 상태 변경 없음

// ✅ 순수 함수
public static boolean isHeavy(Apple apple) {
    return apple.getWeight() > 150;
}
// - 입력만 보고 판단
// - 부작용 없음
```

**비순수 함수:**
```java
// ❌ 비순수: 외부 상태 의존
int threshold = 150;
public static boolean isHeavy(Apple apple) {
    return apple.getWeight() > threshold;
    // threshold 값이 바뀌면 결과도 바뀜!
}

// ❌ 비순수: 부작용 있음
List<Apple> result = new ArrayList<>();
public static boolean process(Apple apple) {
    result.add(apple);  // 외부 상태 변경!
    return true;
}
```

### Q12: 왜 함수형 프로그래밍이 병렬 처리에 좋은가요?

**A:** **공유 가변 상태가 없기** 때문입니다.

**문제: 공유 가변 상태**
```java
// 위험한 코드
List<Apple> result = new ArrayList<>();
inventory.parallelStream()
    .filter(a -> a.getWeight() > 150)
    .forEach(result::add);  // 💥

// 두 스레드가 동시에 add() 호출
// Thread 1: result.add(a1)
// Thread 2: result.add(a2)
// → ArrayIndexOutOfBoundsException!
```

**해결: 불변 방식**
```java
// 안전한 코드
List<Apple> result = inventory.parallelStream()
    .filter(a -> a.getWeight() > 150)
    .collect(Collectors.toList());  // ✅

// collect()는 내부적으로:
// 1. 각 스레드가 독립적인 리스트 생성
// 2. 병렬로 요소 추가
// 3. 최종적으로 모든 리스트 합침
// → 스레드 안전!
```

---

## 💬 Session 7: 성능과 최적화

### Q13: 스트림이 항상 빠른가요?

**A:** 아닙니다. **상황에 따라 다릅니다**.

**작은 데이터셋:**
```java
List<Integer> small = Arrays.asList(1, 2, 3, 4, 5);

// 반복문: 빠름 (~100ns)
for (int i : small) {
    if (i > 3) { ... }
}

// 스트림: 느림 (~500ns, 오버헤드)
small.stream().filter(i -> i > 3).forEach(...);
```

**큰 데이터셋:**
```java
List<Integer> large = IntStream.range(0, 1_000_000)
    .boxed()
    .collect(Collectors.toList());

// 순차 스트림: 중간
large.stream().filter(i -> i > 500_000).count();

// 병렬 스트림: 가장 빠름!
large.parallelStream().filter(i -> i > 500_000).count();
```

**결론:**
```
작은 데이터 (< 1000) → 반복문
중간 데이터 (1000 ~ 10000) → 순차 스트림
큰 데이터 (> 10000) → 병렬 스트림
```

### Q14: 박싱/언박싱이 왜 느린가요?

**A:** **메모리 할당과 타입 변환** 비용이 크기 때문입니다.

**박싱 비용:**
```java
// ❌ 비효율: Integer 객체 생성
int sum = list.stream()
    .map(x -> x.getValue())  // int → Integer (박싱)
    .reduce(0, (a, b) -> a + b);  // Integer → int (언박싱)

// 과정:
// 1. x.getValue() → int 반환
// 2. int를 Integer로 박싱 (객체 생성!)
// 3. reduce에서 Integer를 int로 언박싱
// 4. 덧셈 후 다시 Integer로 박싱
// → 1백만 요소면 3백만 번 객체 생성!
```

**최적화:**
```java
// ✅ 효율적: 기본형 스트림
int sum = list.stream()
    .mapToInt(X::getValue)  // IntStream 사용
    .sum();  // 박싱 없음!

// 과정:
// 1. x.getValue() → int 반환
// 2. int 그대로 IntStream으로
// 3. sum()도 int로 처리
// → 박싱 0번!
```

---

## 🎯 핵심 통찰

### 배운 것들

1. **자바 8 = 패러다임 전환**
   - 명령형 → 선언형
   - 순차적 → 병렬적
   - 복잡함 → 간결함

2. **스트림 ≠ 컬렉션**
   - 스트림 = 데이터 처리 파이프라인
   - 지연 평가 (Lazy Evaluation)
   - 내부 반복

3. **동작 파라미터화 = 유연성**
   - 코드 중복 제거
   - 변화에 대응
   - 전략 패턴의 현대적 구현

4. **람다 > 익명 클래스**
   - invokedynamic
   - this의 의미 다름
   - 더 빠르고 간결

5. **함수형 = 안전한 병렬 처리**
   - 순수 함수
   - 불변성
   - 공유 가변 상태 없음

### 실전 원칙

```java
// ✅ DO: 선언형으로
list.stream()
    .filter(x -> x > 10)
    .collect(Collectors.toList());

// ❌ DON'T: 명령형으로
List<X> result = new ArrayList<>();
for (X x : list) {
    if (x > 10) result.add(x);
}

// ✅ DO: 순수 함수
x -> x.getValue() > 150

// ❌ DON'T: 부작용
x -> { result.add(x); return true; }

// ✅ DO: 기본형 스트림
list.stream().mapToInt(X::getValue).sum();

// ❌ DON'T: 박싱
list.stream().map(X::getValue).reduce(0, Integer::sum);
```

---

<div align="center">

**💡 최종 메시지**

> *"질문하고, 대화하고, 이해하라.*  
> *AI는 선생님이 아니라 대화 상대다."*

**계속 질문하고, 계속 배우세요! 🚀**

</div>
