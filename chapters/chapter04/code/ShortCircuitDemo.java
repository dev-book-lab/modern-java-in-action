package chapter04.code;

import static chapter04.code.Dish.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 쇼트서킷(Short-circuit) 완벽 데모
 * 
 * 정의:
 * - 모든 요소를 처리하지 않고도 결과를 반환할 수 있는 연산
 * 
 * 종류:
 * - 중간 연산: limit(n)
 * - 최종 연산: anyMatch(), allMatch(), noneMatch(), findFirst(), findAny()
 * 
 * 학습 목표:
 * - 각 쇼트서킷 연산의 동작 방식 이해
 * - 성능 이점 정량적 분석
 * - 실전 활용 패턴 학습
 * 
 * @author Modern Java In Action
 */
public class ShortCircuitDemo {

    public static void main(String[] args) {
        demonstrateLimit();
        System.out.println("\n" + "=".repeat(60) + "\n");
        demonstrateAnyMatch();
        System.out.println("\n" + "=".repeat(60) + "\n");
        demonstrateAllMatch();
        System.out.println("\n" + "=".repeat(60) + "\n");
        demonstrateFindFirst();
        System.out.println("\n" + "=".repeat(60) + "\n");
        demonstratePerformanceBenefit();
    }

    /**
     * limit() - 지정한 개수만 처리
     * 
     * 특징:
     * - n개를 찾으면 즉시 중단
     * - 무한 스트림에도 사용 가능
     */
    private static void demonstrateLimit() {
        System.out.println("=== limit() 쇼트서킷 ===\n");
        
        System.out.println("시나리오: 처음 3개의 고칼로리 요리 찾기\n");
        
        List<String> highCalorieDishes = menu.stream()
            .peek(dish -> System.out.println("처리: " + dish.getName()))
            .filter(dish -> {
                boolean isHigh = dish.getCalories() > 300;
                System.out.println("  → " + dish.getName() + 
                                 " (칼로리: " + dish.getCalories() + 
                                 ") " + (isHigh ? "✅ 통과" : "❌ 탈락"));
                return isHigh;
            })
            .limit(3)  // 3개만!
            .map(dish -> {
                System.out.println("    → 이름 추출: " + dish.getName());
                return dish.getName();
            })
            .collect(java.util.stream.Collectors.toList());
        
        System.out.println("\n결과: " + highCalorieDishes);
        System.out.println("\n분석:");
        System.out.println("- 전체 메뉴: " + menu.size() + "개");
        System.out.println("- 3개 찾자마자 중단!");
        System.out.println("- 나머지는 처리조차 안 함");
    }

    /**
     * anyMatch() - 하나라도 조건 만족?
     * 
     * 특징:
     * - 조건을 만족하는 요소 발견 즉시 true 반환
     * - 나머지 요소는 검사하지 않음
     */
    private static void demonstrateAnyMatch() {
        System.out.println("=== anyMatch() 쇼트서킷 ===\n");
        
        System.out.println("질문: 채식 요리가 있나요?\n");
        
        boolean hasVegetarianDish = menu.stream()
            .peek(dish -> System.out.println("검사: " + dish.getName()))
            .anyMatch(dish -> {
                boolean isVegetarian = dish.isVegetarian();
                System.out.println("  → " + (isVegetarian ? 
                    "✅ 채식 요리 발견!" : "❌ 고기/생선 요리"));
                return isVegetarian;
            });
        
        System.out.println("\n결과: " + (hasVegetarianDish ? "있음" : "없음"));
        
        System.out.println("\n\n질문: 1000 칼로리가 넘는 요리가 있나요?\n");
        
        boolean hasHighCalorie = menu.stream()
            .peek(dish -> System.out.println("검사: " + dish.getName() + 
                                           " (" + dish.getCalories() + " kcal)"))
            .anyMatch(dish -> dish.getCalories() > 1000);
        
        System.out.println("\n결과: " + (hasHighCalorie ? "있음" : "없음"));
        System.out.println("→ 모든 요리를 검사했지만 찾지 못함");
    }

