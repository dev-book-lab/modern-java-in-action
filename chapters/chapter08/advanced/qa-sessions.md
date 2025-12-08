# Chapter 08. 컬렉션 API 개선 - Q&A Sessions

> 컬렉션 팩토리, Map 연산, ConcurrentHashMap 관련 자주 묻는 질문과 답변

---

## 📚 목차

1. [컬렉션 팩토리](#1-컬렉션-팩토리)
2. [리스트/집합 처리](#2-리스트집합-처리)
3. [Map 연산](#3-map-연산)
4. [ConcurrentHashMap](#4-concurrenthashmap)
5. [성능 및 최적화](#5-성능-및-최적화)

---

## 1. 컬렉션 팩토리

### Q1. List.of()와 Arrays.asList()의 차이는?

**A:** 불변성과 null 허용 여부가 다릅니다.

```java
// Arrays.asList - 고정 크기, null 허용
List<String> list1 = Arrays.asList("A", "B", null);  // ✅ OK
list1.set(0, "X");  // ✅ OK
list1.add("C");     // ❌ UnsupportedOperationException

// List.of - 완전 불변, null 불허
List<String> list2 = List.of("A", "B", "C");
list2.set(0, "X");  // ❌ UnsupportedOperationException
list2.add("D");     // ❌ UnsupportedOperationException

// List.of + null
List<String> list3 = List.of("A", null);  // ❌ NullPointerException
```

**비교표:**

| 특징 | Arrays.asList | List.of |
|------|--------------|---------|
| 수정 (set) | ✅ | ❌ |
| 추가 (add) | ❌ | ❌ |
| Null | ✅ | ❌ |
| 성능 | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |

---

### Q2. 왜 List.of()는 0~10개까지 오버로딩하나요?

**A:** **성능 최적화**를 위해서입니다.

```java
// 가변 인수 (11개 이상)
static <E> List<E> of(E... elements) {
    // 배열 할당 필요 → 느림
    E[] array = new E[elements.length];
    // ...
}

// 전용 메서드 (0~10개)
static <E> List<E> of(E e1, E e2, E e3) {
    // 배열 할당 없음 → 빠름!
    return new List12<>(e1, e2, e3);
}
```

**성능 차이:**
```
10개 리스트 생성 (100만 회)

가변 인수:    ~50ms (배열 할당)
전용 메서드:  ~20ms (직접 할당)

차이: 2.5배
```

**설계 원칙:**
- 90% 케이스: 10개 이하 → 최적화
- 10% 케이스: 11개 이상 → 가변 인수

---

### Q3. Set.of()에 중복을 넣으면?

**A:** **즉시 예외** 발생합니다.

```java
// ❌ IllegalArgumentException
Set<String> set = Set.of("A", "B", "A");
// java.lang.IllegalArgumentException: duplicate element: A

// HashSet은 조용히 무시
Set<String> hashSet = new HashSet<>(Arrays.asList("A", "B", "A"));
System.out.println(hashSet);  // [A, B] - 중복 제거됨
```

**이유:**
```java
// Set.of 내부 (단순화)
SetN(E... input) {
    for (int i = 0; i < input.length; i++) {
        for (int j = i + 1; j < input.length; j++) {
            if (input[i].equals(input[j])) {
                throw new IllegalArgumentException(
                    "duplicate element: " + input[i]
                );
            }
        }
    }
}
```

**빠른 실패 (Fail-Fast):**
- 버그를 조기에 발견
- 명확한 에러 메시지

---

### Q4. Map.of()는 10개 이상은 어떻게?

**A:** **Map.ofEntries()**를 사용하세요.

```java
import static java.util.Map.entry;

// Map.of - 10개 이하
Map<String, Integer> map1 = Map.of(
    "A", 1,
    "B", 2,
    "C", 3
);

// Map.ofEntries - 10개 초과
Map<String, Integer> map2 = Map.ofEntries(
    entry("A", 1),
    entry("B", 2),
    entry("C", 3),
    entry("D", 4),
    entry("E", 5),
    entry("F", 6)
);
```

**entry() 메서드:**
```java
// Map.Entry 팩토리 메서드
static <K, V> Map.Entry<K, V> entry(K k, V v) {
    return new KeyValueHolder<>(
        Objects.requireNonNull(k),
        Objects.requireNonNull(v)
    );
}
```

---

### Q5. 팩토리로 만든 컬렉션을 수정하려면?

**A:** **복사**하세요.

```java
// 불변 리스트
List<String> immutable = List.of("A", "B", "C");

// ❌ 직접 수정 불가
immutable.add("D");  // UnsupportedOperationException

// ✅ 가변 복사본 생성
List<String> mutable = new ArrayList<>(immutable);
mutable.add("D");  // OK!

// Map도 마찬가지
Map<String, Integer> immutableMap = Map.of("A", 1);
Map<String, Integer> mutableMap = new HashMap<>(immutableMap);
mutableMap.put("B", 2);  // OK!
```

---

## 2. 리스트/집합 처리

### Q6. removeIf는 어떻게 ConcurrentModificationException을 피하나요?

**A:** **Iterator를 사용하지 않고** 직접 배열을 조작합니다.

```java
// ❌ for-each (실패)
for (String s : list) {
    if (s.startsWith("test")) {
        list.remove(s);  // ConcurrentModificationException!
    }
}

// 왜 실패?
// 1. for-each는 내부적으로 Iterator 사용
// 2. Iterator.next() → modCount 체크
// 3. list.remove() → modCount++
// 4. modCount != expectedModCount → 예외!

// ✅ removeIf (성공)
list.removeIf(s -> s.startsWith("test"));

// 왜 성공?
// 1. Iterator 사용 안 함
// 2. 배열 직접 조작
// 3. modCount는 마지막에 한 번만 증가
```

**내부 구현:**
```java
// ArrayList.removeIf (단순화)
public boolean removeIf(Predicate<E> filter) {
    BitSet removeSet = new BitSet(size);
    
    // 1. 삭제할 요소 찾기 (modCount 증가 안 함)
    for (int i = 0; i < size; i++) {
        if (filter.test(elementData[i])) {
            removeSet.set(i);
        }
    }
    
    // 2. 배열 압축 (한 번에)
    int w = 0;
    for (int i = 0; i < size; i++) {
        if (!removeSet.get(i)) {
            elementData[w++] = elementData[i];
        }
    }
    
    // 3. modCount 한 번만 증가
    modCount++;
    
    return true;
}
```

---

### Q7. replaceAll과 Stream.map의 차이는?

**A:** **원본 변경 여부**가 다릅니다.

```java
List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));

// Stream.map - 새 리스트 생성 (원본 불변)
List<String> newList = list.stream()
    .map(String::toUpperCase)
    .collect(Collectors.toList());

System.out.println(list);     // [a, b, c] (변경 없음)
System.out.println(newList);  // [A, B, C] (새 리스트)

// replaceAll - 원본 변경
list.replaceAll(String::toUpperCase);
System.out.println(list);  // [A, B, C] (변경됨)
```

**선택 기준:**
```
원본 보존 필요 → Stream.map
원본 변경 OK → replaceAll (더 빠름, 메모리 효율적)
```

---

### Q8. List.sort vs Collections.sort 차이는?

**A:** 거의 같지만 **List.sort가 더 현대적**입니다.

```java
List<String> list = Arrays.asList("C", "A", "B");

// Collections.sort (Java 1.2)
Collections.sort(list);  // 정적 메서드

// List.sort (Java 8)
list.sort(Comparator.naturalOrder());  // 인스턴스 메서드
```

**내부 구현:**
```java
// Collections.sort
public static <T extends Comparable<? super T>> void sort(List<T> list) {
    list.sort(null);  // ⭐ 내부적으로 List.sort 호출!
}

// List.sort (디폴트 메서드)
default void sort(Comparator<? super E> c) {
    Object[] a = this.toArray();
    Arrays.sort(a, (Comparator) c);
    ListIterator<E> i = this.listIterator();
    for (Object e : a) {
        i.next();
        i.set((E) e);
    }
}
```

**권장:** `list.sort(...)` 사용 (더 간결, 메서드 체이닝)

---

## 3. Map 연산

### Q9. computeIfAbsent는 언제 함수를 실행하나요?

**A:** **키가 없거나 값이 null일 때만** 실행합니다.

```java
Map<String, Integer> map = new HashMap<>();
map.put("A", 1);
map.put("B", null);

// 키 없음 → 함수 실행
map.computeIfAbsent("C", k -> {
    System.out.println("함수 실행!");
    return 3;
});
// 출력: "함수 실행!"
// 결과: {A=1, B=null, C=3}

// 키 있음 (null 아님) → 함수 실행 안 함
map.computeIfAbsent("A", k -> {
    System.out.println("실행되지 않음");
    return 100;
});
// 출력 없음
// 결과: {A=1, B=null, C=3} (A는 1 유지)

// 키 있음 (null) → 함수 실행
map.computeIfAbsent("B", k -> {
    System.out.println("B는 null이라 실행!");
    return 2;
});
// 출력: "B는 null이라 실행!"
// 결과: {A=1, B=2, C=3}
```

**핵심:** 캐시처럼 동작 (있으면 재사용, 없으면 계산)

---

### Q10. merge에서 BiFunction이 null을 반환하면?

**A:** **엔트리가 제거**됩니다.

```java
Map<String, Integer> inventory = new HashMap<>();
inventory.put("apple", 10);

// 재고 감소 (0 이하면 제거)
inventory.merge("apple", -3, (current, delta) -> {
    int newQty = current + delta;
    return newQty > 0 ? newQty : null;  // ⭐ null → 제거!
});

System.out.println(inventory);  // {apple=7}

// 한 번 더
inventory.merge("apple", -10, (current, delta) -> {
    int newQty = current + delta;
    return newQty > 0 ? newQty : null;  // -3 → null
});

System.out.println(inventory);  // {} (apple 제거됨)
```

**활용:**
- 조건부 제거
- 카운터 (0되면 제거)
- 임시 데이터 관리

---

### Q11. putIfAbsent vs computeIfAbsent 차이는?

**A:** **함수 실행 여부**가 다릅니다.

```java
Map<String, List<String>> map = new HashMap<>();

// putIfAbsent - 값을 미리 생성
List<String> list1 = new ArrayList<>();  // ⚠️ 항상 생성!
map.putIfAbsent("key", list1);
// 키 있으면: list1 버려짐 (메모리 낭비)
// 키 없으면: list1 저장

// computeIfAbsent - 필요할 때만 생성
map.computeIfAbsent("key", k -> new ArrayList<>());  // ✅ 필요할 때만
// 키 있으면: 함수 실행 안 함 (효율적!)
// 키 없으면: 함수 실행 → 리스트 생성
```

**성능 차이:**
```java
// 키가 이미 있는 경우 (100만 회)

// putIfAbsent
for (int i = 0; i < 1_000_000; i++) {
    map.putIfAbsent(existingKey, new ExpensiveObject());  // ⚠️ 항상 생성!
}
// 시간: ~500ms
// 메모리: 100만 개 객체 생성 후 버림

// computeIfAbsent
for (int i = 0; i < 1_000_000; i++) {
    map.computeIfAbsent(existingKey, k -> new ExpensiveObject());  // ✅ 0개 생성
}
// 시간: ~50ms
// 메모리: 0개 객체 생성

// 차이: 10배!
```

---

### Q12. remove(key, value)는 왜 필요한가요?

**A:** **원자적 연산**이 필요해서입니다.

```java
// ❌ 두 단계 (Race Condition)
if (map.get(key).equals(value)) {  // 시점 1
    map.remove(key);                // 시점 2
    // 사이에 다른 스레드가 값 변경 가능!
}

// ✅ 원자적 (Thread-Safe)
boolean removed = map.remove(key, value);
// 내부적으로 하나의 연산으로 처리
```

**멀티스레드 시나리오:**
```java
Map<String, String> sessions = new ConcurrentHashMap<>();
sessions.put("session123", "user-alice");

// Thread 1: 로그아웃
if (sessions.get("session123").equals("user-alice")) {
    // ⚠️ 여기서 Thread 2가 값 변경!
    sessions.remove("session123");  // 다른 사용자 세션 삭제! 💥
}

// Thread 2: 세션 탈취
sessions.put("session123", "user-hacker");

// ✅ 올바른 방법
boolean removed = sessions.remove("session123", "user-alice");
if (removed) {
    // user-alice의 세션만 제거됨
} else {
    // 값이 다르면 제거 안 됨 (안전!)
}
```

---

### Q13. replaceAll은 언제 사용하나요?

**A:** **모든 값을 일괄 변환**할 때 사용합니다.

```java
Map<String, Integer> prices = new HashMap<>();
prices.put("apple", 1000);
prices.put("banana", 500);
prices.put("cherry", 2000);

// ❌ 하나씩 변환 (장황)
for (Map.Entry<String, Integer> entry : prices.entrySet()) {
    entry.setValue((int)(entry.getValue() * 1.1));  // 10% 인상
}

// ✅ replaceAll (간결)
prices.replaceAll((item, price) -> (int)(price * 1.1));

// ✅ 조건부 변환
prices.replaceAll((item, price) -> 
    price >= 1000 ? (int)(price * 0.9) : price  // 1000원 이상만 10% 할인
);

// ✅ 키 기반 변환
prices.replaceAll((item, price) -> 
    item.equals("apple") ? price + 100 : price  // 사과만 100원 인상
);
```

---

## 4. ConcurrentHashMap

### Q14. ConcurrentHashMap은 왜 null을 허용 안 하나요?

**A:** **애매모호함 제거**를 위해서입니다.

```java
// HashMap
Map<String, Integer> hashMap = new HashMap<>();
hashMap.put("key", null);

Integer value = hashMap.get("key");
if (value == null) {
    // ❓ 키가 없는 것? 값이 null인 것?
}

// ConcurrentHashMap
Map<String, Integer> concMap = new ConcurrentHashMap<>();
concMap.put("key", null);  // ❌ NullPointerException

Integer value = concMap.get("key");
if (value == null) {
    // ✅ 확실: 키가 없음!
}
```

**멀티스레드 시나리오:**
```java
// 문제 상황
if (map.containsKey(key)) {  // true
    // ⚠️ 여기서 다른 스레드가 값을 null로 변경!
    Integer value = map.get(key);  // null
    // ❓ 키가 삭제된 것? 값이 null인 것?
}

// null 불허로 해결
if (concMap.containsKey(key)) {  // true
    Integer value = concMap.get(key);  // null이면
    // ✅ 확실: 다른 스레드가 삭제함
}
```

---

### Q15. ConcurrentHashMap의 size()는 정확한가요?

**A:** **근사값**입니다.

```java
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

// Thread 1: 삽입
IntStream.range(0, 1000).parallel().forEach(i -> 
    map.put("key" + i, i)
);

// Thread 2: 크기 확인
System.out.println(map.size());  // 500? 800? 1000?
// 정확하지 않을 수 있음!
```

**이유:**
```java
// size() 내부 (단순화)
public int size() {
    long n = sumCount();  // 각 세그먼트 카운트 합산
    return (n < 0L) ? 0 :
           (n > Integer.MAX_VALUE) ? Integer.MAX_VALUE :
           (int)n;
}

// ⚠️ 합산 중에 다른 스레드가 삽입/삭제 가능!
```

**대안:**
```java
// ✅ mappingCount() - long 반환 (더 정확)
long count = map.mappingCount();

// ⚠️ 여전히 근사값!
// 완벽한 정확성 필요하면 락 사용
synchronized (map) {
    int exactSize = map.size();
}
```

---

### Q16. ConcurrentHashMap의 forEach는 일관성이 보장되나요?

**A:** **약한 일관성 (Weakly Consistent)**입니다.

```java
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
map.put("A", 1);
map.put("B", 2);

// forEach 시작
map.forEach(1, (key, value) -> {
    System.out.println(key + " = " + value);
    
    // ⚠️ 도중에 다른 스레드가 수정!
    if (key.equals("A")) {
        // Thread 2가 "C" 추가
        map.put("C", 3);
    }
});

// 출력 (불확실):
// A = 1
// B = 2
// C = 3  ← 볼 수도, 안 볼 수도 있음
```

**특징:**
- ConcurrentModificationException 없음
- 최신 상태 보장 안 됨
- 반복 중 추가된 요소는 보이거나 안 보이거나

**강한 일관성 필요하면:**
```java
// ✅ 스냅샷 생성
Map<String, Integer> snapshot = new HashMap<>(map);
snapshot.forEach((k, v) -> process(k, v));
```

---

### Q17. ConcurrentHashMap은 언제 사용하나요?

**A:** **멀티스레드 + 읽기 많음**일 때 사용합니다.

```java
// ✅ 좋은 경우
- 웹 서버 세션 관리
- 캐시
- 이벤트 카운터
- 설정 관리

// ❌ 나쁜 경우
- 단일 스레드
- 강한 일관성 필요
- Null 값 필요
- 정확한 크기 필요
```

**선택 가이드:**
```
단일 스레드:
→ HashMap

멀티스레드 + 읽기 많음:
→ ConcurrentHashMap

멀티스레드 + 쓰기 많음:
→ Collections.synchronizedMap (락)
→ 또는 외부 동기화

읽기 전용:
→ Collections.unmodifiableMap
```

---

## 5. 성능 및 최적화

### Q18. 컬렉션 팩토리가 정말 빠른가요?

**A:** **작은 컬렉션(≤10개)**에서 매우 빠릅니다.

```java
// 벤치마크 (100만 회)

// new ArrayList + add
List<String> list1 = new ArrayList<>();
list1.add("A");
list1.add("B");
list1.add("C");
// 시간: ~80ms

// Arrays.asList
List<String> list2 = Arrays.asList("A", "B", "C");
// 시간: ~40ms

// List.of
List<String> list3 = List.of("A", "B", "C");
// 시간: ~20ms ⭐
// 개선: 4배!
```

**이유:**
```
1. 배열 할당 없음 (0~10개)
2. 전용 클래스 (List12, ListN)
3. JIT 인라이닝
4. @Stable 최적화
```

---

### Q19. Map.of는 큰 맵에도 적합한가요?

**A:** **아니오**, 10개 초과는 Map.ofEntries 사용하세요.

```java
// ❌ 나쁜 예
Map<String, Integer> bigMap = Map.of(
    "k1", 1, "k2", 2, ..., "k50", 50  // 컴파일 에러!
);

// ✅ 좋은 예 (10개 초과)
Map<String, Integer> bigMap = Map.ofEntries(
    entry("k1", 1),
    entry("k2", 2),
    // ...
    entry("k50", 50)
);

// ✅ 더 큰 맵 (변경 필요)
Map<String, Integer> veryBigMap = new HashMap<>();
for (int i = 0; i < 1000; i++) {
    veryBigMap.put("k" + i, i);
}
```

**성능:**
```
10개 이하:  List.of, Map.of (최고 ⭐⭐⭐⭐⭐)
10~100개:  Map.ofEntries (좋음 ⭐⭐⭐⭐)
100개 이상: new HashMap (일반 ⭐⭐⭐)
```

---

### Q20. computeIfAbsent vs getOrDefault 차이는?

**A:** **함수 실행 여부**와 **저장 여부**가 다릅니다.

```java
Map<String, Integer> map = new HashMap<>();

// getOrDefault - 저장 안 함
Integer value1 = map.getOrDefault("key", 1);
System.out.println(value1);  // 1
System.out.println(map);     // {} (빈 맵 유지)

// computeIfAbsent - 저장함
Integer value2 = map.computeIfAbsent("key", k -> 1);
System.out.println(value2);  // 1
System.out.println(map);     // {key=1} (저장됨!)
```

**사용 시나리오:**
```java
// getOrDefault - 읽기만
int count = countMap.getOrDefault(word, 0);
System.out.println(word + " 등장: " + count + "회");

// computeIfAbsent - 캐시, 그룹핑
List<String> group = groupMap.computeIfAbsent(key, k -> new ArrayList<>());
group.add(item);  // 저장된 리스트에 추가
```

---

### Q21. merge는 언제 사용하나요?

**A:** **카운터, 합산, 연결** 등에 사용합니다.

```java
// 1. 카운터
Map<String, Integer> wordCount = new HashMap<>();
words.forEach(word -> 
    wordCount.merge(word, 1, Integer::sum)  // ⭐ 가장 간결!
);

// 2. 합산
Map<String, Double> totalSales = new HashMap<>();
sales.forEach(sale -> 
    totalSales.merge(sale.getProduct(), sale.getAmount(), Double::sum)
);

// 3. 문자열 연결
Map<String, String> tags = new HashMap<>();
tags.merge("post123", "java", (old, new_) -> old + ", " + new_);
tags.merge("post123", "spring", (old, new_) -> old + ", " + new_);
// 결과: {post123="java, spring"}

// 4. 조건부 제거
inventory.merge(item, -quantity, (current, delta) -> {
    int newQty = current + delta;
    return newQty > 0 ? newQty : null;  // 0 이하면 제거
});
```

**vs 다른 방법:**
```java
// ❌ 장황
Integer count = wordCount.get(word);
if (count == null) {
    wordCount.put(word, 1);
} else {
    wordCount.put(word, count + 1);
}

// ❌ 덜 간결
wordCount.compute(word, (k, v) -> v == null ? 1 : v + 1);

// ✅ 최선
wordCount.merge(word, 1, Integer::sum);
```

---

### Q22. removeIf vs Iterator.remove 성능 차이는?

**A:** **removeIf가 약 30% 빠릅니다**.

```java
// 벤치마크: 100만 개, 절반 제거

// Iterator.remove
long start = System.nanoTime();
Iterator<Integer> iter = list.iterator();
while (iter.hasNext()) {
    if (iter.next() % 2 == 0) {
        iter.remove();
    }
}
long time1 = System.nanoTime() - start;
// 시간: ~40ms

// removeIf
long start = System.nanoTime();
list.removeIf(n -> n % 2 == 0);
long time2 = System.nanoTime() - start;
// 시간: ~30ms ⭐
// 개선: 25%
```

**이유:**
```
removeIf:
1. BitSet 사용 (메모리 효율)
2. 한 번에 압축 (2패스)
3. modCount 한 번만 증가

Iterator.remove:
1. 매번 배열 이동
2. modCount 매번 증가
```

---

### Q23. ConcurrentHashMap vs Collections.synchronizedMap 성능 차이는?

**A:** **ConcurrentHashMap이 약 10배 빠릅니다** (멀티스레드).

```java
// 벤치마크: 10개 스레드, 각 10만 회 put/get

// Collections.synchronizedMap
Map<Integer, Integer> syncMap = 
    Collections.synchronizedMap(new HashMap<>());

IntStream.range(0, 100_000).parallel().forEach(i -> {
    syncMap.put(i, i);
    syncMap.get(i);
});
// 시간: ~2000ms

// ConcurrentHashMap
Map<Integer, Integer> concMap = new ConcurrentHashMap<>();

IntStream.range(0, 100_000).parallel().forEach(i -> {
    concMap.put(i, i);
    concMap.get(i);
});
// 시간: ~200ms ⭐
// 개선: 10배!
```

**이유:**
```
synchronizedMap:
- 전체 락 (모든 메서드 synchronized)
- 한 번에 하나의 스레드만
- 읽기/쓰기 모두 블로킹

ConcurrentHashMap:
- 부분 락 (버킷 단위)
- 여러 스레드 동시 접근
- Lock-Free 읽기
```

---

## 6. 실전 팁

### Q24. 언제 어떤 메서드를 사용해야 하나요?

**A:** 상황별 가이드입니다.

```java
// 캐시 구현
map.computeIfAbsent(key, k -> loadFromDB(k));

// 단어 빈도 카운트
map.merge(word, 1, Integer::sum);

// 그룹핑
map.computeIfAbsent(category, k -> new ArrayList<>()).add(item);

// 조건부 제거 (리스트)
list.removeIf(e -> e.startsWith("temp"));

// 조건부 제거 (맵)
map.remove(key, expectedValue);

// 일괄 변환 (리스트)
list.replaceAll(String::trim);

// 일괄 변환 (맵)
map.replaceAll((k, v) -> v * 1.1);

// 맵 병합
map2.forEach((k, v) -> map1.merge(k, v, Integer::sum));

// 멀티스레드 + 읽기 많음
Map<K, V> cache = new ConcurrentHashMap<>();
```

---

**작성일**: 2024년 12월  
**대상**: Modern Java In Action Chapter 8
