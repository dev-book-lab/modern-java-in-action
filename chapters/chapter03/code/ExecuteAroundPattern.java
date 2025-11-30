package chapter03.code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * Execute Around Pattern (실행 어라운드 패턴)
 * 
 * 패턴 구조:
 *   초기화/준비 코드
 *       ↓
 *   실제 작업 (변경 가능) ← 동작 파라미터화!
 *       ↓
 *   정리/마무리 코드
 * 
 * 핵심 개념:
 * - 자원 처리 코드를 설정/정리 코드가 둘러싸는 형태
 * - 변하는 부분(실제 작업)을 람다로 전달
 * - try-with-resources로 자원 자동 관리
 * 
 * 학습 목표:
 * 1. 실행 어라운드 패턴 이해
 * 2. 동작 파라미터화로 유연성 확보
 * 3. 함수형 인터페이스 설계
 * 4. 람다를 통한 동작 전달
 * 
 * @author Modern Java In Action
 */
public class ExecuteAroundPattern {

  /**
   * 테스트용 샘플 데이터
   * 실제로는 파일에서 읽어오지만, 예제를 위해 문자열로 대체
   */
  private static final String SAMPLE_DATA = 
      "Modern Java In Action\n" +
      "Lambda Expressions\n" +
      "Functional Programming\n" +
      "Stream API";

