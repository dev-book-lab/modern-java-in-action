import java.util.*;
import java.util.stream.Collectors;

/**
 * Chapter 01: 스트림 API 예제
 * - 외부 반복 vs 내부 반복
 * - 순차 스트림 vs 병렬 스트림
 * - 스트림 체이닝
 * - 다양한 스트림 연산
 */
public class StreamExample {

    public static void main(String[] args) {
        // 사과 리스트 생성
        List<Apple> inventory = Arrays.asList(
                new Apple(80, "green"),
                new Apple(155, "green"),
                new Apple(120, "red"),
                new Apple(200, "red"),
                new Apple(90, "green"),
                new Apple(170, "green"),
                new Apple(140, "red")
        );

        System.out.println("=== 원본 리스트 ===");
        System.out.println(inventory);
        System.out.println();

        // ========== 외부 반복 vs 내부 반복 ==========
        System.out.println("=== 1. 외부 반복 (for-each) ===");
        List<Apple> heavyApples1 = new ArrayList<>();
        for (Apple apple : inventory) {
            if (apple.getWeight() > 150) {
                heavyApples1.add(apple);
            }
        }
        System.out.println("무거운 사과 (외부 반복): " + heavyApples1);

        System.out.println("\n=== 2. 내부 반복 (Stream API) ===");
        List<Apple> heavyApples2 = inventory.stream()
                .filter(apple -> apple.getWeight() > 150)
                .collect(Collectors.toList());
        System.out.println("무거운 사과 (내부 반복): " + heavyApples2);
        System.out.println();

        // ========== 스트림 체이닝 ==========
        System.out.println("=== 3. 스트림 체이닝 ===");
        List<Apple> result = inventory.stream()
                .filter(apple -> "green".equals(apple.getColor()))  // 녹색만
                .filter(apple -> apple.getWeight() > 80)             // 80g 초과
                .sorted(Comparator.comparing(Apple::getWeight))      // 무게순 정렬
                .collect(Collectors.toList());
        System.out.println("녹색이면서 80g 초과 (무게순): " + result);
        System.out.println();

        // ========== 순차 vs 병렬 스트림 ==========
        System.out.println("=== 4. 순차 스트림 vs 병렬 스트림 ===");

        // 순차 처리
        long startSeq = System.nanoTime();
        long countSeq = inventory.stream()
                .filter(apple -> apple.getWeight() > 100)
                .count();
        long endSeq = System.nanoTime();
        System.out.println("순차 스트림: " + countSeq + "개 (" + (endSeq - startSeq) / 1000 + "μs)");

        // 병렬 처리
        long startPar = System.nanoTime();
        long countPar = inventory.parallelStream()
                .filter(apple -> apple.getWeight() > 100)
                .count();
        long endPar = System.nanoTime();
        System.out.println("병렬 스트림: " + countPar + "개 (" + (endPar - startPar) / 1000 + "μs)");
        System.out.println();

        // ========== map: 변환 ==========
        System.out.println("=== 5. map: 색상만 추출 ===");
        List<String> colors = inventory.stream()
                .map(Apple::getColor)
                .distinct()  // 중복 제거
                .collect(Collectors.toList());
        System.out.println("사과 색상 목록: " + colors);
        System.out.println();

        // ========== forEach: 각 요소 출력 ==========
        System.out.println("=== 6. forEach: 빨간 사과만 출력 ===");
        inventory.stream()
                .filter(apple -> "red".equals(apple.getColor()))
                .forEach(apple -> System.out.println("  " + apple));
        System.out.println();

        // ========== 통계 연산 ==========
        System.out.println("=== 7. 통계: 평균, 최대, 최소 ===");
        double avgWeight = inventory.stream()
                .mapToInt(Apple::getWeight)
                .average()
                .orElse(0.0);
        System.out.printf("평균 무게: %.2fg%n", avgWeight);

        int maxWeight = inventory.stream()
                .mapToInt(Apple::getWeight)
                .max()
                .orElse(0);
        System.out.println("최대 무게: " + maxWeight + "g");

        int minWeight = inventory.stream()
                .mapToInt(Apple::getWeight)
                .min()
                .orElse(0);
        System.out.println("최소 무게: " + minWeight + "g");
        System.out.println();

        // ========== 그룹핑 ==========
        System.out.println("=== 8. 그룹핑: 색상별 분류 ===");
        Map<String, List<Apple>> groupedByColor = inventory.stream()
                .collect(Collectors.groupingBy(Apple::getColor));
        groupedByColor.forEach((color, apples) ->
                System.out.println(color + " (" + apples.size() + "개): " + apples));
        System.out.println();

        // ========== limit & skip ==========
        System.out.println("=== 9. limit & skip ===");
        List<Apple> first3 = inventory.stream()
                .limit(3)
                .collect(Collectors.toList());
        System.out.println("처음 3개: " + first3);

        List<Apple> skip2 = inventory.stream()
                .skip(2)
                .limit(3)
                .collect(Collectors.toList());
        System.out.println("2개 건너뛰고 3개: " + skip2);
        System.out.println();

        // ========== anyMatch, allMatch, noneMatch ==========
        System.out.println("=== 10. anyMatch, allMatch, noneMatch ===");
        boolean hasHeavy = inventory.stream()
                .anyMatch(apple -> apple.getWeight() > 150);
        System.out.println("150g 초과 사과가 있는가? " + hasHeavy);

        boolean allGreen = inventory.stream()
                .allMatch(apple -> "green".equals(apple.getColor()));
        System.out.println("모두 녹색인가? " + allGreen);

        boolean noBrown = inventory.stream()
                .noneMatch(apple -> "brown".equals(apple.getColor()));
        System.out.println("갈색 사과가 없는가? " + noBrown);
    }

    // ===== Apple 클래스 =====
    public static class Apple {
        private int weight;
        private String color;

        public Apple(int weight, String color) {
            this.weight = weight;
            this.color = color;
        }

        public int getWeight() {
            return weight;
        }

        public String getColor() {
            return color;
        }

        @Override
        public String toString() {
            return String.format("Apple{color='%s', weight=%d}", color, weight);
        }
    }
}
