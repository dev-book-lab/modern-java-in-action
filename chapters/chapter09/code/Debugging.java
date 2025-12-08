package chapter09.code;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 람다 디버깅 완벽 가이드
 * 
 * 학습 목표:
 * - 람다 스택 트레이스 읽기
 * - 메서드 참조의 장점
 * - peek를 활용한 로깅
 * - 디버깅 전략
 * 
 * 핵심 개념:
 * - lambda$메서드명$번호 패턴
 * - 메서드 참조 → 명확한 스택 트레이스
 * - peek → 중간 연산 로깅
 */
public class Debugging {

  public static void main(String[] args) {
    System.out.println("=".repeat(80));
    System.out.println("람다 디버깅 완벽 가이드");
    System.out.println("=".repeat(80));

    demonstrateLambdaStackTrace();
    demonstrateMethodReferenceStackTrace();
    demonstratePeekLogging();
    demonstrateDebuggingStrategies();
    demonstrateBestPractices();

    System.out.println("\n" + "=".repeat(80));
    System.out.println("💡 핵심 정리:");
    System.out.println("   ✅ 람다: lambda$메서드$번호");
    System.out.println("   ✅ 메서드 참조: 명확한 메서드 이름");
    System.out.println("   ✅ peek: 중간 값 확인");
    System.out.println("   ✅ 복잡하면 메서드 분리");
    System.out.println("=".repeat(80));
  }

  /**
   * 1. 람다 스택 트레이스
   * 
   * 람다 이름 패턴:
   * - lambda$메서드명$번호
   * - lambda$main$0: main의 첫 번째 람다
   * - lambda$main$1: main의 두 번째 람다
   */
  private static void demonstrateLambdaStackTrace() {
    System.out.println("\n1️⃣  람다 스택 트레이스\n");

    System.out.println("📌 람다 이름 패턴:");
    System.out.println("   lambda$메서드명$번호");
    System.out.println("   - lambda$main$0: main의 첫 번째 람다");
    System.out.println("   - lambda$main$1: main의 두 번째 람다");
    System.out.println("   - lambda$process$0: process의 첫 번째 람다");

    // 예제 1: NullPointerException
    System.out.println("\n📌 예제 1 - NullPointerException:");
    System.out.println("   코드:");
    System.out.println("   List<Point> points = Arrays.asList(");
    System.out.println("       new Point(12, 2),");
    System.out.println("       null  // ← 문제!");
    System.out.println("   );");
    System.out.println("   points.stream()");
    System.out.println("       .map(p -> p.getX())  // lambda$main$0");
    System.out.println("       .forEach(System.out::println);");

    try {
      List<Point> points = Arrays.asList(new Point(12, 2), null);
      points.stream()
          .map(p -> p.getX())  // lambda$main$0 - 첫 번째 람다
          .forEach(System.out::println);
    } catch (NullPointerException e) {
      System.out.println("\n   스택 트레이스:");
      System.out.println("   " + e.getClass().getName());
      
      // 첫 몇 줄만 출력
      StackTraceElement[] stack = e.getStackTrace();
      for (int i = 0; i < Math.min(3, stack.length); i++) {
        String line = stack[i].toString();
        System.out.println("       at " + line);
        
        // lambda 패턴 강조
        if (line.contains("lambda$")) {
          System.out.println("           ↑ 람다 표시!");
        }
      }
    }

    // 예제 2: 여러 람다
    System.out.println("\n📌 예제 2 - 여러 람다 구분:");
    System.out.println("   코드:");
    System.out.println("   numbers.stream()");
    System.out.println("       .map(n -> n * 2)        // lambda$main$1");
    System.out.println("       .filter(n -> n > 10)    // lambda$main$2");
    System.out.println("       .map(n -> n / 0)        // lambda$main$3 ← 에러!");

    try {
      List<Integer> numbers = Arrays.asList(2, 3, 4, 5);
      numbers.stream()
          .map(n -> n * 2)       // lambda$main$1
          .filter(n -> n > 10)   // lambda$main$2
          .map(n -> n / 0)       // lambda$main$3 - 에러!
          .forEach(System.out::println);
    } catch (ArithmeticException e) {
      System.out.println("\n   스택 트레이스:");
      System.out.println("   " + e.getClass().getName() + ": " + e.getMessage());
      
      StackTraceElement[] stack = e.getStackTrace();
      for (int i = 0; i < Math.min(3, stack.length); i++) {
        String line = stack[i].toString();
        System.out.println("       at " + line);
        
        if (line.contains("lambda$main$3")) {
          System.out.println("           ↑ 세 번째 람다 (map)에서 에러!");
        }
      }
    }

    System.out.println("\n💡 해석 방법:");
    System.out.println("   1. lambda$main$N → main 메서드의 N번째 람다");
    System.out.println("   2. 줄 번호 확인");
    System.out.println("   3. 코드에서 해당 람다 찾기");
  }

