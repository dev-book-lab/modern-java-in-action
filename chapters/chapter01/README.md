# Modern Java in Action - Chapter 01 정리

## 📌 Chapter 1: 자바 8, 9, 10, 11 - 무슨 일이 일어나고 있는가?

---

## 1. 자바 8의 핵심 변화

### 1.1 역사적 의미
- **자바 역사상 가장 큰 변화**가 자바 8에서 발생
- 두 가지 핵심 요구사항:
    1. **간결한 코드**
    2. **멀티코어 프로세서의 쉬운 활용**

### 1.2 자바 8 이전의 문제점
```java
// 자바 8 이전: CPU 코어 하나만 사용
// 나머지 코어를 사용하려면 복잡한 멀티스레드 코드 작성 필요
for (Apple apple : inventory) {
    if ("green".equals(apple.getColor())) {
        result.add(apple);
    }
}
// 병렬 처리를 위해서는 synchronized, 스레드 관리 등 복잡한 코드 필요
```

---

## 2. 자바 8의 3대 핵심 기술

### 2.1 스트림 API (Stream API)

#### 개념
- **스트림**: 한 번에 한 개씩 만들어지는 연속적인 데이터 항목들의 모임
- `Stream<T>`: T 형식으로 구성된 일련의 항목

#### 외부 반복 vs 내부 반복
```java
// 외부 반복 (External Iteration) - 자바 8 이전
List<Apple> heavyApples = new ArrayList<>();
for (Apple apple : inventory) {
    if (apple.getWeight() > 150) {
        heavyApples.add(apple);
    }
}

// 내부 반복 (Internal Iteration) - 자바 8 이후
List<Apple> heavyApples = inventory.stream()
    .filter(apple -> apple.getWeight() > 150)
    .collect(Collectors.toList());
```

#### 병렬 처리
```java
// 순차 처리
inventory.stream()
    .filter(a -> a.getWeight() > 150)
    .collect(Collectors.toList());

// 병렬 처리 (멀티코어 활용)
inventory.parallelStream()
    .filter(a -> a.getWeight() > 150)
    .collect(Collectors.toList());
```

**스트림의 장점:**
- 라이브러리가 내부적으로 리스트를 나눠서 여러 CPU로 분산(fork)
- 각 CPU에서 처리 후 결과를 합침(join)
- synchronized 없이 안전한 병렬 처리

---

### 2.2 동작 파라미터화 (Behavior Parameterization)

#### 메서드를 다른 메서드의 인수로 전달

**자바 8 이전: 코드 중복**
```java
// 녹색 사과 필터링
public static List<Apple> filterGreenApples(List<Apple> inventory) {
    List<Apple> result = new ArrayList<>();
    for (Apple apple : inventory) {
        if ("green".equals(apple.getColor())) {
            result.add(apple);
        }
    }
    return result;
}

// 무거운 사과 필터링
public static List<Apple> filterHeavyApples(List<Apple> inventory) {
    List<Apple> result = new ArrayList<>();
    for (Apple apple : inventory) {
        if (apple.getWeight() > 150) {
            result.add(apple);
        }
    }
    return result;
}
// 문제: 복사-붙여넣기, 유지보수 어려움
```

**자바 8: 동작 파라미터화**
```java
// 조건을 메서드로 정의
public static boolean isGreenApple(Apple apple) {
    return "green".equals(apple.getColor());
}

public static boolean isHeavyApple(Apple apple) {
    return apple.getWeight() > 150;
}

// 하나의 메서드로 모든 조건 처리
public static List<Apple> filterApples(List<Apple> inventory, Predicate<Apple> p) {
    List<Apple> result = new ArrayList<>();
    for (Apple apple : inventory) {
        if (p.test(apple)) {  // 조건 검사
            result.add(apple);
        }
    }
    return result;
}

// 사용
List<Apple> greenApples = filterApples(inventory, FilteringApples::isGreenApple);
List<Apple> heavyApples = filterApples(inventory, FilteringApples::isHeavyApple);
```

#### Predicate란?

**개념:**
- 수학: 인수로 값을 받아 true/false를 반환하는 함수
- 자바 8: `Predicate<T>` 함수형 인터페이스

**구조:**
```java
@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);  // 추상 메서드
    
    // default 메서드들
    default Predicate<T> and(Predicate<? super T> other) { ... }
    default Predicate<T> or(Predicate<? super T> other) { ... }
    default Predicate<T> negate() { ... }
}
```

**동작 원리:**
```java
// 람다 표현식
Predicate<Apple> p = (Apple a) -> a.getWeight() > 150;

// 컴파일러가 내부적으로 변환
Predicate<Apple> p = new Predicate<Apple>() {
    @Override
    public boolean test(Apple a) {
        return a.getWeight() > 150;  // 람다 본문이 구현부가 됨!
    }
};

// p.test(apple) 호출 시 위의 test() 메서드가 실행됨
```

