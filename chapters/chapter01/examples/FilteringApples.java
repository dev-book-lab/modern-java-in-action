import java.util.*;
import java.util.function.Predicate;

public class FilteringApples {

    public static void main(String[] args) {
        // 사과 리스트 생성
        List<Apple> inventory = Arrays.asList(
                new Apple(80, "green"),
                new Apple(155, "green"),
                new Apple(120, "red")
        );

        System.out.println("=== 자바 8 이전 방식 ===");
        List<Apple> greenApples = filterGreenApples(inventory);
        System.out.println("녹색 사과: " + greenApples);

        List<Apple> heavyApples = filterHeavyApples(inventory);
        System.out.println("무거운 사과: " + heavyApples);

        System.out.println("\n=== 자바 8: 메서드 참조 ===");
        List<Apple> greenApples2 = filterApples(inventory, FilteringApples::isGreenApple);
        System.out.println("녹색 사과: " + greenApples2);

        List<Apple> heavyApples2 = filterApples(inventory, FilteringApples::isHeavyApple);
        System.out.println("무거운 사과: " + heavyApples2);

        System.out.println("\n=== 자바 8: 람다 표현식 ===");
        List<Apple> greenApples3 = filterApples(inventory,
                (Apple a) -> "green".equals(a.getColor()));
        System.out.println("녹색 사과: " + greenApples3);

        List<Apple> heavyApples3 = filterApples(inventory,
                (Apple a) -> a.getWeight() > 150);
        System.out.println("무거운 사과: " + heavyApples3);

        List<Apple> weirdApples = filterApples(inventory,
                (Apple a) -> a.getWeight() < 80 || "brown".equals(a.getColor()));
        System.out.println("이상한 조건의 사과: " + weirdApples);
    }

    // ===== 자바 8 이전 방식 =====
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

    // ===== 자바 8: 동작 파라미터화 =====
    public static boolean isGreenApple(Apple apple) {
        return "green".equals(apple.getColor());
    }

    public static boolean isHeavyApple(Apple apple) {
        return apple.getWeight() > 150;
    }

    public static List<Apple> filterApples(List<Apple> inventory, Predicate<Apple> p) {
        List<Apple> result = new ArrayList<>();
        for (Apple apple : inventory) {
            if (p.test(apple)) {
                result.add(apple);
            }
        }
        return result;
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
