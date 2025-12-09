# AI Usage Documentation - Ngo Hung

## Developer Information
- **Name:** Ngo Hung (Anh Ngo)
- **Role:** Database & Backend Developer
- **Period:** October - December 2025
- **Project:** AI Study Assistant - Trinity College Dublin

## Generative AI Tools Used

### Primary Tools
1. **GitHub Copilot** (VS Code Extension)
   - Model: GPT-4 based code completion
   - Usage: Code completion, boilerplate generation, test scaffolding

2. **Claude 3.5 Sonnet** (via GitHub Copilot Chat)
   - Model: Anthropic Claude 3.5 Sonnet
   - Usage: Architecture discussions, debugging, complex problem-solving

## Overall Strategy

### 1. Database Design & Implementation (Jobs 1, 2, 3, 4, 5)
**Approach:**
- Used AI to understand Spring Data JPA repository patterns and query methods
- Asked for explanations of `@OneToMany`, `@ManyToOne` relationships and cascade types
- Requested clarification on naming conventions for derived query methods

**Main Prompts:**
- "Explain the difference between CascadeType.ALL and orphanRemoval in JPA"
- "How to write a Spring Data repository method to find by user and order by created date descending?"
- "What's the best practice for bidirectional relationships in JPA entities?"

**What Worked Well:**
- AI explained complex JPA concepts clearly with code examples
- Helped understand the difference between `findByUserIdOrderByCreatedAtDesc` vs manual query writing
- Provided best practices for entity relationship mapping

**Challenges:**
- Initial suggestions sometimes used deprecated Hibernate annotations
- Had to verify foreign key constraint syntax for MySQL specifically
- AI suggested `@Transactional` in places where it caused test issues - had to debug manually

### 2. Automated Testing (Job 7)
**Approach:**
- Used AI to understand JUnit 5 testing patterns and Spring Boot test configuration
- Asked for help structuring repository tests with real database vs mocks
- Requested explanations of `@SpringBootTest`, `@AutoConfigureTestDatabase`, `@Transactional`

**Main Prompts:**
- "How to write integration tests for Spring Data repositories with real MySQL database?"
- "Explain the difference between @DataJpaTest and @SpringBootTest for repository testing"
- "Why do my tests fail with LazyInitializationException and how to fix it?"

**What Worked Well:**
- AI helped understand test isolation and transaction management
- Provided clear examples of test setup with `@BeforeEach` and proper assertions
- Explained when to use `flush()` to ensure data persistence

**What Didn't Work:**
- AI suggested `@Transactional` on test methods which caused data to rollback unexpectedly
- Had to manually discover the timing issues with `LocalDateTime.now()` in ordered tests
- Required multiple iterations to understand proper use of `@Order` with sequential tests

### 3. Performance Optimization (Job 9)
**Approach:**
- Used AI to understand MySQL indexing strategies and composite indexes
- Asked for explanations of query execution plans and index selection
- Requested help with performance test design and concurrent access testing

**Main Prompts:**
- "Explain composite indexes vs single column indexes for MySQL query: SELECT * FROM flashcard_sets WHERE user_id = ? ORDER BY created_at DESC"
- "How to write performance tests in JUnit that measure query execution time?"
- "What's the best way to test concurrent database access in Spring Boot tests?"
- "Why does my test fail with 'Table doesn't exist' when using Hibernate auto-ddl?"

**What Worked Well:**
- AI explained index strategies clearly with specific examples for my use case
- Helped design the `PerformanceTestDataGenerator` utility class structure
- Provided guidance on `ExecutorService` and `CountDownLatch` for concurrency testing

**Challenges:**
- MySQL syntax differences: AI suggested `CREATE INDEX IF NOT EXISTS` which doesn't work in MySQL 5.7
- Had to manually discover that Hibernate's `CREATE-DROP` strategy was causing test failures
- Transaction boundary issues required manual debugging (AI suggestions didn't work immediately)

### 4. Debugging & Problem Solving
**Approach:**
- Shared error stacktraces with AI to get explanations and potential solutions
- Asked for clarification on Spring Boot configuration issues
- Used AI to understand complex error messages

**Main Prompts:**
- "LazyInitializationException: failed to lazily initialize a collection - what does this mean?"
- "AssertionFailedError: expected: [Second Quiz] but was: [First Quiz] - why is ordering wrong?"
- "SQLSyntaxErrorException: Table doesn't exist - but I see it in my database?"

**What Worked Well:**
- AI quickly identified common Spring/Hibernate pitfalls
- Explained root causes of errors clearly
- Suggested multiple solutions to try

**What Required Manual Work:**
- Timing-based test failures needed manual debugging with `System.out.println`
- Database state issues required understanding of test execution order
- Had to verify AI suggestions against MySQL documentation

## Reflections

### Effective Use Cases
1. **Learning new frameworks:** AI excels at explaining Spring Boot, JPA, JUnit concepts
2. **Code patterns:** Quick access to standard patterns (repository methods, test setup)
3. **Error explanations:** Fast understanding of complex stacktraces
4. **Best practices:** Learning industry standards for database design and testing

### Less Effective Use Cases
1. **Version-specific syntax:** AI sometimes suggests syntax for newer/older versions
2. **Complex debugging:** Multi-layered issues (transactions + timing + lazy loading) required manual investigation
3. **Database-specific features:** MySQL vs PostgreSQL vs H2 differences not always accurate

### Key Learnings
- **Always verify AI suggestions** against official documentation (Spring, Hibernate, MySQL)
- **Understand the "why"** - don't just copy-paste, ask for explanations
- **Iterate and test** - AI's first suggestion often needs refinement
- **Use AI as a teacher** - best for learning concepts, not just getting code
- **Manual debugging still essential** - especially for integration issues

### Academic Integrity
All code was written by me with AI as an educational tool. I understood each piece of code before implementing it, tested thoroughly, and made necessary adjustments. AI was used for learning and explanation, not for direct code generation without understanding.

---

## References
- Spring Data JPA Documentation: https://spring.io/projects/spring-data-jpa
- MySQL 8.0 Reference Manual: https://dev.mysql.com/doc/refman/8.0/en/
- JUnit 5 User Guide: https://junit.org/junit5/docs/current/user-guide/

---