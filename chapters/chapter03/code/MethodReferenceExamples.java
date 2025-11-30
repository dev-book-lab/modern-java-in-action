package chapter03.code;

import java.util.*;
import java.util.function.*;

/**
 * Method Reference Examples (메서드 참조 예제)
 * 
 * 4가지 메서드 참조 유형:
 * 1. 정적 메서드 참조
 * 2. 인스턴스 메서드 참조 (임의 객체)
 * 3. 인스턴스 메서드 참조 (기존 객체)
 * 4. 생성자 참조
 */
public class MethodReferenceExamples {

  public static void main(String... args) {
    System.out.println("=== 메서드 참조 예제 ===\n");

    staticMethodReference();
    instanceMethodReference();
    existingObjectReference();
    constructorReference();
  }

  // ===== 1. 정적 메서드 참조 =====
  private static void staticMethodReference() {
    System.out.println("1. 정적 메서드 참조");
    
    // Before: 람다
    Function<String, Integer> f1 = s -> Integer.parseInt(s);
    
    // After: 메서드 참조
    Function<String, Integer> f2 = Integer::parseInt;
    
    System.out.println("람다: " + f1.apply("123"));
    System.out.println("메서드 참조: " + f2.apply("456"));
    
    // 더 많은 예시
    BiFunction<Integer, Integer, Integer> max1 = (a, b) -> Math.max(a, b);
    BiFunction<Integer, Integer, Integer> max2 = Math::max;
    System.out.println("Math.max(10, 20) = " + max2.apply(10, 20));
    System.out.println();
  }

  // ===== 2. 인스턴스 메서드 참조 (임의 객체) =====
  private static void instanceMethodReference() {
    System.out.println("2. 인스턴스 메서드 참조 (임의 객체)");
    
    // Before: 람다
    Function<String, String> f1 = s -> s.toUpperCase();
    
    // After: 메서드 참조
    Function<String, String> f2 = String::toUpperCase;
    
    System.out.println("람다: " + f1.apply("hello"));
    System.out.println("메서드 참조: " + f2.apply("world"));
    
    // List::contains 예시
    BiPredicate<List<String>, String> contains1 = 
        (list, element) -> list.contains(element);
    BiPredicate<List<String>, String> contains2 = List::contains;
    
    List<String> words = Arrays.asList("Java", "Lambda");
    System.out.println("contains Java: " + contains2.test(words, "Java"));
    System.out.println();
  }

  // ===== 3. 인스턴스 메서드 참조 (기존 객체) =====
  private static void existingObjectReference() {
    System.out.println("3. 인스턴스 메서드 참조 (기존 객체)");
    
    String str = "Hello World";
    
    // Before: 람다
    Supplier<Integer> s1 = () -> str.length();
    
    // After: 메서드 참조
    Supplier<Integer> s2 = str::length;
    
    System.out.println("람다: " + s1.get());
    System.out.println("메서드 참조: " + s2.get());
    
    // 다른 예시
    Predicate<String> p = str::startsWith;
    System.out.println("startsWith(\"Hello\"): " + p.test("Hello"));
    System.out.println();
  }

  // ===== 4. 생성자 참조 =====
  private static void constructorReference() {
    System.out.println("4. 생성자 참조");
    
    // 인수 없는 생성자
    Supplier<Apple> c1 = () -> new Apple(0, Color.GREEN);
    Supplier<Apple> c2 = () -> new Apple(0, Color.GREEN);
    
    Apple a1 = c1.get();
    System.out.println("생성: " + a1);
    
    // 인수 하나
    Function<Integer, Apple> c3 = weight -> new Apple(weight, Color.RED);
    Function<Integer, Apple> c4 = weight -> new Apple(weight, Color.RED);
    
    Apple a2 = c4.apply(100);
    System.out.println("생성: " + a2);
    
    // 인수 두 개
    BiFunction<Integer, Color, Apple> c5 = 
        (weight, color) -> new Apple(weight, color);
    BiFunction<Integer, Color, Apple> c6 = Apple::new;
    
    Apple a3 = c6.apply(150, Color.GREEN);
    System.out.println("생성: " + a3);
    
    // 리스트에서 사과 만들기
    List<Integer> weights = Arrays.asList(100, 150, 200);
    List<Apple> apples = map(weights, 
        weight -> new Apple(weight, Color.RED));
    System.out.println("생성된 사과들: " + apples);
  }

  private static <T, R> List<R> map(List<T> list, Function<T, R> f) {
    List<R> result = new ArrayList<>();
    for (T t : list) {
      result.add(f.apply(t));
    }
    return result;
  }
}
