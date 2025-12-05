package chapter06.code;

import static chapter06.code.Dish.dishTags;
import static chapter06.code.Dish.menu;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.filtering;
import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 그룹화 연산 종합 예제
 * 
 * groupingBy를 활용한 다양한 그룹화 패턴:
 * - 기본 그룹화              : groupingBy(classifier)
 * - 다운스트림 컬렉터        : groupingBy(classifier, downstream)
 * - 다수준 그룹화            : groupingBy(c1, groupingBy(c2))
 * - filtering               : 그룹 내 필터링 (키 유지)
 * - mapping                 : 그룹 내 변환
 * - flatMapping             : 그룹 내 평면화
 * - collectingAndThen       : 결과 변환
 * 
 * 학습 목표:
 * 1. 다양한 기준으로 데이터 그룹화
 * 2. 다운스트림 컬렉터 조합
 * 3. 다수준 그룹화 구조 생성
 * 4. filtering vs filter 차이 이해
 * 5. Optional 제거 방법
 * 
 * @author Modern Java In Action
 */
public class Grouping {

  /**
   * 칼로리 레벨 열거형
   */
  enum CaloricLevel { 
    DIET,    // 400 이하
    NORMAL,  // 400~700
    FAT      // 700 초과
  }

  public static void main(String ... args) {
    // ==================================================
    // 실행 결과 출력
    // ==================================================
    System.out.println("=== 그룹화 연산 ===\n");
    
    System.out.println("1. 타입별 그룹화:");
    System.out.println(groupDishesByType());
    System.out.println();
    
    System.out.println("2. 타입별 요리명:");
    System.out.println(groupDishNamesByType());
    System.out.println();
    
    System.out.println("3. 타입별 태그:");
    System.out.println(groupDishTagsByType());
    System.out.println();
    
    System.out.println("4. 타입별 고칼로리 요리 (filtering):");
    System.out.println(groupCaloricDishesByType());
    System.out.println();
    
    System.out.println("5. 칼로리 레벨별 그룹화:");
    System.out.println(groupDishesByCaloricLevel());
    System.out.println();
    
    System.out.println("6. 타입 + 칼로리 레벨 (2단계):");
    System.out.println(groupDishedByTypeAndCaloricLevel());
    System.out.println();
    
    System.out.println("7. 타입별 개수:");
    System.out.println(countDishesInGroups());
    System.out.println();
    
    System.out.println("8. 타입별 최대 칼로리 요리 (Optional):");
    System.out.println(mostCaloricDishesByType());
    System.out.println();
    
    System.out.println("9. 타입별 최대 칼로리 요리 (Optional 제거):");
    System.out.println(mostCaloricDishesByTypeWithoutOprionals());
    System.out.println();
    
    System.out.println("10. 타입별 총 칼로리:");
    System.out.println(sumCaloriesByType());
    System.out.println();
    
    System.out.println("11. 타입별 칼로리 레벨:");
    System.out.println(caloricLevelsByType());
  }

  // ==================================================
  // 1. 기본 그룹화
  // ==================================================
  /**
   * groupingBy(Function<T, K> classifier)
   * 
   * 동작:
   * - 분류 함수를 적용하여 키 추출
   * - 같은 키를 가진 요소들을 리스트로 수집
   * - Map<K, List<T>> 반환
   * 
   * 예시:
   * pork(MEAT) → MEAT 그룹에 추가
   * beef(MEAT) → MEAT 그룹에 추가
   * prawns(FISH) → FISH 그룹에 추가
   * 
   * 결과:
   * {
   *   MEAT=[pork, beef, chicken],
   *   FISH=[prawns, salmon],
   *   OTHER=[french fries, rice, ...]
   * }
   */
  private static Map<Dish.Type, List<Dish>> groupDishesByType() {
    return menu.stream()
        .collect(groupingBy(Dish::getType));
    
    // 축약형: groupingBy(Dish::getType)
    // 완전형: groupingBy(Dish::getType, toList())
  }

