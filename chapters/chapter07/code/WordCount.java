package chapter07.code;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Spliterator를 사용한 단어 카운터
 * 
 * 목표:
 * - 문자열의 단어 개수를 병렬로 카운트
 * - 단어 경계(공백)에서만 분할
 * - 커스텀 Spliterator 구현
 * 
 * 문제:
 * - 기본 Spliterator는 임의 위치에서 분할
 * - 단어가 잘릴 수 있음 ("Hello World" → "Hello W" | "orld")
 * 
 * 해결:
 * - WordCounterSpliterator: 공백에서만 분할
 * - 단어 무결성 보장
 */
public class WordCount {

  /**
   * 테스트용 문장
   * 
   * 특징:
   * - 불규칙한 공백
   * - 연속된 공백
   * - 단어: 19개
   */
  public static final String SENTENCE =
      " Nel   mezzo del cammin  di nostra  vita "
      + "mi  ritrovai in una  selva oscura"
      + " che la  dritta via era   smarrita ";

  public static void main(String[] args) {
    System.out.println("=".repeat(80));
    System.out.println("단어 카운터 예제");
    System.out.println("=".repeat(80));
    System.out.println("\n문장: \"" + SENTENCE + "\"");
    System.out.println();
    
    // 1. 반복문 버전
    int iterativeCount = countWordsIteratively(SENTENCE);
    System.out.println("1️⃣  반복문 버전: " + iterativeCount + " words");
    
    // 2. 순차 스트림 버전
    int sequentialCount = countWords(SENTENCE);
    System.out.println("2️⃣  스트림 버전 (병렬): " + sequentialCount + " words");
    
    System.out.println("\n" + "=".repeat(80));
    System.out.println("✅ 결과 일치: " + (iterativeCount == sequentialCount));
    System.out.println("=".repeat(80));
  }

  /**
   * 반복문을 사용한 단어 카운트 (기준)
   * 
   * 알고리즘:
   * 1. lastSpace = true로 시작
   * 2. 문자 순회:
   *    - 공백이면: lastSpace = true
   *    - 문자이고 lastSpace=true면: counter++
   * 
   * 예시: " Hello  World "
   * 
   * ' ' → lastSpace = true
   * 'H' → lastSpace=true → counter=1, lastSpace=false
   * 'e' → lastSpace=false (그대로)
   * 'l' → lastSpace=false (그대로)
   * 'l' → lastSpace=false (그대로)
   * 'o' → lastSpace=false (그대로)
   * ' ' → lastSpace = true
   * ' ' → lastSpace = true (그대로)
   * 'W' → lastSpace=true → counter=2, lastSpace=false
   * 'o' → lastSpace=false (그대로)
   * 'r' → lastSpace=false (그대로)
   * 'l' → lastSpace=false (그대로)
   * 'd' → lastSpace=false (그대로)
   * ' ' → lastSpace = true
   * 
   * 결과: counter = 2
   * 
   * @param s 문자열
   * @return 단어 개수
   */
  public static int countWordsIteratively(String s) {
    int counter = 0;
    boolean lastSpace = true;  // 초기: 공백 이후 상태
    
    for (char c : s.toCharArray()) {
      if (Character.isWhitespace(c)) {
        lastSpace = true;  // 공백 만남
      } else {
        if (lastSpace) {
          counter++;  // 새 단어 시작!
        }
        lastSpace = false;  // 단어 중간
      }
    }
    
    return counter;
  }

  /**
   * 스트림을 사용한 단어 카운트 (병렬)
   * 
   * 과정:
   * 1. WordCounterSpliterator 생성
   * 2. 병렬 스트림 생성 (true)
   * 3. reduce로 카운트
   * 
   * @param s 문자열
   * @return 단어 개수
   */
  public static int countWords(String s) {
    // 1. 커스텀 Spliterator 생성
    Spliterator<Character> spliterator = new WordCounterSpliterator(s);
    
    // 2. 병렬 스트림 생성
    Stream<Character> stream = StreamSupport.stream(spliterator, true);  // true = 병렬
    
    // 3. 카운트
    return countWords(stream);
  }

