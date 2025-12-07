package chapter07.code;

import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

/**
 * JMH (Java Microbenchmark Harness)를 사용한 병렬 스트림 벤치마크
 * 
 * JMH란?
 * - OpenJDK에서 제공하는 마이크로벤치마크 도구
 * - JVM 워밍업, JIT 컴파일 등을 고려한 정확한 측정
 * - 통계 분석 (평균, 표준편차, 신뢰구간)
 * 
 * 사용법:
 * 1. pom.xml에 JMH 의존성 추가
 * 2. mvn clean install
 * 3. java -jar target/benchmarks.jar
 */
@State(Scope.Thread)  // 각 스레드마다 별도 상태
@BenchmarkMode(Mode.AverageTime)  // 평균 시간 측정
@OutputTimeUnit(TimeUnit.MILLISECONDS)  // 밀리초 단위
@Fork(value = 2, jvmArgs = { "-Xms4G", "-Xmx4G" })  // 2번 실행, 힙 4GB 고정
@Measurement(iterations = 2)  // 측정 반복 2회
@Warmup(iterations = 3)  // 워밍업 3회
public class ParallelStreamBenchmark {

  /**
   * 테스트 데이터 크기
   * 
   * 1천만 개를 선택한 이유:
   * - 병렬화 효과 측정에 충분
   * - 실행 시간 적당 (수 ms ~ 수십 ms)
   * - GC 영향 최소화
   */
  private static final long N = 10_000_000L;

  /**
   * 1. 전통적 for 루프 (기준)
   * 
   * 특징:
   * - 가장 간단
   * - JVM 최적화 우수
   * - 오버헤드 최소
   * 
   * 예상 결과: ~3ms
   */
  @Benchmark
  public long iterativeSum() {
    long result = 0;
    for (long i = 1L; i <= N; i++) {
      result += i;
    }
    return result;
  }

  /**
   * 2. Stream.iterate 순차
   * 
   * 문제점:
   * - 박싱/언박싱 (Long)
   * - 순차 의존성
   * 
   * 예상 결과: ~50ms
   */
  @Benchmark
  public long sequentialSum() {
    return Stream.iterate(1L, i -> i + 1)
        .limit(N)
        .reduce(0L, Long::sum);
  }

  /**
   * 3. Stream.iterate 병렬 (비효율!)
   * 
   * 문제점:
   * - iterate는 병렬화 불가
   * - 스레드 오버헤드만 추가
   * - 순차보다 느림!
   * 
   * 예상 결과: ~80ms
   */
  @Benchmark
  public long parallelSum() {
    return Stream.iterate(1L, i -> i + 1)
        .limit(N)
        .parallel()
        .reduce(0L, Long::sum);
  }

  /**
   * 4. LongStream.range 순차
   * 
   * 개선점:
   * - 기본형 long
   * - 효율적 분할 가능
   * 
   * 예상 결과: ~5ms
   */
  @Benchmark
  public long rangedSum() {
    return LongStream.rangeClosed(1, N)
        .reduce(0L, Long::sum);
  }

  /**
   * 5. LongStream.range 병렬 (효율적!)
   * 
   * 장점:
   * - 기본형 long
   * - O(1) 분할
   * - Work-Stealing
   * 
   * 예상 결과: ~1ms
   * 
   * 최고 성능! ⭐⭐⭐⭐⭐
   */
  @Benchmark
  public long parallelRangedSum() {
    return LongStream.rangeClosed(1, N)
        .parallel()
        .reduce(0L, Long::sum);
  }

  /**
   * 각 반복 후 GC 실행
   * 
   * 목적:
   * - 이전 테스트의 GC 영향 제거
   * - 일관된 힙 상태 유지
   * - 측정 정확도 향상
   */
  @TearDown(Level.Invocation)
  public void tearDown() {
    System.gc();
  }

  /**
   * 실행 방법:
   * 
   * 1. pom.xml에 추가:
   * <dependency>
   *   <groupId>org.openjdk.jmh</groupId>
   *   <artifactId>jmh-core</artifactId>
   *   <version>1.37</version>
   * </dependency>
   * <dependency>
   *   <groupId>org.openjdk.jmh</groupId>
   *   <artifactId>jmh-generator-annprocess</artifactId>
   *   <version>1.37</version>
   * </dependency>
   * 
   * 2. 빌드:
   * mvn clean install
   * 
   * 3. 실행:
   * java -jar target/benchmarks.jar
   * 
   * 4. 결과 저장:
   * java -jar target/benchmarks.jar -rf json -rff results.json
   */

  /**
   * 예상 결과 (8코어 기준):
   * 
   * Benchmark                              Mode  Cnt  Score   Error  Units
   * ParallelStreamBenchmark.iterativeSum   avgt    4  3.214 ± 0.124  ms/op
   * ParallelStreamBenchmark.sequentialSum  avgt    4 50.342 ± 2.431  ms/op
   * ParallelStreamBenchmark.parallelSum    avgt    4 79.821 ± 4.123  ms/op
   * ParallelStreamBenchmark.rangedSum      avgt    4  5.123 ± 0.234  ms/op
   * ParallelStreamBenchmark.parallelRanged avgt    4  0.987 ± 0.051  ms/op
   * 
   * 분석:
   * - iterativeSum: 기준 (100%)
   * - sequentialSum: 15배 느림 (박싱)
   * - parallelSum: 25배 느림! (비효율)
   * - rangedSum: 1.6배 느림 (기본형)
   * - parallelRangedSum: 3배 빠름! ⭐
   */

  /**
   * JMH 어노테이션 설명:
   * 
   * @State(Scope.Thread)
   * - 각 스레드마다 별도 인스턴스
   * - 스레드 간 상태 공유 없음
   * 
   * @BenchmarkMode(Mode.AverageTime)
   * - 평균 시간 측정
   * - 다른 모드: Throughput, SampleTime, SingleShotTime
   * 
   * @OutputTimeUnit(TimeUnit.MILLISECONDS)
   * - 결과를 밀리초로 출력
   * - 다른 단위: NANOSECONDS, MICROSECONDS, SECONDS
   * 
   * @Fork(value = 2, jvmArgs = {...})
   * - JVM을 2번 재시작하여 실행
   * - jvmArgs: JVM 옵션 설정
   * - 힙 크기 고정으로 일관성 확보
   * 
   * @Warmup(iterations = 3)
   * - 측정 전 3번 워밍업
   * - JIT 컴파일 최적화 유도
   * - 첫 실행의 느린 속도 배제
   * 
   * @Measurement(iterations = 2)
   * - 실제 측정 2회
   * - 평균값 계산에 사용
   * 
   * @TearDown(Level.Invocation)
   * - 각 벤치마크 메서드 호출 후 실행
   * - Level.Iteration: 반복마다
   * - Level.Trial: 전체 종료 시
   */

  /**
   * 추가 벤치마크 아이디어:
   * 
   * 1. 다양한 데이터 크기:
   * @Param({"100", "1000", "10000", "100000", "1000000"})
   * int size;
   * 
   * 2. 다양한 연산:
   * - 단순 합계 vs 복잡한 계산
   * - filter vs map vs flatMap
   * 
   * 3. 다양한 자료구조:
   * - ArrayList vs LinkedList
   * - HashSet vs TreeSet
   * 
   * 4. 다양한 병렬도:
   * System.setProperty(
   *   "java.util.concurrent.ForkJoinPool.common.parallelism",
   *   "4"
   * );
   */

}
