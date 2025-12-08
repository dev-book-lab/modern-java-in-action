package chapter08.code;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

/**
 * 리스트/집합 처리 메서드 완벽 가이드
 * 
 * 목표:
 * - removeIf, replaceAll, sort 사용법 마스터
 * - ConcurrentModificationException 이해
 * - 기존 방식과 새 방식 비교
 * - 성능 차이 측정
 */
public class ListSetProcessing {

  public static void main(String[] args) {
    System.out.println("=".repeat(80));
    System.out.println("리스트/집합 처리 메서드 완벽 가이드");
    System.out.println("=".repeat(80));

    demonstrateRemoveIf();
    demonstrateConcurrentModification();
    demonstrateReplaceAll();
    demonstrateSort();
    comparePerformance();

    System.out.println("\n" + "=".repeat(80));
    System.out.println("💡 핵심 정리:");
    System.out.println("   - removeIf: 조건부 제거, ConcurrentModificationException 없음");
    System.out.println("   - replaceAll: 원본 변경, Stream.map보다 효율적");
    System.out.println("   - sort: 인스턴스 메서드, Collections.sort보다 현대적");
    System.out.println("=".repeat(80));
  }

  /**
   * 1. removeIf 데모
   */
  private static void demonstrateRemoveIf() {
    System.out.println("\n1️⃣  removeIf - 조건부 제거\n");

    // 테스트 데이터
    List<String> codes = new ArrayList<>(Arrays.asList(
        "test123", "prod456", "test789", "dev111", "test222"
    ));

    System.out.println("📌 초기 데이터:");
    System.out.println("   " + codes);

    // ❌ 기존 방식 1: Iterator 사용 (복잡)
    System.out.println("\n📌 기존 방식 1 - Iterator (복잡):");
    List<String> codes1 = new ArrayList<>(codes);
    for (Iterator<String> it = codes1.iterator(); it.hasNext(); ) {
      String code = it.next();
      if (code.startsWith("test")) {
        it.remove();
      }
    }
    System.out.println("   결과: " + codes1);
    System.out.println("   문제점: 5줄 코드, 읽기 어려움");

    // ✅ 새 방식: removeIf (간결)
    System.out.println("\n📌 새 방식 - removeIf (간결):");
    List<String> codes2 = new ArrayList<>(codes);
    codes2.removeIf(code -> code.startsWith("test"));
    System.out.println("   결과: " + codes2);
    System.out.println("   장점: 1줄 코드, 명확한 의도");

    // 다양한 조건
    System.out.println("\n📌 다양한 조건 예제:");
    
    List<Integer> numbers = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    System.out.println("   원본: " + numbers);
    
    numbers.removeIf(n -> n % 2 == 0);
    System.out.println("   짝수 제거: " + numbers);
    
    List<String> words = new ArrayList<>(Arrays.asList("a", "bb", "ccc", "dddd"));
    System.out.println("\n   원본: " + words);
    
    words.removeIf(w -> w.length() > 2);
    System.out.println("   길이 > 2 제거: " + words);
  }

