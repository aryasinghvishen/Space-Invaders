import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class SpaceInvaders extends JPanel implements ActionListener, KeyListener {
    
    // Game constants
    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;
    private static final int PLAYER_SIZE = 30;
    private static final int BULLET_SIZE = 5;
    private static final int ALIEN_SIZE = 25;
    private static final int ALIEN_ROWS = 3;
    private static final int ALIEN_COLS = 8;
    private static final int ALIEN_SPACING = 50;
    
    // Game objects
    private int playerX, playerY;
    private ArrayList<Point> bullets;
    private ArrayList<Point> aliens;
    private ArrayList<Point> alienBullets;
    private int score;
    private int lives;
    private boolean gameRunning;
    private boolean leftPressed, rightPressed, spacePressed;
    
    // Game timing
    private Timer gameTimer;
    private int alienMoveDelay = 0;
    private int alienShootDelay = 0;
    private Random random;
    
    public SpaceInvaders() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        
        random = new Random();
        bullets = new ArrayList<>();
        aliens = new ArrayList<>();
        alienBullets = new ArrayList<>();
        
        startGame();
    }
    
    private void startGame() {
        playerX = WIDTH / 2 - PLAYER_SIZE / 2;
        playerY = HEIGHT - 50;
        score = 0;
        lives = 3;
        gameRunning = true;
        
        // Create aliens
        aliens.clear();
        for (int row = 0; row < ALIEN_ROWS; row++) {
            for (int col = 0; col < ALIEN_COLS; col++) {
                int x = 50 + col * ALIEN_SPACING;
                int y = 50 + row * ALIEN_SPACING;
                aliens.add(new Point(x, y));
            }
        }
        
        bullets.clear();
        alienBullets.clear();
        
        gameTimer = new Timer(16, this); // ~60 FPS
        gameTimer.start();
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }
    
    private void draw(Graphics g) {
        // Draw player
        g.setColor(Color.GREEN);
        g.fillRect(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);
        
        // Draw player as spaceship
        int[] xPoints = {playerX, playerX + PLAYER_SIZE / 2, playerX + PLAYER_SIZE};
        int[] yPoints = {playerY + PLAYER_SIZE, playerY, playerY + PLAYER_SIZE};
        g.setColor(Color.CYAN);
        g.fillPolygon(xPoints, yPoints, 3);
        
        // Draw bullets
        g.setColor(Color.YELLOW);
        for (Point bullet : bullets) {
            g.fillRect(bullet.x, bullet.y, BULLET_SIZE, BULLET_SIZE);
        }
        
        // Draw alien bullets
        g.setColor(Color.RED);
        for (Point bullet : alienBullets) {
            g.fillRect(bullet.x, bullet.y, BULLET_SIZE, BULLET_SIZE);
        }
        
        // Draw aliens
        for (Point alien : aliens) {
            g.setColor(Color.RED);
            g.fillRect(alien.x, alien.y, ALIEN_SIZE, ALIEN_SIZE);
            
            // Draw alien details
            g.setColor(Color.WHITE);
            g.fillRect(alien.x + 5, alien.y + 5, 5, 5); // Left eye
            g.fillRect(alien.x + 15, alien.y + 5, 5, 5); // Right eye
            g.fillRect(alien.x + 8, alien.y + 15, 9, 3); // Mouth
        }
        
        // Draw score and lives
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 10, 25);
        g.drawString("Lives: " + lives, WIDTH - 100, 25);
        
        // Draw game over
        if (!gameRunning) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("GAME OVER", WIDTH / 2 - 100, HEIGHT / 2);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Final Score: " + score, WIDTH / 2 - 80, HEIGHT / 2 + 40);
            g.drawString("Press R to Restart", WIDTH / 2 - 80, HEIGHT / 2 + 80);
        }
    }
    
    private void updateGame() {
        if (!gameRunning) return;
        
        // Move player
        if (leftPressed && playerX > 0) {
            playerX -= 5;
        }
        if (rightPressed && playerX < WIDTH - PLAYER_SIZE) {
            playerX += 5;
        }
        
        // Shoot bullet
        if (spacePressed && bullets.size() < 3) {
            bullets.add(new Point(playerX + PLAYER_SIZE / 2 - BULLET_SIZE / 2, playerY));
            spacePressed = false; // Single shot per press
        }
        
        // Move bullets upward
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Point bullet = bullets.get(i);
            bullet.y -= 8;
            
            // Remove bullets that go off screen
            if (bullet.y < 0) {
                bullets.remove(i);
            }
        }
        
        // Move alien bullets downward
        for (int i = alienBullets.size() - 1; i >= 0; i--) {
            Point bullet = alienBullets.get(i);
            bullet.y += 5;
            
            // Remove bullets that go off screen
            if (bullet.y > HEIGHT) {
                alienBullets.remove(i);
            }
        }
        
        // Alien movement
        alienMoveDelay++;
        if (alienMoveDelay >= 30) { // Move aliens every 30 frames
            boolean moveDown = false;
            
            // Check if any alien hits the edge
            for (Point alien : aliens) {
                if (alien.x <= 0 || alien.x >= WIDTH - ALIEN_SIZE) {
                    moveDown = true;
                    break;
                }
            }
            
            // Move aliens
            int direction = moveDown ? 0 : (alienMoveDelay / 30) % 2 == 0 ? 2 : -2;
            for (Point alien : aliens) {
                if (moveDown) {
                    alien.y += 20; // Move down
                    // Reverse horizontal direction
                    direction = (alienMoveDelay / 30) % 2 == 0 ? 2 : -2;
                }
                alien.x += direction;
                
                // Game over if aliens reach bottom
                if (alien.y > HEIGHT - 100) {
                    gameOver();
                    return;
                }
            }
            alienMoveDelay = 0;
        }
        
        // Alien shooting
        alienShootDelay++;
        if (alienShootDelay >= 60 && !aliens.isEmpty()) { // Shoot every 60 frames
            // Random alien shoots
            Point shooter = aliens.get(random.nextInt(aliens.size()));
            alienBullets.add(new Point(shooter.x + ALIEN_SIZE / 2, shooter.y + ALIEN_SIZE));
            alienShootDelay = 0;
        }
        
        // Check bullet-alien collisions
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Point bullet = bullets.get(i);
            for (int j = aliens.size() - 1; j >= 0; j--) {
                Point alien = aliens.get(j);
                if (bullet.x < alien.x + ALIEN_SIZE &&
                    bullet.x + BULLET_SIZE > alien.x &&
                    bullet.y < alien.y + ALIEN_SIZE &&
                    bullet.y + BULLET_SIZE > alien.y) {
                    
                    // Collision detected
                    bullets.remove(i);
                    aliens.remove(j);
                    score += 100;
                    break;
                }
            }
        }
        
        // Check alien bullet-player collisions
        for (int i = alienBullets.size() - 1; i >= 0; i--) {
            Point bullet = alienBullets.get(i);
            if (bullet.x < playerX + PLAYER_SIZE &&
                bullet.x + BULLET_SIZE > playerX &&
                bullet.y < playerY + PLAYER_SIZE &&
                bullet.y + BULLET_SIZE > playerY) {
                
                // Hit player
                alienBullets.remove(i);
                lives--;
                
                if (lives <= 0) {
                    gameOver();
                }
                break;
            }
        }
        
        // Check win condition
        if (aliens.isEmpty()) {
            score += 1000; // Bonus for clearing level
            startGame(); // Start next level
        }
    }
    
    private void gameOver() {
        gameRunning = false;
        gameTimer.stop();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();
        repaint();
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        switch (key) {
            case KeyEvent.VK_LEFT:
                leftPressed = true;
                break;
            case KeyEvent.VK_RIGHT:
                rightPressed = true;
                break;
            case KeyEvent.VK_SPACE:
                spacePressed = true;
                break;
            case KeyEvent.VK_R:
                if (!gameRunning) {
                    startGame();
                }
                break;
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        
        switch (key) {
            case KeyEvent.VK_LEFT:
                leftPressed = false;
                break;
            case KeyEvent.VK_RIGHT:
                rightPressed = false;
                break;
            case KeyEvent.VK_SPACE:
                spacePressed = false;
                break;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Space Invaders");
        SpaceInvaders game = new SpaceInvaders();
        
        frame.add(game);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}