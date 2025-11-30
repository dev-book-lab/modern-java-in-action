package chapter04.code;

import static java.util.stream.Collectors.toList;
import static chapter04.code.Dish.menu;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 내부 반복 vs 외부 반복 비교 데모
 * 
 * 핵심 차이:
 * - 외부 반복: 개발자가 직접 제어 (How - 어떻게)
 * - 내부 반복: 라이브러리가 제어 (What - 무엇을)
 * 
 * 학습 목표:
 * - 외부 반복의 3가지 형태 이해
 * - 내부 반복의 이점 체감
 * - 병렬 처리의 용이성 비교
 * 
 * @author Modern Java In Action
 */
public class InternalVsExternalIteration {

    public static void main(String[] args) {
        demonstrateExternalIteration();
        System.out.println("\n" + "=".repeat(50) + "\n");
        demonstrateInternalIteration();
        System.out.println("\n" + "=".repeat(50) + "\n");
        comparePerformance();
    }

    /**
     * 외부 반복의 3가지 형태
     * 
     * 공통점:
     * - 개발자가 명시적으로 반복 제어
     * - "어떻게(How)" 반복할지 작성
     * - 병렬화 어려움
     */
    private static void demonstrateExternalIteration() {
        System.out.println("=== 외부 반복 (External Iteration) ===\n");
        
        // 형태 1: for 루프 (인덱스 기반)
        System.out.println("1. for 루프 (인덱스 기반):");
        System.out.println("   문제점: Off-by-one 에러 위험, 인덱스 관리 필요");
        List<String> names1 = new ArrayList<>();
        for (int i = 0; i < menu.size(); i++) {
            Dish dish = menu.get(i);
            if (dish.getCalories() < 400) {
                names1.add(dish.getName());
            }
        }
        System.out.println("   결과: " + names1);
        
        // 형태 2: for-each 루프
        System.out.println("\n2. for-each 루프:");
        System.out.println("   개선점: 인덱스 관리 불필요, 더 읽기 쉬움");
        System.out.println("   문제점: 여전히 외부 반복, 병렬화 어려움");
        List<String> names2 = new ArrayList<>();
        for (Dish dish : menu) {
            if (dish.getCalories() < 400) {
                names2.add(dish.getName());
            }
        }
        System.out.println("   결과: " + names2);
        
        // 형태 3: Iterator
        System.out.println("\n3. Iterator 사용:");
        System.out.println("   장점: 반복 중 요소 제거 가능");
        System.out.println("   단점: 가장 장황함, 에러 가능성 높음");
        List<String> names3 = new ArrayList<>();
        Iterator<Dish> iterator = menu.iterator();
        while (iterator.hasNext()) {
            Dish dish = iterator.next();
            if (dish.getCalories() < 400) {
                names3.add(dish.getName());
            }
        }
        System.out.println("   결과: " + names3);
        
        System.out.println("\n외부 반복의 공통 문제점:");
        System.out.println("- 개발자가 '어떻게' 반복할지 명시");
        System.out.println("- 반복 변수 관리 필요 (i, iterator 등)");
        System.out.println("- 병렬 처리를 위해서는 복잡한 코드 필요");
        System.out.println("- 라이브러리가 최적화할 여지 없음");
    }

    /**
     * 내부 반복의 이점
     * 
     * 특징:
     * - 라이브러리가 반복 관리
     * - "무엇을(What)" 할지만 명시
     * - 자동 최적화
     * - 병렬 처리 쉬움
     */
    private static void demonstrateInternalIteration() {
        System.out.println("=== 내부 반복 (Internal Iteration) ===\n");
        
        // 순차 스트림
        System.out.println("1. 순차 스트림:");
        List<String> names = menu.stream()
            .filter(dish -> dish.getCalories() < 400)
            .map(Dish::getName)
            .collect(toList());
        System.out.println("   결과: " + names);
        System.out.println("   특징: 선언적, 간결함, 가독성 높음");
        
        // 병렬 스트림
        System.out.println("\n2. 병렬 스트림:");
        List<String> namesParallel = menu.parallelStream()  // 이것만 변경!
            .filter(dish -> dish.getCalories() < 400)
            .map(Dish::getName)
            .collect(toList());
        System.out.println("   결과: " + namesParallel);
        System.out.println("   특징: 코드 한 줄만 변경, 자동 병렬 처리");
        
        System.out.println("\n내부 반복의 장점:");
        System.out.println("✅ 개발자는 '무엇을' 할지만 명시");
        System.out.println("✅ 반복 로직은 라이브러리가 관리");
        System.out.println("✅ 자동 최적화 (루프 퓨전, 쇼트서킷)");
        System.out.println("✅ 병렬 처리 용이 (.parallelStream())");
        System.out.println("✅ 에러 가능성 낮음");
    }

