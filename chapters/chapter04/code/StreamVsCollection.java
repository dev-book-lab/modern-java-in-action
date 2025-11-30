package chapter04.code;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * 스트림 vs 컬렉션 비교 데모
 * 
 * 핵심 차이점:
 * 1. 탐색 횟수: 컬렉션은 여러 번, 스트림은 단 한 번
 * 2. 데이터 계산 시점: 컬렉션은 즉시(Eager), 스트림은 필요 시(Lazy)
 * 3. 반복 방식: 컬렉션은 외부 반복, 스트림은 내부 반복
 * 
 * 학습 목표:
 * - 스트림의 일회성 특성 이해
 * - IllegalStateException 발생 원인 파악
 * - 스트림 재생성 방법 학습
 * 
 * @author Modern Java In Action
 */
public class StreamVsCollection {

    public static void main(String... args) {
        demonstrateOneTimeConsumption();
        System.out.println("\n" + "=".repeat(50) + "\n");
        demonstrateCollectionMultipleTraversal();
        System.out.println("\n" + "=".repeat(50) + "\n");
        demonstrateStreamRecreation();
    }

    /**
     * 스트림의 일회성 특성 데모
     * 
     * 핵심:
     * - 스트림은 단 한 번만 소비 가능
     * - 두 번째 연산 시도 → IllegalStateException 발생
     */
    private static void demonstrateOneTimeConsumption() {
        System.out.println("=== 스트림의 일회성 특성 ===\n");
        
        List<String> names = Arrays.asList("Java8", "Lambdas", "In", "Action");
        Stream<String> stream = names.stream();
        
        System.out.println("첫 번째 소비:");
        stream.forEach(System.out::println);
        
        System.out.println("\n두 번째 소비 시도:");
        try {
            stream.forEach(System.out::println);  // IllegalStateException!
        } catch (IllegalStateException e) {
            System.err.println("❌ 에러 발생: " + e.getMessage());
            System.err.println("→ 스트림은 이미 소비되었거나 닫혔습니다!");
        }
        
        System.out.println("\n설명:");
        System.out.println("- 스트림은 데이터를 '소비'하는 개념");
        System.out.println("- 한 번 소비하면 다시 사용 불가");
        System.out.println("- DVD가 아닌 스트리밍 서비스와 유사");
    }

    /**
     * 컬렉션의 다중 탐색 데모
     * 
     * 컬렉션 특징:
     * - 데이터를 메모리에 모두 저장
     * - 여러 번 탐색 가능
     * - DVD와 유사 (언제든 다시 재생)
     */
    private static void demonstrateCollectionMultipleTraversal() {
        System.out.println("=== 컬렉션의 다중 탐색 ===\n");
        
        List<String> names = Arrays.asList("Java8", "Lambdas", "In", "Action");
        
        System.out.println("첫 번째 탐색:");
        for (String name : names) {
            System.out.println(name);
        }
        
        System.out.println("\n두 번째 탐색:");
        for (String name : names) {
            System.out.println(name);
        }
        
        System.out.println("\n세 번째 탐색 (forEach):");
        names.forEach(System.out::println);
        
        System.out.println("\n✅ 모두 성공!");
        System.out.println("→ 컬렉션은 여러 번 탐색 가능");
    }

    /**
     * 스트림 재생성 방법 데모
     * 
     * 해결책:
     * 1. 매번 새로운 스트림 생성
     * 2. Supplier 패턴 사용
     */
    private static void demonstrateStreamRecreation() {
        System.out.println("=== 스트림 재생성 방법 ===\n");
        
        List<String> names = Arrays.asList("Java8", "Lambdas", "In", "Action");
        
        // 방법 1: 매번 새로운 스트림 생성
        System.out.println("방법 1: 매번 stream() 호출");
        System.out.println("첫 번째:");
        names.stream().forEach(System.out::println);
        
        System.out.println("\n두 번째:");
        names.stream().forEach(System.out::println);  // 새로운 스트림!
        
        System.out.println("\n✅ 성공! (매번 새로운 스트림 생성)\n");
        
        // 방법 2: Supplier 사용
        System.out.println("방법 2: Supplier 패턴");
        java.util.function.Supplier<Stream<String>> streamSupplier = 
            () -> Stream.of("Java8", "Lambdas", "In", "Action");
        
        System.out.println("첫 번째:");
        streamSupplier.get().forEach(System.out::println);
        
        System.out.println("\n두 번째:");
        streamSupplier.get().forEach(System.out::println);  // 새로운 스트림!
        
        System.out.println("\n✅ 성공! (Supplier가 매번 새 스트림 생성)");
    }

    /**
     * 스트림 vs 컬렉션 비교표
     * 
     * ┌─────────────┬──────────────┬────────────────┐
     * │   특성      │   컬렉션     │    스트림      │
     * ├─────────────┼──────────────┼────────────────┤
     * │ 데이터 저장 │ 메모리에 모두│ 필요시 계산   │
     * │ 탐색 횟수   │ 여러 번 가능 │ 단 한 번만    │
     * │ 계산 시점   │ Eager (즉시) │ Lazy (게으름) │
     * │ 반복 방식   │ 외부 반복    │ 내부 반복     │
     * │ 비유       │ DVD          │ Netflix       │
     * └─────────────┴──────────────┴────────────────┘
     * 
     * 선택 기준:
     * - 여러 번 탐색 필요? → 컬렉션
     * - 데이터 처리 파이프라인? → 스트림
     * - 병렬 처리? → 스트림
     * - 크기가 작고 단순? → 컬렉션
     */
}
