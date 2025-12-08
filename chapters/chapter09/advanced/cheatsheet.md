# Chapter 09. 리팩터링, 테스팅, 디버깅 - CheatSheet

> 람다와 스트림을 활용한 리팩터링, 테스팅, 디버깅 핵심 요약

---

## 🔄 리팩터링 Quick Reference

### 익명 클래스 → 람다

```java
// Before
Runnable r = new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello");
    }
};

// After
Runnable r = () -> System.out.println("Hello");
```

### 람다 → 메서드 참조

```java
// Before
inventory.sort((a1, a2) -> a1.getWeight().compareTo(a2.getWeight()));

// After
inventory.sort(Comparator.comparing(Apple::getWeight));
```

### 명령형 → 스트림

```java
// Before
List<String> names = new ArrayList<>();
for (Dish dish : menu) {
    if (dish.getCalories() > 300) {
        names.add(dish.getName());
    }
}

// After
List<String> names = menu.stream()
    .filter(d -> d.getCalories() > 300)
    .map(Dish::getName)
    .collect(toList());
```

---

## ⚠️ 리팩터링 주의사항

### this 차이

```java
// 익명 클래스: this = 익명 클래스 자신
new Runnable() {
    public void run() {
        System.out.println(this);  // 익명 클래스
    }
}

// 람다: this = 감싸는 클래스
() -> System.out.println(this);  // 외부 클래스
```

### 섀도잉

```java
int a = 10;

// ✅ 익명 클래스: 섀도잉 가능
new Runnable() {
    public void run() {
        int a = 20;  // OK
    }
}

// ❌ 람다: 섀도잉 불가
() -> {
    int a = 20;  // 컴파일 에러
}
```

### 오버로딩 모호함

```java
doSomething(new Task() { ... });  // ✅ OK
doSomething(() -> ...);           // ❌ 모호함
doSomething((Task)() -> ...);     // ✅ 명시적 캐스팅
```

---

## 🎨 디자인 패턴 리팩터링

### 전략 패턴

```java
// Before
new Validator(new IsNumeric())

// After
new Validator(s -> s.matches("\\d+"))
```

### 템플릿 메서드

```java
// Before
abstract class OnlineBanking {
    public void processCustomer(int id) {
        Customer c = Database.getCustomerWithId(id);
        makeCustomerHappy(c);
    }
    abstract void makeCustomerHappy(Customer c);
}

// After
public void processCustomer(int id, Consumer<Customer> makeCustomerHappy) {
    Customer c = Database.getCustomerWithId(id);
    makeCustomerHappy.accept(c);
}
```

### 옵저버 패턴

```java
// Before
feed.registerObserver(new NYTimes());

// After
feed.registerObserver(tweet -> {
    if (tweet.contains("money")) {
        System.out.println("Breaking news!");
    }
});
```

### 의무 체인

```java
// Before
p1.setSuccessor(p2);
String result = p1.handle(text);

// After
UnaryOperator<String> p1 = text -> "Header: " + text;
UnaryOperator<String> p2 = text -> text.replaceAll("labda", "lambda");
Function<String, String> pipeline = p1.andThen(p2);
String result = pipeline.apply(text);
```

### 팩토리 패턴

```java
// Before
switch (name) {
    case "loan": return new Loan();
    case "stock": return new Stock();
}

// After
Map<String, Supplier<Product>> map = Map.of(
    "loan", Loan::new,
    "stock", Stock::new
);
Product p = map.get(name).get();
```

---

## 🧪 테스팅 Quick Reference

### 1. public 필드 테스트

```java
// 코드
public class Point {
    public static final Comparator<Point> compareByXAndThenY = 
        Comparator.comparing(Point::getX).thenComparing(Point::getY);
}

// 테스트
@Test
public void test() {
    int result = Point.compareByXAndThenY.compare(p1, p2);
    assertTrue(result < 0);
}
```

### 2. 메서드 동작 테스트

```java
// 코드
public static List<Point> moveAllPointsRightBy(List<Point> points, int x) {
    return points.stream()
        .map(p -> new Point(p.getX() + x, p.getY()))
        .collect(toList());
}

// 테스트
@Test
public void test() {
    List<Point> result = Point.moveAllPointsRightBy(points, 10);
    assertEquals(expected, result);
}
```

### 3. 복잡한 람다 → 메서드 분리

```java
// Before
list.stream().filter(s -> { /* 복잡한 로직 */ })

// After
list.stream().filter(MyClass::isValid)

@Test
public void testIsValid() {
    assertTrue(MyClass.isValid("test"));
}
```

### 4. 고차원 함수 테스트

```java
// 함수를 인수로 받는 경우
@Test
public void testFilter() {
    List<Integer> even = filter(numbers, i -> i % 2 == 0);
    assertEquals(Arrays.asList(2, 4), even);
}

// 함수를 반환하는 경우
@Test
public void testGreaterThan() {
    Predicate<Integer> gt5 = greaterThan(5);
    assertTrue(gt5.test(6));
    assertFalse(gt5.test(3));
}
```

---

## 🐛 디버깅 Quick Reference

### 람다 스택 트레이스

```
Exception in thread "main" java.lang.NullPointerException
    at Debugging.lambda$main$0(Debugging.java:6)
                 ↑      ↑    ↑
                 |      |    └─ 첫 번째 람다 (0부터 시작)
                 |      └────── 메서드 이름
                 └───────────── 람다 표시
```

**이름 패턴:**
- `lambda$main$0` - main 메서드의 첫 번째 람다
- `lambda$main$1` - main 메서드의 두 번째 람다
- `lambda$process$0` - process 메서드의 첫 번째 람다