    /**
     * 성능 및 가독성 비교
     * 
     * 시나리오: 400 칼로리 미만 요리 중 채식 메뉴만 선택
     */
    private static void comparePerformance() {
        System.out.println("=== 성능 및 가독성 비교 ===\n");
        
        System.out.println("시나리오: 400 칼로리 미만 & 채식 메뉴 선택\n");
        
        // 외부 반복 방식
        System.out.println("외부 반복:");
        System.out.println("```java");
        System.out.println("List<String> names = new ArrayList<>();");
        System.out.println("for (Dish dish : menu) {");
        System.out.println("    if (dish.getCalories() < 400 && dish.isVegetarian()) {");
        System.out.println("        names.add(dish.getName());");
        System.out.println("    }");
        System.out.println("}");
        System.out.println("```");
        
        List<String> externalResult = new ArrayList<>();
        for (Dish dish : menu) {
            if (dish.getCalories() < 400 && dish.isVegetarian()) {
                externalResult.add(dish.getName());
            }
        }
        System.out.println("결과: " + externalResult);
        System.out.println("코드 줄 수: 5줄");
        System.out.println("가독성: ★★☆☆☆");
        
        // 내부 반복 방식
        System.out.println("\n내부 반복:");
        System.out.println("```java");
        System.out.println("List<String> names = menu.stream()");
        System.out.println("    .filter(d -> d.getCalories() < 400)");
        System.out.println("    .filter(Dish::isVegetarian)");
        System.out.println("    .map(Dish::getName)");
        System.out.println("    .collect(toList());");
        System.out.println("```");
        
        List<String> internalResult = menu.stream()
            .filter(dish -> dish.getCalories() < 400)
            .filter(Dish::isVegetarian)
            .map(Dish::getName)
            .collect(toList());
        System.out.println("결과: " + internalResult);
        System.out.println("코드 줄 수: 5줄");
        System.out.println("가독성: ★★★★★");
        
        System.out.println("\n비교 분석:");
        System.out.println("┌──────────────┬────────────┬────────────┐");
        System.out.println("│    항목      │  외부 반복 │  내부 반복 │");
        System.out.println("├──────────────┼────────────┼────────────┤");
        System.out.println("│ 가독성       │    낮음    │    높음    │");
        System.out.println("│ 병렬화       │    어려움  │    쉬움    │");
        System.out.println("│ 에러 위험    │    높음    │    낮음    │");
        System.out.println("│ 코드 스타일  │   명령형   │   선언형   │");
        System.out.println("│ 최적화       │    수동    │    자동    │");
        System.out.println("└──────────────┴────────────┴────────────┘");
    }

    /**
     * 병렬 처리 코드 비교
     * 
     * 외부 반복으로 병렬화하려면:
     * ```java
     * ExecutorService executor = Executors.newFixedThreadPool(4);
     * List<Future<List<String>>> futures = new ArrayList<>();
     * 
     * // 데이터 분할
     * int chunkSize = menu.size() / 4;
     * for (int i = 0; i < 4; i++) {
     *     final int start = i * chunkSize;
     *     final int end = (i == 3) ? menu.size() : (i + 1) * chunkSize;
     *     
     *     futures.add(executor.submit(() -> {
     *         List<String> result = new ArrayList<>();
     *         for (int j = start; j < end; j++) {
     *             Dish dish = menu.get(j);
     *             if (dish.getCalories() < 400) {
     *                 result.add(dish.getName());
     *             }
     *         }
     *         return result;
     *     }));
     * }
     * 
     * // 결과 합치기
     * List<String> allResults = new ArrayList<>();
     * for (Future<List<String>> future : futures) {
     *     allResults.addAll(future.get());
     * }
     * 
     * executor.shutdown();
     * ```
     * → 50줄 이상의 복잡한 코드!
     * 
     * 내부 반복으로 병렬화:
     * ```java
     * List<String> result = menu.parallelStream()
     *     .filter(d -> d.getCalories() < 400)
     *     .map(Dish::getName)
     *     .collect(toList());
     * ```
     * → 단 4줄!
     * 
     * 결론:
     * - 스트림의 내부 반복이 압도적으로 간단
     * - 스레드 관리, 동기화 걱정 불필요
     * - 라이브러리가 모든 것을 처리
     */
}
