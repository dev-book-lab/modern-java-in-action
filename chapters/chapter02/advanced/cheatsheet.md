# 동작 파라미터화 CheatSheet

> 빠르게 참조할 수 있는 핵심 요약

---

## 🎯 핵심 개념

```
동작 파라미터화 = 메서드의 동작을 파라미터로 전달
→ 런타임에 동작 결정
→ 유연하고 재사용 가능한 코드
```

---

## 📋 주요 함수형 인터페이스

| 인터페이스 | 메서드 시그니처 | 용도 | 예시 |
|-----------|----------------|------|------|
| `Predicate<T>` | `T → boolean` | 필터링 | `a -> a.getWeight() > 150` |
| `Consumer<T>` | `T → void` | 부작용 | `a -> System.out.println(a)` |
| `Function<T,R>` | `T → R` | 변환 | `a -> a.getWeight()` |
| `Supplier<T>` | `() → T` | 생산 | `() -> new Apple()` |
| `Comparator<T>` | `(T,T) → int` | 정렬 | `(a,b) -> a - b` |
| `Runnable` | `() → void` | 스레드 | `() -> doWork()` |
| `Callable<T>` | `() → T` | 결과 반환 | `() -> compute()` |

---

## 🔧 Predicate 패턴

### 기본 필터링

```java
public static <T> List<T> filter(List<T> list, Predicate<T> p) {
    List<T> result = new ArrayList<>();
    for (T e : list) {
        if (p.test(e)) result.add(e);
    }
    return result;
}

// 사용
List<Apple> greenApples = filter(inventory, a -> a.getColor() == GREEN);
```

### Predicate 합성

```java
Predicate<Apple> isGreen = a -> a.getColor() == GREEN;
Predicate<Apple> isHeavy = a -> a.getWeight() > 150;

// AND
filter(inventory, isGreen.and(isHeavy));

// OR
filter(inventory, isGreen.or(isHeavy));

// NOT
filter(inventory, isGreen.negate());

// 체이닝
filter(inventory, isGreen.and(isHeavy).or(isFresh));
```

---

## 🔄 Function 패턴

```java
public static <T, R> List<R> map(List<T> list, Function<T, R> f) {
    List<R> result = new ArrayList<>();
    for (T e : list) {
        result.add(f.apply(e));
    }
    return result;
}

// 사용
List<Integer> weights = map(inventory, Apple::getWeight);
List<String> descriptions = map(inventory, a -> a.getColor() + " Apple");
```

---

## 📤 Consumer 패턴

```java
public static <T> void forEach(List<T> list, Consumer<T> c) {
    for (T e : list) {
        c.accept(e);
    }
}

// 사용
forEach(inventory, System.out::println);
forEach(inventory, a -> a.setColor(RED));
```

---

## 📌 Comparator 패턴

```java
// 기본 정렬
inventory.sort(Comparator.comparingInt(Apple::getWeight));

// 역순
inventory.sort(Comparator.comparingInt(Apple::getWeight).reversed());

// 다중 조건
inventory.sort(
    Comparator.comparing(Apple::getColor)
              .thenComparingInt(Apple::getWeight)
);
```

---

## 🧵 스레드 패턴

### Runnable

```java
// 결과 없음
Thread t = new Thread(() -> System.out.println("Hello"));
t.start();
```

### Callable

```java
// 결과 반환
ExecutorService executor = Executors.newSingleThreadExecutor();
Future<Integer> future = executor.submit(() -> {
    return IntStream.rangeClosed(1, 10).sum();
});
Integer result = future.get();
```

---

## 🔤 람다 문법

```java
// 파라미터 없음
() -> "Hello"

// 파라미터 1개
a -> a.getWeight() > 150
(Apple a) -> a.getWeight() > 150

// 파라미터 여러 개
(a, b) -> a.getWeight() - b.getWeight()

// 여러 줄
(Apple a) -> {
    System.out.println(a);
    return a.getWeight() > 150;
}

// 메서드 참조
Apple::getWeight
System.out::println
Integer::parseInt
Apple::new
```

---

## 💡 가장 많이 쓰는 10가지

```java
// 1. 필터링
filter(list, x -> condition);

// 2. Predicate 합성
filter(list, p1.and(p2).or(p3));

// 3. 각 요소 처리
forEach(list, System.out::println);

// 4. 값 변환
map(list, X::getValue);

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

## ⚡ 핵심 원칙

### DRY 원칙
- 중복 코드 제거
- 변하는 부분과 변하지 않는 부분 분리

### 전략 패턴
- 알고리즘 캡슐화
- 런타임에 동작 선택

### 점진적 개선
```
익명 클래스 → 람다 → 메서드 참조
```

---

## 🎓 언제 무엇을 사용할까?

| 상황 | 사용할 것 |
|------|-----------|
| 조건 검사 (필터링) | `Predicate` |
| 각 요소 처리 (출력, 저장) | `Consumer` |
| 요소 변환 | `Function` |
| 정렬 | `Comparator` |
| 스레드 (결과 없음) | `Runnable` |
| 스레드 (결과 필요) | `Callable` |

---

## 📚 더 알아보기

- [Deep Dive](deep-dive.md) - 상세 원리
- [Practice Guide](practice-guide.md) - 10가지 패턴
- [Comparator Guide](comparator-guide.md) - 정렬 완벽 가이드
- [Q&A Sessions](qa-sessions.md) - AI 대화 기록

---

<div align="center">

**💡 핵심 메시지**

> *동작 파라미터화 = 코드를 데이터처럼 전달*  
> *→ 유연하고 재사용 가능한 API*

</div>
