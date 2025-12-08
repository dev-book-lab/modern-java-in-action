package chapter08.code;

import static java.util.Map.entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Map 계산/삭제/교체/합침 패턴 완벽 가이드
 * 
 * 목표:
 * - computeIfAbsent, computeIfPresent, compute 차이 이해
 * - remove, replace 변형 이해
 * - merge 활용법 마스터
 * - 실전 패턴 학습
 */
public class MapPatterns {

  public static void main(String[] args) {
    System.out.println("=".repeat(80));
    System.out.println("Map 계산/삭제/교체/합침 패턴 완벽 가이드");
    System.out.println("=".repeat(80));

    demonstrateComputePatterns();
    demonstrateRemovePatterns();
    demonstrateReplacePatterns();
    demonstrateMergePatterns();
    realWorldExamples();

    System.out.println("\n" + "=".repeat(80));
    System.out.println("💡 핵심 정리:");
    System.out.println("   - computeIfAbsent: 캐시, 그룹핑");
    System.out.println("   - merge: 카운터, 합산");
    System.out.println("   - remove(k, v): Thread-Safe 조건부 제거");
    System.out.println("   - replace(k, old, new): Thread-Safe 조건부 교체");
    System.out.println("=".repeat(80));
  }

  /**
   * 1. 계산 패턴
   */
  private static void demonstrateComputePatterns() {
    System.out.println("\n1️⃣  계산 패턴 (compute*)\n");

    // computeIfAbsent
    System.out.println("📌 computeIfAbsent - 키 없으면 계산:");
    Map<String, Integer> map1 = new HashMap<>();
    map1.put("A", 1);
    
    System.out.println("   초기: " + map1);
    
    int value1 = map1.computeIfAbsent("B", k -> {
      System.out.println("   → 함수 실행: 'B' 키 생성");
      return 2;
    });
    System.out.println("   'B' 추가 후: " + map1 + ", 반환값: " + value1);
    
    int value2 = map1.computeIfAbsent("A", k -> {
      System.out.println("   → 함수 실행: 'A' 키 생성");
      return 999;
    });
    System.out.println("   'A' 재시도: " + map1 + ", 반환값: " + value2);
    System.out.println("   → 함수 실행 안 함! (키 이미 존재)");

    // computeIfPresent
    System.out.println("\n📌 computeIfPresent - 키 있으면 계산:");
    Map<String, Integer> map2 = new HashMap<>();
    map2.put("A", 100);
    
    System.out.println("   초기: " + map2);
    
    Integer result1 = map2.computeIfPresent("A", (k, v) -> {
      System.out.println("   → 함수 실행: 'A' 값 수정 (100 → 110)");
      return v + 10;
    });
    System.out.println("   'A' 수정 후: " + map2 + ", 반환값: " + result1);
    
    Integer result2 = map2.computeIfPresent("B", (k, v) -> {
      System.out.println("   → 함수 실행: 'B' 값 수정");
      return v + 10;
    });
    System.out.println("   'B' 시도: " + map2 + ", 반환값: " + result2);
    System.out.println("   → 함수 실행 안 함! (키 없음)");

    // compute
    System.out.println("\n📌 compute - 항상 계산:");
    Map<String, Integer> map3 = new HashMap<>();
    map3.put("A", 5);
    
    System.out.println("   초기: " + map3);
    
    Integer result3 = map3.compute("A", (k, v) -> {
      System.out.println("   → 함수 실행: 'A' (기존값: " + v + ")");
      return v == null ? 1 : v + 1;
    });
    System.out.println("   'A' 계산: " + map3 + ", 반환값: " + result3);
    
    Integer result4 = map3.compute("B", (k, v) -> {
      System.out.println("   → 함수 실행: 'B' (기존값: " + v + ")");
      return v == null ? 1 : v + 1;
    });
    System.out.println("   'B' 계산: " + map3 + ", 반환값: " + result4);

    // 비교 표
    System.out.println("\n📌 비교 요약:");
    System.out.println("   ┌────────────────────┬──────────┬──────────┐");
    System.out.println("   │      메서드         │ 키 없음  │ 키 있음  │");
    System.out.println("   ├────────────────────┼──────────┼──────────┤");
    System.out.println("   │ computeIfAbsent    │ 계산 ✅  │ 무시 ❌  │");
    System.out.println("   │ computeIfPresent   │ 무시 ❌  │ 계산 ✅  │");
    System.out.println("   │ compute            │ 계산 ✅  │ 계산 ✅  │");
    System.out.println("   └────────────────────┴──────────┴──────────┘");
  }

