package chapter03.code;

/**
 * Apple 클래스
 * 
 * 람다 표현식과 메서드 참조 예제에서 사용되는 도메인 클래스
 * 
 * @author Modern Java In Action
 */
public class Apple {

  // 사과의 무게 (그램 단위)
  private int weight = 0;
  
  // 사과의 색상
  private Color color;

  /**
   * 사과 생성자
   * 
   * @param weight 무게
   * @param color  색상
   */
  public Apple(int weight, Color color) {
    this.weight = weight;
    this.color = color;
  }

  /**
   * 무게 getter
   * 
   * @return 사과의 무게
   */
  public int getWeight() {
    return weight;
  }

  /**
   * 무게 setter
   * 
   * @param weight 설정할 무게
   */
  public void setWeight(int weight) {
    this.weight = weight;
  }

  /**
   * 색상 getter
   * 
   * @return 사과의 색상
   */
  public Color getColor() {
    return color;
  }

  /**
   * 색상 setter
   * 
   * @param color 설정할 색상
   */
  public void setColor(Color color) {
    this.color = color;
  }

  /**
   * 문자열 표현
   * 
   * @return "Apple{color=RED, weight=150}" 형식의 문자열
   */
  @SuppressWarnings("boxing")
  @Override
  public String toString() {
    return String.format("Apple{color=%s, weight=%d}", color, weight);
  }

}
