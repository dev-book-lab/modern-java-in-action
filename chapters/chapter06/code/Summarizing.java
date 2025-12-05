package chapter06.code;

import static chapter06.code.Dish.menu;
import static java.util.stream.Collectors.averagingInt;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.summarizingInt;
import static java.util.stream.Collectors.summingInt;

import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.function.BinaryOperator;

/**
 * 리듀싱과 요약 연산 종합 예제
 * 
 * Collectors 클래스의 다양한 리듀싱/요약 메서드:
 * - counting()         : 개수 세기
 * - summingInt()       : 합계 계산
 * - averagingInt()     : 평균 계산
 * - summarizingInt()   : 통계 정보 (count, sum, min, max, average)
 * - joining()          : 문자열 연결
 * - reducing()         : 범용 리듀싱 연산
 * 
 * 학습 목표:
 * 1. 기본 집계 연산 활용
 * 2. reducing의 다양한 형태 이해
 * 3. 통계 정보 한 번에 수집
 * 4. 문자열 연결의 효율적 방법
 * 
 * @author Modern Java In Action
 */
public class Summarizing {

  public static void main(String... args) {
    // ==================================================
    // 실행 결과 출력
    // ==================================================
    System.out.println("=== 리듀싱과 요약 연산 ===\n");
    
    System.out.println("요리 개수: " + howManyDishes());
    System.out.println("가장 칼로리 높은 요리: " + findMostCaloricDish());
    System.out.println("가장 칼로리 높은 요리 (Comparator): " + findMostCaloricDishUsingComparator());
    System.out.println("총 칼로리: " + calculateTotalCalories() + " kcal");
    System.out.println("평균 칼로리: " + calculateAverageCalories() + " kcal");
    System.out.println("메뉴 통계: " + calculateMenuStatistics());
    System.out.println("\n요리 목록:");
    System.out.println("  공백 없이: " + getShortMenu());
    System.out.println("  쉼표 구분: " + getShortMenuCommaSeparated());
  }

  // ==================================================
  // 1. counting() - 개수 세기
  // ==================================================
  /**
   * counting()
   * 
   * 동작:
   * - 스트림의 요소 개수 반환
   * - Long 타입 반환
   * 
   * 간단한 대안:
   * - stream.count()
   * 
   * 사용 이유:
   * - 다운스트림 컬렉터로 사용할 때 유용
   * - 예: groupingBy(Dish::getType, counting())
   */
  private static long howManyDishes() {
    return menu.stream().collect(counting());
    
    // 대안: menu.stream().count()
  }

  // ==================================================
  // 2. reducing() - 최대값 찾기 (람다)
  // ==================================================
  /**
   * reducing(BinaryOperator<T> op)
   * 
   * 한 개의 인수를 받는 reducing:
   * - 초기값 없음
   * - 스트림의 첫 번째 요소가 초기값
   * - Optional<T> 반환 (빈 스트림 가능)
   * 
   * 동작:
   * 1. 첫 번째 요소를 accumulator로 설정
   * 2. 나머지 요소들과 BinaryOperator 적용
   * 3. 최종 결과 반환
   * 
   * 예시:
   * [pork(800), beef(700), chicken(400)]
   * 
   * accumulator = pork(800)
   * beef(700) 비교 → pork(800) 유지
   * chicken(400) 비교 → pork(800) 유지
   * 
   * 결과: pork(800)
   */
  private static Dish findMostCaloricDish() {
    return menu.stream()
        .collect(reducing(
            (d1, d2) -> d1.getCalories() > d2.getCalories() ? d1 : d2
        ))
        .get();  // Optional.get() - 값이 있다고 확신할 때만 사용
  }

  // ==================================================
  // 3. reducing() - 최대값 찾기 (Comparator)
  // ==================================================
  /**
   * reducing(BinaryOperator<T> op)
   * 
   * Comparator와 BinaryOperator 조합:
   * 1. Comparator 생성
   * 2. BinaryOperator.maxBy()로 변환
   * 3. reducing에 전달
   * 
   * 장점:
   * - 코드 재사용성
   * - 가독성 향상
   * - 정렬 기준 변경 용이
   */
  private static Dish findMostCaloricDishUsingComparator() {
    // 1. Comparator 생성
    Comparator<Dish> dishCaloriesComparator = Comparator.comparingInt(Dish::getCalories);
    
    // 2. BinaryOperator로 변환
    BinaryOperator<Dish> moreCaloricOf = BinaryOperator.maxBy(dishCaloriesComparator);
    
    // 3. reducing 적용
    return menu.stream()
        .collect(reducing(moreCaloricOf))
        .get();
  }

  // ==================================================
  // 4. summingInt() - 합계 계산
  // ==================================================
  /**
   * summingInt(ToIntFunction<T> mapper)
   * 
   * 동작:
   * - 각 요소를 int로 매핑
   * - 모든 int 값을 합산
   * - int 반환 (박싱 없음)
   * 
   * 유사 메서드:
   * - summingLong(ToLongFunction)
   * - summingDouble(ToDoubleFunction)
   * 
   * 예시:
   * pork(800) + beef(700) + chicken(400) + ... = 4200
   */
  private static int calculateTotalCalories() {
    return menu.stream()
        .collect(summingInt(Dish::getCalories));
  }

