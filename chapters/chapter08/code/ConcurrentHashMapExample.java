package chapter08.code;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

/**
 * ConcurrentHashMap 완벽 가이드
 * 
 * 목표:
 * - ConcurrentHashMap vs HashMap vs Hashtable 차이 이해
 * - 동시성 제어 메커니즘 학습
 * - 성능 비교
 * - 실전 활용법
 */
public class ConcurrentHashMapExample {

  private static final int THREAD_COUNT = 10;
  private static final int OPERATIONS_PER_THREAD = 100_000;

  public static void main(String[] args) throws InterruptedException {
    System.out.println("=".repeat(80));
    System.out.println("ConcurrentHashMap 완벽 가이드");
    System.out.println("=".repeat(80));

    demonstrateBasicUsage();
    demonstrateNullHandling();
    demonstrateAtomicOperations();
    demonstrateBulkOperations();
    comparePerformance();
    demonstratePitfalls();

    System.out.println("\n" + "=".repeat(80));
    System.out.println("💡 핵심 정리:");
    System.out.println("   - ConcurrentHashMap: Thread-Safe + 고성능");
    System.out.println("   - Null 불허: 명확성");
    System.out.println("   - 원자적 연산: putIfAbsent, remove(k,v), replace(k,old,new)");
    System.out.println("   - 병렬 연산: forEach, reduce, search (병렬성 기준값)");
    System.out.println("=".repeat(80));
  }

  /**
   * 1. 기본 사용법
   */
  private static void demonstrateBasicUsage() {
    System.out.println("\n1️⃣  기본 사용법\n");

    // 생성
    System.out.println("📌 생성:");
    Map<String, Integer> map = new ConcurrentHashMap<>();
    System.out.println("   생성: " + map);

    // 추가
    System.out.println("\n📌 추가:");
    map.put("Alice", 25);
    map.put("Bob", 30);
    map.put("Charlie", 35);
    System.out.println("   " + map);

    // 조회
    System.out.println("\n📌 조회:");
    Integer age = map.get("Alice");
    System.out.println("   Alice: " + age);

    // 제거
    System.out.println("\n📌 제거:");
    map.remove("Bob");
    System.out.println("   Bob 제거 후: " + map);

    // 반복
    System.out.println("\n📌 반복:");
    map.forEach((name, ageValue) -> 
        System.out.println("   " + name + ": " + ageValue)
    );
  }

  /**
   * 2. Null 처리
   */
  private static void demonstrateNullHandling() {
    System.out.println("\n2️⃣  Null 처리\n");

    System.out.println("📌 HashMap - Null 허용:");
    Map<String, Integer> hashMap = new HashMap<>();
    hashMap.put("key", null);
    hashMap.put(null, 1);
    System.out.println("   " + hashMap + " ✅");

    System.out.println("\n📌 ConcurrentHashMap - Null 불허:");
    Map<String, Integer> concMap = new ConcurrentHashMap<>();
    
    try {
      concMap.put("key", null);
      System.out.println("   null 값: ✅");
    } catch (NullPointerException e) {
      System.out.println("   null 값: NullPointerException ❌");
    }
    
    try {
      concMap.put(null, 1);
      System.out.println("   null 키: ✅");
    } catch (NullPointerException e) {
      System.out.println("   null 키: NullPointerException ❌");
    }

    System.out.println("\n📌 Null 불허 이유:");
    System.out.println("   멀티스레드 환경에서 애매모호함 제거");
    System.out.println("   ");
    System.out.println("   if (map.get(key) == null) {");
    System.out.println("       // 키가 없는 것? 값이 null인 것?");
    System.out.println("       // 다른 스레드가 삭제한 것?");
    System.out.println("   }");
    System.out.println("   ");
    System.out.println("   → Null 불허로 명확하게!");
  }

  /**
   * 3. 원자적 연산
   */
  private static void demonstrateAtomicOperations() {
    System.out.println("\n3️⃣  원자적 연산\n");

    Map<String, Integer> map = new ConcurrentHashMap<>();
    map.put("counter", 0);

    // putIfAbsent
    System.out.println("📌 putIfAbsent - Thread-Safe 추가:");
    System.out.println("   초기: " + map);
    
    Integer oldValue1 = map.putIfAbsent("counter", 100);
    System.out.println("   'counter' 추가 시도: " + map + ", 이전값: " + oldValue1);
    
    Integer oldValue2 = map.putIfAbsent("new-key", 200);
    System.out.println("   'new-key' 추가: " + map + ", 이전값: " + oldValue2);

    // remove(key, value)
    System.out.println("\n📌 remove(key, value) - Thread-Safe 제거:");
    map.put("session", 123);
    System.out.println("   초기: " + map);
    
    boolean removed1 = map.remove("session", 123);
    System.out.println("   제거 (일치): " + removed1 + " → " + map);
    
    map.put("session", 456);
    boolean removed2 = map.remove("session", 123);
    System.out.println("   제거 시도 (불일치): " + removed2 + " → " + map);

    // replace(key, oldValue, newValue)
    System.out.println("\n📌 replace(key, oldValue, newValue) - CAS:");
    map.put("counter", 100);
    System.out.println("   초기: " + map);
    
    boolean replaced1 = map.replace("counter", 100, 101);
    System.out.println("   교체 (100 → 101): " + replaced1 + " → " + map);
    
    boolean replaced2 = map.replace("counter", 100, 102);
    System.out.println("   교체 시도 (100 → 102): " + replaced2 + " → " + map);
  }

