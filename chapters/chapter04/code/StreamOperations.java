package chapter04.code;

import static java.util.stream.Collectors.*;
import static chapter04.code.Dish.menu;

import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 스트림 연산 종합 예제
 * 
 * 중간 연산 (Intermediate Operations):
 * - filter, map, flatMap, distinct, sorted, limit, skip, peek
 * - 스트림을 반환 → 체이닝 가능
 * - 게으르게 실행 (Lazy)
 * 
 * 최종 연산 (Terminal Operations):
 * - forEach, collect, reduce, count, anyMatch, allMatch, noneMatch
 * - findFirst, findAny, min, max
 * - 결과를 반환 → 스트림 닫힘
 * - 즉시 실행 (Eager)
 * 
 * @author Modern Java In Action
 */
public class StreamOperations {

    public static void main(String[] args) {
        demonstrateIntermediateOperations();
        System.out.println("\n" + "=".repeat(60) + "\n");
        demonstrateTerminalOperations();
        System.out.println("\n" + "=".repeat(60) + "\n");
        demonstratePipelineExamples();
    }

    /**
     * 중간 연산 데모
     */
    private static void demonstrateIntermediateOperations() {
        System.out.println("=== 중간 연산 (Intermediate Operations) ===\n");
        
        // filter - 조건 필터링
        System.out.println("1. filter - 채식 요리만:");
        menu.stream()
            .filter(Dish::isVegetarian)
            .map(Dish::getName)
            .forEach(name -> System.out.println("   " + name));
        
        // map - 요소 변환
        System.out.println("\n2. map - 요리 이름만 추출:");
        menu.stream()
            .map(Dish::getName)
            .limit(5)
            .forEach(name -> System.out.println("   " + name));
        
        // distinct - 중복 제거
        System.out.println("\n3. distinct - 타입별 중복 제거:");
        menu.stream()
            .map(Dish::getType)
            .distinct()
            .forEach(type -> System.out.println("   " + type));
        
        // sorted - 정렬
        System.out.println("\n4. sorted - 칼로리 순 정렬:");
        menu.stream()
            .sorted((d1, d2) -> Integer.compare(d1.getCalories(), d2.getCalories()))
            .map(d -> d.getName() + " (" + d.getCalories() + " kcal)")
            .forEach(s -> System.out.println("   " + s));
        
        // limit - 개수 제한
        System.out.println("\n5. limit - 처음 3개만:");
        menu.stream()
            .limit(3)
            .map(Dish::getName)
            .forEach(name -> System.out.println("   " + name));
        
        // skip - 건너뛰기
        System.out.println("\n6. skip - 처음 3개 건너뛰고:");
        menu.stream()
            .skip(3)
            .limit(3)
            .map(Dish::getName)
            .forEach(name -> System.out.println("   " + name));
        
        // peek - 중간 확인 (디버깅)
        System.out.println("\n7. peek - 파이프라인 추적:");
        menu.stream()
            .peek(d -> System.out.println("   소스: " + d.getName()))
            .filter(d -> d.getCalories() > 300)
            .peek(d -> System.out.println("     → 필터 통과: " + d.getName()))
            .map(Dish::getName)
            .peek(name -> System.out.println("       → 이름 추출: " + name))
            .limit(3)
            .collect(toList());
    }

    /**
     * 최종 연산 데모
     */
    private static void demonstrateTerminalOperations() {
        System.out.println("=== 최종 연산 (Terminal Operations) ===\n");
        
        // forEach - 각 요소 처리
        System.out.println("1. forEach - 요소 출력:");
        menu.stream()
            .filter(d -> d.getCalories() < 400)
            .forEach(d -> System.out.println("   " + d.getName()));
        
        // collect - 결과 수집
        System.out.println("\n2. collect - 리스트로 수집:");
        List<String> names = menu.stream()
            .map(Dish::getName)
            .collect(toList());
        System.out.println("   " + names);
        
        // count - 개수 세기
        System.out.println("\n3. count - 채식 요리 개수:");
        long vegetarianCount = menu.stream()
            .filter(Dish::isVegetarian)
            .count();
        System.out.println("   " + vegetarianCount + "개");
        
        // anyMatch - 하나라도 만족?
        System.out.println("\n4. anyMatch - 채식 요리 있나요?");
        boolean hasVegetarian = menu.stream()
            .anyMatch(Dish::isVegetarian);
        System.out.println("   " + (hasVegetarian ? "있음" : "없음"));
        
        // allMatch - 모두 만족?
        System.out.println("\n5. allMatch - 모두 1000 칼로리 미만?");
        boolean allLowCalorie = menu.stream()
            .allMatch(d -> d.getCalories() < 1000);
        System.out.println("   " + (allLowCalorie ? "예" : "아니오"));
        
        // noneMatch - 하나도 만족 안함?
        System.out.println("\n6. noneMatch - 1000 칼로리 넘는 것 없나요?");
        boolean noneHighCalorie = menu.stream()
            .noneMatch(d -> d.getCalories() > 1000);
        System.out.println("   " + (noneHighCalorie ? "없음" : "있음"));
        
        // findFirst - 첫 번째 요소
        System.out.println("\n7. findFirst - 첫 번째 채식 요리:");
        Optional<Dish> firstVegetarian = menu.stream()
            .filter(Dish::isVegetarian)
            .findFirst();
        firstVegetarian.ifPresent(d -> 
            System.out.println("   " + d.getName()));
        
        // findAny - 아무 요소
        System.out.println("\n8. findAny - 아무 채식 요리:");
        Optional<Dish> anyVegetarian = menu.stream()
            .filter(Dish::isVegetarian)
            .findAny();
        anyVegetarian.ifPresent(d -> 
            System.out.println("   " + d.getName()));
        
        // min - 최소값
        System.out.println("\n9. min - 최저 칼로리 요리:");
        Optional<Dish> minCalorieDish = menu.stream()
            .min((d1, d2) -> Integer.compare(d1.getCalories(), d2.getCalories()));
        minCalorieDish.ifPresent(d -> 
            System.out.println("   " + d.getName() + " (" + d.getCalories() + " kcal)"));
        
        // max - 최대값
        System.out.println("\n10. max - 최고 칼로리 요리:");
        Optional<Dish> maxCalorieDish = menu.stream()
            .max((d1, d2) -> Integer.compare(d1.getCalories(), d2.getCalories()));
        maxCalorieDish.ifPresent(d -> 
            System.out.println("   " + d.getName() + " (" + d.getCalories() + " kcal)"));
        
        // reduce - 축약
        System.out.println("\n11. reduce - 총 칼로리:");
        int totalCalories = menu.stream()
            .map(Dish::getCalories)
            .reduce(0, Integer::sum);
        System.out.println("   " + totalCalories + " kcal");
    }