  /**
   * 2. 메서드 참조 스택 트레이스
   * 
   * 장점: 명확한 메서드 이름 표시
   */
  private static void demonstrateMethodReferenceStackTrace() {
    System.out.println("\n2️⃣  메서드 참조 스택 트레이스\n");

    System.out.println("📌 메서드 참조의 장점:");
    System.out.println("   람다: lambda$main$N (익명, 불명확)");
    System.out.println("   메서드 참조: 명확한 메서드 이름!");

    // 예제: 메서드 참조
    System.out.println("\n📌 예제 - 메서드 참조:");
    System.out.println("   코드:");
    System.out.println("   numbers.stream()");
    System.out.println("       .map(DebuggingExample::divideByZero)  // 메서드 참조");
    System.out.println("       .forEach(System.out::println);");

    try {
      List<Integer> numbers = Arrays.asList(1, 2, 3);
      numbers.stream()
          .map(Debugging::divideByZero)  // 메서드 참조
          .forEach(System.out::println);
    } catch (ArithmeticException e) {
      System.out.println("\n   스택 트레이스:");
      System.out.println("   " + e.getClass().getName() + ": " + e.getMessage());
      
      StackTraceElement[] stack = e.getStackTrace();
      for (int i = 0; i < Math.min(3, stack.length); i++) {
        String line = stack[i].toString();
        System.out.println("       at " + line);
        
        if (line.contains("divideByZero")) {
          System.out.println("           ↑ 명확한 메서드 이름!");
        }
      }
    }

    System.out.println("\n📌 비교:");
    System.out.println("   ┌──────────────────────┬─────────────────────────┐");
    System.out.println("   │      방식             │    스택 트레이스         │");
    System.out.println("   ├──────────────────────┼─────────────────────────┤");
    System.out.println("   │ 람다                 │ lambda$main$0           │");
    System.out.println("   │ 메서드 참조          │ divideByZero            │");
    System.out.println("   └──────────────────────┴─────────────────────────┘");

    System.out.println("\n💡 권장:");
    System.out.println("   - 프로덕션 코드: 메서드 참조 사용");
    System.out.println("   - 디버깅 필요: 메서드로 추출");
    System.out.println("   - 간단한 람다: 그대로 유지");
  }

