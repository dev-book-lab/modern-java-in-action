# Chapter 02. 동작 파라미터화 코드 전달하기

<div align="center">

**"변화하는 요구사항에 유연하게 대응하는 코드 작성법"**

> *메서드의 동작을 파라미터로 전달해서 런타임에 결정한다*

[📖 Deep Dive](advanced/deep-dive.md) | [💻 Practice](code/) | [📋 CheatSheet](advanced/cheatsheet.md)

</div>

---

## 🎯 학습 목표

이 챕터를 마치면 다음을 할 수 있습니다:

- [ ] **동작 파라미터화**의 개념과 필요성을 설명할 수 있다
- [ ] **Predicate 패턴**으로 필터링 로직을 추상화할 수 있다
- [ ] **익명 클래스 → 람다 → 메서드 참조** 로의 발전 과정을 이해한다
- [ ] **Comparator, Runnable, Callable** 을 실전에서 활용할 수 있다
- [ ] **제너릭을 통한 일반화**로 재사용 가능한 코드를 작성할 수 있다

---

## 📚 핵심 개념

### 동작 파라미터화란?

**동작 파라미터화(Behavior Parameterization)** 는 아직 어떻게 실행할지 결정하지 않은 코드 블록을 메서드의 인수로 전달하는 기법입니다.

```java
// ❌ 요구사항마다 메서드가 증가
filterGreenApples(inventory);
filterRedApples(inventory);
filterHeavyApples(inventory);

// ✅ 동작을 파라미터로 전달
filter(inventory, apple -> apple.getColor() == GREEN);
filter(inventory, apple -> apple.getColor() == RED);
filter(inventory, apple -> apple.getWeight() > 150);
```

### 왜 필요한가?

실무에서는 요구사항이 끊임없이 변합니다:

1. **"녹색 사과만 필터링해주세요"**
2. **"빨간 사과도 필터링할 수 있어야 해요"**
3. **"150그램 이상인 사과도 필터링하고 싶어요"**
4. **"녹색이면서 무거운 사과를 찾고 싶어요"**
5. **"색상과 무게를 동적으로 선택하고 싶어요"**

