package chapter07.code;

import static chapter07.code.ParallelStreamsHarness.FORK_JOIN_POOL;

import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.LongStream;

/**
 * Fork/Join 프레임워크를 사용한 합계 계산
 * 
 * RecursiveTask를 상속하여 분할 정복(Divide and Conquer) 구현
 * 
 * 동작 원리:
 * 1. 작업이 충분히 작으면 순차 처리
 * 2. 그렇지 않으면 둘로 분할
 * 3. 왼쪽은 fork (비동기)
 * 4. 오른쪽은 compute (동기, 현재 스레드)
 * 5. join으로 왼쪽 결과 대기
 * 6. 두 결과 병합
 */
public class ForkJoinSumCalculator extends RecursiveTask<Long> {

  /**
   * 임계값 (THRESHOLD)
   * 
   * 의미:
   * - 이 크기 이하면 순차 처리
   * - 이 크기 초과면 분할
   * 
   * 10,000을 선택한 이유:
   * - CPU 집약적 작업에 적합
   * - 너무 작으면: 과도한 분할 (오버헤드)
   * - 너무 크면: 불충분한 병렬화
   * 
   * 다른 작업의 임계값:
   * - 메모리 접근: 100,000
   * - 복잡한 연산: 5,000
   * - 간단한 연산: 50,000
   */
  public static final long THRESHOLD = 10_000;

  /**
   * 처리할 숫자 배열
   */
  private final long[] numbers;
  
  /**
   * 배열의 시작 인덱스 (포함)
   */
  private final int start;
  
  /**
   * 배열의 끝 인덱스 (제외)
   */
  private final int end;

  /**
   * 전체 배열을 처리하는 생성자
   * 
   * @param numbers 숫자 배열
   */
  public ForkJoinSumCalculator(long[] numbers) {
    this(numbers, 0, numbers.length);
  }

  /**
   * 부분 배열을 처리하는 생성자 (내부용)
   * 
   * @param numbers 숫자 배열
   * @param start 시작 인덱스 (포함)
   * @param end 끝 인덱스 (제외)
   */
  private ForkJoinSumCalculator(long[] numbers, int start, int end) {
    this.numbers = numbers;
    this.start = start;
    this.end = end;
  }

  /**
   * 핵심 계산 로직 (RecursiveTask 구현)
   * 
   * 분할 정복 패턴:
   * 1. Base Case: 작업이 충분히 작으면 순차 처리
   * 2. Recursive Case: 둘로 분할하고 재귀 호출
   * 
   * @return 부분 배열의 합
   */
  @Override
  protected Long compute() {
    int length = end - start;  // 현재 처리할 요소 개수
    
    // Base Case: 임계값 이하면 순차 처리
    if (length <= THRESHOLD) {
      return computeSequentially();
    }
    
    // Recursive Case: 분할
    
    // 1. 왼쪽 절반 작업 생성
    ForkJoinSumCalculator leftTask = 
        new ForkJoinSumCalculator(numbers, start, start + length / 2);
    
    // 2. 왼쪽 작업을 비동기로 실행 (다른 스레드에 맡김)
    leftTask.fork();
    // → ForkJoinPool의 작업 큐에 추가
    // → Work-Stealing 대상이 됨
    
    // 3. 오른쪽 절반 작업 생성
    ForkJoinSumCalculator rightTask = 
        new ForkJoinSumCalculator(numbers, start + length / 2, end);
    
    // 4. 오른쪽 작업을 동기로 실행 (현재 스레드가 직접 처리)
    Long rightResult = rightTask.compute();
    // 💡 핵심: 현재 스레드를 활용! (낭비 없음)
    
    // 5. 왼쪽 작업 완료 대기
    Long leftResult = leftTask.join();
    // → 왼쪽 작업이 끝날 때까지 블로킹
    // → Work-Stealing: 대기 중 다른 작업 가져올 수 있음
    
    // 6. 두 결과 병합
    return leftResult + rightResult;
  }

  /**
   * 순차 처리 (Base Case)
   * 
   * 임계값 이하의 작업을 처리
   * - 단순한 for 루프
   * - 매우 빠름 (JVM 최적화)
   * - 오버헤드 없음
   * 
   * @return 부분 배열의 합
   */
  private long computeSequentially() {
    long sum = 0;
    for (int i = start; i < end; i++) {
      sum += numbers[i];
    }
    return sum;
  }

  /**
   * 편의 메서드: Fork/Join으로 합계 계산
   * 
   * 사용 예제:
   * long sum = ForkJoinSumCalculator.forkJoinSum(10_000_000);
   * 
   * @param n 1부터 n까지의 합 계산
   * @return 합계
   */
  public static long forkJoinSum(long n) {
    // 1. 배열 생성
    long[] numbers = LongStream.rangeClosed(1, n).toArray();
    
    // 2. ForkJoinTask 생성
    ForkJoinTask<Long> task = new ForkJoinSumCalculator(numbers);
    
    // 3. ForkJoinPool에 제출하고 결과 대기
    return FORK_JOIN_POOL.invoke(task);
  }

  /**
   * 실행 흐름 예제 (N=1,000,000, THRESHOLD=10,000):
   * 
   * compute() [0, 1,000,000]  (Thread-1)
   *   ├─ fork: [0, 500,000]   (Thread-2에 맡김)
   *   └─ compute: [500,000, 1,000,000]  (Thread-1이 직접 처리)
   *       ├─ fork: [500,000, 750,000]   (Thread-3에 맡김)
   *       └─ compute: [750,000, 1,000,000]  (Thread-1이 계속)
   *           ├─ fork: [750,000, 875,000]
   *           └─ compute: [875,000, 1,000,000]
   *               ...
   * 
   * 최종 분할 수: ~100개
   * 사용 스레드: 7개 (8코어 기준)
   * Work-Stealing: 유휴 스레드가 다른 작업 가져감
   */

  /**
   * 주의사항:
   * 
   * ✅ 올바른 패턴:
   * leftTask.fork();                    // 비동기
   * rightResult = rightTask.compute();  // 동기
   * leftResult = leftTask.join();       // 대기
   * 
   * ❌ 잘못된 패턴:
   * leftTask.fork();
   * rightTask.fork();  // 현재 스레드 낭비!
   * 
   * ❌ 절대 금지:
   * FORK_JOIN_POOL.invoke(task);  // 데드락 위험!
   */

}
