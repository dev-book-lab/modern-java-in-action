package chapter03.code;

import static java.util.Comparator.comparing;
import java.util.*;
import java.util.function.*;

/**
 * Lambda Composition (람다 조합)
 * 
 * 학습 목표:
 * - Comparator 조합 (reversed, thenComparing)
 * - Predicate 조합 (and, or, negate)
 * - Function 조합 (andThen, compose)
 */
public class LambdaComposition {

  public static void main(String... args) {
    System.out.println("=== 람다 조합 예제 ===\n");

    comparatorComposition();
    predicateComposition();
    functionComposition();
  }

  // ===== Comparator 조합 =====
  private static void comparatorComposition() {
    System.out.println("1. Comparator 조합\n");

    List<Apple> inventory = Arrays.asList(
        new Apple(120, Color.RED),
        new Apple(120, Color.GREEN),
        new Apple(80, Color.GREEN),
        new Apple(155, Color.RED)
    );
    
    System.out.println("원본: " + inventory);
    
    // reversed(): 역정렬
    List<Apple> inv1 = new ArrayList<>(inventory);
    inv1.sort(comparing(Apple::getWeight).reversed());
    System.out.println("무게 역순: " + inv1);
    
    // thenComparing(): Comparator 연결
    List<Apple> inv2 = new ArrayList<>(inventory);
    inv2.sort(comparing(Apple::getWeight)
        .thenComparing(Apple::getColor));
    System.out.println("무게순, 같으면 색상순: " + inv2);
    
    // 복합: 역순 + 연결
    List<Apple> inv3 = new ArrayList<>(inventory);
    inv3.sort(comparing(Apple::getWeight)
        .reversed()
        .thenComparing(Apple::getColor));
    System.out.println("무게 역순, 같으면 색상순: " + inv3);
    System.out.println();
  }

  // ===== Predicate 조합 =====
  private static void predicateComposition() {
    System.out.println("2. Predicate 조합\n");

    Predicate<Apple> red = a -> Color.RED.equals(a.getColor());
    Predicate<Apple> heavy = a -> a.getWeight() > 100;
    
    List<Apple> inventory = Arrays.asList(
        new Apple(80, Color.GREEN),
        new Apple(120, Color.RED),
        new Apple(155, Color.GREEN)
    );
    
    System.out.println("전체: " + inventory);
    
    // negate(): NOT
    List<Apple> notRed = filter(inventory, red.negate());
    System.out.println("빨강 아닌 것: " + notRed);
    
    // and(): AND
    List<Apple> redAndHeavy = filter(inventory, red.and(heavy));
    System.out.println("빨강이면서 무거운 것: " + redAndHeavy);
    
    // or(): OR
    List<Apple> redOrHeavy = filter(inventory, red.or(heavy));
    System.out.println("빨강이거나 무거운 것: " + redOrHeavy);
    
    // 복합: and + or
    Predicate<Apple> complex = red.and(heavy)
        .or(a -> Color.GREEN.equals(a.getColor()));
    List<Apple> result = filter(inventory, complex);
    System.out.println("(빨강 and 무거움) or 초록: " + result);
    System.out.println();
  }

  // ===== Function 조합 =====
  private static void functionComposition() {
    System.out.println("3. Function 조합\n");

    Function<Integer, Integer> f = x -> x + 1;
    Function<Integer, Integer> g = x -> x * 2;
    
    // andThen: f → g
    Function<Integer, Integer> h1 = f.andThen(g);
    System.out.println("andThen: f(1) = 2, g(2) = 4");
    System.out.println("결과: " + h1.apply(1));
    
    // compose: g → f
    Function<Integer, Integer> h2 = f.compose(g);
    System.out.println("\ncompose: g(1) = 2, f(2) = 3");
    System.out.println("결과: " + h2.apply(1));
    
    // 실용적인 예시: 문자열 처리 파이프라인
    Function<String, String> trim = String::trim;
    Function<String, String> upper = String::toUpperCase;
    Function<String, Integer> length = String::length;
    
    Function<String, Integer> pipeline = 
        trim.andThen(upper).andThen(length);
    
    String input = "  hello world  ";
    System.out.println("\n파이프라인 예시:");
    System.out.println("입력: \"" + input + "\"");
    System.out.println("trim → upper → length");
    System.out.println("결과: " + pipeline.apply(input));
  }

  private static List<Apple> filter(List<Apple> list, Predicate<Apple> p) {
    List<Apple> result = new ArrayList<>();
    for (Apple a : list) {
      if (p.test(a)) {
        result.add(a);
      }
    }
    return result;
  }
}
