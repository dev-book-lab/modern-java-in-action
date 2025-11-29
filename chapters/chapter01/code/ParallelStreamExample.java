package chapter01.code;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Chapter 01: 병렬 스트림 예제
 * - 순차 vs 병렬 성능 비교
 * - 공유 가변 상태의 위험성
 * - 안전한 병렬 처리
 */
public class ParallelStreamExample {

    public static void main(String[] args) {
        // 큰 데이터셋 생성
        List<Integer> numbers = IntStream.rangeClosed(1, 1_000_000)
                .boxed()
                .collect(Collectors.toList());

        System.out.println("=== 1. 순차 스트림 vs 병렬 스트림 (성능 비교) ===");

        // 순차 스트림
        long startSeq = System.currentTimeMillis();
        long sumSeq = numbers.stream()
                .filter(n -> n % 2 == 0)
                .mapToLong(n -> n * n)
                .sum();
        long endSeq = System.currentTimeMillis();
        System.out.println("순차 스트림 결과: " + sumSeq);
        System.out.println("순차 스트림 시간: " + (endSeq - startSeq) + "ms");

        // 병렬 스트림
        long startPar = System.currentTimeMillis();
        long sumPar = numbers.parallelStream()
                .filter(n -> n % 2 == 0)
                .mapToLong(n -> n * n)
                .sum();
        long endPar = System.currentTimeMillis();
        System.out.println("병렬 스트림 결과: " + sumPar);
        System.out.println("병렬 스트림 시간: " + (endPar - startPar) + "ms");
        System.out.println();

        // ========== 공유 가변 상태의 위험 ==========
        System.out.println("=== 2. 공유 가변 상태의 위험성 ===");

        List<Integer> smallNumbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // ❌ 위험: 공유 가변 상태
        List<Integer> dangerousResult = new ArrayList<>();
        try {
            smallNumbers.parallelStream()
                    .filter(n -> n % 2 == 0)
                    .forEach(dangerousResult::add);  // ❌ 여러 스레드가 동시 접근!
            System.out.println("위험한 방식 결과: " + dangerousResult);
            System.out.println("⚠️  결과가 매번 다를 수 있음! (스레드 안전하지 않음)");
        } catch (Exception e) {
            System.out.println("❌ 오류 발생: " + e.getMessage());
        }

        // ✅ 안전: 불변 방식
        List<Integer> safeResult = smallNumbers.parallelStream()
                .filter(n -> n % 2 == 0)
                .collect(Collectors.toList());  // ✅ 스레드 안전
        System.out.println("안전한 방식 결과: " + safeResult);
        System.out.println("✅ 항상 일관된 결과");
        System.out.println();

        // ========== 함수형 프로그래밍: 부작용 없는 함수 ==========
        System.out.println("=== 3. 함수형 프로그래밍: 부작용 없는 함수 ===");

        List<Apple> apples = Arrays.asList(
                new Apple(80, "green"),
                new Apple(155, "green"),
                new Apple(120, "red")
        );

        // ✅ 순수 함수: 입력만 보고 판단
        long count = apples.parallelStream()
                .filter(ParallelStreamExample::isHeavyApple)  // 순수 함수
                .count();
        System.out.println("순수 함수 사용: " + count + "개");

        System.out.println();

        // ========== 병렬 스트림 활용 예제 ==========
        System.out.println("=== 4. 병렬 스트림 실전 활용 ===");

        // 소수 찾기 (계산 집약적)
        long startPrime = System.currentTimeMillis();
        long primeCount = IntStream.rangeClosed(1, 100_000)
                .parallel()
                .filter(ParallelStreamExample::isPrime)
                .count();
        long endPrime = System.currentTimeMillis();
        System.out.println("1~100,000 사이 소수 개수: " + primeCount);
        System.out.println("계산 시간: " + (endPrime - startPrime) + "ms");
    }

    // ===== 순수 함수 예제 =====
    public static boolean isHeavyApple(Apple apple) {
        return apple.getWeight() > 150;  // 입력만 보고 판단, 외부 상태 의존 없음
    }

    // 소수 판별 (계산 집약적 작업)
    public static boolean isPrime(int number) {
        if (number < 2) return false;
        for (int i = 2; i <= Math.sqrt(number); i++) {
            if (number % i == 0) return false;
        }
        return true;
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
