package chapter07.code;

import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * 병렬 스트림 예제
 * 
 * 다양한 방식의 합계 연산을 비교:
 * 1. iterativeSum: 전통적 for 루프
 * 2. sequentialSum: Stream.iterate 순차
 * 3. parallelSum: Stream.iterate 병렬 (비효율!)
 * 4. rangedSum: LongStream.range 순차
 * 5. parallelRangedSum: LongStream.range 병렬 (효율적!)
 * 6. sideEffectSum: 공유 가변 상태 순차
 * 7. sideEffectParallelSum: 공유 가변 상태 병렬 (위험!)
 */
public class ParallelStreams {

  /**
   * 1. 전통적인 반복문을 사용한 합계 (기준)
   * 
   * 특징:
   * - 가장 간단하고 직관적
   * - JVM 최적화가 잘 됨 (루프 언롤링 등)
   * - 예상 성능: ~3ms (1천만 개)
   * 
   * @param n 합계를 구할 범위 (1부터 n까지)
   * @return 1부터 n까지의 합
   */
  public static long iterativeSum(long n) {
    long result = 0;
    for (long i = 0; i <= n; i++) {
      result += i;
    }
    return result;
  }

  /**
   * 2. Stream.iterate를 사용한 순차 합계
   * 
   * 문제점:
   * - Stream.iterate는 순차적 의존성 있음
   * - 박싱/언박싱 오버헤드 (Long 객체 생성)
   * - 예상 성능: ~50ms (1천만 개)
   * 
   * iterate의 동작:
   * 1 → (i -> i + 1) → 2 → (i -> i + 1) → 3 → ...
   * 각 단계가 이전 결과에 의존!
   * 
   * @param n 합계를 구할 범위
   * @return 1부터 n까지의 합
   */
  public static long sequentialSum(long n) {
    return Stream.iterate(1L, i -> i + 1)  // Long 객체 생성 (박싱)
        .limit(n)
        .reduce(Long::sum)                 // Optional<Long> 반환
        .get();                            // Long 추출
  }

  /**
   * 3. Stream.iterate를 병렬로 실행 (비효율!)
   * 
   * 문제점:
   * - iterate는 병렬화 불가능 (순차 의존성)
   * - parallel() 호출해도 효과 없음
   * - 오히려 스레드 오버헤드로 더 느림!
   * - 예상 성능: ~80ms (1천만 개)
   * 
   * 왜 느린가?
   * - 분할 불가능: 다음 값이 이전 값에 의존
   * - 스레드 생성 비용만 추가됨
   * - Work-Stealing도 효과 없음
   * 
   * ❌ 나쁜 예제! 교육용으로만 사용!
   * 
   * @param n 합계를 구할 범위
   * @return 1부터 n까지의 합
   */
  public static long parallelSum(long n) {
    return Stream.iterate(1L, i -> i + 1)
        .limit(n)
        .parallel()  // ❌ 효과 없음!
        .reduce(Long::sum)
        .get();
  }

  /**
   * 4. LongStream.range를 사용한 순차 합계
   * 
   * 개선점:
   * - 기본형 long 사용 (박싱/언박싱 없음)
   * - 범위가 미리 정해짐
   * - 예상 성능: ~5ms (1천만 개)
   * 
   * LongStream의 장점:
   * - long 직접 처리 (Long 객체 생성 안 함)
   * - 메모리 효율: 8 bytes vs 24 bytes
   * - 캐시 친화적
   * 
   * @param n 합계를 구할 범위
   * @return 1부터 n까지의 합
   */
  public static long rangedSum(long n) {
    return LongStream.rangeClosed(1, n)  // 기본형 long
        .reduce(Long::sum)                // OptionalLong 반환
        .getAsLong();                     // long 추출 (언박싱 없음)
  }

  /**
   * 5. LongStream.range를 병렬로 실행 (효율적!)
   * 
   * 장점:
   * - 효율적인 분할 가능 (O(1))
   * - 박싱/언박싱 없음
   * - Work-Stealing 효과적
   * - 예상 성능: ~1ms (1천만 개)
   * 
   * 분할 과정:
   * [1...10,000,000]
   *   ↓ 분할
   * [1...5,000,000] | [5,000,001...10,000,000]
   *   ↓                    ↓
   * Thread-1            Thread-2
   * 
   * 최적화 요소:
   * 1. 기본형 연산
   * 2. 균등 분할
   * 3. 캐시 효율
   * 4. Work-Stealing
   * 
   * ✅ 병렬 스트림의 올바른 사용 예제!
   * 
   * @param n 합계를 구할 범위
   * @return 1부터 n까지의 합
   */
  public static long parallelRangedSum(long n) {
    return LongStream.rangeClosed(1, n)
        .parallel()  // ✅ 효율적!
        .reduce(Long::sum)
        .getAsLong();
  }

