# Chapter 08. 컬렉션 API 개선 - Deep Dive

> 컬렉션 팩토리, Map 연산, ConcurrentHashMap의 내부 메커니즘 완벽 분석

---

## 📚 목차

1. [컬렉션 팩토리 내부 구조](#1-컬렉션-팩토리-내부-구조)
2. [removeIf/replaceAll 내부 동작](#2-removeif-replaceall-내부-동작)
3. [Map 계산 패턴 메커니즘](#3-map-계산-패턴-메커니즘)
4. [HashMap Treeification 상세](#4-hashmap-treeification-상세)
5. [ConcurrentHashMap 동시성 제어](#5-concurrenthashmap-동시성-제어)

---

## 1. 컬렉션 팩토리 내부 구조

### 1.1 List.of() 내부 구현

#### 오버로딩 구조

```java
// JDK 소스코드 (java.util.List)

// 0개
static <E> List<E> of() {
    return ImmutableCollections.emptyList();
}

// 1개
static <E> List<E> of(E e1) {
    return new ImmutableCollections.List12<>(e1);
}

// 2개
static <E> List<E> of(E e1, E e2) {
    return new ImmutableCollections.List12<>(e1, e2);
}

// 3~10개 (생략)
static <E> List<E> of(E e1, E e2, E e3) { ... }
// ...
static <E> List<E> of(E e1, ..., E e10) { ... }

// 11개 이상
@SafeVarargs
static <E> List<E> of(E... elements) {
    return new ImmutableCollections.ListN<>(elements);
}
```

**설계 이유:**
```
0~10개: 전용 클래스 → 배열 할당 없음 → 빠름
11개 이상: 가변 인수 → 배열 할당 → 느림 (하지만 11개 이상은 드묾)
```

---

#### 내부 클래스 구조

```java
// List12 - 1~2개 전용
static final class List12<E> extends AbstractImmutableList<E> {
    
    private final E e0;  // 첫 번째 요소
    @Stable
    private final E e1;  // 두 번째 요소 (없으면 null)
    
    List12(E e0) {
        this.e0 = Objects.requireNonNull(e0);  // null 체크!
        this.e1 = null;
    }
    
    List12(E e0, E e1) {
        this.e0 = Objects.requireNonNull(e0);
        this.e1 = Objects.requireNonNull(e1);
    }
    
    @Override
    public int size() {
        return e1 != null ? 2 : 1;  // O(1)
    }
    
    @Override
    public E get(int index) {
        if (index == 0) return e0;
        else if (index == 1 && e1 != null) return e1;
        else throw new IndexOutOfBoundsException();
    }
    
    @Override
    public boolean contains(Object o) {
        return o.equals(e0) || o.equals(e1);  // 최대 2번 비교
    }
}

// ListN - 3개 이상
static final class ListN<E> extends AbstractImmutableList<E> {
    
    @Stable
    private final E[] elements;  // 배열 저장
    
    @SafeVarargs
    ListN(E... input) {
        // null 체크
        for (int i = 0; i < input.length; i++) {
            Objects.requireNonNull(input[i]);
        }
        // 복사 (외부 수정 방지)
        this.elements = input.clone();
    }
    
    @Override
    public int size() {
        return elements.length;
    }
    
    @Override
    public E get(int index) {
        return elements[index];  // 범위 체크는 배열이 자동으로
    }
}
```

---

#### 불변성 보장 메커니즘

```java
abstract class AbstractImmutableList<E> extends AbstractList<E> {
    
    // ⭐ 모든 변경 메서드를 막음
    
    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
    
    // Iterator도 불변
    @Override
    public Iterator<E> iterator() {
        return new Itr();  // 읽기 전용 Iterator
    }
}
```

---

### 1.2 Set.of() 내부 구현

```java
// Set0 - 비어있는 집합
static final class Set0<E> extends AbstractImmutableSet<E> {
    @Override
    public int size() { return 0; }
    
    @Override
    public boolean contains(Object o) { return false; }
    
    @Override
    public Iterator<E> iterator() {
        return Collections.emptyIterator();
    }
}

// Set1 - 1개
static final class Set1<E> extends AbstractImmutableSet<E> {
    private final E e0;
    
    Set1(E e0) {
        this.e0 = Objects.requireNonNull(e0);
    }
    
    @Override
    public int size() { return 1; }
    
    @Override
    public boolean contains(Object o) {
        return o.equals(e0);
    }
}

// SetN - 2개 이상
static final class SetN<E> extends AbstractImmutableSet<E> {
    
    private final E[] elements;
    private final int size;
    
    @SafeVarargs
    SetN(E... input) {
        size = input.length;  // 중복 제거 후 크기
        
        // ⭐ 중복 체크!
        for (int i = 0; i < input.length; i++) {
            Objects.requireNonNull(input[i]);
            for (int j = i + 1; j < input.length; j++) {
                if (input[i].equals(input[j])) {
                    throw new IllegalArgumentException(
                        "duplicate element: " + input[i]
                    );
                }
            }
        }
        
        this.elements = input.clone();
    }
    
    @Override
    public boolean contains(Object o) {
        for (E e : elements) {
            if (o.equals(e)) return true;
        }
        return false;
    }
}
```

**중복 체크 비용:**
```
O(n²) - 생성 시 한 번만 수행
작은 집합 (< 10개)에서는 무시할 수준
```

---

### 1.3 Map.of() 내부 구현

```java
// Map0 - 비어있는 맵
static final class Map0<K, V> extends AbstractImmutableMap<K, V> {
    @Override
    public int size() { return 0; }
    
    @Override
    public V get(Object key) { return null; }
}

// Map1 - 1개
static final class Map1<K, V> extends AbstractImmutableMap<K, V> {
    private final K k0;
    private final V v0;
    
    Map1(K k0, V v0) {
        this.k0 = Objects.requireNonNull(k0);
        this.v0 = Objects.requireNonNull(v0);
    }
    
    @Override
    public int size() { return 1; }
    
    @Override
    public V get(Object key) {
        return k0.equals(key) ? v0 : null;
    }
}

// MapN - 2개 이상
static final class MapN<K, V> extends AbstractImmutableMap<K, V> {
    
    // ⭐ 키와 값을 번갈아 저장: [k0, v0, k1, v1, k2, v2, ...]
    private final Object[] table;
    private final int size;
    
    MapN(Object... input) {
        if ((input.length & 1) != 0) {  // 홀수면 에러
            throw new InternalError("length is odd");
        }
        size = input.length >> 1;  // / 2
        
        // null 체크 + 중복 키 체크
        for (int i = 0; i < input.length; i += 2) {
            Objects.requireNonNull(input[i]);     // 키
            Objects.requireNonNull(input[i + 1]); // 값
            
            // 중복 키 체크
            for (int j = 0; j < i; j += 2) {
                if (input[i].equals(input[j])) {
                    throw new IllegalArgumentException(
                        "duplicate key: " + input[i]
                    );
                }
            }
        }
        
        this.table = input;
    }
    
    @Override
    public V get(Object key) {
        // 순차 탐색: O(n)
        for (int i = 0; i < table.length; i += 2) {
            if (key.equals(table[i])) {
                return (V) table[i + 1];
            }
        }
        return null;
    }
}
```

**성능 특성:**
```
작은 맵 (< 10개):
- 순차 탐색 O(n)
- 메모리 효율적 (해시 테이블 없음)
- 캐시 친화적

큰 맵 (≥ 10개):
- Map.ofEntries 사용 권장
- 내부적으로 HashMap 기반
```

---

### 1.4 메모리 비교

```java
// 벤치마크: 10개 요소 저장

// ArrayList
new ArrayList<>(List.of(1, 2, ..., 10))
메모리: ~80 bytes (배열 + 오버헤드)

// ImmutableCollections.ListN
List.of(1, 2, ..., 10)
메모리: ~56 bytes (배열만)
절약: 30%

// HashMap
new HashMap<>(Map.of(1, "a", 2, "b", ..., 10, "j"))
메모리: ~512 bytes (버킷 배열 + 엔트리)

// ImmutableCollections.MapN
Map.of(1, "a", 2, "b", ..., 10, "j")
메모리: ~96 bytes (단일 배열)
절약: 81%
```

---

## 2. removeIf/replaceAll 내부 동작

### 2.1 removeIf 내부 구현

```java
// ArrayList.removeIf (단순화)
public boolean removeIf(Predicate<? super E> filter) {
    Objects.requireNonNull(filter);
    
    int removeCount = 0;
    final int size = this.size;
    final BitSet removeSet = new BitSet(size);  // ⭐ 삭제할 인덱스 기록
    
    // ⭐ 1단계: 삭제할 요소 찾기 (읽기 only)
    for (int i = 0; i < size; i++) {
        if (filter.test(elementData[i])) {
            removeSet.set(i);  // 비트 설정
            removeCount++;
        }
    }
    
    // 삭제할 게 없으면 종료
    if (removeCount == 0) {
        return false;
    }
    
    // ⭐ 2단계: 배열 압축 (쓰기)
    int w = 0;  // 쓰기 포인터
    for (int i = 0; i < size; i++) {
        if (!removeSet.get(i)) {  // 삭제 안 할 요소만
            elementData[w++] = elementData[i];
        }
    }
    
    // ⭐ 3단계: 나머지 null 처리
    for (int i = w; i < size; i++) {
        elementData[i] = null;  // GC 대상
    }
    
    this.size = w;
    modCount++;  // ⭐ 수정 카운트 증가
    
    return true;
}
```

**핵심 최적화:**
```
1. BitSet 사용 → 메모리 효율적 (비트 단위)
2. 2패스 알고리즘 → 한 번에 압축
3. modCount 한 번만 증가 → Iterator 안전
```

---

#### ConcurrentModificationException 회피

```java
// ❌ 일반 for-each (실패)
for (E element : list) {
    if (condition) {
        list.remove(element);
        // modCount++ → Iterator.expectedModCount와 불일치
        // 다음 next() 호출 시 예외 발생!
    }
}

// ✅ removeIf (성공)
list.removeIf(condition);
// modCount는 마지막에 한 번만 증가
// Iterator 사용하지 않음
```

---

### 2.2 replaceAll 내부 구현

```java
// ArrayList.replaceAll (단순화)
public void replaceAll(UnaryOperator<E> operator) {
    Objects.requireNonNull(operator);
    
    final int size = this.size;
    
    // ⭐ 배열 직접 수정 (Iterator 없음)
    for (int i = 0; i < size; i++) {
        elementData[i] = operator.apply(elementData[i]);
    }
    
    modCount++;  // ⭐ 한 번만 증가
}
```

**Stream vs replaceAll:**

```java
// Stream (새 리스트 생성)
List<String> newList = list.stream()
    .map(String::toUpperCase)
    .collect(Collectors.toList());
// 장점: 불변성, 함수형
// 단점: 새 리스트 생성 (메모리)

// replaceAll (원본 수정)
list.replaceAll(String::toUpperCase);
// 장점: 메모리 효율적, 빠름
// 단점: 원본 변경
```

---

## 3. Map 계산 패턴 메커니즘

### 3.1 computeIfAbsent 내부 동작

```java
// HashMap.computeIfAbsent (단순화)
public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
    Objects.requireNonNull(mappingFunction);
    
    int hash = hash(key);
    Node<K,V>[] tab; 
    Node<K,V> first; 
    int n, i;
    
    // 1. 테이블 초기화
    if ((tab = table) == null || (n = tab.length) == 0) {
        n = (tab = resize()).length;
    }
    
    // 2. 버킷 찾기
    first = tab[i = (n - 1) & hash];
    
    // 3. 버킷이 비어있으면
    if (first == null) {
        V v = mappingFunction.apply(key);  // ⭐ 함수 실행
        if (v != null) {
            tab[i] = new Node<>(hash, key, v, null);  // 새 노드 생성
            ++modCount;
            ++size;
        }
        return v;
    }
    
    // 4. 버킷에서 키 찾기
    Node<K,V> e = first;
    do {
        if (e.hash == hash && Objects.equals(key, e.key)) {
            return e.value;  // ⭐ 기존 값 반환 (함수 실행 안 함!)
        }
    } while ((e = e.next) != null);
    
    // 5. 키 없음 → 함수 실행 후 추가
    V v = mappingFunction.apply(key);
    if (v != null) {
        // 버킷에 추가 (연결 리스트 또는 트리)
        addEntry(hash, key, v, i);
    }
    
    return v;
}
```

**핵심 포인트:**
```
1. 키 있으면 함수 실행 안 함 (효율)
2. 함수가 null 반환 → 저장 안 함
3. 함수 내부에서 같은 맵 수정 금지 (ConcurrentModificationException)
```

---

### 3.2 merge 내부 동작

```java
// HashMap.merge (단순화)
public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
    Objects.requireNonNull(value);
    Objects.requireNonNull(remappingFunction);
    
    int hash = hash(key);
    Node<K,V>[] tab; 
    Node<K,V> first; 
    int n, i;
    
    if ((tab = table) == null || (n = tab.length) == 0) {
        n = (tab = resize()).length;
    }
    
    first = tab[i = (n - 1) & hash];
    
    // 1. 버킷이 비어있으면 → value 저장
    if (first == null) {
        tab[i] = new Node<>(hash, key, value, null);
        ++modCount;
        ++size;
        return value;
    }
    
    // 2. 버킷에서 키 찾기
    Node<K,V> e = first;
    do {
        if (e.hash == hash && Objects.equals(key, e.key)) {
            // ⭐ 키 있음 → 함수로 병합
            V oldValue = e.value;
            V newValue = remappingFunction.apply(oldValue, value);
            
            if (newValue != null) {
                e.value = newValue;  // 값 교체
            } else {
                // null 반환 → 엔트리 제거!
                removeNode(hash, key, null, false, true);
            }
            return newValue;
        }
    } while ((e = e.next) != null);
    
    // 3. 키 없음 → value 저장
    addEntry(hash, key, value, i);
    return value;
}
```

**사용 패턴:**

```java
// 카운터
map.merge(word, 1, Integer::sum);

// 내부 동작:
// 1. word 없음 → 1 저장
// 2. word 있음 → remappingFunction.apply(기존값, 1)
//              → Integer.sum(기존값, 1)
//              → 기존값 + 1

// 조건부 제거
map.merge(key, -1, (current, delta) -> {
    int newVal = current + delta;
    return newVal > 0 ? newVal : null;  // null → 제거
});
```

---

## 4. HashMap Treeification 상세

### 4.1 변환 조건

```java
// HashMap 상수
static final int TREEIFY_THRESHOLD = 8;      // 트리로 변환
static final int UNTREEIFY_THRESHOLD = 6;    // 리스트로 복원
static final int MIN_TREEIFY_CAPACITY = 64;  // 최소 테이블 크기
```

**변환 로직:**

```java
// HashMap.putVal (단순화)
final V putVal(int hash, K key, V value, boolean onlyIfAbsent, boolean evict) {
    // ... 버킷 찾기 및 삽입
    
    if (binCount >= TREEIFY_THRESHOLD - 1) {  // 8개 이상
        treeifyBin(tab, hash);  // ⭐ 트리화
    }
    
    return null;
}

// HashMap.treeifyBin
final void treeifyBin(Node<K,V>[] tab, int hash) {
    int n, index; 
    Node<K,V> e;
    
    // ⭐ 테이블 크기가 64 미만이면 리사이징으로 해결
    if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY) {
        resize();
        return;
    }
    
    // 버킷 찾기
    if ((e = tab[index = (n - 1) & hash]) != null) {
        TreeNode<K,V> hd = null, tl = null;
        
        // 1. Node → TreeNode 변환
        do {
            TreeNode<K,V> p = replacementTreeNode(e, null);
            if (tl == null)
                hd = p;
            else {
                p.prev = tl;
                tl.next = p;
            }
            tl = p;
        } while ((e = e.next) != null);
        
        // 2. Red-Black Tree 구성
        if ((tab[index] = hd) != null)
            hd.treeify(tab);
    }
}
```

---

### 4.2 TreeNode 구조

```java
static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {
    TreeNode<K,V> parent;  // 부모
    TreeNode<K,V> left;    // 왼쪽 자식
    TreeNode<K,V> right;   // 오른쪽 자식
    TreeNode<K,V> prev;    // 이전 노드 (순회용)
    boolean red;           // Red-Black Tree 색상
    
    // 탐색
    final TreeNode<K,V> find(int h, Object k, Class<?> kc) {
        TreeNode<K,V> p = this;
        do {
            int ph, dir; 
            K pk;
            TreeNode<K,V> pl = p.left, pr = p.right, q;
            
            // 해시 비교
            if ((ph = p.hash) > h)
                p = pl;  // 왼쪽
            else if (ph < h)
                p = pr;  // 오른쪽
            else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                return p;  // 찾음!
            
            // 해시 같으면 Comparable로 비교
            else if ((kc != null || (kc = comparableClassFor(k)) != null) &&
                     (dir = compareComparables(kc, k, pk)) != 0)
                p = (dir < 0) ? pl : pr;
            else if ((q = pr.find(h, k, kc)) != null)
                return q;
            else
                p = pl;
        } while (p != null);
        return null;
    }
}
```

---

### 4.3 성능 비교

```java
// 시나리오: 10,000개가 모두 같은 버킷 (최악의 경우)

// Java 7 (연결 리스트)
map.get(key);  // O(10,000) = 10,000번 비교

// Java 8+ (Red-Black Tree)
map.get(key);  // O(log 10,000) ≈ 14번 비교

// 성능 개선: 약 700배!
```

---

## 5. ConcurrentHashMap 동시성 제어

### 5.1 Java 8+ 내부 구조

```java
public class ConcurrentHashMap<K,V> extends AbstractMap<K,V> {
    
    // ⭐ 버킷 배열 (volatile)
    transient volatile Node<K,V>[] table;
    
    static class Node<K,V> implements Map.Entry<K,V> {
        final int hash;
        final K key;
        volatile V val;        // ⭐ volatile
        volatile Node<K,V> next; // ⭐ volatile
    }
    
    // 포워딩 노드 (리사이징 중)
    static final class ForwardingNode<K,V> extends Node<K,V> {
        final Node<K,V>[] nextTable;
    }
    
    // 트리 노드
    static final class TreeNode<K,V> extends Node<K,V> {
        TreeNode<K,V> parent;
        TreeNode<K,V> left;
        TreeNode<K,V> right;
        TreeNode<K,V> prev;
        boolean red;
    }
}
```

---

### 5.2 put() 동작 (CAS + synchronized)

```java
// ConcurrentHashMap.putVal (단순화)
final V putVal(K key, V value, boolean onlyIfAbsent) {
    if (key == null || value == null) throw new NullPointerException();
    
    int hash = spread(key.hashCode());
    int binCount = 0;
    
    for (Node<K,V>[] tab = table;;) {
        Node<K,V> f; int n, i, fh;
        
        // 1. 테이블 초기화
        if (tab == null || (n = tab.length) == 0)
            tab = initTable();
        
        // 2. 버킷이 비어있으면 CAS로 삽입 (락 없음!)
        else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
            if (casTabAt(tab, i, null, new Node<K,V>(hash, key, value, null)))
                break;  // ⭐ 성공! (Lock-Free)
        }
        
        // 3. 리사이징 중이면 도와줌
        else if ((fh = f.hash) == MOVED)
            tab = helpTransfer(tab, f);
        
        // 4. 버킷에 노드 있으면 synchronized
        else {
            V oldVal = null;
            
            // ⭐ 버킷 헤드에만 락!
            synchronized (f) {
                if (tabAt(tab, i) == f) {  // 더블 체크
                    
                    // 연결 리스트
                    if (fh >= 0) {
                        binCount = 1;
                        for (Node<K,V> e = f;; ++binCount) {
                            K ek;
                            if (e.hash == hash &&
                                ((ek = e.key) == key || key.equals(ek))) {
                                oldVal = e.val;
                                if (!onlyIfAbsent)
                                    e.val = value;
                                break;
                            }
                            Node<K,V> pred = e;
                            if ((e = e.next) == null) {
                                pred.next = new Node<>(hash, key, value, null);
                                break;
                            }
                        }
                    }
                    
                    // 트리
                    else if (f instanceof TreeBin) {
                        Node<K,V> p;
                        binCount = 2;
                        if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key, value)) != null) {
                            oldVal = p.val;
                            if (!onlyIfAbsent)
                                p.val = value;
                        }
                    }
                }
            }  // ⭐ synchronized 끝
            
            // 트리화 체크
            if (binCount != 0) {
                if (binCount >= TREEIFY_THRESHOLD)
                    treeifyBin(tab, i);
                if (oldVal != null)
                    return oldVal;
                break;
            }
        }
    }
    
    addCount(1L, binCount);
    return null;
}
```

**핵심 메커니즘:**
```
1. 빈 버킷: CAS (Lock-Free)
   → compareAndSwapObject (Unsafe)
   → 원자적 연산

2. 버킷 있음: synchronized (버킷 헤드만)
   → 다른 버킷은 동시 접근 가능
   → 높은 동시성

3. 리사이징 중: 협력 (helpTransfer)
   → 모든 스레드가 도움
   → 빠른 리사이징
```

---

### 5.3 get() 동작 (Lock-Free)

```java
// ConcurrentHashMap.get
public V get(Object key) {
    Node<K,V>[] tab; 
    Node<K,V> e, p; 
    int n, eh; 
    K ek;
    
    int h = spread(key.hashCode());
    
    // ⭐ 락 없이 읽기!
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (e = tabAt(tab, (n - 1) & h)) != null) {
        
        // 첫 노드 확인
        if ((eh = e.hash) == h) {
            if ((ek = e.key) == key || (ek != null && key.equals(ek)))
                return e.val;  // ⭐ volatile 읽기
        }
        
        // 포워딩 노드 또는 트리
        else if (eh < 0)
            return (p = e.find(h, key)) != null ? p.val : null;
        
        // 연결 리스트 순회
        while ((e = e.next) != null) {
            if (e.hash == h &&
                ((ek = e.key) == key || (ek != null && key.equals(ek))))
                return e.val;
        }
    }
    return null;
}

// Unsafe를 이용한 volatile 읽기
static final <K,V> Node<K,V> tabAt(Node<K,V>[] tab, int i) {
    return (Node<K,V>)U.getObjectVolatile(tab, ((long)i << ASHIFT) + ABASE);
}
```

**Lock-Free 가능 이유:**
```
1. volatile 필드
   → val, next 모두 volatile
   → 메모리 가시성 보장
   → happens-before 관계

2. 불변 해시/키
   → hash, key는 final
   → 변경 불가

3. 원자적 참조
   → Node 참조 자체가 원자적
   → 중간 상태 없음
```

---

### 5.4 CAS (Compare-And-Swap)

```java
// ConcurrentHashMap에서 사용하는 CAS

// 버킷에 노드 삽입 (원자적)
static final <K,V> boolean casTabAt(Node<K,V>[] tab, int i,
                                     Node<K,V> c, Node<K,V> v) {
    return U.compareAndSwapObject(tab, ((long)i << ASHIFT) + ABASE, c, v);
}

// 동작:
// if (tab[i] == c) {
//     tab[i] = v;
//     return true;
// }
// return false;

// ⭐ 이 모든 게 하드웨어 수준에서 원자적!
```

**사용 예:**

```java
Node<K,V> newNode = new Node<>(hash, key, value, null);

// ⭐ CAS 시도
if (casTabAt(tab, i, null, newNode)) {
    // 성공: 버킷이 비어있었고, newNode 삽입됨
    break;
} else {
    // 실패: 다른 스레드가 먼저 삽입함
    // 재시도 (for 루프 계속)
}
```

---

### 5.5 성능 비교

```java
// 벤치마크: 10개 스레드, 각 100만 회 put/get

// HashMap + synchronized
synchronized (map) {
    map.put(key, value);
}
// 시간: ~5000ms
// 이유: 전체 락, 한 번에 하나

// Hashtable
table.put(key, value);
// 시간: ~4500ms
// 이유: 메서드 synchronized

// Collections.synchronizedMap
syncMap.put(key, value);
// 시간: ~4500ms
// 이유: Hashtable과 동일

// ConcurrentHashMap
concMap.put(key, value);
// 시간: ~500ms ⭐
// 이유: 버킷 단위 락, CAS, Lock-Free 읽기

// 개선: 약 9배!
```

---

## 6. 추가 최적화

### 6.1 @Stable 어노테이션

```java
static final class List12<E> extends AbstractImmutableList<E> {
    private final E e0;
    @Stable  // ⭐ JVM 최적화 힌트
    private final E e1;
}
```

**효과:**
- JIT 컴파일러에게 "이 필드는 안정적 (변하지 않음)" 힌트
- 더 공격적인 최적화 가능
- 상수 폴딩, 인라이닝 등

---

### 6.2 메서드 인라이닝

```java
// List.of(1, 2, 3) 호출

// 컴파일 전:
List<Integer> list = List.of(1, 2, 3);

// JIT 컴파일 후 (인라이닝):
List<Integer> list = new List12<>(
    Objects.requireNonNull(1),
    Objects.requireNonNull(2)
);
// 메서드 호출 오버헤드 제거!
```

---

## 7. 핵심 정리

### 컬렉션 팩토리

```
최적화:
1. 0~10개 전용 클래스 (배열 할당 없음)
2. 불변 → 최적화 가능
3. @Stable → JIT 힌트
4. 메서드 인라이닝

트레이드오프:
- 작은 컬렉션: 매우 빠름 ⭐⭐⭐⭐⭐
- 큰 컬렉션 (≥ 10): 가변 인수 사용 (배열 할당)
```

### removeIf/replaceAll

```
최적화:
1. BitSet 사용 (메모리 효율)
2. 2패스 알고리즘 (한 번에 압축)
3. modCount 한 번만 증가

안전성:
- ConcurrentModificationException 없음
- Iterator 불필요
```

### HashMap Treeification

```
조건:
1. 버킷 ≥ 8개
2. 테이블 ≥ 64

효과:
- O(n) → O(log n)
- 최악의 경우 700배 개선

비용:
- TreeNode 생성 (메모리)
- 트리 구성 (시간)
```

### ConcurrentHashMap

```
동시성 제어:
1. CAS (Lock-Free 삽입)
2. synchronized (버킷 헤드만)
3. volatile (Lock-Free 읽기)

성능:
- HashMap + lock보다 9배 빠름
- Hashtable보다 9배 빠름
- 높은 동시성 ⭐⭐⭐⭐⭐
```

---

**작성일**: 2024년 12월  
**대상**: Modern Java In Action Chapter 8
