package chapter05.code;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 스트림 실전 연습 - 거래자와 거래 데이터 처리
 * 
 * 시나리오:
 * - 거래자(Trader): 이름, 도시
 * - 거래(Transaction): 거래자, 연도, 금액
 * 
 * 학습 목표:
 * 모든 스트림 연산을 활용한 실전 쿼리 작성
 * 
 * @author Modern Java In Action
 */
public class PuttingIntoPractice {

  public static void main(String... args) {
    
    // 거래자 생성
    Trader raoul = new Trader("Raoul", "Cambridge");
    Trader mario = new Trader("Mario", "Milan");
    Trader alan = new Trader("Alan", "Cambridge");
    Trader brian = new Trader("Brian", "Cambridge");

    // 거래 내역
    List<Transaction> transactions = Arrays.asList(
        new Transaction(brian, 2011, 300),
        new Transaction(raoul, 2012, 1000),
        new Transaction(raoul, 2011, 400),
        new Transaction(mario, 2012, 710),
        new Transaction(mario, 2012, 700),
        new Transaction(alan, 2012, 950)
    );

    // 질의 1: 2011년 거래 찾아서 금액 오름차순 정렬
    System.out.println("=== Query 1: 2011년 거래 (금액순) ===");
    List<Transaction> tr2011 = transactions.stream()
        .filter(transaction -> transaction.getYear() == 2011)
        .sorted(comparing(Transaction::getValue))
        .collect(toList());
    tr2011.forEach(System.out::println);
    System.out.println();

    // 질의 2: 거래자가 근무하는 모든 도시 (중복 제거)
    System.out.println("=== Query 2: 거래자 근무 도시 ===");
    List<String> cities = transactions.stream()
        .map(transaction -> transaction.getTrader().getCity())
        .distinct()
        .collect(toList());
    System.out.println(cities);
    System.out.println();

    // 질의 3: Cambridge 거래자 찾아서 이름순 정렬
    System.out.println("=== Query 3: Cambridge 거래자 ===");
    List<Trader> traders = transactions.stream()
        .map(Transaction::getTrader)
        .filter(trader -> trader.getCity().equals("Cambridge"))
        .distinct()
        .sorted(comparing(Trader::getName))
        .collect(toList());
    traders.forEach(System.out::println);
    System.out.println();

    // 질의 4: 모든 거래자 이름을 알파벳순 정렬하여 문자열로
    System.out.println("=== Query 4: 모든 거래자 이름 ===");
    
    // 방법 1: reduce
    String traderStr = transactions.stream()
        .map(transaction -> transaction.getTrader().getName())
        .distinct()
        .sorted()
        .reduce("", (n1, n2) -> n1 + n2);
    System.out.println("reduce: " + traderStr);
    
    // 방법 2: joining (더 효율적)
    String traderStr2 = transactions.stream()
        .map(transaction -> transaction.getTrader().getName())
        .distinct()
        .sorted()
        .collect(joining(", "));
    System.out.println("joining: " + traderStr2);
    System.out.println();

    // 질의 5: Milan에 거래자가 있는가?
    System.out.println("=== Query 5: Milan 거래자 존재? ===");
    boolean milanBased = transactions.stream()
        .anyMatch(transaction -> transaction.getTrader().getCity().equals("Milan"));
    System.out.println(milanBased ? "있음" : "없음");
    System.out.println();

    // 질의 6: Cambridge 거래자의 모든 거래 금액 합계
    System.out.println("=== Query 6: Cambridge 거래 금액 합계 ===");
    int sum = transactions.stream()
        .filter(t -> "Cambridge".equals(t.getTrader().getCity()))
        .mapToInt(Transaction::getValue)
        .sum();
    System.out.println(sum);
    System.out.println();

    // 질의 7: 전체 거래 중 최대 금액
    System.out.println("=== Query 7: 최대 거래 금액 ===");
    int highestValue = transactions.stream()
        .mapToInt(Transaction::getValue)
        .reduce(0, Integer::max);
    System.out.println(highestValue);
    System.out.println();

    // 질의 8: 전체 거래 중 최소 금액 거래
    System.out.println("=== Query 8: 최소 거래 금액 ===");
    Optional<Transaction> smallestTransaction = transactions.stream()
        .min(comparing(Transaction::getValue));
    System.out.println(smallestTransaction.map(String::valueOf).orElse("No transactions found"));
  }
}
