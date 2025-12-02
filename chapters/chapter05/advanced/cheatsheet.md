# Chapter 05 Cheat Sheet 📝

> 스트림 활용의 핵심만 빠르게!

---

## 🎯 필터링

| 연산 | 설명 | 예제 |
|------|------|------|
| `filter` | 조건 필터 | `.filter(d -> d.getCalories() < 400)` |
| `distinct` | 중복 제거 | `.distinct()` |
| `limit(n)` | n개만 | `.limit(3)` |
| `skip(n)` | n개 제외 | `.skip(2)` |

**Java 9+:**
- `takeWhile` - 조건 false까지
- `dropWhile` - 조건 false부터

---

## 🔄 매핑

```java
map(Function)         // 1:1 변환
flatMap(Function)     // 평면화
mapToInt/Long/Double  // 기본형 변환
```

---

## 🔍 매칭 & 검색

```java
anyMatch   // 하나라도?
allMatch   // 모두?
noneMatch  // 하나도 없나?
findAny    // 아무거나
findFirst  // 첫 번째
```

---

## ♻️ 리듀싱

```java
reduce(0, Integer::sum)    // 합계
reduce(Integer::max)       // 최대
reduce((a,b) -> a+b)      // 커스텀
```

---

## 🔢 기본형 스트림

```java
// 변환
mapToInt/Long/Double  // → IntStream
boxed()               // → Stream<Integer>
asLongStream()        // IntStream → LongStream

// 전용 메서드
sum(), average(), max(), min()
summaryStatistics()

// 범위
IntStream.range(1, 100)        // [1, 100)
IntStream.rangeClosed(1, 100)  // [1, 100]
```

---

## 🌊 스트림 생성

```java
Stream.of("A", "B")
Stream.empty()
Stream.ofNullable(value)   // Java 9+
Arrays.stream(array)
Files.lines(path)
Stream.iterate(0, n -> n+1).limit(10)
Stream.generate(Math::random).limit(5)
```

---

## ⚡ 지연 평가 (Lazy Evaluation)

**핵심 개념:**
- 중간 연산은 실행하지 않고 **기록만** 함
- 최종 연산이 호출될 때 **한 번에 실행**
- 필요한 만큼만 처리 (쇼트서킷)

**예시:**
```java
list.stream()
    .filter(n -> {
        System.out.println("filter: " + n);
        return n % 2 == 0;
    })
    .map(n -> {
        System.out.println("map: " + n);
        return n * n;
    })
    .limit(2);
// 아무것도 출력 안 됨! (최종 연산 없음)

// .collect(toList()) 추가하면 출력 시작!
```

---

## 💡 자주 쓰는 패턴

### 필터 + 맵 + 수집
```java
list.stream()
    .filter(condition)
    .map(transformer)
    .collect(toList());
```

### 숫자 집계
```java
list.stream()
    .mapToInt(Item::getValue)
    .sum();
```

### Optional 처리
```java
stream.findAny()
    .ifPresent(System.out::println);
```

### 실전 쿼리 패턴
```java
// 특정 조건 필터링 + 정렬 + 수집
transactions.stream()
    .filter(t -> t.getYear() == 2011)
    .sorted(comparing(Transaction::getValue))
    .collect(toList());

// 그룹별 추출 + 중복 제거
transactions.stream()
    .map(t -> t.getTrader().getCity())
    .distinct()
    .collect(toList());

// 조건 검사
transactions.stream()
    .anyMatch(t -> t.getTrader().getCity().equals("Milan"));
```

---

## ⚠️ 주의사항

1. **스트림 재사용 불가** - 한 번만 소비
2. **무한 스트림에 limit 필수** - 아니면 무한 루프
3. **파일 스트림 close 필수** - try-with-resources 사용
4. **박싱 비용 주의** - 기본형 스트림 사용
5. **지연 평가 이해** - 최종 연산 없으면 실행 안 됨

---

## 🚀 최적화

```java
// ✅ filter 먼저 (데이터 줄이기)
stream.filter(...).map(...).limit(n)

// ✅ 기본형 스트림 사용
stream.mapToInt(...).sum()

// ✅ 쇼트서킷 활용
stream.anyMatch(...) // 찾으면 즉시 종료

// ✅ limit 활용 (조기 종료)
stream.filter(...).limit(10)
```

---

## 📊 성능 비교

| 연산 | 전체 순회 | 조기 종료 |
|------|----------|----------|
| filter | ✅ | ❌ |
| map | ✅ | ❌ |
| anyMatch | ❌ | ✅ |
| limit | ❌ | ✅ |
| takeWhile | ❌ | ✅ |

---

**마지막 업데이트**: 2024년 12월