  // ==================================================
  // 2. mapping - 그룹 내 변환
  // ==================================================
  /**
   * groupingBy + mapping
   * 
   * mapping(Function<T, U> mapper, Collector<U, A, R> downstream)
   * 
   * 동작:
   * 1. 그룹화 수행
   * 2. 각 그룹의 요소에 mapper 적용
   * 3. downstream 컬렉터로 수집
   * 
   * 예시:
   * MEAT 그룹: [pork, beef, chicken]
   * → mapping(getName): ["pork", "beef", "chicken"]
   * → toList(): ["pork", "beef", "chicken"]
   */
  private static Map<Dish.Type, List<String>> groupDishNamesByType() {
    return menu.stream()
        .collect(
            groupingBy(
                Dish::getType,                    // 분류 함수
                mapping(Dish::getName, toList())  // 다운스트림: 이름 추출
            )
        );
  }

  // ==================================================
  // 3. flatMapping - 그룹 내 평면화
  // ==================================================
  /**
   * groupingBy + flatMapping
   * 
   * flatMapping(Function<T, Stream<U>>, Collector<U, A, R>)
   * 
   * 동작:
   * 1. 그룹화 수행
   * 2. 각 그룹의 요소에 mapper 적용 (Stream 반환)
   * 3. 스트림들을 평면화
   * 4. downstream 컬렉터로 수집
   * 
   * 예시:
   * MEAT 그룹: [pork, beef, chicken]
   * 
   * pork → dishTags.get("pork") → ["greasy", "salty"]
   * beef → dishTags.get("beef") → ["salty", "roasted"]
   * chicken → dishTags.get("chicken") → ["fried", "crisp"]
   * 
   * 평면화: ["greasy", "salty", "salty", "roasted", "fried", "crisp"]
   * toSet(): {"greasy", "salty", "roasted", "fried", "crisp"}
   */
  private static Map<Dish.Type, Set<String>> groupDishTagsByType() {
    return menu.stream()
        .collect(
            groupingBy(
                Dish::getType,
                flatMapping(
                    dish -> dishTags.get(dish.getName()).stream(),  // Stream<String>
                    toSet()  // 중복 제거
                )
            )
        );
  }

  // ==================================================
  // 4. filtering - 그룹 내 필터링 (키 유지)
  // ==================================================
  /**
   * groupingBy + filtering
   * 
   * filtering(Predicate<T>, Collector<T, A, R>)
   * 
   * filter vs filtering:
   * 
   * ❌ filter 먼저 (키 누락 가능):
   * menu.stream()
   *     .filter(dish -> dish.getCalories() > 500)
   *     .collect(groupingBy(Dish::getType))
   * → FISH 타입이 모두 500 이하면 FISH 키 자체가 사라짐!
   * 
   * ✅ filtering 컬렉터 (모든 키 유지):
   * menu.stream()
   *     .collect(groupingBy(
   *         Dish::getType,
   *         filtering(dish -> dish.getCalories() > 500, toList())
   *     ))
   * → FISH=[] 로 키는 유지됨!
   * 
   * 동작:
   * 1. 모든 요소를 타입별로 그룹화
   * 2. 각 그룹에서 조건 만족하는 요소만 필터링
   * 3. 조건 만족하는 요소가 없어도 빈 리스트로 키 유지
   */
  private static Map<Dish.Type, List<Dish>> groupCaloricDishesByType() {
    // ❌ filter 먼저 사용 (키 누락)
    // return menu.stream()
    //     .filter(dish -> dish.getCalories() > 500)
    //     .collect(groupingBy(Dish::getType));
    
    // ✅ filtering 컬렉터 사용 (키 유지)
    return menu.stream()
        .collect(
            groupingBy(
                Dish::getType,
                filtering(dish -> dish.getCalories() > 500, toList())
            )
        );
  }

  // ==================================================
  // 5. 람다 표현식으로 분류
  // ==================================================
  /**
   * 복잡한 분류 기준
   * 
   * 메서드 참조 대신 람다 표현식 사용:
   * - 조건부 로직
   * - 여러 필드 조합
   * - 계산된 값
   * 
   * 예시: 칼로리 레벨로 분류
   * - 400 이하: DIET
   * - 400~700: NORMAL
   * - 700 초과: FAT
   */
  private static Map<CaloricLevel, List<Dish>> groupDishesByCaloricLevel() {
    return menu.stream()
        .collect(
            groupingBy(dish -> {
              if (dish.getCalories() <= 400) {
                return CaloricLevel.DIET;
              }
              else if (dish.getCalories() <= 700) {
                return CaloricLevel.NORMAL;
              }
              else {
                return CaloricLevel.FAT;
              }
            })
        );
  }

