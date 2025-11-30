package chapter04.code;

import static java.util.stream.Collectors.toList;
import static chapter04.code.Dish.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 루프 퓨전(Loop Fusion) 완벽 데모
 * 
 * 정의:
 * - 여러 개의 중간 연산을 하나의 패스(pass)로 합쳐서 실행하는 최적화 기법
 * 
 * 효과:
 * - 루프 횟수 감소 (3번 → 1번)
 * - 중간 컬렉션 제거
 * - 캐시 효율 증가
 * - 메모리 절약
 * 
 * 학습 목표:
 * - 전통적 방식의 비효율성 이해
 * - 스트림의 루프 퓨전 메커니즘 파악
 * - 성능 이점 정량 분석
 * 
 * @author Modern Java In Action
 */
public class LoopFusionDemo {

    public static void main(String[] args) {
        demonstrateTraditionalApproach();
        System.out.println("\n" + "=".repeat(60) + "\n");
        demonstrateLoopFusion();
        System.out.println("\n" + "=".repeat(60) + "\n");
        comparePerformance();
        System.out.println("\n" + "=".repeat(60) + "\n");
        demonstrateStatefulOperations();
    }

    /**
     * 전통적 방식: 3번의 루프
     * 
     * 문제점:
     * - 각 연산마다 전체 순회
     * - 중간 컬렉션 2개 생성
     * - 메모리 낭비
     * - 시간 낭비
     */
    private static void demonstrateTraditionalApproach() {
        System.out.println("=== 전통적 방식 (3번의 루프) ===\n");
        System.out.println("작업: 400 칼로리 미만 요리를 찾아 이름을 대문자로 변환\n");
        
        // 루프 1: 필터링
        System.out.println("루프 1: 필터링");
        List<Dish> filtered = new ArrayList<>();
        for (Dish dish : menu) {
            System.out.println("  필터링: " + dish.getName() + 
                             " (" + dish.getCalories() + " kcal)");
            if (dish.getCalories() < 400) {
                filtered.add(dish);
            }
        }
        System.out.println("  중간 결과 1: " + filtered);
        System.out.println("  메모리: " + filtered.size() + "개 저장\n");
        
        // 루프 2: 변환
        System.out.println("루프 2: 변환 (대문자)");
        List<String> mapped = new ArrayList<>();
        for (Dish dish : filtered) {
            String upperName = dish.getName().toUpperCase();
            System.out.println("  변환: " + dish.getName() + " → " + upperName);
            mapped.add(upperName);
        }
        System.out.println("  중간 결과 2: " + mapped);
        System.out.println("  메모리: " + mapped.size() + "개 저장\n");
        
        // 루프 3: 정렬
        System.out.println("루프 3: 정렬");
        Collections.sort(mapped);
        System.out.println("  최종 결과: " + mapped);
        
        System.out.println("\n문제점 분석:");
        System.out.println("✗ 총 " + menu.size() + "개 요소를 3번 순회");
        System.out.println("✗ 중간 컬렉션 2개 생성 (filtered, mapped)");
        System.out.println("✗ 메모리: 원본 + 중간1 + 중간2 = 3배");
        System.out.println("✗ 시간: 3번의 루프");
    }

    /**
     * 스트림 방식: 루프 퓨전 (1번의 루프)
     * 
     * 장점:
     * - 한 번의 순회로 모든 연산 수행
     * - 중간 컬렉션 없음
     * - 메모리 효율적
     * - 시간 효율적
     */
    private static void demonstrateLoopFusion() {
        System.out.println("=== 스트림 방식 (루프 퓨전) ===\n");
        System.out.println("작업: 400 칼로리 미만 요리를 찾아 이름을 대문자로 변환\n");
        
        List<String> result = menu.stream()
            .peek(dish -> System.out.println("시작: " + dish.getName()))
            .filter(dish -> {
                boolean pass = dish.getCalories() < 400;
                System.out.println("  → filter: " + dish.getName() + 
                                 " (" + dish.getCalories() + " kcal) " +
                                 (pass ? "✅ 통과" : "❌ 탈락"));
                return pass;
            })
            .peek(dish -> System.out.println("    → filter 통과!"))
            .map(dish -> {
                String upper = dish.getName().toUpperCase();
                System.out.println("      → map: " + dish.getName() + 
                                 " → " + upper);
                return upper;
            })
            .peek(name -> System.out.println("        → map 완료: " + name))
            .sorted()
            .collect(toList());
        
        System.out.println("\n최종 결과: " + result);
        
        System.out.println("\n실행 과정 분석:");
        System.out.println("- 'pork' → filter(❌) → (버림, 다음 단계 안 감)");
        System.out.println("- 'season fruit' → filter(✅) → map → sorted → collect");
        System.out.println("- 각 요소가 파이프라인 전체를 한 번에 통과!");
        
        System.out.println("\n장점 분석:");
        System.out.println("✓ 총 " + menu.size() + "개 요소를 1번만 순회");
        System.out.println("✓ 중간 컬렉션 0개 (메모리 절약)");
        System.out.println("✓ 메모리: 원본만 유지");
        System.out.println("✓ 시간: 1번의 패스");
    }

    /**
     * 성능 비교: 대용량 데이터
     * 
     * 시나리오: 100만 개 숫자 처리
     */
    private static void comparePerformance() {
        System.out.println("=== 성능 비교 (대용량 데이터) ===\n");
        
        // 100만 개 숫자 생성
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < 1_000_000; i++) {
            numbers.add(i);
        }
        
        System.out.println("데이터: 100만 개 숫자");
        System.out.println("작업: 짝수 필터링 → 제곱 → 합계\n");
        
