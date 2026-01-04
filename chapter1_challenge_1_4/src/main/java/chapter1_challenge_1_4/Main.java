package chapter1_challenge_1_4;

import java.io.*;

public class Main {
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new FileReader("config.txt"));

            int version = Integer.parseInt(br.readLine());
            if (version < 2) {
                throw new Exception("Config version too old!");
            }

            String path = br.readLine();
            File file = new File(path);
            if (!file.exists()) {
                throw new IOException("File path does not exist: " + path);
            }

            System.out.println("Config loaded successfully!");
            br.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("Error: Config file not found!");
        }
        catch (NumberFormatException e) {
            System.out.println("Error: Invalid number format in config file.");
        }
        catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        finally {
            System.out.println("Config read attempt finished.");
        }
    }
}

package chapter1_challenge_1_4;

import java.io.*;

public class Main {
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new FileReader("config.txt"));

            int version = Integer.parseInt(br.readLine());
            if (version < 2) {
                throw new Exception("Config version too old!");
            }

            String path = br.readLine();
            File file = new File(path);
            if (!file.exists()) {
                throw new IOException("File path does not exist: " + path);
            }

            System.out.println("Config loaded successfully!");
            br.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("Error: Config file not found!");
        }
        catch (NumberFormatException e) {
            System.out.println("Error: Invalid number format in config file.");
        }
        catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        finally {
            System.out.println("Config read attempt finished.");
        }
    }
}