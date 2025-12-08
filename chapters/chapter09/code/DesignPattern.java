package chapter09.code;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * 디자인 패턴 람다 리팩터링 예제
 * 
 * 학습 목표:
 * - 전략 패턴 → 람다
 * - 템플릿 메서드 패턴 → Consumer
 * - 옵저버 패턴 → 람다
 * - 의무 체인 패턴 → Function.andThen
 * - 팩토리 패턴 → Map + Supplier
 * 
 * 핵심 개념:
 * - 함수형 인터페이스 활용
 * - 클래스 vs 람다 선택 기준
 * - 실전 적용 사례
 */
public class DesignPattern {

  public static void main(String[] args) {
    System.out.println("=".repeat(80));
    System.out.println("디자인 패턴 람다 리팩터링 예제");
    System.out.println("=".repeat(80));

    demonstrateStrategy();
    demonstrateTemplateMethod();
    demonstrateObserver();
    demonstrateChainOfResponsibility();
    demonstrateFactory();

    System.out.println("\n" + "=".repeat(80));
    System.out.println("💡 핵심 정리:");
    System.out.println("   ✅ 간단한 패턴 → 람다");
    System.out.println("   ✅ 복잡한 패턴 → 클래스 유지");
    System.out.println("   ✅ 함수형 인터페이스 → 람다 가능");
    System.out.println("   ✅ 여러 메서드/상태 → 클래스 필요");
    System.out.println("=".repeat(80));
  }

  /**
   * 1. 전략 패턴 (Strategy Pattern)
   * 
   * Before: 전략 인터페이스 + 구현 클래스
   * After: 람다 표현식
   * 
   * 언제 람다 사용?
   * - 전략이 간단 (상태 없음)
   * - 일회성 사용
   */
  private static void demonstrateStrategy() {
    System.out.println("\n1️⃣  전략 패턴\n");

    // ❌ Before: 클래스 기반
    System.out.println("📌 Before - 클래스 기반:");
    Validator numericValidator = new Validator(new IsNumeric());
    System.out.println("   \"12345\" 숫자? " + numericValidator.validate("12345"));
    System.out.println("   \"abc\" 숫자? " + numericValidator.validate("abc"));

    Validator lowerCaseValidator = new Validator(new IsAllLowerCase());
    System.out.println("   \"hello\" 소문자? " + lowerCaseValidator.validate("hello"));
    System.out.println("   \"Hello\" 소문자? " + lowerCaseValidator.validate("Hello"));

    // ✅ After: 람다
    System.out.println("\n📌 After - 람다:");
    Validator numericValidatorLambda = new Validator(s -> s.matches("\\d+"));
    System.out.println("   \"12345\" 숫자? " + numericValidatorLambda.validate("12345"));

    Validator lowerCaseValidatorLambda = new Validator(s -> s.matches("[a-z]+"));
    System.out.println("   \"hello\" 소문자? " + lowerCaseValidatorLambda.validate("hello"));

    System.out.println("\n   → 클래스 불필요! 인라인으로 전략 전달");
  }

  /**
   * 2. 템플릿 메서드 패턴 (Template Method Pattern)
   * 
   * Before: 추상 클래스 + 상속
   * After: Consumer 파라미터
   * 
   * 장점:
   * - 상속 불필요
   * - 유연성 증가
   */
  private static void demonstrateTemplateMethod() {
    System.out.println("\n2️⃣  템플릿 메서드 패턴\n");

    // ❌ Before: 상속 기반
    System.out.println("📌 Before - 상속 기반:");
    OnlineBanking banking1 = new OnlineBankingImpl();
    banking1.processCustomer(1337);

    // ✅ After: Consumer 파라미터
    System.out.println("\n📌 After - Consumer 파라미터:");
    OnlineBankingLambda banking2 = new OnlineBankingLambda();
    banking2.processCustomer(1337, c -> 
        System.out.println("   안녕하세요, " + c.getName() + "님!")
    );

    // 다양한 동작 전달 가능
    System.out.println("\n📌 다양한 동작:");
    banking2.processCustomer(1337, c -> 
        System.out.println("   VIP 고객 " + c.getName() + "님 환영합니다!")
    );
    banking2.processCustomer(1337, c -> 
        System.out.println("   " + c.getName() + "님의 포인트: 1000P")
    );

    System.out.println("\n   → 상속 없이 동작 커스터마이징!");
  }