---

### 2.3 메서드 참조와 람다

#### 2.3.1 메서드 참조 (Method Reference)

**기존 방식: 익명 클래스**
```java
File[] hiddenFiles = new File(".").listFiles(new FileFilter() {
    public boolean accept(File file) {
        return file.isHidden();
    }
});
```

**자바 8: 메서드 참조**
```java
File[] hiddenFiles = new File(".").listFiles(File::isHidden);
// :: 연산자: "이 메서드를 값으로 사용하라"
```

#### 2.3.2 람다 (Lambda)

**기본 문법:**
```java
// 형식: (파라미터) -> 표현식
(Apple a) -> "green".equals(a.getColor())
(Apple a) -> a.getWeight() > 150
(int x) -> x + 1
```

**사용 예시:**
```java
// 한 번만 사용할 조건은 람다로 간단히
filterApples(inventory, (Apple a) -> "green".equals(a.getColor()));
filterApples(inventory, (Apple a) -> a.getWeight() > 150);
filterApples(inventory, (Apple a) -> a.getWeight() < 80 || "brown".equals(a.getColor()));
```

**언제 메서드 참조 vs 람다?**
- **람다**: 간단하고 한두 번만 사용하는 경우
- **메서드 참조**: 재사용되거나, 복잡하거나, 의미를 명확히 표현해야 하는 경우
```java
// 복잡한 람다 → 메서드 참조로 개선
filterApples(inventory, (Apple a) -> {
        return a.getWeight() > 150 &&
        "green".equals(a.getColor());
        });

// ↓ 개선

public static boolean isHeavyGreenApple(Apple apple) {
    return apple.getWeight() > 150 &&
            "green".equals(apple.getColor());
}

filterApples(inventory, FilteringApples::isHeavyGreenApple);
```

---

## 3. 일급 시민 (First-Class Citizen)

### 3.1 개념
- **일급 시민**: 프로그램 실행 중에 자유롭게 전달할 수 있는 값
- 전통적으로 자바에서는 기본값(int, double)과 객체만 일급 시민

### 3.2 자바 8의 변화
```java
// 자바 8 이전: 메서드는 이급 시민
// - 값으로 전달 불가
// - 변수에 할당 불가

// 자바 8 이후: 메서드와 람다가 일급 시민
// - 메서드를 값으로 전달 가능
Predicate<Apple> p = FilteringApples::isGreenApple;

// - 변수에 할당 가능
Comparator<Apple> byWeight = Comparator.comparing(Apple::getWeight);

// - 메서드의 인수로 전달 가능
inventory.sort(Comparator.comparing(Apple::getWeight));
```

---

## 4. 디폴트 메서드 (Default Method)

### 4.1 왜 필요한가?

**문제 상황:**
```java
// List 인터페이스에 sort()를 추가하고 싶다면?
public interface List<E> {
    void sort(Comparator<? super E> c);  // 새 메서드 추가
}

// ❌ 모든 List 구현체가 깨짐!
public class ArrayList<E> implements List<E> {
    // sort()를 구현하지 않으면 컴파일 에러!
}

// 전 세계의 모든 커스텀 List 구현체도 깨짐
```

**해결책: default 메서드**
```java
public interface List<E> {
    // default 메서드: 인터페이스에 구현이 있음!
    default void sort(Comparator<? super E> c) {
        Object[] a = this.toArray();
        Arrays.sort(a, (Comparator) c);
        ListIterator<E> i = this.listIterator();
        for (Object e : a) {
            i.next();
            i.set((E) e);
        }
    }
}

// ✅ 기존 구현체들은 아무 수정 없이도 sort() 사용 가능!
```

### 4.2 이론 vs 현실

**이론적으로 올바른 설계:**
```java
list.sort(comparator);  // 리스트가 자기 자신을 정렬
list.add(element);
list.remove(index);
// 모든 동작이 list 객체가 수행 → 객체지향적
```

**자바 7까지의 현실:**
```java
Collections.sort(list, comparator);  // 외부 유틸리티 클래스 사용
list.add(element);
list.remove(index);
// 정렬만 외부에 의존 → 비객체지향적
```

**자바 8 이후:**
```java
list.sort(comparator);  // 드디어 이론대로!
list.add(element);
list.remove(index);
```

### 4.3 와일드카드 이해하기
```java
default void sort(Comparator<? super E> c) { ... }
//                          ^^^^^^^^
//                          상위 타입 허용
```

