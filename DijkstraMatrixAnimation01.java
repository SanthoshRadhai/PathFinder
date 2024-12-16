import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class DijkstraMatrixAnimation01 extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
    private static final int ROWS = 20;
    private static final int COLS = 20;
    private int[][] matrix;
    private javax.swing.Timer timer;
    private java.util.List<Point> path = new ArrayList<>();
    private java.util.List<Point> animatedPath = new ArrayList<>();
    private boolean pathFound = false;
    private boolean findingPath = false;
    private boolean obstaclesPlaced = false;
    private boolean addingObstacles = false; // To track obstacle addition mode
    private int animationIndex = 0;

    private Point startPoint = null;
    private Point endPoint = null;

    public DijkstraMatrixAnimation01() {
        matrix = new int[ROWS][COLS];
        initializeMatrix();
        addMouseListener(this);
        addMouseMotionListener(this);
        timer = new javax.swing.Timer(100, this);

        // Set up the buttons
        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> startPathfinding());

        JButton addObstacleButton = new JButton("Add Obstacle");
        addObstacleButton.addActionListener(e -> toggleAddObstacle());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(addObstacleButton);

        JFrame frame = new JFrame("Dijkstra Matrix Animation");
        frame.setLayout(new BorderLayout());
        frame.add(this, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setSize(600, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void initializeMatrix() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                matrix[i][j] = 1; // Default value, 1 for open cell
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int cellWidth = getWidth() / COLS;
        int cellHeight = getHeight() / ROWS;

        // Draw grid with obstacles
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                int value = matrix[i][j];
                Color color = (value == 0) ? Color.BLACK : new Color(200, 200, 200); // Black for obstacles
                g.setColor(color);
                g.fillRect(j * cellWidth, i * cellHeight, cellWidth, cellHeight);
            }
        }

        // Draw start and end points
        if (startPoint != null) {
            g.setColor(Color.BLUE);
            g.fillRect(startPoint.y * cellWidth, startPoint.x * cellHeight, cellWidth, cellHeight);
        }
        if (endPoint != null) {
            g.setColor(Color.RED);
            g.fillRect(endPoint.y * cellWidth, endPoint.x * cellHeight, cellWidth, cellHeight);
        }

        // Draw the animated path in bright green
        g.setColor(Color.GREEN);
        for (int i = 0; i < animationIndex && i < animatedPath.size(); i++) {
            Point p = animatedPath.get(i);
            g.fillRect(p.y * cellWidth, p.x * cellHeight, cellWidth, cellHeight);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (findingPath && animationIndex < animatedPath.size()) {
            animationIndex++;
            repaint();
        } else if (findingPath) {
            findingPath = false;
        }
    }

    private void startPathfinding() {
        if (startPoint != null && endPoint != null && obstaclesPlaced) {
            path = findShortestPath(startPoint, endPoint);
            animatedPath.clear();
            animatedPath.addAll(path);
            findingPath = true;
            animationIndex = 0;
            timer.start();
        }
    }

    private java.util.List<Point> findShortestPath(Point start, Point end) {
        Map<Point, Integer> distance = new HashMap<>();
        Map<Point, Point> previous = new HashMap<>();
        PriorityQueue<Point> queue = new PriorityQueue<>(Comparator.comparingInt(distance::get));

        distance.put(start, 0);
        queue.add(start);

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            if (current.equals(end)) break;

            for (Point neighbor : getNeighbors(current)) {
                if (matrix[neighbor.x][neighbor.y] == 0) continue; // Skip obstacles

                int newDist = distance.get(current) + matrix[neighbor.x][neighbor.y];
                if (newDist < distance.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    distance.put(neighbor, newDist);
                    previous.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        java.util.List<Point> path = new ArrayList<>();
        for (Point at = end; at != null; at = previous.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }

    private java.util.List<Point> getNeighbors(Point p) {
        java.util.List<Point> neighbors = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : directions) {
            int newRow = p.x + d[0];
            int newCol = p.y + d[1];
            if (newRow >= 0 && newRow < ROWS && newCol >= 0 && newCol < COLS) {
                neighbors.add(new Point(newRow, newCol));
            }
        }
        return neighbors;
    }

    private void toggleAddObstacle() {
        addingObstacles = !addingObstacles; // Toggle the obstacle adding mode
        String state = addingObstacles ? "enabled" : "disabled";
        JOptionPane.showMessageDialog(this, "Obstacle placement is now " + state + ".");
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int cellWidth = getWidth() / COLS;
        int cellHeight = getHeight() / ROWS;
        int row = e.getY() / cellHeight;
        int col = e.getX() / cellWidth;

        Point selectedPoint = new Point(row, col);
        if (addingObstacles) {
            // Toggle obstacle placement on mouse press if adding obstacles is enabled
            if (matrix[row][col] == 0) {
                matrix[row][col] = 1; // Remove obstacle
            } else {
                matrix[row][col] = 0; // Add obstacle
            }
            obstaclesPlaced = true; // Mark that obstacles have been placed
        } else {
            // Set start and end points
            if (startPoint == null) {
                startPoint = selectedPoint;
            } else if (endPoint == null) {
                endPoint = selectedPoint;
            }
        }
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (addingObstacles) { // Allow obstacle placement only if in adding mode
            int cellWidth = getWidth() / COLS;
            int cellHeight = getHeight() / ROWS;
            int row = e.getY() / cellHeight;
            int col = e.getX() / cellWidth;

            if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
                matrix[row][col] = (matrix[row][col] == 0) ? 1 : 0; // Toggle obstacle
                obstaclesPlaced = true; // Mark that obstacles have been placed
                repaint();
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {
        new DijkstraMatrixAnimation01();
    }
}