  // ==================================================
  // 6. 다수준 그룹화 (2단계)
  // ==================================================
  /**
   * groupingBy 중첩
   * 
   * Map<K1, Map<K2, List<T>>> 구조:
   * - 1차 키: Dish.Type
   * - 2차 키: CaloricLevel
   * - 값: List<Dish>
   * 
   * 동작:
   * 1. 타입별로 1차 그룹화
   * 2. 각 타입 그룹을 칼로리 레벨로 2차 그룹화
   * 
   * 결과 구조:
   * {
   *   MEAT={
   *     DIET=[chicken],
   *     NORMAL=[beef],
   *     FAT=[pork]
   *   },
   *   FISH={
   *     DIET=[prawns],
   *     NORMAL=[salmon]
   *   },
   *   OTHER={...}
   * }
   * 
   * n수준 그룹화:
   * - n=1: Map<K, List<T>>
   * - n=2: Map<K1, Map<K2, List<T>>>
   * - n=3: Map<K1, Map<K2, Map<K3, List<T>>>>
   */
  private static Map<Dish.Type, Map<CaloricLevel, List<Dish>>> groupDishedByTypeAndCaloricLevel() {
    return menu.stream()
        .collect(
            groupingBy(
                Dish::getType,  // 1차 분류
                groupingBy((Dish dish) -> {  // 2차 분류
                  if (dish.getCalories() <= 400) {
                    return CaloricLevel.DIET;
                  }
                  else if (dish.getCalories() <= 700) {
                    return CaloricLevel.NORMAL;
                  }
                  else {
                    return CaloricLevel.FAT;
                  }
                })
            )
        );
  }

  // ==================================================
  // 7. counting - 그룹별 개수
  // ==================================================
  /**
   * groupingBy + counting
   * 
   * counting()
   * - 각 그룹의 요소 개수 반환
   * - Long 타입
   * 
   * 동작:
   * 1. 타입별로 그룹화
   * 2. 각 그룹의 요소 개수 세기
   * 
   * 결과:
   * {MEAT=3, FISH=2, OTHER=4}
   */
  private static Map<Dish.Type, Long> countDishesInGroups() {
    return menu.stream()
        .collect(
            groupingBy(Dish::getType, counting())
        );
  }

  // ==================================================
  // 8. reducing - 그룹별 최대값 (Optional)
  // ==================================================
  /**
   * groupingBy + reducing
   * 
   * reducing(BinaryOperator<T>)
   * - 초기값 없음
   * - Optional<T> 반환
   * 
   * 문제:
   * - 각 그룹의 결과가 Optional로 래핑됨
   * - 사용하기 불편
   * 
   * 결과 타입:
   * Map<Dish.Type, Optional<Dish>>
   */
  private static Map<Dish.Type, Optional<Dish>> mostCaloricDishesByType() {
    return menu.stream()
        .collect(
            groupingBy(
                Dish::getType,
                reducing((Dish d1, Dish d2) -> 
                    d1.getCalories() > d2.getCalories() ? d1 : d2
                )
            )
        );
  }

  // ==================================================
  // 9. collectingAndThen - Optional 제거
  // ==================================================
  /**
   * collectingAndThen(Collector<T, A, R>, Function<R, RR>)
   * 
   * 동작:
   * 1. 첫 번째 컬렉터 적용
   * 2. 결과에 변환 함수 적용
   * 
   * 예시:
   * 1. reducing(...) → Optional<Dish>
   * 2. Optional::get → Dish
   * 
   * 결과:
   * - Optional이 제거됨
   * - 각 그룹에 요소가 있음이 보장됨
   * 
   * 결과 타입:
   * Map<Dish.Type, Dish>
   * 
   * 주의:
   * - 빈 그룹이 있으면 NoSuchElementException
   * - 하지만 groupingBy는 빈 그룹을 만들지 않음
   */
  private static Map<Dish.Type, Dish> mostCaloricDishesByTypeWithoutOprionals() {
    return menu.stream()
        .collect(
            groupingBy(
                Dish::getType,
                collectingAndThen(
                    reducing((d1, d2) -> 
                        d1.getCalories() > d2.getCalories() ? d1 : d2
                    ),
                    Optional::get  // Optional<Dish> → Dish
                )
            )
        );
  }