  /**
   * 2. 삭제 패턴
   */
  private static void demonstrateRemovePatterns() {
    System.out.println("\n2️⃣  삭제 패턴\n");

    // remove(key)
    System.out.println("📌 remove(key) - 일반 제거:");
    Map<String, String> map1 = new HashMap<>();
    map1.put("session123", "user-alice");
    map1.put("session456", "user-bob");
    
    System.out.println("   초기: " + map1);
    
    String removed = map1.remove("session123");
    System.out.println("   제거 후: " + map1);
    System.out.println("   제거된 값: " + removed);

    // remove(key, value) - Thread-Safe
    System.out.println("\n📌 remove(key, value) - 조건부 제거 (Thread-Safe):");
    Map<String, String> map2 = new HashMap<>();
    map2.put("session123", "user-alice");
    map2.put("session456", "user-bob");
    
    System.out.println("   초기: " + map2);
    
    boolean removed1 = map2.remove("session123", "user-alice");
    System.out.println("   제거 시도 (일치): " + removed1 + " → " + map2);
    
    boolean removed2 = map2.remove("session456", "user-wrong");
    System.out.println("   제거 시도 (불일치): " + removed2 + " → " + map2);

    // Race Condition 시나리오
    System.out.println("\n📌 멀티스레드 시나리오:");
    System.out.println("   ❌ 잘못된 방식 (Race Condition):");
    System.out.println("      if (map.get(key).equals(value)) {  // 시점 1");
    System.out.println("          map.remove(key);                 // 시점 2");
    System.out.println("      }");
    System.out.println("      → 사이에 다른 스레드가 값 변경 가능!");
    System.out.println("\n   ✅ 올바른 방식 (원자적):");
    System.out.println("      map.remove(key, value);");
    System.out.println("      → 하나의 연산으로 처리!");
  }

  /**
   * 3. 교체 패턴
   */
  private static void demonstrateReplacePatterns() {
    System.out.println("\n3️⃣  교체 패턴\n");

    // replace(key, value)
    System.out.println("📌 replace(key, value) - 키 있으면 교체:");
    Map<String, Integer> map1 = new HashMap<>();
    map1.put("Alice", 25);
    
    System.out.println("   초기: " + map1);
    
    Integer old1 = map1.replace("Alice", 26);
    System.out.println("   교체 (키 있음): " + map1 + ", 이전값: " + old1);
    
    Integer old2 = map1.replace("Bob", 30);
    System.out.println("   교체 시도 (키 없음): " + map1 + ", 이전값: " + old2);

    // replace(key, oldValue, newValue) - CAS
    System.out.println("\n📌 replace(key, oldValue, newValue) - CAS:");
    Map<String, Integer> map2 = new HashMap<>();
    map2.put("counter", 100);
    
    System.out.println("   초기: " + map2);
    
    boolean success1 = map2.replace("counter", 100, 101);
    System.out.println("   교체 시도 (100 → 101): " + success1 + " → " + map2);
    
    boolean success2 = map2.replace("counter", 100, 102);
    System.out.println("   교체 시도 (100 → 102): " + success2 + " → " + map2);
    System.out.println("   → 값 불일치 (101 ≠ 100), 교체 실패!");

    // replaceAll
    System.out.println("\n📌 replaceAll - 모든 값 변환:");
    Map<String, Integer> prices = new HashMap<>();
    prices.put("apple", 1000);
    prices.put("banana", 500);
    prices.put("cherry", 2000);
    
    System.out.println("   초기: " + prices);
    
    prices.replaceAll((item, price) -> (int)(price * 1.1));
    System.out.println("   10% 인상: " + prices);

    // 조건부 replaceAll
    System.out.println("\n📌 조건부 replaceAll:");
    Map<String, Integer> prices2 = new HashMap<>();
    prices2.put("apple", 1000);
    prices2.put("banana", 500);
    prices2.put("cherry", 2000);
    
    System.out.println("   초기: " + prices2);
    
    prices2.replaceAll((item, price) -> 
        price >= 1000 ? (int)(price * 0.9) : price
    );
    System.out.println("   1000원 이상만 10% 할인: " + prices2);
  }

