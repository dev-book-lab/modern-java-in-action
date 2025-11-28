# Chapter 01. 자바 8, 9, 10, 11: 무슨 일이 일어나고 있는가?

<div align="center">

**"자바 역사상 가장 큰 변화"**

> *"간결한 코드 + 멀티코어 프로세서의 쉬운 활용"*

[📖 Deep Dive](advanced/deep-dive.md) | [💻 Code](code/) | [📋 CheatSheet](advanced/cheatsheet.md)

</div>

---

## 🎯 학습 목표

이 챕터를 마치면 다음을 할 수 있습니다:

- [ ] **자바 8의 3대 핵심 기술**을 이해하고 설명할 수 있다
- [ ] **스트림 API**로 병렬 처리를 간단하게 구현할 수 있다
- [ ] **동작 파라미터화**로 코드 중복을 제거할 수 있다
- [ ] **메서드 참조와 람다**의 차이를 이해하고 적절히 사용할 수 있다
- [ ] **디폴트 메서드**가 왜 필요한지 설명할 수 있다
- [ ] **함수형 프로그래밍**의 핵심 원칙을 이해한다

---

## 📚 자바 8 이전의 문제점

### 코드 중복과 복잡성

```java
// ❌ 자바 8 이전: 조건마다 메서드를 복사-붙여넣기
public static List<Apple> filterGreenApples(List<Apple> inventory) {
    List<Apple> result = new ArrayList<>();
    for (Apple apple : inventory) {
        if ("green".equals(apple.getColor())) {
            result.add(apple);
        }
    }
    return result;
}

public static List<Apple> filterHeavyApples(List<Apple> inventory) {
    List<Apple> result = new ArrayList<>();
    for (Apple apple : inventory) {
        if (apple.getWeight() > 150) {
            result.add(apple);
        }
    }
    return result;
}
// 문제: 코드 중복, DRY 원칙 위반, 유지보수 어려움
```

### 병렬 처리의 어려움

```java
// ❌ 자바 8 이전: 복잡한 멀티스레드 코드
public class ParallelSum {
    private int[] array;
    private int numThreads;
    
    public int sum() throws InterruptedException {
        int size = (int) Math.ceil(array.length * 1.0 / numThreads);
        int[] sums = new int[numThreads];
        Thread[] threads = new Thread[numThreads];
        
        for (int i = 0; i < numThreads; i++) {
            final int start = i * size;
            final int end = (i + 1) * size;
            threads[i] = new Thread(() -> {
                int sum = 0;
                for (int j = start; j < end && j < array.length; j++) {
                    sum += array[j];
                }
                sums[i] = sum;
            });
            threads[i].start();
        }
        
        for (int i = 0; i < numThreads; i++) {
            threads[i].join();
        }
        
        int total = 0;
        for (int s : sums) {
            total += s;
        }
        return total;
    }
}
// 문제: 복잡함, 에러 발생 가능성 높음, synchronized 필요
```

---

## 🚀 자바 8의 3대 핵심 기술

### 1. 스트림 API (Stream API)

#### 개념
**스트림**: 한 번에 한 개씩 만들어지는 연속적인 데이터 항목들의 모임

#### 외부 반복 vs 내부 반복

```java
// ❌ 외부 반복 (External Iteration)
List<Apple> heavyApples = new ArrayList<>();
for (Apple apple : inventory) {
    if (apple.getWeight() > 150) {
        heavyApples.add(apple);
    }
}

// ✅ 내부 반복 (Internal Iteration)
List<Apple> heavyApples = inventory.stream()
    .filter(apple -> apple.getWeight() > 150)
    .collect(Collectors.toList());
```

#### 병렬 처리가 이렇게 쉽다!

```java
// 순차 처리
List<Apple> result = inventory.stream()
    .filter(apple -> apple.getWeight() > 150)
    .collect(Collectors.toList());

// 병렬 처리 (멀티코어 활용)
List<Apple> result = inventory.parallelStream()  // 이 한 줄의 차이!
    .filter(apple -> apple.getWeight() > 150)
    .collect(Collectors.toList());
```

**스트림의 마법:**
1. 라이브러리가 리스트를 자동으로 분할(fork)
2. 각 CPU 코어에서 병렬로 처리
3. 결과를 합침(join)
4. `synchronized` 불필요!

---

### 2. 동작 파라미터화 (Behavior Parameterization)

#### 혁명적 개념: 메서드를 값처럼 전달

