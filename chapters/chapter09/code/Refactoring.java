package chapter09.code;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 리팩터링 종합 예제
 * 
 * 학습 목표:
 * - 익명 클래스 → 람다 리팩터링
 * - 람다 → 메서드 참조 리팩터링
 * - 명령형 → 스트림 리팩터링
 * - 조건부 연기 실행 패턴
 * - 실행 어라운드 패턴
 * 
 * 주요 개념:
 * - 코드 가독성 개선
 * - 함수형 프로그래밍 패턴
 * - 성능 최적화
 */
public class Refactoring {

  private static final Logger logger = Logger.getLogger(Refactoring.class.getName());

  public static void main(String[] args) {
    System.out.println("=".repeat(80));
    System.out.println("리팩터링 종합 예제");
    System.out.println("=".repeat(80));

    demonstrateAnonymousToLambda();
    demonstrateLambdaToMethodReference();
    demonstrateImperativeToStream();
    demonstrateConditionalDeferred();
    demonstrateExecuteAround();

    System.out.println("\n" + "=".repeat(80));
    System.out.println("💡 핵심 정리:");
    System.out.println("   ✅ 익명 클래스 → 람다: 간결성");
    System.out.println("   ✅ 람다 → 메서드 참조: 명확성");
    System.out.println("   ✅ 명령형 → 스트림: 선언적");
    System.out.println("   ✅ 조건부 연기: 불필요한 평가 방지");
    System.out.println("   ✅ 실행 어라운드: 중복 제거");
    System.out.println("=".repeat(80));
  }

  /**
   * 1. 익명 클래스 → 람다 리팩터링
   * 
   * Before: 장황한 익명 클래스
   * After: 간결한 람다 표현식
   * 
   * 주의사항:
   * - this 의미 차이
   * - 섀도잉 불가
   * - 오버로딩 모호함
   */
  private static void demonstrateAnonymousToLambda() {
    System.out.println("\n1️⃣  익명 클래스 → 람다 리팩터링\n");

    List<Apple> inventory = Arrays.asList(
        new Apple(80, "green"),
        new Apple(155, "green"),
        new Apple(120, "red")
    );

    // ❌ Before: 익명 클래스 (장황)
    System.out.println("📌 Before - 익명 클래스:");
    inventory.sort(new Comparator<Apple>() {
      @Override
      public int compare(Apple a1, Apple a2) {
        return Integer.compare(a1.getWeight(), a2.getWeight());
      }
    });
    System.out.println("   정렬 완료: " + inventory);

    // ✅ After: 람다 (간결)
    System.out.println("\n📌 After - 람다:");
    inventory.sort((a1, a2) -> Integer.compare(a1.getWeight(), a2.getWeight()));
    System.out.println("   정렬 완료: " + inventory);

    // 코드 비교
    System.out.println("\n📌 비교:");
    System.out.println("   익명 클래스: 7줄");
    System.out.println("   람다:       1줄");
    System.out.println("   → 86% 코드 감소!");
  }

  /**
   * 2. 람다 → 메서드 참조 리팩터링
   * 
   * Before: 람다 표현식
   * After: 메서드 참조
   * 
   * 장점:
   * - 의도가 더 명확
   * - 재사용 가능
   * - 테스트 용이
   */
  private static void demonstrateLambdaToMethodReference() {
    System.out.println("\n2️⃣  람다 → 메서드 참조 리팩터링\n");

    List<Apple> inventory = Arrays.asList(
        new Apple(80, "green"),
        new Apple(155, "green"),
        new Apple(120, "red")
    );

    // ❌ Before: 람다
    System.out.println("📌 Before - 람다:");
    inventory.sort((a1, a2) -> Integer.compare(a1.getWeight(), a2.getWeight()));
    System.out.println("   " + inventory);

    // ✅ After: Comparing + 메서드 참조
    System.out.println("\n📌 After - 메서드 참조:");
    inventory.sort(Comparator.comparing(Apple::getWeight));
    System.out.println("   " + inventory);

    // 복잡한 예제: 칼로리 수준 계산
    System.out.println("\n📌 복잡한 람다 → 메서드 추출:");
    
    List<Dish> menu = Arrays.asList(
        new Dish("pork", 800),
        new Dish("beef", 700),
        new Dish("chicken", 400),
        new Dish("rice", 350)
    );

    // ❌ Before: 복잡한 람다
    System.out.println("   Before:");
    menu.stream()
        .map(dish -> {
          if (dish.getCalories() <= 400) return "DIET";
          else if (dish.getCalories() <= 700) return "NORMAL";
          else return "FAT";
        })
        .forEach(level -> System.out.println("   - " + level));

    // ✅ After: 메서드 참조
    System.out.println("\n   After:");
    menu.stream()
        .map(Dish::getCaloricLevel)  // 메서드로 추출!
        .forEach(level -> System.out.println("   - " + level));
  }