        // 전통적 방식 (3번의 루프)
        System.out.println("1. 전통적 방식 (3번 루프):");
        long start1 = System.nanoTime();
        
        // 루프 1: 필터링
        List<Integer> filtered = new ArrayList<>();
        for (Integer n : numbers) {
            if (n % 2 == 0) {
                filtered.add(n);
            }
        }
        
        // 루프 2: 변환
        List<Integer> mapped = new ArrayList<>();
        for (Integer n : filtered) {
            mapped.add(n * n);
        }
        
        // 루프 3: 합계
        int sum1 = 0;
        for (Integer n : mapped) {
            sum1 += n;
        }
        
        long end1 = System.nanoTime();
        double time1 = (end1 - start1) / 1_000_000.0;
        
        System.out.println("   결과: " + sum1);
        System.out.println("   시간: " + String.format("%.2f", time1) + " ms");
        System.out.println("   메모리: 원본(100만) + 중간1(50만) + 중간2(50만) = 200만 개");
        
        // 스트림 방식 (루프 퓨전)
        System.out.println("\n2. 스트림 방식 (루프 퓨전):");
        long start2 = System.nanoTime();
        
        int sum2 = numbers.stream()
            .filter(n -> n % 2 == 0)
            .map(n -> n * n)
            .mapToInt(Integer::intValue)
            .sum();
        
        long end2 = System.nanoTime();
        double time2 = (end2 - start2) / 1_000_000.0;
        
        System.out.println("   결과: " + sum2);
        System.out.println("   시간: " + String.format("%.2f", time2) + " ms");
        System.out.println("   메모리: 원본(100만) + 중간(0) = 100만 개");
        
        System.out.println("\n성능 비교:");
        System.out.println("   시간: " + String.format("%.1f", time1 / time2) + "배 빠름");
        System.out.println("   메모리: 절반 사용 (200만 → 100만)");
    }

    /**
     * 상태 있는 연산에서의 루프 퓨전 한계
     * 
     * 상태 없는 연산 (완벽한 퓨전):
     * - filter, map, flatMap
     * 
     * 상태 있는 연산 (퓨전 제한):
     * - sorted, distinct
     */
    private static void demonstrateStatefulOperations() {
        System.out.println("=== 상태 있는 연산의 루프 퓨전 한계 ===\n");
        
        System.out.println("1. 상태 없는 연산 (완벽한 퓨전):");
        List<String> result1 = menu.stream()
            .peek(d -> System.out.println("  시작: " + d.getName()))
            .filter(d -> d.getCalories() < 400)  // 상태 없음
            .peek(d -> System.out.println("    filter 통과"))
            .map(Dish::getName)                  // 상태 없음
            .peek(n -> System.out.println("      map 완료: " + n))
            .limit(3)
            .collect(toList());
        
        System.out.println("  → 각 요소가 파이프라인 전체를 한 번에 통과!\n");
        
        System.out.println("2. 상태 있는 연산 (퓨전 제한):");
        List<String> result2 = menu.stream()
            .peek(d -> System.out.println("  시작: " + d.getName()))
            .filter(d -> d.getCalories() < 400)
            .peek(d -> System.out.println("    filter 통과"))
            .sorted(Comparator.comparing(Dish::getCalories))  // 상태 있음!
            .peek(d -> System.out.println("      sorted 완료: " + d.getName()))
            .map(Dish::getName)
            .peek(n -> System.out.println("        map 완료: " + n))
            .limit(3)
            .collect(toList());
        
        System.out.println("\n설명:");
        System.out.println("- sorted는 모든 요소를 봐야 정렬 가능");
        System.out.println("- 따라서 filter 후 모든 요소가 sorted에 도달");
        System.out.println("- sorted 이후의 map은 다시 퓨전 가능");
        
        System.out.println("\n실행 단계:");
        System.out.println("1단계: filter 실행 (모든 요소)");
        System.out.println("2단계: sorted 실행 (중간 버퍼 생성)");
        System.out.println("3단계: map + limit 실행 (퓨전)");
    }

    /**
     * 루프 퓨전 원리 (내부 구현 - 개념적)
     * 
     * 스트림 코드:
     * ```java
     * stream
     *     .filter(condition1)
     *     .map(transformation)
     *     .filter(condition2)
     *     .collect(toList());
     * ```
     * 
     * 내부적으로 실행되는 로직:
     * ```java
     * List<R> result = new ArrayList<>();
     * for (T element : source) {
     *     if (condition1(element)) {        // filter
     *         R transformed = transformation(element);  // map
     *         if (condition2(transformed)) {  // filter
     *             result.add(transformed);    // collect
     *         }
     *     }
     * }
     * ```
     * 
     * 핵심:
     * - 모든 중간 연산이 하나의 루프 안에서 실행
     * - 중간 컬렉션 생성 없음
     * - 한 번의 패스로 완료
     * 
     * 루프 퓨전이 불가능한 경우:
     * ```java
     * stream
     *     .filter(condition)
     *     .sorted()           // ← 여기서 끊김!
     *     .map(transformation)
     *     .collect(toList());
     * ```
     * 
     * 실행 과정:
     * ```java
     * // 1단계: filter 실행
     * List<T> filtered = new ArrayList<>();
     * for (T element : source) {
     *     if (condition(element)) {
     *         filtered.add(element);
     *     }
     * }
     * 
     * // 2단계: sorted 실행
     * filtered.sort(...);
     * 
     * // 3단계: map + collect 실행 (다시 퓨전)
     * List<R> result = new ArrayList<>();
     * for (T element : filtered) {
     *     result.add(transformation(element));
     * }
     * ```
     */
}
