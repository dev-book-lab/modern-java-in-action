package chapter02.code;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Predicate 패턴과 함수형 인터페이스 실전 활용
 * 
 * 학습 목표:
 * 1. Java 표준 Predicate 사용법
 * 2. Predicate 합성 (and, or, negate)
 * 3. Consumer와 Function 활용
 * 4. 제너릭으로 일반화
 */
public class PredicateExamples {
    
    // Apple 클래스 (재사용)
    static class Apple {
        private String color;
        private int weight;
        private boolean fresh;
        
        public Apple(int weight, String color, boolean fresh) {
            this.weight = weight;
            this.color = color;
            this.fresh = fresh;
        }
        
        public String getColor() { return color; }
        public int getWeight() { return weight; }
        public boolean isFresh() { return fresh; }
        
        @Override
        public String toString() {
            return String.format("Apple{color=%s, weight=%dg, fresh=%s}", 
                color, weight, fresh);
        }
    }
    
    // ========================================
    // 제너릭 필터 메서드
    // ========================================
    
    public static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        for (T element : list) {
            if (predicate.test(element)) {
                result.add(element);
            }
        }
        return result;
    }
    
    // ========================================
    // Consumer: 부작용 수행
    // ========================================
    
    public static <T> void forEach(List<T> list, Consumer<T> consumer) {
        for (T element : list) {
            consumer.accept(element);
        }
    }
    
    // ========================================
    // Function: 값 변환
    // ========================================
    
    public static <T, R> List<R> map(List<T> list, Function<T, R> function) {
        List<R> result = new ArrayList<>();
        for (T element : list) {
            result.add(function.apply(element));
        }
        return result;
    }
    
    // ========================================
    // 메인 데모
    // ========================================
    
    public static void main(String[] args) {
        // 테스트 데이터
        List<Apple> inventory = Arrays.asList(
            new Apple(80, "green", true),
            new Apple(155, "green", false),
            new Apple(120, "red", true),
            new Apple(200, "red", false),
            new Apple(90, "yellow", true)
        );
        
        System.out.println("=".repeat(70));
        System.out.println("Predicate 패턴 실전 활용");
        System.out.println("=".repeat(70));
        System.out.println();
        
        // ========================================
        // 1. 기본 Predicate 사용
        // ========================================
        
        System.out.println("【1】 기본 Predicate 사용");
        
        Predicate<Apple> isGreen = apple -> "green".equals(apple.getColor());
        Predicate<Apple> isHeavy = apple -> apple.getWeight() > 150;
        Predicate<Apple> isFresh = Apple::isFresh;
        
        List<Apple> greenApples = filter(inventory, isGreen);
        List<Apple> heavyApples = filter(inventory, isHeavy);
        List<Apple> freshApples = filter(inventory, isFresh);
        
        System.out.println("녹색 사과: " + greenApples);
        System.out.println("무거운 사과: " + heavyApples);
        System.out.println("신선한 사과: " + freshApples);
        System.out.println();
        
        // ========================================
        // 2. Predicate 합성 - AND
        // ========================================
        
        System.out.println("【2】 Predicate 합성 - AND (모두 만족)");
        
        Predicate<Apple> greenAndHeavy = isGreen.and(isHeavy);
        Predicate<Apple> greenAndHeavyAndFresh = isGreen.and(isHeavy).and(isFresh);
        
        List<Apple> result1 = filter(inventory, greenAndHeavy);
        List<Apple> result2 = filter(inventory, greenAndHeavyAndFresh);
        
        System.out.println("녹색 AND 무거운: " + result1);
        System.out.println("녹색 AND 무거운 AND 신선: " + result2);
        System.out.println();
        
        // ========================================
        // 3. Predicate 합성 - OR
        // ========================================
        
        System.out.println("【3】 Predicate 합성 - OR (하나라도 만족)");
        
        Predicate<Apple> greenOrHeavy = isGreen.or(isHeavy);
        List<Apple> result3 = filter(inventory, greenOrHeavy);
        
        System.out.println("녹색 OR 무거운: " + result3);
        System.out.println();
        
        // ========================================
        // 4. Predicate 합성 - NOT
        // ========================================
        
        System.out.println("【4】 Predicate 합성 - NOT (반대)");
        
        Predicate<Apple> notGreen = isGreen.negate();
        Predicate<Apple> notFresh = isFresh.negate();
        
        List<Apple> result4 = filter(inventory, notGreen);
        List<Apple> result5 = filter(inventory, notFresh);
        
        System.out.println("녹색 아닌 사과: " + result4);
        System.out.println("신선하지 않은 사과: " + result5);
        System.out.println();
        
        // ========================================
        // 5. 복합 조건 - 체이닝
        // ========================================
        
        System.out.println("【5】 복합 조건 체이닝");
        
        // (녹색 AND 무거운) OR 신선한
        Predicate<Apple> complex1 = isGreen.and(isHeavy).or(isFresh);
        
        // NOT(녹색 AND 무거운)
        Predicate<Apple> complex2 = isGreen.and(isHeavy).negate();
        
        System.out.println("(녹색 AND 무거운) OR 신선: " + filter(inventory, complex1));
        System.out.println("NOT(녹색 AND 무거운): " + filter(inventory, complex2));
        System.out.println();
        
        // ========================================
        // 6. Consumer - 부작용 수행
        // ========================================
        
        System.out.println("【6】 Consumer - 부작용 수행");
        
        System.out.print("모든 사과 출력: ");
        forEach(inventory, apple -> System.out.print(apple.getColor() + " "));
        System.out.println();
        
        System.out.println("무게 정보 출력:");
        forEach(inventory, apple -> 
            System.out.println("  - " + apple.getColor() + " 사과: " + apple.getWeight() + "g"));
        
        System.out.println();
        
        // ========================================
        // 7. Function - 값 변환
        // ========================================
        
        System.out.println("【7】 Function - 값 변환");
        
        // 무게만 추출
        List<Integer> weights = map(inventory, Apple::getWeight);
        System.out.println("무게 리스트: " + weights);
        
        // 색상만 추출
        List<String> colors = map(inventory, Apple::getColor);
        System.out.println("색상 리스트: " + colors);
        
        // 설명 문자열 생성
        List<String> descriptions = map(inventory, apple ->
            String.format("%s 사과 (%dg)", apple.getColor(), apple.getWeight()));
        System.out.println("설명 리스트:");
        descriptions.forEach(desc -> System.out.println("  - " + desc));
        
        System.out.println();
        
        // ========================================
        // 8. 제너릭 일반화 - 다른 타입에도 사용
        // ========================================
        
        System.out.println("【8】 제너릭 일반화 - 정수 필터링");
        
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        
        List<Integer> evenNumbers = filter(numbers, n -> n % 2 == 0);
        List<Integer> greaterThan5 = filter(numbers, n -> n > 5);
        
        System.out.println("짝수: " + evenNumbers);
        System.out.println("5보다 큰 수: " + greaterThan5);
        System.out.println();
        
        System.out.println("【9】 제너릭 일반화 - 문자열 필터링");
        
        List<String> strings = Arrays.asList("apple", "banana", "cat", "dog", "elephant");
        
        List<String> longStrings = filter(strings, s -> s.length() > 3);
        List<String> startsWithA = filter(strings, s -> s.startsWith("a"));
        
        System.out.println("길이 > 3: " + longStrings);
        System.out.println("'a'로 시작: " + startsWithA);
        System.out.println();
        
        // ========================================
        // 10. 실전 조합 예제
        // ========================================
        
        System.out.println("【10】 실전 조합 - 필터 → 변환 → 출력");
        
        System.out.println("무거운 사과들의 색상:");
        List<String> heavyAppleColors = map(
            filter(inventory, apple -> apple.getWeight() > 150),
            Apple::getColor
        );
        forEach(heavyAppleColors, color -> System.out.println("  - " + color));
        
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println("💡 핵심: Predicate, Consumer, Function의 조합으로 강력한 파이프라인!");
        System.out.println("=".repeat(70));
    }
}
