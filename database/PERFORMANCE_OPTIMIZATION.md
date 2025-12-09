# Database Performance Optimization - Job 9

## Overview
This document explains how to add database indexes for optimal query performance.

## Prerequisites
- MySQL server running on localhost:3306
- Database `aichat_db` exists
- User `aichat_user` has permissions

## Adding Indexes

### Method 1: Using MySQL Command Line
```bash
mysql -u aichat_user -paichat_pass aichat_db < database/add-indexes.sql
```

### Method 2: Using MySQL Workbench
1. Open MySQL Workbench
2. Connect to localhost:3306
3. Select database `aichat_db`
4. Open `database/add-indexes.sql`
5. Execute the script

### Method 3: Manual SQL Commands
Copy and paste these commands into MySQL console:

```sql
USE aichat_db;

-- FLASHCARD_SETS INDEXES
CREATE INDEX IF NOT EXISTS idx_flashcard_sets_user_id ON flashcard_sets(user_id);
CREATE INDEX IF NOT EXISTS idx_flashcard_sets_created_at ON flashcard_sets(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_flashcard_sets_user_created ON flashcard_sets(user_id, created_at DESC);

-- QUIZ_SETS INDEXES
CREATE INDEX IF NOT EXISTS idx_quiz_sets_user_id ON quiz_sets(user_id);
CREATE INDEX IF NOT EXISTS idx_quiz_sets_created_at ON quiz_sets(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_quiz_sets_user_created ON quiz_sets(user_id, created_at DESC);

-- FLASHCARDS INDEXES
CREATE INDEX IF NOT EXISTS idx_flashcards_set_id ON flashcards(set_id);
CREATE INDEX IF NOT EXISTS idx_flashcards_set_position ON flashcards(set_id, position);

-- QUIZ_QUESTIONS INDEXES
CREATE INDEX IF NOT EXISTS idx_quiz_questions_set_id ON quiz_questions(quiz_set_id);
CREATE INDEX IF NOT EXISTS idx_quiz_questions_set_position ON quiz_questions(quiz_set_id, position);

-- USERS INDEXES
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- ANALYZE TABLES
ANALYZE TABLE flashcard_sets;
ANALYZE TABLE quiz_sets;
ANALYZE TABLE flashcards;
ANALYZE TABLE quiz_questions;
ANALYZE TABLE users;
```

## Verifying Indexes

After adding indexes, verify they were created:

```sql
-- Show all indexes on flashcard_sets
SHOW INDEX FROM flashcard_sets;

-- Show all indexes on quiz_sets
SHOW INDEX FROM quiz_sets;

-- Show all indexes on flashcards
SHOW INDEX FROM flashcards;

-- Show all indexes on quiz_questions
SHOW INDEX FROM quiz_questions;

-- Show all indexes on users
SHOW INDEX FROM users;
```

### Expected Output for flashcard_sets:
```
Table           | Non_unique | Key_name                          | Column_name | Index_type
----------------|------------|-----------------------------------|-------------|------------
flashcard_sets  | 0          | PRIMARY                           | id          | BTREE
flashcard_sets  | 1          | idx_flashcard_sets_user_id        | user_id     | BTREE
flashcard_sets  | 1          | idx_flashcard_sets_created_at     | created_at  | BTREE
flashcard_sets  | 1          | idx_flashcard_sets_user_created   | user_id     | BTREE
flashcard_sets  | 1          | idx_flashcard_sets_user_created   | created_at  | BTREE
flashcard_sets  | 1          | fk_flashcard_sets_user            | user_id     | BTREE
```

## Running Performance Tests

After adding indexes, run performance tests to verify improvements:

```bash
# Run only performance tests
mvn test -Dtest=PerformanceTest

# Run all tests
mvn clean test
```

## Expected Performance Improvements

### Before Indexes:
- History query (50+ sets): ~500-1000ms
- Get by ID query: ~200-500ms
- Login query: ~50-100ms

### After Indexes:
- History query (50+ sets): < 100ms (90% improvement)
- Get by ID query: < 50ms (75% improvement)
- Login query: < 10ms (80% improvement)

## Index Purpose Explanation

### Composite Indexes
- `idx_flashcard_sets_user_created (user_id, created_at DESC)`
  - **Query:** `SELECT * FROM flashcard_sets WHERE user_id = ? ORDER BY created_at DESC`
  - **Benefit:** Single index scan handles both filtering AND sorting
  - **Used by:** GET /api/flashcards/history

### Single Column Indexes
- `idx_users_username (username)`
  - **Query:** `SELECT * FROM users WHERE username = ?`
  - **Benefit:** Fast user lookup during authentication
  - **Used by:** Every authenticated API request

### Position Indexes
- `idx_flashcards_set_position (set_id, position)`
  - **Query:** `SELECT * FROM flashcards WHERE set_id = ? ORDER BY position`
  - **Benefit:** Flashcards retrieved in correct order without additional sorting
  - **Used by:** GET /api/flashcards/{id}

## Troubleshooting

### Index Already Exists Error
If you see "Duplicate key name", indexes already exist. This is OK - they won't be recreated.

### Permission Denied
Ensure `aichat_user` has INDEX privileges:
```sql
GRANT INDEX ON aichat_db.* TO 'aichat_user'@'localhost';
FLUSH PRIVILEGES;
```

### Performance Not Improved
1. Verify indexes exist: `SHOW INDEX FROM tablename`
2. Run ANALYZE TABLE to update statistics
3. Clear query cache: `RESET QUERY CACHE;`
4. Restart MySQL server

## Success Criteria

✅ All indexes created successfully  
✅ SHOW INDEX shows expected indexes  
✅ History endpoint < 1 second with 50+ sets  
✅ Get by ID < 500ms  
✅ No data corruption with concurrent requests  
✅ Transaction rollback on errors works correctly  

## Performance Test Results

Run `PerformanceTest.java` to validate:
- ✅ Test 1: Generate 50+ test sets
- ✅ Test 2: Flashcard history < 1s
- ✅ Test 3: Get by ID < 500ms
- ✅ Test 4: Quiz history < 1s
- ✅ Test 5: Concurrent requests (no corruption)
- ✅ Test 6: Transaction rollback

All tests must pass for Job 9 completion.
