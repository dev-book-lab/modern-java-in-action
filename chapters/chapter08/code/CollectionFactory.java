package chapter08.code;

import static java.util.Map.entry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 컬렉션 팩토리 메서드 완벽 가이드
 * 
 * 목표:
 * - List.of, Set.of, Map.of 사용법 마스터
 * - 기존 방식과의 차이점 이해
 * - 불변 컬렉션의 특성 파악
 * - 언제 어떤 방법을 사용할지 판단
 */
public class CollectionFactory {

  public static void main(String[] args) {
    System.out.println("=".repeat(80));
    System.out.println("컬렉션 팩토리 메서드 완벽 가이드");
    System.out.println("=".repeat(80));

    demonstrateListCreation();
    demonstrateSetCreation();
    demonstrateMapCreation();
    comparePerformance();
    handleImmutability();

    System.out.println("\n" + "=".repeat(80));
    System.out.println("💡 핵심 정리:");
    System.out.println("   - List.of, Set.of, Map.of: 간결, 불변, null 불허");
    System.out.println("   - Arrays.asList: 고정 크기, 수정 가능, null 허용");
    System.out.println("   - new ArrayList: 완전 가변");
    System.out.println("   - 읽기 전용 → 팩토리 메서드");
    System.out.println("   - 수정 필요 → new ArrayList/HashMap");
    System.out.println("=".repeat(80));
  }

  /**
   * 1. 리스트 생성 방법 비교
   */
  private static void demonstrateListCreation() {
    System.out.println("\n1️⃣  리스트 생성 방법 비교\n");

    // 방법 1: 전통적 방식
    System.out.println("📌 전통적 방식 (new ArrayList):");
    List<String> traditional = new ArrayList<>();
    traditional.add("Alice");
    traditional.add("Bob");
    traditional.add("Charlie");
    System.out.println("   결과: " + traditional);
    System.out.println("   특징: 가변, null 허용, 자유로운 추가/삭제");

    // 방법 2: Arrays.asList
    System.out.println("\n📌 Arrays.asList:");
    List<String> asList = Arrays.asList("Alice", "Bob", "Charlie");
    System.out.println("   결과: " + asList);
    asList.set(0, "David");  // ✅ 수정 가능
    System.out.println("   수정 후: " + asList);
    try {
      asList.add("Eve");  // ❌ 추가 불가
    } catch (UnsupportedOperationException e) {
      System.out.println("   추가 시도: UnsupportedOperationException ❌");
    }
    System.out.println("   특징: 고정 크기, 수정 가능, null 허용");

    // 방법 3: List.of (Java 9+)
    System.out.println("\n📌 List.of (권장):");
    List<String> listOf = List.of("Alice", "Bob", "Charlie");
    System.out.println("   결과: " + listOf);
    try {
      listOf.set(0, "David");  // ❌ 수정 불가
    } catch (UnsupportedOperationException e) {
      System.out.println("   수정 시도: UnsupportedOperationException ❌");
    }
    try {
      listOf.add("Eve");  // ❌ 추가 불가
    } catch (UnsupportedOperationException e) {
      System.out.println("   추가 시도: UnsupportedOperationException ❌");
    }
    System.out.println("   특징: 완전 불변, null 불허, 최고 성능");

    // Null 처리 비교
    System.out.println("\n📌 Null 처리:");
    try {
      List<String> withNull = Arrays.asList("A", null, "C");
      System.out.println("   Arrays.asList + null: " + withNull + " ✅");
    } catch (NullPointerException e) {
      System.out.println("   Arrays.asList + null: NullPointerException ❌");
    }

    try {
      List<String> listOfNull = List.of("A", null, "C");
      System.out.println("   List.of + null: " + listOfNull + " ✅");
    } catch (NullPointerException e) {
      System.out.println("   List.of + null: NullPointerException ❌");
    }
  }

  /**
   * 2. 집합 생성 방법 비교
   */
  private static void demonstrateSetCreation() {
    System.out.println("\n2️⃣  집합 생성 방법 비교\n");

    // 방법 1: 전통적 방식
    System.out.println("📌 전통적 방식 (new HashSet):");
    Set<String> traditional = new HashSet<>();
    traditional.add("Apple");
    traditional.add("Banana");
    traditional.add("Cherry");
    System.out.println("   결과: " + traditional);

    // 방법 2: Set.of (Java 9+)
    System.out.println("\n📌 Set.of (권장):");
    Set<String> setOf = Set.of("Apple", "Banana", "Cherry");
    System.out.println("   결과: " + setOf);
    System.out.println("   특징: 불변, 중복 즉시 예외");

    // 중복 처리
    System.out.println("\n📌 중복 처리:");
    
    Set<String> hashSetDup = new HashSet<>(Arrays.asList("A", "B", "A"));
    System.out.println("   HashSet + 중복: " + hashSetDup + " (조용히 제거) ✅");

    try {
      Set<String> setOfDup = Set.of("A", "B", "A");
      System.out.println("   Set.of + 중복: " + setOfDup);
    } catch (IllegalArgumentException e) {
      System.out.println("   Set.of + 중복: IllegalArgumentException ❌");
      System.out.println("   → Fail-Fast: 버그 조기 발견!");
    }
  }