  // ==================================================
  // 5. averagingInt() - 평균 계산
  // ==================================================
  /**
   * averagingInt(ToIntFunction<T> mapper)
   * 
   * 동작:
   * - 각 요소를 int로 매핑
   * - 합계와 개수를 동시에 계산
   * - 평균 반환 (Double 타입)
   * 
   * 유사 메서드:
   * - averagingLong(ToLongFunction)
   * - averagingDouble(ToDoubleFunction)
   * 
   * 예시:
   * 총 합: 4200, 개수: 9
   * 평균: 4200 / 9 = 466.67
   */
  private static Double calculateAverageCalories() {
    return menu.stream()
        .collect(averagingInt(Dish::getCalories));
  }

  // ==================================================
  // 6. summarizingInt() - 통계 정보
  // ==================================================
  /**
   * summarizingInt(ToIntFunction<T> mapper)
   * 
   * 동작:
   * - 한 번의 순회로 모든 통계 수집
   * - IntSummaryStatistics 객체 반환
   * 
   * IntSummaryStatistics:
   * - getCount()    : 개수
   * - getSum()      : 합계
   * - getMin()      : 최소값
   * - getMax()      : 최대값
   * - getAverage()  : 평균
   * 
   * 장점:
   * - 효율적 (한 번의 순회)
   * - 여러 통계 정보 동시 수집
   * 
   * 유사 메서드:
   * - summarizingLong(ToLongFunction)
   * - summarizingDouble(ToDoubleFunction)
   */
  private static IntSummaryStatistics calculateMenuStatistics() {
    return menu.stream()
        .collect(summarizingInt(Dish::getCalories));
    
    // 사용 예:
    // IntSummaryStatistics stats = calculateMenuStatistics();
    // stats.getCount()    → 9
    // stats.getSum()      → 4200
    // stats.getMin()      → 120
    // stats.getMax()      → 800
    // stats.getAverage()  → 466.67
  }

  // ==================================================
  // 7. joining() - 문자열 연결 (구분자 없음)
  // ==================================================
  /**
   * joining()
   * 
   * 동작:
   * - 각 요소의 toString() 호출
   * - 모든 문자열을 연결
   * - 구분자 없음
   * 
   * 내부 구현:
   * - StringBuilder 사용
   * - 효율적인 문자열 연결
   * 
   * 주의:
   * - map(Dish::getName) 필요
   * - Dish 객체를 직접 joining하면 toString() 결과 사용
   */
  private static String getShortMenu() {
    return menu.stream()
        .map(Dish::getName)  // Dish → String 변환
        .collect(joining());
    
    // 결과: "porkbeefchickenfrench friesrice..."
  }

  // ==================================================
  // 8. joining() - 문자열 연결 (구분자 있음)
  // ==================================================
  /**
   * joining(CharSequence delimiter)
   * 
   * 동작:
   * - 각 요소의 toString() 호출
   * - delimiter로 구분하여 연결
   * 
   * 오버로드 버전:
   * - joining()                      : 구분자 없음
   * - joining(delimiter)             : 구분자만
   * - joining(delimiter, prefix, suffix) : 구분자 + 접두사/접미사
   * 
   * 예시:
   * joining(", ", "[", "]")
   * → "[pork, beef, chicken, ...]"
   */
  private static String getShortMenuCommaSeparated() {
    return menu.stream()
        .map(Dish::getName)
        .collect(joining(", "));
    
    // 결과: "pork, beef, chicken, french fries, rice, ..."
  }

}

/**
 * 리듀싱과 요약 연산 정리
 * 
 * ┌──────────────────┬────────────────┬──────────────────┐
 * │   메서드         │   반환 타입    │   설명           │
 * ├──────────────────┼────────────────┼──────────────────┤
 * │ counting()       │ Long           │ 개수             │
 * │ summingInt()     │ Integer        │ 합계             │
 * │ averagingInt()   │ Double         │ 평균             │
 * │ summarizingInt() │ IntSummary..   │ 통계 (한 번에)   │
 * │ joining()        │ String         │ 문자열 연결      │
 * │ reducing()       │ Optional<T>/T  │ 범용 리듀싱      │
 * └──────────────────┴────────────────┴──────────────────┘
 * 
 * reducing의 세 가지 형태:
 * 1. reducing(BinaryOperator<T>)
 *    - 초기값 없음
 *    - Optional<T> 반환
 * 
 * 2. reducing(T identity, BinaryOperator<T>)
 *    - 초기값 있음
 *    - T 반환
 * 
 * 3. reducing(U identity, Function<T, U>, BinaryOperator<U>)
 *    - 초기값 + 변환 함수
 *    - U 반환
 * 
 * 선택 가이드:
 * - 개수만 필요 → counting() 또는 count()
 * - 합계 → summingInt() 또는 mapToInt().sum()
 * - 평균 → averagingInt()
 * - 여러 통계 → summarizingInt()
 * - 문자열 연결 → joining()
 * - 커스텀 리듀싱 → reducing()
 */