  /**
   * 3. 명령형 → 스트림 리팩터링
   * 
   * Before: for 루프 + if
   * After: filter + map + collect
   * 
   * 장점:
   * - 의도 명확 (무엇을 하는지)
   * - 최적화 (쇼트서킷, 게으른 평가)
   * - 병렬 처리 간편
   */
  private static void demonstrateImperativeToStream() {
    System.out.println("\n3️⃣  명령형 → 스트림 리팩터링\n");

    List<Dish> menu = Arrays.asList(
        new Dish("pork", 800),
        new Dish("beef", 700),
        new Dish("chicken", 400),
        new Dish("rice", 350),
        new Dish("pizza", 550)
    );

    // ❌ Before: 명령형
    System.out.println("📌 Before - 명령형:");
    List<String> dishNames = new ArrayList<>();
    for (Dish dish : menu) {
      if (dish.getCalories() > 300) {  // 필터링
        dishNames.add(dish.getName());  // 추출
      }
    }
    System.out.println("   결과: " + dishNames);
    System.out.println("   특징: 어떻게(How) 하는지 명시");

    // ✅ After: 스트림
    System.out.println("\n📌 After - 스트림:");
    List<String> dishNamesStream = menu.stream()
        .filter(dish -> dish.getCalories() > 300)  // 필터링
        .map(Dish::getName)                        // 추출
        .collect(Collectors.toList());
    System.out.println("   결과: " + dishNamesStream);
    System.out.println("   특징: 무엇을(What) 하는지 명시");

    // 병렬 처리
    System.out.println("\n📌 병렬 처리 (간편):");
    List<String> dishNamesParallel = menu.parallelStream()
        .filter(dish -> dish.getCalories() > 300)
        .map(Dish::getName)
        .collect(Collectors.toList());
    System.out.println("   결과: " + dishNamesParallel);
    System.out.println("   → stream()만 parallelStream()으로 변경!");
  }

  /**
   * 4. 조건부 연기 실행 패턴
   * 
   * 문제: 불필요한 평가
   * 해결: Supplier로 평가 연기
   * 
   * 성능:
   * - Before: 항상 평가 (비효율)
   * - After: 필요 시에만 평가 (효율)
   */
  private static void demonstrateConditionalDeferred() {
    System.out.println("\n4️⃣  조건부 연기 실행 패턴\n");

    // 로거 레벨 설정 (FINER보다 높음 → 로그 안 남음)
    logger.setLevel(Level.INFO);

    // ❌ Before: 즉시 평가 (비효율)
    System.out.println("📌 Before - 즉시 평가:");
    long start1 = System.nanoTime();
    if (logger.isLoggable(Level.FINER)) {
      logger.finer("Problem: " + generateDiagnostic());  // 평가됨!
    }
    long duration1 = System.nanoTime() - start1;
    System.out.println("   문제: generateDiagnostic() 항상 실행됨");
    System.out.println("   시간: " + duration1 / 1_000_000.0 + "ms");

    // ✅ After: 지연 평가 (효율)
    System.out.println("\n📌 After - 지연 평가:");
    long start2 = System.nanoTime();
    logger.log(Level.FINER, () -> "Problem: " + generateDiagnostic());
    long duration2 = System.nanoTime() - start2;
    System.out.println("   해결: 조건 만족 시에만 실행!");
    System.out.println("   시간: " + duration2 / 1_000_000.0 + "ms");
    System.out.println("   → " + String.format("%.0f배 빠름!", (double) duration1 / duration2));

    // log 메서드 내부 동작
    System.out.println("\n📌 내부 동작:");
    System.out.println("   public void log(Level level, Supplier<String> msgSupplier) {");
    System.out.println("       if (logger.isLoggable(level)) {");
    System.out.println("           log(level, msgSupplier.get());  // 필요 시에만 실행!");
    System.out.println("       }");
    System.out.println("   }");
  }

