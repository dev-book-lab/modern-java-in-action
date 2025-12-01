package chapter05.code;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;

import chapter04.code.Dish;

/**
 * 매핑 연산 종합 예제
 * 
 * 스트림의 핵심 매핑 연산:
 * - map(Function)         : 각 요소를 다른 요소로 변환 (1:1 매핑)
 * - flatMap(Function)     : 스트림의 각 요소를 스트림으로 변환 후 평면화 (1:N 매핑)
 * 
 * 학습 목표:
 * 1. map()을 이용한 데이터 변환 이해
 * 2. flatMap()의 평면화 개념 마스터
 * 3. map vs flatMap 차이점 명확히 구분
 * 4. 실전 예제: 문자열 분리, 숫자쌍 생성
 * 
 * @author Modern Java In Action
 */
public class Mapping {

  public static void main(String... args) {
    
    // ==================================================
    // 1. map으로 요리명 추출
    // ==================================================
    System.out.println("=== 1. Extracting data with map ===");
    
    /**
     * map(Function<T, R> mapper)
     * 
     * 동작 방식:
     * - 각 요소 T에 함수를 적용하여 새로운 요소 R 생성
     * - Stream<T> → Stream<R> 변환
     * - 1:1 매핑 (각 요소마다 정확히 하나의 결과)
     * 
     * 예시: Dish → String (요리명 추출)
     */
    List<String> dishNames = Dish.menu.stream()
        .map(Dish::getName)  // Dish → String
        .collect(toList());
    System.out.println("요리명: " + dishNames);
    System.out.println();

    // ==================================================
    // 2. map으로 문자열 길이 추출
    // ==================================================
    System.out.println("=== 2. Finding the length of each word ===");
    
    /**
     * map 체이닝
     * 
     * String → Integer (길이 변환)
     * 각 단어의 길이를 구함
     */
    List<String> words = Arrays.asList("Hello", "World");
    System.out.println("단어: " + words);
    
    List<Integer> wordLengths = words.stream()
        .map(String::length)  // String → Integer
        .collect(toList());
    System.out.println("길이: " + wordLengths);
    System.out.println();

    // ==================================================
    // 3. flatMap으로 고유 문자 추출
    // ==================================================
    System.out.println("=== 3. Finding unique characters with flatMap ===");
    
    /**
     * flatMap(Function<T, Stream<R>> mapper)
     * 
     * 문제: 각 단어에서 고유 문자 추출
     * 목표: ["Hello", "World"] → ["H", "e", "l", "o", "W", "r", "d"]
     * 
     * 동작 과정:
     * 1. 각 단어를 문자 배열로 split: "Hello" → ["H","e","l","l","o"]
     * 2. 배열을 스트림으로 변환: ["H","e","l","l","o"] → Stream<String>
     * 3. 모든 스트림을 하나로 평면화: Stream<String>
     * 4. 중복 제거: distinct()
     * 
     * 핵심 개념:
     * - map만 사용하면: Stream<String[]> (배열의 스트림)
     * - flatMap 사용하면: Stream<String> (문자의 스트림)
     * 
     * flatMap = map + flatten
     * - map: 각 요소를 스트림으로 변환
     * - flatten: 모든 스트림을 하나의 스트림으로 평면화
     */
    System.out.println("단어: " + words);
    System.out.println("고유 문자:");
    words.stream()
        .flatMap((String line) -> Arrays.stream(line.split("")))  // 평면화!
        .distinct()
        .forEach(System.out::println);
    
    System.out.println();

    // ==================================================
    // 4. flatMap으로 숫자쌍 생성
    // ==================================================
    System.out.println("=== 4. Generating pairs with flatMap ===");
    
    /**
     * 문제: 두 리스트의 모든 조합 생성
     * 
     * 입력:
     * - numbers1 = [1, 2, 3, 4, 5]
     * - numbers2 = [6, 7, 8]
     * 
     * 목표: 모든 숫자쌍 생성 후 합이 3으로 나누어떨어지는 쌍만 선택
     * 
     * 동작 과정:
     * 1. numbers1의 각 숫자 i에 대해
     * 2. numbers2의 모든 숫자 j와 쌍 생성 (1:N 매핑)
     * 3. flatMap으로 모든 쌍을 하나의 스트림으로 평면화
     * 4. filter로 조건 만족하는 쌍만 선택
     * 
     * flatMap이 필요한 이유:
     * - numbers1의 각 요소마다 여러 개의 쌍 생성 (1:N)
     * - map만 사용하면 Stream<Stream<int[]>>
     * - flatMap 사용하면 Stream<int[]>
     */
    List<Integer> numbers1 = Arrays.asList(1, 2, 3, 4, 5);
    List<Integer> numbers2 = Arrays.asList(6, 7, 8);
    
    System.out.println("리스트1: " + numbers1);
    System.out.println("리스트2: " + numbers2);
    System.out.println("합이 3으로 나누어떨어지는 숫자쌍:");
    
    List<int[]> pairs = numbers1.stream()
        .flatMap((Integer i) ->                        // 각 i에 대해
            numbers2.stream()                           // numbers2의 모든 j와
                .map((Integer j) -> new int[]{i, j})   // 쌍 생성
        )  // flatMap이 평면화
        .filter(pair -> (pair[0] + pair[1]) % 3 == 0)  // 조건 필터
        .collect(toList());
    
    pairs.forEach(pair -> 
        System.out.printf("(%d, %d) → 합: %d%n", pair[0], pair[1], pair[0] + pair[1]));
  }
}

/**
 * 매핑 연산 정리
 * 
 * ┌──────────┬─────────────────┬────────────────────┬──────────────┐
 * │   연산   │   변환 방식     │     반환 타입      │   사용 예    │
 * ├──────────┼─────────────────┼────────────────────┼──────────────┤
 * │ map      │ T → R (1:1)     │ Stream<R>          │ 타입 변환    │
 * │ flatMap  │ T → Stream<R>   │ Stream<R>          │ 평면화       │
 * │          │ (1:N, 평면화)   │ (평탄한 스트림)    │              │
 * └──────────┴─────────────────┴────────────────────┴──────────────┘
 * 
 * 선택 가이드:
 * 1. 단순 변환 (1:1) → map
 *    - 타입 변환: Dish → String
 *    - 값 계산: Integer → Integer
 * 
 * 2. 평면화 필요 (1:N) → flatMap
 *    - 문자열 분리: "Hello" → ['H','e','l','l','o']
 *    - 컬렉션 평탄화: List<List<T>> → List<T>
 *    - 조합 생성: [1,2] × [3,4] → [(1,3),(1,4),(2,3),(2,4)]
 * 
 * 핵심 원칙:
 * - 결과가 Stream<Stream<T>> 형태면 → flatMap 사용
 * - 중첩 구조를 1차원으로 만들 때 → flatMap 사용
 * - 일반적인 변환 → map 사용
 */
