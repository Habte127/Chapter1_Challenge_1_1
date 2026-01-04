import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.Scanner;
import javax.swing.*;

public class Chapter2Applet extends JPanel implements Runnable, KeyListener {
    // --- Game mode ---
    private static boolean vsComputer = false;

    // Ball
    private int ballX = 200, ballY = 150;
    private int ballDX = 5, ballDY = 5;
    private final int BALL_SIZE = 20;

    // Paddles
    private final int PADDLE_WIDTH = 10, PADDLE_HEIGHT = 70;
    private int player1Y = 120;
    private int player2Y = 120;
    private final int PLAYER1_X = 20;
    private final int PLAYER2_X = 370;

    // Scores
    private int player1Score = 0;
    private int player2Score = 0;

    // High scores
    private int player1HighScore = 0;
    private int player2HighScore = 0;
    private static final String HIGH_SCORE_FILE = "highscore.txt";

    // Key states
    private boolean p1Up = false, p1Down = false;
    private boolean p2Up = false, p2Down = false;

    // Thread
    private Thread thread;
    private boolean running = false;

    public Chapter2Applet() {
        loadHighScores(); // Load high scores
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(400, 300));
        setFocusable(true);
        addKeyListener(this);

        running = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        while (running) {
            // Player 1 movement
            if (p1Up && player1Y > 0) player1Y -= 6;
            if (p1Down && player1Y + PADDLE_HEIGHT < getHeight()) player1Y += 6;

            // Player 2 / Computer movement
            if (vsComputer) {
                if (ballY + BALL_SIZE / 2 < player2Y + PADDLE_HEIGHT / 2) player2Y -= 4;
                if (ballY + BALL_SIZE / 2 > player2Y + PADDLE_HEIGHT / 2) player2Y += 4;
            } else {
                if (p2Up && player2Y > 0) player2Y -= 6;
                if (p2Down && player2Y + PADDLE_HEIGHT < getHeight()) player2Y += 6;
            }

            // Ball movement
            ballX += ballDX;
            ballY += ballDY;

            // Bounce top/bottom
            if (ballY <= 0 || ballY + BALL_SIZE >= getHeight()) ballDY = -ballDY;

            // Paddle collisions
            if (ballX <= PLAYER1_X + PADDLE_WIDTH &&
                ballY + BALL_SIZE >= player1Y &&
                ballY <= player1Y + PADDLE_HEIGHT) {
                ballDX = -ballDX;
                ballX = PLAYER1_X + PADDLE_WIDTH;
            }
            if (ballX + BALL_SIZE >= PLAYER2_X &&
                ballY + BALL_SIZE >= player2Y &&
                ballY <= player2Y + PADDLE_HEIGHT) {
                ballDX = -ballDX;
                ballX = PLAYER2_X - BALL_SIZE;
            }

            // Scoring
            if (ballX < 0) {
                player2Score++;
                updateHighScores();
                resetBall();
            }
            if (ballX > getWidth()) {
                player1Score++;
                updateHighScores();
                resetBall();
            }

            repaint();

            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void resetBall() {
        ballX = getWidth() / 2 - BALL_SIZE / 2;
        ballY = getHeight() / 2 - BALL_SIZE / 2;
        ballDX = -ballDX;
        ballDY = 4 * (Math.random() > 0.5 ? 1 : -1);
    }

    private void updateHighScores() {
        boolean updated = false;
        if (player1Score > player1HighScore) {
            player1HighScore = player1Score;
            updated = true;
        }
        if (player2Score > player2HighScore) {
            player2HighScore = player2Score;
            updated = true;
        }
        if (updated) saveHighScores();
    }
    private void saveHighScores() {
        try (PrintWriter out = new PrintWriter(new FileWriter(HIGH_SCORE_FILE))) {
            out.println(player1HighScore);
            out.println(player2HighScore);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadHighScores() {
        File file = new File(HIGH_SCORE_FILE);
        if (file.exists()) {
            try (Scanner sc = new Scanner(file)) {
                if (sc.hasNextInt()) player1HighScore = sc.nextInt();
                if (sc.hasNextInt()) player2HighScore = sc.nextInt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Ball
        g.setColor(Color.RED);
        g.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);

        // Paddles
        g.setColor(Color.WHITE);
        g.fillRect(PLAYER1_X, player1Y, PADDLE_WIDTH, PADDLE_HEIGHT);
        g.fillRect(PLAYER2_X, player2Y, PADDLE_WIDTH, PADDLE_HEIGHT);

        // Center line
        g.setColor(Color.GRAY);
        for (int i = 0; i < getHeight(); i += 15)
            g.fillRect(getWidth() / 2 - 1, i, 2, 10);

        // Current scores
        g.setFont(new Font("Consolas", Font.BOLD, 16));
        g.setColor(Color.YELLOW);
        g.drawString("Player 1: " + player1Score, 20, 25);
        g.drawString(vsComputer ? "Computer: " + player2Score : "Player 2: " + player2Score,
                     getWidth() - 180, 25);

        // High scores
        g.setFont(new Font("Consolas", Font.PLAIN, 14));
        g.setColor(Color.CYAN);
        g.drawString("High Score P1: " + player1HighScore, 20, getHeight() - 20);
        g.drawString(vsComputer ? "High Score CPU: " + player2HighScore : "High Score P2: " + player2HighScore,
                     getWidth() - 180, getHeight() - 20);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> p1Up = true;
            case KeyEvent.VK_S -> p1Down = true;
            case KeyEvent.VK_UP -> p2Up = true;
            case KeyEvent.VK_DOWN -> p2Down = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> p1Up = false;
            case KeyEvent.VK_S -> p1Down = false;
            case KeyEvent.VK_UP -> p2Up = false;
            case KeyEvent.VK_DOWN -> p2Down = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    // Main
    public static void main(String[] args) {
        String[] options = {"Player vs Computer", "Player vs Player"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "Choose Game Mode:",
                "Pong Game",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        vsComputer = (choice == 0);

        JFrame frame = new JFrame(vsComputer ? "Pong - Player vs Computer" : "Pong - Player vs Player");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new Chapter2Applet());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}