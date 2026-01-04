package com.diary;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.*;

public class DiaryFileHandler {
    private final Path entriesDir;
    private final Path backupDir;
    
    public DiaryFileHandler(String entriesDir, String backupDir) throws IOException {
        this.entriesDir = Paths.get(entriesDir);
        this.backupDir = Paths.get(backupDir);
        createDirectories();
    }
    
    private void createDirectories() throws IOException {
        Files.createDirectories(entriesDir);
        Files.createDirectories(backupDir);
    }
    
    public void saveEntry(DiaryEntry entry) throws IOException {
        Path filePath = entriesDir.resolve(entry.getFilename());
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write(entry.getContent());
        }
    }
    
    public String readEntry(String filename) throws IOException {
        Path filePath = entriesDir.resolve(filename);
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("Entry not found: " + filename);
        }
        return Files.readString(filePath, StandardCharsets.UTF_8);
    }
    
    public List<String> listEntries() throws IOException {
        List<String> entries = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(entriesDir, "diary_*.txt")) {
            for (Path entry : stream) {
                entries.add(entry.getFileName().toString());
            }
        }
        entries.sort(Collections.reverseOrder());
        return entries;
    }
    
    public List<DiaryEntry> searchEntries(String keyword) throws IOException {
        List<DiaryEntry> results = new ArrayList<>();
        List<String> filenames = listEntries();
        
        for (String filename : filenames) {
            try {
                String content = readEntry(filename);
                if (content.toLowerCase().contains(keyword.toLowerCase())) {
                    LocalDateTime timestamp = extractTimestampFromFilename(filename);
                    results.add(new DiaryEntry(timestamp, content));
                }
            } catch (IOException e) {
                System.err.println("Warning: Could not read file " + filename + ": " + e.getMessage());
            }
        }
        
        return results;
    }
    
    LocalDateTime extractTimestampFromFilename(String filename) {
        String timestampStr = filename
            .replace("diary_", "")
            .replace(".txt", "");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
        return LocalDateTime.parse(timestampStr, formatter);
    }
    
    public void createBackup() throws IOException {
        String backupName = "diary_backup_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + 
            ".zip";
        
        Path backupPath = backupDir.resolve(backupName);
        
        try (ZipOutputStream zos = new ZipOutputStream(
                new FileOutputStream(backupPath.toFile()))) {
            
            List<String> entries = listEntries();
            for (String entry : entries) {
                Path filePath = entriesDir.resolve(entry);
                zos.putNextEntry(new ZipEntry(entry));
                Files.copy(filePath, zos);
                zos.closeEntry();
            }
            
            // Also backup config if exists
            Path configPath = Paths.get("diary_config.ser");
            if (Files.exists(configPath)) {
                zos.putNextEntry(new ZipEntry("diary_config.ser"));
                Files.copy(configPath, zos);
                zos.closeEntry();
            }
        }
        
        System.out.println("Backup created: " + backupPath);
    }
    
    public boolean deleteEntry(String filename) throws IOException {
        Path filePath = entriesDir.resolve(filename);
        return Files.deleteIfExists(filePath);
    }
    
    public long getTotalEntries() throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(entriesDir, "diary_*.txt")) {
            long count = 0;
            for (Path ignored : stream) {
                count++;
            }
            return count;
        }
    }
}