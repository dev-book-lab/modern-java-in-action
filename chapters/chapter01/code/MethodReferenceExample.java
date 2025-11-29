package chapter01.code;

import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Chapter 01: 메서드 참조 예제
 * - 익명 클래스 vs 메서드 참조
 * - 다양한 메서드 참조 패턴
 */
public class MethodReferenceExample {

    public static void main(String[] args) {
        System.out.println("=== 1. 파일 필터링: 익명 클래스 vs 메서드 참조 ===");

        // 익명 클래스 (자바 8 이전)
        File[] hiddenFiles1 = new File(".").listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isHidden();
            }
        });
        System.out.println("익명 클래스로 숨김 파일 찾기: " + Arrays.toString(hiddenFiles1));

        // 메서드 참조 (자바 8)
        File[] hiddenFiles2 = new File(".").listFiles(File::isHidden);
        System.out.println("메서드 참조로 숨김 파일 찾기: " + Arrays.toString(hiddenFiles2));
        System.out.println();

        // ========== 정렬: Comparator ==========
        System.out.println("=== 2. 정렬: 익명 클래스 vs 람다 vs 메서드 참조 ===");
        List<String> names = Arrays.asList("Charlie", "Alice", "Bob");

        // 익명 클래스
        List<String> sorted1 = new ArrayList<>(names);
        Collections.sort(sorted1, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareTo(s2);
            }
        });
        System.out.println("익명 클래스: " + sorted1);

        // 람다
        List<String> sorted2 = new ArrayList<>(names);
        Collections.sort(sorted2, (s1, s2) -> s1.compareTo(s2));
        System.out.println("람다: " + sorted2);

        // 메서드 참조
        List<String> sorted3 = new ArrayList<>(names);
        Collections.sort(sorted3, String::compareTo);
        System.out.println("메서드 참조: " + sorted3);
        System.out.println();

        // ========== 메서드 참조 3가지 유형 ==========
        System.out.println("=== 3. 메서드 참조의 3가지 유형 ===");

        List<String> words = Arrays.asList("apple", "banana", "cherry");

        // 1. 정적 메서드 참조: ClassName::staticMethod
        List<Integer> lengths1 = words.stream()
                .map(MethodReferenceExample::getLength)  // 정적 메서드
                .toList();
        System.out.println("정적 메서드 참조: " + lengths1);

        // 2. 인스턴스 메서드 참조: instance::instanceMethod
        MethodReferenceExample example = new MethodReferenceExample();
        List<Integer> lengths2 = words.stream()
                .map(example::getLengthInstance)  // 인스턴스 메서드
                .toList();
        System.out.println("인스턴스 메서드 참조: " + lengths2);

        // 3. 특정 타입의 임의 객체의 인스턴스 메서드: ClassName::instanceMethod
        List<String> upperWords = words.stream()
                .map(String::toUpperCase)  // String의 toUpperCase 메서드
                .toList();
        System.out.println("타입의 메서드 참조: " + upperWords);
        System.out.println();

        // ========== 생성자 참조 ==========
        System.out.println("=== 4. 생성자 참조 ===");

        // 람다
        Function<Integer, Apple> appleFactory1 = (weight) -> new Apple(weight, "red");
        Apple apple1 = appleFactory1.apply(100);
        System.out.println("람다로 생성: " + apple1);

        // 생성자 참조
        Function<Integer, Apple> appleFactory2 = Apple::new;
        Apple apple2 = appleFactory2.apply(150);
        System.out.println("생성자 참조로 생성: " + apple2);
        System.out.println();

        // ========== 일급 시민으로서의 메서드 ==========
        System.out.println("=== 5. 메서드를 변수에 할당 ===");

        // Comparator를 변수에 할당
        Comparator<Apple> byWeight = Comparator.comparing(Apple::getWeight);

        List<Apple> apples = Arrays.asList(
                new Apple(155, "green"),
                new Apple(80, "red"),
                new Apple(120, "green")
        );

        apples.sort(byWeight);
        System.out.println("무게순 정렬: " + apples);

        // reversed
        apples.sort(byWeight.reversed());
        System.out.println("무게 역순: " + apples);

        // thenComparing
        apples.sort(Comparator.comparing(Apple::getWeight)
                .thenComparing(Apple::getColor));
        System.out.println("무게 후 색상순: " + apples);
    }

    // 정적 메서드
    public static int getLength(String s) {
        return s.length();
    }

    // 인스턴스 메서드
    public int getLengthInstance(String s) {
        return s.length();
    }

    // ===== Apple 클래스 =====
    public static class Apple {
        private int weight;
        private String color;

        public Apple(int weight, String color) {
            this.weight = weight;
            this.color = color;
        }

        // 생성자 참조용: weight만 받는 생성자
        public Apple(int weight) {
            this(weight, "unknown");
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