  /**
   * 3. peek를 활용한 로깅
   * 
   * peek: 중간 연산 (스트림 소비 안 함)
   * forEach: 최종 연산 (스트림 소비)
   */
  private static void demonstratePeekLogging() {
    System.out.println("\n3️⃣  peek를 활용한 로깅\n");

    System.out.println("📌 peek vs forEach:");
    System.out.println("   peek: 중간 연산 (스트림 계속 사용 가능)");
    System.out.println("   forEach: 최종 연산 (스트림 소비됨)");

    // 예제 1: 기본 사용
    System.out.println("\n📌 예제 1 - 각 단계 추적:");
    List<Integer> numbers = Arrays.asList(2, 3, 4, 5);
    
    System.out.println("   입력: " + numbers);
    System.out.println("\n   처리 과정:");
    
    List<Integer> result = numbers.stream()
        .peek(n -> System.out.println("   from stream: " + n))
        .map(n -> n + 17)
        .peek(n -> System.out.println("   after map (+17): " + n))
        .filter(n -> n % 2 == 0)
        .peek(n -> System.out.println("   after filter (even): " + n))
        .limit(3)
        .peek(n -> System.out.println("   after limit (3): " + n))
        .collect(Collectors.toList());
    
    System.out.println("\n   최종 결과: " + result);

    // 예제 2: 문제 찾기
    System.out.println("\n📌 예제 2 - 문제 찾기:");
    System.out.println("   질문: 왜 결과가 빈 리스트인가?");
    
    List<String> words = Arrays.asList("hello", "world", "java");
    
    List<String> debugResult = words.stream()
        .peek(s -> System.out.println("   원본: " + s))
        .map(String::toUpperCase)
        .peek(s -> System.out.println("   대문자: " + s))
        .filter(s -> s.length() > 10)  // ← 문제! 모두 10 이하
        .peek(s -> System.out.println("   필터 통과: " + s))
        .collect(Collectors.toList());
    
    System.out.println("   결과: " + debugResult);
    System.out.println("   → peek로 확인: filter에서 모두 제거됨!");

    System.out.println("\n💡 활용:");
    System.out.println("   - 각 단계 중간 값 확인");
    System.out.println("   - 문제 위치 파악");
    System.out.println("   - 데이터 흐름 추적");
  }

  /**
   * 4. 디버깅 전략
   */
  private static void demonstrateDebuggingStrategies() {
    System.out.println("\n4️⃣  디버깅 전략\n");

    System.out.println("📌 전략 1: 메서드 참조 사용");
    System.out.println("   Before:");
    System.out.println("   .map(n -> n / 0)  // lambda$main$N");
    System.out.println();
    System.out.println("   After:");
    System.out.println("   .map(MyClass::divideByZero)  // divideByZero");

    System.out.println("\n📌 전략 2: peek 사용");
    System.out.println("   .peek(n -> System.out.println(\"Processing: \" + n))");
    System.out.println("   .map(n -> complexOperation(n))");
    System.out.println("   .peek(n -> System.out.println(\"Result: \" + n))");

    System.out.println("\n📌 전략 3: 단계별 분리");
    System.out.println("   Before (체이닝):");
    System.out.println("   List<String> result = words.stream()");
    System.out.println("       .filter(...).map(...).sorted().collect(toList());");
    System.out.println();
    System.out.println("   After (단계별):");
    System.out.println("   Stream<String> s1 = words.stream();");
    System.out.println("   Stream<String> s2 = s1.filter(...);  // 브레이크포인트");
    System.out.println("   Stream<String> s3 = s2.map(...);     // 브레이크포인트");
    System.out.println("   List<String> result = s3.collect(toList());");

    System.out.println("\n📌 전략 4: try-catch 래핑");
    System.out.println("   .map(n -> {");
    System.out.println("       try {");
    System.out.println("           return complexOperation(n);");
    System.out.println("       } catch (Exception e) {");
    System.out.println("           System.err.println(\"Error: \" + e);");
    System.out.println("           return defaultValue;");
    System.out.println("       }");
    System.out.println("   })");

    // 실제 예제
    System.out.println("\n📌 실전 예제:");
    List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
    
    System.out.println("   안전한 처리 (예외 처리 포함):");
    List<Integer> safeResult = numbers.stream()
        .map(n -> {
          try {
            return 100 / (n - 3);  // n=3일 때 0으로 나누기
          } catch (ArithmeticException e) {
            System.out.println("   예외 발생: " + n + " → 기본값 0 반환");
            return 0;
          }
        })
        .collect(Collectors.toList());
    
    System.out.println("   결과: " + safeResult);
  }