  /**
   * 4. 병렬 연산
   */
  private static void demonstrateBulkOperations() {
    System.out.println("\n4️⃣  병렬 연산\n");

    ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
    map.put("A", 10);
    map.put("B", 20);
    map.put("C", 30);
    map.put("D", 40);
    map.put("E", 50);

    long parallelismThreshold = 1;  // 병렬 최대화

    // forEach
    System.out.println("📌 forEach (병렬):");
    map.forEach(parallelismThreshold, (key, value) -> 
        System.out.println("   [" + Thread.currentThread().getName() + "] " + key + " = " + value)
    );

    // reduce
    System.out.println("\n📌 reduce (병렬):");
    Integer sum = map.reduceValues(parallelismThreshold, Integer::sum);
    System.out.println("   합계: " + sum);
    
    Integer max = map.reduceValues(parallelismThreshold, Integer::max);
    System.out.println("   최댓값: " + max);

    // search
    System.out.println("\n📌 search (병렬):");
    String result = map.search(parallelismThreshold, (key, value) -> 
        value > 30 ? key : null
    );
    System.out.println("   값 > 30인 첫 키: " + result);

    // mappingCount vs size
    System.out.println("\n📌 mappingCount vs size:");
    System.out.println("   size():          " + map.size() + " (int)");
    System.out.println("   mappingCount():  " + map.mappingCount() + " (long)");
    System.out.println("   → mappingCount 권장 (long 범위)");
  }

  /**
   * 5. 성능 비교
   */
  private static void comparePerformance() throws InterruptedException {
    System.out.println("\n5️⃣  성능 비교 (" + THREAD_COUNT + " 스레드, 각 " + 
        OPERATIONS_PER_THREAD + " 회 연산)\n");

    // HashMap + synchronized
    long time1 = benchmarkHashMapSynchronized();
    System.out.println("   HashMap + synchronized:       " + time1 + " ms");

    // Hashtable
    long time2 = benchmarkHashtable();
    System.out.println("   Hashtable:                    " + time2 + " ms");

    // Collections.synchronizedMap
    long time3 = benchmarkSynchronizedMap();
    System.out.println("   Collections.synchronizedMap:  " + time3 + " ms");

    // ConcurrentHashMap
    long time4 = benchmarkConcurrentHashMap();
    System.out.println("   ConcurrentHashMap:            " + time4 + " ms ⭐");

    System.out.println("\n   개선:");
    System.out.println("   - vs HashMap+sync: " + String.format("%.1f배", (double) time1 / time4));
    System.out.println("   - vs Hashtable:    " + String.format("%.1f배", (double) time2 / time4));
  }

  /**
   * HashMap + synchronized 벤치마크
   */
  private static long benchmarkHashMapSynchronized() throws InterruptedException {
    Map<Integer, Integer> map = new HashMap<>();
    long start = System.currentTimeMillis();

    Thread[] threads = new Thread[THREAD_COUNT];
    for (int i = 0; i < THREAD_COUNT; i++) {
      final int threadId = i;
      threads[i] = new Thread(() -> {
        for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
          int key = threadId * OPERATIONS_PER_THREAD + j;
          synchronized (map) {
            map.put(key, key);
            map.get(key);
          }
        }
      });
      threads[i].start();
    }

    for (Thread thread : threads) {
      thread.join();
    }