  /**
   * 5. 실행 어라운드 패턴
   * 
   * 문제: 준비/정리 코드 중복
   * 해결: 템플릿 메서드 + 람다
   * 
   * 구조:
   * - 준비 (setup)
   * - 실행 (action) ← 람다로 파라미터화
   * - 정리 (cleanup)
   */
  private static void demonstrateExecuteAround() {
    System.out.println("\n5️⃣  실행 어라운드 패턴\n");

    // ❌ Before: 중복 코드
    System.out.println("📌 Before - 중복 코드:");
    System.out.println("   // 한 줄 읽기");
    System.out.println("   try (BufferedReader br = new BufferedReader(...)) {");
    System.out.println("       return br.readLine();  // ← 이 부분만 다름");
    System.out.println("   }");
    System.out.println();
    System.out.println("   // 두 줄 읽기");
    System.out.println("   try (BufferedReader br = new BufferedReader(...)) {");
    System.out.println("       return br.readLine() + br.readLine();  // ← 이 부분만 다름");
    System.out.println("   }");
    System.out.println("   → 준비/정리 코드 중복!");

    // ✅ After: 실행 어라운드
    System.out.println("\n📌 After - 실행 어라운드:");
    try {
      String oneLine = processFile(br -> br.readLine());
      System.out.println("   한 줄: " + oneLine);

      String twoLines = processFile(br -> br.readLine() + "\n" + br.readLine());
      System.out.println("   두 줄: " + twoLines);

    } catch (IOException e) {
      System.err.println("   파일 처리 오류: " + e.getMessage());
    }

    // 구조 설명
    System.out.println("\n📌 구조:");
    System.out.println("   1. 준비: 파일 열기");
    System.out.println("   2. 실행: 람다로 전달된 동작");
    System.out.println("   3. 정리: 파일 닫기 (자동)");
    System.out.println("   → 준비/정리 코드 재사용!");
  }

  // ========== 헬퍼 메서드 ==========

  /**
   * 진단 정보 생성 (비용이 큰 연산 시뮬레이션)
   */
  private static String generateDiagnostic() {
    try {
      Thread.sleep(100);  // 100ms 소요
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return "Diagnostic info";
  }

  /**
   * 실행 어라운드 템플릿 메서드
   * 
   * @param processor 파일 처리 로직 (람다)
   * @return 처리 결과
   */
  private static String processFile(BufferedReaderProcessor processor) throws IOException {
    // 준비: 리소스 생성 (실제로는 문자열로 시뮬레이션)
    String mockData = "Hello, Modern Java in Action!\nThis is a test for Execute Around pattern.\n";
    
    try (BufferedReader br = new BufferedReader(
        new java.io.StringReader(mockData))) {
      
      // 실행: 람다로 전달된 동작
      return processor.process(br);
      
      // 정리: try-with-resources로 자동
    }
  }

  /**
   * 함수형 인터페이스: BufferedReader 처리
   */
  @FunctionalInterface
  interface BufferedReaderProcessor {
    String process(BufferedReader br) throws IOException;
  }

  // ========== 도메인 클래스 ==========

  /**
   * Apple 클래스
   */
  static class Apple {
    private final int weight;
    private final String color;

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
      return color + "(" + weight + "g)";
    }
  }

  /**
   * Dish 클래스
   */
  static class Dish {
    private final String name;
    private final int calories;

    public Dish(String name, int calories) {
      this.name = name;
      this.calories = calories;
    }

    public String getName() {
      return name;
    }

    public int getCalories() {
      return calories;
    }

    /**
     * 칼로리 수준 계산 (메서드 추출)
     * 
     * 복잡한 람다를 메서드로 분리한 예
     */
    public String getCaloricLevel() {
      if (this.calories <= 400) return "DIET";
      else if (this.calories <= 700) return "NORMAL";
      else return "FAT";
    }

    @Override
    public String toString() {
      return name + "(" + calories + " cal)";
    }
  }

  /**
   * 주의사항 정리:
   * 
   * 1. 익명 클래스 → 람다
   *    ⚠️ this 의미 다름
   *    ⚠️ 섀도잉 불가
   *    ⚠️ 오버로딩 모호함
   * 
   * 2. 람다 → 메서드 참조
   *    ✅ 3줄 이상 람다 → 메서드 분리
   *    ✅ 재사용 필요 → 메서드 참조
   * 
   * 3. 명령형 → 스트림
   *    ✅ for + if → filter + map
   *    ✅ 병렬 처리 간편
   * 
   * 4. 조건부 연기
   *    ✅ Supplier로 평가 연기
   *    ✅ 10-1000배 성능 향상
   * 
   * 5. 실행 어라운드
   *    ✅ 준비/정리 코드 재사용
   *    ✅ 중복 제거
   */
}