  /**
   * 5. 베스트 프랙티스
   */
  private static void demonstrateBestPractices() {
    System.out.println("\n5️⃣  디버깅 베스트 프랙티스\n");

    System.out.println("📌 언제 어떤 방법?");
    System.out.println();
    System.out.println("   ┌─────────────────────────┬─────────────────────────┐");
    System.out.println("   │      상황                │      방법                │");
    System.out.println("   ├─────────────────────────┼─────────────────────────┤");
    System.out.println("   │ 스택 트레이스 불명확    │ 메서드 참조              │");
    System.out.println("   │ 중간 값 확인 필요       │ peek                     │");
    System.out.println("   │ 복잡한 람다             │ 메서드 분리              │");
    System.out.println("   │ 예외 처리 필요          │ try-catch 래핑           │");
    System.out.println("   │ 체인 디버깅 어려움      │ 단계별 분리              │");
    System.out.println("   └─────────────────────────┴─────────────────────────┘");

    System.out.println("\n📌 체크리스트:");
    System.out.println("   ☐ 복잡한 람다는 메서드로 분리했는가?");
    System.out.println("   ☐ 메서드 참조를 사용할 수 있는가?");
    System.out.println("   ☐ peek로 중간 값을 확인했는가?");
    System.out.println("   ☐ 예외 처리가 적절한가?");
    System.out.println("   ☐ 단계별로 나눠서 디버깅했는가?");

    System.out.println("\n📌 프로덕션 코드 권장사항:");
    System.out.println("   1. 메서드 참조 > 람다");
    System.out.println("   2. 3줄+ 람다 → 메서드 분리");
    System.out.println("   3. peek는 개발 시에만 (프로덕션에서 제거)");
    System.out.println("   4. 적절한 예외 처리");
    System.out.println("   5. 의미 있는 메서드 이름");

    // 좋은 예 vs 나쁜 예
    System.out.println("\n📌 좋은 예 vs 나쁜 예:");
    
    System.out.println("\n   ❌ 나쁜 예:");
    System.out.println("   words.stream()");
    System.out.println("       .map(s -> {");
    System.out.println("           // 10줄의 복잡한 로직...");
    System.out.println("       })");
    System.out.println("       .filter(s -> { /* 복잡 */ })");
    
    System.out.println("\n   ✅ 좋은 예:");
    System.out.println("   words.stream()");
    System.out.println("       .map(MyClass::transform)      // 명확");
    System.out.println("       .filter(MyClass::isValid)     // 테스트 가능");
    System.out.println("       .collect(toList());");
  }

  // ========== 헬퍼 메서드 ==========

  /**
   * 0으로 나누기 (에러 발생용)
   */
  public static int divideByZero(int n) {
    return n / 0;  // ArithmeticException
  }

  // ========== 도메인 클래스 ==========

  /**
   * Point 클래스
   */
  static class Point {
    private final int x;
    private final int y;

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

    @Override
    public String toString() {
      return "(" + x + "," + y + ")";
    }
  }

  /**
   * 디버깅 패턴 정리:
   * 
   * 1. 스택 트레이스 읽기
   *    lambda$main$0 → main의 첫 번째 람다
   *    lambda$main$1 → main의 두 번째 람다
   * 
   * 2. 메서드 참조 장점
   *    명확한 메서드 이름 표시
   * 
   * 3. peek 활용
   *    중간 연산 로깅
   *    데이터 흐름 추적
   * 
   * 4. 디버깅 전략
   *    - 메서드 참조 사용
   *    - peek로 중간 값 확인
   *    - 단계별 분리
   *    - try-catch 래핑
   * 
   * 5. 프로덕션 권장
   *    - 메서드 참조 > 람다
   *    - 복잡한 람다 → 메서드
   *    - peek 제거 (개발용만)
   *    - 예외 처리
   * 
   * IDE 도구:
   * - IntelliJ: Stream Trace
   * - 브레이크포인트 (람다 내부 가능)
   * - Evaluate Expression
   */
}
