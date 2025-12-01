package chapter05.code;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.IntSupplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 스트림 생성 종합 예제
 * 
 * 다양한 스트림 생성 방법:
 * 1. 값으로 생성: Stream.of, Stream.empty
 * 2. 배열로 생성: Arrays.stream
 * 3. 파일로 생성: Files.lines
 * 4. 무한 스트림: Stream.iterate, Stream.generate
 * 
 * 학습 목표:
 * 1. 다양한 소스에서 스트림 생성
 * 2. 무한 스트림의 개념과 활용
 * 3. iterate vs generate 차이점
 * 4. 파일 스트림의 올바른 사용법
 * 
 * @author Modern Java In Action
 */
public class BuildingStreams {

  public static void main(String... args) throws Exception {
    
    // ==================================================
    // 1. Stream.of로 값 생성
    // ==================================================
    System.out.println("=== 1. Stream.of ===");
    
    /**
     * Stream.of(T... values)
     * 
     * 특징:
     * - 명시적으로 값을 나열하여 스트림 생성
     * - 가변 인자 사용
     * - 타입 추론 가능
     */
    Stream<String> stream = Stream.of("Java 8", "Lambdas", "In", "Action");
    stream.map(String::toUpperCase).forEach(System.out::println);
    System.out.println();

    // ==================================================
    // 2. Stream.empty로 빈 스트림
    // ==================================================
    System.out.println("=== 2. Stream.empty ===");
    
    /**
     * Stream.empty()
     * 
     * 특징:
     * - 요소가 없는 스트림
     * - null 대신 사용
     * 
     * 사용 시나리오:
     * - 메서드 반환값으로 "결과 없음" 표현
     * - NPE 방지
     */
    Stream<String> emptyStream = Stream.empty();
    System.out.println("빈 스트림 개수: " + emptyStream.count());
    System.out.println();

    // ==================================================
    // 3. Arrays.stream으로 배열 생성
    // ==================================================
    System.out.println("=== 3. Arrays.stream ===");
    
    /**
     * Arrays.stream(T[] array)
     * 
     * 특징:
     * - 배열을 스트림으로 변환
     * - 범위 지정 가능: Arrays.stream(array, start, end)
     * - 기본형 배열 지원: int[], long[], double[]
     */
    int[] numbers = { 2, 3, 5, 7, 11, 13 };
    System.out.println("배열 합계: " + Arrays.stream(numbers).sum());
    System.out.println();

    // ==================================================
    // 4. Stream.iterate로 짝수 생성
    // ==================================================
    System.out.println("=== 4. Stream.iterate (even numbers) ===");
    
    /**
     * Stream.iterate(T seed, UnaryOperator<T> f)
     * 
     * 동작:
     * - seed(초기값)부터 시작
     * - f를 반복 적용하여 무한 스트림 생성
     * - 이전 값에 의존 (순차적)
     * 
     * 특징:
     * - 무한 스트림 → limit 필수!
     * - 순차적 계산
     * - 이전 결과 사용
     * 
     * 주의:
     * - limit 없으면 무한 루프!
     */
    System.out.println("짝수 10개:");
    Stream.iterate(0, n -> n + 2)
        .limit(10)
        .forEach(System.out::println);
    System.out.println();

    // ==================================================
    // 5. iterate로 피보나치 (배열 버전)
    // ==================================================
    System.out.println("=== 5. Fibonacci with iterate (array) ===");
    
    /**
     * 피보나치 수열: 0, 1, 1, 2, 3, 5, 8, 13, ...
     * 
     * 규칙: f(n) = f(n-1) + f(n-2)
     * 
     * 구현:
     * - 상태: [이전, 현재]
     * - 다음: [현재, 이전+현재]
     */
    System.out.println("피보나치 배열 상태:");
    Stream.iterate(new int[] { 0, 1 }, t -> new int[] { t[1], t[0] + t[1] })
        .limit(10)
        .forEach(t -> System.out.printf("(%d, %d)%n", t[0], t[1]));
    System.out.println();

    // ==================================================
    // 6. iterate로 피보나치 (값 버전)
    // ==================================================
    System.out.println("=== 6. Fibonacci with iterate (values) ===");
    
    /**
     * 피보나치 값만 출력
     * 
     * map(t -> t[0]): 배열에서 첫 번째 값만 추출
     */
    System.out.println("피보나치 수열:");
    Stream.iterate(new int[] { 0, 1 }, t -> new int[] { t[1], t[0] + t[1] })
        .limit(10)
        .map(t -> t[0])
        .forEach(System.out::println);
    System.out.println();

    // ==================================================
    // 7. Stream.generate로 랜덤 생성
    // ==================================================
    System.out.println("=== 7. Stream.generate (random) ===");
    
    /**
     * Stream.generate(Supplier<T> s)
     * 
     * 동작:
     * - Supplier를 반복 호출하여 무한 스트림 생성
     * - 이전 값에 독립적 (상태 없음)
     * - 각 호출이 독립적
     * 
     * iterate vs generate:
     * - iterate: 이전 값 사용 (순차적, 상태 있음)
     * - generate: 독립적 생성 (병렬 가능, 상태 없음)
     * 
     * 사용 예:
     * - 랜덤 값 생성
     * - 상수 생성
     * 
     * 주의:
     * - limit 필수!
     * - Supplier가 상태를 가지면 병렬 처리 위험
     */
    System.out.println("랜덤 숫자 10개:");
    Stream.generate(Math::random)
        .limit(10)
        .forEach(System.out::println);
    System.out.println();

    // ==================================================
    // 8. generate로 상수 생성
    // ==================================================
    System.out.println("=== 8. Stream.generate (constant) ===");
    
    /**
     * generate로 상수 생성
     * 
     * () -> 1: 항상 1 반환
     */
    System.out.println("1을 5개:");
    IntStream.generate(() -> 1)
        .limit(5)
        .forEach(System.out::println);
    System.out.println();

    // ==================================================
    // 9. generate로 상수 생성 (익명 클래스)
    // ==================================================
    System.out.println("=== 9. Stream.generate (anonymous class) ===");
    
    /**
     * 익명 클래스로 Supplier 구현
     * 
     * 항상 2를 반환
     */
    System.out.println("2를 5개:");
    IntStream.generate(new IntSupplier() {
      @Override
      public int getAsInt() {
        return 2;
      }
    }).limit(5).forEach(System.out::println);
    System.out.println();

    // ==================================================
    // 10. generate로 피보나치 (비추천)
    // ==================================================
    System.out.println("=== 10. Fibonacci with generate (not recommended) ===");
    
    /**
     * generate로 피보나치 구현
     * 
     * 문제점:
     * - Supplier가 상태를 가져야 함 (변경 가능)
     * - 병렬 처리 불안전
     * - iterate가 더 적합
     * 
     * 결론:
     * - 순차적 계산 → iterate
     * - 독립적 생성 → generate
     */
    IntSupplier fib = new IntSupplier() {
      private int previous = 0;
      private int current = 1;

      @Override
      public int getAsInt() {
        int nextValue = previous + current;
        previous = current;
        current = nextValue;
        return previous;
      }
    };
    
    System.out.println("피보나치 (generate, 비추천):");
    IntStream.generate(fib)
        .limit(10)
        .forEach(System.out::println);
    System.out.println("→ 상태 변경 → 병렬 처리 위험!");
    System.out.println();

    // ==================================================
    // 11. Files.lines로 파일 처리
    // ==================================================
    System.out.println("=== 11. Files.lines ===");
    
    /**
     * Files.lines(Path path)
     * 
     * 특징:
     * - 파일의 각 행을 Stream<String>으로
     * - 지연 처리 (Lazy) - 한 번에 모두 읽지 않음
     * - 대용량 파일 처리에 적합
     * 
     * 주의사항:
     * - 파일 리소스를 사용 → try-with-resources 필수!
     * - close()를 명시적으로 호출하지 않으면 리소스 누수
     */
    try {
      long uniqueWords = Files.lines(Paths.get("lambdasinaction/chap5/data.txt"), Charset.defaultCharset())
          .flatMap(line -> Arrays.stream(line.split(" ")))
          .distinct()
          .count();
      System.out.println("고유 단어 수: " + uniqueWords);
    } catch (Exception e) {
      System.out.println("파일을 찾을 수 없습니다.");
    }
  }
}

/**
 * 스트림 생성 방법 정리
 * 
 * ┌──────────────────┬───────────────────┬─────────────────┐
 * │   생성 방법      │   메서드          │   특징          │
 * ├──────────────────┼───────────────────┼─────────────────┤
 * │ 값               │ of, empty         │ 고정 데이터     │
 * │ 배열             │ Arrays.stream     │ 범위 지정 가능  │
 * │ 파일             │ Files.lines       │ close 필수      │
 * │ 무한 (순차)      │ iterate           │ 이전 값 사용    │
 * │ 무한 (독립)      │ generate          │ 랜덤, 상수      │
 * └──────────────────┴───────────────────┴─────────────────┘
 * 
 * 무한 스트림 주의사항:
 * 1. limit 필수 (무한 루프 방지)
 * 2. sorted 사용 불가 (전체 필요)
 * 3. 상태 없는 연산 권장 (병렬 안전)
 */
