package chapter05.code;

import java.util.Optional;

import chapter04.code.Dish;

/**
 * 검색과 매칭 연산 예제
 * 
 * 스트림의 핵심 검색/매칭 연산:
 * - anyMatch(Predicate)    : 하나라도 조건을 만족하는가?
 * - allMatch(Predicate)    : 모두 조건을 만족하는가?
 * - noneMatch(Predicate)   : 하나도 조건을 만족하지 않는가?
 * - findFirst()            : 첫 번째 요소 찾기
 * - findAny()              : 아무 요소나 찾기
 * 
 * 학습 목표:
 * 1. 쇼트서킷 연산의 개념과 장점 이해
 * 2. anyMatch/allMatch/noneMatch의 차이점
 * 3. findFirst vs findAny 선택 기준
 * 4. Optional의 올바른 사용법
 * 
 * @author Modern Java In Action
 */
public class Finding {

  public static void main(String... args) {
    
    // ==================================================
    // 1. anyMatch - 채식 요리가 있는가?
    // ==================================================
    System.out.println("=== 1. anyMatch - Is vegetarian friendly? ===");
    
    /**
     * anyMatch(Predicate<T> predicate)
     * 
     * 동작 방식:
     * - 스트림을 순회하며 조건을 검사
     * - 조건을 만족하는 요소를 찾으면 즉시 true 반환 (쇼트서킷)
     * - 모든 요소가 조건 불만족이면 false 반환
     * 
     * 특징:
     * - 최종 연산 (Terminal operation)
     * - 쇼트서킷 연산 → 전체를 검사하지 않아도 됨
     * - 반환 타입: boolean
     * 
     * 사용 시나리오:
     * - "채식 요리가 있나요?"
     * - "300 칼로리 이상인 요리가 있나요?"
     * - 존재 여부 확인
     */
    if (isVegetarianFriendlyMenu()) {
      System.out.println("✓ 채식 요리가 있습니다!");
    }
    System.out.println();

    // ==================================================
    // 2. allMatch - 모든 요리가 건강식인가?
    // ==================================================
    System.out.println("=== 2. allMatch - Is healthy menu? ===");
    
    /**
     * allMatch(Predicate<T> predicate)
     * 
     * 동작 방식:
     * - 스트림을 순회하며 모든 요소가 조건을 만족하는지 검사
     * - 조건을 만족하지 않는 요소를 찾으면 즉시 false 반환 (쇼트서킷)
     * - 모든 요소가 조건 만족이면 true 반환
     * 
     * anyMatch vs allMatch:
     * - anyMatch: 하나라도 있으면 true
     * - allMatch: 모두 있어야 true
     */
    System.out.println("모든 요리가 1000 칼로리 미만? " + isHealthyMenu());
    System.out.println();

    // ==================================================
    // 3. noneMatch - 고칼로리 요리가 없는가?
    // ==================================================
    System.out.println("=== 3. noneMatch - Is healthy menu? ===");
    
    /**
     * noneMatch(Predicate<T> predicate)
     * 
     * 동작 방식:
     * - 스트림을 순회하며 조건을 만족하는 요소가 없는지 검사
     * - 조건을 만족하는 요소를 찾으면 즉시 false 반환 (쇼트서킷)
     * - 모든 요소가 조건 불만족이면 true 반환
     * 
     * 특징:
     * - allMatch의 부정 버전
     * - noneMatch(x) == allMatch(!x)
     * 
     * 관계식:
     * - anyMatch(p) = !noneMatch(p)
     * - allMatch(p) = !anyMatch(!p)
     * - noneMatch(p) = !anyMatch(p)
     */
    System.out.println("1000 칼로리 이상인 요리가 없나요? " + isHealthyMenu2());
    System.out.println();

    // ==================================================
    // 4. findAny - 채식 요리 아무거나
    // ==================================================
    System.out.println("=== 4. findAny - Find any vegetarian dish ===");
    
    /**
     * findAny()
     * 
     * 동작 방식:
     * - 스트림에서 임의의 요소 하나를 반환
     * - 요소를 찾으면 즉시 종료 (쇼트서킷)
     * - 병렬 스트림에서 어떤 요소가 반환될지 불확실
     * 
     * 특징:
     * - 최종 연산
     * - 쇼트서킷 연산
     * - 반환 타입: Optional<T>
     * 
     * Optional:
     * - 값이 있을 수도, 없을 수도 있음을 나타내는 컨테이너
     * - null을 반환하는 대신 Optional.empty() 반환
     * - NullPointerException 방지
     * 
     * Optional 사용법:
     * - ifPresent(Consumer): 값이 있으면 Consumer 실행
     * - orElse(T): 값이 있으면 반환, 없으면 기본값 반환
     * - orElseGet(Supplier): 값이 있으면 반환, 없으면 Supplier 실행
     * - orElseThrow(): 값이 있으면 반환, 없으면 예외
     */
    Optional<Dish> dish = findVegetarianDish();
    dish.ifPresent(d -> System.out.println("찾은 채식 요리: " + d.getName()));
  }

  /**
   * 채식 요리가 있는지 확인
   */
  private static boolean isVegetarianFriendlyMenu() {
    return Dish.menu.stream().anyMatch(Dish::isVegetarian);
  }

  /**
   * 모든 요리가 1000 칼로리 미만인지 확인 (allMatch)
   */
  private static boolean isHealthyMenu() {
    return Dish.menu.stream().allMatch(d -> d.getCalories() < 1000);
  }

  /**
   * 1000 칼로리 이상인 요리가 없는지 확인 (noneMatch)
   * 
   * noneMatch(d >= 1000) == allMatch(d < 1000)
   */
  private static boolean isHealthyMenu2() {
    return Dish.menu.stream().noneMatch(d -> d.getCalories() >= 1000);
  }

  /**
   * 채식 요리 하나 찾기
   */
  private static Optional<Dish> findVegetarianDish() {
    return Dish.menu.stream().filter(Dish::isVegetarian).findAny();
  }
}

/**
 * 검색과 매칭 연산 정리
 * 
 * ┌─────────────┬────────────┬─────────────┬──────────────┐
 * │    연산     │  반환 타입 │  쇼트서킷   │   사용 예    │
 * ├─────────────┼────────────┼─────────────┼──────────────┤
 * │ anyMatch    │ boolean    │ 예          │ 존재 확인    │
 * │ allMatch    │ boolean    │ 예          │ 전체 확인    │
 * │ noneMatch   │ boolean    │ 예          │ 부재 확인    │
 * │ findFirst   │ Optional<T>│ 예          │ 첫 요소      │
 * │ findAny     │ Optional<T>│ 예          │ 임의 요소    │
 * └─────────────┴────────────┴─────────────┴──────────────┘
 * 
 * 매칭 연산 관계:
 * - anyMatch(p) = !noneMatch(p)
 * - allMatch(p) = !anyMatch(!p) = noneMatch(!p)
 * - noneMatch(p) = !anyMatch(p) = allMatch(!p)
 * 
 * 선택 가이드:
 * 1. "~가 있나요?" → anyMatch
 * 2. "모두 ~인가요?" → allMatch
 * 3. "~가 없나요?" → noneMatch
 * 4. "첫 번째는?" → findFirst
 * 5. "아무거나" → findAny (병렬 시 빠름)
 */
