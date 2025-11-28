# 자바 8 CheatSheet

> 빠르게 참조할 수 있는 핵심 요약

---

## 🎯 자바 8의 3대 핵심

```
1. 스트림 API      → 병렬 처리를 쉽게
2. 동작 파라미터화 → 코드 중복 제거
3. 디폴트 메서드   → 인터페이스 진화
```

---

## 📋 스트림 API 빠른 참조

### 기본 패턴

```java
// 필터링
list.stream()
    .filter(x -> condition)
    .collect(Collectors.toList());

// 변환
list.stream()
    .map(x -> x.getValue())
    .collect(Collectors.toList());

// 정렬
list.stream()
    .sorted(Comparator.comparing(X::getValue))
    .collect(Collectors.toList());

// 제한
list.stream()
    .limit(10)
    .collect(Collectors.toList());
```

### 병렬 처리

```java
// 순차 → 병렬 (한 단어만 바꾸면 됨!)
list.stream()       →  list.parallelStream()
```

### 통계 연산

```java
// 개수
long count = list.stream().count();

// 합계
int sum = list.stream().mapToInt(X::getValue).sum();

// 평균
double avg = list.stream().mapToInt(X::getValue).average().orElse(0.0);

// 최대/최소
int max = list.stream().mapToInt(X::getValue).max().orElse(0);
int min = list.stream().mapToInt(X::getValue).min().orElse(0);
```

### 그룹핑

```java
// 속성별 그룹화
Map<String, List<Apple>> grouped = apples.stream()
    .collect(Collectors.groupingBy(Apple::getColor));

// 개수로 그룹화
Map<String, Long> counted = apples.stream()
    .collect(Collectors.groupingBy(
        Apple::getColor,
        Collectors.counting()
    ));
```

---

## 🔧 람다 문법

```java
// 기본 형태
(파라미터) -> 표현식

// 예시
() -> "Hello"                              // 파라미터 없음
x -> x + 1                                 // 파라미터 1개
(x, y) -> x + y                            // 파라미터 여러 개
(Apple a) -> a.getWeight() > 150           // 타입 명시
x -> { System.out.println(x); return x; }  // 여러 줄
```

---

## 🎯 메서드 참조

| 형태 | 문법 | 예시 |
|------|------|------|
| 정적 메서드 | `ClassName::staticMethod` | `Integer::parseInt` |
| 인스턴스 메서드 | `instance::instanceMethod` | `System.out::println` |
| 타입의 메서드 | `ClassName::instanceMethod` | `String::toUpperCase` |
| 생성자 | `ClassName::new` | `Apple::new` |

---

## 📊 함수형 인터페이스

| 인터페이스 | 메서드 | 용도 | 예시 |
|-----------|--------|------|------|
| `Predicate<T>` | `boolean test(T)` | 조건 검사 | `a -> a > 10` |
| `Consumer<T>` | `void accept(T)` | 소비 | `x -> System.out.println(x)` |
| `Function<T,R>` | `R apply(T)` | 변환 | `s -> s.length()` |
| `Supplier<T>` | `T get()` | 공급 | `() -> new Apple()` |
| `Comparator<T>` | `int compare(T,T)` | 비교 | `(a,b) -> a - b` |

---

## 🔄 Comparator 패턴

```java
// 기본 정렬
list.sort(Comparator.comparing(X::getValue));

// 역순
list.sort(Comparator.comparing(X::getValue).reversed());

// 다중 조건
list.sort(
    Comparator.comparing(X::getFirst)
              .thenComparing(X::getSecond)
);

// int 최적화
list.sort(Comparator.comparingInt(X::getValue));
```

---

## 💡 디폴트 메서드

```java
// 인터페이스에 구현 포함
public interface MyInterface {
    // 추상 메서드
    void abstractMethod();
    
    // 디폴트 메서드
    default void defaultMethod() {
        System.out.println("Default implementation");
    }
}

// 구현체는 defaultMethod() 구현 안 해도 됨!
```

---

## 🎨 실전 패턴 10가지

### 1. 필터링 후 변환
```java
list.stream()
    .filter(x -> x > 10)
    .map(x -> x * 2)
    .collect(Collectors.toList());
```

### 2. 정렬 후 제한
```java
list.stream()
    .sorted(Comparator.comparing(X::getValue))
    .limit(5)
    .collect(Collectors.toList());
```

### 3. 중복 제거
```java
list.stream()
    .distinct()
    .collect(Collectors.toList());
```

### 4. 평탄화
```java
list.stream()
    .flatMap(x -> x.getItems().stream())
    .collect(Collectors.toList());
```

### 5. 조건 확인
```java
boolean hasHeavy = list.stream()
    .anyMatch(x -> x.getWeight() > 150);

boolean allGreen = list.stream()
    .allMatch(x -> "green".equals(x.getColor()));

boolean noBrown = list.stream()
    .noneMatch(x -> "brown".equals(x.getColor()));
```

