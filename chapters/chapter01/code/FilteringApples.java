import java.util.*;
import java.util.function.Predicate;

/**
 * Chapter 01: 동작 파라미터화 예제
 * - 자바 8 이전 vs 이후 비교
 * - 메서드 참조와 람다 표현식
 * - Predicate 활용
 */
public class FilteringApples {

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
        System.out.println();

        // ========== 자바 8 이전 방식 ==========
        System.out.println("=== 1. 자바 8 이전 방식 (코드 중복) ===");
        List<Apple> greenApples = filterGreenApples(inventory);
        System.out.println("녹색 사과: " + greenApples);

        List<Apple> heavyApples = filterHeavyApples(inventory);
        System.out.println("무거운 사과 (150g 초과): " + heavyApples);
        System.out.println();

        // ========== 자바 8: 메서드 참조 ==========
        System.out.println("=== 2. 자바 8: 메서드 참조 ===");
        List<Apple> greenApples2 = filterApples(inventory, FilteringApples::isGreenApple);
        System.out.println("녹색 사과: " + greenApples2);

        List<Apple> heavyApples2 = filterApples(inventory, FilteringApples::isHeavyApple);
        System.out.println("무거운 사과: " + heavyApples2);
        System.out.println();

        // ========== 자바 8: 람다 표현식 ==========
        System.out.println("=== 3. 자바 8: 람다 표현식 ===");
        List<Apple> greenApples3 = filterApples(inventory,
                (Apple a) -> "green".equals(a.getColor()));
        System.out.println("녹색 사과: " + greenApples3);

        List<Apple> heavyApples3 = filterApples(inventory,
                (Apple a) -> a.getWeight() > 150);
        System.out.println("무거운 사과: " + heavyApples3);

        // 복잡한 조건
        List<Apple> weirdApples = filterApples(inventory,
                (Apple a) -> a.getWeight() < 80 || "brown".equals(a.getColor()));
        System.out.println("80g 미만 또는 갈색 사과: " + weirdApples);
        System.out.println();

        // ========== Predicate 조합 ==========
        System.out.println("=== 4. Predicate 조합 (and, or, negate) ===");
        Predicate<Apple> greenApplePredicate = (Apple a) -> "green".equals(a.getColor());
        Predicate<Apple> heavyApplePredicate = (Apple a) -> a.getWeight() > 150;

        // and: 녹색이면서 무거운 사과
        List<Apple> greenAndHeavy = filterApples(inventory,
                greenApplePredicate.and(heavyApplePredicate));
        System.out.println("녹색이면서 무거운 사과: " + greenAndHeavy);

        // or: 녹색이거나 무거운 사과
        List<Apple> greenOrHeavy = filterApples(inventory,
                greenApplePredicate.or(heavyApplePredicate));
        System.out.println("녹색이거나 무거운 사과: " + greenOrHeavy);

        // negate: 녹색이 아닌 사과
        List<Apple> notGreen = filterApples(inventory,
                greenApplePredicate.negate());
        System.out.println("녹색이 아닌 사과: " + notGreen);
        System.out.println();

        // ========== 복잡한 람다 → 메서드 참조 개선 ==========
        System.out.println("=== 5. 복잡한 조건: 람다 vs 메서드 참조 ===");
        // 람다 (가독성 낮음)
        List<Apple> result1 = filterApples(inventory, (Apple a) -> {
            return a.getWeight() > 150 && "green".equals(a.getColor());
        });
        System.out.println("람다 방식: " + result1);

        // 메서드 참조 (가독성 높음)
        List<Apple> result2 = filterApples(inventory, FilteringApples::isHeavyGreenApple);
        System.out.println("메서드 참조: " + result2);
    }

    // ===== 자바 8 이전: 각 조건마다 메서드 작성 (코드 중복) =====
    public static List<Apple> filterGreenApples(List<Apple> inventory) {
        List<Apple> result = new ArrayList<>();
        for (Apple apple : inventory) {
            if ("green".equals(apple.getColor())) {
                result.add(apple);
            }
        }
        return result;
    }

    public static List<Apple> filterHeavyApples(List<Apple> inventory) {
        List<Apple> result = new ArrayList<>();
        for (Apple apple : inventory) {
            if (apple.getWeight() > 150) {
                result.add(apple);
            }
        }
        return result;
    }

    // ===== 자바 8: 동작 파라미터화 (하나의 메서드로 모든 조건 처리) =====
    public static List<Apple> filterApples(List<Apple> inventory, Predicate<Apple> p) {
        List<Apple> result = new ArrayList<>();
        for (Apple apple : inventory) {
            if (p.test(apple)) {
                result.add(apple);
            }
        }
        return result;
    }

    // ===== 메서드 참조용 메서드들 =====
    public static boolean isGreenApple(Apple apple) {
        return "green".equals(apple.getColor());
    }

    public static boolean isHeavyApple(Apple apple) {
        return apple.getWeight() > 150;
    }

    public static boolean isHeavyGreenApple(Apple apple) {
        return apple.getWeight() > 150 && "green".equals(apple.getColor());
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
