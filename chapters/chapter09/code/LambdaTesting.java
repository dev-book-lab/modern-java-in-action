package chapter09.code;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 람다 테스팅 완벽 가이드
 * 
 * 학습 목표:
 * - 보이는 람다 표현식 테스팅
 * - 람다를 사용하는 메서드 테스팅
 * - 복잡한 람다 메서드로 분리
 * - 고차원 함수 테스팅
 * 
 * 핵심 원칙:
 * - 람다 자체가 아닌 메서드의 동작 테스트
 * - public 필드는 직접 테스트 가능
 * - 복잡하면 메서드로 분리
 */
public class LambdaTesting {

  public static void main(String[] args) {
    System.out.println("=".repeat(80));
    System.out.println("람다 테스팅 완벽 가이드");
    System.out.println("=".repeat(80));

    testPublicLambdaField();
    testMethodBehavior();
    testExtractedMethod();
    testHigherOrderFunctions();
    demonstrateBestPractices();

    System.out.println("\n" + "=".repeat(80));
    System.out.println("💡 핵심 정리:");
    System.out.println("   ✅ public 필드 → 직접 테스트");
    System.out.println("   ✅ 일반적 → 메서드 동작 테스트");
    System.out.println("   ✅ 복잡한 람다 → 메서드 분리");
    System.out.println("   ✅ 고차원 함수 → 다양한 입력");
    System.out.println("=".repeat(80));
  }

  /**
   * 1. 보이는 람다 표현식 테스팅
   * 
   * public static 필드로 선언된 람다는 직접 테스트 가능
   * 
   * 예: Point.compareByXAndThenY
   */
  private static void testPublicLambdaField() {
    System.out.println("\n1️⃣  보이는 람다 표현식 테스팅\n");

    System.out.println("📌 Point 클래스의 public Comparator:");
    System.out.println("   public static final Comparator<Point> compareByXAndThenY");

    // 테스트 데이터
    Point p1 = new Point(10, 15);
    Point p2 = new Point(10, 20);
    Point p3 = new Point(5, 100);

    System.out.println("\n📌 테스트 실행:");
    
    // Test 1: X 같고 Y 다름
    int result1 = Point.compareByXAndThenY.compare(p1, p2);
    System.out.println("   p1(10,15) vs p2(10,20): " + result1);
    System.out.println("   → " + (result1 < 0 ? "✅ PASS" : "❌ FAIL") + 
                       " (p1.y < p2.y이므로 음수)");

    // Test 2: X 다름
    int result2 = Point.compareByXAndThenY.compare(p1, p3);
    System.out.println("   p1(10,15) vs p3(5,100): " + result2);
    System.out.println("   → " + (result2 > 0 ? "✅ PASS" : "❌ FAIL") + 
                       " (p1.x > p3.x이므로 양수)");

    // Test 3: 같은 점
    int result3 = Point.compareByXAndThenY.compare(p1, p1);
    System.out.println("   p1(10,15) vs p1(10,15): " + result3);
    System.out.println("   → " + (result3 == 0 ? "✅ PASS" : "❌ FAIL") + 
                       " (같으므로 0)");

    System.out.println("\n💡 핵심:");
    System.out.println("   - public 필드로 선언 → 직접 테스트 가능");
    System.out.println("   - 재사용 가능한 람다에 적합");
  }