    /**
     * 파이프라인 실전 예제
     */
    private static void demonstratePipelineExamples() {
        System.out.println("=== 파이프라인 실전 예제 ===\n");
        
        // 예제 1: 고칼로리 요리 Top 3
        System.out.println("1. 고칼로리 요리 Top 3:");
        List<String> top3HighCalorie = menu.stream()
            .sorted((d1, d2) -> Integer.compare(d2.getCalories(), d1.getCalories()))
            .map(d -> d.getName() + " (" + d.getCalories() + " kcal)")
            .limit(3)
            .collect(toList());
        top3HighCalorie.forEach(s -> System.out.println("   " + s));
        
        // 예제 2: 타입별 그룹화
        System.out.println("\n2. 타입별 요리 그룹화:");
        Map<Dish.Type, List<String>> dishesByType = menu.stream()
            .collect(groupingBy(Dish::getType, 
                               mapping(Dish::getName, toList())));
        dishesByType.forEach((type, dishes) -> {
            System.out.println("   " + type + ": " + dishes);
        });
        
        // 예제 3: 통계 정보
        System.out.println("\n3. 칼로리 통계:");
        IntSummaryStatistics calorieStats = menu.stream()
            .mapToInt(Dish::getCalories)
            .summaryStatistics();
        System.out.println("   개수: " + calorieStats.getCount());
        System.out.println("   합계: " + calorieStats.getSum() + " kcal");
        System.out.println("   평균: " + String.format("%.1f", calorieStats.getAverage()) + " kcal");
        System.out.println("   최소: " + calorieStats.getMin() + " kcal");
        System.out.println("   최대: " + calorieStats.getMax() + " kcal");
        
        // 예제 4: 조건별 분할
        System.out.println("\n4. 채식/비채식 분할:");
        Map<Boolean, List<String>> partitionedMenu = menu.stream()
            .collect(partitioningBy(Dish::isVegetarian,
                                   mapping(Dish::getName, toList())));
        System.out.println("   채식: " + partitionedMenu.get(true));
        System.out.println("   비채식: " + partitionedMenu.get(false));
    }

    /**
     * 중간 연산 vs 최종 연산 비교
     * 
     * ┌──────────────┬────────────────┬────────────────┐
     * │    항목      │   중간 연산    │   최종 연산    │
     * ├──────────────┼────────────────┼────────────────┤
     * │ 반환 타입    │ Stream<T>      │ 구체적 타입    │
     * │ 체이닝       │ 가능           │ 불가능         │
     * │ 실행 시점    │ Lazy (게으름)  │ Eager (즉시)   │
     * │ 스트림 상태  │ 열림           │ 닫힘           │
     * │ 횟수         │ 여러 번 가능   │ 한 번만        │
     * └──────────────┴────────────────┴────────────────┘
     * 
     * 중간 연산 종류:
     * - filter(Predicate)      : 조건 필터링
     * - map(Function)          : 요소 변환
     * - flatMap(Function)      : 스트림 평면화
     * - distinct()             : 중복 제거
     * - sorted()               : 정렬
     * - sorted(Comparator)     : 비교자로 정렬
     * - limit(long)            : 개수 제한
     * - skip(long)             : 건너뛰기
     * - peek(Consumer)         : 엿보기 (디버깅)
     * 
     * 최종 연산 종류:
     * - forEach(Consumer)      : 각 요소 처리
     * - collect(Collector)     : 결과 수집
     * - reduce(...)            : 축약
     * - count()                : 개수
     * - anyMatch(Predicate)    : 하나라도 만족?
     * - allMatch(Predicate)    : 모두 만족?
     * - noneMatch(Predicate)   : 하나도 불만족?
     * - findFirst()            : 첫 번째 요소
     * - findAny()              : 아무 요소
     * - min(Comparator)        : 최소값
     * - max(Comparator)        : 최대값
     */
}
