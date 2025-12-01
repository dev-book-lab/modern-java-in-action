package chapter05.code;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;

/**
 * 스트림의 지연 평가 (Lazy Evaluation) 데모
 * 
 * 중간 연산 vs 최종 연산:
 * - 중간 연산: filter, map 등 → 지연 실행
 * - 최종 연산: collect, count 등 → 즉시 실행
 * 
 * 학습 목표:
 * 1. 지연 평가의 개념 이해
 * 2. 중간 연산이 즉시 실행되지 않음을 확인
 * 3. 쇼트서킷의 효과 체감
 * 
 * @author raoul-gabrielurma
 * @author Modern Java In Action
 */
public class Laziness {

  public static void main(String[] args) {
    
    System.out.println("=== Lazy Evaluation Demo ===\n");
    
    /**
     * 지연 평가 (Lazy Evaluation)
     * 
     * 특징:
     * - 중간 연산은 최종 연산이 호출되기 전까지 실행되지 않음
     * - 필요한 만큼만 처리 (쇼트서킷)
     * - 성능 최적화
     * 
     * 동작 방식:
     * 1. 중간 연산 체이닝 → 연산 등록만
     * 2. 최종 연산 호출 → 실제 실행
     * 3. 파이프라인 융합 → 한 번에 처리
     */
    List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);
    
    System.out.println("입력: " + numbers);
    System.out.println("목표: 짝수의 제곱 중 처음 2개\n");
    
    List<Integer> twoEvenSquares = numbers.stream()
        .filter(n -> {
          System.out.println("filtering " + n);
          return n % 2 == 0;
        })
        .map(n -> {
          System.out.println("mapping " + n);
          return n * n;
        })
        .limit(2)
        .collect(toList());
    
    System.out.println("\n결과: " + twoEvenSquares);
    System.out.println("\n분석:");
    System.out.println("- limit(2) 덕분에 2개만 처리하고 종료");
    System.out.println("- 1: 필터 실패 → map 미실행");
    System.out.println("- 2: 필터 통과 → map 실행 → 4");
    System.out.println("- 3: 필터 실패 → map 미실행");
    System.out.println("- 4: 필터 통과 → map 실행 → 16");
    System.out.println("- 5 이후: 검사조차 안 함 (쇼트서킷!)");
  }
}
