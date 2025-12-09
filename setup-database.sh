## For easier database setup
#!/bin/bash

echo "Setting up MySQL database for AI Chat..."

# Step 1: Create database and user
sudo mysql << EOF
CREATE DATABASE IF NOT EXISTS aichat_db;
CREATE USER IF NOT EXISTS 'aichat_user'@'localhost' IDENTIFIED BY 'aichat_pass';
GRANT ALL PRIVILEGES ON aichat_db.* TO 'aichat_user'@'localhost';
FLUSH PRIVILEGES;
SELECT 'Database and user created!' AS '';
EOF

echo "✅ Database and user setup complete!"

# Step 2: Create tables from schema
echo "Creating database tables..."

if [ -f "database/create-tables.sql" ]; then
    mysql -u aichat_user -paichat_pass < database/create-tables.sql 2>&1 | grep -v "Using a password on the command line"
    echo "✅ Database tables created!"
else
    echo "⚠️  Warning: database/create-tables.sql not found. Tables will be created by Hibernate."
fi

# Step 3: Apply performance indexes (indexes are already in create-tables.sql)
echo ""
echo "✅ Database setup complete with performance indexes!"
echo "You can now run: ./mvnw spring-boot:run"