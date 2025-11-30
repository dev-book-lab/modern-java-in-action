package chapter03.code;

import static java.util.Comparator.comparing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Sorting Evolution (정렬의 진화)
 * 
 * 코드 전달 → 익명 클래스 → 람다 → 메서드 참조
 * 
 * 학습 목표:
 * 1. 코드가 점점 간결해지는 과정 이해
 * 2. 각 단계의 장단점 파악
 * 3. 메서드 참조의 강력함 체험
 */
public class SortingEvolution {

  public static void main(String... args) {
    System.out.println("=== 정렬 코드의 진화 ===\n");

    // ===== 1단계: 코드 전달 (Comparator 구현 클래스) =====
    System.out.println("1단계: Comparator 구현 클래스");
    List<Apple> inventory1 = new ArrayList<>(Arrays.asList(
        new Apple(80, Color.GREEN),
        new Apple(155, Color.GREEN),
        new Apple(120, Color.RED)
    ));
    System.out.println("정렬 전: " + inventory1);
    
    // AppleComparator 클래스 사용
    inventory1.sort(new AppleComparator());
    System.out.println("정렬 후: " + inventory1);
    System.out.println("→ 별도의 클래스 필요\n");

    // ===== 2단계: 익명 클래스 =====
    System.out.println("2단계: 익명 클래스");
    List<Apple> inventory2 = new ArrayList<>(Arrays.asList(
        new Apple(80, Color.GREEN),
        new Apple(155, Color.GREEN),
        new Apple(30, Color.GREEN)
    ));
    System.out.println("정렬 전: " + inventory2);
    
    // 익명 클래스로 Comparator 구현
    inventory2.sort(new Comparator<Apple>() {
      @Override
      public int compare(Apple a1, Apple a2) {
        return a1.getWeight() - a2.getWeight();
      }
    });
    System.out.println("정렬 후: " + inventory2);
    System.out.println("→ 여전히 장황함\n");

    // ===== 3단계: 람다 표현식 (타입 명시) =====
    System.out.println("3단계: 람다 표현식 (타입 명시)");
    List<Apple> inventory3 = new ArrayList<>(Arrays.asList(
        new Apple(80, Color.GREEN),
        new Apple(20, Color.RED),
        new Apple(155, Color.GREEN)
    ));
    System.out.println("정렬 전: " + inventory3);
    
    // 람다 (타입 명시)
    inventory3.sort((Apple a1, Apple a2) -> 
        a1.getWeight() - a2.getWeight());
    System.out.println("정렬 후: " + inventory3);
    System.out.println("→ 훨씬 간결!\n");

    // ===== 4단계: 람다 표현식 (타입 추론) =====
    System.out.println("4단계: 람다 표현식 (타입 추론)");
    List<Apple> inventory4 = new ArrayList<>(Arrays.asList(
        new Apple(155, Color.GREEN),
        new Apple(10, Color.RED),
        new Apple(80, Color.GREEN)
    ));
    System.out.println("정렬 전: " + inventory4);
    
    // 람다 (타입 추론)
    inventory4.sort((a1, a2) -> 
        a1.getWeight() - a2.getWeight());
    System.out.println("정렬 후: " + inventory4);
    System.out.println("→ 더 간결!\n");

    // ===== 5단계: 메서드 참조 =====
    System.out.println("5단계: 메서드 참조");
    List<Apple> inventory5 = new ArrayList<>(Arrays.asList(
        new Apple(120, Color.RED),
        new Apple(10, Color.RED),
        new Apple(155, Color.GREEN)
    ));
    System.out.println("정렬 전: " + inventory5);
    
    // 메서드 참조 사용!
    inventory5.sort(comparing(Apple::getWeight));
    System.out.println("정렬 후: " + inventory5);
    System.out.println("→ 가장 간결하고 의미 명확!\n");

    // ===== 6단계: 역정렬 + 연결 =====
    System.out.println("6단계: 역정렬 + thenComparing");
    List<Apple> inventory6 = new ArrayList<>(Arrays.asList(
        new Apple(120, Color.RED),
        new Apple(120, Color.GREEN),  // 같은 무게
        new Apple(155, Color.GREEN)
    ));
    System.out.println("정렬 전: " + inventory6);
    
    // 무게 역순 → 같으면 색상순
    inventory6.sort(comparing(Apple::getWeight)
        .reversed()
        .thenComparing(Apple::getColor));
    System.out.println("정렬 후: " + inventory6);
    System.out.println("→ 복잡한 정렬도 쉽게!\n");
  }

  /**
   * 1단계에서 사용하는 Comparator 구현 클래스
   * 
   * 문제점:
   * - 한 번만 사용할 건데 별도 클래스 필요
   * - 코드가 분산됨
   */
  static class AppleComparator implements Comparator<Apple> {
    @Override
    public int compare(Apple a1, Apple a2) {
      return a1.getWeight() - a2.getWeight();
    }
  }

  /*
   * ===== 진화 과정 요약 =====
   * 
   * 1단계: Comparator 구현 클래스
   *   inventory.sort(new AppleComparator());
   *   문제: 별도 클래스 필요
   * 
   * 2단계: 익명 클래스
   *   inventory.sort(new Comparator<Apple>() {
   *     public int compare(Apple a1, Apple a2) {
   *       return a1.getWeight() - a2.getWeight();
   *     }
   *   });
   *   문제: 여전히 장황함
   * 
   * 3단계: 람다 (타입 명시)
   *   inventory.sort((Apple a1, Apple a2) -> 
   *       a1.getWeight() - a2.getWeight());
   *   개선: 간결해짐
   * 
   * 4단계: 람다 (타입 추론)
   *   inventory.sort((a1, a2) -> 
   *       a1.getWeight() - a2.getWeight());
   *   개선: 더 간결해짐
   * 
   * 5단계: 메서드 참조
   *   inventory.sort(comparing(Apple::getWeight));
   *   개선: 가장 간결하고 의미 명확!
   * 
   * 6단계: 조합
   *   inventory.sort(comparing(Apple::getWeight)
   *       .reversed()
   *       .thenComparing(Apple::getColor));
   *   개선: 복잡한 정렬도 쉽게!
   */
}
