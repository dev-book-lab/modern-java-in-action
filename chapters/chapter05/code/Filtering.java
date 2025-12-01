package chapter05.code;

import static java.util.stream.Collectors.toList;

import chapter04.code.Dish;
import java.util.Arrays;
import java.util.List;

/**
 * 필터링과 슬라이싱 연산 종합 예제
 * 
 * 스트림의 핵심 필터링 연산:
 * - filter(Predicate)      : 조건에 맞는 요소만 선택
 * - distinct()             : 중복 제거 (hashCode/equals 사용)
 * - takeWhile(Predicate)   : 조건이 거짓이 될 때까지 선택 (Java 9+)
 * - dropWhile(Predicate)   : 조건이 거짓이 될 때부터 선택 (Java 9+)
 * - limit(long)            : 최대 n개까지만 선택 (쇼트서킷)
 * - skip(long)             : 처음 n개 제외
 * 
 * 학습 목표:
 * 1. filter()를 이용한 프레디케이트 필터링
 * 2. distinct()를 이용한 중복 제거 원리 이해
 * 3. takeWhile/dropWhile의 동작 방식과 filter 차이점
 * 4. limit/skip을 이용한 페이징 구현
 * 5. 정렬된 데이터에서 슬라이싱의 효율성
 * 
 * @author Modern Java In Action
 */
public class Filtering {