    /**
     * allMatch() - 모든 요소가 조건 만족?
     * 
     * 특징:
     * - 조건을 만족하지 않는 요소 발견 즉시 false 반환
     * - 하나라도 실패하면 나머지 검사 안 함
     */
    private static void demonstrateAllMatch() {
        System.out.println("=== allMatch() 쇼트서킷 ===\n");
        
        System.out.println("질문: 모든 요리가 1000 칼로리 미만인가요?\n");
        
        boolean allLowCalorie = menu.stream()
            .peek(dish -> System.out.println("검사: " + dish.getName() + 
                                           " (" + dish.getCalories() + " kcal)"))
            .allMatch(dish -> {
                boolean isLow = dish.getCalories() < 1000;
                System.out.println("  → " + (isLow ? "✅ 조건 만족" : "❌ 조건 불만족"));
                return isLow;
            });
        
        System.out.println("\n결과: " + (allLowCalorie ? "모두 만족" : "일부 불만족"));
        System.out.println("→ 모든 요리가 1000 칼로리 미만이므로 전체 검사");
        
        System.out.println("\n\n질문: 모든 요리가 500 칼로리 미만인가요?\n");
        
        boolean allVeryLowCalorie = menu.stream()
            .peek(dish -> System.out.println("검사: " + dish.getName() + 
                                           " (" + dish.getCalories() + " kcal)"))
            .allMatch(dish -> {
                boolean isVeryLow = dish.getCalories() < 500;
                System.out.println("  → " + (isVeryLow ? "✅ 조건 만족" : "❌ 조건 불만족 - 중단!"));
                return isVeryLow;
            });
        
        System.out.println("\n결과: " + (allVeryLowCalorie ? "모두 만족" : "일부 불만족"));
        System.out.println("→ pork(800)에서 실패 → 즉시 중단!");
    }

    /**
     * findFirst() - 첫 번째 요소 찾기
     * 
     * 특징:
     * - 첫 번째 조건 만족 요소 발견 즉시 반환
     * - 순서가 보장됨 (순차/병렬 모두)
     */
    private static void demonstrateFindFirst() {
        System.out.println("=== findFirst() 쇼트서킷 ===\n");
        
        System.out.println("질문: 첫 번째 채식 요리는?\n");
        
        Optional<Dish> firstVegetarian = menu.stream()
            .peek(dish -> System.out.println("검사: " + dish.getName()))
            .filter(dish -> {
                boolean isVegetarian = dish.isVegetarian();
                System.out.println("  → " + (isVegetarian ? 
                    "✅ 채식 요리 발견! 검색 중단" : "❌ 고기/생선"));
                return isVegetarian;
            })
            .findFirst();
        
        firstVegetarian.ifPresent(dish -> {
            System.out.println("\n결과: " + dish.getName());
            System.out.println("→ french fries 발견 후 즉시 중단");
            System.out.println("→ rice, season fruit, pizza는 검사조차 안 함!");
        });
        
        // findAny와 비교
        System.out.println("\n\n=== findAny() vs findFirst() ===\n");
        System.out.println("순차 스트림에서는 결과가 같음:");
        
        Optional<Dish> anyVegetarian = menu.stream()
            .filter(Dish::isVegetarian)
            .findAny();
        
        System.out.println("findFirst: " + firstVegetarian.get().getName());
        System.out.println("findAny: " + anyVegetarian.get().getName());
        
        System.out.println("\n병렬 스트림에서는 findAny가 더 빠름:");
        System.out.println("- findFirst: 순서 보장을 위해 동기화 필요");
        System.out.println("- findAny: 어느 스레드든 먼저 찾으면 반환");
    }

