package chapter03.code;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Lambda Basics (람다 기초)
 * 
 * 학습 목표:
 * 1. 람다 표현식 기본 문법
 * 2. 함수형 인터페이스 사용
 * 3. 형식 추론
 * 4. 람다로 필터링과 정렬
 * 
 * @author Modern Java In Action
 */
public class LambdaBasics {

  public static void main(String... args) {
    System.out.println("=== 람다 표현식 기초 ===\n");

    // ===== 1. 가장 간단한 람다 =====
    System.out.println("1. 가장 간단한 람다 - Runnable");
    
    // Before: 익명 클래스
    Runnable r1 = new Runnable() {
      @Override
      public void run() {
        System.out.println("Hello from anonymous class!");
      }
    };
    r1.run();
    
    // After: 람다 표현식
    Runnable r2 = () -> System.out.println("Hello from lambda!");
    r2.run();
    System.out.println();

    // ===== 2. 람다로 필터링 =====
    System.out.println("2. 람다로 사과 필터링");
    
    List<Apple> inventory = Arrays.asList(
        new Apple(80, Color.GREEN),
        new Apple(155, Color.GREEN),
        new Apple(120, Color.RED)
    );
    System.out.println("전체 사과: " + inventory);
    
    // 녹색 사과만 필터링
    List<Apple> greenApples = filter(inventory, 
        (Apple a) -> a.getColor() == Color.GREEN);
    System.out.println("녹색 사과: " + greenApples);
    
    // 무거운 사과만 필터링 (150g 이상)
    List<Apple> heavyApples = filter(inventory,
        (Apple a) -> a.getWeight() > 150);
    System.out.println("무거운 사과: " + heavyApples);
    System.out.println();

    // ===== 3. 람다로 정렬 =====
    System.out.println("3. 람다로 사과 정렬");
    
    List<Apple> inventory2 = new ArrayList<>(Arrays.asList(
        new Apple(120, Color.RED),
        new Apple(80, Color.GREEN),
        new Apple(155, Color.GREEN)
    ));
    System.out.println("정렬 전: " + inventory2);
    
    // 무게 기준 정렬 (람다 사용)
    Comparator<Apple> byWeight = 
        (Apple a1, Apple a2) -> a1.getWeight() - a2.getWeight();
    inventory2.sort(byWeight);
    System.out.println("정렬 후: " + inventory2);
    System.out.println();

    // ===== 4. 형식 추론 =====
    System.out.println("4. 형식 추론 (Type Inference)");
    
    // 타입 명시
    Comparator<Apple> c1 = 
        (Apple a1, Apple a2) -> a1.getWeight() - a2.getWeight();
    System.out.println("타입 명시: (Apple a1, Apple a2) -> ...");
    
    // 타입 추론 (컴파일러가 자동으로 추론)
    Comparator<Apple> c2 = 
        (a1, a2) -> a1.getWeight() - a2.getWeight();
    System.out.println("타입 추론: (a1, a2) -> ...");
    System.out.println("→ 컴파일러가 Apple 타입임을 추론!");
    System.out.println();

    // ===== 5. 다양한 람다 형태 =====
    System.out.println("5. 다양한 람다 형태");
    demonstrateLambdaForms();
  }

  /**
   * 사과 필터링 메서드
   * 
   * 동작 파라미터화:
   * - 필터링 조건을 람다로 전달받음
   * - ApplePredicate 인터페이스 사용
   * 
   * @param inventory 사과 리스트
   * @param p 필터링 조건 (람다)
   * @return 필터링된 사과 리스트
   */
  public static List<Apple> filter(
      List<Apple> inventory, 
      ApplePredicate p) {
    
    List<Apple> result = new ArrayList<>();
    
    // 각 사과를 검사
    for (Apple apple : inventory) {
      // 람다 실행! (조건 검사)
      if (p.test(apple)) {
        result.add(apple);
      }
    }
    
    return result;
  }

  /**
   * 다양한 람다 형태 시연
   * 
   * 람다 문법:
   * - 표현식 스타일: (파라미터) -> 표현식
   * - 블록 스타일: (파라미터) -> { 문장들; }
   */
  private static void demonstrateLambdaForms() {
    // (1) 파라미터 없음, 상수 반환
    System.out.println("(1) () -> 42");
    java.util.function.Supplier<Integer> s1 = () -> 42;
    System.out.println("    결과: " + s1.get());

    // (2) 파라미터 하나, 표현식
    System.out.println("(2) (String s) -> s.length()");
    java.util.function.Function<String, Integer> f1 =
            (String s) -> s.length();
    System.out.println("    \"Hello\" 길이: " + f1.apply("Hello"));

    // (3) 파라미터 여러 개, 표현식
    System.out.println("(3) (x, y) -> x + y");
    java.util.function.BiFunction<Integer, Integer, Integer> f2 =
            (x, y) -> x + y;  // ✅ 타입 추론 사용 (int 제거)
    System.out.println("    3 + 5 = " + f2.apply(3, 5));

    // (4) 블록 스타일 (여러 문장)
    System.out.println("(4) (x, y) -> { ... }");
    java.util.function.BiFunction<Integer, Integer, Integer> f3 =
            (x, y) -> {  // ✅ 타입 추론 사용 (int 제거)
              System.out.println("    계산 중...");
              int sum = x + y;
              return sum;
            };
    System.out.println("    10 + 20 = " + f3.apply(10, 20));
  }

  /**
   * ApplePredicate 함수형 인터페이스
   * 
   * 역할:
   * - Apple을 받아서 boolean을 반환
   * - 사과 필터링 조건 정의
   * 
   * 함수 디스크립터:
   * - (Apple) -> boolean
   * 
   * 사용 예시:
   * - a -> a.getColor() == Color.GREEN  (녹색 사과)
   * - a -> a.getWeight() > 150          (무거운 사과)
   * - a -> Color.RED.equals(a.getColor()) && a.getWeight() < 100
   */
  @FunctionalInterface
  interface ApplePredicate {
    /**
     * 사과를 검사하는 메서드
     * 
     * @param apple 검사할 사과
     * @return 조건을 만족하면 true
     */
    boolean test(Apple apple);
  }

  /*
   * ===== 핵심 정리 =====
   * 
   * 람다 표현식:
   * - (파라미터) -> 표현식
   * - (파라미터) -> { 문장들; }
   * 
   * 함수형 인터페이스:
   * - 추상 메서드가 하나인 인터페이스
   * - 람다 표현식의 타입
   * 
   * 형식 추론:
   * - 컴파일러가 컨텍스트로 타입 추론
   * - (Apple a) -> ... vs (a) -> ...
   * 
   * 동작 파라미터화:
   * - 동작(람다)을 메서드에 전달
   * - 코드의 유연성 증가
   * 
   * Before vs After:
   * Before (익명 클래스):
   *   new Comparator<Apple>() {
   *     public int compare(Apple a1, Apple a2) {
   *       return a1.getWeight() - a2.getWeight();
   *     }
   *   }
   * 
   * After (람다):
   *   (a1, a2) -> a1.getWeight() - a2.getWeight()
   * 
   * → 훨씬 간결!
   */
}
