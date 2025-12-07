package chapter07.code;

import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

/**
 * 병렬 스트림 성능 측정 도구
 * 
 * 각 합계 메서드의 성능을 측정하고 비교하는 하네스(Harness)
 * 
 * 실행 방법:
 * - 10번 반복 실행
 * - 가장 빠른 시간 기록
 * - 워밍업 효과 포함
 */
public class ParallelStreamsHarness {

  /**
   * ForkJoinPool 인스턴스
   * 
   * 용도:
   * - Fork/Join 프레임워크 실행
   * - 병렬 스트림의 기본 풀
   * - 커스텀 병렬 처리
   */
  public static final ForkJoinPool FORK_JOIN_POOL = new ForkJoinPool();

  public static void main(String[] args) {
    System.out.println("=".repeat(80));
    System.out.println("병렬 스트림 성능 비교 (N = 10,000,000)");
    System.out.println("=".repeat(80));
    
    // 1. 전통적 for 루프 (기준)
    System.out.println("\n1️⃣  Iterative Sum (for loop):");
    long iterativeTime = measurePerf(ParallelStreams::iterativeSum, 10_000_000L);
    System.out.println("   ⏱️  Best: " + iterativeTime + " msecs");
    
    // 2. Stream.iterate 순차
    System.out.println("\n2️⃣  Sequential Sum (Stream.iterate):");
    long sequentialTime = measurePerf(ParallelStreams::sequentialSum, 10_000_000L);
    System.out.println("   ⏱️  Best: " + sequentialTime + " msecs");
    System.out.println("   📊 vs iterative: " + String.format("%.1fx slower", (double)sequentialTime / iterativeTime));
    
    // 3. Stream.iterate 병렬 (비효율!)
    System.out.println("\n3️⃣  Parallel Sum (Stream.iterate + parallel) ❌:");
    long parallelTime = measurePerf(ParallelStreams::parallelSum, 10_000_000L);
    System.out.println("   ⏱️  Best: " + parallelTime + " msecs");
    System.out.println("   📊 vs sequential: " + String.format("%.1fx slower!", (double)parallelTime / sequentialTime));
    
    // 4. LongStream.range 순차
    System.out.println("\n4️⃣  Ranged Sum (LongStream.rangeClosed):");
    long rangedTime = measurePerf(ParallelStreams::rangedSum, 10_000_000L);
    System.out.println("   ⏱️  Best: " + rangedTime + " msecs");
    System.out.println("   📊 vs iterative: " + String.format("%.1fx", (double)rangedTime / iterativeTime));
    
    // 5. LongStream.range 병렬 (효율적!)
    System.out.println("\n5️⃣  Parallel Ranged Sum (LongStream + parallel) ✅:");
    long parallelRangedTime = measurePerf(ParallelStreams::parallelRangedSum, 10_000_000L);
    System.out.println("   ⏱️  Best: " + parallelRangedTime + " msecs");
    System.out.println("   📊 vs iterative: " + String.format("%.1fx faster!", (double)iterativeTime / parallelRangedTime));
    System.out.println("   🏆 Speedup: " + String.format("%.1fx", (double)rangedTime / parallelRangedTime));
    
    // 6. Fork/Join 프레임워크
    System.out.println("\n6️⃣  ForkJoin Sum (RecursiveTask):");
    long forkJoinTime = measurePerf(ForkJoinSumCalculator::forkJoinSum, 10_000_000L);
    System.out.println("   ⏱️  Best: " + forkJoinTime + " msecs");
    System.out.println("   📊 vs iterative: " + String.format("%.1fx", (double)forkJoinTime / iterativeTime));
    
    // 7. 공유 가변 상태 순차
    System.out.println("\n7️⃣  Side Effect Sum (순차):");
    long sideEffectTime = measurePerf(ParallelStreams::sideEffectSum, 10_000_000L);
    System.out.println("   ⏱️  Best: " + sideEffectTime + " msecs");
    
    // 8. 공유 가변 상태 병렬 (위험!)
    System.out.println("\n8️⃣  Side Effect Parallel Sum ⚠️  (Race Condition!):");
    long sideEffectParallelTime = measurePerf(ParallelStreams::sideEffectParallelSum, 10_000_000L);
    System.out.println("   ⏱️  Best: " + sideEffectParallelTime + " msecs");
    System.out.println("   ⚠️  결과가 매번 다름! (Race Condition)");
    
    // 요약
    System.out.println("\n" + "=".repeat(80));
    System.out.println("📊 성능 요약 (빠른 순서):");
    System.out.println("=".repeat(80));
    System.out.println("1️⃣  Parallel Ranged Sum:      " + parallelRangedTime + " ms ⭐⭐⭐⭐⭐");
    System.out.println("2️⃣  ForkJoin Sum:              " + forkJoinTime + " ms ⭐⭐⭐⭐⭐");
    System.out.println("3️⃣  Iterative Sum:             " + iterativeTime + " ms ⭐⭐⭐⭐⭐");
    System.out.println("4️⃣  Ranged Sum:                " + rangedTime + " ms ⭐⭐⭐⭐");
    System.out.println("5️⃣  Sequential Sum:            " + sequentialTime + " ms ⭐");
    System.out.println("6️⃣  Parallel Sum (iterate):    " + parallelTime + " ms ❌");
    System.out.println("\n💡 결론: LongStream + parallel이 가장 빠름!");
    System.out.println("=".repeat(80));
  }

  /**
   * 성능 측정 메서드
   * 
   * 측정 방식:
   * 1. 10번 반복 실행
   * 2. 각 실행 시간 측정
   * 3. 가장 빠른 시간 반환
   * 4. 결과 출력 (검증용)
   * 
   * 왜 10번?
   * - JVM 워밍업 효과 포함
   * - JIT 컴파일 최적화 고려
   * - 일시적 지연 배제
   * 
   * 왜 최솟값?
   * - 최상의 성능 측정
   * - GC 영향 최소화
   * - 시스템 노이즈 배제
   * 
   * @param f 측정할 함수
   * @param input 입력값
   * @return 가장 빠른 실행 시간 (밀리초)
   */
  public static <T, R> long measurePerf(Function<T, R> f, T input) {
    long fastest = Long.MAX_VALUE;
    
    for (int i = 0; i < 10; i++) {
      long start = System.nanoTime();  // 시작 시간 (나노초)
      
      R result = f.apply(input);       // 함수 실행
      
      long duration = (System.nanoTime() - start) / 1_000_000;  // 밀리초 변환
      
      // 결과 출력 (정확성 검증)
      System.out.println("   🔄 Run " + (i + 1) + ": " + duration + " ms (result: " + result + ")");
      
      // 최솟값 갱신
      if (duration < fastest) {
        fastest = duration;
      }
    }
    
    return fastest;
  }

  /**
   * 사용 예제:
   * 
   * // 커스텀 함수 측정
   * long time = measurePerf(n -> {
   *   return LongStream.rangeClosed(1, n)
   *       .parallel()
   *       .sum();
   * }, 10_000_000L);
   * 
   * System.out.println("Time: " + time + " ms");
   */

}