  public static void main(String... args) throws IOException {
    System.out.println("=== 실행 어라운드 패턴 예제 ===\n");

    // ===== 1단계: 제한적인 메서드 =====
    System.out.println("1단계: 한 줄만 읽는 제한적인 메서드");
    String result = processFileLimited();
    System.out.println("결과: " + result);
    System.out.println();

    // ===== 2단계: 동작 파라미터화 - 한 줄 읽기 =====
    System.out.println("2단계: 람다로 한 줄 읽기");
    String oneLine = processFile((BufferedReader br) -> br.readLine());
    System.out.println("결과: " + oneLine);
    System.out.println();

    // ===== 3단계: 동작 파라미터화 - 두 줄 읽기 =====
    System.out.println("3단계: 람다로 두 줄 읽기");
    String twoLines = processFile((BufferedReader br) -> 
        br.readLine() + "\n" + br.readLine());
    System.out.println("결과:\n" + twoLines);
    System.out.println();

    // ===== 4단계: 더 복잡한 동작 - 모든 줄 읽기 =====
    System.out.println("4단계: 람다로 모든 줄 읽기");
    String allLines = processFile((BufferedReader br) -> {
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line).append("\n");
      }
      return sb.toString();
    });
    System.out.println("결과:\n" + allLines);
    System.out.println();

    // ===== 5단계: 줄 개수 세기 =====
    System.out.println("5단계: 람다로 줄 개수 세기");
    Integer lineCount = processFileForCount((BufferedReader br) -> {
      int count = 0;
      while (br.readLine() != null) {
        count++;
      }
      return count;
    });
    System.out.println("총 줄 수: " + lineCount);
  }

  /**
   * 1단계: 제한적인 메서드
   * 
   * 문제점:
   * - 한 줄만 읽을 수 있음
   * - 다른 동작을 하려면 메서드를 새로 만들어야 함
   * - 코드 중복 발생
   * 
   * try-with-resources:
   * - try (리소스 선언) { ... }
   * - try 블록 종료 시 자동으로 close() 호출
   * - AutoCloseable 인터페이스 구현 객체에 사용 가능
   * 
   * @return 파일의 첫 번째 줄
   * @throws IOException 파일 읽기 실패 시
   */
  public static String processFileLimited() throws IOException {
    // try-with-resources: BufferedReader 자동 close
    try (BufferedReader br = new BufferedReader(
            new StringReader(SAMPLE_DATA))) {
      
      return br.readLine();  // 한 줄만 읽기 (제한적!)
      
      // try 블록 종료 시 자동으로 br.close() 호출됨
    }
  }

  /**
   * 2-4단계: 동작 파라미터화된 메서드
   * 
   * 개선점:
   * - BufferedReader를 어떻게 처리할지 람다로 전달받음
   * - 한 줄, 두 줄, 모든 줄 등 다양한 동작 가능
   * - 코드 중복 제거
   * 
   * 패턴 구조:
   *   초기화: BufferedReader 생성
   *       ↓
   *   실제 작업: p.process(br) ← 람다 실행!
   *       ↓
   *   정리: br.close() (자동)
   * 
   * @param p BufferedReader를 처리하는 동작
   * @return 처리 결과 문자열
   * @throws IOException 파일 읽기 실패 시
   */
  public static String processFile(BufferedReaderProcessor p) 
      throws IOException {
    
    // 초기화: BufferedReader 생성
    try (BufferedReader br = new BufferedReader(
            new StringReader(SAMPLE_DATA))) {
      
      // 실제 작업: 람다 실행!
      return p.process(br);
      
      // 정리: br.close() 자동 호출
    }
  }

  /**
   * 5단계: 다른 타입 반환 (Integer)
   * 
   * 확장성:
   * - 반환 타입을 다르게 할 수도 있음
   * - 제네릭을 사용하면 더 범용적으로 만들 수 있음
   * 
   * @param p BufferedReader를 처리하는 동작
   * @return 처리 결과 정수
   * @throws IOException 파일 읽기 실패 시
   */
  public static Integer processFileForCount(
      BufferedReaderProcessorForInt p) throws IOException {
    
    try (BufferedReader br = new BufferedReader(
            new StringReader(SAMPLE_DATA))) {
      return p.process(br);
    }
  }

  /**
   * BufferedReaderProcessor 함수형 인터페이스
   * 
   * 역할:
   * - BufferedReader를 받아서 String을 반환하는 동작 정의
   * - 람다 표현식의 타입 역할
   * 
   * 함수 디스크립터:
   * - (BufferedReader) -> String
   * 
   * IOException:
   * - BufferedReader.readLine()이 IOException을 던질 수 있음
   * - 따라서 함수형 인터페이스도 throws 선언 필요
   * 
   * @FunctionalInterface:
   * - 함수형 인터페이스임을 명시
   * - 추상 메서드가 2개 이상이면 컴파일 에러
   */
  @FunctionalInterface
  public interface BufferedReaderProcessor {
    
    /**
     * BufferedReader를 처리하는 메서드
     * 
     * @param b 처리할 BufferedReader
     * @return 처리 결과 문자열
     * @throws IOException 읽기 실패 시
     */
    String process(BufferedReader b) throws IOException;
  }

  /**
   * Integer 반환용 함수형 인터페이스
   * 
   * 확장 예시:
   * - 반환 타입만 다른 함수형 인터페이스
   * - 제네릭을 사용하면 하나로 통합 가능:
   *   interface Processor<R> {
   *     R process(BufferedReader b) throws IOException;
   *   }
   */
  @FunctionalInterface
  public interface BufferedReaderProcessorForInt {
    Integer process(BufferedReader b) throws IOException;
  }

  /*
   * ===== 핵심 정리 =====
   * 
   * 실행 어라운드 패턴:
   * 1. 준비/정리 코드는 고정
   * 2. 실제 작업은 동작 파라미터화
   * 3. 람다로 다양한 동작 전달
   * 
   * 4단계 진화:
   * 1단계: 제한적인 메서드 (한 줄만)
   * 2단계: 동작 파라미터화 (람다 전달)
   * 3단계: 함수형 인터페이스 정의
   * 4단계: 다양한 동작 실행
   * 
   * 장점:
   * - 코드 중복 제거
   * - 유연성 증가
   * - 자원 관리 자동화
   * 
   * 실전 활용:
   * - 파일 읽기/쓰기
   * - DB 연결
   * - 트랜잭션 처리
   * - Lock 관리
   */
}