**왜 `? super E`인가?**
```java
class Fruit { }
class Apple extends Fruit { }

// Apple 리스트
List<Apple> apples = new ArrayList<>();

// Fruit Comparator (상위 타입)
Comparator<Fruit> fruitComparator = ...;

// ✅ Apple 리스트를 Fruit Comparator로 정렬 가능!
apples.sort(fruitComparator);
// Apple은 Fruit이므로 안전!
```

**제네릭 와일드카드 정리:**
```java
// 1. <T> - 정확히 T 타입
List<Apple> apples;

// 2. <? extends T> - T 또는 T의 하위 타입 (상한 제한)
List<? extends Fruit> fruits;  // Apple, Orange 등 가능

// 3. <? super T> - T 또는 T의 상위 타입 (하한 제한)
Comparator<? super Apple> comp;  // Apple, Fruit, Object 가능
```

---

## 5. 병렬성과 함수형 프로그래밍

### 5.1 공유 가변 상태의 문제
```java
// 위험한 코드: 공유 가변 상태
List<Apple> result = new ArrayList<>();
inventory.parallelStream()
    .filter(a -> a.getWeight() > 150)
    .forEach(a -> result.add(a));  // ❌ 여러 스레드가 동시에 접근!
```

### 5.2 안전한 병렬 처리
```java
// 안전한 코드: 불변 방식
List<Apple> result = inventory.parallelStream()
    .filter(a -> a.getWeight() > 150)
    .collect(Collectors.toList());  // ✅ 스레드 안전
```

### 5.3 함수형 프로그래밍의 핵심

1. **함수를 일급값으로 사용**
2. **공유 가변 상태 없음**
    - 메서드가 부작용(side-effect) 없음
    - 같은 입력에 항상 같은 출력
    - 다른 메서드/스레드와 상호작용 없음
```java
// 함수형: 부작용 없음
public static boolean isHeavy(Apple apple) {
    return apple.getWeight() > 150;  // 입력만 보고 판단
}

// 비함수형: 부작용 있음
int threshold = 150;
public static boolean isHeavy(Apple apple) {
    return apple.getWeight() > threshold;  // 외부 상태에 의존
}
```

---

## 6. 기타 함수형 프로그래밍 아이디어

### 6.1 Optional - NPE 회피
```java
// 자바 8 이전
String name = getAppleName();
if (name != null) {
    System.out.println(name.toUpperCase());
}

// 자바 8: Optional
Optional<String> name = getAppleName();
name.ifPresent(n -> System.out.println(n.toUpperCase()));
```

### 6.2 패턴 매칭 (제안 단계)
- switch를 확장한 형태
- 데이터 형식 분류와 분석을 동시에 수행
- 자바 8에서는 완벽히 제공하지 않음

---

## 7. 핵심 정리

### 자바 8의 변화 요약

| 항목 | 자바 8 이전 | 자바 8 이후 |
|------|------------|------------|
| **병렬 처리** | 복잡한 멀티스레드 코드 | `parallelStream()` 한 줄 |
| **코드 재사용** | 복사-붙여넣기 | 동작 파라미터화 |
| **메서드 전달** | 익명 클래스 (장황함) | 람다, 메서드 참조 (간결함) |
| **인터페이스 진화** | 불가능 (구현체 모두 수정) | default 메서드로 가능 |
| **정렬** | `Collections.sort(list)` | `list.sort()` |

### 함수형 프로그래밍의 핵심

1. **일급 함수**: 메서드와 람다를 값처럼 사용
2. **불변성**: 공유 가변 상태 없음
3. **병렬성**: 안전한 병렬 처리
4. **간결성**: 더 적은 코드로 더 많은 표현

### 실무 적용 가이드
```java
// ❌ 피해야 할 패턴
for (Apple apple : inventory) {
    if (apple.getWeight() > 150) {
        result.add(apple);
    }
}

// ✅ 권장 패턴
List<Apple> result = inventory.stream()
    .filter(apple -> apple.getWeight() > 150)
    .collect(Collectors.toList());

// ✅ 병렬 처리가 필요한 경우
List<Apple> result = inventory.parallelStream()
    .filter(apple -> apple.getWeight() > 150)
    .collect(Collectors.toList());
```

---

## 8. 다음 단계

- **Chapter 2**: 동작 파라미터화 자세히
- **Chapter 3**: 람다 표현식
- **Chapter 4-7**: 스트림 API
- **Chapter 9**: 디폴트 메서드
- **Chapter 18-19**: 함수형 프로그래밍

---

**🎯 Chapter 1 핵심 메시지**

자바 8은 단순한 기능 추가가 아니라 **프로그래밍 패러다임의 전환**입니다.
- 명령형 → 선언형
- 순차적 → 병렬적
- 복잡함 → 간결함

이 변화를 이해하고 활용하면, 더 빠르고 안전하며 유지보수하기 쉬운 코드를 작성할 수 있습니다.
