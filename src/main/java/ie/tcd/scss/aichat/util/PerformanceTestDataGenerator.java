package ie.tcd.scss.aichat.util;

import ie.tcd.scss.aichat.model.*;
import ie.tcd.scss.aichat.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility class for performance testing
 * Generates large amounts of test data for performance validation
 */
@Component
public class PerformanceTestDataGenerator {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FlashcardSetRepository flashcardSetRepository;

    @Autowired
    private QuizSetRepository quizSetRepository;

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    private final Random random = new Random();

    /**
     * Create a test user for performance testing
     */
    @Transactional
    public User createTestUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash("$2a$10$test.hashed.password.for.performance.testing");
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Generate specified number of flashcard sets for a user
     * Each set contains 5-10 flashcards
     * 
     * @param user User to create sets for
     * @param count Number of sets to create
     * @return List of created flashcard sets
     */
    @Transactional
    public List<FlashcardSet> generateFlashcardSets(User user, int count) {
        List<FlashcardSet> sets = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            FlashcardSet set = new FlashcardSet();
            set.setUser(user);
            set.setTitle("Performance Test Flashcard Set #" + i);
            set.setStudyMaterial(generateStudyMaterial(i));
            set.setCreatedAt(LocalDateTime.now().minusDays(count - i)); // Spread over time
            set.setUpdatedAt(set.getCreatedAt());

            // Create 5-10 flashcards per set
            int cardCount = 5 + random.nextInt(6);
            for (int j = 1; j <= cardCount; j++) {
                Flashcard card = new Flashcard();
                card.setFlashcardSet(set);
                card.setQuestion("Question " + j + " for set " + i + ": " + generateQuestion(i, j));
                card.setAnswer("Answer " + j + " for set " + i + ": " + generateAnswer(i, j));
                card.setPosition(j);
                set.getFlashcards().add(card);
            }

            sets.add(flashcardSetRepository.save(set));
        }

        return sets;
    }

    /**
     * Generate specified number of quiz sets for a user
     * Each set contains 3-5 questions
     * 
     * @param user User to create sets for
     * @param count Number of sets to create
     * @return List of created quiz sets
     */
    @Transactional
    public List<QuizSet> generateQuizSets(User user, int count) {
        List<QuizSet> sets = new ArrayList<>();
        String[] difficulties = {"EASY", "MEDIUM", "HARD"};
        
        for (int i = 1; i <= count; i++) {
            QuizSet set = new QuizSet();
            set.setUser(user);
            set.setTitle("Performance Test Quiz Set #" + i);
            set.setStudyMaterial(generateStudyMaterial(i));
            set.setDifficulty(difficulties[i % 3]);
            set.setCreatedAt(LocalDateTime.now().minusDays(count - i)); // Spread over time
            set.setUpdatedAt(set.getCreatedAt());

            // Create 3-5 questions per set
            int questionCount = 3 + random.nextInt(3);
            for (int j = 1; j <= questionCount; j++) {
                QuizQuestion question = new QuizQuestion();
                question.setQuizSet(set);
                question.setQuestion("Question " + j + " for quiz " + i + ": " + generateQuestion(i, j));
                question.setOptionA("Option A: " + generateOption(i, j, "A"));
                question.setOptionB("Option B: " + generateOption(i, j, "B"));
                question.setOptionC("Option C: " + generateOption(i, j, "C"));
                question.setOptionD("Option D: " + generateOption(i, j, "D"));
                question.setCorrectAnswer(generateCorrectAnswer());
                question.setExplanation("Explanation for question " + j + ": " + generateExplanation(i, j));
                question.setPosition(j);
                set.getQuestions().add(question);
            }

            sets.add(quizSetRepository.save(set));
        }

        return sets;
    }

    /**
     * Clean up test data for a user
     */
    @Transactional
    public void cleanupTestData(User user) {
        // Delete all flashcard sets (cascade will delete flashcards)
        List<FlashcardSet> flashcardSets = flashcardSetRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        flashcardSetRepository.deleteAll(flashcardSets);

        // Delete all quiz sets (cascade will delete questions)
        List<QuizSet> quizSets = quizSetRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        quizSetRepository.deleteAll(quizSets);

        // Delete user
        userRepository.delete(user);
    }

    // Helper methods to generate realistic test data

    private String generateStudyMaterial(int setNumber) {
        String[] topics = {
            "Computer Science fundamentals",
            "Database management systems",
            "Software engineering principles",
            "Web development technologies",
            "Data structures and algorithms"
        };
        
        return "Study material for " + topics[setNumber % topics.length] + 
               ". This is test set number " + setNumber + 
               ". Contains important concepts and definitions for learning and practice.";
    }

    private String generateQuestion(int setId, int questionId) {
        String[] questionTypes = {
            "What is the definition of",
            "How does one implement",
            "What are the benefits of",
            "When should you use",
            "Why is it important to understand"
        };
        
        return questionTypes[(setId + questionId) % questionTypes.length] + 
               " concept #" + setId + "-" + questionId + "?";
    }

    private String generateAnswer(int setId, int answerId) {
        return "The answer to question " + answerId + " in set " + setId + 
               " involves understanding key principles and applying best practices. " +
               "This concept is fundamental to the topic being studied.";
    }

    private String generateOption(int setId, int questionId, String optionLabel) {
        return "Choice " + optionLabel + " for question " + questionId + 
               " in set " + setId + " - this is a plausible answer option";
    }

    private String generateCorrectAnswer() {
        String[] options = {"A", "B", "C", "D"};
        return options[random.nextInt(4)];
    }

    private String generateExplanation(int setId, int questionId) {
        return "This explanation clarifies why the correct answer is correct for question " + 
               questionId + " in set " + setId + ". " +
               "Understanding this concept is essential for mastering the topic.";
    }

    /**
     * Get statistics about generated data
     */
    public String getStatistics(User user) {
        List<FlashcardSet> flashcardSets = flashcardSetRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<QuizSet> quizSets = quizSetRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        
        // Count all flashcards and questions (simple approach to avoid lazy loading)
        long totalFlashcards = flashcardRepository.count();
        long totalQuestions = quizQuestionRepository.count();
        
        return String.format(
            "User: %s\n" +
            "Flashcard Sets: %d (Total Flashcards: %d)\n" +
            "Quiz Sets: %d (Total Questions: %d)\n" +
            "Total Records: %d",
            user.getUsername(),
            flashcardSets.size(),
            totalFlashcards,
            quizSets.size(),
            totalQuestions,
            flashcardSets.size() + quizSets.size() + totalFlashcards + totalQuestions
        );
    }
}
