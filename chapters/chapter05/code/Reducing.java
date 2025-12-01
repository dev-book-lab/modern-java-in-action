package chapter05.code;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import chapter04.code.Dish;

/**
 * 리듀싱 연산 종합 예제
 * 
 * reduce 연산:
 * - reduce(초기값, BinaryOperator) : T 반환
 * - reduce(BinaryOperator)          : Optional<T> 반환
 * 
 * 학습 목표:
 * 1. reduce의 동작 원리 완벽 이해
 * 2. 초기값 있음/없음의 차이점
 * 3. 실전 예제: 합계, 최대/최소, 곱셈
 * 4. reduce를 이용한 다양한 집계 연산
 * 
 * @author Modern Java In Action
 */
public class Reducing {

  public static void main(String... args) {
    
    // ==================================================
    // 1. reduce로 합계 구하기 (람다)
    // ==================================================
    System.out.println("=== 1. Sum with lambda ===");
    
    /**
     * reduce(T identity, BinaryOperator<T> accumulator)
     * 
     * 동작 방식:
     * - identity: 초기값 (계산의 시작점)
     * - accumulator: 두 요소를 결합하는 함수
     * - 결과: 스트림의 모든 요소를 하나의 값으로 축약
     * 
     * 계산 과정 (합계):
     * numbers = [3, 4, 5, 1, 2]
     * reduce(0, (a,b) -> a + b)
     * 
     * 0 + 3 = 3
     * 3 + 4 = 7
     * 7 + 5 = 12
     * 12 + 1 = 13
     * 13 + 2 = 15  ← 최종 결과
     * 
     * 특징:
     * - 초기값이 있으므로 빈 스트림에도 안전
     * - 반환 타입: T (Optional 아님)
     * - 초기값은 항등원(identity element)이어야 함
     */
    List<Integer> numbers = Arrays.asList(3, 4, 5, 1, 2);
    System.out.println("숫자: " + numbers);
    
    int sum = numbers.stream().reduce(0, (a, b) -> a + b);
    System.out.println("합계 (람다): " + sum);
    System.out.println();

    // ==================================================
    // 2. reduce로 합계 구하기 (메서드 참조)
    // ==================================================
    System.out.println("=== 2. Sum with method reference ===");
    
    /**
     * 메서드 참조 사용
     * 
     * (a, b) -> a + b  ==  Integer::sum
     * 
     * 더 간결하고 읽기 쉬운 코드
     */
    int sum2 = numbers.stream().reduce(0, Integer::sum);
    System.out.println("합계 (메서드 참조): " + sum2);
    System.out.println();

    // ==================================================
    // 3. reduce로 최대값 구하기
    // ==================================================
    System.out.println("=== 3. Maximum ===");
    
    /**
     * reduce로 최대값 찾기
     * 
     * (a, b) -> Integer.max(a, b)
     * 
     * 동작 과정:
     * numbers = [3, 4, 5, 1, 2]
     * 
     * max(0, 3) = 3
     * max(3, 4) = 4
     * max(4, 5) = 5
     * max(5, 1) = 5
     * max(5, 2) = 5  ← 최종 결과
     * 
     * 주의: 초기값 0 때문에 모든 요소가 음수면 잘못된 결과
     * → 초기값 없는 reduce 사용 권장
     */
    int max = numbers.stream().reduce(0, (a, b) -> Integer.max(a, b));
    System.out.println("최대값 (초기값 0): " + max);
    System.out.println();

    // ==================================================
    // 4. reduce로 최소값 구하기 (초기값 없음)
    // ==================================================
    System.out.println("=== 4. Minimum without initial value ===");
    
    /**
     * reduce(BinaryOperator<T> accumulator)
     * 
     * 동작 방식:
     * - 초기값 없음 → 첫 번째 요소를 초기값으로 사용
     * - 빈 스트림이면? → Optional.empty() 반환
     * 
     * 계산 과정:
     * numbers = [3, 4, 5, 1, 2]
     * reduce(Integer::min)
     * 
     * 3 (첫 요소가 초기값)
     * min(3, 4) = 3
     * min(3, 5) = 3
     * min(3, 1) = 1
     * min(1, 2) = 1  ← 최종 결과
     * 
     * 초기값 있음 vs 없음:
     * - 초기값 있음: T 반환, 빈 스트림 = 초기값
     * - 초기값 없음: Optional<T> 반환, 빈 스트림 = Optional.empty()
     * 
     * 왜 Optional?
     * - 빈 스트림에서는 결과가 없음
     * - null 대신 Optional로 안전하게 표현
     */
    Optional<Integer> min = numbers.stream().reduce(Integer::min);
    min.ifPresent(m -> System.out.println("최소값: " + m));
    System.out.println();

    // ==================================================
    // 5. 실전 예제: 총 칼로리 계산
    // ==================================================
    System.out.println("=== 5. Total calories ===");
    
    /**
     * 메뉴의 총 칼로리 계산
     * 
     * 방법:
     * 1. map(Dish::getCalories): Stream<Integer>
     * 2. reduce(0, Integer::sum): 모든 칼로리 합산
     * 
     * 동작 과정:
     * menu.map(Dish::getCalories): [800, 600, 450, ...]
     * reduce(0, Integer::sum): 모든 칼로리 합산
     */
    int calories = Dish.menu.stream()
        .map(Dish::getCalories)
        .reduce(0, Integer::sum);
    System.out.println("총 칼로리: " + calories + " kcal");
  }
}

/**
 * reduce 연산 정리
 * 
 * ┌──────────────────┬─────────────┬──────────────┬──────────────┐
 * │   형태           │  반환 타입  │   빈 스트림  │   사용 예    │
 * ├──────────────────┼─────────────┼──────────────┼──────────────┤
 * │ reduce(초기값,f) │ T           │ 초기값 반환  │ 안전한 연산  │
 * │ reduce(f)        │ Optional<T> │ empty() 반환 │ 결과 불확실  │
 * └──────────────────┴─────────────┴──────────────┴──────────────┘
 * 
 * 초기값 선택:
 * - 합계: 0 (a + 0 = a)
 * - 곱셈: 1 (a * 1 = a)
 * - 문자열 결합: "" (a + "" = a)
 * - 최대값: Integer.MIN_VALUE
 * - 최소값: Integer.MAX_VALUE
 * 
 * reduce vs 다른 연산:
 * ┌────────────┬──────────────────┬─────────────────┐
 * │   목적     │   reduce         │   전용 메서드   │
 * ├────────────┼──────────────────┼─────────────────┤
 * │ 합계       │ reduce(0, sum)   │ sum() ✓         │
 * │ 개수       │ reduce(0, +1)    │ count() ✓       │
 * │ 최대       │ reduce(max)      │ max() ✓         │
 * │ 최소       │ reduce(min)      │ min() ✓         │
 * └────────────┴──────────────────┴─────────────────┘
 * 
 * 권장사항:
 * - 전용 메서드가 있으면 그것을 사용 (더 명확하고 효율적)
 * - 커스텀 연산은 reduce 사용
 */