  /**
   * 3. 맵 생성 방법 비교
   */
  private static void demonstrateMapCreation() {
    System.out.println("\n3️⃣  맵 생성 방법 비교\n");

    // 방법 1: 전통적 방식
    System.out.println("📌 전통적 방식 (new HashMap):");
    Map<String, Integer> traditional = new HashMap<>();
    traditional.put("Alice", 25);
    traditional.put("Bob", 30);
    traditional.put("Charlie", 35);
    System.out.println("   결과: " + traditional);

    // 방법 2: Map.of (10개 이하)
    System.out.println("\n📌 Map.of (10개 이하):");
    Map<String, Integer> mapOf = Map.of(
        "Alice", 25,
        "Bob", 30,
        "Charlie", 35
    );
    System.out.println("   결과: " + mapOf);
    System.out.println("   특징: 간결, 불변");

    // 방법 3: Map.ofEntries (10개 초과)
    System.out.println("\n📌 Map.ofEntries (10개 초과):");
    Map<String, Integer> mapOfEntries = Map.ofEntries(
        entry("Alice", 25),
        entry("Bob", 30),
        entry("Charlie", 35),
        entry("David", 40),
        entry("Eve", 45)
    );
    System.out.println("   결과: " + mapOfEntries);
    System.out.println("   특징: 확장성, 가독성");

    // 중복 키 처리
    System.out.println("\n📌 중복 키 처리:");
    
    Map<String, Integer> hashMapDup = new HashMap<>();
    hashMapDup.put("A", 1);
    hashMapDup.put("A", 2);  // 덮어씀
    System.out.println("   HashMap + 중복 키: " + hashMapDup + " (덮어씀) ✅");

    try {
      Map<String, Integer> mapOfDup = Map.of("A", 1, "A", 2);
      System.out.println("   Map.of + 중복 키: " + mapOfDup);
    } catch (IllegalArgumentException e) {
      System.out.println("   Map.of + 중복 키: IllegalArgumentException ❌");
    }
  }

  /**
   * 4. 성능 비교
   */
  private static void comparePerformance() {
    System.out.println("\n4️⃣  성능 비교 (10개 요소, 100만 회 생성)\n");

    int iterations = 1_000_000;

    // new ArrayList
    long start = System.nanoTime();
    for (int i = 0; i < iterations; i++) {
      List<Integer> list = new ArrayList<>();
      list.add(1); list.add(2); list.add(3); list.add(4); list.add(5);
      list.add(6); list.add(7); list.add(8); list.add(9); list.add(10);
    }
    long time1 = (System.nanoTime() - start) / 1_000_000;

    // Arrays.asList
    start = System.nanoTime();
    for (int i = 0; i < iterations; i++) {
      List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }
    long time2 = (System.nanoTime() - start) / 1_000_000;

    // List.of
    start = System.nanoTime();
    for (int i = 0; i < iterations; i++) {
      List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }
    long time3 = (System.nanoTime() - start) / 1_000_000;

    System.out.println("   new ArrayList:  " + time1 + " ms");
    System.out.println("   Arrays.asList:  " + time2 + " ms");
    System.out.println("   List.of:        " + time3 + " ms ⭐ (최고!)");
    System.out.println("\n   개선: " + String.format("%.1f배", (double) time1 / time3));
  }

  /**
   * 5. 불변성 다루기
   */
  private static void handleImmutability() {
    System.out.println("\n5️⃣  불변성 다루기\n");

    // 불변 리스트
    List<String> immutable = List.of("A", "B", "C");
    System.out.println("📌 불변 리스트: " + immutable);

    // 수정 필요 시 → 가변 복사본 생성
    List<String> mutable = new ArrayList<>(immutable);
    mutable.add("D");
    System.out.println("   가변 복사본: " + mutable);
    System.out.println("   원본 유지: " + immutable);

    // 불변 맵
    Map<String, Integer> immutableMap = Map.of("A", 1, "B", 2);
    System.out.println("\n📌 불변 맵: " + immutableMap);

    // 수정 필요 시
    Map<String, Integer> mutableMap = new HashMap<>(immutableMap);
    mutableMap.put("C", 3);
    System.out.println("   가변 복사본: " + mutableMap);
    System.out.println("   원본 유지: " + immutableMap);
  }

  /**
   * 오버로딩 구조 설명:
   * 
   * List.of는 0~10개까지 전용 메서드 제공:
   * 
   * static <E> List<E> of()                          // 0개
   * static <E> List<E> of(E e1)                      // 1개
   * static <E> List<E> of(E e1, E e2)                // 2개
   * ...
   * static <E> List<E> of(E e1, ..., E e10)          // 10개
   * static <E> List<E> of(E... elements)             // 11개 이상
   * 
   * 이유:
   * - 가변 인수는 배열 할당 필요 → 느림
   * - 10개 이하는 전용 메서드 → 배열 할당 없음 → 빠름
   * - 90% 케이스가 10개 이하
   */

  /**
   * 선택 가이드:
   * 
   * ✅ List.of, Set.of, Map.of 사용:
   *    - 작은 컬렉션 (< 10개)
   *    - 불변 필요
   *    - 읽기 전용
   *    - null 불필요
   * 
   * ✅ new ArrayList, new HashMap 사용:
   *    - 수정 필요
   *    - 동적 크기
   *    - null 허용 필요
   * 
   * ✅ Arrays.asList 사용:
   *    - 배열 → 리스트 변환
   *    - 고정 크기 OK
   *    - null 허용 필요
   */

}