    return System.currentTimeMillis() - start;
  }

  /**
   * Hashtable 벤치마크
   */
  private static long benchmarkHashtable() throws InterruptedException {
    Map<Integer, Integer> map = new Hashtable<>();
    long start = System.currentTimeMillis();

    Thread[] threads = new Thread[THREAD_COUNT];
    for (int i = 0; i < THREAD_COUNT; i++) {
      final int threadId = i;
      threads[i] = new Thread(() -> {
        for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
          int key = threadId * OPERATIONS_PER_THREAD + j;
          map.put(key, key);
          map.get(key);
        }
      });
      threads[i].start();
    }

    for (Thread thread : threads) {
      thread.join();
    }

    return System.currentTimeMillis() - start;
  }

  /**
   * Collections.synchronizedMap 벤치마크
   */
  private static long benchmarkSynchronizedMap() throws InterruptedException {
    Map<Integer, Integer> map = Collections.synchronizedMap(new HashMap<>());
    long start = System.currentTimeMillis();

    Thread[] threads = new Thread[THREAD_COUNT];
    for (int i = 0; i < THREAD_COUNT; i++) {
      final int threadId = i;
      threads[i] = new Thread(() -> {
        for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
          int key = threadId * OPERATIONS_PER_THREAD + j;
          map.put(key, key);
          map.get(key);
        }
      });
      threads[i].start();
    }

    for (Thread thread : threads) {
      thread.join();
    }

    return System.currentTimeMillis() - start;
  }

  /**
   * ConcurrentHashMap 벤치마크
   */
  private static long benchmarkConcurrentHashMap() throws InterruptedException {
    Map<Integer, Integer> map = new ConcurrentHashMap<>();
    long start = System.currentTimeMillis();

    Thread[] threads = new Thread[THREAD_COUNT];
    for (int i = 0; i < THREAD_COUNT; i++) {
      final int threadId = i;
      threads[i] = new Thread(() -> {
        for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
          int key = threadId * OPERATIONS_PER_THREAD + j;
          map.put(key, key);
          map.get(key);
        }
      });
      threads[i].start();
    }

    for (Thread thread : threads) {
      thread.join();
    }

    return System.currentTimeMillis() - start;
  }

  /**
   * 6. 주의사항
   */
  private static void demonstratePitfalls() {
    System.out.println("\n6️⃣  주의사항\n");

    ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
    map.put("A", 1);
    map.put("B", 2);
    map.put("C", 3);

    // 약한 일관성
    System.out.println("📌 약한 일관성 (Weakly Consistent):");
    System.out.println("   초기: " + map);
    
    System.out.println("   forEach 중 수정:");
    map.forEach((k, v) -> {
      System.out.println("   처리 중: " + k + " = " + v);
      if (k.equals("A")) {
        map.put("D", 4);  // 반복 중 추가
        System.out.println("   → 'D' 추가됨");
      }
    });
    System.out.println("   최종: " + map);
    System.out.println("   → ConcurrentModificationException 없음!");
    System.out.println("   → 'D'가 보일 수도, 안 보일 수도 있음");

    // 복합 연산
    System.out.println("\n📌 복합 연산 주의:");
    ConcurrentHashMap<String, Integer> counter = new ConcurrentHashMap<>();
    counter.put("count", 0);
    
    System.out.println("   ❌ 잘못된 방식:");
    System.out.println("      int count = map.get(\"count\");  // 읽기");
    System.out.println("      map.put(\"count\", count + 1);   // 쓰기");
    System.out.println("      → Race Condition!");
    
    System.out.println("\n   ✅ 올바른 방식:");
    System.out.println("      map.merge(\"count\", 1, Integer::sum);");
    System.out.println("      → 원자적 연산!");

    // 크기는 근사값
    System.out.println("\n📌 크기는 근사값:");
    System.out.println("   size()는 정확하지 않을 수 있음");
    System.out.println("   → 반복 중 다른 스레드가 수정 가능");
    System.out.println("   → 완벽한 정확성 필요하면 락 사용");
  }

  /**
   * 동시성 제어 메커니즘:
   * 
   * Java 7:
   * - Segment 기반 (16개 세그먼트)
   * - ReentrantLock 사용
   * - 최대 16개 스레드 동시 쓰기
   * 
   * Java 8+:
   * - Node 기반 (버킷 단위)
   * - CAS + synchronized
   * - 무제한 동시성
   * - Treeification 지원
   * 
   * put() 동작:
   * 1. 버킷 비어있음 → CAS (Lock-Free)
   * 2. 버킷 있음 → synchronized (버킷 헤드만)
   * 3. 리사이징 중 → 협력
   * 
   * get() 동작:
   * - Lock-Free (volatile 읽기)
   * - 매우 빠름!
   */

  /**
   * 실전 활용:
   * 
   * ✅ 웹 서버 세션 관리:
   *    ConcurrentHashMap<String, Session> sessions
   * 
   * ✅ 캐시:
   *    ConcurrentHashMap<String, Data> cache
   * 
   * ✅ 이벤트 카운터:
   *    ConcurrentHashMap<String, AtomicInteger> counters
   * 
   * ✅ 설정 관리:
   *    ConcurrentHashMap<String, Config> configs
   * 
   * ✅ Rate Limiter:
   *    ConcurrentHashMap<String, RateLimit> limiters
   */

  /**
   * 선택 가이드:
   * 
   * ✅ ConcurrentHashMap:
   *    - 멀티스레드
   *    - 읽기 많음
   *    - 높은 동시성
   * 
   * ✅ HashMap:
   *    - 단일 스레드
   *    - null 허용 필요
   * 
   * ✅ Collections.synchronizedMap:
   *    - 복합 연산 많음
   *    - 강한 일관성 필요
   */

}
