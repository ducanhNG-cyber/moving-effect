import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class MovingTextExplosion extends JPanel implements ActionListener {
    private final Timer timer;
    private final List<TextExplosion> explosions = new ArrayList<>();
    private final Random rand = new Random();

    public MovingTextExplosion() {
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(800, 600));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                explosions.add(new TextExplosion("Java Passion", 400, 300, e.getX(), e.getY())); // xuất phát từ trung tâm
            }
        });

        timer = new Timer(30, this); // ~33 fps
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        Iterator<TextExplosion> iter = explosions.iterator();
        while (iter.hasNext()) {
            TextExplosion ex = iter.next();
            if (ex.isFinished()) {
                iter.remove();
                continue;
            }
            ex.draw(g2d);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (TextExplosion ex : explosions) {
            ex.update();
        }
        repaint();
    }

    // Phần hiệu ứng chính
    class TextExplosion {
        String text;
        int startX, startY;
        int destX, destY;
        double curX, curY;
        long startTime;
        final long travelTime = 2000; // 2s
        boolean exploded = false;
        Font font = new Font("Serif", Font.BOLD, 24);
        List<FlyingChar> flyingChars = new ArrayList<>();
        List<Particle> particles = new ArrayList<>();

        public TextExplosion(String text, int startX, int startY, int destX, int destY) {
            this.text = text;
            this.startX = startX;
            this.startY = startY;
            this.destX = destX;
            this.destY = destY;
            this.curX = startX;
            this.curY = startY;
            this.startTime = System.currentTimeMillis();
        }

        void update() {
            long now = System.currentTimeMillis();
            long elapsed = now - startTime;

            if (!exploded) {
                if (elapsed >= travelTime) {
                    curX = destX;
                    curY = destY;
                    explode();
                } else {
                    double t = elapsed / (double) travelTime;
                    curX = startX + t * (destX - startX);
                    curY = startY + t * (destY - startY);
                }
            } else {
                for (FlyingChar fc : flyingChars) fc.update();
                for (Particle p : particles) p.update();
            }
        }

        void draw(Graphics2D g2d) {
            g2d.setFont(font);
            if (!exploded) {
                g2d.setColor(Color.CYAN);

                FontMetrics fm = g2d.getFontMetrics();
                int width = fm.stringWidth(text);
                int drawX = (int) curX - width / 2;

                g2d.drawString(text, drawX, (int) curY);
            } else {
                for (FlyingChar fc : flyingChars) fc.draw(g2d);
                for (Particle p : particles) p.draw(g2d);
            }
        }

        boolean isFinished() {
            if (!exploded) return false;
            for (FlyingChar fc : flyingChars)
                if (fc.alpha > 0) return false;
            return true;
        }

        void explode() {
            exploded = true;

            FontMetrics fm = getFontMetrics(font);
            int totalWidth = fm.stringWidth(text);
            int startCharX = destX - totalWidth / 2;
            int curX = startCharX;

            for (int i = 0; i < text.length(); i++) {
                char ch = text.charAt(i);
                int cw = fm.charWidth(ch);
                double angle = rand.nextDouble() * 2 * Math.PI;
                double dx = Math.cos(angle) * (1 + rand.nextDouble() * 3);
                double dy = Math.sin(angle) * (1 + rand.nextDouble() * 3);
                flyingChars.add(new FlyingChar(ch, curX, destY, dx, dy));
                curX += cw;
            }

            // tạo hiệu ứng nổ (particle tròn nhỏ)
            for (int i = 0; i < 100; i++) {
                double angle = rand.nextDouble() * 2 * Math.PI;
                double speed = 2 + rand.nextDouble() * 3;
                double dx = Math.cos(angle) * speed;
                double dy = Math.sin(angle) * speed;
                particles.add(new Particle(destX, destY, dx, dy));
            }
        }
    }

    class FlyingChar {
        char ch;
        double x, y;
        double dx, dy;
        float alpha = 1.0f;
        Color color = Color.getHSBColor(rand.nextFloat(), 1f, 1f);

        public FlyingChar(char ch, double x, double y, double dx, double dy) {
            this.ch = ch;
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
        }

        void update() {
            x += dx;
            y += dy;
            alpha -= 0.02f;
            if (alpha < 0) alpha = 0;
        }

        void draw(Graphics2D g2d) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.setColor(color);
            g2d.setFont(new Font("Serif", Font.BOLD, 24));
            g2d.drawString(String.valueOf(ch), (int) x, (int) y);
        }
    }

    class Particle {
        double x, y, dx, dy;
        float alpha = 1.0f;
        Color color = Color.YELLOW;

        public Particle(double x, double y, double dx, double dy) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
        }

        void update() {
            x += dx;
            y += dy;
            alpha -= 0.04f;
            if (alpha < 0) alpha = 0;
        }

        void draw(Graphics2D g2d) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.setColor(color);
            g2d.fillOval((int) x, (int) y, 4, 4);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Moving Text Explosion");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new MovingTextExplosion());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
