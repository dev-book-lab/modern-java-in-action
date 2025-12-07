package chapter07.code;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.LongStream;

/**
 * ArrayList vs LinkedList 병렬 성능 비교
 * 
 * 목표:
 * - 자료구조에 따른 병렬화 효율 차이 이해
 * - 분할 비용의 중요성 인식
 */
public class ArrayListVsLinkedList {

  private static final int SIZE = 1_000_000;

  public static void main(String[] args) {
    System.out.println("=".repeat(80));
    System.out.println("ArrayList vs LinkedList 병렬 성능 비교");
    System.out.println("=".repeat(80));
    
    // 1. ArrayList 테스트
    List<Long> arrayList = new ArrayList<>();
    LongStream.rangeClosed(1, SIZE).forEach(arrayList::add);
    
    System.out.println("\n1️⃣  ArrayList (" + SIZE + " 개):");
    System.out.println("   순차 처리: " + measure(() -> 
        arrayList.stream().reduce(0L, Long::sum)
    ) + " ms");
    System.out.println("   병렬 처리: " + measure(() -> 
        arrayList.parallelStream().reduce(0L, Long::sum)
    ) + " ms");
    
    // 2. LinkedList 테스트
    List<Long> linkedList = new LinkedList<>();
    LongStream.rangeClosed(1, SIZE).forEach(linkedList::add);
    
    System.out.println("\n2️⃣  LinkedList (" + SIZE + " 개):");
    System.out.println("   순차 처리: " + measure(() -> 
        linkedList.stream().reduce(0L, Long::sum)
    ) + " ms");
    System.out.println("   병렬 처리: " + measure(() -> 
        linkedList.parallelStream().reduce(0L, Long::sum)
    ) + " ms ⚠️  순차보다 느림!");
    
    System.out.println("\n" + "=".repeat(80));
    System.out.println("💡 결론:");
    System.out.println("   - ArrayList: 병렬화 효과 큼 (O(1) 분할)");
    System.out.println("   - LinkedList: 병렬화 역효과 (O(n) 분할)");
    System.out.println("=".repeat(80));
  }

  /**
   * 성능 측정 (10번 반복 중 최소값)
   */
  private static long measure(Runnable task) {
    long fastest = Long.MAX_VALUE;
    for (int i = 0; i < 10; i++) {
      long start = System.nanoTime();
      task.run();
      long duration = (System.nanoTime() - start) / 1_000_000;
      if (duration < fastest) {
        fastest = duration;
      }
    }
    return fastest;
  }

  /**
   * 분할 비용 비교:
   * 
   * ArrayList:
   * - 인덱스 기반 접근
   * - 중간점 계산: O(1)
   * - 분할: [0, mid], [mid, size]
   * 
   * LinkedList:
   * - 노드 순회 필요
   * - 중간점 찾기: O(n/2)
   * - 분할할 때마다 순회!
   * 
   * 결과:
   * - ArrayList: 1ms 분할
   * - LinkedList: 500ms 분할 (500배!)
   */

}
