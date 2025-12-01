package chapter05.code;

import static java.util.stream.Collectors.toList;
import static chapter04.code.Dish.menu;

import chapter04.code.Dish;
import java.util.Arrays;
import java.util.List;

/**
 * 필터링과 슬라이싱 연산 예제
 * 
 * 학습 목표:
 * 1. filter()를 이용한 프레디케이트 필터링
 * 2. distinct()를 이용한 중복 제거
 * 3. takeWhile/dropWhile을 이용한 슬라이싱 (Java 9+)
 * 4. limit/skip을 이용한 스트림 축소
 */
public class Filtering {

  public static void main(String... args) {
    
    // ==================================================
    // 1. 프레디케이트로 필터링
    // ==================================================
    System.out.println("=== 1. Filtering with a predicate ===");
    
    // filter(): Predicate<T>를 받아 조건에 맞는 요소만 포함하는 스트림 반환
    // 여기서는 채식 요리만 필터링
    List<Dish> vegetarianMenu = menu.stream()
        .filter(Dish::isVegetarian)  // 메서드 참조: dish -> dish.isVegetarian()
        .collect(toList());           // List로 수집
    vegetarianMenu.forEach(System.out::println);
    
    System.out.println();

    // ==================================================
    // 2. 고유 요소로 필터링 (중복 제거)
    // ==================================================
    System.out.println("=== 2. Filtering unique elements ===");
    
    List<Integer> numbers = Arrays.asList(1, 2, 1, 3, 3, 2, 4);
    
    numbers.stream()
        .filter(i -> i % 2 == 0)  // 짝수만 필터링: 2, 2, 4
        .distinct()                // 중복 제거: 2, 4
        .forEach(System.out::println);
    
    // distinct()는 hashCode()와 equals()를 사용하여 중복 판단
    
    System.out.println();

    // ==================================================
    // 3. 스트림 슬라이싱 (Java 9+)
    // ==================================================
    
    // 칼로리 값을 기준으로 이미 오름차순 정렬된 리스트
    List<Dish> specialMenu = Arrays.asList(
        new Dish("season fruit", true, 120, Dish.Type.OTHER),
        new Dish("prawns", false, 300, Dish.Type.FISH),
        new Dish("rice", true, 350, Dish.Type.OTHER),
        new Dish("chicken", false, 400, Dish.Type.MEAT),
        new Dish("french fries", true, 530, Dish.Type.OTHER)
    );
    
    // --------------------------------------------------
    // 3-1. filter() 사용 - 전체 검사
    // --------------------------------------------------
    System.out.println("=== 3-1. Filtered sorted menu (filter) ===");
    
    List<Dish> filteredMenu = specialMenu.stream()
        .filter(dish -> dish.getCalories() < 320)  // 모든 요소를 다 검사
        .collect(toList());
    filteredMenu.forEach(System.out::println);
    // 결과: season fruit(120), prawns(300)
    
    System.out.println();

    // --------------------------------------------------
    // 3-2. takeWhile() 사용 - 조건이 false가 될 때까지만 검사
    // --------------------------------------------------
    System.out.println("=== 3-2. Sorted menu sliced with takeWhile() ===");
    
    List<Dish> slicedMenu1 = specialMenu.stream()
        .takeWhile(dish -> dish.getCalories() < 320)  // 조건이 false가 되면 즉시 중단
        .collect(toList());
    slicedMenu1.forEach(System.out::println);
    // 결과: season fruit(120), prawns(300)
    // rice(350)에서 조건이 false가 되어 중단!
    // 정렬된 데이터에서 filter()보다 훨씬 효율적!
    
    System.out.println();

    // --------------------------------------------------
    // 3-3. dropWhile() 사용 - 조건이 false가 될 때부터 가져옴
    // --------------------------------------------------
    System.out.println("=== 3-3. Sorted menu sliced with dropWhile() ===");
    
    List<Dish> slicedMenu2 = specialMenu.stream()
        .dropWhile(dish -> dish.getCalories() < 320)  // 조건이 false가 될 때까지 버림
        .collect(toList());
    slicedMenu2.forEach(System.out::println);
    // 결과: rice(350), chicken(400), french fries(530)
    // season fruit(120), prawns(300)은 버려짐
    
    System.out.println();

    // ==================================================
    // 4. 스트림 축소 - limit()
    // ==================================================
    System.out.println("=== 4. Truncating a stream (limit) ===");
    
    // limit(n): 처음 n개 요소만 반환
    List<Dish> dishesLimit3 = menu.stream()
        .filter(d -> d.getCalories() > 300)  // 300 칼로리 이상 필터링
        .limit(3)                             // 처음 3개만 선택 (쇼트서킷!)
        .collect(toList());
    dishesLimit3.forEach(System.out::println);
    // limit은 쇼트서킷 연산: 3개를 찾으면 더 이상 처리하지 않음
    
    System.out.println();

    // ==================================================
    // 5. 요소 건너뛰기 - skip()
    // ==================================================
    System.out.println("=== 5. Skipping elements (skip) ===");
    
    // skip(n): 처음 n개 요소를 제외하고 반환
    List<Dish> dishesSkip2 = menu.stream()
        .filter(d -> d.getCalories() > 300)  // 300 칼로리 이상 필터링
        .skip(2)                              // 처음 2개 건너뛰기
        .collect(toList());
    dishesSkip2.forEach(System.out::println);
    // skip은 처음 n개를 소비하므로 쇼트서킷이 아님
    
    System.out.println();
    
    // ==================================================
    // 요약
    // ==================================================
    System.out.println("=== 요약 ===");
    System.out.println("filter: 조건에 맞는 모든 요소 (전체 검사)");
    System.out.println("distinct: 중복 제거 (hashCode/equals 사용)");
    System.out.println("takeWhile: 조건이 false까지 (조기 종료, 정렬 데이터)");
    System.out.println("dropWhile: 조건이 false부터 (정렬 데이터)");
    System.out.println("limit: 처음 n개만 (쇼트서킷)");
    System.out.println("skip: 처음 n개 제외");
  }
}
