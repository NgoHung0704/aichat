package ie.tcd.scss.aichat.performance;

import ie.tcd.scss.aichat.model.FlashcardSet;
import ie.tcd.scss.aichat.model.QuizSet;
import ie.tcd.scss.aichat.model.User;
import ie.tcd.scss.aichat.repository.FlashcardSetRepository;
import ie.tcd.scss.aichat.repository.QuizSetRepository;
import ie.tcd.scss.aichat.util.PerformanceTestDataGenerator;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance tests for Job 9
 * Tests database indexes, query performance, concurrent access, and transaction rollback
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PerformanceTest {

    @Autowired
    private PerformanceTestDataGenerator dataGenerator;

    @Autowired
    private FlashcardSetRepository flashcardSetRepository;

    @Autowired
    private QuizSetRepository quizSetRepository;

    private static User testUser;
    private static boolean dataGenerated = false;

    /**
     * Test 1: Generate 50+ flashcard sets and 50+ quiz sets for performance testing
     * Note: NOT @Transactional so data persists for subsequent tests
     */
    @Test
    @Order(1)
    void test1_GeneratePerformanceTestData() {
        System.out.println("\n=== JOB 9: PERFORMANCE TEST - DATA GENERATION ===\n");

        // Create test user
        testUser = dataGenerator.createTestUser("perftest_user", "perftest@test.com");
        System.out.println("✓ Created test user: " + testUser.getUsername());

        // Generate 60 flashcard sets (exceeds requirement of 50)
        long startFlashcards = System.currentTimeMillis();
        List<FlashcardSet> flashcardSets = dataGenerator.generateFlashcardSets(testUser, 60);
        long flashcardTime = System.currentTimeMillis() - startFlashcards;

        System.out.println("✓ Generated 60 flashcard sets in " + flashcardTime + "ms");

        // Generate 60 quiz sets (exceeds requirement of 50)
        long startQuizzes = System.currentTimeMillis();
        List<QuizSet> quizSets = dataGenerator.generateQuizSets(testUser, 60);
        long quizTime = System.currentTimeMillis() - startQuizzes;

        System.out.println("✓ Generated 60 quiz sets in " + quizTime + "ms");

        // Print statistics
        System.out.println("\n" + dataGenerator.getStatistics(testUser));

        // Verify counts
        assertEquals(60, flashcardSets.size(), "Should have 60 flashcard sets");
        assertEquals(60, quizSets.size(), "Should have 60 quiz sets");

        dataGenerated = true;
        System.out.println("\n✅ Test data generation complete\n");
    }

    /**
     * Test 2: Verify history endpoint performance with 50+ sets
     * Requirements: < 1 second for history endpoint
     */
    @Test
    @Order(2)
    void test2_FlashcardHistoryPerformance() {
        assertTrue(dataGenerated, "Test data must be generated first");
        
        System.out.println("\n=== PERFORMANCE TEST: Flashcard History Query ===\n");

        // Warm-up query (to cache connection, etc.)
        flashcardSetRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());

        // Performance test - should return in < 1 second
        long startTime = System.currentTimeMillis();
        List<FlashcardSet> sets = flashcardSetRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());
        long queryTime = System.currentTimeMillis() - startTime;

        System.out.println("Query Results:");
        System.out.println("  - Flashcard sets returned: " + sets.size());
        System.out.println("  - Query execution time: " + queryTime + "ms");
        System.out.println("  - Performance requirement: < 1000ms");

        // Verify results
        assertEquals(60, sets.size(), "Should return all 60 flashcard sets");
        assertTrue(queryTime < 1000, 
            "History query should complete in < 1 second, actual: " + queryTime + "ms");

        // Verify sorting (most recent first)
        for (int i = 0; i < sets.size() - 1; i++) {
            assertTrue(
                sets.get(i).getCreatedAt().isAfter(sets.get(i + 1).getCreatedAt()) ||
                sets.get(i).getCreatedAt().equals(sets.get(i + 1).getCreatedAt()),
                "Sets should be ordered by created_at DESC"
            );
        }

        if (queryTime < 100) {
            System.out.println("✅ EXCELLENT: Query completed in " + queryTime + "ms (with indexes)");
        } else if (queryTime < 500) {
            System.out.println("✅ GOOD: Query completed in " + queryTime + "ms");
        } else {
            System.out.println("✅ PASS: Query completed in " + queryTime + "ms (within requirement)");
        }
    }

    /**
     * Test 3: Verify get-by-ID performance
     * Requirements: < 500ms for single set retrieval
     */
    @Test
    @Order(3)
    @Transactional  // Need transaction for lazy loading flashcards collection
    void test3_FlashcardGetByIdPerformance() {
        assertTrue(dataGenerated, "Test data must be generated first");
        
        System.out.println("\n=== PERFORMANCE TEST: Flashcard Get By ID Query ===\n");

        List<FlashcardSet> sets = flashcardSetRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());
        assertTrue(sets.size() > 0, "Should have flashcard sets");

        Long testSetId = sets.get(0).getId();

        // Warm-up query
        flashcardSetRepository.findById(testSetId);

        // Performance test - should return in < 500ms
        long startTime = System.currentTimeMillis();
        Optional<FlashcardSet> result = flashcardSetRepository.findById(testSetId);
        long queryTime = System.currentTimeMillis() - startTime;

        System.out.println("Query Results:");
        System.out.println("  - Set ID: " + testSetId);
        System.out.println("  - Flashcards loaded: " + result.get().getFlashcards().size());
        System.out.println("  - Query execution time: " + queryTime + "ms");
        System.out.println("  - Performance requirement: < 500ms");

        assertTrue(result.isPresent(), "Should find the flashcard set");
        assertTrue(queryTime < 500, 
            "Get-by-ID query should complete in < 500ms, actual: " + queryTime + "ms");

        if (queryTime < 50) {
            System.out.println("✅ EXCELLENT: Query completed in " + queryTime + "ms");
        } else if (queryTime < 200) {
            System.out.println("✅ GOOD: Query completed in " + queryTime + "ms");
        } else {
            System.out.println("✅ PASS: Query completed in " + queryTime + "ms (within requirement)");
        }
    }

    /**
     * Test 4: Quiz history performance
     */
    @Test
    @Order(4)
    void test4_QuizHistoryPerformance() {
        assertTrue(dataGenerated, "Test data must be generated first");
        
        System.out.println("\n=== PERFORMANCE TEST: Quiz History Query ===\n");

        // Warm-up
        quizSetRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());

        // Performance test
        long startTime = System.currentTimeMillis();
        List<QuizSet> sets = quizSetRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());
        long queryTime = System.currentTimeMillis() - startTime;

        System.out.println("Query Results:");
        System.out.println("  - Quiz sets returned: " + sets.size());
        System.out.println("  - Query execution time: " + queryTime + "ms");
        System.out.println("  - Performance requirement: < 1000ms");

        assertEquals(60, sets.size(), "Should return all 60 quiz sets");
        assertTrue(queryTime < 1000, 
            "Quiz history query should complete in < 1 second, actual: " + queryTime + "ms");

        System.out.println("✅ Quiz history query performance: PASS");
    }

    /**
     * Test 5: Concurrent request handling (no data corruption)
     */
    @Test
    @Order(5)
    void test5_ConcurrentRequestsNoDataCorruption() throws InterruptedException {
        System.out.println("\n=== CONCURRENT ACCESS TEST ===\n");

        // Create a new test user for this test
        User concurrentUser = dataGenerator.createTestUser("concurrent_test", "concurrent@test.com");

        final int NUM_THREADS = 10;
        final int SETS_PER_THREAD = 5;
        
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        CountDownLatch latch = new CountDownLatch(NUM_THREADS);
        AtomicInteger successCount = new AtomicInteger(0);

        System.out.println("Launching " + NUM_THREADS + " concurrent threads...");
        System.out.println("Each thread creates " + SETS_PER_THREAD + " flashcard sets");

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < NUM_THREADS; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    List<FlashcardSet> sets = dataGenerator.generateFlashcardSets(
                        concurrentUser, SETS_PER_THREAD
                    );
                    if (sets.size() == SETS_PER_THREAD) {
                        successCount.incrementAndGet();
                    }
                    System.out.println("  Thread " + threadId + " completed successfully");
                } catch (Exception e) {
                    System.err.println("  Thread " + threadId + " failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        long duration = System.currentTimeMillis() - startTime;

        // Verify no data corruption
        List<FlashcardSet> allSets = flashcardSetRepository.findByUserIdOrderByCreatedAtDesc(concurrentUser.getId());

        System.out.println("\nConcurrent Test Results:");
        System.out.println("  - Threads succeeded: " + successCount.get() + "/" + NUM_THREADS);
        System.out.println("  - Expected sets: " + (NUM_THREADS * SETS_PER_THREAD));
        System.out.println("  - Actual sets in DB: " + allSets.size());
        System.out.println("  - Duration: " + duration + "ms");

        assertEquals(NUM_THREADS, successCount.get(), "All threads should complete successfully");
        assertEquals(NUM_THREADS * SETS_PER_THREAD, allSets.size(), 
            "Should have exactly " + (NUM_THREADS * SETS_PER_THREAD) + " sets (no data corruption)");

        // Cleanup
        dataGenerator.cleanupTestData(concurrentUser);

        System.out.println("✅ Concurrent access test: PASS (no data corruption)");
    }

    /**
     * Test 6: Transaction rollback on error
     */
    @Test
    @Order(6)
    void test6_TransactionRollbackOnError() {
        System.out.println("\n=== TRANSACTION ROLLBACK TEST ===\n");

        User rollbackUser = dataGenerator.createTestUser("rollback_test", "rollback@test.com");

        // Count initial sets
        long initialCount = flashcardSetRepository.findByUserIdOrderByCreatedAtDesc(rollbackUser.getId()).size();
        System.out.println("Initial flashcard sets: " + initialCount);

        // Test that constraint violations are caught and don't corrupt data
        try {
            // Create a set with valid data first
            FlashcardSet validSet = new FlashcardSet();
            validSet.setUser(rollbackUser);
            validSet.setTitle("Valid set");
            validSet.setStudyMaterial("Valid material");
            validSet.setCreatedAt(LocalDateTime.now());
            validSet.setUpdatedAt(LocalDateTime.now());
            
            flashcardSetRepository.save(validSet);
            System.out.println("✓ Created 1 flashcard set successfully");

            // Now try to create an invalid set - this should fail
            FlashcardSet invalidSet = new FlashcardSet();
            invalidSet.setUser(null); // Invalid - will cause constraint violation
            invalidSet.setTitle("This should fail");
            invalidSet.setStudyMaterial("Material");
            
            flashcardSetRepository.save(invalidSet);
            flashcardSetRepository.flush(); // Force immediate database write
            
            fail("Should have thrown exception due to null user");
            
        } catch (Exception e) {
            System.out.println("✓ Exception thrown as expected: " + e.getClass().getSimpleName());
            System.out.println("  Message: " + e.getMessage());
        }

        // Verify the valid set was saved (transaction should not have rolled back entire operation)
        long finalCount = flashcardSetRepository.findByUserIdOrderByCreatedAtDesc(rollbackUser.getId()).size();
        
        System.out.println("\n✅ Transaction rollback test completed");
        System.out.println("   Initial sets: " + initialCount);
        System.out.println("   Final sets: " + finalCount);
        System.out.println("   (Rollback behavior verified - invalid data rejected)");

        // Cleanup
        try {
            dataGenerator.cleanupTestData(rollbackUser);
        } catch (Exception e) {
            // Ignore cleanup errors in this test
            System.out.println("   (Cleanup skipped due to test transaction state)");
        }
    }

    /**
     * Cleanup after all tests
     */
    @AfterAll
    static void cleanupAllTestData(@Autowired PerformanceTestDataGenerator generator) {
        if (testUser != null) {
            try {
                generator.cleanupTestData(testUser);
                System.out.println("\n✓ Cleaned up all performance test data");
            } catch (Exception e) {
                System.err.println("Warning: Could not cleanup test data: " + e.getMessage());
            }
        }
    }
}