    /**
     * 쇼트서킷 성능 이점 정량 분석
     * 
     * 시나리오: 100만 개 숫자 중 첫 번째 짝수 찾기
     */
    private static void demonstratePerformanceBenefit() {
        System.out.println("=== 쇼트서킷 성능 이점 ===\n");
        
        // 100만 개 숫자 생성
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < 1_000_000; i++) {
            numbers.add(i);
        }
        
        System.out.println("데이터: 100만 개 숫자 (0 ~ 999,999)");
        System.out.println("작업: 첫 번째 짝수 찾기\n");
        
        // 쇼트서킷 없음 (전체 검사)
        System.out.println("1. 쇼트서킷 없음 (모든 짝수 찾고 첫 번째 선택):");
        long start1 = System.nanoTime();
        
        List<Integer> allEven = numbers.stream()
            .filter(n -> n % 2 == 0)
            .collect(java.util.stream.Collectors.toList());
        Integer first1 = allEven.get(0);
        
        long end1 = System.nanoTime();
        double time1 = (end1 - start1) / 1_000_000.0;
        
        System.out.println("   결과: " + first1);
        System.out.println("   시간: " + String.format("%.2f", time1) + " ms");
        System.out.println("   처리: 1,000,000개 모두 검사");
        
        // 쇼트서킷 사용
        System.out.println("\n2. 쇼트서킷 사용 (findFirst):");
        long start2 = System.nanoTime();
        
        Optional<Integer> first2 = numbers.stream()
            .filter(n -> n % 2 == 0)
            .findFirst();
        
        long end2 = System.nanoTime();
        double time2 = (end2 - start2) / 1_000_000.0;
        
        System.out.println("   결과: " + first2.get());
        System.out.println("   시간: " + String.format("%.2f", time2) + " ms");
        System.out.println("   처리: 1개만 검사 (0이 짝수이므로)");
        
        System.out.println("\n성능 비교:");
        System.out.println("   속도 향상: " + String.format("%.0f", time1 / time2) + "배");
        System.out.println("   처리 비율: 0.0001% (1/1,000,000)");
        
        // 무한 스트림 예제
        System.out.println("\n\n3. 무한 스트림에서의 쇼트서킷:");
        System.out.println("   작업: 무한 수열에서 처음 10개의 짝수");
        
        List<Integer> first10Even = Stream.iterate(0, n -> n + 1)
            .peek(n -> System.out.print(n + " "))
            .filter(n -> n % 2 == 0)
            .limit(10)  // 쇼트서킷!
            .collect(java.util.stream.Collectors.toList());
        
        System.out.println("\n   결과: " + first10Even);
        System.out.println("   → limit(10)이 없었다면 무한 루프!");
        System.out.println("   → 쇼트서킷 덕분에 유한 시간에 완료");
    }

    /**
     * 실전 쇼트서킷 패턴
     * 
     * 패턴 1: 존재 여부 확인
     * ```java
     * // ❌ 비효율
     * boolean exists = list.stream()
     *     .filter(condition)
     *     .collect(toList())
     *     .size() > 0;
     * 
     * // ✅ 효율
     * boolean exists = list.stream()
     *     .anyMatch(condition);
     * ```
     * 
     * 패턴 2: 첫 번째 요소 찾기
     * ```java
     * // ❌ 비효율
     * Optional<T> first = list.stream()
     *     .filter(condition)
     *     .collect(toList())
     *     .stream()
     *     .findFirst();
     * 
     * // ✅ 효율
     * Optional<T> first = list.stream()
     *     .filter(condition)
     *     .findFirst();
     * ```
     * 
     * 패턴 3: 조건 검증
     * ```java
     * // ❌ 비효율
     * boolean allValid = list.stream()
     *     .filter(condition)
     *     .count() == list.size();
     * 
     * // ✅ 효율
     * boolean allValid = list.stream()
     *     .allMatch(condition);
     * ```
     * 
     * 핵심:
     * - 전체 검사가 필요 없으면 쇼트서킷 사용
     * - 성능 향상이 극적 (수백~수천 배)
     * - 무한 스트림 처리 가능
     */
}
