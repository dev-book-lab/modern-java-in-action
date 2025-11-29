package chapter02.code;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 동작 파라미터화의 발전 과정을 단계별로 보여주는 예제
 * 
 * 학습 목표:
 * 1. 요구사항 변화에 따른 코드의 문제점 이해
 * 2. 동작 파라미터화로 해결하는 과정 체험
 * 3. 전략 패턴의 실전 적용
 */
public class FilteringApples {
    
    // 테스트용 Apple 클래스
    public static class Apple {
        private Color color;
        private int weight;
        
        public Apple(int weight, Color color) {
            this.weight = weight;
            this.color = color;
        }
        
        public Color getColor() {
            return color;
        }
        
        public int getWeight() {
            return weight;
        }
        
        @Override
        public String toString() {
            return String.format("Apple{color=%s, weight=%dg}", color, weight);
        }
    }
    
    public enum Color {
        GREEN, RED, YELLOW
    }
    
    // ========================================
    // 1단계: 녹색 사과만 필터링
    // ❌ 문제: 다른 색상이 필요하면 메서드를 계속 추가해야 함
    // ========================================
    
    public static List<Apple> filterGreenApples(List<Apple> inventory) {
        List<Apple> result = new ArrayList<>();
        for (Apple apple : inventory) {
            if (apple.getColor() == Color.GREEN) {
                result.add(apple);
            }
        }
        return result;
    }
    
    public static List<Apple> filterRedApples(List<Apple> inventory) {
        List<Apple> result = new ArrayList<>();
        for (Apple apple : inventory) {
            if (apple.getColor() == Color.RED) {
                result.add(apple);
            }
        }
        return result;
    }
    
    // ========================================
    // 2단계: 색을 파라미터화
    // ⚠️ 개선: 색상은 유연해졌지만, 무게 필터링은?
    // ========================================
    
    public static List<Apple> filterApplesByColor(List<Apple> inventory, Color color) {
        List<Apple> result = new ArrayList<>();
        for (Apple apple : inventory) {
            if (apple.getColor() == color) {
                result.add(apple);
            }
        }
        return result;
    }
    
    public static List<Apple> filterApplesByWeight(List<Apple> inventory, int weight) {
        List<Apple> result = new ArrayList<>();
        for (Apple apple : inventory) {
            if (apple.getWeight() > weight) {
                result.add(apple);
            }
        }
        return result;
    }
    
    // ========================================
    // 3단계: 모든 속성을 파라미터화
    // ❌ 최악: flag의 의미를 알 수 없고, 유연성도 부족
    // ========================================
    
    public static List<Apple> filterApples(
            List<Apple> inventory, 
            Color color, 
            int weight, 
            boolean flag) {
        
        List<Apple> result = new ArrayList<>();
        for (Apple apple : inventory) {
            if ((flag && apple.getColor() == color) 
                || (!flag && apple.getWeight() > weight)) {
                result.add(apple);
            }
        }
        return result;
    }
    
    // ========================================
    // 4단계: 동작을 추상화 - Predicate 패턴
    // ✅ 핵심 돌파구! 전략 패턴 적용
    // ========================================
    
    // Predicate 인터페이스 정의
    interface ApplePredicate {
        boolean test(Apple apple);
    }
    
    // 구체적인 전략 구현체들
    static class AppleGreenColorPredicate implements ApplePredicate {
        @Override
        public boolean test(Apple apple) {
            return apple.getColor() == Color.GREEN;
        }
    }
    
    static class AppleHeavyWeightPredicate implements ApplePredicate {
        @Override
        public boolean test(Apple apple) {
            return apple.getWeight() > 150;
        }
    }
    
    static class AppleRedAndHeavyPredicate implements ApplePredicate {
        @Override
        public boolean test(Apple apple) {
            return apple.getColor() == Color.RED 
                && apple.getWeight() > 150;
        }
    }
    
    // 동작 파라미터화된 필터 메서드
    public static List<Apple> filterApples(List<Apple> inventory, ApplePredicate predicate) {
        List<Apple> result = new ArrayList<>();
        for (Apple apple : inventory) {
            if (predicate.test(apple)) {  // 전략 패턴!
                result.add(apple);
            }
        }
        return result;
    }
    
    // ========================================
    // 5단계: 익명 클래스
    // ✅ 클래스 정의 없이 즉석에서 구현
    // ⚠️ 하지만 여전히 보일러플레이트 코드가 많음
    // ========================================
    
