# Chapter 08. 컬렉션 API 개선 - CheatSheet

> 컬렉션 팩토리, 처리 메서드, Map 고급 연산 핵심 요약

---

## 🏭 컬렉션 팩토리 Quick Reference

### 기본 사용법

```java
// List
List<String> list = List.of("A", "B", "C");

// Set
Set<String> set = Set.of("A", "B", "C");

// Map (10개 이하)
Map<String, Integer> map = Map.of("A", 1, "B", 2);

// Map (10개 초과)
Map<String, Integer> map = Map.ofEntries(
    entry("A", 1),
    entry("B", 2),
    entry("C", 3)
);
```

### 특징 비교

| 방법 | 가변 | Null | 추가/삭제 | 성능 |
|------|------|------|----------|------|
| **new ArrayList()** | ✅ | ✅ | ✅ | ⭐⭐⭐⭐ |
| **Arrays.asList()** | 부분 | ✅ | ❌ | ⭐⭐⭐ |
| **List.of()** | ❌ | ❌ | ❌ | ⭐⭐⭐⭐⭐ |
| **Collections.unmodifiable* | ❌ | ✅ | ❌ | ⭐⭐⭐ |

---

## 📝 리스트/집합 처리 Quick Reference

### removeIf - 조건부 제거

```java
✅ 사용
list.removeIf(e -> e.startsWith("test"));
numbers.removeIf(n -> n % 2 == 0);

❌ 피하기
for (Iterator<E> it = list.iterator(); it.hasNext();) {
    if (condition) it.remove();
}
```

### replaceAll - 요소 변환

```java
✅ 사용
list.replaceAll(String::toUpperCase);
codes.replaceAll(s -> s.trim());

❌ 피하기
for (int i = 0; i < list.size(); i++) {
    list.set(i, transform(list.get(i)));
}
```

### sort - 정렬

```java
✅ 사용
list.sort(Comparator.naturalOrder());
list.sort(Comparator.reverseOrder());
list.sort((a, b) -> a.length() - b.length());

❌ 피하기
Collections.sort(list);
```

---

## 🗺️ Map 처리 Quick Reference

### forEach - 반복

```java
✅ 간결
map.forEach((k, v) -> System.out.println(k + "=" + v));

❌ 장황
for (Map.Entry<K, V> entry : map.entrySet()) {
    K k = entry.getKey();
    V v = entry.getValue();
}
```

### 정렬

```java
// 키 기준
map.entrySet().stream()
    .sorted(Map.Entry.comparingByKey())
    .forEach(...);

// 값 기준
map.entrySet().stream()
    .sorted(Map.Entry.comparingByValue())
    .forEach(...);
```

---

## 🧮 계산 패턴

### computeIfAbsent

```java
// 캐시
map.computeIfAbsent(key, k -> expensiveCalculation());

// 그룹핑
multimap.computeIfAbsent(key, k -> new ArrayList<>())
    .add(value);

// 단어 카운트 (초기화)
map.computeIfAbsent(word, k -> 0);
map.put(word, map.get(word) + 1);
```

### computeIfPresent

```java
// 값 증가
map.computeIfPresent(key, (k, v) -> v + 1);

// 조건부 제거 (null 반환 시)
map.computeIfPresent(key, (k, v) -> 
    v > 0 ? v : null  // 0 이하면 제거
);
```

### compute

```java
// 카운터 (키 유무 관계없이)
map.compute(word, (k, v) -> v == null ? 1 : v + 1);

// 항상 계산
map.compute(key, (k, v) -> transform(v));
```

---

## 🗑️ 삭제 패턴

```java
// 단순 제거
map.remove(key);

// 조건부 제거 (원자적)
map.remove(key, value);  // 키-값 일치 시만 제거

// 예제
if (map.get(key).equals(value)) {  // ❌ Race Condition
    map.remove(key);
}
map.remove(key, value);  // ✅ Thread-Safe
```