  public static void main(String... args) {
    
    // ==================================================
    // 1. 프레디케이트로 필터링
    // ==================================================
    System.out.println("=== 1. Filtering with a predicate ===");
    
    /**
     * filter(Predicate<T> predicate)
     * 
     * 동작 방식:
     * - Predicate가 true를 반환하는 요소만 포함
     * - 전체 스트림을 순회하며 각 요소 검사
     * - 중간 연산 → Stream<T> 반환
     * 
     * 특징:
     * - 상태 없는 연산 (Stateless)
     * - 병렬 처리에 안전
     * - 여러 조건을 and/or로 연결 가능
     */
    List<Dish> vegetarianMenu = Dish.menu.stream()
        .filter(Dish::isVegetarian)  // 메서드 참조: dish -> dish.isVegetarian()
        .collect(toList());           // 최종 연산: List로 수집
    
    System.out.println("채식 요리:");
    vegetarianMenu.forEach(System.out::println);
    
    System.out.println();

    // ==================================================
    // 2. 고유 요소로 필터링 (중복 제거)
    // ==================================================
    System.out.println("=== 2. Filtering unique elements ===");
    
    /**
     * distinct()
     * 
     * 동작 방식:
     * - 객체의 hashCode()와 equals() 메서드 사용
     * - 내부적으로 Set을 사용하여 중복 판단
     * - 이미 나온 요소는 필터링
     * 
     * 특징:
     * - 상태 있는 연산 (Stateful) - 이전 요소들을 기억
     * - 정렬되지 않은 데이터에서도 작동
     * - 커스텀 객체는 hashCode/equals 구현 필수
     * 
     * 예시: [1, 2, 1, 3, 3, 2, 4]
     * → filter(짝수): [2, 2, 4]
     * → distinct(): [2, 4]
     */
    List<Integer> numbers = Arrays.asList(1, 2, 1, 3, 3, 2, 4);
    
    System.out.println("원본: " + numbers);
    System.out.println("짝수만 필터링 후 중복 제거:");
    numbers.stream()
        .filter(i -> i % 2 == 0)  // 짝수만: 2, 2, 4
        .distinct()                // 중복 제거: 2, 4
        .forEach(System.out::println);
    
    System.out.println();

    // ==================================================
    // 3. 스트림 슬라이싱 (Java 9+)
    // ==================================================
    
    /**
     * 칼로리 값을 기준으로 이미 오름차순 정렬된 리스트
     * 
     * 정렬된 데이터에서:
     * - filter: 전체 검사 (비효율적)
     * - takeWhile: 조건 false까지만 검사 (효율적)
     * - dropWhile: 조건 false부터 선택
     */
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
    
    /**
     * filter의 동작:
     * - 모든 요소를 검사 (5개 전부)
     * - season fruit(120) → 통과
     * - prawns(300) → 통과
     * - rice(350) → 실패 (하지만 계속 검사)
     * - chicken(400) → 실패
     * - french fries(530) → 실패
     * 
     * 결과: 2개 선택, 5개 검사 (효율 40%)
     */
    List<Dish> filteredMenu = specialMenu.stream()
        .filter(dish -> dish.getCalories() < 320)
        .collect(toList());
    filteredMenu.forEach(System.out::println);
    
    System.out.println();

    // --------------------------------------------------
    // 3-2. takeWhile() 사용 - 조건이 false가 될 때까지만 검사
    // --------------------------------------------------
    System.out.println("=== 3-2. Sorted menu sliced with takeWhile() ===");
    
    /**
     * takeWhile(Predicate<T> predicate)
     * 
     * 동작 방식:
     * - 처음부터 순회하면서 Predicate가 true인 동안만 선택
     * - Predicate가 처음으로 false를 반환하면 즉시 중단 (쇼트서킷)
     * - 나머지 요소는 검사하지 않음
     * 
     * 실행 과정:
     * - season fruit(120) → 통과
     * - prawns(300) → 통과
     * - rice(350) → 실패 → 즉시 중단!
     * - chicken, french fries는 검사조차 안 함
     * 
     * 결과: 2개 선택, 3개 검사 (효율 67%)
     * 
     * filter vs takeWhile:
     * - filter: 전체 스트림 순회 (모든 요소 검사)
     * - takeWhile: 조건이 false가 되면 즉시 종료 (일부만 검사)
     * 
     * 사용 시나리오:
     * - 정렬된 데이터에서 특정 조건까지만 선택
     * - 예: 320 칼로리 미만 요리만 선택 (칼로리 오름차순 정렬 시)
     * 
     * 주의사항:
     * - 정렬되지 않은 데이터에서는 예상치 못한 결과
     * - Java 9+ 에서만 사용 가능
     */
    List<Dish> slicedMenu1 = specialMenu.stream()
        .takeWhile(dish -> dish.getCalories() < 320)
        .collect(toList());
    slicedMenu1.forEach(System.out::println);
    
    System.out.println();

    // --------------------------------------------------
    // 3-3. dropWhile() 사용 - 조건이 false가 될 때부터 가져옴
    // --------------------------------------------------
    System.out.println("=== 3-3. Sorted menu sliced with dropWhile() ===");
    
    /**
     * dropWhile(Predicate<T> predicate)
     * 
     * 동작 방식:
     * - 처음부터 순회하면서 Predicate가 true인 동안 버림
     * - Predicate가 처음으로 false를 반환하면 그때부터 모든 요소 선택
     * - takeWhile과 정반대 동작
     * 
     * 실행 과정:
     * - season fruit(120) → 버림
     * - prawns(300) → 버림
     * - rice(350) → false → 여기서부터 선택 시작!
     * - chicken(400) → 선택
     * - french fries(530) → 선택
     * 
     * takeWhile vs dropWhile:
     * - takeWhile: 조건이 false가 될 때까지 선택
     * - dropWhile: 조건이 false가 될 때부터 선택
     * 
     * 사용 시나리오:
     * - 정렬된 데이터에서 특정 조건 이후부터 선택
     * - 예: 320 칼로리 이상 요리만 선택
     */
    List<Dish> slicedMenu2 = specialMenu.stream()
        .dropWhile(dish -> dish.getCalories() < 320)
        .collect(toList());
    slicedMenu2.forEach(System.out::println);
    
    System.out.println();

    // ==================================================
    // 4. 스트림 축소 - limit()
    // ==================================================
    System.out.println("=== 4. Truncating a stream (limit) ===");
    
    /**
     * limit(long maxSize)
     * 
     * 동작 방식:
     * - 최대 maxSize개의 요소만 선택
     * - maxSize개를 선택하면 즉시 중단 (쇼트서킷)
     * - 순서가 있는 스트림에서는 처음 n개 선택
     * 
     * 특징:
     * - 쇼트서킷 연산 (Short-circuit)
     * - 전체 데이터를 처리하지 않음 → 성능 향상
     * - 정렬과 함께 사용하면 Top N 선택 가능
     * 
     * 사용 시나리오:
     * - Top N 선택
     * - 페이징 구현 (skip과 함께)
     * - 무한 스트림 제한
     * 
     * 예시: 300 칼로리 초과 요리 중 처음 3개
     */
    System.out.println("300 칼로리 초과 요리 Top 3:");
    List<Dish> dishesLimit3 = Dish.menu.stream()
        .filter(d -> d.getCalories() > 300)  // 필터링 먼저
        .limit(3)                             // 3개만 선택 (쇼트서킷!)
        .collect(toList());
    dishesLimit3.forEach(System.out::println);
    
    System.out.println();

    // ==================================================
    // 5. 요소 건너뛰기 - skip()
    // ==================================================
    System.out.println("=== 5. Skipping elements (skip) ===");
    
    /**
     * skip(long n)
     * 
     * 동작 방식:
     * - 처음 n개의 요소를 제외
     * - n개를 건너뛴 후 나머지 요소 반환
     * - n이 스트림 크기보다 크면 빈 스트림 반환
     * 
     * 특징:
     * - 상태 있는 연산 (Stateful) - n개를 세어야 함
     * - limit과 상호 보완적
     * - 처음 n개는 반드시 순회해야 함
     * 
     * limit vs skip:
     * - limit(3): 처음 3개만
     * - skip(3): 처음 3개 제외
     * 
     * 사용 시나리오:
     * - 페이징 구현: skip(page * size).limit(size)
     * - 헤더 행 건너뛰기
     * - 샘플 데이터 제외
     * 
     * 예시: 300 칼로리 초과 요리 중 처음 2개 제외
     */
    System.out.println("300 칼로리 초과 요리 중 처음 2개 제외:");
    List<Dish> dishesSkip2 = Dish.menu.stream()
        .filter(d -> d.getCalories() > 300)
        .skip(2)
        .collect(toList());
    dishesSkip2.forEach(System.out::println);
    
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

/**
 * 필터링과 슬라이싱 연산 정리
 * 
 * ┌─────────────┬──────────────┬──────────────┬─────────────┐
 * │    연산     │  전체 순회?  │    상태     │  쇼트서킷   │
 * ├─────────────┼──────────────┼──────────────┼─────────────┤
 * │ filter      │ 예           │ 무상태      │ 아니오      │
 * │ distinct    │ 예           │ 상태 있음   │ 아니오      │
 * │ takeWhile   │ 조건까지     │ 무상태      │ 예          │
 * │ dropWhile   │ 조건부터     │ 무상태      │ 예          │
 * │ limit       │ n개까지      │ 무상태      │ 예          │
 * │ skip        │ n개 후       │ 상태 있음   │ 아니오      │
 * └─────────────┴──────────────┴──────────────┴─────────────┘
 * 
 * 선택 가이드:
 * 1. 조건 필터링 → filter
 * 2. 중복 제거 → distinct
 * 3. 정렬된 데이터에서 조건까지 → takeWhile
 * 4. 정렬된 데이터에서 조건부터 → dropWhile
 * 5. 개수 제한 → limit
 * 6. 일부 건너뛰기 → skip
 * 7. 페이징 → skip + limit
 * 
 * 성능 팁:
 * - filter 연산은 가장 먼저 배치
 * - 정렬된 데이터는 takeWhile/dropWhile 활용
 * - limit는 쇼트서킷 → 일찍 배치
 * - distinct는 비용이 큼 → 필요시에만 사용
 */
