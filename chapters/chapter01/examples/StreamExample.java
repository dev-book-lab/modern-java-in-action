import java.util.*;
import java.util.stream.Collectors;

public class StreamExample {

    public static void main(String[] args) {
        // 사과 리스트 생성
        List<Apple> inventory = Arrays.asList(
                new Apple(80, "green"),
                new Apple(155, "green"),
                new Apple(120, "red"),
                new Apple(200, "red"),
                new Apple(90, "green")
        );

        System.out.println("=== 원본 리스트 ===");
        System.out.println(inventory);

        System.out.println("\n=== 외부 반복 (for-each) ===");
        List<Apple> heavyApples1 = new ArrayList<>();
        for (Apple apple : inventory) {
            if (apple.getWeight() > 150) {
                heavyApples1.add(apple);
            }
        }
        System.out.println("무거운 사과: " + heavyApples1);

        System.out.println("\n=== 내부 반복 (Stream API) ===");
        List<Apple> heavyApples2 = inventory.stream()
                .filter(apple -> apple.getWeight() > 150)
                .collect(Collectors.toList());
        System.out.println("무거운 사과: " + heavyApples2);

        System.out.println("\n=== 스트림 체이닝 ===");
        List<Apple> result = inventory.stream()
                .filter(apple -> "green".equals(apple.getColor()))  // 녹색만
                .filter(apple -> apple.getWeight() > 80)             // 80g 초과
                .sorted(Comparator.comparing(Apple::getWeight))      // 무게순 정렬
                .collect(Collectors.toList());
        System.out.println("녹색이면서 80g 초과 (무게순): " + result);

        System.out.println("\n=== 병렬 스트림 ===");
        long count = inventory.parallelStream()
                .filter(apple -> apple.getWeight() > 100)
                .count();
        System.out.println("100g 초과 사과 개수: " + count);

        System.out.println("\n=== map: 색상만 추출 ===");
        List<String> colors = inventory.stream()
                .map(Apple::getColor)
                .distinct()  // 중복 제거
                .collect(Collectors.toList());
        System.out.println("사과 색상 목록: " + colors);

        System.out.println("\n=== forEach: 각 요소 출력 ===");
        inventory.stream()
                .filter(apple -> "red".equals(apple.getColor()))
                .forEach(apple -> System.out.println("  " + apple));

        System.out.println("\n=== 통계: 평균 무게 ===");
        double avgWeight = inventory.stream()
                .mapToInt(Apple::getWeight)
                .average()
                .orElse(0.0);
        System.out.printf("평균 무게: %.2fg%n", avgWeight);

        System.out.println("\n=== 그룹핑: 색상별 분류 ===");
        Map<String, List<Apple>> groupedByColor = inventory.stream()
                .collect(Collectors.groupingBy(Apple::getColor));
        groupedByColor.forEach((color, apples) ->
                System.out.println(color + ": " + apples));
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