  /**
   * 2. 람다를 사용하는 메서드의 동작 테스팅
   * 
   * 람다 자체가 아닌 메서드의 결과를 테스트
   * 
   * 핵심: 람다는 구현 세부사항
   */
  private static void testMethodBehavior() {
    System.out.println("\n2️⃣  람다를 사용하는 메서드 테스팅\n");

    System.out.println("📌 Point.moveAllPointsRightBy 메서드:");
    System.out.println("   내부에서 람다 사용하지만, 람다는 테스트 안 함!");

    // 테스트 데이터
    List<Point> points = Arrays.asList(
        new Point(5, 5),
        new Point(10, 5)
    );

    List<Point> expected = Arrays.asList(
        new Point(15, 5),
        new Point(20, 5)
    );

    System.out.println("\n📌 테스트 실행:");
    System.out.println("   입력: " + points);
    System.out.println("   이동: +10");
    
    List<Point> result = Point.moveAllPointsRightBy(points, 10);
    System.out.println("   결과: " + result);
    System.out.println("   예상: " + expected);
    
    boolean passed = result.equals(expected);
    System.out.println("   → " + (passed ? "✅ PASS" : "❌ FAIL"));

    // 추가 테스트: 빈 리스트
    System.out.println("\n📌 엣지 케이스 - 빈 리스트:");
    List<Point> emptyResult = Point.moveAllPointsRightBy(Arrays.asList(), 10);
    System.out.println("   결과: " + emptyResult);
    System.out.println("   → " + (emptyResult.isEmpty() ? "✅ PASS" : "❌ FAIL"));

    System.out.println("\n💡 핵심:");
    System.out.println("   - 람다는 내부 구현 (테스트 안 함)");
    System.out.println("   - 메서드의 동작 결과를 테스트");
    System.out.println("   - 다양한 입력/경계 조건 테스트");
  }

  /**
   * 3. 복잡한 람다를 메서드로 분리
   * 
   * Before: 복잡한 람다 (테스트 어려움)
   * After: 메서드 추출 (테스트 가능)
   * 
   * 기준: 3줄 이상 → 메서드 분리 고려
   */
  private static void testExtractedMethod() {
    System.out.println("\n3️⃣  복잡한 람다 메서드로 분리\n");

    System.out.println("📌 Before - 복잡한 람다 (테스트 어려움):");
    System.out.println("   words.stream()");
    System.out.println("       .filter(s -> {");
    System.out.println("           // 10줄의 복잡한 검증 로직...");
    System.out.println("       })");
    System.out.println("   → 람다를 어떻게 테스트?");

    System.out.println("\n📌 After - 메서드 추출 (테스트 가능):");
    System.out.println("   words.stream()");
    System.out.println("       .filter(DataProcessor::isValid)  // 메서드 참조!");

    // 테스트 데이터
    List<String> testCases = Arrays.asList(
        "java",      // 유효
        "stream",    // 유효
        "hi",        // 무효 (짧음)
        "HELLO",     // 무효 (대문자)
        "test123"    // 유효
    );

    System.out.println("\n📌 isValid 메서드 테스트:");
    for (String word : testCases) {
      boolean valid = DataProcessor.isValid(word);
      System.out.printf("   \"%s\" → %s%n", word, valid ? "✅ 유효" : "❌ 무효");
    }

    // 실제 필터링 테스트
    System.out.println("\n📌 전체 파이프라인 테스트:");
    List<String> words = Arrays.asList("java", "Stream", "hi", "lambda");
    List<String> filtered = DataProcessor.filterValid(words);
    System.out.println("   입력: " + words);
    System.out.println("   결과: " + filtered);
    System.out.println("   → " + (filtered.size() == 2 ? "✅ PASS" : "❌ FAIL"));

    System.out.println("\n💡 핵심:");
    System.out.println("   - 복잡한 람다 (3줄+) → 메서드 분리");
    System.out.println("   - 메서드 직접 테스트 가능");
    System.out.println("   - 재사용성, 가독성 향상");
  }

  /**
   * 4. 고차원 함수 테스팅
   * 
   * 고차원 함수: 함수를 인수로 받거나 반환하는 함수
   * 
   * 전략:
   * - 함수를 인수로 받음 → 다양한 함수로 테스트
   * - 함수를 반환 → 반환된 함수 실행하여 테스트
   */
  private static void testHigherOrderFunctions() {
    System.out.println("\n4️⃣  고차원 함수 테스팅\n");

    // Case 1: 함수를 인수로 받는 경우
    System.out.println("📌 Case 1 - 함수를 인수로 받는 경우:");
    System.out.println("   filter(List<T>, Predicate<T>)");

    List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6);

    // Test 1: 짝수 필터
    List<Integer> evenNumbers = filter(numbers, n -> n % 2 == 0);
    System.out.println("   짝수: " + evenNumbers);
    System.out.println("   → " + (evenNumbers.equals(Arrays.asList(2, 4, 6)) ? 
                       "✅ PASS" : "❌ FAIL"));

