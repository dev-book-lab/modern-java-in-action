package chapter04.code;

import static java.util.stream.Collectors.toList;
import static chapter04.code.Dish.menu;

import java.util.List;

/**
 * 스트림의 게으른 실행(Lazy Evaluation) 데모
 * 
 * 핵심 개념:
 * - 중간 연산은 최종 연산이 호출될 때까지 실행되지 않음
 * - 루프 퓨전: 여러 중간 연산이 하나의 패스로 합쳐짐
 * - 쇼트서킷: 필요한 만큼만 처리하고 중단
 * 
 * 학습 목표:
 * - peek()을 통한 실행 과정 추적
 * - filter와 map의 실행 순서 이해
 * - limit의 쇼트서킷 효과 확인
 * 
 * @author Modern Java In Action
 */
public class LazyEvaluationDemo {

    public static void main(String[] args) {
        demonstrateLazyEvaluation();
        System.out.println("\n" + "=".repeat(50) + "\n");
        demonstrateLoopFusion();
        System.out.println("\n" + "=".repeat(50) + "\n");
        demonstrateShortCircuit();
    }

    /**
     * 게으른 실행 데모
     * 
     * 실험:
     * 1. 중간 연산만 있는 경우 → 실행 안 됨
     * 2. 최종 연산 추가 → 이제 실행됨
     */
    private static void demonstrateLazyEvaluation() {
        System.out.println("=== 게으른 실행 데모 ===\n");
        
        System.out.println("1. 중간 연산만 있는 경우:");
        menu.stream()
            .filter(dish -> {
                System.out.println("filter 실행: " + dish.getName());
                return dish.getCalories() > 300;
            })
            .map(dish -> {
                System.out.println("map 실행: " + dish.getName());
                return dish.getName();
            });
        System.out.println("→ 아무것도 출력 안 됨! (최종 연산이 없어서)\n");
        
        System.out.println("2. 최종 연산 추가:");
        List<String> names = menu.stream()
            .filter(dish -> {
                System.out.println("filter 실행: " + dish.getName());
                return dish.getCalories() > 300;
            })
            .map(dish -> {
                System.out.println("  map 실행: " + dish.getName());
                return dish.getName();
            })
            .collect(toList());
        System.out.println("→ 결과: " + names);
    }

    /**
     * 루프 퓨전(Loop Fusion) 데모
     * 
     * 핵심:
     * - filter와 map이 각각 별도 루프가 아닌
     * - 하나의 패스로 병합되어 실행됨
     * 
     * 증명:
     * - pork: filter → map → limit
     * - beef: filter → map → limit
     * - chicken: filter → map → limit → 완료!
     * - (나머지는 처리 안 됨)
     */
    private static void demonstrateLoopFusion() {
        System.out.println("=== 루프 퓨전 데모 ===\n");
        
        List<String> highCalorieNames = menu.stream()
            .peek(dish -> System.out.println("소스: " + dish.getName()))
            .filter(dish -> {
                System.out.println("  → filter 검사: " + dish.getName() + 
                                 " (칼로리: " + dish.getCalories() + ")");
                return dish.getCalories() > 300;
            })
            .peek(dish -> System.out.println("    → filter 통과!"))
            .map(dish -> {
                String name = dish.getName();
                System.out.println("      → map 실행: " + name);
                return name;
            })
            .peek(name -> System.out.println("        → map 완료: " + name))
            .limit(3)
            .peek(name -> System.out.println("          → limit 통과: " + name))
            .collect(toList());
        
        System.out.println("\n최종 결과: " + highCalorieNames);
        System.out.println("\n관찰 포인트:");
        System.out.println("- 각 요소가 파이프라인 전체를 통과 (루프 퓨전)");
        System.out.println("- 3개 찾으면 즉시 중단 (쇼트서킷)");
    }

    /**
     * 쇼트서킷(Short-circuit) 데모
     * 
     * limit 연산:
     * - 지정한 개수만큼만 처리
     * - 나머지는 처리하지 않음
     * 
     * 성능 이점:
     * - 메뉴가 100개여도 3개만 처리
     * - 불필요한 연산 회피
     */
    private static void demonstrateShortCircuit() {
        System.out.println("=== 쇼트서킷 데모 ===\n");
        
        System.out.println("메뉴 총 개수: " + menu.size());
        System.out.println("처리 과정:\n");
        
        List<String> names = menu.stream()
            .filter(dish -> {
                System.out.println("filtering: " + dish.getName());
                return dish.getCalories() > 300;
            })
            .map(dish -> {
                System.out.println("  mapping: " + dish.getName());
                return dish.getName();
            })
            .limit(3)  // 3개만!
            .collect(toList());
        
        System.out.println("\n결과: " + names);
        System.out.println("\n분석:");
        System.out.println("- 전체 메뉴: " + menu.size() + "개");
        System.out.println("- limit(3)로 인해 고칼로리 요리 3개만 처리");
        System.out.println("- 나머지 요리는 filter 조차 실행 안 됨!");
    }

    /**
     * 실행 결과 분석
     * 
     * 출력 패턴:
     * filtering pork
     * mapping pork
     * filtering beef  
     * mapping beef
     * filtering chicken
     * mapping chicken
     * [pork, beef, chicken]
     * 
     * 이것이 의미하는 것:
     * 1. filter와 map이 한 과정으로 병합됨 (루프 퓨전)
     * 2. 각 요소가 파이프라인 전체를 순회
     * 3. limit(3) 때문에 3개 찾으면 중단 (쇼트서킷)
     * 4. french fries 이후는 처리조차 안 됨
     * 
     * 만약 루프 퓨전이 없었다면:
     * filtering pork
     * filtering beef
     * filtering chicken
     * filtering french fries
     * ... (전체 필터링)
     * mapping pork
     * mapping beef
     * mapping chicken
     * ... (전체 매핑)
     * 
     * 결론:
     * - 스트림은 훨씬 효율적!
     * - 필요한 만큼만 처리
     * - 중간 컬렉션 생성 안 함
     */
}