  /**
   * 2. ConcurrentModificationException 데모
   */
  private static void demonstrateConcurrentModification() {
    System.out.println("\n2️⃣  ConcurrentModificationException 이해\n");

    List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E"));

    // ❌ 잘못된 방식: for-each + remove
    System.out.println("📌 잘못된 방식 - for-each + remove:");
    System.out.println("   초기: " + list);
    
    try {
      for (String item : list) {
        if (item.equals("C")) {
          list.remove(item);  // ConcurrentModificationException!
        }
      }
      System.out.println("   결과: " + list);
    } catch (ConcurrentModificationException e) {
      System.out.println("   ❌ ConcurrentModificationException 발생!");
      System.out.println("   원인: Iterator와 List 상태 불일치");
    }

    // ✅ 올바른 방식 1: Iterator.remove
    System.out.println("\n📌 올바른 방식 1 - Iterator.remove:");
    List<String> list1 = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E"));
    for (Iterator<String> it = list1.iterator(); it.hasNext(); ) {
      String item = it.next();
      if (item.equals("C")) {
        it.remove();  // ✅ 안전
      }
    }
    System.out.println("   결과: " + list1);

    // ✅ 올바른 방식 2: removeIf
    System.out.println("\n📌 올바른 방식 2 - removeIf:");
    List<String> list2 = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E"));
    list2.removeIf(item -> item.equals("C"));
    System.out.println("   결과: " + list2);

    // ✅ 올바른 방식 3: 역방향 순회
    System.out.println("\n📌 올바른 방식 3 - 역방향 순회:");
    List<String> list3 = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E"));
    for (int i = list3.size() - 1; i >= 0; i--) {
      if (list3.get(i).equals("C")) {
        list3.remove(i);  // ✅ 안전 (인덱스가 영향 안 받음)
      }
    }
    System.out.println("   결과: " + list3);
    System.out.println("   이유: 뒤에서 삭제 → 앞 인덱스 영향 없음");
  }

  /**
   * 3. replaceAll 데모
   */
  private static void demonstrateReplaceAll() {
    System.out.println("\n3️⃣  replaceAll - 요소 변환\n");

    List<String> codes = new ArrayList<>(Arrays.asList("a12", "C14", "b13"));

    System.out.println("📌 초기 데이터:");
    System.out.println("   " + codes);

    // ❌ 기존 방식 1: Stream (새 리스트 생성)
    System.out.println("\n📌 기존 방식 1 - Stream (새 리스트):");
    List<String> codes1 = codes.stream()
        .map(code -> Character.toUpperCase(code.charAt(0)) + code.substring(1))
        .collect(Collectors.toList());
    System.out.println("   새 리스트: " + codes1);
    System.out.println("   원본: " + codes + " (변경 없음)");
    System.out.println("   문제점: 새 리스트 생성 (메모리)");

    // ❌ 기존 방식 2: ListIterator (복잡)
    System.out.println("\n📌 기존 방식 2 - ListIterator (복잡):");
    List<String> codes2 = new ArrayList<>(codes);
    for (ListIterator<String> it = codes2.listIterator(); it.hasNext(); ) {
      String code = it.next();
      it.set(Character.toUpperCase(code.charAt(0)) + code.substring(1));
    }
    System.out.println("   결과: " + codes2);
    System.out.println("   문제점: 5줄 코드, 읽기 어려움");

    // ✅ 새 방식: replaceAll (간결)
    System.out.println("\n📌 새 방식 - replaceAll (간결):");
    List<String> codes3 = new ArrayList<>(codes);
    codes3.replaceAll(code -> Character.toUpperCase(code.charAt(0)) + code.substring(1));
    System.out.println("   결과: " + codes3);
    System.out.println("   장점: 1줄 코드, 원본 변경, 메모리 효율");

    // 다양한 변환
    System.out.println("\n📌 다양한 변환 예제:");
    
    List<String> words = new ArrayList<>(Arrays.asList("hello", "world"));
    System.out.println("   원본: " + words);
    
    words.replaceAll(String::toUpperCase);
    System.out.println("   대문자: " + words);
    
    List<Integer> numbers = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
    System.out.println("\n   원본: " + numbers);
    
    numbers.replaceAll(n -> n * n);
    System.out.println("   제곱: " + numbers);
  }