    // Test 2: 3보다 큰 수
    List<Integer> greaterThan3 = filter(numbers, n -> n > 3);
    System.out.println("   >3: " + greaterThan3);
    System.out.println("   → " + (greaterThan3.equals(Arrays.asList(4, 5, 6)) ? 
                       "✅ PASS" : "❌ FAIL"));

    // Test 3: 모든 수
    List<Integer> allNumbers = filter(numbers, n -> true);
    System.out.println("   전부: " + allNumbers);
    System.out.println("   → " + (allNumbers.equals(numbers) ? 
                       "✅ PASS" : "❌ FAIL"));

    // Case 2: 함수를 반환하는 경우
    System.out.println("\n📌 Case 2 - 함수를 반환하는 경우:");
    System.out.println("   greaterThan(int) → Predicate<Integer>");

    // Test 1: greaterThan(5)
    Predicate<Integer> gt5 = greaterThan(5);
    System.out.println("   greaterThan(5).test(6): " + gt5.test(6));
    System.out.println("   → " + (gt5.test(6) ? "✅ PASS" : "❌ FAIL"));
    System.out.println("   greaterThan(5).test(3): " + gt5.test(3));
    System.out.println("   → " + (!gt5.test(3) ? "✅ PASS" : "❌ FAIL"));

    // Test 2: greaterThan(10)
    Predicate<Integer> gt10 = greaterThan(10);
    System.out.println("   greaterThan(10).test(15): " + gt10.test(15));
    System.out.println("   → " + (gt10.test(15) ? "✅ PASS" : "❌ FAIL"));

    // Case 3: Comparator 반환
    System.out.println("\n📌 Case 3 - Comparator 반환:");
    Comparator<String> byLength = getComparator("length");
    int cmp = byLength.compare("hello", "hi");
    System.out.println("   byLength(\"hello\", \"hi\"): " + cmp);
    System.out.println("   → " + (cmp > 0 ? "✅ PASS" : "❌ FAIL") + 
                       " (\"hello\"가 더 길므로 양수)");

