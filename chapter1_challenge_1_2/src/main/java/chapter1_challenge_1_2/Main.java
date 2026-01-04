package chapter1_challenge_1_2;

public class Main {
    public static void main(String[] args) {
        String[] winningNumbers = {"12-34-56-78-90", "33-44-11-66-22", "01-02-03-04-05"};

        double highestAverage = 0;
        String bestNumber = "";

        for (String num : winningNumbers) {
            String clean = num.replace("-", "");
            char[] digits = clean.toCharArray();
            int[] values = new int[digits.length];
            int sum = 0;

            for (int i = 0; i < digits.length; i++) {
                values[i] = Character.getNumericValue(digits[i]);
                sum += values[i];
            }

            double avg = (double) sum / digits.length;

            System.out.println("Analyzing: " + num);
            System.out.println("Digit Sum: " + sum + ", Digit Average: " + avg);

            if (avg > highestAverage) {
                highestAverage = avg;
                bestNumber = num;
            }
        }

        System.out.println("The winning number with the highest average is: " +
                bestNumber + " with an average of " + highestAverage);
    }
}