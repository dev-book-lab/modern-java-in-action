package chapter07.code;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

/**
 * 커스텀 ForkJoinPool 사용 예제
 * 
 * 언제 필요한가?
 * - 블로킹 I/O 작업 (네트워크, 파일)
 * - commonPool 격리 필요
 * - 특정 병렬도 설정
 */
public class CustomThreadPool {

  public static void main(String[] args) throws Exception {
    List<String> urls = Arrays.asList(
        "http://example.com/1",
        "http://example.com/2",
        "http://example.com/3",
        "http://example.com/4",
        "http://example.com/5"
    );

    System.out.println("=".repeat(80));
    System.out.println("커스텀 ForkJoinPool 예제");
    System.out.println("=".repeat(80));

    // ❌ commonPool 사용 (문제)
    System.out.println("\n1️⃣  commonPool 사용 (문제):");
    System.out.println("   - 블로킹 작업이 모든 스레드 점유");
    System.out.println("   - 다른 병렬 스트림도 영향 받음");
    
    long start1 = System.nanoTime();
    List<String> results1 = urls.parallelStream()
        .map(CustomThreadPool::downloadContent)  // 블로킹 I/O
        .collect(Collectors.toList());
    long duration1 = (System.nanoTime() - start1) / 1_000_000;
    
    System.out.println("   ⏱️  시간: " + duration1 + " ms");
    System.out.println("   📊 commonPool 사용률: 100% (문제!)");

    // ✅ 커스텀 풀 사용 (해결)
    System.out.println("\n2️⃣  커스텀 풀 사용 (해결):");
    System.out.println("   - 전용 스레드 풀 (20개)");
    System.out.println("   - commonPool 영향 없음");
    
    ForkJoinPool customPool = new ForkJoinPool(20);
    
    long start2 = System.nanoTime();
    List<String> results2 = customPool.submit(() ->
        urls.parallelStream()
            .map(CustomThreadPool::downloadContent)
            .collect(Collectors.toList())
    ).get();
    long duration2 = (System.nanoTime() - start2) / 1_000_000;
    
    System.out.println("   ⏱️  시간: " + duration2 + " ms");
    System.out.println("   📊 commonPool 사용률: 0% (격리 성공!)");
    
    customPool.shutdown();

    System.out.println("\n" + "=".repeat(80));
    System.out.println("💡 결론:");
    System.out.println("   - 블로킹 작업: 커스텀 풀 필수");
    System.out.println("   - CPU 작업: commonPool 사용");
    System.out.println("=".repeat(80));
  }

  /**
   * 블로킹 I/O 시뮬레이션 (1초 대기)
   */
  private static String downloadContent(String url) {
    try {
      System.out.println("   [" + Thread.currentThread().getName() + "] " + url);
      Thread.sleep(1000);  // 블로킹
      return "Content from " + url;
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 커스텀 풀 설정 가이드:
   * 
   * CPU 집약적:
   * - 병렬도 = CPU 코어 수
   * - ForkJoinPool pool = new ForkJoinPool();
   * 
   * I/O 블로킹:
   * - 병렬도 = 동시 I/O 연결 수
   * - ForkJoinPool pool = new ForkJoinPool(20);
   * 
   * 혼합:
   * - 병렬도 = 코어 수 * (1 + 대기시간/실행시간)
   * - 예: 8코어 * (1 + 100ms/10ms) = 88
   */

}