**자바 8 이전의 고통:**
```java
// 녹색 사과 필터링
public static List<Apple> filterGreenApples(...) { ... }

// 빨간 사과 필터링
public static List<Apple> filterRedApples(...) { ... }

// 무거운 사과 필터링
public static List<Apple> filterHeavyApples(...) { ... }

// ... 조건마다 메서드가 증가!
```

**자바 8의 해법:**
```java
// 하나의 메서드로 모든 조건 처리!
public static List<Apple> filterApples(List<Apple> inventory, Predicate<Apple> p) {
    List<Apple> result = new ArrayList<>();
    for (Apple apple : inventory) {
        if (p.test(apple)) {  // 조건은 p가 결정
            result.add(apple);
        }
    }
    return result;
}

// 사용
filterApples(inventory, apple -> "green".equals(apple.getColor()));
filterApples(inventory, apple -> apple.getWeight() > 150);
filterApples(inventory, apple -> "red".equals(apple.getColor()) && apple.getWeight() > 150);
```

#### Predicate의 비밀

**Predicate는 함수형 인터페이스:**
```java
@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);  // 유일한 추상 메서드
    
    // default 메서드들
    default Predicate<T> and(Predicate<? super T> other) { ... }
    default Predicate<T> or(Predicate<? super T> other) { ... }
    default Predicate<T> negate() { ... }
}
```

**람다가 Predicate가 되는 과정:**
```java
// 1. 람다 표현식
Predicate<Apple> p = apple -> apple.getWeight() > 150;

// 2. 컴파일러가 내부적으로 변환
Predicate<Apple> p = new Predicate<Apple>() {
    @Override
    public boolean test(Apple apple) {
        return apple.getWeight() > 150;  // 람다 본문이 여기로!
    }
};

// 3. p.test(myApple) 호출 시 위의 test() 메서드 실행!
```

---

### 3. 메서드 참조와 람다

#### 메서드 참조 (::)

**기존: 익명 클래스의 보일러플레이트**
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
// :: = "이 메서드를 값으로 사용하라"
```

#### 람다 표현식

**기본 문법:**
```java
// (파라미터) -> 표현식

apple -> "green".equals(apple.getColor())
apple -> apple.getWeight() > 150
(x, y) -> x + y
() -> System.out.println("Hello")
```

**메서드 참조 vs 람다 선택 가이드:**

```java
// ✅ 간단하고 한 번만 사용 → 람다
filterApples(inventory, apple -> apple.getWeight() > 150);

// ✅ 재사용되거나 의미가 명확해야 함 → 메서드 참조
public static boolean isHeavyApple(Apple apple) {
    return apple.getWeight() > 150;
}
filterApples(inventory, FilteringApples::isHeavyApple);

// ✅ 복잡한 로직 → 메서드로 추출 후 참조
public static boolean isHeavyGreenApple(Apple apple) {
    return apple.getWeight() > 150 && "green".equals(apple.getColor());
}
filterApples(inventory, FilteringApples::isHeavyGreenApple);
```

---

## 💡 일급 시민 (First-Class Citizen)

### 개념

**일급 시민:** 프로그램 실행 중에 자유롭게 전달할 수 있는 값

```
전통적 자바
├─ 일급 시민: int, double, String, 객체
└─ 이급 시민: 메서드, 클래스 (값으로 전달 불가)

자바 8
├─ 일급 시민: int, double, String, 객체, 메서드, 람다
└─ (메서드가 일급 시민으로 승격!)
```

### 메서드가 일급 시민이 되면?

```java
// 변수에 할당 가능
Predicate<Apple> isGreen = FilteringApples::isGreenApple;

// 파라미터로 전달 가능
filterApples(inventory, FilteringApples::isGreenApple);

// 반환값으로 사용 가능
public Predicate<Apple> createPredicate() {
    return FilteringApples::isGreenApple;
}

// Comparator를 변수에 저장
Comparator<Apple> byWeight = Comparator.comparing(Apple::getWeight);
inventory.sort(byWeight);
inventory.sort(byWeight.reversed());
```

---

## 🔧 디폴트 메서드 (Default Method)

### 문제 상황

```java
// List 인터페이스에 sort()를 추가하고 싶다면?
public interface List<E> {
    void sort(Comparator<? super E> c);  // 새 메서드
}

// ❌ 모든 List 구현체가 깨짐!
// ArrayList, LinkedList, 전 세계의 커스텀 List...
```

### 해결책: default 메서드

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

// ✅ 기존 구현체들은 수정 없이 sort() 사용 가능!
```

### 이론 vs 현실