  /**
   * 3. 옵저버 패턴 (Observer Pattern)
   * 
   * Before: Observer 인터페이스 + 구현 클래스
   * After: 람다 표현식
   * 
   * 주의:
   * - 간단한 옵저버 → 람다
   * - 상태 있는 옵저버 → 클래스 유지
   */
  private static void demonstrateObserver() {
    System.out.println("\n3️⃣  옵저버 패턴\n");

    // ❌ Before: 클래스 기반
    System.out.println("📌 Before - 클래스 기반:");
    Feed feed1 = new Feed();
    feed1.registerObserver(new NYTimes());
    feed1.registerObserver(new Guardian());
    feed1.notifyObservers("The queen said her favourite book is Modern Java!");
    System.out.println();

    // ✅ After: 람다
    System.out.println("📌 After - 람다:");
    Feed feed2 = new Feed();
    
    feed2.registerObserver(tweet -> {
      if (tweet != null && tweet.contains("money")) {
        System.out.println("   [NYTimes] Breaking news in NY! " + tweet);
      }
    });
    
    feed2.registerObserver(tweet -> {
      if (tweet != null && tweet.contains("queen")) {
        System.out.println("   [Guardian] Yet another news in London... " + tweet);
      }
    });
    
    feed2.notifyObservers("Money money money, give me money!");
    System.out.println();

    // 상태 있는 옵저버 예제
    System.out.println("📌 상태 있는 옵저버 (클래스 유지 권장):");
    System.out.println("   class StatefulObserver implements Observer {");
    System.out.println("       private int notificationCount = 0;  // 상태!");
    System.out.println("       public void notify(String tweet) {");
    System.out.println("           notificationCount++;");
    System.out.println("       }");
    System.out.println("   }");
    System.out.println("   → 상태가 있으면 클래스 사용!");
  }

  /**
   * 4. 의무 체인 패턴 (Chain of Responsibility)
   * 
   * Before: ProcessingObject + setSuccessor
   * After: Function.andThen
   * 
   * 장점:
   * - 간결
   * - 함수 조합 자유
   */
  private static void demonstrateChainOfResponsibility() {
    System.out.println("\n4️⃣  의무 체인 패턴\n");

    String text = "Aren't labdas really sexy?!!";

    // ❌ Before: 클래스 기반
    System.out.println("📌 Before - 클래스 기반:");
    ProcessingObject<String> p1 = new HeaderTextProcessing();
    ProcessingObject<String> p2 = new SpellCheckerProcessing();
    p1.setSuccessor(p2);  // 체인 연결
    String result1 = p1.handle(text);
    System.out.println("   결과: " + result1);

    // ✅ After: Function.andThen
    System.out.println("\n📌 After - Function.andThen:");
    UnaryOperator<String> headerProcessing = 
        t -> "From Raoul, Mario and Alan: " + t;
    UnaryOperator<String> spellCheckerProcessing = 
        t -> t.replaceAll("labda", "lambda");
    
    // 함수 조합
    Function<String, String> pipeline = 
        headerProcessing.andThen(spellCheckerProcessing);
    
    String result2 = pipeline.apply(text);
    System.out.println("   결과: " + result2);

    // 복잡한 체인
    System.out.println("\n📌 복잡한 체인:");
    Function<String, String> complexPipeline = headerProcessing
        .andThen(spellCheckerProcessing)
        .andThen(String::toUpperCase)
        .andThen(s -> s + " [PROCESSED]");
    
    System.out.println("   결과: " + complexPipeline.apply(text));
  }

