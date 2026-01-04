package com.diary;

import java.io.*;
import java.util.*;

public class DiaryApp {
    public static void main(String[] args) {
        System.out.println("=== Personal Diary Manager ===");
        System.out.println("Initializing application...");
        
        try {
            DiaryManager diaryManager = new DiaryManager();
            Scanner scanner = new Scanner(System.in);
            DiaryMenu menu = new DiaryMenu(scanner, diaryManager);
            
            // Handle Ctrl+C gracefully
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n\nApplication is shutting down...");
                diaryManager.saveConfiguration();
                scanner.close();
            }));
            
            menu.displayMainMenu();
            
        } catch (IOException e) {
            System.err.println("Fatal error initializing application: " + e.getMessage());
            System.err.println("Please check directory permissions and try again.");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}