  // ==================================================
  // 10. summingInt - 그룹별 합계
  // ==================================================
  /**
   * groupingBy + summingInt
   * 
   * summingInt(ToIntFunction<T>)
   * - 각 요소를 int로 변환
   * - 그룹별로 합계 계산
   * 
   * 동작:
   * 1. 타입별로 그룹화
   * 2. 각 그룹의 칼로리 합계 계산
   * 
   * 결과:
   * {MEAT=1900, FISH=850, OTHER=1550}
   */
  private static Map<Dish.Type, Integer> sumCaloriesByType() {
    return menu.stream()
        .collect(
            groupingBy(
                Dish::getType,
                summingInt(Dish::getCalories)
            )
        );
  }

  // ==================================================
  // 11. mapping - 그룹별 Set 수집
  // ==================================================
  /**
   * groupingBy + mapping + toSet
   * 
   * 동작:
   * 1. 타입별로 그룹화
   * 2. 각 요소를 칼로리 레벨로 변환
   * 3. Set으로 수집 (중복 제거)
   * 
   * 예시:
   * MEAT 그룹: [pork(800), beef(700), chicken(400)]
   * → [FAT, NORMAL, DIET]
   * → toSet(): {FAT, NORMAL, DIET}
   * 
   * 결과:
   * {
   *   MEAT={DIET, NORMAL, FAT},
   *   FISH={DIET, NORMAL},
   *   OTHER={DIET, NORMAL, FAT}
   * }
   */
  private static Map<Dish.Type, Set<CaloricLevel>> caloricLevelsByType() {
    return menu.stream()
        .collect(
            groupingBy(
                Dish::getType, 
                mapping(
                    dish -> {
                      if (dish.getCalories() <= 400) {
                        return CaloricLevel.DIET;
                      }
                      else if (dish.getCalories() <= 700) {
                        return CaloricLevel.NORMAL;
                      }
                      else {
                        return CaloricLevel.FAT;
                      }
                    },
                    toSet()  // 중복 제거
                )
            )
        );
  }

}

/**
 * 그룹화 연산 정리
 * 
 * ┌─────────────────┬──────────────────┬────────────────┐
 * │   패턴          │   다운스트림     │   결과 타입    │
 * ├─────────────────┼──────────────────┼────────────────┤
 * │ 기본            │ toList()         │ Map<K, List>   │
 * │ 개수            │ counting()       │ Map<K, Long>   │
 * │ 합계            │ summingInt()     │ Map<K, Int>    │
 * │ 평균            │ averagingInt()   │ Map<K, Double> │
 * │ 최대/최소       │ maxBy/minBy()    │ Map<K, Opt<T>> │
 * │ 변환            │ mapping()        │ Map<K, List>   │
 * │ 평면화          │ flatMapping()    │ Map<K, Set>    │
 * │ 필터링          │ filtering()      │ Map<K, List>   │
 * │ 다수준          │ groupingBy()     │ Map<K, Map>    │
 * └─────────────────┴──────────────────┴────────────────┘
 * 
 * groupingBy 오버로드:
 * 1. groupingBy(classifier)
 *    → groupingBy(classifier, toList())
 * 
 * 2. groupingBy(classifier, downstream)
 *    → groupingBy(classifier, HashMap::new, downstream)
 * 
 * 3. groupingBy(classifier, mapFactory, downstream)
 *    → Map 구현체 지정 가능 (TreeMap, LinkedHashMap 등)
 * 
 * 실무 패턴:
 * - 그룹별 개수: groupingBy(c, counting())
 * - 그룹별 합계: groupingBy(c, summingInt())
 * - 그룹별 최대: groupingBy(c, collectingAndThen(maxBy(), Optional::get))
 * - 그룹별 이름: groupingBy(c, mapping(getName, toList()))
 * - 키 유지 필터: groupingBy(c, filtering(p, toList()))
 */
