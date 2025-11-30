package chapter03.code;

import java.util.*;
import java.util.function.*;

/**
 * Functional Interface Examples
 * 
 * java.util.function 패키지의 주요 함수형 인터페이스 학습
 * 
 * 핵심 인터페이스:
 * - Predicate<T>: T → boolean
 * - Consumer<T>: T → void
 * - Function<T,R>: T → R
 * - Supplier<T>: () → T
 * - UnaryOperator<T>: T → T
 * - BinaryOperator<T>: (T,T) → T
 */
public class FunctionalInterfaceExamples {

  public static void main(String... args) {
    System.out.println("=== 함수형 인터페이스 예제 ===\n");

    predicateExamples();
    consumerExamples();
    functionExamples();
    supplierExamples();
    operatorExamples();
    primitiveExamples();
  }

  // ===== Predicate<T>: (T) -> boolean =====
  private static void predicateExamples() {
    System.out.println("1. Predicate<T> - 조건 검사");
    
    Predicate<String> nonEmpty = s -> !s.isEmpty();
    Predicate<String> startsWithJ = s -> s.startsWith("J");
    
    List<String> words = Arrays.asList("Java", "", "Lambda", "");
    System.out.println("전체: " + words);
    
    List<String> filtered = filter(words, nonEmpty);
    System.out.println("비어있지 않은 문자열: " + filtered);
    
    // Predicate 조합: and, or, negate
    Predicate<String> javaWords = nonEmpty.and(startsWithJ);
    List<String> jWords = filter(words, javaWords);
    System.out.println("J로 시작하는 문자열: " + jWords);
    System.out.println();
  }

  // ===== Consumer<T>: (T) -> void =====
  private static void consumerExamples() {
    System.out.println("2. Consumer<T> - 값 소비");
    
    Consumer<String> printer = s -> System.out.println("  출력: " + s);
    
    List<String> items = Arrays.asList("Apple", "Banana", "Cherry");
    System.out.println("forEach로 출력:");
    forEach(items, printer);
    System.out.println();
  }

  // ===== Function<T,R>: (T) -> R =====
  private static void functionExamples() {
    System.out.println("3. Function<T,R> - 변환");
    
    Function<String, Integer> toLength = s -> s.length();
    Function<Integer, Integer> square = n -> n * n;
    
    List<String> words = Arrays.asList("Modern", "Java", "In", "Action");
    System.out.println("단어: " + words);
    
    List<Integer> lengths = map(words, toLength);
    System.out.println("길이: " + lengths);
    
    // Function 조합: andThen
    Function<String, Integer> lengthSquared = toLength.andThen(square);
    List<Integer> squared = map(words, lengthSquared);
    System.out.println("길이의 제곱: " + squared);
    System.out.println();
  }

  // ===== Supplier<T>: () -> T =====
  private static void supplierExamples() {
    System.out.println("4. Supplier<T> - 값 생성");
    
    Supplier<Double> randomSupplier = () -> Math.random();
    Supplier<List<String>> listFactory = () -> new ArrayList<>();
    
    System.out.println("랜덤 값: " + randomSupplier.get());
    System.out.println("랜덤 값: " + randomSupplier.get());
    
    List<String> list = listFactory.get();
    System.out.println("생성된 리스트: " + list);
    System.out.println();
  }

  // ===== UnaryOperator / BinaryOperator =====
  private static void operatorExamples() {
    System.out.println("5. Operator - 같은 타입 연산");
    
    UnaryOperator<Integer> doubleIt = n -> n * 2;
    BinaryOperator<Integer> add = (a, b) -> a + b;
    BinaryOperator<Integer> max = (a, b) -> a > b ? a : b;
    
    System.out.println("10의 2배: " + doubleIt.apply(10));
    System.out.println("3 + 5: " + add.apply(3, 5));
    System.out.println("max(8, 12): " + max.apply(8, 12));
    System.out.println();
  }

  // ===== 기본형 특화 인터페이스 =====
  private static void primitiveExamples() {
    System.out.println("6. 기본형 특화 - 박싱 없음!");
    
    // 박싱 발생 (느림)
    Predicate<Integer> evenBoxed = i -> i % 2 == 0;
    System.out.println("Predicate (박싱): " + evenBoxed.test(10));
    
    // 박싱 없음 (빠름)
    IntPredicate even = i -> i % 2 == 0;
    System.out.println("IntPredicate (박싱 없음): " + even.test(10));
    
    // 기타 예시
    IntFunction<String> intToString = i -> "숫자: " + i;
    ToIntFunction<String> strToInt = String::length;
    IntUnaryOperator square = i -> i * i;
    
    System.out.println(intToString.apply(42));
    System.out.println("길이: " + strToInt.applyAsInt("Hello"));
    System.out.println("5의 제곱: " + square.applyAsInt(5));
  }

  // ===== 유틸리티 메서드 =====
  
  public static <T> List<T> filter(List<T> list, Predicate<T> p) {
    List<T> result = new ArrayList<>();
    for (T t : list) {
      if (p.test(t)) {
        result.add(t);
      }
    }
    return result;
  }

  public static <T> void forEach(List<T> list, Consumer<T> c) {
    for (T t : list) {
      c.accept(t);
    }
  }

  public static <T, R> List<R> map(List<T> list, Function<T, R> f) {
    List<R> result = new ArrayList<>();
    for (T t : list) {
      result.add(f.apply(t));
    }
    return result;
  }
}