  /**
   * 6. 공유 가변 상태를 사용한 순차 합계
   * 
   * 문제점:
   * - 함수형 프로그래밍 원칙 위배
   * - 부작용(side effect) 발생
   * - 하지만 순차 실행은 정상 동작
   * 
   * 동작 과정:
   * forEach(1) → accumulator.total = 1
   * forEach(2) → accumulator.total = 3
   * forEach(3) → accumulator.total = 6
   * ...
   * 
   * @param n 합계를 구할 범위
   * @return 1부터 n까지의 합
   */
  public static long sideEffectSum(long n) {
    Accumulator accumulator = new Accumulator();
    LongStream.rangeClosed(1, n)
        .forEach(accumulator::add);  // 순차 실행
    return accumulator.total;
  }

  /**
   * 7. 공유 가변 상태를 병렬로 접근 (위험!)
   * 
   * 치명적 문제:
   * - Race Condition 발생
   * - 결과가 매번 다름 (비결정적)
   * - 데이터 손실 발생
   * 
   * Race Condition 시나리오:
   * 
   * Thread-1: total 읽기 (100)
   * Thread-2: total 읽기 (100)  ← 같은 값!
   * Thread-1: 100 + 50 = 150 계산
   * Thread-2: 100 + 30 = 130 계산
   * Thread-1: total 쓰기 (150)
   * Thread-2: total 쓰기 (130)  ← 덮어씀!
   * 
   * 결과: 30이 손실됨!
   * 
   * 실행 결과:
   * 실행1: 487,623 ❌
   * 실행2: 492,138 ❌
   * 실행3: 495,721 ❌
   * 예상: 500,500 (실제와 다름!)
   * 
   * ❌ 절대 사용하면 안 되는 안티패턴!
   * 
   * 해결 방법:
   * 1. reduce 사용 (불변 연산)
   * 2. AtomicLong 사용 (느림)
   * 3. synchronized (느림)
   * → reduce가 최선!
   * 
   * @param n 합계를 구할 범위
   * @return 1부터 n까지의 합 (부정확!)
   */
  public static long sideEffectParallelSum(long n) {
    Accumulator accumulator = new Accumulator();
    LongStream.rangeClosed(1, n)
        .parallel()  // ❌ Race Condition!
        .forEach(accumulator::add);
    return accumulator.total;  // 틀린 결과!
  }

  /**
   * 공유 가변 상태를 가진 누산기 (안티패턴!)
   * 
   * 문제점:
   * - 가변 상태 (total)
   * - 원자적 연산 아님 (total += value)
   * - 스레드 안전하지 않음
   * 
   * total += value는 3단계:
   * 1. total 읽기 (READ)
   * 2. value 더하기 (COMPUTE)
   * 3. total 쓰기 (WRITE)
   * 
   * → 3단계 사이에 다른 스레드가 끼어들 수 있음!
   */
  public static class Accumulator {
    private long total = 0;  // ⚠️ 가변 상태!

    /**
     * 값을 누적 (스레드 안전하지 않음!)
     */
    public void add(long value) {
      total += value;  // ⚠️ 원자적 연산 아님!
    }
  }

  /**
   * 성능 비교 요약 (1천만 개 기준):
   * 
   * 1. iterativeSum:           ~3ms   ⭐⭐⭐⭐⭐ (기준)
   * 2. sequentialSum:          ~50ms  ⭐      (박싱)
   * 3. parallelSum:            ~80ms  ❌      (더 느림!)
   * 4. rangedSum:              ~5ms   ⭐⭐⭐⭐  (기본형)
   * 5. parallelRangedSum:      ~1ms   ⭐⭐⭐⭐⭐ (최고!)
   * 6. sideEffectSum:          ~5ms   ⭐⭐⭐⭐  (정확)
   * 7. sideEffectParallelSum:  ~2ms   ❌      (부정확!)
   * 
   * 결론:
   * ✅ 병렬 스트림: LongStream + parallel
   * ❌ 피해야 할 것: iterate + parallel, 공유 가변 상태
   */

}