---

## 🔄 교체 패턴

### replace

```java
// 키 있으면 교체
map.replace(key, newValue);

// 키-값 일치 시 교체 (CAS)
map.replace(key, oldValue, newValue);

// 모든 값 변환
map.replaceAll((k, v) -> v.toUpperCase());
```

### 사용 예시

```java
// 값 업데이트 (키 있을 때만)
map.replace("key", "newValue");

// 원자적 업데이트
map.replace("counter", 5, 6);  // 5일 때만 6으로

// 일괄 변환
prices.replaceAll((item, price) -> price * 1.1);
```

---

## 🔗 합침 패턴

### putAll vs merge

```java
// putAll - 단순 병합 (덮어씀)
Map<K, V> combined = new HashMap<>(map1);
combined.putAll(map2);

// merge - 중복 키 처리
map1.forEach((k, v) -> 
    map2.merge(k, v, (v1, v2) -> v1 + " & " + v2)
);
```

### merge 활용

```java
// 카운터
map.merge(word, 1, Integer::sum);

// 합산
map.merge(key, value, Double::sum);

// 문자열 연결
map.merge(key, "new", (old, new_) -> old + ", " + new_);

// 조건부 제거 (null 반환 시)
map.merge(key, -1, (current, delta) -> {
    int newVal = current + delta;
    return newVal > 0 ? newVal : null;
});
```

---

## 🔐 ConcurrentHashMap Quick Reference

### 기본 사용

```java
// 생성
Map<K, V> map = new ConcurrentHashMap<>();

// Thread-Safe 연산
map.put(key, value);
map.get(key);
map.putIfAbsent(key, value);
map.remove(key, value);
map.replace(key, oldValue, newValue);
```

### 병렬 연산

```java
// 병렬성 기준값
long threshold = 1;  // 1 = 병렬 최대화

// forEach
map.forEach(threshold, (k, v) -> process(k, v));

// reduce
Long max = map.reduceValues(threshold, Long::max);
Long sum = map.reduceValues(threshold, 0L, Long::sum);

// search
String result = map.search(threshold, (k, v) -> 
    v > 10 ? k : null
);
```

### 계수

```java
// ❌ size() - int 범위
int size = map.size();

// ✅ mappingCount() - long 범위
long count = map.mappingCount();
```

---

## 🎯 패턴별 사용 가이드

### 캐시 구현

```java
Map<String, Data> cache = new HashMap<>();

// computeIfAbsent 사용
Data data = cache.computeIfAbsent(key, k -> loadFromDB(k));
```

### 단어 빈도 카운트

```java
Map<String, Integer> wordCount = new HashMap<>();

// merge 사용 (최선)
words.forEach(word -> 
    wordCount.merge(word, 1, Integer::sum)
);

// compute 사용
words.forEach(word -> 
    wordCount.compute(word, (k, v) -> v == null ? 1 : v + 1)
);
```

### 그룹핑

```java
Map<String, List<Person>> groups = new HashMap<>();

// computeIfAbsent 사용
people.forEach(person -> 
    groups.computeIfAbsent(person.getCity(), k -> new ArrayList<>())
        .add(person)
);
```

### Map 병합

```java
Map<String, Integer> result = new HashMap<>(map1);

// merge로 값 합산
map2.forEach((k, v) -> 
    result.merge(k, v, Integer::sum)
);

// merge로 문자열 연결
map2.forEach((k, v) -> 
    result.merge(k, v, (v1, v2) -> v1 + " & " + v2)
);
```

---

## ⚡ 성능 최적화

### 초기 용량 설정

```java
// ❌ 기본 용량 (16)
Map<K, V> map = new HashMap<>();

// ✅ 예상 크기 지정
Map<K, V> map = new HashMap<>(expectedSize);

// ✅ Load Factor 고려
Map<K, V> map = new HashMap<>(expectedSize / 0.75 + 1);
```

### 불변 vs 가변