  /**
   * 스트림을 reduce로 카운트
   * 
   * reduce 패턴:
   * - identity: new WordCounter(0, true)
   * - accumulator: WordCounter::accumulate
   * - combiner: WordCounter::combine
   * 
   * @param stream 문자 스트림
   * @return 단어 개수
   */
  private static int countWords(Stream<Character> stream) {
    WordCounter wordCounter = stream.reduce(
        new WordCounter(0, true),     // 초기값
        WordCounter::accumulate,      // 누적
        WordCounter::combine          // 병합
    );
    return wordCounter.getCounter();
  }

  /**
   * 불변 단어 카운터
   * 
   * 특징:
   * - 불변 객체 (final 필드)
   * - 함수형 스타일
   * - 스레드 안전
   * 
   * 상태:
   * - counter: 단어 개수
   * - lastSpace: 직전이 공백인지 여부
   */
  private static class WordCounter {
    private final int counter;
    private final boolean lastSpace;

    public WordCounter(int counter, boolean lastSpace) {
      this.counter = counter;
      this.lastSpace = lastSpace;
    }

    /**
     * 문자 하나를 처리 (accumulate)
     * 
     * 로직:
     * - 공백이면:
     *   - lastSpace=false인 경우만 새 객체 (counter 유지, lastSpace=true)
     *   - lastSpace=true면 this 반환 (변화 없음)
     * 
     * - 문자이면:
     *   - lastSpace=true면 새 단어! (counter++, lastSpace=false)
     *   - lastSpace=false면 단어 중간 (this 반환)
     * 
     * @param c 처리할 문자
     * @return 새 WordCounter 또는 this
     */
    public WordCounter accumulate(Character c) {
      if (Character.isWhitespace(c)) {
        // 공백 처리
        return lastSpace 
            ? this  // 이미 공백 상태 → 변화 없음
            : new WordCounter(counter, true);  // 공백으로 전환
      } else {
        // 문자 처리
        return lastSpace 
            ? new WordCounter(counter + 1, false)  // 새 단어!
            : this;  // 단어 중간 → 변화 없음
      }
    }

    /**
     * 두 WordCounter 병합 (combine)
     * 
     * 병렬 처리 시 필요:
     * - Thread-1: WordCounter(3, true)
     * - Thread-2: WordCounter(5, false)
     * - combine: WordCounter(8, false)
     * 
     * 주의:
     * - counter는 단순 합산
     * - lastSpace는 오른쪽 값 사용
     *   (오른쪽이 문장의 끝 부분이므로)
     * 
     * @param wordCounter 병합할 카운터
     * @return 병합된 카운터
     */
    public WordCounter combine(WordCounter wordCounter) {
      return new WordCounter(
          counter + wordCounter.counter,  // 카운트 합산
          wordCounter.lastSpace           // 오른쪽 lastSpace 사용
      );
    }

    public int getCounter() {
      return counter;
    }
  }

  /**
   * 커스텀 Spliterator: 공백에서만 분할
   * 
   * 목표:
   * - 단어 경계(공백)에서만 분할
   * - 단어가 잘리지 않도록 보장
   * 
   * 핵심:
   * - tryAdvance: 문자 하나씩 처리
   * - trySplit: 공백 찾아서 분할
   * - estimateSize: 남은 문자 수
   * - characteristics: ORDERED, SIZED, SUBSIZED, NONNULL, IMMUTABLE
   */
  private static class WordCounterSpliterator implements Spliterator<Character> {
    /**
     * 전체 문자열
     */
    private final String string;
    
    /**
     * 현재 처리 중인 인덱스
     * 
     * ⭐ 중요: trySplit()에서 업데이트 필수!
     * → 업데이트 안 하면 중복 처리 발생
     */
    private int currentChar = 0;

    private WordCounterSpliterator(String string) {
      this.string = string;
    }

    /**
     * 요소 하나씩 처리 (tryAdvance)
     * 
     * 동작:
     * 1. currentChar 위치의 문자 가져오기
     * 2. Consumer에 전달
     * 3. currentChar 증가
     * 4. 남은 요소 있으면 true, 없으면 false
     * 
     * @param action 문자를 처리할 Consumer
     * @return 처리할 요소가 남았는지 여부
     */
    @Override
    public boolean tryAdvance(Consumer<? super Character> action) {
      // currentChar 위치의 문자를 Consumer에 전달
      action.accept(string.charAt(currentChar++));
      
      // 남은 요소가 있는지 확인
      return currentChar < string.length();
    }

