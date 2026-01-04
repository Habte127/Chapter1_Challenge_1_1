package com.diary;

import java.io.*;
import java.util.*;

public class DiaryMenu {
    private final Scanner scanner;
    private final DiaryManager diaryManager;
    private boolean running;
    
    public DiaryMenu(Scanner scanner, DiaryManager diaryManager) {
        this.scanner = scanner;
        this.diaryManager = diaryManager;
        this.running = true;
    }
    
    public void displayMainMenu() {
        while (running) {
            System.out.println("\n=== Personal Diary Manager ===");
            System.out.println("1. Write New Entry");
            System.out.println("2. Read Previous Entries");
            System.out.println("3. Search Entries");
            System.out.println("4. Create Backup");
            System.out.println("5. View Statistics");
            System.out.println("6. Settings");
            System.out.println("7. Exit");
            System.out.print("Choose an option (1-7): ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                handleMainMenuChoice(choice);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number!");
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
    
    private void handleMainMenuChoice(int choice) throws IOException {
        switch (choice) {
            case 1 -> writeNewEntry();
            case 2 -> readEntries();
            case 3 -> searchEntries();
            case 4 -> createBackup();
            case 5 -> showStatistics();
            case 6 -> showSettings();
            case 7 -> exitApplication();
            default -> System.out.println("Invalid choice! Please try again.");
        }
    }
    
    private void writeNewEntry() throws IOException {
        System.out.println("\n=== Write New Entry ===");
        System.out.println("Enter your diary entry (type 'END' on a new line to finish):");
        
        StringBuilder content = new StringBuilder();
        String line;
        while (!(line = scanner.nextLine()).equals("END")) {
            content.append(line).append("\n");
        }
        
        if (content.length() == 0) {
            System.out.println("Entry cancelled - no content provided.");
            return;
        }
        
        DiaryEntry entry = new DiaryEntry(content.toString());
        diaryManager.saveEntry(entry);
        System.out.println("Entry saved successfully: " + entry.getFilename());
    }
    
    private void readEntries() throws IOException {
        System.out.println("\n=== Read Previous Entries ===");
        List<String> entries = diaryManager.listEntries();
        
        if (entries.isEmpty()) {
            System.out.println("No diary entries found.");
            return;
        }
        
        int choice = displayPaginatedList(entries, "Select an entry to read (0 to return): ");
        
        if (choice == 0) return;
        
        if (choice > 0 && choice <= entries.size()) {
            String filename = entries.get(choice - 1);
            String content = diaryManager.readEntry(filename);
            System.out.println("\n--- Entry: " + filename + " ---");
            System.out.println(content);
            System.out.println("--- End of Entry ---");
            
            System.out.println("\nOptions: [D]elete this entry, [E]dit, [R]eturn");
            String option = scanner.nextLine().toLowerCase();
            handleEntryOptions(option, filename);
        }
    }
    
    private void handleEntryOptions(String option, String filename) throws IOException {
        switch (option) {
            case "d" -> {
                System.out.print("Are you sure you want to delete this entry? (y/n): ");
                if (scanner.nextLine().equalsIgnoreCase("y")) {
                    if (diaryManager.deleteEntry(filename)) {
                        System.out.println("Entry deleted successfully.");
                    }
                }
            }
            case "e" -> editEntry(filename);
        }
    }
    
    private void editEntry(String filename) throws IOException {
        System.out.println("\n=== Edit Entry ===");
        String currentContent = diaryManager.readEntry(filename);
        System.out.println("Current content:\n" + currentContent);
        System.out.println("\nEnter new content (type 'END' on a new line to finish):");
        
        StringBuilder content = new StringBuilder();
        String line;
        while (!(line = scanner.nextLine()).equals("END")) {
            content.append(line).append("\n");
        }
        
        if (content.length() > 0) {
            DiaryEntry entry = new DiaryEntry(diaryManager.extractTimestamp(filename), content.toString());
            diaryManager.saveEntry(entry);
            if (!entry.getFilename().equals(filename)) {
                diaryManager.deleteEntry(filename);
            }
            System.out.println("Entry updated successfully.");
        }
    }
    
    private void searchEntries() throws IOException {
        System.out.println("\n=== Search Entries ===");
        System.out.print("Enter search keyword: ");
        String keyword = scanner.nextLine().trim();
        
        if (keyword.isEmpty()) {
            System.out.println("Search keyword cannot be empty!");
            return;
        }
        
        diaryManager.addRecentSearch(keyword);
        List<DiaryEntry> results = diaryManager.searchEntries(keyword);
        
        if (results.isEmpty()) {
            System.out.println("No entries found containing: " + keyword);
        } else {
            System.out.println("\nFound " + results.size() + " entries:");
            for (int i = 0; i < results.size(); i++) {
                System.out.println((i + 1) + ". " + results.get(i));
            }
        }
    }
    
    private void createBackup() throws IOException {
        System.out.println("\n=== Create Backup ===");
        System.out.print("Are you sure you want to create a backup? (y/n): ");
        if (scanner.nextLine().equalsIgnoreCase("y")) {
            diaryManager.createBackup();
        }
    }
    
    private void showStatistics() throws IOException {
        System.out.println("\n=== Diary Statistics ===");
        System.out.println("Total entries: " + diaryManager.getTotalEntries());
        System.out.println("Entries directory: " + diaryManager.getEntriesDirectory());
        System.out.println("Recent searches: " + diaryManager.getRecentSearches());
    }
    
    private void showSettings() {
        System.out.println("\n=== Settings ===");
        System.out.println("1. Change entries directory");
        System.out.println("2. Change backup directory");
        System.out.println("3. Toggle auto-backup");
        System.out.println("4. View recent searches");
        System.out.println("5. Back to main menu");
        System.out.print("Choose an option (1-5): ");
        
        try {
            int choice = Integer.parseInt(scanner.nextLine());
            handleSettingsChoice(choice);
        } catch (NumberFormatException | IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private void handleSettingsChoice(int choice) throws IOException {
        switch (choice) {
            case 1 -> {
                System.out.print("Enter new entries directory: ");
                String dir = scanner.nextLine();
                diaryManager.setEntriesDirectory(dir);
            }
            case 2 -> {
                System.out.print("Enter new backup directory: ");
                String dir = scanner.nextLine();
                diaryManager.setBackupDirectory(dir);
            }
            case 3 -> {
                boolean autoBackup = diaryManager.toggleAutoBackup();
                System.out.println("Auto-backup: " + (autoBackup ? "ENABLED" : "DISABLED"));
            }
            case 4 -> {
                System.out.println("\nRecent searches:");
                for (String search : diaryManager.getRecentSearches()) {
                    System.out.println("- " + search);
                }
            }
        }
    }
    
    private int displayPaginatedList(List<String> items, String prompt) throws IOException {
        int pageSize = diaryManager.getPageSize();
        int totalPages = (int) Math.ceil((double) items.size() / pageSize);
        int currentPage = 0;
        
        while (currentPage < totalPages) {
            int start = currentPage * pageSize;
            int end = Math.min(start + pageSize, items.size());
            
            System.out.println("\nPage " + (currentPage + 1) + " of " + totalPages);
            System.out.println("---");
            
            for (int i = start; i < end; i++) {
                System.out.println((i + 1) + ". " + items.get(i));
            }
            
            System.out.println("\n[N]ext page, [P]revious page, " + prompt);
            System.out.print("Your choice: ");
            String choice = scanner.nextLine().toLowerCase();
            
            if (choice.equals("n") && currentPage < totalPages - 1) {
                currentPage++;
            } else if (choice.equals("p") && currentPage > 0) {
                currentPage--;
            } else {
                try {
                    int selection = Integer.parseInt(choice);
                    if (selection == 0) return 0;
                    if (selection >= 1 && selection <= items.size()) {
                        return selection;
                    }
                } catch (NumberFormatException e) {
                    // Not a number, continue pagination
                }
            }
        }
        return 0;
    }
    
    private void exitApplication() {
        System.out.print("\nSave configuration before exiting? (y/n): ");
        if (scanner.nextLine().equalsIgnoreCase("y")) {
            diaryManager.saveConfiguration();
        }
        System.out.println("Thank you for using Personal Diary Manager!");
        running = false;
    }
}