```java
// 이론적으로 올바른 설계
list.sort(comparator);  // 객체지향적!

// 자바 7까지의 현실
Collections.sort(list, comparator);  // 유틸리티 클래스에 의존

// 자바 8 이후
list.sort(comparator);  // 드디어 이론대로!
```

### 와일드카드 이해하기

```java
default void sort(Comparator<? super E> c)
//                          ^^^^^^^^
//                          E의 상위 타입 허용
```

**왜 `? super E`?**
```java
class Fruit { }
class Apple extends Fruit { }

List<Apple> apples = new ArrayList<>();
Comparator<Fruit> fruitComp = ...;

// ✅ Apple 리스트를 Fruit Comparator로 정렬 가능!
apples.sort(fruitComp);  // Apple은 Fruit이므로 안전
```

---

## 🔥 병렬성과 함수형 프로그래밍

### 공유 가변 상태의 위험

```java
// ❌ 위험: 공유 가변 상태
List<Apple> result = new ArrayList<>();
inventory.parallelStream()
    .filter(a -> a.getWeight() > 150)
    .forEach(a -> result.add(a));  // 여러 스레드가 동시 접근!

// 💥 결과:
// - ArrayIndexOutOfBoundsException
// - 누락된 데이터
// - 예측 불가능한 동작
```

### 안전한 병렬 처리

```java
// ✅ 안전: 불변 방식
List<Apple> result = inventory.parallelStream()
    .filter(a -> a.getWeight() > 150)
    .collect(Collectors.toList());  // 스레드 안전!
```

### 함수형 프로그래밍의 핵심 원칙

1. **순수 함수 (Pure Function)**
```java
// ✅ 순수 함수: 같은 입력 → 항상 같은 출력
public static boolean isHeavy(Apple apple) {
    return apple.getWeight() > 150;  // 외부 상태 의존 없음
}

// ❌ 비순수 함수: 외부 상태에 의존
int threshold = 150;
public static boolean isHeavy(Apple apple) {
    return apple.getWeight() > threshold;  // threshold 변경 시 결과 달라짐
}
```

2. **부작용 없음 (No Side Effects)**
```java
// ✅ 부작용 없음
public static int add(int a, int b) {
    return a + b;  // 계산만 수행
}

// ❌ 부작용 있음
public static int addAndLog(int a, int b) {
    System.out.println("Adding...");  // 외부 상태 변경 (출력)
    return a + b;
}
```

3. **불변성 (Immutability)**
```java
// ✅ 불변 방식
List<Apple> filtered = inventory.stream()
    .filter(a -> a.getWeight() > 150)
    .collect(Collectors.toList());  // 새 리스트 생성

// ❌ 가변 방식
inventory.removeIf(a -> a.getWeight() <= 150);  // 원본 수정
```

---

## 🎁 기타 함수형 프로그래밍 아이디어

### Optional - NPE 회피

```java
// ❌ 자바 8 이전
String name = getAppleName();
if (name != null) {
    System.out.println(name.toUpperCase());
}

// ✅ 자바 8: Optional
Optional<String> name = getAppleName();
name.ifPresent(n -> System.out.println(n.toUpperCase()));

// 더 강력한 활용
String result = getAppleName()
    .map(String::toUpperCase)
    .orElse("NO NAME");
```

---

## 📊 자바 8 변화 요약

| 항목 | 자바 7 | 자바 8 | 개선점 |
|------|--------|--------|--------|
| **병렬 처리** | Thread, synchronized 등 복잡 | `parallelStream()` | 한 줄로 병렬화 |
| **코드 재사용** | 복사-붙여넣기 | 동작 파라미터화 | DRY 원칙 준수 |
| **메서드 전달** | 익명 클래스 (장황) | 람다, 메서드 참조 | 간결함 |
| **인터페이스 진화** | 불가능 | default 메서드 | 하위 호환성 |
| **정렬** | `Collections.sort(list)` | `list.sort()` | 객체지향적 |
| **null 처리** | if-null 체크 | Optional | 안전성 |

---

## 💻 실전 가이드

### Before & After

```java
// ❌ 자바 7 스타일
List<Apple> heavyApples = new ArrayList<>();
for (Apple apple : inventory) {
    if (apple.getWeight() > 150) {
        heavyApples.add(apple);
    }
}
Collections.sort(heavyApples, new Comparator<Apple>() {
    public int compare(Apple a1, Apple a2) {
        return Integer.compare(a1.getWeight(), a2.getWeight());
    }
});

// ✅ 자바 8 스타일
List<Apple> heavyApples = inventory.stream()
    .filter(apple -> apple.getWeight() > 150)
    .sorted(Comparator.comparing(Apple::getWeight))
    .collect(Collectors.toList());
```

