package chapter06.code;

import static chapter06.code.Dish.menu;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.maxBy;
import static java.util.stream.Collectors.partitioningBy;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 분할 연산 종합 예제
 * 
 * partitioningBy를 활용한 Boolean 기반 분할:
 * - 기본 분할: partitioningBy(predicate)
 * - 다운스트림: partitioningBy(predicate, downstream)
 * - 분할 + 그룹화 조합
 * - collectingAndThen으로 Optional 제거
 * 
 * 학습 목표:
 * 1. partitioningBy의 기본 사용법
 * 2. 다운스트림 컬렉터와 조합
 * 3. partitioningBy vs groupingBy 차이
 * 4. Boolean 키의 장점 이해
 * 
 * @author Modern Java In Action
 */
public class Partitioning {

  public static void main(String... args) {
    // ==================================================
    // 실행 결과 출력
    // ==================================================
    System.out.println("=== 분할 연산 ===\n");
    
    System.out.println("1. 채식/비채식 분할:");
    System.out.println(partitionByVegeterian());
    System.out.println();
    
    System.out.println("2. 채식/비채식 → 타입별 그룹화:");
    System.out.println(vegetarianDishesByType());
    System.out.println();
    
    System.out.println("3. 채식/비채식별 최대 칼로리 요리:");
    System.out.println(mostCaloricPartitionedByVegetarian());
  }

  // ==================================================
  // 1. 기본 분할
  // ==================================================
  /**
   * partitioningBy(Predicate<T> predicate)
   * 
   * 동작:
   * - Predicate 결과에 따라 true/false 두 그룹으로 분할
   * - 항상 두 개의 키 보장
   * - Map<Boolean, List<T>> 반환
   * 
   * 예시:
   * pork(채식X) → false 그룹
   * beef(채식X) → false 그룹
   * french fries(채식O) → true 그룹
   * rice(채식O) → true 그룹
   * 
   * 결과:
   * {
   *   false=[pork, beef, chicken, prawns, salmon],
   *   true=[french fries, rice, season fruit, pizza]
   * }
   * 
   * partitioningBy vs groupingBy:
   * - partitioningBy: Boolean 키, 항상 2개
   * - groupingBy: 임의 키, 0~n개
   */
  private static Map<Boolean, List<Dish>> partitionByVegeterian() {
    return menu.stream()
        .collect(partitioningBy(Dish::isVegetarian));
  }

  // ==================================================
  // 2. 분할 + 그룹화 조합
  // ==================================================
  /**
   * partitioningBy + groupingBy
   * 
   * Map<Boolean, Map<Type, List<Dish>>> 구조:
   * - 1차: 채식 여부 (Boolean)
   * - 2차: 요리 타입 (Type)
   * 
   * 동작:
   * 1. 채식/비채식으로 분할
   * 2. 각 그룹을 타입별로 그룹화
   * 
   * 결과:
   * {
   *   false={
   *     MEAT=[pork, beef, chicken],
   *     FISH=[prawns, salmon]
   *   },
   *   true={
   *     OTHER=[french fries, rice, season fruit, pizza]
   *   }
   * }
   * 
   * 장점:
   * - 두 단계 분류 간결하게 표현
   * - 채식/비채식 그룹 항상 존재
   */
  private static Map<Boolean, Map<Dish.Type, List<Dish>>> vegetarianDishesByType() {
    return menu.stream()
        .collect(
            partitioningBy(
                Dish::isVegetarian,         // 1차: 채식 여부
                groupingBy(Dish::getType)   // 2차: 타입별
            )
        );
  }

  // ==================================================
  // 3. 분할 + collectingAndThen
  // ==================================================
  /**
   * partitioningBy + collectingAndThen + maxBy
   * 
   * 동작:
   * 1. 채식/비채식으로 분할
   * 2. 각 그룹에서 최대 칼로리 요리 찾기
   * 3. Optional 제거
   * 
   * 단계별 실행:
   * 
   * 1. partitioningBy(isVegetarian)
   *    false → [pork(800), beef(700), chicken(400), ...]
   *    true → [french fries(530), rice(350), pizza(550), ...]
   * 
   * 2. maxBy(comparingInt(getCalories))
   *    false → Optional[pork(800)]
   *    true → Optional[pizza(550)]
   * 
   * 3. Optional::get
   *    false → pork(800)
   *    true → pizza(550)
   * 
   * 최종 결과:
   * {
   *   false=pork,
   *   true=pizza
   * }
   * 
   * 결과 타입:
   * Map<Boolean, Dish>
   */
  private static Object mostCaloricPartitionedByVegetarian() {
    return menu.stream()
        .collect(
            partitioningBy(
                Dish::isVegetarian,
                collectingAndThen(
                    maxBy(comparingInt(Dish::getCalories)),
                    Optional::get  // Optional<Dish> → Dish
                )
            )
        );
  }

}

/**
 * 분할 연산 정리
 * 
 * partitioningBy 특징:
 * 1. Boolean 키만 사용
 * 2. 항상 두 개의 키 보장 (true, false)
 * 3. 조건을 만족하는 요소가 없어도 빈 리스트로 키 유지
 * 4. Boolean 조건에 최적화됨
 * 
 * ┌────────────────┬──────────────┬──────────────────┐
 * │   특성         │ partitioningBy │  groupingBy     │
 * ├────────────────┼──────────────┼──────────────────┤
 * │ 키 타입        │ Boolean      │ 임의 타입        │
 * │ 키 개수        │ 항상 2개     │ 0~n개            │
 * │ 빈 그룹        │ 항상 존재    │ 키 없음          │
 * │ 최적화         │ Boolean      │ 일반             │
 * └────────────────┴──────────────┴──────────────────┘
 * 
 * 사용 시나리오:
 * - 조건에 따른 이진 분류 (참/거짓)
 * - 합격/불합격, 통과/실패, 활성/비활성
 * - 빈 그룹도 키로 유지해야 하는 경우
 * 
 * 예시:
 * - 합격/불합격: score >= 60
 * - 성인/미성년: age >= 18
 * - 활성/비활성: isActive
 * 
 * 실무 패턴:
 * 1. 기본 분할:
 *    partitioningBy(predicate)
 * 
 * 2. 분할 + 개수:
 *    partitioningBy(predicate, counting())
 * 
 * 3. 분할 + 그룹화:
 *    partitioningBy(predicate, groupingBy(classifier))
 * 
 * 4. 분할 + 최대값:
 *    partitioningBy(predicate, collectingAndThen(maxBy(), Optional::get))
 */
