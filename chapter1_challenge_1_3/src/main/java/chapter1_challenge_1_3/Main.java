package chapter1_challenge_1_3;

import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Random rand = new Random();

        int health = 100;
        boolean defeated = false;

        for (int room = 1; room <= 5; room++) {
            System.out.println("Entering room " + room + "...");
            int event = rand.nextInt(3) + 1;

            switch (event) {
                case 1:
                    System.out.println("A trap sprung!");
                    health -= 20;
                    break;
                case 2:
                    System.out.println("You found a healing potion!");
                    health += 15;
                    if (health > 100) health = 100;
                    break;
                case 3:
                    System.out.println("A monster appears!");
                    int monsterNum = rand.nextInt(5) + 1;
                    int guess;
                    do {
                        System.out.print("Guess a number (1-5): ");
                        guess = sc.nextInt();
                    } while (guess != monsterNum);
                    System.out.println("You defeated the monster!");
                    break;
            }

            System.out.println("Health is now: " + health);

            if (health <= 0) {
                System.out.println("You have been defeated in room " + room + ".");
                defeated = true;
                break;
            }
        }

        if (!defeated) {
            System.out.println("You cleared the dungeon! Victorious with " + health + " health!");
        }
    }
}