```java
// 불변 (읽기 전용)
Map<K, V> immutable = Map.of(...);
Map<K, V> immutable = Collections.unmodifiableMap(map);

// 가변 (수정 필요)
Map<K, V> mutable = new HashMap<>(Map.of(...));
```

---

## 🐛 자주 하는 실수

### 1. 팩토리 메서드 + null

```java
❌ List.of(1, 2, null);  // NullPointerException
✅ Arrays.asList(1, 2, null);  // OK
```

### 2. Set.of + 중복

```java
❌ Set.of("A", "B", "A");  // IllegalArgumentException
✅ new HashSet<>(Arrays.asList("A", "B", "A"));  // [A, B]
```

### 3. 팩토리 메서드 + 수정

```java
List<String> list = List.of("A", "B");
❌ list.add("C");  // UnsupportedOperationException
✅ List<String> mutable = new ArrayList<>(List.of("A", "B"));
   mutable.add("C");
```

### 4. forEach + 제거

```java
❌ list.forEach(e -> {
    if (condition) list.remove(e);  // ConcurrentModificationException
});

✅ list.removeIf(condition);
```

### 5. compute + 같은 맵 수정

```java
❌ map.computeIfAbsent(key, k -> {
    map.put(otherKey, value);  // ConcurrentModificationException
    return newValue;
});

✅ map.computeIfAbsent(key, k -> calculateValue(k));
```

### 6. merge + null

```java
// null 반환 → 엔트리 제거됨
map.merge(key, value, (v1, v2) -> null);  // 제거!

// 의도적 제거
map.merge(key, -1, (current, delta) -> {
    int newVal = current + delta;
    return newVal > 0 ? newVal : null;  // 0 이하면 제거
});
```

---

## 📊 메서드 비교표

### 계산 패턴

| 메서드 | 키 없을 때 | 키 있을 때 | 용도 |
|--------|-----------|-----------|------|
| **computeIfAbsent** | 계산 후 저장 | 기존 값 | 캐시, 그룹핑 |
| **computeIfPresent** | 무시 | 계산 후 저장 | 조건부 업데이트 |
| **compute** | 계산 후 저장 | 계산 후 저장 | 일반 계산 |
| **merge** | value 저장 | 병합 후 저장 | 카운터, 합산 |

### 삭제/교체 패턴

| 메서드 | 조건 | 반환 | 원자적 |
|--------|------|------|-------|
| **remove(key)** | 키 존재 | 이전 값 | ❌ |
| **remove(key, value)** | 키-값 일치 | boolean | ✅ |
| **replace(key, value)** | 키 존재 | 이전 값 | ❌ |
| **replace(key, old, new)** | 키-값 일치 | boolean | ✅ |

---

## 🔥 빠른 체크리스트

### 컬렉션 생성

```
□ 작고 불변? → List.of, Set.of, Map.of
□ 수정 필요? → new ArrayList, new HashMap
□ Null 필요? → Arrays.asList, new HashMap
□ 성능 중요? → 초기 용량 지정
```

### 리스트/집합 처리

```
□ 조건부 제거? → removeIf
□ 요소 변환? → replaceAll (List만)
□ 정렬? → list.sort
□ ConcurrentModificationException? → removeIf 사용
```

### Map 처리

```
□ 캐시? → computeIfAbsent
□ 카운터? → merge(key, 1, Integer::sum)
□ 조건부 삭제? → remove(key, value)
□ 조건부 교체? → replace(key, old, new)
□ Map 병합? → merge
□ 멀티스레드? → ConcurrentHashMap
```

---

## 📚 추가 자료

- [📖 Deep Dive](deep-dive.md) - 내부 메커니즘 상세 분석
- [💬 Q&A](qa-sessions.md) - 자주 묻는 질문과 답변
- [💻 Code](../code/) - 실전 예제 코드

---

**작성일**: 2024년 12월  
**대상**: Modern Java In Action Chapter 8
