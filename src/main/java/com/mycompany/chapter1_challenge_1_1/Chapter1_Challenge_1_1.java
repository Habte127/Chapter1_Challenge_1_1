package chapter1_challenge_1_1;

import java.util.Scanner;

public class Main{
    
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter a positive integer: ");
        int number = sc.nextInt();

        int digits = (int) Math.log10(number) + 1;
        int lastDigit = number % 10;
        int firstDigit = (int) (number / Math.pow(10, digits - 1));
        int secondDigit = (number / (int)Math.pow(10, digits - 2)) % 10;
        int secondLastDigit = (number / 10) % 10;

        int product = firstDigit * lastDigit;
        int sum = secondDigit + secondLastDigit;
        String code = product + "" + sum;

        System.out.println("The decrypted code is: " + code);
    }
}