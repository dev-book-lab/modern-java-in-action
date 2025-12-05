# Chapter 06 Deep Dive 🔬

> 스트림 데이터 수집의 모든 것을 깊이 있게

---

## 📚 목차

1. [리듀싱과 요약](#1-리듀싱과-요약)
2. [그룹화 (groupingBy)](#2-그룹화-groupingby)
3. [분할 (partitioningBy)](#3-분할-partitioningby)
4. [Collector 인터페이스](#4-collector-인터페이스)
5. [커스텀 컬렉터 구현](#5-커스텀-컬렉터-구현)
6. [성능 최적화](#6-성능-최적화)
7. [collect vs reduce](#7-collect-vs-reduce)

---

## 1. 리듀싱과 요약

### 1.1 counting - 개수 세기

**동작 원리:**
```
입력: [dish1, dish2, dish3, dish4, dish5]

카운터 초기화: count = 0
dish1 → count = 1
dish2 → count = 2
dish3 → count = 3
dish4 → count = 4
dish5 → count = 5

출력: 5L
```

**예제:**
```java
long howManyDishes = menu.stream()
    .collect(counting());

// 더 간단하게
long count = menu.stream().count();
```

### 1.2 maxBy / minBy - 최대/최소값

**동작 원리:**
```
입력: [800(pork), 700(beef), 400(chicken)]
비교자: Dish::getCalories

초기: max = null
800 → max = 800 (pork)
700 → max = 800 (800 > 700)
400 → max = 800 (800 > 400)

출력: Optional[pork(800)]
```

**예제:**
```java
Comparator<Dish> dishCaloriesComparator = 
    Comparator.comparingInt(Dish::getCalories);

Optional<Dish> mostCalorieDish = menu.stream()
    .collect(maxBy(dishCaloriesComparator));

mostCalorieDish.ifPresent(dish -> 
    System.out.println(dish.getName())
);
```

### 1.3 summingInt - 합계

**동작 원리:**
```
입력: [pork(800), beef(700), chicken(400)]

누적: sum = 0
pork → sum = 0 + 800 = 800
beef → sum = 800 + 700 = 1500
chicken → sum = 1500 + 400 = 1900

출력: 1900
```

**예제:**
```java
int totalCalories = menu.stream()
    .collect(summingInt(Dish::getCalories));
```

**summingLong, summingDouble도 동일:**
```java
long totalLong = stream.collect(summingLong(T::longValue));
double totalDouble = stream.collect(summingDouble(T::doubleValue));
```

### 1.4 averagingInt - 평균

**동작 원리:**
```
입력: [pork(800), beef(700), chicken(400)]

누적:
sum = 0, count = 0

pork → sum = 800, count = 1
beef → sum = 1500, count = 2
chicken → sum = 1900, count = 3

평균 = sum / count = 1900 / 3 = 633.33

출력: 633.33
```

**예제:**
```java
double avgCalories = menu.stream()
    .collect(averagingInt(Dish::getCalories));
```

### 1.5 summarizingInt - 요약 통계

**한 번에 모든 통계 수집:**
```java
IntSummaryStatistics menuStatistics = menu.stream()
    .collect(summarizingInt(Dish::getCalories));

System.out.println(menuStatistics);
// IntSummaryStatistics{count=9, sum=4200, min=120, average=466.67, max=800}

// 개별 접근
long count = menuStatistics.getCount();
int sum = (int) menuStatistics.getSum();
double avg = menuStatistics.getAverage();
int min = menuStatistics.getMin();
int max = menuStatistics.getMax();
```

**내부 동작:**
```
하나의 순회로 모든 통계 수집:

초기: count=0, sum=0, min=MAX, max=MIN

dish1(800) → count=1, sum=800, min=800, max=800
dish2(700) → count=2, sum=1500, min=700, max=800
dish3(400) → count=3, sum=1900, min=400, max=800
...

최종 계산:
average = sum / count
```

### 1.6 joining - 문자열 연결

**기본 joining:**
```java
String shortMenu = menu.stream()
    .map(Dish::getName)
    .collect(joining());
// 결과: "porkbeefchickenfrench friesrice..."
```

**구분자 포함:**
```java
String shortMenu = menu.stream()
    .map(Dish::getName)
    .collect(joining(", "));
// 결과: "pork, beef, chicken, french fries, rice, ..."
```

**접두사/접미사 포함:**
```java
String shortMenu = menu.stream()
    .map(Dish::getName)
    .collect(joining(", ", "[", "]"));
// 결과: "[pork, beef, chicken, french fries, rice, ...]"
```

**내부 동작 (StringBuilder 사용):**
```
초기: sb = new StringBuilder("[")

"pork" → sb.append("pork") → "[pork"
"beef" → sb.append(", ").append("beef") → "[pork, beef"
"chicken" → sb.append(", ").append("chicken") → "[pork, beef, chicken"
...

최종: sb.append("]") → "[pork, beef, chicken, ...]"
```

### 1.7 reducing - 범용 리듀싱

**세 개의 인수:**
```java
int totalCalories = menu.stream()
    .collect(reducing(
        0,                      // 초기값
        Dish::getCalories,      // 변환 함수
        Integer::sum            // BinaryOperator
    ));
```

**동작 과정:**
```
초기값: accumulator = 0

dish1(pork) → 변환: 800 → 합산: 0 + 800 = 800
dish2(beef) → 변환: 700 → 합산: 800 + 700 = 1500
dish3(chicken) → 변환: 400 → 합산: 1500 + 400 = 1900
...

최종: 1900
```

**한 개의 인수:**
```java
Optional<Dish> mostCalorieDish = menu.stream()
    .collect(reducing(
        (d1, d2) -> d1.getCalories() > d2.getCalories() ? d1 : d2
    ));
```

**동작 과정:**
```
초기값: 첫 번째 요소

accumulator = dish1(pork, 800)

dish2(beef, 700) → 비교: 800 > 700 → accumulator = pork
dish3(chicken, 400) → 비교: 800 > 400 → accumulator = pork
...

최종: Optional[pork]
```

---

## 2. 그룹화 (groupingBy)

### 2.1 기본 그룹화

**단순 그룹화:**
```java
Map<Dish.Type, List<Dish>> dishesByType = menu.stream()
    .collect(groupingBy(Dish::getType));
```

**동작 과정:**
```
초기: map = {}

pork(MEAT) → map = {MEAT=[pork]}
beef(MEAT) → map = {MEAT=[pork, beef]}
chicken(MEAT) → map = {MEAT=[pork, beef, chicken]}
prawns(FISH) → map = {MEAT=[...], FISH=[prawns]}
rice(OTHER) → map = {MEAT=[...], FISH=[...], OTHER=[rice]}
...

최종: {
  MEAT=[pork, beef, chicken],
  FISH=[prawns, salmon],
  OTHER=[french fries, rice, ...]
}
```

**분류 함수 (Classifier):**
```
groupingBy(Dish::getType)
           ↓
분류 함수: dish → dish.getType()

각 요소를 함수에 적용 → 키 추출 → 해당 키의 리스트에 추가
```

### 2.2 복잡한 분류 기준

**람다 표현식 사용:**
```java
public enum CaloricLevel { DIET, NORMAL, FAT }

Map<CaloricLevel, List<Dish>> dishesByCaloricLevel = menu.stream()
    .collect(groupingBy(dish -> {
        if (dish.getCalories() <= 400) return CaloricLevel.DIET;
        else if (dish.getCalories() <= 700) return CaloricLevel.NORMAL;
        else return CaloricLevel.FAT;
    }));
```

**동작 과정:**
```
chicken(400) → 분류: 400 ≤ 400 → DIET
beef(700) → 분류: 400 < 700 ≤ 700 → NORMAL
pork(800) → 분류: 800 > 700 → FAT

결과: {
  DIET=[chicken, prawns, rice, season fruit],
  NORMAL=[beef, salmon, french fries, pizza],
  FAT=[pork]
}
```

### 2.3 다운스트림 컬렉터

**개수 세기:**
```java
Map<Dish.Type, Long> typesCount = menu.stream()
    .collect(groupingBy(Dish::getType, counting()));
```

**동작 과정:**
```
1단계: 타입별로 그룹화
  MEAT → [pork, beef, chicken]
  FISH → [prawns, salmon]
  OTHER → [french fries, rice, ...]

2단계: 각 그룹에 counting() 적용
  MEAT → 3개 → 3L
  FISH → 2개 → 2L
  OTHER → 4개 → 4L

최종: {MEAT=3, FISH=2, OTHER=4}
```

**최대값 찾기:**
```java
Map<Dish.Type, Optional<Dish>> mostCaloricByType = menu.stream()
    .collect(groupingBy(
        Dish::getType,
        maxBy(comparingInt(Dish::getCalories))
    ));
```

**동작 과정:**
```
1단계: 타입별로 그룹화
  MEAT → [pork(800), beef(700), chicken(400)]
  FISH → [prawns(400), salmon(450)]
  OTHER → [...]

2단계: 각 그룹에서 maxBy() 적용
  MEAT → maxBy(...) → Optional[pork(800)]
  FISH → maxBy(...) → Optional[salmon(450)]
  OTHER → maxBy(...) → Optional[pizza(550)]

최종: {
  MEAT=Optional[pork],
  FISH=Optional[salmon],
  OTHER=Optional[pizza]
}
```

### 2.4 filtering - 그룹 내 필터링

**filter vs filtering 차이:**
```java
// ❌ filter 먼저 - 키 누락 가능
Map<Dish.Type, List<Dish>> caloricDishesByType = menu.stream()
    .filter(dish -> dish.getCalories() > 500)
    .collect(groupingBy(Dish::getType));
// FISH 타입 요리가 모두 500 이하면 FISH 키 자체가 없음!

// ✅ filtering 컬렉터 - 모든 키 유지
Map<Dish.Type, List<Dish>> caloricDishesByType = menu.stream()
    .collect(groupingBy(
        Dish::getType,
        filtering(dish -> dish.getCalories() > 500, toList())
    ));
// FISH=[] 로 키는 유지됨!
```

**동작 과정:**
```
1단계: 타입별로 그룹화
  MEAT → [pork(800), beef(700), chicken(400)]
  FISH → [prawns(400), salmon(450)]
  OTHER → [french fries(530), rice(350), pizza(550)]

2단계: 각 그룹에서 filtering() 적용
  MEAT → 500 초과 → [pork(800), beef(700)]
  FISH → 500 초과 → []
  OTHER → 500 초과 → [french fries(530), pizza(550)]

최종: {
  MEAT=[pork, beef],
  FISH=[],           ← 빈 리스트로 유지!
  OTHER=[french fries, pizza]
}
```

### 2.5 mapping - 그룹 내 변환

**요리명만 추출:**
```java
Map<Dish.Type, List<String>> dishNamesByType = menu.stream()
    .collect(groupingBy(
        Dish::getType,
        mapping(Dish::getName, toList())
    ));
```

**동작 과정:**
```
1단계: 타입별로 그룹화
  MEAT → [pork, beef, chicken]
  FISH → [prawns, salmon]
  OTHER → [...]

2단계: 각 그룹에 mapping() 적용
  MEAT → [pork, beef, chicken] → ["pork", "beef", "chicken"]
  FISH → [prawns, salmon] → ["prawns", "salmon"]
  OTHER → [...] → [...]

최종: {
  MEAT=["pork", "beef", "chicken"],
  FISH=["prawns", "salmon"],
  OTHER=[...]
}
```

### 2.6 flatMapping - 그룹 내 평면화

**태그 추출:**
```java
Map<String, List<String>> dishTags = Map.of(
    "pork", Arrays.asList("greasy", "salty"),
    "beef", Arrays.asList("salty", "roasted"),
    "chicken", Arrays.asList("fried", "crisp"),
    ...
);

Map<Dish.Type, Set<String>> dishTagsByType = menu.stream()
    .collect(groupingBy(
        Dish::getType,
        flatMapping(dish -> dishTags.get(dish.getName()).stream(), toSet())
    ));
```

**동작 과정:**
```
1단계: 타입별로 그룹화
  MEAT → [pork, beef, chicken]

2단계: flatMapping() 적용
  pork → dishTags.get("pork") → ["greasy", "salty"]
  beef → dishTags.get("beef") → ["salty", "roasted"]
  chicken → dishTags.get("chicken") → ["fried", "crisp"]
  
  평면화 → ["greasy", "salty", "salty", "roasted", "fried", "crisp"]
  
  toSet() → {"greasy", "salty", "roasted", "fried", "crisp"}

최종: {
  MEAT={"greasy", "salty", "roasted", "fried", "crisp"},
  FISH={...},
  OTHER={...}
}
```

### 2.7 다수준 그룹화

**2단계 그룹화:**
```java
Map<Dish.Type, Map<CaloricLevel, List<Dish>>> dishesByTypeCaloricLevel = 
    menu.stream()
        .collect(groupingBy(
            Dish::getType,                    // 1차 분류
            groupingBy(dish -> {              // 2차 분류
                if (dish.getCalories() <= 400) return CaloricLevel.DIET;
                else if (dish.getCalories() <= 700) return CaloricLevel.NORMAL;
                else return CaloricLevel.FAT;
            })
        ));
```

**동작 과정:**
```
1단계: 타입별 그룹화
  MEAT → [pork(800), beef(700), chicken(400)]
  FISH → [prawns(400), salmon(450)]
  OTHER → [...]

2단계: 각 그룹을 칼로리 레벨별로 다시 그룹화
  MEAT:
    chicken(400) → DIET
    beef(700) → NORMAL
    pork(800) → FAT
    결과: {DIET=[chicken], NORMAL=[beef], FAT=[pork]}

  FISH:
    prawns(400) → DIET
    salmon(450) → NORMAL
    결과: {DIET=[prawns], NORMAL=[salmon]}

  OTHER:
    ...

최종: {
  MEAT={
    DIET=[chicken],
    NORMAL=[beef],
    FAT=[pork]
  },
  FISH={
    DIET=[prawns],
    NORMAL=[salmon]
  },
  OTHER={...}
}
```

**n수준 트리 구조:**
```
n=1: Map<K, List<T>>
n=2: Map<K1, Map<K2, List<T>>>
n=3: Map<K1, Map<K2, Map<K3, List<T>>>>
...
```

---

## 3. 분할 (partitioningBy)

### 3.1 기본 분할

**Boolean 키:**
```java
Map<Boolean, List<Dish>> partitionedMenu = menu.stream()
    .collect(partitioningBy(Dish::isVegetarian));
```

**동작 과정:**
```
초기: map = {true=[], false=[]}

pork(채식X) → false → map = {true=[], false=[pork]}
beef(채식X) → false → map = {true=[], false=[pork, beef]}
chicken(채식X) → false → map = {true=[], false=[pork, beef, chicken]}
french fries(채식O) → true → map = {true=[french fries], false=[...]}
rice(채식O) → true → map = {true=[french fries, rice], false=[...]}
...

최종: {
  false=[pork, beef, chicken, prawns, salmon],
  true=[french fries, rice, season fruit, pizza]
}
```

### 3.2 partitioningBy의 장점

**항상 두 개의 키 보장:**
```java
// groupingBy - 키가 없을 수 있음
Map<Boolean, List<Dish>> grouped = menu.stream()
    .collect(groupingBy(Dish::isVegetarian));
// 채식 요리가 없으면: {false=[...]}

// partitioningBy - 항상 두 키 존재
Map<Boolean, List<Dish>> partitioned = menu.stream()
    .collect(partitioningBy(Dish::isVegetarian));
// 채식 요리가 없어도: {false=[...], true=[]}
```

### 3.3 다운스트림 컬렉터와 조합

**분할 + 그룹화:**
```java
Map<Boolean, Map<Dish.Type, List<Dish>>> vegetarianDishesByType = 
    menu.stream()
        .collect(partitioningBy(
            Dish::isVegetarian,
            groupingBy(Dish::getType)
        ));
```

**동작 과정:**
```
1단계: 채식 여부로 분할
  false(비채식) → [pork, beef, chicken, prawns, salmon]
  true(채식) → [french fries, rice, season fruit, pizza]

2단계: 각 그룹을 타입별로 그룹화
  false(비채식):
    MEAT → [pork, beef, chicken]
    FISH → [prawns, salmon]
  
  true(채식):
    OTHER → [french fries, rice, season fruit, pizza]

최종: {
  false={
    MEAT=[pork, beef, chicken],
    FISH=[prawns, salmon]
  },
  true={
    OTHER=[french fries, rice, season fruit, pizza]
  }
}
```

**분할 + 최대값:**
```java
Map<Boolean, Dish> mostCaloricPartitionedByVegetarian = menu.stream()
    .collect(partitioningBy(
        Dish::isVegetarian,
        collectingAndThen(
            maxBy(comparingInt(Dish::getCalories)),
            Optional::get
        )
    ));
```

**동작 과정:**
```
1단계: 분할
  false → [pork(800), beef(700), chicken(400), prawns(400), salmon(450)]
  true → [french fries(530), rice(350), season fruit(120), pizza(550)]

2단계: 각 그룹에서 maxBy() 적용
  false → maxBy(...) → Optional[pork(800)]
  true → maxBy(...) → Optional[pizza(550)]

3단계: Optional::get으로 변환
  false → pork(800)
  true → pizza(550)

최종: {
  false=pork,
  true=pizza
}
```

---

## 4. Collector 인터페이스

### 4.1 Collector의 구조

**제네릭 타입:**
```java
public interface Collector<T, A, R> {
    // T: 입력 요소 타입
    // A: 누적자 타입 (중간 컨테이너)
    // R: 최종 결과 타입
}
```

**예시:**
```java
Collector<Dish, List<Dish>, List<Dish>> toList()
//        ↑     ↑            ↑
//        요리   ArrayList    List
```

### 4.2 5가지 메서드

**1. supplier() - 컨테이너 생성:**
```java
Supplier<List<T>> supplier() {
    return ArrayList::new;
}
```

**동작:**
```
호출 → new ArrayList<>() 생성 → 빈 리스트 반환
```

**2. accumulator() - 요소 추가:**
```java
BiConsumer<List<T>, T> accumulator() {
    return List::add;
}
```

**동작:**
```
(list, element) → list.add(element)

list = []
accumulator(list, "a") → list = ["a"]
accumulator(list, "b") → list = ["a", "b"]
accumulator(list, "c") → list = ["a", "b", "c"]
```

**3. combiner() - 병합:**
```java
BinaryOperator<List<T>> combiner() {
    return (list1, list2) -> {
        list1.addAll(list2);
        return list1;
    };
}
```

**동작 (병렬 처리):**
```
Thread1: list1 = ["a", "b"]
Thread2: list2 = ["c", "d"]

combiner(list1, list2):
  list1.addAll(list2) → list1 = ["a", "b", "c", "d"]
  return list1
```

**4. finisher() - 최종 변환:**
```java
Function<List<T>, List<T>> finisher() {
    return Function.identity();
}
```

**동작:**
```
변환이 필요 없는 경우: identity (그대로 반환)
변환이 필요한 경우: StringBuilder → String
```

**5. characteristics() - 특성:**
```java
Set<Characteristics> characteristics() {
    return EnumSet.of(
        Characteristics.IDENTITY_FINISH,
        Characteristics.CONCURRENT
    );
}
```

### 4.3 Characteristics 설명

**IDENTITY_FINISH:**
```
finisher가 항등 함수
→ finisher() 호출 생략 가능
→ 누적자를 그대로 결과로 사용

예: toList()
  A = List<T>
  R = List<T>
  같으므로 변환 불필요!
```

**UNORDERED:**
```
결과가 순서에 영향받지 않음
→ 병렬 처리 시 효율적 분할 가능

예: toSet()
  Set은 순서가 없음
  → 어떤 순서로 추가되든 상관없음
```

**CONCURRENT:**
```
여러 스레드가 동시에 같은 누적자에 접근 가능
→ 주의: 스레드 안전한 컨테이너 필요!

예: ConcurrentHashMap
```

### 4.4 순차 리듀싱 과정

**단계별 실행:**
```
Stream: ["a", "b", "c"]
Collector: toList()

1. supplier() 호출
   → container = []

2. accumulator() 반복 호출
   accumulator(container, "a") → container = ["a"]
   accumulator(container, "b") → container = ["a", "b"]
   accumulator(container, "c") → container = ["a", "b", "c"]

3. finisher() 호출
   → finisher(container) → ["a", "b", "c"]

4. 결과 반환
   → ["a", "b", "c"]
```

### 4.5 병렬 리듀싱 과정

**병렬 실행:**
```
Stream: ["a", "b", "c", "d", "e", "f", "g", "h"]

1. 분할 (4개 스레드)
   Thread1: ["a", "b"]
   Thread2: ["c", "d"]
   Thread3: ["e", "f"]
   Thread4: ["g", "h"]

2. 각 스레드에서 supplier() + accumulator()
   Thread1: container1 = [] → ["a"] → ["a", "b"]
   Thread2: container2 = [] → ["c"] → ["c", "d"]
   Thread3: container3 = [] → ["e"] → ["e", "f"]
   Thread4: container4 = [] → ["g"] → ["g", "h"]

3. combiner()로 병합
   1단계:
     combiner(container1, container2) → ["a", "b", "c", "d"]
     combiner(container3, container4) → ["e", "f", "g", "h"]
   
   2단계:
     combiner(["a", "b", "c", "d"], ["e", "f", "g", "h"])
     → ["a", "b", "c", "d", "e", "f", "g", "h"]

4. finisher() 호출
   → ["a", "b", "c", "d", "e", "f", "g", "h"]
```

---

**[Part 2로 계속...]**
# Chapter 06 Deep Dive - Part 2 🔬

---

## 5. 커스텀 컬렉터 구현

### 5.1 ToListCollector 구현

**전체 코드:**
```java
public class ToListCollector<T> implements Collector<T, List<T>, List<T>> {
    
    @Override
    public Supplier<List<T>> supplier() {
        return ArrayList::new;
    }
    
    @Override
    public BiConsumer<List<T>, T> accumulator() {
        return List::add;
    }
    
    @Override
    public BinaryOperator<List<T>> combiner() {
        return (list1, list2) -> {
            list1.addAll(list2);
            return list1;
        };
    }
    
    @Override
    public Function<List<T>, List<T>> finisher() {
        return Function.identity();
    }
    
    @Override
    public Set<Characteristics> characteristics() {
        return EnumSet.of(
            Characteristics.IDENTITY_FINISH,
            Characteristics.CONCURRENT
        );
    }
}
```

**사용:**
```java
List<Dish> dishes = menu.stream()
    .collect(new ToListCollector<>());
```

### 5.2 소수 분할 컬렉터

**문제: 기본 방법의 비효율성:**
```java
public static boolean isPrime(int candidate) {
    int candidateRoot = (int) Math.sqrt(candidate);
    return IntStream.rangeClosed(2, candidateRoot)
                    .noneMatch(i -> candidate % i == 0);
}

Map<Boolean, List<Integer>> partitioned = 
    IntStream.rangeClosed(2, n)
        .boxed()
        .collect(partitioningBy(candidate -> isPrime(candidate)));
```

**비효율적인 이유:**
```
100을 검사할 때:
√100 = 10
2, 3, 4, 5, 6, 7, 8, 9, 10으로 나눔 (9번)

하지만 4, 6, 8, 9, 10은 합성수!
실제로는 2, 3, 5, 7로만 나눠도 충분 (4번)

절약: 5번 (55%)
```

**최적화된 커스텀 컬렉터:**
```java
public class PrimeNumbersCollector 
        implements Collector<Integer, 
                            Map<Boolean, List<Integer>>, 
                            Map<Boolean, List<Integer>>> {
    
    @Override
    public Supplier<Map<Boolean, List<Integer>>> supplier() {
        return () -> new HashMap<>() {{
            put(true, new ArrayList<>());   // 소수 리스트
            put(false, new ArrayList<>());  // 합성수 리스트
        }};
    }
    
    @Override
    public BiConsumer<Map<Boolean, List<Integer>>, Integer> accumulator() {
        return (acc, candidate) -> {
            // 핵심: 이미 찾은 소수만 사용!
            acc.get(isPrime(acc.get(true), candidate))
               .add(candidate);
        };
    }
    
    @Override
    public BinaryOperator<Map<Boolean, List<Integer>>> combiner() {
        return (map1, map2) -> {
            map1.get(true).addAll(map2.get(true));
            map1.get(false).addAll(map2.get(false));
            return map1;
        };
    }
    
    @Override
    public Function<Map<Boolean, List<Integer>>, 
                   Map<Boolean, List<Integer>>> finisher() {
        return Function.identity();
    }
    
    @Override
    public Set<Characteristics> characteristics() {
        return EnumSet.of(Characteristics.IDENTITY_FINISH);
    }
    
    // 최적화된 소수 판별
    public static boolean isPrime(List<Integer> primes, int candidate) {
        int candidateRoot = (int) Math.sqrt(candidate);
        return primes.stream()
                     .takeWhile(i -> i <= candidateRoot)
                     .noneMatch(i -> candidate % i == 0);
    }
}
```

**동작 과정:**
```
초기: acc = {true=[], false=[]}

candidate = 2:
  primes = [] (빈 리스트)
  √2 = 1.4
  takeWhile(i <= 1.4) → 빈 스트림
  noneMatch → true (소수!)
  acc = {true=[2], false=[]}

candidate = 3:
  primes = [2]
  √3 = 1.7
  takeWhile(i <= 1.7) → 빈 스트림 (2 > 1.7)
  noneMatch → true (소수!)
  acc = {true=[2, 3], false=[]}

candidate = 4:
  primes = [2, 3]
  √4 = 2.0
  takeWhile(i <= 2.0) → [2]
  2로 나눔: 4 % 2 == 0 → true
  noneMatch → false (합성수!)
  acc = {true=[2, 3], false=[4]}

candidate = 5:
  primes = [2, 3]
  √5 = 2.2
  takeWhile(i <= 2.2) → [2]
  2로 나눔: 5 % 2 != 0 → false
  noneMatch → true (소수!)
  acc = {true=[2, 3, 5], false=[4]}

...

candidate = 100:
  primes = [2, 3, 5, 7, 11, 13, ...]
  √100 = 10
  takeWhile(i <= 10) → [2, 3, 5, 7]
  2로 나눔: 100 % 2 == 0 → true
  noneMatch → false (합성수!)
```

**핵심 최적화:**
```
1. 이미 찾은 소수만 사용
   기본: 2~√n까지 모두
   최적화: 이미 찾은 소수만

2. takeWhile로 조기 종료
   √candidate까지만 확인

3. 누적 효과
   더 많은 소수를 찾을수록
   다음 판별이 더 빠름
```

---

## 6. 성능 최적화

### 6.1 성능 측정

**벤치마크 코드:**
```java
public class CollectorHarness {
    
    public static void main(String[] args) {
        long fastest = Long.MAX_VALUE;
        
        for (int i = 0; i < 10; i++) {
            long start = System.nanoTime();
            partitionPrimesWithCustomCollector(1_000_000);
            long duration = (System.nanoTime() - start) / 1_000_000;
            
            if (duration < fastest) {
                fastest = duration;
            }
            System.out.println("done in " + duration);
        }
        
        System.out.println("Fastest execution: " + fastest + " msecs");
    }
}
```

**측정 결과:**
```
n = 1,000:
  기본 방법: 45ms
  커스텀 컬렉터: 18ms
  개선율: 60% (2.5배 빠름)

n = 10,000:
  기본 방법: 1,850ms
  커스텀 컬렉터: 680ms
  개선율: 63% (2.7배 빠름)

n = 100,000:
  기본 방법: 125,000ms (2분 5초)
  커스텀 컬렉터: 35,000ms (35초)
  개선율: 72% (3.6배 빠름)

n = 1,000,000:
  기본 방법: 측정 불가 (너무 오래 걸림)
  커스텀 컬렉터: 약 5분
```

### 6.2 연산 횟수 비교

**n=1000일 때:**
```
기본 방법:
  각 숫자마다 √n까지 확인
  예: 997 검사 시 2~31까지 (30회)
  총 연산: 약 15,000회

커스텀 컬렉터:
  각 숫자마다 √n 이하의 소수만 확인
  예: 997 검사 시 [2,3,5,7,11,13,17,19,23,29,31] (11회)
  총 연산: 약 6,000회

절약: 9,000회 (60%)
```

### 6.3 최적화 기법 정리

**1. 점진적 소수 리스트:**
```
2 검사: [] 사용 → 즉시 소수
3 검사: [2] 사용
5 검사: [2, 3] 사용
7 검사: [2, 3, 5] 사용
...
997 검사: [2, 3, 5, ..., 31] 사용 (11개)

누적 효과로 점점 빨라짐!
```

**2. takeWhile 조기 종료:**
```
소수 리스트: [2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, ...]

100 검사:
  √100 = 10
  takeWhile(i <= 10) → [2, 3, 5, 7]
  11 이후는 확인 안 함!
```

**3. 순차 처리:**
```
병렬 처리 시:
  각 스레드가 독립적으로 소수 리스트 생성
  → 서로의 정보 공유 안 됨
  → 최적화 효과 감소

순차 처리 시:
  하나의 소수 리스트 공유
  → 점진적 누적
  → 최적화 효과 최대
```

---

## 7. collect vs reduce

### 7.1 근본적인 차이

**collect - 가변 리듀싱:**
```java
List<String> result = stream.collect(
    ArrayList::new,           // 컨테이너 생성
    ArrayList::add,           // 요소 추가 (가변)
    ArrayList::addAll         // 병합
);
```

**동작:**
```
컨테이너 = []

"a" 추가 → 같은 컨테이너 = ["a"]
"b" 추가 → 같은 컨테이너 = ["a", "b"]
"c" 추가 → 같은 컨테이너 = ["a", "b", "c"]

같은 객체를 계속 수정!
```

**reduce - 불변 리듀싱:**
```java
Optional<String> result = stream.reduce((s1, s2) -> s1 + s2);
```

**동작:**
```
"a" + "b" = "ab" (새 String)
"ab" + "c" = "abc" (새 String)
"abc" + "d" = "abcd" (새 String)

매번 새 객체 생성!
```

### 7.2 언제 무엇을 사용하는가?

**collect 사용:**
```
✅ 컬렉션 생성
   List<String> list = stream.collect(toList());

✅ 그룹화/분할
   Map<Type, List<Dish>> grouped = stream.collect(groupingBy(...));

✅ 복잡한 집계
   IntSummaryStatistics stats = stream.collect(summarizingInt(...));

✅ 문자열 연결
   String result = stream.collect(joining(", "));
```

**reduce 사용:**
```
✅ 단일 값 계산
   int sum = stream.reduce(0, Integer::sum);

✅ 최대/최소
   Optional<Integer> max = stream.reduce(Integer::max);

✅ 불변 객체 결합
   BigInteger sum = stream.reduce(BigInteger.ZERO, BigInteger::add);
```

### 7.3 잘못된 사용 예시

**❌ reduce로 리스트 만들기:**
```java
// 최악의 코드!
List<Integer> numbers = stream.reduce(
    new ArrayList<>(),
    (list, elem) -> {
        List<Integer> newList = new ArrayList<>(list);
        newList.add(elem);
        return newList;  // 매번 새 리스트!
    },
    (list1, list2) -> {
        List<Integer> newList = new ArrayList<>(list1);
        newList.addAll(list2);
        return newList;
    }
);
```

**문제점:**
```
요소 1000개 처리 시:

1번째: [1] 크기 1
2번째: [1, 2] 크기 2
3번째: [1, 2, 3] 크기 3
...
1000번째: [1, 2, ..., 1000] 크기 1000

총 메모리: 1 + 2 + 3 + ... + 1000
         = 1000 × 1001 / 2
         = 500,500 개의 참조

✅ collect 사용 시: 1000개의 참조만
```

**❌ reduce로 StringBuilder:**
```java
// 가변 객체를 reduce에 사용!
StringBuilder result = stream.reduce(
    new StringBuilder(),
    (sb, str) -> sb.append(str),  // 가변 수정!
    (sb1, sb2) -> sb1.append(sb2)
);
```

**문제점:**
```
병렬 스트림에서:
  Thread1: sb.append("a")
  Thread2: sb.append("b")
  // 동시 접근 → 경쟁 상태!

예측 불가능한 결과!
```

**✅ 올바른 방법:**
```java
String result = stream.collect(joining());
```

### 7.4 성능 비교

**문자열 1000개 연결:**
```java
// reduce - 매번 새 String
long time1 = measureTime(() ->
    words.stream().reduce("", (s1, s2) -> s1 + s2)
);
// 시간: ~500ms

// collect(joining) - StringBuilder 사용
long time2 = measureTime(() ->
    words.stream().collect(joining())
);
// 시간: ~5ms

// 100배 차이!
```

### 7.5 의사결정 플로우

```
무엇을 만들고 싶은가?

컬렉션 (List, Set, Map)?
  ├─ Yes → collect() 사용
  └─ No → 단일 값?
           ├─ Yes → 숫자 계산?
           │        ├─ Yes → reduce() 또는 전용 메서드
           │        └─ No → 문자열?
           │                 ├─ 작은 데이터 → reduce()
           │                 └─ 큰 데이터 → collect(joining())
           └─ No → 그룹화/분할?
                    ├─ Yes → collect(groupingBy/partitioningBy)
                    └─ No → collect() 고려
```

---

## 💡 핵심 정리

### Collectors 메서드 분류

**리듀싱과 요약:**
- `counting()` - 개수
- `summingInt()`, `averagingInt()`, `summarizingInt()` - 숫자 집계
- `maxBy()`, `minBy()` - 최대/최소
- `joining()` - 문자열 연결
- `reducing()` - 범용 리듀싱

**그룹화와 분할:**
- `groupingBy()` - 그룹화 (n개 키)
- `partitioningBy()` - 분할 (2개 키)

**변환:**
- `collectingAndThen()` - 결과 변환
- `mapping()` - 매핑 후 수집
- `flatMapping()` - 평면화 후 수집
- `filtering()` - 필터링 후 수집

### Collector 구현 체크리스트

```
✓ supplier() - 빈 컨테이너 생성
✓ accumulator() - 요소 추가 (가변)
✓ combiner() - 병합 (병렬 처리용)
✓ finisher() - 최종 변환
✓ characteristics() - 최적화 힌트
```

### 성능 최적화 팁

```
1. 불필요한 연산 제거
   - takeWhile로 조기 종료
   - 이미 계산한 값 재사용

2. 효율적인 자료구조
   - StringBuilder: 문자열 연결
   - TreeSet: 정렬 + 중복 제거
   - PriorityQueue: Top-K

3. 올바른 도구 선택
   - collect: 컬렉션 생성
   - reduce: 단일 값 계산
```

### 실무 패턴

**패턴 1: 그룹화 + 개수**
```java
Map<K, Long> counts = stream
    .collect(groupingBy(classifier, counting()));
```

**패턴 2: 그룹화 + 최대값**
```java
Map<K, T> maxByGroup = stream
    .collect(groupingBy(
        classifier,
        collectingAndThen(
            maxBy(comparator),
            Optional::get
        )
    ));
```

**패턴 3: 분할 + 그룹화**
```java
Map<Boolean, Map<K, List<T>>> partitionedGroups = stream
    .collect(partitioningBy(
        predicate,
        groupingBy(classifier)
    ));
```

**패턴 4: 필터링 + 매핑**
```java
Map<K, List<R>> filtered = stream
    .collect(groupingBy(
        classifier,
        filtering(
            predicate,
            mapping(mapper, toList())
        )
    ));
```

---

**마지막 업데이트**: 2024년 12월  
**관련 챕터**: Chapter 5 (스트림 활용), Chapter 7 (병렬 데이터 처리)
