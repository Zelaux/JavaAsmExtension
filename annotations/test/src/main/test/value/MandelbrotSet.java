package test.value;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class MandelbrotSet extends JPanel {
    private final int WIDTH = 800;
    private final int HEIGHT = 800;
    private final int MAX_ITER = 1000;

    private BufferedImage image;

    private double minX = -2.0;
    private double maxX = 2.0;
    private double minY = -2.0;
    private double maxY = 2.0;

    private Point lastMousePoint;

    public MandelbrotSet() {
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        drawMandelbrot();

        // Обработка событий мыши
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePoint = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                lastMousePoint = null;
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePoint != null) {
                    double deltaX = (e.getX() - lastMousePoint.x) * (maxX - minX) / WIDTH;
                    double deltaY = (e.getY() - lastMousePoint.y) * (maxY - minY) / HEIGHT;
                    minX -= deltaX;
                    maxX -= deltaX;
                    minY += deltaY; // Обратите внимание на знак, чтобы прокрутка была вверх
                    maxY += deltaY;

                    drawMandelbrot();
                    repaint();
                    lastMousePoint = e.getPoint();
                }
            }
        });

        addMouseWheelListener(e -> {
            double zoomFactor = e.getWheelRotation() > 0 ? 1.25 : 0.75; // Уменьшение или увеличение
            double x = minX + (maxX - minX) * e.getX() / WIDTH;
            double y = minY + (maxY - minY) * e.getY() / HEIGHT;
            zoom(x, y, zoomFactor);
            drawMandelbrot();
            repaint();
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Mandelbrot Set");
        MandelbrotSet mandelbrotSet = new MandelbrotSet();
        frame.add(mandelbrotSet);
        frame.setSize(mandelbrotSet.WIDTH, mandelbrotSet.HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void drawMandelbrot() {
        long startTime = System.nanoTime();
        double dx = maxX - minX;
        double dy = maxY - minY;
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                double cX = minX + dx * x / WIDTH;
                double cY = minY + dy * y / HEIGHT;
                ComplexNumber c = new ComplexNumber(cX, cY);
                ComplexNumber z = new ComplexNumber(0,0);
                int iter = 0;

                while (z.magnitudeSquared() < 4 && iter < MAX_ITER) {
                    z = z.mul(z).add(c);
                    iter++;
                }

                int color = iter == MAX_ITER ? 0 : Color.HSBtoRGB((float) iter / MAX_ITER, 1, 1);
                image.setRGB(x, y, color);
            }
        }
        long endTime = System.nanoTime();

        System.out.printf("Time to paint: %.3fms%n", (endTime - startTime) / 1_000_000.);
    }

    private void zoom(double x, double y, double zoomFactor) {
        double newWidth = (maxX - minX) * zoomFactor;
        double newHeight = (maxY - minY) * zoomFactor;
        if (newWidth <= 0.001 || newHeight <= 0.001) return;
        minX = x - newWidth / 2;
        maxX = x + newWidth / 2;
        minY = y - newHeight / 2;
        maxY = y + newHeight / 2;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);
    }
}