각 요구사항마다 새로운 메서드를 만드는 것은 **DRY 원칙(Don't Repeat Yourself)** 을 위반합니다.

동작 파라미터화를 사용하면 **하나의 메서드로 모든 요구사항을 처리**할 수 있습니다.

---

## 🚀 발전 과정 - 7단계

### 1단계: 녹색 사과 필터링

```java
// ❌ 문제: 다른 색상이 필요하면?
public static List<Apple> filterGreenApples(List<Apple> inventory) {
    List<Apple> result = new ArrayList<>();
    for (Apple apple : inventory) {
        if (apple.getColor() == Color.GREEN) {
            result.add(apple);
        }
    }
    return result;
}
```

### 2단계: 색을 파라미터화

```java
// ⚠️ 개선되었지만, 무게 필터링은?
public static List<Apple> filterApplesByColor(List<Apple> inventory, Color color) {
    List<Apple> result = new ArrayList<>();
    for (Apple apple : inventory) {
        if (apple.getColor() == color) {
            result.add(apple);
        }
    }
    return result;
}
```

### 3단계: 가능한 모든 속성으로 필터링

```java
// ❌ 최악의 코드 - flag의 의미를 알 수 없음
public static List<Apple> filterApples(
    List<Apple> inventory, Color color, int weight, boolean flag) {
    
    List<Apple> result = new ArrayList<>();
    for (Apple apple : inventory) {
        if ((flag && apple.getColor() == color) 
            || (!flag && apple.getWeight() > weight)) {
            result.add(apple);
        }
    }
    return result;
}

// 사용할 때도 혼란스러움
filterApples(inventory, GREEN, 0, true);  // true가 뭐지?
filterApples(inventory, null, 150, false); // null은 왜?
```

### 4단계: 동작을 추상화 - Predicate 패턴

```java
// ✅ 핵심 돌파구!
interface ApplePredicate {
    boolean test(Apple apple);
}

// 구현체들
class AppleGreenColorPredicate implements ApplePredicate {
    public boolean test(Apple apple) {
        return apple.getColor() == Color.GREEN;
    }
}

class AppleHeavyWeightPredicate implements ApplePredicate {
    public boolean test(Apple apple) {
        return apple.getWeight() > 150;
    }
}

// 동작 파라미터화된 메서드
public static List<Apple> filter(List<Apple> inventory, ApplePredicate p) {
    List<Apple> result = new ArrayList<>();
    for (Apple apple : inventory) {
        if (p.test(apple)) {  // 전략 패턴!
            result.add(apple);
        }
    }
    return result;
}

// 사용
List<Apple> greenApples = filter(inventory, new AppleGreenColorPredicate());
List<Apple> heavyApples = filter(inventory, new AppleHeavyWeightPredicate());
```

**이것이 전략 디자인 패턴(Strategy Design Pattern)입니다!**

### 5단계: 익명 클래스로 간소화

```java
// ✅ 클래스 정의 없이 즉석에서 구현
List<Apple> redApples = filter(inventory, new ApplePredicate() {
    @Override
    public boolean test(Apple apple) {
        return apple.getColor() == Color.RED;
    }
});

// ⚠️ 하지만 여전히 보일러플레이트 코드가 많음
```

### 6단계: 람다로 간결하게 (Java 8+)

```java
// ✅ 최고의 간결함!
List<Apple> redApples = filter(inventory, 
    apple -> apple.getColor() == Color.RED);

List<Apple> heavyApples = filter(inventory, 
    apple -> apple.getWeight() > 150);

List<Apple> greenAndHeavy = filter(inventory,
    apple -> apple.getColor() == Color.GREEN && apple.getWeight() > 150);
```

### 7단계: 제너릭으로 일반화

```java
// ✅ 모든 타입에 사용 가능!
public interface Predicate<T> {
    boolean test(T t);
}

public static <T> List<T> filter(List<T> list, Predicate<T> p) {
    List<T> result = new ArrayList<>();
    for (T e : list) {
        if (p.test(e)) {
            result.add(e);
        }
    }
    return result;
}

// 사과 필터링
List<Apple> redApples = filter(inventory, 
    apple -> apple.getColor() == Color.RED);

// 정수 필터링
List<Integer> evenNumbers = filter(numbers, 
    n -> n % 2 == 0);

// 문자열 필터링
List<String> longStrings = filter(strings, 
    s -> s.length() > 5);
```

---

## 💡 핵심 함수형 인터페이스

Java 8은 자주 사용되는 함수형 인터페이스를 제공합니다:

| 인터페이스 | 메서드 시그니처 | 용도 | 예시 |
|-----------|----------------|------|------|
| `Predicate<T>` | `T → boolean` | 필터링 (조건 검사) | `a -> a.getWeight() > 150` |
| `Consumer<T>` | `T → void` | 부작용 (출력, 저장) | `a -> System.out.println(a)` |
| `Function<T,R>` | `T → R` | 변환 (맵핑) | `a -> a.getWeight()` |
| `Supplier<T>` | `() → T` | 생산 (생성) | `() -> new Apple()` |
| `Comparator<T>` | `(T,T) → int` | 정렬 비교 | `(a,b) -> a.getWeight() - b.getWeight()` |
| `Runnable` | `() → void` | 스레드 실행 | `() -> doWork()` |
| `Callable<T>` | `() → T` | 스레드 결과 반환 | `() -> compute()` |

### 사용 예제

```java
// 1. Predicate - 필터링
List<Apple> greenApples = filter(inventory, 
    a -> a.getColor() == Color.GREEN);

// 2. Consumer - 각 요소 처리
forEach(inventory, a -> System.out.println(a));
forEach(inventory, a -> save(a));

// 3. Function - 값 변환
List<Integer> weights = map(inventory, a -> a.getWeight());
List<String> descriptions = map(inventory, a -> a.getColor() + " Apple");

// 4. Comparator - 정렬
inventory.sort(Comparator.comparingInt(Apple::getWeight));
inventory.sort(Comparator.comparingInt(Apple::getWeight).reversed());

// 5. Runnable - 스레드
new Thread(() -> System.out.println("Hello")).start();

// 6. Callable - 결과 반환하는 스레드
ExecutorService executor = Executors.newSingleThreadExecutor();
Future<String> result = executor.submit(() -> Thread.currentThread().getName());
```

---

## 🎓 실전 활용

### Pattern 1: Predicate 합성

```java
Predicate<Apple> isGreen = a -> a.getColor() == Color.GREEN;
Predicate<Apple> isHeavy = a -> a.getWeight() > 150;

// AND: 둘 다 만족
filter(inventory, isGreen.and(isHeavy));

// OR: 하나라도 만족
filter(inventory, isGreen.or(isHeavy));

// NOT: 반대
filter(inventory, isGreen.negate());

// 복합: 체이닝
filter(inventory, isGreen.and(isHeavy).or(isFresh));
```

### Pattern 2: Comparator 활용

```java
// 기본 정렬
students.sort(Comparator.comparingInt(Student::getScore));

// 역순
students.sort(Comparator.comparingInt(Student::getScore).reversed());

// 다중 조건
students.sort(
    Comparator.comparingInt(Student::getGrade)
              .thenComparing(Student::getName)
);
```

### Pattern 3: 스레드와 동작 파라미터화

```java
// Runnable - 결과 없음
Thread t = new Thread(() -> System.out.println("Task"));
t.start();

// Callable - 결과 반환
ExecutorService executor = Executors.newSingleThreadExecutor();
Future<Integer> future = executor.submit(() -> {
    return IntStream.rangeClosed(1, 10).sum();
});
Integer result = future.get();
```

---

## 📂 학습 자료 구조

```
chapter02/
├── README.md                  # 👈 현재 문서
├── code/                      # 실습 코드
│   ├── FilteringApples.java   # 필터링 발전 과정
│   ├── PredicateExamples.java # Predicate 패턴
│   ├── ComparatorExamples.java# 정렬 예제
│   └── ThreadExamples.java    # Runnable/Callable
└── advanced/                  # 심화 학습
    ├── deep-dive.md           # 상세 원리 설명
    ├── cheatsheet.md          # 빠른 참조 가이드
    ├── practice-guide.md      # 10가지 실전 패턴
    ├── comparator-guide.md    # Comparator 완벽 가이드
    └── qa-sessions.md         # AI와의 Q&A 세션
```

---

## 🔑 핵심 원칙

### 1. DRY (Don't Repeat Yourself)
- 중복된 코드를 추상화하라
- 변하는 부분과 변하지 않는 부분을 분리하라

### 2. 전략 패턴 (Strategy Pattern)
- 알고리즘을 캡슐화하라
- 런타임에 동작을 선택하라

### 3. 점진적 개선
```
단순 구현 → 파라미터화 → 동작 추상화 → 익명 클래스 
→ 람다 → 메서드 참조 → 제너릭 일반화
```

### 4. 함수형 사고
```
데이터 → filter → map → reduce → 결과
```

---

## ⚡ Quick Reference

### 가장 자주 사용하는 10가지 패턴

```java
// 1. 필터링
filter(list, x -> condition);

// 2. Predicate 합성
filter(list, pred1.and(pred2).or(pred3));

// 3. 각 요소 처리
forEach(list, x -> System.out.println(x));

// 4. 값 변환
map(list, x -> x.getValue());

// 5. 정렬
list.sort(Comparator.comparing(X::getValue));

// 6. 역순 정렬
list.sort(Comparator.comparing(X::getValue).reversed());

// 7. 다중 정렬
list.sort(Comparator.comparing(X::getFirst)
                    .thenComparing(X::getSecond));

// 8. 스레드 실행
new Thread(() -> doWork()).start();

// 9. 결과 반환 스레드
executor.submit(() -> compute());

// 10. 메서드 참조
list.sort(Comparator.comparing(Apple::getWeight));
```

---

## 🎯 학습 체크리스트

- [ ] 동작 파라미터화의 개념을 **자신의 언어로** 설명할 수 있다
- [ ] Predicate 패턴의 **장점 3가지**를 말할 수 있다
- [ ] 익명 클래스와 람다의 **차이점**을 설명할 수 있다
- [ ] **7가지 함수형 인터페이스**의 용도를 구분할 수 있다
- [ ] Comparator로 **다중 조건 정렬**을 구현할 수 있다
- [ ] Runnable과 Callable의 **차이점**을 설명할 수 있다
- [ ] 제너릭으로 **타입 안전한 일반화**를 할 수 있다
- [ ] **전략 패턴**을 실무에 적용할 수 있다

---

## 📖 더 알아보기

- [Deep Dive](advanced/deep-dive.md) - 동작 파라미터화의 내부 원리
- [CheatSheet](advanced/cheatsheet.md) - 빠른 참조 가이드
- [Practice Guide](advanced/practice-guide.md) - 10가지 실전 패턴
- [Comparator Guide](advanced/comparator-guide.md) - 정렬 완벽 가이드
- [Q&A Sessions](advanced/qa-sessions.md) - AI와의 대화 기록

---

## 🚀 다음 단계

이제 **Chapter 3: 람다 표현식**으로 넘어갈 준비가 되었습니다!

Chapter 3에서는:
- 람다 표현식의 **문법과 제약사항**
- **함수형 인터페이스**의 설계 원칙
- **메서드 참조**의 4가지 형태
- **클로저와 변수 캡처**
- **타입 추론**의 메커니즘

을 학습합니다.

---

<div align="center">

**💡 Key Takeaway**

> *"동작 파라미터화는 코드를 데이터처럼 전달하는 기법이다.*  
> *이를 통해 유연하고 재사용 가능한 API를 만들 수 있다."*

**🌟 동작 파라미터화를 마스터하면, 함수형 프로그래밍의 세계로 들어갈 문이 열립니다!**

</div>