  /**
   * 5. 팩토리 패턴 (Factory Pattern)
   * 
   * Before: switch 문
   * After: Map<String, Supplier<Product>>
   * 
   * 장점:
   * - switch 불필요
   * - 확장 용이
   */
  private static void demonstrateFactory() {
    System.out.println("\n5️⃣  팩토리 패턴\n");

    // ❌ Before: switch 문
    System.out.println("📌 Before - switch 문:");
    Product loan1 = ProductFactory.createProduct("loan");
    System.out.println("   생성: " + loan1.getClass().getSimpleName());

    // ✅ After: Map + Supplier
    System.out.println("\n📌 After - Map + Supplier:");
    Product loan2 = ProductFactory.createProductLambda("loan");
    System.out.println("   생성: " + loan2.getClass().getSimpleName());
    
    Product stock = ProductFactory.createProductLambda("stock");
    System.out.println("   생성: " + stock.getClass().getSimpleName());

    // 장점
    System.out.println("\n📌 장점:");
    System.out.println("   ✅ switch 불필요");
    System.out.println("   ✅ 새 제품 추가: map.put(\"new\", New::new)");
    System.out.println("   ✅ 확장성");

    // 제한사항
    System.out.println("\n📌 제한사항:");
    System.out.println("   ⚠️ Supplier는 인수 없는 생성자만");
    System.out.println("   ⚠️ 인수 필요 시: TriFunction 또는 빌더 패턴");
  }

  // ========== 전략 패턴 클래스 ==========

  /**
   * 전략 인터페이스 (함수형 인터페이스)
   */
  @FunctionalInterface
  interface ValidationStrategy {
    boolean execute(String s);
  }

  /**
   * 구체 전략 1: 숫자 검증
   */
  static class IsNumeric implements ValidationStrategy {
    @Override
    public boolean execute(String s) {
      return s.matches("\\d+");
    }
  }

  /**
   * 구체 전략 2: 소문자 검증
   */
  static class IsAllLowerCase implements ValidationStrategy {
    @Override
    public boolean execute(String s) {
      return s.matches("[a-z]+");
    }
  }

  /**
   * 컨텍스트: 전략 사용
   */
  static class Validator {
    private final ValidationStrategy strategy;

    public Validator(ValidationStrategy strategy) {
      this.strategy = strategy;
    }

    public boolean validate(String s) {
      return strategy.execute(s);
    }
  }

  // ========== 템플릿 메서드 패턴 클래스 ==========

  /**
   * Before: 추상 클래스
   */
  static abstract class OnlineBanking {
    public void processCustomer(int id) {
      Customer c = Database.getCustomerWithId(id);
      makeCustomerHappy(c);  // 추상 메서드
    }

    abstract void makeCustomerHappy(Customer c);
  }

  /**
   * Before: 구현 클래스
   */
  static class OnlineBankingImpl extends OnlineBanking {
    @Override
    void makeCustomerHappy(Customer c) {
      System.out.println("   Hello, " + c.getName() + "!");
    }
  }

  /**
   * After: Consumer 파라미터
   */
  static class OnlineBankingLambda {
    public void processCustomer(int id, Consumer<Customer> makeCustomerHappy) {
      Customer c = Database.getCustomerWithId(id);
      makeCustomerHappy.accept(c);  // 람다 실행
    }
  }

  // ========== 옵저버 패턴 클래스 ==========

  /**
   * 옵저버 인터페이스 (함수형 인터페이스)
   */
  @FunctionalInterface
  interface Observer {
    void notify(String tweet);
  }

  /**
   * 주제 인터페이스
   */
  interface Subject {
    void registerObserver(Observer o);
    void notifyObservers(String tweet);
  }

  /**
   * 구체 옵저버 1: NYTimes
   */
  static class NYTimes implements Observer {
    @Override
    public void notify(String tweet) {
      if (tweet != null && tweet.contains("money")) {
        System.out.println("   [NYTimes] Breaking news in NY! " + tweet);
      }
    }
  }