  /**
   * 4. 합침 패턴
   */
  private static void demonstrateMergePatterns() {
    System.out.println("\n4️⃣  합침 패턴 (merge)\n");

    // 기본 사용
    System.out.println("📌 merge 기본 사용:");
    Map<String, Integer> map1 = new HashMap<>();
    
    System.out.println("   초기: " + map1);
    
    map1.merge("apple", 1, Integer::sum);
    System.out.println("   'apple' 추가: " + map1);
    
    map1.merge("apple", 1, Integer::sum);
    System.out.println("   'apple' 증가: " + map1);
    
    map1.merge("apple", 3, Integer::sum);
    System.out.println("   'apple' +3: " + map1);

    // 단어 빈도 카운트
    System.out.println("\n📌 단어 빈도 카운트:");
    List<String> words = List.of("apple", "banana", "apple", "cherry", "banana", "apple");
    Map<String, Integer> wordCount = new HashMap<>();
    
    System.out.println("   단어: " + words);
    
    for (String word : words) {
      wordCount.merge(word, 1, Integer::sum);
    }
    System.out.println("   빈도: " + wordCount);

    // Map 병합
    System.out.println("\n📌 Map 병합:");
    Map<String, String> family = new HashMap<>(Map.ofEntries(
        entry("Teo", "Star Wars"),
        entry("Cristina", "James Bond")
    ));
    Map<String, String> friends = Map.ofEntries(
        entry("Raphael", "Star Wars"),
        entry("Cristina", "Matrix")  // 중복 키!
    );
    
    System.out.println("   family: " + family);
    System.out.println("   friends: " + friends);
    
    friends.forEach((k, v) -> 
        family.merge(k, v, (movie1, movie2) -> movie1 + " & " + movie2)
    );
    System.out.println("   병합 후: " + family);
    System.out.println("   → 'Cristina'의 영화가 합쳐짐!");

    // 조건부 제거 (null 반환)
    System.out.println("\n📌 조건부 제거 (null 반환):");
    Map<String, Integer> inventory = new HashMap<>();
    inventory.put("apple", 10);
    inventory.put("banana", 3);
    
    System.out.println("   초기: " + inventory);
    
    // 재고 감소 (0 이하면 제거)
    inventory.merge("apple", -3, (current, delta) -> {
      int newQty = current + delta;
      System.out.println("   'apple': " + current + " + " + delta + " = " + newQty);
      return newQty > 0 ? newQty : null;
    });
    System.out.println("   결과: " + inventory);
    
    inventory.merge("banana", -5, (current, delta) -> {
      int newQty = current + delta;
      System.out.println("   'banana': " + current + " + " + delta + " = " + newQty);
      return newQty > 0 ? newQty : null;  // 0 이하 → null → 제거!
    });
    System.out.println("   결과: " + inventory);
  }

  /**
   * 5. 실전 예제
   */
  private static void realWorldExamples() {
    System.out.println("\n5️⃣  실전 예제\n");

    // 1. 멀티맵 (그룹핑)
    System.out.println("📌 멀티맵 (그룹핑):");
    Map<String, List<String>> groupMap = new HashMap<>();
    
    groupMap.computeIfAbsent("fruits", k -> new ArrayList<>()).add("apple");
    groupMap.computeIfAbsent("fruits", k -> new ArrayList<>()).add("banana");
    groupMap.computeIfAbsent("vegetables", k -> new ArrayList<>()).add("carrot");
    
    System.out.println("   " + groupMap);

    // 2. 카운터
    System.out.println("\n📌 카운터 (이벤트 집계):");
    List<String> events = List.of("login", "logout", "login", "purchase", "login");
    Map<String, Integer> eventCount = new HashMap<>();
    
    events.forEach(event -> eventCount.merge(event, 1, Integer::sum));
    System.out.println("   " + eventCount);

    // 3. 합산
    System.out.println("\n📌 합산 (매출 집계):");
    Map<String, Double> sales = new HashMap<>();
    
    sales.merge("product-A", 100.0, Double::sum);
    sales.merge("product-B", 200.0, Double::sum);
    sales.merge("product-A", 150.0, Double::sum);
    
    System.out.println("   " + sales);

    // 4. 최댓값 추적
    System.out.println("\n📌 최댓값 추적:");
    Map<String, Integer> maxScores = new HashMap<>();
    
    maxScores.merge("Alice", 80, Integer::max);
    maxScores.merge("Alice", 95, Integer::max);
    maxScores.merge("Bob", 70, Integer::max);
    maxScores.merge("Alice", 85, Integer::max);
    
    System.out.println("   " + maxScores);
  }

  /**
   * merge 동작 원리:
   * 
   * map.merge(key, value, remappingFunction)
   * 
   * 1. 키 없음:
   *    map.put(key, value);
   *    return value;
   * 
   * 2. 키 있음:
   *    V newValue = remappingFunction.apply(oldValue, value);
   *    if (newValue != null) {
   *        map.put(key, newValue);
   *    } else {
   *        map.remove(key);  // null → 제거!
   *    }
   *    return newValue;
   */

  /**
   * 패턴 선택 가이드:
   * 
   * ✅ computeIfAbsent:
   *    - 캐시
   *    - 그룹핑 (멀티맵)
   *    - 초기화 필요
   * 
   * ✅ computeIfPresent:
   *    - 조건부 업데이트
   *    - 보너스 적용
   *    - null로 제거
   * 
   * ✅ compute:
   *    - 키 유무 상관없이 계산
   *    - 카운터 (간단)
   * 
   * ✅ merge:
   *    - 카운터 (최선!)
   *    - 합산
   *    - 최댓값/최솟값
   *    - Map 병합
   * 
   * ✅ remove(k, v):
   *    - Thread-Safe 제거
   *    - 세션 관리
   * 
   * ✅ replace(k, old, new):
   *    - Thread-Safe 교체
   *    - CAS 패턴
   */

}
