package com.diary;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DiaryConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String CONFIG_FILE = "diary_config.ser";
    
    private String entriesDirectory;
    private String backupDirectory;
    private List<String> recentSearches;
    private Properties settings;
    
    public DiaryConfig() {
        this.entriesDirectory = "entries";
        this.backupDirectory = "backups";
        this.recentSearches = new ArrayList<>();
        this.settings = new Properties();
        initializeSettings();
    }
    
    private void initializeSettings() {
        settings.setProperty("autoBackup", "false");
        settings.setProperty("maxEntriesPerPage", "10");
        settings.setProperty("defaultEncoding", "UTF-8");
    }
    
    public void saveConfig() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(CONFIG_FILE))) {
            oos.writeObject(this);
        } catch (IOException e) {
            System.err.println("Warning: Could not save configuration: " + e.getMessage());
        }
    }
    
    public static DiaryConfig loadConfig() {
        Path configPath = Paths.get(CONFIG_FILE);
        if (Files.exists(configPath)) {
            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(CONFIG_FILE))) {
                return (DiaryConfig) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Warning: Could not load configuration, using defaults: " + e.getMessage());
            }
        }
        return new DiaryConfig();
    }
    
    // Getters and Setters
    public String getEntriesDirectory() { return entriesDirectory; }
    public void setEntriesDirectory(String entriesDirectory) { 
        this.entriesDirectory = entriesDirectory; 
    }
    
    public String getBackupDirectory() { return backupDirectory; }
    public void setBackupDirectory(String backupDirectory) { 
        this.backupDirectory = backupDirectory; 
    }
    
    public List<String> getRecentSearches() { return recentSearches; }
    
    public void addSearch(String keyword) {
        recentSearches.remove(keyword);
        recentSearches.add(0, keyword);
        if (recentSearches.size() > 10) {
            recentSearches.remove(recentSearches.size() - 1);
        }
    }
    
    public String getSetting(String key) {
        return settings.getProperty(key);
    }
    
    public void setSetting(String key, String value) {
        settings.setProperty(key, value);
    }
}