### 6. 그룹화 후 변환
```java
Map<String, List<String>> result = apples.stream()
    .collect(Collectors.groupingBy(
        Apple::getColor,
        Collectors.mapping(Apple::getName, Collectors.toList())
    ));
```

### 7. 파티셔닝
```java
Map<Boolean, List<Apple>> partitioned = apples.stream()
    .collect(Collectors.partitioningBy(
        apple -> apple.getWeight() > 150
    ));
```

### 8. 리듀스
```java
int sum = list.stream()
    .reduce(0, (a, b) -> a + b);

int sum = list.stream()
    .reduce(0, Integer::sum);
```

### 9. 조인
```java
String joined = list.stream()
    .map(Object::toString)
    .collect(Collectors.joining(", "));
```

### 10. Optional 활용
```java
Optional<Apple> heaviest = apples.stream()
    .max(Comparator.comparing(Apple::getWeight));

heaviest.ifPresent(System.out::println);
String name = heaviest.map(Apple::getName).orElse("None");
```

---

## ⚡ 성능 팁

### 1. 박싱 회피
```java
// ❌ 박싱
list.stream().map(x -> x.getValue()).collect(Collectors.toList());

// ✅ 기본형 스트림
list.stream().mapToInt(X::getValue).toArray();
```

### 2. 병렬 스트림 주의
```java
// ✅ 좋은 경우: 계산 집약적, 독립적
list.parallelStream()
    .filter(x -> isPrime(x))  // 각 요소가 독립적
    .collect(Collectors.toList());

// ❌ 나쁜 경우: 순서 중요, 공유 상태
list.parallelStream()
    .sorted()  // 순서 필요 → 병렬 이점 감소
    .collect(Collectors.toList());
```

### 3. 스트림 재사용 불가
```java
// ❌ 오류!
Stream<Apple> stream = list.stream();
stream.filter(a -> a.getWeight() > 150).count();
stream.filter(a -> "green".equals(a.getColor())).count();  // IllegalStateException

// ✅ 매번 새 스트림 생성
list.stream().filter(a -> a.getWeight() > 150).count();
list.stream().filter(a -> "green".equals(a.getColor())).count();
```

---

## 🔍 자주 하는 실수

### 1. 공유 가변 상태
```java
// ❌ 위험!
List<Apple> result = new ArrayList<>();
list.parallelStream()
    .filter(a -> a.getWeight() > 150)
    .forEach(result::add);  // 스레드 안전하지 않음!

// ✅ 안전
List<Apple> result = list.parallelStream()
    .filter(a -> a.getWeight() > 150)
    .collect(Collectors.toList());
```

### 2. 불필요한 박싱
```java
// ❌ 비효율
int sum = list.stream()
    .map(X::getValue)
    .reduce(0, (a, b) -> a + b);

// ✅ 효율적
int sum = list.stream()
    .mapToInt(X::getValue)
    .sum();
```

### 3. Optional 오용
```java
// ❌ Optional의 의미 없음
if (optional.isPresent()) {
    return optional.get();
} else {
    return null;
}

// ✅ Optional 활용
return optional.orElse(null);
```

---

## 📚 빠른 변환표

| 자바 7 | 자바 8 |
|--------|--------|
| `for (Apple a : list) { ... }` | `list.forEach(a -> ...)` |
| `Collections.sort(list, comp)` | `list.sort(comp)` |
| `new ArrayList<>()` + for loop | `list.stream().filter(...).collect(...)` |
| `if (x != null) { ... }` | `Optional.ofNullable(x).ifPresent(...)` |
| 익명 클래스 | 람다 표현식 |

---

## 🎓 학습 순서

```
1. 람다 기본 문법
    ↓
2. 메서드 참조
    ↓
3. 스트림 기본 (filter, map, collect)
    ↓
4. Comparator
    ↓
5. Optional
    ↓
6. 고급 스트림 (reduce, groupingBy)
    ↓
7. 병렬 스트림
    ↓
8. 커스텀 컬렉터
```

---

## 💾 자주 사용하는 코드 스니펫

```java
// 리스트 → 맵
Map<K, V> map = list.stream()
    .collect(Collectors.toMap(X::getKey, X::getValue));

// 리스트 → 세트
Set<T> set = list.stream()
    .collect(Collectors.toSet());

// 조건부 필터링 후 첫 번째
Optional<Apple> first = list.stream()
    .filter(a -> a.getWeight() > 150)
    .findFirst();

// 평균 계산
double avg = list.stream()
    .mapToInt(X::getValue)
    .average()
    .orElse(0.0);

// 문자열 합치기
String result = list.stream()
    .map(X::getName)
    .collect(Collectors.joining(", ", "[", "]"));
```

---

<div align="center">

**💡 핵심 기억하기**

> *스트림 = 데이터 처리 파이프라인*  
> *람다 = 간결한 동작 전달*  
> *병렬 = 한 단어로 멀티코어 활용*

</div>
