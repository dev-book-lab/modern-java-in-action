package chapter04.code;

import java.util.Arrays;
import java.util.List;

/**
 * 요리를 나타내는 도메인 클래스
 * 
 * 스트림 학습을 위한 예제 데이터로 사용됨
 * 
 * @author Modern Java In Action
 */
public class Dish {

    // ========== 필드 ==========
    
    /**
     * 요리 이름
     */
    private final String name;
    
    /**
     * 채식 여부
     */
    private final boolean vegetarian;
    
    /**
     * 칼로리
     */
    private final int calories;
    
    /**
     * 요리 타입 (고기, 생선, 기타)
     */
    private final Type type;

    // ========== 생성자 ==========
    
    /**
     * Dish 객체 생성
     * 
     * @param name 요리 이름
     * @param vegetarian 채식 여부
     * @param calories 칼로리
     * @param type 요리 타입
     */
    public Dish(String name, boolean vegetarian, int calories, Type type) {
        this.name = name;
        this.vegetarian = vegetarian;
        this.calories = calories;
        this.type = type;
    }

    // ========== Getter 메서드 ==========
    
    public String getName() {
        return name;
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    public int getCalories() {
        return calories;
    }

    public Type getType() {
        return type;
    }

    // ========== 열거형 ==========
    
    /**
     * 요리 타입
     */
    public enum Type {
        /** 고기 */
        MEAT,
        /** 생선 */
        FISH,
        /** 기타 */
        OTHER
    }

    // ========== Object 메서드 오버라이드 ==========
    
    /**
     * 요리 이름을 문자열로 반환
     */
    @Override
    public String toString() {
        return name;
    }

    // ========== 테스트용 메뉴 데이터 ==========
    
    /**
     * 스트림 예제를 위한 샘플 메뉴
     * 
     * 다양한 칼로리와 타입의 요리를 포함하여
     * filter, map, sorted 등의 스트림 연산 학습에 활용
     */
    public static final List<Dish> menu = Arrays.asList(
        new Dish("pork", false, 800, Dish.Type.MEAT),          // 고칼로리 고기
        new Dish("beef", false, 700, Dish.Type.MEAT),          // 고칼로리 고기
        new Dish("chicken", false, 400, Dish.Type.MEAT),       // 중간 칼로리 고기
        new Dish("french fries", true, 530, Dish.Type.OTHER),  // 고칼로리 채식
        new Dish("rice", true, 350, Dish.Type.OTHER),          // 중간 칼로리 채식
        new Dish("season fruit", true, 120, Dish.Type.OTHER),  // 저칼로리 채식
        new Dish("pizza", true, 550, Dish.Type.OTHER),         // 고칼로리 채식
        new Dish("prawns", false, 400, Dish.Type.FISH),        // 중간 칼로리 생선
        new Dish("salmon", false, 450, Dish.Type.FISH)         // 중간 칼로리 생선
    );
}
