package chapter05.code;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import chapter04.code.Dish;

/**
 * 기본형 특화 스트림 종합 예제
 * 
 * 기본형 특화 스트림:
 * - IntStream    : int 전용 스트림
 * - LongStream   : long 전용 스트림
 * - DoubleStream : double 전용 스트림
 * 
 * 학습 목표:
 * 1. 박싱 비용 문제와 기본형 스트림의 필요성
 * 2. mapToInt/Long/Double 변환
 * 3. boxed()로 객체 스트림 복원
 * 4. 기본형 스트림 전용 메서드 활용
 * 5. 숫자 범위 생성 (range, rangeClosed)
 * 
 * @author Modern Java In Action
 */
public class NumericStreams {

  public static void main(String... args) {
    
    // ==================================================
    // 1. Arrays.stream으로 배열 출력
    // ==================================================
    System.out.println("=== 1. Arrays.stream ===");
    
    /**
     * Arrays.stream(배열)
     * 
     * 배열을 스트림으로 변환
     * 기본형 배열도 지원
     */
    List<Integer> numbers = Arrays.asList(3, 4, 5, 1, 2);
    System.out.println("배열 요소:");
    Arrays.stream(numbers.toArray())
        .forEach(System.out::println);
    System.out.println();

    // ==================================================
    // 2. mapToInt로 총 칼로리 계산
    // ==================================================
    System.out.println("=== 2. mapToInt for sum ===");
    
    /**
     * mapToInt(ToIntFunction)
     * 
     * Stream<T> → IntStream 변환
     * 
     * 장점:
     * - 박싱 비용 제거 (3~5배 빠름)
     * - 전용 메서드 사용 가능: sum, average, max, min
     * 
     * 예시:
     * Stream<Dish> → IntStream (칼로리)
     */
    int calories = Dish.menu.stream()
        .mapToInt(Dish::getCalories)
        .sum();
    System.out.println("총 칼로리: " + calories + " kcal");
    System.out.println();

    // ==================================================
    // 3. OptionalInt와 max
    // ==================================================
    System.out.println("=== 3. OptionalInt with max ===");
    
    /**
     * OptionalInt
     * 
     * Optional<Integer>와 다름:
     * - Optional<Integer>: 객체 (박싱)
     * - OptionalInt: 기본형 (박싱 없음)
     * 
     * 메서드:
     * - getAsInt(): int 값 반환
     * - orElse(defaultValue): 값 없으면 기본값
     * - isPresent(): 값 존재 여부
     */
    OptionalInt maxCalories = Dish.menu.stream()
        .mapToInt(Dish::getCalories)
        .max();

    int max;
    if (maxCalories.isPresent()) {
      max = maxCalories.getAsInt();
    } else {
      max = 1;  // 기본값
    }
    System.out.println("최대 칼로리: " + max + " kcal");
    System.out.println();

    // ==================================================
    // 4. rangeClosed로 1~100 짝수 개수
    // ==================================================
    System.out.println("=== 4. rangeClosed for even numbers ===");
    
    /**
     * IntStream.rangeClosed(start, end)
     * 
     * [start, end] 범위의 숫자 생성 (끝 포함)
     * 
     * range vs rangeClosed:
     * - range(1, 100): [1, 100) → 1~99
     * - rangeClosed(1, 100): [1, 100] → 1~100
     * 
     * 특징:
     * - 메모리 효율적 (지연 생성)
     * - 무한 스트림이 아님
     */
    IntStream evenNumbers = IntStream.rangeClosed(1, 100)
        .filter(n -> n % 2 == 0);
    System.out.println("1~100 짝수 개수: " + evenNumbers.count() + "개");
    System.out.println();

    // ==================================================
    // 5. 피타고라스 수 (버전 1)
    // ==================================================
    System.out.println("=== 5. Pythagorean triples (v1) ===");
    
    /**
     * 피타고라스 수 찾기
     * 
     * 조건: a² + b² = c² (a < b < c)
     * 
     * 방법:
     * 1. a 범위: 1~100
     * 2. b 범위: a~100
     * 3. c 계산: sqrt(a² + b²)
     * 4. c가 정수인지 확인
     */
    Stream<int[]> pythagoreanTriples = IntStream.rangeClosed(1, 100).boxed()
        .flatMap(a -> IntStream.rangeClosed(a, 100)
            .filter(b -> Math.sqrt(a * a + b * b) % 1 == 0).boxed()
            .map(b -> new int[] { a, b, (int) Math.sqrt(a * a + b * b) }));
    
    System.out.println("처음 5개:");
    pythagoreanTriples.limit(5)
        .forEach(t -> System.out.println(t[0] + "² + " + t[1] + "² = " + t[2] + "²"));
    System.out.println();

    // ==================================================
    // 6. 피타고라스 수 (버전 2 - 최적화)
    // ==================================================
    System.out.println("=== 6. Pythagorean triples (v2 - optimized) ===");
    
    /**
     * 최적화 버전
     * 
     * 개선점:
     * - sqrt 계산을 한 번만
     * - filter를 mapToObj 안으로 이동
     */
    Stream<int[]> pythagoreanTriples2 = IntStream.rangeClosed(1, 100).boxed()
        .flatMap(a -> IntStream.rangeClosed(a, 100)
            .mapToObj(b -> new double[]{a, b, Math.sqrt(a * a + b * b)})
            .filter(t -> t[2] % 1 == 0))
        .map(array -> Arrays.stream(array).mapToInt(a -> (int) a).toArray());
    
    System.out.println("처음 5개:");
    pythagoreanTriples2.limit(5)
        .forEach(t -> System.out.println(t[0] + "² + " + t[1] + "² = " + t[2] + "²"));
  }

  /**
   * 완전 제곱수 확인
   */
  public static boolean isPerfectSquare(int n) {
    return Math.sqrt(n) % 1 == 0;
  }
}

/**
 * 기본형 특화 스트림 정리
 * 
 * ┌──────────────┬─────────────┬──────────────┬──────────────┐
 * │   스트림     │  기본형     │  객체 스트림 │  전용 메서드 │
 * ├──────────────┼─────────────┼──────────────┼──────────────┤
 * │ IntStream    │ int         │ Stream<Int>  │ sum,avg,max  │
 * │ LongStream   │ long        │ Stream<Long> │ sum,avg,max  │
 * │ DoubleStream │ double      │ Stream<Dbl>  │ sum,avg,max  │
 * └──────────────┴─────────────┴──────────────┴──────────────┘
 * 
 * 변환 메서드:
 * - Stream → 기본형: mapToInt, mapToLong, mapToDouble
 * - 기본형 → Stream: boxed()
 * - 기본형 간: asLongStream, asDoubleStream
 * 
 * 박싱 비용:
 * - int → Integer: 메모리 4배, 속도 3~5배 느림
 * - 기본형 스트림: 박싱 없음 → 빠름
 * 
 * 숫자 범위:
 * - range(1, 100): [1, 100) → 99개
 * - rangeClosed(1, 100): [1, 100] → 100개
 */
