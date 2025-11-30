package chapter04.code;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 스트림 기본 예제: Java 7 vs Java 8 비교
 * 
 * 400 칼로리 미만의 요리를 칼로리 순으로 정렬하여 이름만 추출하는 작업을
 * Java 7의 전통적인 방식과 Java 8의 스트림 방식으로 비교
 * 
 * 학습 목표:
 * - 스트림의 선언형 코드 스타일 이해
 * - 코드 간결성과 가독성 향상 체감
 * - 병렬 처리의 용이성 인지
 * 
 * @author Modern Java In Action
 */
public class StreamBasic {

    public static void main(String... args) {
        System.out.println("=== Java 7 방식 ===");
        getLowCaloricDishesNamesInJava7(Dish.menu).forEach(System.out::println);

        System.out.println("\n=== Java 8 방식 (스트림) ===");
        getLowCaloricDishesNamesInJava8(Dish.menu).forEach(System.out::println);
        
        System.out.println("\n=== Java 8 방식 (병렬 스트림) ===");
        getLowCaloricDishesNamesInJava8Parallel(Dish.menu).forEach(System.out::println);
    }

    /**
     * Java 7 방식: 명령형 프로그래밍 (How - 어떻게)
     * 
     * 문제점:
     * 1. 가비지 변수(lowCaloricDishes)가 필요함 - 중간 결과 저장용
     * 2. 코드가 장황함 - 필터링, 정렬, 변환이 각각 별도의 루프
     * 3. 가독성이 떨어짐 - 비즈니스 로직이 제어 구문에 묻힘
     * 4. 병렬화가 어려움 - synchronized 등 직접 관리 필요
     * 
     * @param dishes 메뉴 리스트
     * @return 400 칼로리 미만의 요리 이름 (칼로리 순 정렬)
     */
    public static List<String> getLowCaloricDishesNamesInJava7(List<Dish> dishes) {
        // 1단계: 400 칼로리 미만 필터링
        List<Dish> lowCaloricDishes = new ArrayList<>();  // 가비지 변수!
        for (Dish d : dishes) {
            if (d.getCalories() < 400) {  // 명시적 조건 검사
                lowCaloricDishes.add(d);
            }
        }
        
        // 2단계: 칼로리 기준 정렬
        Collections.sort(lowCaloricDishes, new Comparator<Dish>() {
            @Override
            public int compare(Dish d1, Dish d2) {
                return Integer.compare(d1.getCalories(), d2.getCalories());
            }
        });  // 익명 클래스 - 장황함
        
        // 3단계: 요리 이름만 추출
        List<String> lowCaloricDishesName = new ArrayList<>();  // 또 다른 가비지 변수!
        for (Dish d : lowCaloricDishes) {
            lowCaloricDishesName.add(d.getName());
        }
        
        return lowCaloricDishesName;
    }

    /**
     * Java 8 방식: 선언형 프로그래밍 (What - 무엇을)
     * 
     * 장점:
     * 1. 중간 변수 불필요 - 파이프라인으로 직접 연결
     * 2. 코드가 간결함 - 각 연산이 메서드 체이닝
     * 3. 가독성이 높음 - 비즈니스 로직이 명확히 드러남
     * 4. 병렬화가 쉬움 - stream()을 parallelStream()으로만 변경
     * 
     * 동작 방식:
     * - filter: 400 칼로리 미만만 통과
     * - sorted: 칼로리 오름차순 정렬
     * - map: Dish 객체에서 이름(String)만 추출
     * - collect: 결과를 List로 수집
     * 
     * @param dishes 메뉴 리스트
     * @return 400 칼로리 미만의 요리 이름 (칼로리 순 정렬)
     */
    public static List<String> getLowCaloricDishesNamesInJava8(List<Dish> dishes) {
        return dishes.stream()                          // 스트림 생성
            .filter(d -> d.getCalories() < 400)         // 중간 연산: 필터링
            .sorted(comparing(Dish::getCalories))       // 중간 연산: 정렬
            .map(Dish::getName)                         // 중간 연산: 변환
            .collect(toList());                         // 최종 연산: 수집
    }

    /**
     * Java 8 병렬 스트림: 멀티코어 활용
     * 
     * 변경 사항:
     * - stream() → parallelStream()만 변경
     * - 나머지 코드는 동일!
     * 
     * 내부 동작:
     * - 데이터를 여러 청크로 분할
     * - 각 청크를 병렬로 처리
     * - 결과를 자동으로 합침
     * - 스레드 풀 관리는 라이브러리가 담당
     * 
     * 주의사항:
     * - 소량 데이터에서는 오버헤드로 인해 오히려 느릴 수 있음
     * - 상태를 변경하는 연산은 피해야 함 (스레드 안전성)
     * 
     * @param dishes 메뉴 리스트
     * @return 400 칼로리 미만의 요리 이름 (칼로리 순 정렬)
     */
    public static List<String> getLowCaloricDishesNamesInJava8Parallel(List<Dish> dishes) {
        return dishes.parallelStream()                  // 병렬 스트림 생성!
            .filter(d -> d.getCalories() < 400)         // 병렬 필터링
            .sorted(comparing(Dish::getCalories))       // 병렬 정렬
            .map(Dish::getName)                         // 병렬 변환
            .collect(toList());                         // 결과 수집
    }
}
