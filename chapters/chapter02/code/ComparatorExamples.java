import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Comparator를 이용한 정렬 완벽 가이드
 * 
 * 학습 목표:
 * 1. Comparator 기본 사용법
 * 2. comparing() 메서드의 활용
 * 3. reversed()로 역순 정렬
 * 4. thenComparing()으로 다중 조건 정렬
 * 5. 익명 클래스 → 람다 → 메서드 참조로의 발전
 */
public class ComparatorExamples {
    
    // Student 클래스
    static class Student {
        private String name;
        private int grade;  // 학년
        private int score;  // 성적
        
        public Student(String name, int grade, int score) {
            this.name = name;
            this.grade = grade;
            this.score = score;
        }
        
        public String getName() { return name; }
        public int getGrade() { return grade; }
        public int getScore() { return score; }
        
        @Override
        public String toString() {
            return String.format("Student{name='%s', grade=%d, score=%d}", 
                name, grade, score);
        }
    }
    
    public static void main(String[] args) {
        // 테스트 데이터
        List<Student> students = new ArrayList<>(Arrays.asList(
            new Student("Alice", 2, 85),
            new Student("Bob", 1, 92),
            new Student("Charlie", 2, 78),
            new Student("David", 1, 88),
            new Student("Eve", 3, 95),
            new Student("Frank", 2, 92),
            new Student("Grace", 3, 88),
            new Student("Henry", 1, 85)
        ));
        
        System.out.println("=".repeat(70));
        System.out.println("Comparator 정렬 완벽 가이드");
        System.out.println("=".repeat(70));
        System.out.println();
        
        System.out.println("【원본 데이터】");
        students.forEach(System.out::println);
        System.out.println();
        
        // ========================================
        // 1. 익명 클래스 방식 (Java 5~7)
        // ========================================
        
        System.out.println("【1】 익명 클래스 - 성적순 정렬");
        
        List<Student> list1 = new ArrayList<>(students);
        list1.sort(new Comparator<Student>() {
            @Override
            public int compare(Student s1, Student s2) {
                return Integer.compare(s1.getScore(), s2.getScore());
            }
        });
        
        list1.forEach(System.out::println);
        System.out.println("⚠️  장황하고 보일러플레이트 많음");
        System.out.println();
        
        // ========================================
        // 2. 람다 표현식 (Java 8+)
        // ========================================
        
        System.out.println("【2】 람다 표현식 - 성적순 정렬");
        
        List<Student> list2 = new ArrayList<>(students);
        list2.sort((s1, s2) -> Integer.compare(s1.getScore(), s2.getScore()));
        
        list2.forEach(System.out::println);
        System.out.println("✅ 간결해짐");
        System.out.println();
        
        // ========================================
        // 3. Comparator.comparing() - 가장 권장!
        // ========================================
        
        System.out.println("【3】 Comparator.comparing() - 성적순 정렬");
        
        List<Student> list3 = new ArrayList<>(students);
        list3.sort(Comparator.comparingInt(Student::getScore));
        
        list3.forEach(System.out::println);
        System.out.println("✅ 가장 간결하고 명확!");
        System.out.println();
        
        // ========================================
        // 4. reversed() - 역순 정렬
        // ========================================
        
        System.out.println("【4】 reversed() - 성적 높은 순");
        
        List<Student> list4 = new ArrayList<>(students);
        list4.sort(Comparator.comparingInt(Student::getScore).reversed());
        
        list4.forEach(System.out::println);
        System.out.println("✅ 내림차순 정렬");
        System.out.println();
        
        // ========================================
        // 5. thenComparing() - 다중 조건
        // ========================================
        
        System.out.println("【5】 thenComparing() - 학년순, 같으면 성적순");
        
        List<Student> list5 = new ArrayList<>(students);
        list5.sort(
            Comparator.comparingInt(Student::getGrade)
                      .thenComparingInt(Student::getScore)
        );
        
        list5.forEach(System.out::println);
        System.out.println("✅ 1차: 학년(오름), 2차: 성적(오름)");
        System.out.println();
        
        // ========================================
        // 6. 복합 정렬 - 학년순, 성적 높은 순
        // ========================================
        
        System.out.println("【6】 복합 정렬 - 학년순, 같으면 성적 높은 순");
        
        List<Student> list6 = new ArrayList<>(students);
        list6.sort(
            Comparator.comparingInt(Student::getGrade)
                      .thenComparingInt(Student::getScore).reversed()
        );
        
        list6.forEach(System.out::println);
        System.out.println("✅ 1차: 학년(오름), 2차: 성적(내림)");
        System.out.println();
        
        // ========================================
        // 7. 3중 정렬 - 학년 → 성적 → 이름
        // ========================================
        
        System.out.println("【7】 3중 정렬 - 학년 → 성적(높은순) → 이름순");
        
        List<Student> list7 = new ArrayList<>(students);
        list7.sort(
            Comparator.comparingInt(Student::getGrade)
                      .thenComparingInt(Student::getScore).reversed()
                      .thenComparing(Student::getName)
        );
        
        list7.forEach(System.out::println);
        System.out.println("✅ 3단계 정렬 체이닝");
        System.out.println();
        
        // ========================================
        // 8. Comparator 재사용
        // ========================================
        
        System.out.println("【8】 Comparator 재사용");
        
        Comparator<Student> byScore = Comparator.comparingInt(Student::getScore);
        Comparator<Student> byGrade = Comparator.comparingInt(Student::getGrade);
        
        List<Student> list8a = new ArrayList<>(students);
        List<Student> list8b = new ArrayList<>(students);
        
        list8a.sort(byScore);
        list8b.sort(byScore.reversed());
        
        System.out.println("성적 오름차순:");
        list8a.subList(0, 3).forEach(s -> System.out.println("  " + s));
        
        System.out.println("성적 내림차순:");
        list8b.subList(0, 3).forEach(s -> System.out.println("  " + s));
        
        System.out.println("✅ Comparator를 변수에 저장하여 재사용");
        System.out.println();
        
        // ========================================
        // 9. 조건부 정렬
        // ========================================
        
        System.out.println("【9】 조건부 정렬");
        
        boolean isDescending = true;
        Comparator<Student> scoreComparator = Comparator.comparingInt(Student::getScore);
        
        List<Student> list9 = new ArrayList<>(students);
        list9.sort(isDescending ? scoreComparator.reversed() : scoreComparator);
        
        System.out.println(isDescending ? "내림차순:" : "오름차순:");
        list9.forEach(System.out::println);
        System.out.println("✅ 런타임에 정렬 방향 결정");
        System.out.println();
        
        // ========================================
        // 10. 실전 예제 - 상위 3명 선발
        // ========================================
        
        System.out.println("【10】 실전 예제 - 성적 상위 3명");
        
        List<Student> top3 = new ArrayList<>(students);
        top3.sort(Comparator.comparingInt(Student::getScore).reversed());
        
        System.out.println("🏆 성적 상위 3명:");
        for (int i = 0; i < 3 && i < top3.size(); i++) {
            Student s = top3.get(i);
            System.out.printf("  %d등: %s (성적: %d점)%n", 
                i + 1, s.getName(), s.getScore());
        }
        System.out.println();
        
        // ========================================
        // 11. 성능 최적화 - comparingInt vs comparing
        // ========================================
        
        System.out.println("【11】 성능 최적화");
        
        // ❌ 박싱 오버헤드
        Comparator<Student> inefficient = Comparator.comparing(Student::getScore);
        
        // ✅ 박싱 없음
        Comparator<Student> efficient = Comparator.comparingInt(Student::getScore);
        
        System.out.println("✅ comparingInt() 사용으로 박싱 오버헤드 제거");
        System.out.println("   (대량 데이터에서 성능 차이 발생)");
        System.out.println();
        
        System.out.println("=".repeat(70));
        System.out.println("💡 핵심: Comparator.comparing()으로 간결하고 명확한 정렬!");
        System.out.println("=".repeat(70));
    }
}
