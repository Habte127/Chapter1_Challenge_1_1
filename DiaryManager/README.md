# Personal Diary Manager

A command-line diary application that allows users to write entries, read previous entries, and manage diary files.

## Features

- **Write Mode**: Create new diary entries with automatic timestamping
- **Read Mode**: Browse and read previous entries with pagination
- **Search Functionality**: Search entries by keywords
- **Entry Management**: Edit and delete existing entries
- **Backup System**: Create ZIP backups of all entries
- **Configuration**: Persistent settings and recent searches
- **Auto-backup**: Optional automatic backup every 10 entries

## Requirements

- Java 17 or higher
- Maven 3.6+ (for building)

## Building and Running

### Using Maven:
```bash
# Clone and navigate to project directory
cd Chapter4_Challenge_DiaryManager

# Build the project
mvn clean compile

# Package as executable JAR
mvn package

# Run the application
java -jar target/chapter4-challenge-diarymanager-1.0.0.jar