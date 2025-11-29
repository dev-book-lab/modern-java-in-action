package chapter02.code;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * Runnable과 Callable을 이용한 스레드 프로그래밍
 * 
 * 학습 목표:
 * 1. Runnable으로 코드 블록 실행
 * 2. Callable로 결과 반환
 * 3. ExecutorService 활용
 * 4. 익명 클래스 → 람다로의 발전
 */
public class ThreadExamples {
    
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("=".repeat(70));
        System.out.println("Runnable & Callable - 스레드 동작 파라미터화");
        System.out.println("=".repeat(70));
        System.out.println();
        
        // ========================================
        // 1. Runnable - 익명 클래스 방식
        // ========================================
        
        System.out.println("【1】 Runnable - 익명 클래스");
        
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("  익명 클래스로 스레드 실행");
                System.out.println("  현재 스레드: " + Thread.currentThread().getName());
            }
        });
        
        thread1.start();
        thread1.join();  // 완료 대기
        System.out.println("⚠️  보일러플레이트 많음");
        System.out.println();
        
        // ========================================
        // 2. Runnable - 람다 표현식
        // ========================================
        
        System.out.println("【2】 Runnable - 람다 표현식");
        
        Thread thread2 = new Thread(() -> {
            System.out.println("  람다로 스레드 실행");
            System.out.println("  현재 스레드: " + Thread.currentThread().getName());
        });
        
        thread2.start();
        thread2.join();
        System.out.println("✅ 간결하고 명확!");
        System.out.println();
        
        // ========================================
        // 3. Runnable - 여러 작업 실행
        // ========================================
        
        System.out.println("【3】 Runnable - 여러 작업 실행");
        
        Runnable task1 = () -> {
            System.out.println("  Task 1 실행 중...");
            sleep(100);
            System.out.println("  Task 1 완료!");
        };
        
        Runnable task2 = () -> {
            System.out.println("  Task 2 실행 중...");
            sleep(100);
            System.out.println("  Task 2 완료!");
        };
        
        Thread t1 = new Thread(task1);
        Thread t2 = new Thread(task2);
        
        t1.start();
        t2.start();
        
        t1.join();
        t2.join();
        
        System.out.println("✅ 병렬 실행 완료");
        System.out.println();
        
        // ========================================
        // 4. Callable - 결과 반환
        // ========================================
        
        System.out.println("【4】 Callable - 결과 반환 (익명 클래스)");
        
        ExecutorService executor1 = Executors.newSingleThreadExecutor();
        
        Future<String> future1 = executor1.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                sleep(100);
                return "익명 클래스에서 계산한 결과";
            }
        });
        
        String result1 = future1.get();  // 결과 대기
        System.out.println("  결과: " + result1);
        executor1.shutdown();
        System.out.println();
        
        // ========================================
        // 5. Callable - 람다로 간결하게
        // ========================================
        
        System.out.println("【5】 Callable - 람다 표현식");
        
        ExecutorService executor2 = Executors.newSingleThreadExecutor();
        
        Future<Integer> future2 = executor2.submit(() -> {
            System.out.println("  계산 중...");
            sleep(100);
            return 1 + 2 + 3 + 4 + 5;
        });
        
        Integer result2 = future2.get();
        System.out.println("  결과: " + result2);
        executor2.shutdown();
        System.out.println("✅ 람다로 간결하게 결과 반환");
        System.out.println();
        
        // ========================================
        // 6. 여러 Callable 병렬 실행
        // ========================================
        
        System.out.println("【6】 여러 Callable 병렬 실행");
        
        ExecutorService executor3 = Executors.newFixedThreadPool(3);
        
        Callable<Integer> task1Callable = () -> {
            System.out.println("  작업 1 시작");
            sleep(200);
            return 10;
        };
        
        Callable<Integer> task2Callable = () -> {
            System.out.println("  작업 2 시작");
            sleep(150);
            return 20;
        };
        
        Callable<Integer> task3Callable = () -> {
            System.out.println("  작업 3 시작");
            sleep(100);
            return 30;
        };
        
        List<Future<Integer>> futures = executor3.invokeAll(
            Arrays.asList(task1Callable, task2Callable, task3Callable)
        );
        
        int sum = 0;
        for (Future<Integer> future : futures) {
            sum += future.get();
        }
        
        System.out.println("  모든 작업 완료!");
        System.out.println("  합계: " + sum);
        executor3.shutdown();
        System.out.println();
        
        // ========================================
        // 7. 실전 예제 - 데이터 처리
        // ========================================
        
        System.out.println("【7】 실전 예제 - 병렬 데이터 처리");
        
        ExecutorService executor4 = Executors.newFixedThreadPool(4);
        
        // 각 스레드가 다른 범위의 합을 계산
        Callable<Integer> range1 = () -> sum(1, 25);
        Callable<Integer> range2 = () -> sum(26, 50);
        Callable<Integer> range3 = () -> sum(51, 75);
        Callable<Integer> range4 = () -> sum(76, 100);
        
        List<Future<Integer>> results = executor4.invokeAll(
            Arrays.asList(range1, range2, range3, range4)
        );
        
        int totalSum = 0;
        for (Future<Integer> future : results) {
            totalSum += future.get();
        }
        
        System.out.println("  1~100의 합 (병렬 계산): " + totalSum);
        executor4.shutdown();
        System.out.println();
        
        // ========================================
        // 8. Runnable vs Callable 비교
        // ========================================
        
        System.out.println("【8】 Runnable vs Callable 비교");
        
        ExecutorService executor5 = Executors.newFixedThreadPool(2);
        
        // Runnable: 결과 반환 불가
        executor5.submit(() -> {
            System.out.println("  Runnable: 결과를 반환할 수 없음 (void)");
        });
        
        // Callable: 결과 반환 가능
        Future<String> callableResult = executor5.submit(() -> {
            return "Callable: 결과 반환 가능!";
        });
        
        System.out.println("  " + callableResult.get());
        executor5.shutdown();
        System.out.println();
        
        // ========================================
        // 9. 타임아웃 처리
        // ========================================
        
        System.out.println("【9】 타임아웃 처리");
        
        ExecutorService executor6 = Executors.newSingleThreadExecutor();
        
        Future<String> slowTask = executor6.submit(() -> {
            sleep(5000);  // 5초 걸리는 작업
            return "완료";
        });
        
        try {
            // 1초만 기다림
            String result = slowTask.get(1, TimeUnit.SECONDS);
            System.out.println("  결과: " + result);
        } catch (TimeoutException e) {
            System.out.println("  ⚠️  작업이 1초 내에 완료되지 않아 취소합니다");
            slowTask.cancel(true);
        }
        
        executor6.shutdownNow();
        System.out.println();
        
        // ========================================
        // 10. 실전 패턴 - 작업 큐
        // ========================================
        
        System.out.println("【10】 실전 패턴 - 작업 큐");
        
        ExecutorService executor7 = Executors.newFixedThreadPool(2);
        
        System.out.println("  5개의 작업을 2개의 스레드로 처리:");
        
        for (int i = 1; i <= 5; i++) {
            final int taskNum = i;
            executor7.submit(() -> {
                System.out.printf("    작업 %d 시작 (스레드: %s)%n", 
                    taskNum, Thread.currentThread().getName());
                sleep(500);
                System.out.printf("    작업 %d 완료%n", taskNum);
                return taskNum;
            });
        }
        
        executor7.shutdown();
        executor7.awaitTermination(10, TimeUnit.SECONDS);
        
        System.out.println("  ✅ 모든 작업 완료");
        System.out.println();
        
        System.out.println("=".repeat(70));
        System.out.println("💡 핵심: 동작 파라미터화로 스레드에 유연하게 작업 전달!");
        System.out.println("=".repeat(70));
    }
    
    // ========================================
    // 헬퍼 메서드
    // ========================================
    
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private static int sum(int start, int end) {
        System.out.printf("  범위 %d~%d 계산 중...%n", start, end);
        sleep(100);
        int result = 0;
        for (int i = start; i <= end; i++) {
            result += i;
        }
        return result;
    }
}