  /**
   * 4. sort 데모
   */
  private static void demonstrateSort() {
    System.out.println("\n4️⃣  sort - 정렬\n");

    List<String> names = new ArrayList<>(Arrays.asList("Charlie", "Alice", "Bob"));

    System.out.println("📌 초기 데이터:");
    System.out.println("   " + names);

    // ❌ 기존 방식: Collections.sort (정적 메서드)
    System.out.println("\n📌 기존 방식 - Collections.sort:");
    List<String> names1 = new ArrayList<>(names);
    java.util.Collections.sort(names1);
    System.out.println("   결과: " + names1);

    // ✅ 새 방식: List.sort (인스턴스 메서드)
    System.out.println("\n📌 새 방식 - List.sort:");
    List<String> names2 = new ArrayList<>(names);
    names2.sort(java.util.Comparator.naturalOrder());
    System.out.println("   결과: " + names2);
    System.out.println("   장점: 메서드 체이닝 가능");

    // 역순 정렬
    System.out.println("\n📌 역순 정렬:");
    List<String> names3 = new ArrayList<>(names);
    names3.sort(java.util.Comparator.reverseOrder());
    System.out.println("   결과: " + names3);

    // 커스텀 정렬
    System.out.println("\n📌 커스텀 정렬 (길이 기준):");
    List<String> words = new ArrayList<>(Arrays.asList("apple", "pie", "banana"));
    System.out.println("   원본: " + words);
    
    words.sort((a, b) -> a.length() - b.length());
    System.out.println("   길이순: " + words);
  }

  /**
   * 5. 성능 비교
   */
  private static void comparePerformance() {
    System.out.println("\n5️⃣  성능 비교 (100만 개, 절반 제거)\n");

    int size = 1_000_000;
    int iterations = 10;

    // Iterator.remove
    long time1 = 0;
    for (int iter = 0; iter < iterations; iter++) {
      List<Integer> list = new ArrayList<>();
      for (int i = 0; i < size; i++) list.add(i);
      
      long start = System.nanoTime();
      Iterator<Integer> it = list.iterator();
      while (it.hasNext()) {
        if (it.next() % 2 == 0) {
          it.remove();
        }
      }
      time1 += (System.nanoTime() - start);
    }
    time1 /= (iterations * 1_000_000);

    // removeIf
    long time2 = 0;
    for (int iter = 0; iter < iterations; iter++) {
      List<Integer> list = new ArrayList<>();
      for (int i = 0; i < size; i++) list.add(i);
      
      long start = System.nanoTime();
      list.removeIf(n -> n % 2 == 0);
      time2 += (System.nanoTime() - start);
    }
    time2 /= (iterations * 1_000_000);

    System.out.println("   Iterator.remove: " + time1 + " ms");
    System.out.println("   removeIf:        " + time2 + " ms ⭐");
    System.out.println("\n   개선: " + String.format("%.1f%%", (1 - (double) time2 / time1) * 100));
  }

  /**
   * removeIf 내부 동작 (단순화):
   * 
   * public boolean removeIf(Predicate<E> filter) {
   *     BitSet removeSet = new BitSet(size);
   *     
   *     // 1. 삭제할 요소 찾기
   *     for (int i = 0; i < size; i++) {
   *         if (filter.test(elementData[i])) {
   *             removeSet.set(i);
   *         }
   *     }
   *     
   *     // 2. 배열 압축
   *     int w = 0;
   *     for (int i = 0; i < size; i++) {
   *         if (!removeSet.get(i)) {
   *             elementData[w++] = elementData[i];
   *         }
   *     }
   *     
   *     // 3. modCount 한 번만 증가
   *     modCount++;
   *     
   *     return true;
   * }
   * 
   * 장점:
   * - BitSet 사용 (메모리 효율)
   * - 2패스 알고리즘 (한 번에 압축)
   * - modCount 한 번만 증가 (ConcurrentModificationException 없음)
   */

  /**
   * ConcurrentModificationException 원리:
   * 
   * ArrayList 내부:
   * - modCount: 수정 횟수 카운터
   * 
   * Iterator 내부:
   * - expectedModCount: 생성 시점의 modCount 저장
   * 
   * next() 호출 시:
   * if (modCount != expectedModCount) {
   *     throw new ConcurrentModificationException();
   * }
   * 
   * 문제 시나리오:
   * 1. Iterator 생성 (expectedModCount = 0)
   * 2. list.remove() 호출 (modCount = 1)
   * 3. iterator.next() 호출 (modCount != expectedModCount) → 예외!
   * 
   * removeIf는 안전:
   * - Iterator 사용 안 함
   * - modCount는 마지막에 한 번만 증가
   */

}