    /**
     * 분할 (trySplit)
     * 
     * 핵심 로직:
     * 1. 남은 크기가 10 미만이면 null (분할 안 함)
     * 2. 중간점 계산
     * 3. 중간점부터 공백 찾기
     * 4. 공백 발견:
     *    - 앞부분 → 새 Spliterator (return)
     *    - 뒷부분 → 현재 객체 (currentChar 업데이트!)
     * 5. 공백 없으면 null
     * 
     * 예시: "Hello World Good Morning" (currentChar=0)
     * 
     * 1. currentSize = 23 - 0 = 23
     * 2. splitPos = 23/2 + 0 = 11
     * 3. string[11] = 'W' (공백 아님)
     *    string[12] = ' ' (공백!) → 여기서 분할!
     * 4. 앞부분: "Hello World " (0~12)
     *    뒷부분: currentChar = 12로 업데이트
     * 
     * Thread-1: "Hello World " 처리
     * Thread-2: "Good Morning" 처리 (currentChar=12부터)
     * 
     * @return 분할된 Spliterator 또는 null
     */
    @Override
    public Spliterator<Character> trySplit() {
      // 1. 남은 크기 계산
      int currentSize = string.length() - currentChar;
      
      // 2. 최소 크기 체크 (너무 작으면 분할 안 함)
      if (currentSize < 10) {
        return null;
      }
      
      // 3. 중간점부터 공백 찾기
      for (int splitPos = currentSize / 2 + currentChar; 
           splitPos < string.length(); 
           splitPos++) {
        
        // 공백 발견!
        if (Character.isWhitespace(string.charAt(splitPos))) {
          
          // 4. 앞부분을 새 Spliterator로
          Spliterator<Character> spliterator = 
              new WordCounterSpliterator(
                  string.substring(currentChar, splitPos)
              );
          
          // 5. ⭐ currentChar 업데이트 (필수!)
          // 업데이트 안 하면 중복 처리 발생!
          currentChar = splitPos;
          
          return spliterator;
        }
      }
      
      // 공백 못 찾음 → 분할 불가
      return null;
    }

    /**
     * 남은 요소 개수 (estimateSize)
     * 
     * @return 처리할 문자 수
     */
    @Override
    public long estimateSize() {
      return string.length() - currentChar;
    }

    /**
     * 특성 (characteristics)
     * 
     * ORDERED: 순서 있음 (문자열 순서)
     * SIZED: 정확한 크기 알 수 있음 (string.length())
     * SUBSIZED: 분할 후에도 크기 O(1)로 계산 가능
     * NONNULL: null 없음 (문자는 null 될 수 없음)
     * IMMUTABLE: 불변 (String은 불변)
     * 
     * @return 특성 플래그
     */
    @Override
    public int characteristics() {
      return ORDERED + SIZED + SUBSIZED + NONNULL + IMMUTABLE;
    }
  }

  /**
   * currentChar 업데이트가 필요한 이유:
   * 
   * ❌ 업데이트 안 하면:
   * 
   * trySplit() 호출:
   * - newSplit: "Hello World " (0~12)
   * - this.currentChar: 0 (그대로!)
   * 
   * Thread-1이 newSplit 처리:
   * - tryAdvance() → string[0] ('H')부터 처리
   * 
   * Thread-2가 this 처리:
   * - tryAdvance() → string[0] ('H')부터 또 처리!
   * → "Hello World " 중복 처리!
   * 
   * ✅ 업데이트 하면:
   * 
   * trySplit() 호출:
   * - newSplit: "Hello World " (0~12)
   * - this.currentChar: 12 (업데이트!)
   * 
   * Thread-1이 newSplit 처리:
   * - "Hello World " 처리
   * 
   * Thread-2가 this 처리:
   * - tryAdvance() → string[12]부터 처리
   * → "Good Morning"만 처리 (정확!)
   */

}
