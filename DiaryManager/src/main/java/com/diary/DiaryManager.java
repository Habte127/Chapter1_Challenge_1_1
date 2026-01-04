package com.diary;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

public class DiaryManager {
    private DiaryConfig config;
    private DiaryFileHandler fileHandler;
    
    public DiaryManager() throws IOException {
        this.config = DiaryConfig.loadConfig();
        initializeFileHandler();
    }
    
    private void initializeFileHandler() throws IOException {
        try {
            this.fileHandler = new DiaryFileHandler(
                config.getEntriesDirectory(),
                config.getBackupDirectory()
            );
        } catch (IOException e) {
            System.err.println("Error initializing file handler: " + e.getMessage());
            throw e;
        }
    }
    
    public void saveEntry(DiaryEntry entry) throws IOException {
        fileHandler.saveEntry(entry);
        checkAutoBackup();
    }
    
    public List<String> listEntries() throws IOException {
        return fileHandler.listEntries();
    }
    
    public String readEntry(String filename) throws IOException {
        return fileHandler.readEntry(filename);
    }
    
    public List<DiaryEntry> searchEntries(String keyword) throws IOException {
        config.addSearch(keyword);
        return fileHandler.searchEntries(keyword);
    }
    
    public void createBackup() throws IOException {
        fileHandler.createBackup();
    }
    
    public boolean deleteEntry(String filename) throws IOException {
        return fileHandler.deleteEntry(filename);
    }
    
    public long getTotalEntries() throws IOException {
        return fileHandler.getTotalEntries();
    }
    
    public LocalDateTime extractTimestamp(String filename) {
        return fileHandler.extractTimestampFromFilename(filename);
    }
    
    public void saveConfiguration() {
        config.saveConfig();
    }
    
    // Configuration management methods
    public void setEntriesDirectory(String dir) throws IOException {
        config.setEntriesDirectory(dir);
        saveConfiguration();
        initializeFileHandler();
    }
    
    public void setBackupDirectory(String dir) throws IOException {
        config.setBackupDirectory(dir);
        saveConfiguration();
        initializeFileHandler();
    }
    
    public boolean toggleAutoBackup() {
        boolean current = Boolean.parseBoolean(config.getSetting("autoBackup"));
        config.setSetting("autoBackup", String.valueOf(!current));
        return !current;
    }
    
    public int getPageSize() {
        return Integer.parseInt(config.getSetting("maxEntriesPerPage"));
    }
    
    public List<String> getRecentSearches() {
        return config.getRecentSearches();
    }
    
    public void addRecentSearch(String keyword) {
        config.addSearch(keyword);
    }
    
    public String getEntriesDirectory() {
        return config.getEntriesDirectory();
    }
    
    private void checkAutoBackup() throws IOException {
        if (Boolean.parseBoolean(config.getSetting("autoBackup"))) {
            if (getTotalEntries() % 10 == 0) { // Backup every 10 entries
                System.out.println("Auto-backup triggered...");
                createBackup();
            }
        }
    }
}