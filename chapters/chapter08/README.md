<div align="center">

# Chapter 08. 컬렉션 API 개선

**"Java 8+의 강력한 컬렉션 API를 활용한 효율적인 데이터 처리"**

> *컬렉션 팩토리, 처리 메서드, Map 고급 연산, ConcurrentHashMap을 마스터하여 모던 자바 컬렉션 활용*

[📖 Deep Dive](advanced/deep-dive.md) | [💻 Code](code/) | [📋 CheatSheet](advanced/cheatsheet.md) | [💬 Q&A](advanced/qa-sessions.md)

</div>

---

## 📚 목차

1. [컬렉션 팩토리](#1-컬렉션-팩토리)
2. [리스트와 집합 처리](#2-리스트와-집합-처리)
3. [맵 처리](#3-맵-처리)
4. [개선된 ConcurrentHashMap](#4-개선된-concurrenthashmap)
5. [핵심 정리](#5-핵심-정리)

---

## 1. 컬렉션 팩토리

### 1.1 왜 필요한가?

**기존 방식의 문제점:**

```java
// ❌ 장황한 코드
List<String> friends = new ArrayList<>();
friends.add("Raphael");
friends.add("Olivia");
friends.add("Thibaut");

// ❌ Arrays.asList - 고정 크기
List<String> friends = Arrays.asList("Raphael", "Olivia");
friends.set(0, "Richard");  // ✅ 가능
friends.add("Thibaut");     // ❌ UnsupportedOperationException

// ❌ Set 생성이 복잡
Set<String> friends = new HashSet<>(Arrays.asList("Raphael", "Olivia"));
```

---

### 1.2 리스트 팩토리

```java
// ✅ List.of - 불변 리스트 생성
List<String> friends = List.of("Raphael", "Olivia", "Thibaut");

// 특징
friends.get(0);           // ✅ 읽기 가능
friends.set(0, "Richard"); // ❌ UnsupportedOperationException
friends.add("Chih-Chun");  // ❌ UnsupportedOperationException
```

**장점:**
- ✅ **간결**: 한 줄로 생성
- ✅ **안전**: 불변 → 의도치 않은 수정 방지
- ✅ **null 불허**: NullPointerException 즉시 발생
- ✅ **효율적**: 내부 최적화 (작은 리스트 전용 클래스)

---

### 1.3 집합 팩토리

```java
// ✅ Set.of - 불변 집합 생성
Set<String> friends = Set.of("Raphael", "Olivia", "Thibaut");

// ❌ 중복 불허
Set<String> invalid = Set.of("Raphael", "Olivia", "Olivia");
// IllegalArgumentException: duplicate element
```

**특징:**
- 중복 체크 즉시 수행
- 순서 보장 안 됨
- null 불허

---

### 1.4 맵 팩토리

#### Map.of() - 10개 이하

```java
// ✅ Map.of - 키-값 쌍 나열
Map<String, Integer> ageOfFriends = Map.of(
    "Raphael", 30,
    "Olivia", 25,
    "Thibaut", 26
);
```

#### Map.ofEntries() - 10개 초과

```java
import static java.util.Map.entry;

// ✅ Map.ofEntries - 엔트리 사용
Map<String, Integer> ageOfFriends = Map.ofEntries(
    entry("Raphael", 30),
    entry("Olivia", 25),
    entry("Thibaut", 26),
    entry("Alice", 28),
    entry("Bob", 32)
);
```

---

### 1.5 오버로딩 vs 가변 인수

**내부 구현:**

```java
// 0~10개: 전용 메서드 (배열 할당 없음)
static <E> List<E> of(E e1)
static <E> List<E> of(E e1, E e2)
static <E> List<E> of(E e1, E e2, E e3)
// ...
static <E> List<E> of(E e1, ..., E e10)

// 11개 이상: 가변 인수 (배열 할당)
static <E> List<E> of(E... elements)
```

**이유:**
- 가변 인수 → 배열 할당 → 가비지 컬렉션 비용
- 10개 이하는 최적화

---

## 2. 리스트와 집합 처리

### 2.1 removeIf - 조건부 제거

#### ❌ 기존 방식의 문제

```java
List<Transaction> transactions = new ArrayList<>();

// ❌ ConcurrentModificationException 발생!
for (Transaction t : transactions) {
    if (Character.isDigit(t.getReferenceCode().charAt(0))) {
        transactions.remove(t);  // Iterator와 List 불일치!
    }
}

// ✅ 해결 1: Iterator 사용 (복잡)
for (Iterator<Transaction> it = transactions.iterator(); it.hasNext();) {
    Transaction t = it.next();
    if (Character.isDigit(t.getReferenceCode().charAt(0))) {
        it.remove();
    }
}
```

#### ✅ removeIf 사용

```java
// ✅ removeIf - 간결하고 안전
transactions.removeIf(t -> 
    Character.isDigit(t.getReferenceCode().charAt(0))
);
```

**장점:**
- ✅ ConcurrentModificationException 없음
- ✅ 간결한 코드
- ✅ 내부 최적화 (효율적)

---

### 2.2 replaceAll - 요소 변환

#### ❌ 기존 방식

```java
List<String> codes = Arrays.asList("a12", "C14", "b13");

// ❌ Stream - 새 리스트 생성
List<String> upperCodes = codes.stream()
    .map(code -> Character.toUpperCase(code.charAt(0)) + code.substring(1))
    .collect(Collectors.toList());
// 원본 codes는 변경 안 됨!

// ✅ Iterator - 복잡
for (ListIterator<String> it = codes.listIterator(); it.hasNext();) {
    String code = it.next();
    it.set(Character.toUpperCase(code.charAt(0)) + code.substring(1));
}
```

#### ✅ replaceAll 사용

```java
// ✅ replaceAll - 원본 변경
codes.replaceAll(code -> 
    Character.toUpperCase(code.charAt(0)) + code.substring(1)
);

System.out.println(codes);  // [A12, C14, B13]
```

**특징:**
- UnaryOperator<E> 받음
- 원본 리스트 직접 변경
- List 전용 (Set은 없음)

---

### 2.3 sort - 리스트 정렬

```java
List<String> names = Arrays.asList("Charlie", "Alice", "Bob");

// ✅ sort 메서드
names.sort(Comparator.naturalOrder());
System.out.println(names);  // [Alice, Bob, Charlie]

// 역순
names.sort(Comparator.reverseOrder());
System.out.println(names);  // [Charlie, Bob, Alice]
```

**비교:**

```java
// Collections.sort (정적 메서드)
Collections.sort(names);

// List.sort (인스턴스 메서드)
names.sort(null);  // 자연 순서
```

---

## 3. 맵 처리

### 3.1 forEach - 맵 반복

#### 기존 방식

```java
Map<String, Integer> ageOfFriends = Map.of(
    "Raphael", 30,
    "Olivia", 25,
    "Thibaut", 26
);

// ❌ 장황한 반복
for (Map.Entry<String, Integer> entry : ageOfFriends.entrySet()) {
    String friend = entry.getKey();
    Integer age = entry.getValue();
    System.out.println(friend + " is " + age + " years old");
}
```

#### forEach 사용

```java
// ✅ forEach - 간결
ageOfFriends.forEach((friend, age) -> 
    System.out.println(friend + " is " + age + " years old")
);
```

---

### 3.2 정렬 메서드

```java
Map<String, String> favouriteMovies = Map.ofEntries(
    entry("Raphael", "Star Wars"),
    entry("Cristina", "Matrix"),
    entry("Olivia", "James Bond")
);

// ✅ 키 기준 정렬
favouriteMovies.entrySet()
    .stream()
    .sorted(Map.Entry.comparingByKey())
    .forEachOrdered(System.out::println);

// ✅ 값 기준 정렬
favouriteMovies.entrySet()
    .stream()
    .sorted(Map.Entry.comparingByValue())
    .forEachOrdered(System.out::println);
```

---

### 3.3 getOrDefault - 기본값

```java
Map<String, String> favouriteMovies = Map.of(
    "Raphael", "Star Wars",
    "Olivia", "James Bond"
);

// ✅ 키 있음
System.out.println(
    favouriteMovies.getOrDefault("Olivia", "Matrix")
);  // James Bond

// ✅ 키 없음 → 기본값
System.out.println(
    favouriteMovies.getOrDefault("Thibaut", "Matrix")
);  // Matrix
```

---

### 3.4 계산 패턴

#### computeIfAbsent - 키 없으면 계산

```java
Map<String, List<String>> friendsToMovies = new HashMap<>();

// ❌ 기존 방식 (장황)
String friend = "Raphael";
List<String> movies = friendsToMovies.get(friend);
if (movies == null) {
    movies = new ArrayList<>();
    friendsToMovies.put(friend, movies);
}
movies.add("Star Wars");

// ✅ computeIfAbsent (간결)
friendsToMovies.computeIfAbsent("Raphael", name -> new ArrayList<>())
    .add("Star Wars");
```

**캐시 구현 예제:**

```java
Map<String, byte[]> dataToHash = new HashMap<>();
MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

// 캐시: 키 없으면 계산, 있으면 재사용
lines.forEach(line -> 
    dataToHash.computeIfAbsent(line, this::calculateDigest)
);

private byte[] calculateDigest(String key) {
    return messageDigest.digest(key.getBytes(StandardCharsets.UTF_8));
}
```

#### computeIfPresent - 키 있으면 계산

```java
Map<String, Integer> scores = new HashMap<>();
scores.put("Alice", 100);

// 보너스 10% 추가 (키 있을 때만)
scores.computeIfPresent("Alice", (key, value) -> value + value / 10);
System.out.println(scores);  // {Alice=110}

// 키 없으면 무시
scores.computeIfPresent("Bob", (key, value) -> value + 10);
System.out.println(scores);  // {Alice=110} (변화 없음)
```

#### compute - 항상 계산

```java
Map<String, Integer> map = new HashMap<>();
map.put("Alice", 25);

// 키 유무 관계없이 계산
map.compute("Alice", (k, v) -> v == null ? 1 : v + 1);  // 26
map.compute("Bob", (k, v) -> v == null ? 1 : v + 1);    // 1
```

---

### 3.5 삭제 패턴

#### remove(key, value) - 조건부 삭제

```java
Map<String, String> favouriteMovies = new HashMap<>();
favouriteMovies.put("Raphael", "Jack Reacher 2");
favouriteMovies.put("Cristina", "Matrix");

// ❌ 기존 방식 (Race Condition 위험)
String key = "Raphael";
String value = "Jack Reacher 2";
if (favouriteMovies.containsKey(key) && 
    Objects.equals(favouriteMovies.get(key), value)) {
    favouriteMovies.remove(key);
}

// ✅ remove(key, value) - 원자적 연산
favouriteMovies.remove(key, value);  // 키-값 모두 일치해야 제거
```

---

### 3.6 교체 패턴

#### replace - 키 있으면 교체

```java
Map<String, Integer> map = new HashMap<>();
map.put("Alice", 25);

// ✅ 키 있으면 교체
Integer oldValue = map.replace("Alice", 26);
System.out.println(oldValue);  // 25
System.out.println(map);       // {Alice=26}

// ✅ 키 없으면 무시
Integer result = map.replace("Bob", 30);
System.out.println(result);  // null
System.out.println(map);     // {Alice=26} (변화 없음)
```

#### replace(key, oldValue, newValue) - CAS

```java
// ✅ 키-값 모두 일치해야 교체 (원자적)
boolean success = map.replace("Alice", 26, 27);
System.out.println(success);  // true

boolean fail = map.replace("Alice", 99, 100);
System.out.println(fail);  // false (값 불일치)
```

#### replaceAll - 모든 값 변환

```java
Map<String, String> data = new HashMap<>();
data.put("name", "alice");
data.put("city", "seoul");

// 모든 값을 대문자로
data.replaceAll((key, value) -> value.toUpperCase());
System.out.println(data);  // {name=ALICE, city=SEOUL}
```

---

### 3.7 합침 패턴

#### putAll - 단순 병합

```java
Map<String, String> family = Map.ofEntries(
    entry("Teo", "Star Wars"),
    entry("Cristina", "James Bond")
);
Map<String, String> friends = Map.ofEntries(
    entry("Raphael", "Star Wars")
);

// ✅ putAll - 중복 키는 덮어씀
Map<String, String> everyone = new HashMap<>(family);
everyone.putAll(friends);
System.out.println(everyone);
```

#### merge - 중복 키 처리

```java
Map<String, String> family = Map.ofEntries(
    entry("Teo", "Star Wars"),
    entry("Cristina", "James Bond")
);
Map<String, String> friends = Map.ofEntries(
    entry("Raphael", "Star Wars"),
    entry("Cristina", "Matrix")  // ⚠️ 중복 키!
);

// ✅ merge - 중복 키 병합
Map<String, String> everyone = new HashMap<>(family);
friends.forEach((k, v) -> 
    everyone.merge(k, v, (movie1, movie2) -> movie1 + " & " + movie2)
);

System.out.println(everyone);
// {Teo=Star Wars, Raphael=Star Wars, Cristina=James Bond & Matrix}
```

**단어 빈도 카운트:**

```java
Map<String, Long> moviesToCount = new HashMap<>();

String movieName = "Matrix";
moviesToCount.merge(movieName, 1L, (count, newVal) -> count + newVal);
// 키 없음 → 1 저장
// 키 있음 → count + 1
```

---

## 4. 개선된 ConcurrentHashMap

### 4.1 왜 ConcurrentHashMap?

**문제:**
- HashMap: Thread-Unsafe (Race Condition)
- Hashtable: Thread-Safe but 느림 (전체 락)
- Collections.synchronizedMap: Hashtable과 동일

**해결:**
- ConcurrentHashMap: Thread-Safe + 빠름 (부분 락)

```java
// ✅ ConcurrentHashMap
Map<String, Integer> map = new ConcurrentHashMap<>();

// 여러 스레드에서 동시에 안전하게 접근
IntStream.range(0, 100).parallel().forEach(i -> {
    map.put("key" + i, i);
    map.get("key" + i);
});
```

---

### 4.2 특징

1. **Lock-Free 읽기**: `get()` 연산은 락 없음
2. **부분 락**: 버킷 단위 락 (Java 8+)
3. **Null 불허**: 키와 값 모두 null 불가
4. **원자적 연산**: `putIfAbsent`, `remove`, `replace`
5. **약한 일관성**: Iterator는 Fail-Safe

---

### 4.3 리듀스와 검색

```java
ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<>();

// 병렬성 기준값 (1 = 병렬 최대화)
long parallelismThreshold = 1;

// forEach: 각 키-값 쌍에 액션 실행
map.forEach(parallelismThreshold, (key, value) -> 
    System.out.println(key + " = " + value)
);

// reduce: 값 집계
Optional<Long> maxValue = Optional.ofNullable(
    map.reduceValues(parallelismThreshold, Long::max)
);

// search: 조건 만족하는 첫 엔트리
String result = map.search(parallelismThreshold, (key, value) -> 
    value > 10 ? key : null
);
```

**병렬성 기준값:**
- `1`: 병렬 최대화 (공통 스레드 풀)
- `Long.MAX_VALUE`: 순차 실행 (단일 스레드)
- 맵 크기 < 기준값 → 순차 실행

---

### 4.4 계수

```java
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

// ❌ size() - int 범위 제한
int size = map.size();

// ✅ mappingCount() - long 반환
long count = map.mappingCount();
```

---

### 4.5 집합 뷰

```java
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
map.put("A", 1);
map.put("B", 2);

// ✅ keySet() - 집합 뷰
Set<String> keys = map.keySet();
System.out.println(keys);  // [A, B]

// 맵 변경 → 집합도 변경
map.put("C", 3);
System.out.println(keys);  // [A, B, C]

// ✅ newKeySet() - ConcurrentHashMap 기반 Set
Set<String> set = ConcurrentHashMap.newKeySet();
set.add("X");
set.add("Y");
```

---

### 4.6 HashMap vs ConcurrentHashMap

| 특징 | HashMap | ConcurrentHashMap |
|------|---------|-------------------|
| **Thread-Safe** | ❌ | ✅ |
| **Null 허용** | ✅ | ❌ |
| **성능 (단일 스레드)** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **성능 (멀티 스레드)** | ❌ | ⭐⭐⭐⭐⭐ |
| **Lock** | 없음 | 버킷 단위 |
| **Iterator** | Fail-Fast | Fail-Safe |

---

## 5. 핵심 정리

### 5.1 컬렉션 팩토리

```java
// List.of - 불변 리스트
List<String> list = List.of("A", "B", "C");

// Set.of - 불변 집합
Set<String> set = Set.of("A", "B", "C");

// Map.of - 불변 맵 (10개 이하)
Map<String, Integer> map = Map.of("A", 1, "B", 2);

// Map.ofEntries - 불변 맵 (10개 초과)
Map<String, Integer> map = Map.ofEntries(
    entry("A", 1),
    entry("B", 2)
);

특징:
✅ 불변 (Immutable)
✅ Null 불허
✅ 간결한 생성
❌ 추가/삭제/변경 불가
```

---

### 5.2 리스트/집합 처리

```java
// removeIf - 조건부 제거
list.removeIf(e -> e.startsWith("A"));

// replaceAll - 요소 변환 (List 전용)
list.replaceAll(String::toUpperCase);

// sort - 정렬 (List 전용)
list.sort(Comparator.naturalOrder());

특징:
✅ 원본 변경
✅ ConcurrentModificationException 없음
✅ 간결한 코드
```

---

### 5.3 맵 처리

```java
// forEach - 반복
map.forEach((k, v) -> System.out.println(k + " = " + v));

// getOrDefault - 기본값
String value = map.getOrDefault("key", "default");

// 계산 패턴
map.computeIfAbsent("key", k -> new ArrayList<>());
map.computeIfPresent("key", (k, v) -> v + 1);
map.compute("key", (k, v) -> v == null ? 1 : v + 1);

// 삭제 패턴
map.remove("key", "value");  // 키-값 일치 시 제거

// 교체 패턴
map.replace("key", newValue);
map.replace("key", oldValue, newValue);
map.replaceAll((k, v) -> v.toUpperCase());

// 합침 패턴
map.merge("key", 1, Integer::sum);  // 카운터, 합산 등
```

---

### 5.4 ConcurrentHashMap

```java
// 생성
Map<String, Integer> map = new ConcurrentHashMap<>();

// 병렬 연산
map.forEach(1, (k, v) -> ...);
Optional<Integer> max = Optional.ofNullable(
    map.reduceValues(1, Integer::max)
);

// 원자적 연산
map.putIfAbsent("key", 1);
map.remove("key", 1);
map.replace("key", 1, 2);

특징:
✅ Thread-Safe
✅ 높은 동시성
✅ Lock-Free 읽기
❌ Null 불허
```

---

### 5.5 선택 가이드

```
컬렉션 생성:
- 작고 불변 → List.of, Set.of, Map.of
- 가변 필요 → new ArrayList<>(), new HashMap<>()

반복 처리:
- 제거 필요 → removeIf
- 변환 필요 → replaceAll (List), map.replaceAll (Map)
- 정렬 필요 → list.sort

맵 연산:
- 캐시 → computeIfAbsent
- 카운터 → merge(key, 1, Integer::sum)
- 병합 → merge
- 조건부 → remove(k, v), replace(k, old, new)

멀티스레드:
- 동시성 → ConcurrentHashMap
- 읽기 전용 → Collections.unmodifiable*
- 단일 스레드 → HashMap
```

---

**작성일**: 2024년 12월  
**대상**: Modern Java In Action Chapter 8  
**난이도**: ⭐⭐⭐ (초급~중급)