  /**
   * 구체 옵저버 2: Guardian
   */
  static class Guardian implements Observer {
    @Override
    public void notify(String tweet) {
      if (tweet != null && tweet.contains("queen")) {
        System.out.println("   [Guardian] Yet another news in London... " + tweet);
      }
    }
  }

  /**
   * 주제 구현: Feed
   */
  static class Feed implements Subject {
    private final List<Observer> observers = new ArrayList<>();

    @Override
    public void registerObserver(Observer o) {
      observers.add(o);
    }

    @Override
    public void notifyObservers(String tweet) {
      observers.forEach(o -> o.notify(tweet));
    }
  }

  // ========== 의무 체인 패턴 클래스 ==========

  /**
   * 추상 처리 객체
   */
  static abstract class ProcessingObject<T> {
    protected ProcessingObject<T> successor;

    public void setSuccessor(ProcessingObject<T> successor) {
      this.successor = successor;
    }

    public T handle(T input) {
      T r = handleWork(input);
      if (successor != null) {
        return successor.handle(r);  // 다음 객체로 전달
      }
      return r;
    }

    protected abstract T handleWork(T input);
  }

  /**
   * 구체 처리 객체 1: 헤더 추가
   */
  static class HeaderTextProcessing extends ProcessingObject<String> {
    @Override
    protected String handleWork(String text) {
      return "From Raoul, Mario and Alan: " + text;
    }
  }

  /**
   * 구체 처리 객체 2: 맞춤법 검사
   */
  static class SpellCheckerProcessing extends ProcessingObject<String> {
    @Override
    protected String handleWork(String text) {
      return text.replaceAll("labda", "lambda");
    }
  }

  // ========== 팩토리 패턴 클래스 ==========

  /**
   * 제품 인터페이스
   */
  interface Product {}

  /**
   * 구체 제품들
   */
  static class Loan implements Product {}
  static class Stock implements Product {}
  static class Bond implements Product {}

  /**
   * 팩토리 클래스
   */
  static class ProductFactory {
    // Before: switch 문
    public static Product createProduct(String name) {
      switch (name) {
        case "loan": return new Loan();
        case "stock": return new Stock();
        case "bond": return new Bond();
        default: throw new IllegalArgumentException("No such product: " + name);
      }
    }

    // After: Map + Supplier
    private static final Map<String, Supplier<Product>> productMap = new HashMap<>();
    
    static {
      productMap.put("loan", Loan::new);
      productMap.put("stock", Stock::new);
      productMap.put("bond", Bond::new);
    }

    public static Product createProductLambda(String name) {
      Supplier<Product> supplier = productMap.get(name);
      if (supplier != null) {
        return supplier.get();
      }
      throw new IllegalArgumentException("No such product: " + name);
    }
  }

  // ========== 헬퍼 클래스 ==========

  /**
   * 고객 클래스
   */
  static class Customer {
    private final String name;

    public Customer(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  /**
   * 데이터베이스 (더미)
   */
  static class Database {
    public static Customer getCustomerWithId(int id) {
      return new Customer("Customer" + id);
    }
  }

  /**
   * 패턴별 선택 가이드:
   * 
   * ✅ 람다 사용:
   *    - 전략 패턴 (간단한 검증)
   *    - 템플릿 메서드 (간단한 동작)
   *    - 옵저버 (상태 없는 알림)
   *    - 의무 체인 (Function.andThen)
   *    - 팩토리 (Supplier로 생성)
   * 
   * ❌ 클래스 유지:
   *    - 복잡한 로직 (10줄+)
   *    - 상태 필요
   *    - 여러 메서드 구현
   *    - 재사용성 중요
   * 
   * 판단 기준:
   *    1. 함수형 인터페이스? → 람다 가능
   *    2. 간단? (1-5줄) → 람다
   *    3. 상태 없음? → 람다
   *    4. 일회성? → 람다
   *    5. 그 외 → 클래스
   */
}