    public static void demonstrateAnonymousClass(List<Apple> inventory) {
        List<Apple> redApples = filterApples(inventory, new ApplePredicate() {
            @Override
            public boolean test(Apple apple) {
                return apple.getColor() == Color.RED;
            }
        });
        
        System.out.println("익명 클래스로 빨간 사과 필터링: " + redApples);
    }
    
    // ========================================
    // 6단계: 람다 표현식
    // ✅ 최고의 간결함!
    // ========================================
    
    public static void demonstrateLambda(List<Apple> inventory) {
        // 빨간 사과
        List<Apple> redApples = filterApples(inventory, 
            apple -> apple.getColor() == Color.RED);
        
        // 무거운 사과
        List<Apple> heavyApples = filterApples(inventory, 
            apple -> apple.getWeight() > 150);
        
        // 녹색이면서 무거운 사과
        List<Apple> greenAndHeavy = filterApples(inventory,
            apple -> apple.getColor() == Color.GREEN && apple.getWeight() > 150);
        
        System.out.println("람다로 빨간 사과: " + redApples);
        System.out.println("람다로 무거운 사과: " + heavyApples);
        System.out.println("람다로 녹색&무거운 사과: " + greenAndHeavy);
    }
    
    // ========================================
    // 메인 메서드 - 모든 단계 실행
    // ========================================
    
    public static void main(String[] args) {
        // 테스트 데이터
        List<Apple> inventory = Arrays.asList(
            new Apple(80, Color.GREEN),
            new Apple(155, Color.GREEN),
            new Apple(120, Color.RED),
            new Apple(200, Color.RED),
            new Apple(90, Color.YELLOW)
        );
        
        System.out.println("=".repeat(60));
        System.out.println("동작 파라미터화 발전 과정");
        System.out.println("=".repeat(60));
        System.out.println();
        
        // 1단계: 색상별 메서드
        System.out.println("【1단계】 색상별 메서드");
        System.out.println("녹색 사과: " + filterGreenApples(inventory));
        System.out.println("빨간 사과: " + filterRedApples(inventory));
        System.out.println("❌ 문제: 색상마다 메서드가 증가");
        System.out.println();
        
        // 2단계: 색을 파라미터화
        System.out.println("【2단계】 색을 파라미터화");
        System.out.println("녹색 사과: " + filterApplesByColor(inventory, Color.GREEN));
        System.out.println("빨간 사과: " + filterApplesByColor(inventory, Color.RED));
        System.out.println("무거운 사과: " + filterApplesByWeight(inventory, 150));
        System.out.println("⚠️  개선: 하지만 여전히 중복 코드 존재");
        System.out.println();
        
        // 3단계: 플래그 사용
        System.out.println("【3단계】 플래그로 모든 속성 처리");
        System.out.println("녹색 사과: " + filterApples(inventory, Color.GREEN, 0, true));
        System.out.println("무거운 사과: " + filterApples(inventory, null, 150, false));
        System.out.println("❌ 최악: flag의 의미를 알 수 없음");
        System.out.println();
        
        // 4단계: 동작 파라미터화 (전략 패턴)
        System.out.println("【4단계】 동작 파라미터화 - 전략 패턴");
        System.out.println("녹색 사과: " + filterApples(inventory, new AppleGreenColorPredicate()));
        System.out.println("무거운 사과: " + filterApples(inventory, new AppleHeavyWeightPredicate()));
        System.out.println("빨강&무거운 사과: " + filterApples(inventory, new AppleRedAndHeavyPredicate()));
        System.out.println("✅ 핵심: 동작을 객체로 전달!");
        System.out.println();
        
        // 5단계: 익명 클래스
        System.out.println("【5단계】 익명 클래스");
        demonstrateAnonymousClass(inventory);
        System.out.println("✅ 개선: 클래스 정의 불필요");
        System.out.println("⚠️  문제: 여전히 보일러플레이트 많음");
        System.out.println();
        
        // 6단계: 람다 표현식
        System.out.println("【6단계】 람다 표현식");
        demonstrateLambda(inventory);
        System.out.println("✅ 최고: 간결하고 명확!");
        System.out.println();
        
        System.out.println("=".repeat(60));
        System.out.println("💡 결론: 동작 파라미터화로 유연한 코드 작성!");
        System.out.println("=".repeat(60));
    }
}