### 메서드 참조 스택 트레이스

```java
// 같은 클래스 메서드 참조
numbers.stream()
    .map(Debugging::divideByZero)  // ✅ 메서드 이름 표시
    .forEach(System.out::println);

// 스택 트레이스
Exception in thread "main" java.lang.ArithmeticException
    at Debugging.divideByZero(Debugging.java:10)
                 ↑ 명확한 메서드 이름!
```

### peek로 중간 값 확인

```java
List<Integer> result = numbers.stream()
    .peek(x -> System.out.println("from stream: " + x))
    .map(x -> x + 17)
    .peek(x -> System.out.println("after map: " + x))
    .filter(x -> x % 2 == 0)
    .peek(x -> System.out.println("after filter: " + x))
    .limit(3)
    .peek(x -> System.out.println("after limit: " + x))
    .collect(toList());
```

**출력:**
```
from stream: 2
after map: 19
from stream: 3
after map: 20
after filter: 20
after limit: 20
...
```

---

## 📊 패턴별 선택 가이드

### 언제 람다로 리팩터링?

| 상황 | 람다 | 클래스 |
|------|------|--------|
| 간단한 익명 클래스 (1-5줄) | ✅ | ❌ |
| 복잡한 로직 (10줄+) | ❌ | ✅ |
| 상태가 있는 옵저버 | ❌ | ✅ |
| 일회성 전략 | ✅ | ❌ |
| 재사용 필요 | △ | ✅ |

### 테스팅 전략

| 람다 위치 | 테스트 방법 |
|-----------|------------|
| public 필드 | 직접 테스트 |
| private 내부 | 메서드 동작 테스트 |
| 복잡한 람다 | 메서드로 분리 |
| 고차원 함수 | 다양한 람다로 검증 |

### 디버깅 전략

| 문제 | 해결 |
|------|------|
| 익명 람다 | 메서드 참조로 변경 |
| 중간 값 불명 | peek 사용 |
| 복잡한 체인 | 단계별 분리 |
| 예외 위치 불명 | 람다 → 메서드 분리 |

---

## 💡 핵심 원칙

### 리팩터링

```
✅ 간단하면 람다
✅ 의도가 명확하면 메서드 참조
✅ 반복이면 스트림
❌ 복잡하면 클래스 유지
```

### 테스팅

```
✅ 메서드의 동작 테스트 (람다 자체 X)
✅ public이면 직접 테스트
✅ 복잡하면 메서드 분리
✅ 고차원 함수는 다양한 입력으로 검증
```

### 디버깅

```
✅ 메서드 참조 > 람다 (스택 트레이스)
✅ peek로 중간 확인
✅ 단계별로 분리
✅ 람다 번호 패턴 이해 (lambda$메서드$번호)
```

---

## 🔥 자주 하는 실수

### 1. this 오해

```java
❌ 람다에서 this는 람다가 아님!
✅ 람다의 this는 감싸는 클래스
```

### 2. 섀도잉 시도

```java
int a = 10;
❌ () -> { int a = 20; }  // 컴파일 에러
✅ 다른 변수명 사용
```

### 3. 오버로딩 모호함

```java
❌ doSomething(() -> ...)  // 어떤 타입?
✅ doSomething((Task)() -> ...)  // 명시
```

### 4. 람다 직접 테스트

```java
❌ 내부 람다를 직접 테스트하려 시도
✅ 람다를 사용하는 메서드 테스트
```

### 5. forEach + 수정

```java
❌ list.forEach(e -> list.remove(e));  // ConcurrentModificationException
✅ list.removeIf(condition);
```

### 6. peek = forEach

```java
❌ peek는 최종 연산 아님
✅ peek는 중간 연산 (디버깅용)
```

---

## 📋 체크리스트

### 리팩터링 전

```
□ 익명 클래스가 3줄 이하?
□ 함수형 인터페이스?
□ this/섀도잉 사용 안 함?
□ 오버로딩 없음?
```

### 테스트 작성 시

```
□ 람다 자체가 아닌 메서드 테스트?
□ public 필드면 직접 테스트?
□ 복잡하면 메서드 분리?
□ 다양한 입력으로 검증?
```

### 디버깅 시

```
□ 스택 트레이스 람다 번호 확인?
□ 메서드 참조로 변경 가능?
□ peek로 중간 값 확인?
□ 단계별 분리 고려?
```

---

## 🎯 빠른 참조

### 리팩터링 순서

```
1. 익명 클래스 → 람다
2. 람다 → 메서드 참조
3. 명령형 → 스트림
4. 조건부 평가 → Supplier
5. 중복 제거 → 실행 어라운드
```

### 디자인 패턴 순서

```
1. 전략/옵저버 → 람다 (간단)
2. 템플릿 메서드 → Consumer
3. 의무 체인 → Function.andThen
4. 팩토리 → Map + Supplier
```

### 테스팅 순서

```
1. public 필드 확인
2. 메서드 동작 테스트
3. 복잡하면 메서드 분리
4. 고차원 함수는 다양한 입력
```

### 디버깅 순서

```
1. 스택 트레이스 확인
2. 람다 번호 파악
3. peek로 중간 값 확인
4. 메서드 참조로 변경 고려
```

---

## 📚 추가 자료

- [📖 Deep Dive](deep-dive.md) - 내부 메커니즘 상세 분석
- [💬 Q&A](qa-sessions.md) - 자주 묻는 질문과 답변

---

**작성일**: 2024년 12월  
**대상**: Modern Java In Action Chapter 9