### 스트림 API 파이프라인

```java
// 데이터 처리 파이프라인
List<String> result = inventory.stream()
    .filter(apple -> "green".equals(apple.getColor()))  // 필터링
    .sorted(Comparator.comparing(Apple::getWeight))      // 정렬
    .map(Apple::toString)                                 // 변환
    .limit(3)                                             // 제한
    .collect(Collectors.toList());                        // 수집

// 병렬 처리
List<String> parallel = inventory.parallelStream()  // 병렬!
    .filter(apple -> "green".equals(apple.getColor()))
    .sorted(Comparator.comparing(Apple::getWeight))
    .map(Apple::toString)
    .limit(3)
    .collect(Collectors.toList());
```

---

## 📂 학습 자료 구조

```
chapter01/
├── README.md                      # 👈 현재 문서
├── code/                          # 실습 코드
│   ├── FilteringApples.java       # 동작 파라미터화
│   ├── MethodReferenceExample.java# 메서드 참조
│   ├── StreamExample.java         # 스트림 API
│   ├── ParallelStreamExample.java # 병렬 스트림
│   └── DefaultMethodExample.java  # 디폴트 메서드
└── advanced/                      # 심화 학습
    ├── deep-dive.md               # 상세 원리 설명
    ├── cheatsheet.md              # 빠른 참조 가이드
    └── qa-sessions.md             # AI와의 Q&A 세션
```

---

## 🎯 핵심 메시지

### 자바 8의 본질

```
자바 8 = 패러다임의 전환

명령형 (How) → 선언형 (What)
순차적 → 병렬적
복잡함 → 간결함
```

### 3대 핵심 기술이 만나는 지점

```
스트림 API
    ↓
동작 파라미터화 (람다/메서드 참조로 동작 전달)
    ↓
병렬 처리 (멀티코어 활용)
    ↓
함수형 프로그래밍 (순수 함수, 불변성)
```

---

## ✅ 학습 체크리스트

### 기본 이해
- [ ] 스트림 API의 개념을 설명할 수 있다
- [ ] 외부 반복과 내부 반복의 차이를 안다
- [ ] 동작 파라미터화가 무엇인지 설명할 수 있다
- [ ] Predicate가 무엇인지 이해한다
- [ ] 람다와 메서드 참조를 구분할 수 있다

### 실전 활용
- [ ] `stream()`과 `parallelStream()`을 사용할 수 있다
- [ ] Predicate로 필터링 로직을 작성할 수 있다
- [ ] 메서드 참조를 적절히 사용할 수 있다
- [ ] Comparator로 정렬을 구현할 수 있다
- [ ] 디폴트 메서드의 필요성을 이해한다

### 심화 이해
- [ ] 함수형 프로그래밍의 3가지 원칙을 설명할 수 있다
- [ ] 공유 가변 상태의 위험성을 이해한다
- [ ] 순수 함수와 부작용의 개념을 안다
- [ ] Optional의 사용법을 안다
- [ ] 일급 시민의 개념을 이해한다

---

## 📖 더 알아보기

- [Deep Dive](advanced/deep-dive.md) - 내부 동작 원리와 설계 철학
- [CheatSheet](advanced/cheatsheet.md) - 빠른 참조 가이드
- [Q&A Sessions](advanced/qa-sessions.md) - AI와의 대화 기록

---

## 🚀 다음 단계

**Chapter 2: 동작 파라미터화**에서는:
- Predicate 패턴을 깊이 있게 학습
- Consumer, Function 등 다른 함수형 인터페이스
- Comparator의 고급 활용
- 전략 패턴과의 관계

**Chapter 3: 람다 표현식**에서는:
- 람다 문법의 모든 것
- 함수형 인터페이스 설계
- 메서드 참조의 4가지 형태
- 타입 추론과 클로저

**Chapter 4-7: 스트림 API**에서는:
- 스트림 연산의 종류
- 중간 연산과 최종 연산
- 병렬 스트림의 성능
- 커스텀 컬렉터

---

<div align="center">

**💡 최종 통찰**

> *"자바 8은 단순한 기능 추가가 아니라  
> 프로그래밍 사고방식의 근본적 변화다."*

**🌟 이 변화를 이해하고 활용하면,  
더 빠르고, 안전하며, 유지보수하기 쉬운 코드를 작성할 수 있습니다.**

</div>
