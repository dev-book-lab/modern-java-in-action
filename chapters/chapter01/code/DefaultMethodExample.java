import java.util.*;

/**
 * Chapter 01: 디폴트 메서드 예제
 * - 자바 7 vs 자바 8 정렬 방식
 * - List.sort() 동작 원리
 * - 와일드카드 이해하기
 */
public class DefaultMethodExample {

    public static void main(String[] args) {
        List<Apple> inventory = new ArrayList<>(Arrays.asList(
                new Apple(155, "green"),
                new Apple(80, "red"),
                new Apple(120, "green"),
                new Apple(200, "red")
        ));

        System.out.println("=== 원본 리스트 ===");
        System.out.println(inventory);
        System.out.println();

        // ========== 자바 7: Collections.sort() ==========
        System.out.println("=== 1. 자바 7 방식: Collections.sort() ===");
        List<Apple> list1 = new ArrayList<>(inventory);
        Collections.sort(list1, new Comparator<Apple>() {
            @Override
            public int compare(Apple a1, Apple a2) {
                return Integer.compare(a1.getWeight(), a2.getWeight());
            }
        });
        System.out.println("Collections.sort() 결과: " + list1);
        System.out.println();

        // ========== 자바 8: List.sort() (디폴트 메서드) ==========
        System.out.println("=== 2. 자바 8 방식: List.sort() ===");
        List<Apple> list2 = new ArrayList<>(inventory);
        list2.sort((a1, a2) -> Integer.compare(a1.getWeight(), a2.getWeight()));
        System.out.println("List.sort() 결과: " + list2);
        System.out.println();

        // ========== Comparator.comparing() ==========
        System.out.println("=== 3. Comparator.comparing() ===");
        List<Apple> list3 = new ArrayList<>(inventory);
        list3.sort(Comparator.comparing(Apple::getWeight));
        System.out.println("무게순: " + list3);

        // 역순
        List<Apple> list4 = new ArrayList<>(inventory);
        list4.sort(Comparator.comparing(Apple::getWeight).reversed());
        System.out.println("무게 역순: " + list4);

        // 다중 조건
        List<Apple> list5 = new ArrayList<>(inventory);
        list5.sort(Comparator.comparing(Apple::getColor)
                .thenComparing(Apple::getWeight));
        System.out.println("색상 후 무게순: " + list5);
        System.out.println();

        // ========== 와일드카드: ? super T ==========
        System.out.println("=== 4. 와일드카드: ? super T ===");

        // Fruit Comparator (상위 타입)
        Comparator<Fruit> fruitComparator = Comparator.comparing(Fruit::getName);

        // Apple 리스트를 Fruit Comparator로 정렬 가능!
        List<Apple> apples = new ArrayList<>(Arrays.asList(
                new Apple(100, "green"),
                new Apple(150, "red")
        ));

        apples.sort(fruitComparator);  // ✅ Apple은 Fruit이므로 안전!
        System.out.println("Fruit Comparator로 Apple 정렬: " + apples);
        System.out.println();

        // ========== removeIf (디폴트 메서드) ==========
        System.out.println("=== 5. removeIf() 디폴트 메서드 ===");
        List<Apple> list6 = new ArrayList<>(inventory);
        System.out.println("제거 전: " + list6);

        list6.removeIf(apple -> apple.getWeight() < 100);
        System.out.println("100g 미만 제거 후: " + list6);
        System.out.println();

        // ========== replaceAll (디폴트 메서드) ==========
        System.out.println("=== 6. replaceAll() 디폴트 메서드 ===");
        List<Apple> list7 = new ArrayList<>(inventory);
        System.out.println("변경 전: " + list7);

        list7.replaceAll(apple -> new Apple(apple.getWeight() + 10, apple.getColor()));
        System.out.println("모든 사과 무게 +10g: " + list7);
    }

    // ===== 클래스들 =====
    public static class Fruit {
        private String name;

        public Fruit(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Fruit{name='" + name + "'}";
        }
    }

    public static class Apple extends Fruit {
        private int weight;
        private String color;

        public Apple(int weight, String color) {
            super("Apple");
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