    System.out.println("\n💡 핵심:");
    System.out.println("   - 함수를 인수로 → 다양한 Predicate/Function 전달");
    System.out.println("   - 함수를 반환 → 반환된 함수 실행 (.test(), .apply())");
    System.out.println("   - 모든 경계 조건 테스트");
  }

  /**
   * 5. 베스트 프랙티스
   */
  private static void demonstrateBestPractices() {
    System.out.println("\n5️⃣  테스팅 베스트 프랙티스\n");

    System.out.println("📌 테스트 우선순위:");
    System.out.println("   1순위: public API (메서드 동작)");
    System.out.println("   2순위: public 필드 람다");
    System.out.println("   3순위: 추출된 헬퍼 메서드");

    System.out.println("\n📌 메서드 분리 기준:");
    System.out.println("   ✅ 3줄 이상");
    System.out.println("   ✅ 재사용 필요");
    System.out.println("   ✅ 복잡한 비즈니스 로직");
    System.out.println("   ✅ 독립적으로 테스트 가치 있음");

    System.out.println("\n📌 테스트 체크리스트:");
    System.out.println("   ☐ 정상 케이스");
    System.out.println("   ☐ 빈 컬렉션");
    System.out.println("   ☐ null 처리");
    System.out.println("   ☐ 경계값");
    System.out.println("   ☐ 예외 상황");

    System.out.println("\n📌 실전 예제:");
    
    // 정상 케이스
    List<Integer> normal = filter(Arrays.asList(1, 2, 3), n -> n > 1);
    System.out.println("   정상: " + normal + " → " + 
                       (normal.size() == 2 ? "✅" : "❌"));

    // 빈 컬렉션
    List<Integer> empty = filter(Arrays.asList(), n -> n > 1);
    System.out.println("   빈 컬렉션: " + empty + " → " + 
                       (empty.isEmpty() ? "✅" : "❌"));

    // 모든 요소 필터링
    List<Integer> allFiltered = filter(Arrays.asList(1, 2, 3), n -> n > 10);
    System.out.println("   전부 필터: " + allFiltered + " → " + 
                       (allFiltered.isEmpty() ? "✅" : "❌"));

    // null 포함 (주의 필요)
    try {
      List<Integer> withNull = Arrays.asList(1, null, 3);
      filter(withNull, n -> n > 1);
      System.out.println("   null 포함: ❌ NullPointerException 예상");
    } catch (NullPointerException e) {
      System.out.println("   null 포함: ✅ NullPointerException 발생 (예상됨)");
    }
  }

  // ========== 헬퍼 메서드 ==========

  /**
   * 고차원 함수: 필터링
   * 
   * Predicate를 인수로 받음
   */
  public static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
    return list.stream()
        .filter(predicate)
        .collect(Collectors.toList());
  }

  /**
   * 고차원 함수: Predicate 생성
   * 
   * Predicate를 반환
   */
  public static Predicate<Integer> greaterThan(int threshold) {
    return x -> x > threshold;
  }

  /**
   * 고차원 함수: Comparator 생성
   * 
   * Comparator를 반환
   */
  public static Comparator<String> getComparator(String type) {
    switch (type) {
      case "length":
        return Comparator.comparingInt(String::length);
      case "alphabetical":
        return Comparator.naturalOrder();
      default:
        throw new IllegalArgumentException("Unknown type: " + type);
    }
  }

  // ========== 도메인 클래스 ==========

  /**
   * Point 클래스
   * 
   * public Comparator 필드로 람다 노출
   */
  static class Point {
    private final int x;
    private final int y;

    // ✅ public static 필드 → 직접 테스트 가능
    public static final Comparator<Point> compareByXAndThenY =
        Comparator.comparingInt(Point::getX)
                  .thenComparingInt(Point::getY);

    public Point(int x, int y) {
      this.x = x;
      this.y = y;
    }

    public int getX() {
      return x;
    }

    public int getY() {
      return y;
    }

    /**
     * 람다를 사용하는 메서드
     * 
     * 람다 자체가 아닌 메서드의 동작을 테스트
     */
    public static List<Point> moveAllPointsRightBy(List<Point> points, int x) {
      return points.stream()
          .map(p -> new Point(p.getX() + x, p.getY()))  // 람다
          .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Point point = (Point) o;
      return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
      return Objects.hash(x, y);
    }

    @Override
    public String toString() {
      return "(" + x + "," + y + ")";
    }
  }

  /**
   * DataProcessor 클래스
   * 
   * 복잡한 람다를 메서드로 추출한 예
   */
  static class DataProcessor {
    
    /**
     * 복잡한 검증 로직을 메서드로 추출
     * 
     * 이제 독립적으로 테스트 가능!
     */
    public static boolean isValid(String s) {
      if (s == null) return false;
      if (s.length() < 3) return false;  // 최소 3글자
      if (!s.equals(s.toLowerCase())) return false;  // 소문자만
      if (!s.matches("^[a-z0-9]+$")) return false;  // 영숫자만
      return true;
    }

    /**
     * 람다 대신 메서드 참조 사용
     */
    public static List<String> filterValid(List<String> words) {
      return words.stream()
          .filter(DataProcessor::isValid)  // 메서드 참조
          .collect(Collectors.toList());
    }

    /**
     * 복잡한 변환 로직도 메서드로
     */
    public static String transform(String s) {
      if (s == null) return "";
      return s.trim()
              .toLowerCase()
              .replaceAll("\\s+", "_");
    }
  }

  /**
   * 테스팅 패턴 정리:
   * 
   * 1. 보이는 람다 (public 필드)
   *    Point.compareByXAndThenY.compare(p1, p2)
   * 
   * 2. 메서드 동작 (람다는 내부)
   *    Point.moveAllPointsRightBy(points, 10)
   * 
   * 3. 메서드 추출 (복잡한 람다)
   *    DataProcessor.isValid("test")
   * 
   * 4. 고차원 함수 (함수 인수/반환)
   *    filter(list, predicate)
   *    greaterThan(5).test(6)
   * 
   * 테스트 작성 순서:
   * 1. 정상 케이스
   * 2. 빈 입력
   * 3. 경계값
   * 4. 예외 상황
   * 5. null 처리
   * 
   * 메서드 분리 시점:
   * - 3줄 이상
   * - 재사용 필요
   * - 테스트 가치 있음
   * - 비즈니스 로직
   */
}
