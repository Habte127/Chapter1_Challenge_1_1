package com.diary;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class DiaryEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FILENAME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
    private static final DateTimeFormatter DISPLAY_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final LocalDateTime timestamp;
    private String content;
    private String filename;
    
    public DiaryEntry(String content) {
        this.timestamp = LocalDateTime.now();
        this.content = content;
        this.filename = generateFilename();
    }
    
    public DiaryEntry(LocalDateTime timestamp, String content) {
        this.timestamp = timestamp;
        this.content = content;
        this.filename = generateFilename();
    }
    
    private String generateFilename() {
        return "diary_" + timestamp.format(FILENAME_FORMATTER) + ".txt";
    }
    
    // Getters
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getContent() { return content; }
    public String getFilename() { return filename; }
    public String getFormattedTimestamp() {
        return timestamp.format(DISPLAY_FORMATTER);
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiaryEntry that = (DiaryEntry) o;
        return Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(content, that.content);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(timestamp, content);
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s", 
            getFormattedTimestamp(), 
            content.substring(0, Math.min(50, content.length())) + 
            (content.length() > 50 ? "..." : ""));
    }
}