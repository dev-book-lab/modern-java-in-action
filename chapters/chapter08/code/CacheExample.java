package chapter08.code;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * computeIfAbsent를 활용한 캐시 구현 예제
 * 
 * 목표:
 * - computeIfAbsent의 실전 활용 이해
 * - 캐시 패턴 구현 방법 학습
 * - 중복 계산 방지 메커니즘 이해
 * 
 * 시나리오:
 * - 문자열에 대한 SHA-256 해시 계산 (비용이 큰 연산)
 * - 같은 문자열은 한 번만 계산하고 재사용
 */
public class CacheExample {

  /**
   * SHA-256 해시 생성기
   * - 암호화 해시 함수
   * - 동일 입력 → 동일 출력
   * - 계산 비용이 상대적으로 높음
   */
  private MessageDigest messageDigest;

  public static void main(String[] args) {
    new CacheExample().run();
  }

  /**
   * 생성자: MessageDigest 초기화
   */
  public CacheExample() {
    try {
      messageDigest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
  }

  /**
   * 메인 실행 메서드
   */
  private void run() {
    System.out.println("=".repeat(80));
    System.out.println("computeIfAbsent를 활용한 캐시 예제");
    System.out.println("=".repeat(80));

    // 테스트 데이터 (일부러 중복 포함)
    List<String> lines = Arrays.asList(
        " Nel   mezzo del cammin  di nostra  vita ",
        "mi  ritrovai in una  selva oscura",
        " che la  dritta via era   smarrita ",
        " Nel   mezzo del cammin  di nostra  vita "  // 중복!
    );

    // 캐시 맵: 문자열 → 해시값
    Map<String, byte[]> dataToHash = new HashMap<>();

    System.out.println("\n1️⃣  문자열 해싱 (캐시 활용):\n");

    // ✅ computeIfAbsent 사용
    lines.forEach(line -> {
      System.out.println("처리 중: \"" + line.trim() + "\"");
      
      // 핵심: computeIfAbsent
      // - 키 있으면: 기존 해시 재사용 (함수 실행 안 함)
      // - 키 없으면: 함수 실행해서 해시 계산 후 저장
      dataToHash.computeIfAbsent(line, this::calculateDigest);
    });

    System.out.println("\n2️⃣  캐시 결과:\n");
    
    // 캐시 내용 출력
    dataToHash.forEach((line, hash) -> {
      String hashString = bytesToHex(hash);
      System.out.printf("문자열: \"%s\"%n", line.trim());
      System.out.printf("해시:   %s%n%n", hashString);
    });

    System.out.println("3️⃣  캐시 효과:\n");
    System.out.println("   - 총 입력: " + lines.size() + "개");
    System.out.println("   - 고유 입력: " + dataToHash.size() + "개");
    System.out.println("   - 중복 제거: " + (lines.size() - dataToHash.size()) + "개");
    System.out.println("   - 계산 절약: " + 
        String.format("%.0f%%", (1 - (double) dataToHash.size() / lines.size()) * 100));

    // 비교: computeIfAbsent 없이 구현
    demonstrateWithoutComputeIfAbsent(lines);

    System.out.println("\n" + "=".repeat(80));
    System.out.println("💡 결론:");
    System.out.println("   - computeIfAbsent: 간결하고 효율적");
    System.out.println("   - 중복 계산 자동 방지");
    System.out.println("   - 캐시 패턴 구현에 최적");
    System.out.println("=".repeat(80));
  }

  /**
   * 해시 계산 함수
   * 
   * computeIfAbsent의 매핑 함수로 사용됨
   * - 키가 없을 때만 호출됨
   * - 계산 비용이 높은 연산
   * 
   * @param key 해시를 계산할 문자열
   * @return SHA-256 해시 (32 bytes)
   */
  private byte[] calculateDigest(String key) {
    System.out.println("   ⚙️  해시 계산 실행: \"" + key.trim() + "\"");
    return messageDigest.digest(key.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * byte[] → 16진수 문자열 변환
   * 
   * @param bytes 바이트 배열
   * @return 16진수 문자열
   */
  private String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }

  /**
   * 비교: computeIfAbsent 없이 구현 (장황함)
   */
  private void demonstrateWithoutComputeIfAbsent(List<String> lines) {
    System.out.println("\n4️⃣  비교: computeIfAbsent 없이 구현\n");

    Map<String, byte[]> manualCache = new HashMap<>();

    lines.forEach(line -> {
      // ❌ 장황한 방식
      byte[] hash = manualCache.get(line);
      if (hash == null) {
        hash = messageDigest.digest(line.getBytes(StandardCharsets.UTF_8));
        manualCache.put(line, hash);
        System.out.println("   수동 계산: \"" + line.trim() + "\"");
      } else {
        System.out.println("   캐시 재사용: \"" + line.trim() + "\"");
      }
    });

    System.out.println("\n   ❌ 문제점:");
    System.out.println("      - 5줄 코드 vs 1줄 코드");
    System.out.println("      - null 체크 필요");
    System.out.println("      - 읽기 어려움");
  }

  /**
   * computeIfAbsent 동작 원리:
   * 
   * 1. 키 존재 확인
   *    if (map.containsKey(key))
   * 
   * 2-A. 키 있으면:
   *      return map.get(key);
   *      함수 실행 안 함! (효율적)
   * 
   * 2-B. 키 없으면:
   *      V value = mappingFunction.apply(key);
   *      map.put(key, value);
   *      return value;
   * 
   * 장점:
   * - 원자적 연산 (Thread-Safe in ConcurrentHashMap)
   * - 중복 계산 방지
   * - 코드 간결
   */

  /**
   * 실전 활용 사례:
   * 
   * 1. 데이터베이스 조회 캐시
   *    userCache.computeIfAbsent(userId, id -> loadUserFromDB(id))
   * 
   * 2. 설정 파일 파싱
   *    configCache.computeIfAbsent(filename, name -> parseConfig(name))
   * 
   * 3. 이미지 썸네일 생성
   *    thumbnailCache.computeIfAbsent(imageUrl, url -> generateThumbnail(url))
   * 
   * 4. 정규식 컴파일
   *    patternCache.computeIfAbsent(regex, r -> Pattern.compile(r))
   * 
   * 5. 그룹핑
   *    groupMap.computeIfAbsent(category, k -> new ArrayList<>()).add(item)
   */

}
