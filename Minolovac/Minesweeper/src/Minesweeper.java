import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class Minesweeper {

    private class MineTile extends JButton {
        int row, col;

        public MineTile(int row, int col) {
            this.row = row;
            this.col = col;
            setOpaque(true);
            setBorder(BorderFactory.createLineBorder(Color.black));
            updateBackground();
        }
        public void updateBackground() {
            if (isEnabled()) {
                if ((row + col) % 2 == 0) {
                    setBackground(new Color(170, 255, 170));
                } 
                else {
                    setBackground(new Color(100, 200, 100));
                }
            } 
            else {
                setBackground(new Color(200, 255, 200));
            }
        }
    }

    int tileSize = 30;
    int numRows, numCols, mineCount;
    int boardWidth, boardHeight;

    JFrame frame = new JFrame("Minesweeper");
    JLabel textLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();
    JButton restartButton = new JButton("🔁");
    JButton changeDifficultyButton = new JButton("🔙");

    MineTile[][] board;
    ArrayList<MineTile> mineList;
    Random random = new Random();

    int flagsLeft;
    int tilesClicked = 0;
    boolean gameOver = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Minesweeper());
    }

    Minesweeper() {
        showDifficultyDialog();
    }

    void showDifficultyDialog() {
        String[] options = {"Easy", "Medium", "Hard"};
        int choice = JOptionPane.showOptionDialog(null, "Select Difficulty", "Minesweeper Difficulty",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        switch (choice) {
            case 0: numRows = 9; numCols = 9; mineCount = 10; break;
            case 1: numRows = 16; numCols = 16; mineCount = 40; break;
            case 2: numRows = 16; numCols = 30; mineCount = 99; break;
            default: System.exit(0);
        }

        setupNewGame();
    }

    void setupNewGame() {
        boardWidth = Math.max(numCols * tileSize, 360);
        boardHeight = Math.max(numRows * tileSize, 360);
        flagsLeft = mineCount;
        tilesClicked = 0;
        gameOver = false;
        mineList = new ArrayList<>();
        board = new MineTile[numRows][numCols];

        frame.getContentPane().removeAll();
        setupGUI();
        setMines();

        frame.revalidate();
        frame.repaint();
    }

    void setupGUI() {
        frame.setSize(boardWidth, boardHeight + 80);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        textLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 25));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("🚩 " + flagsLeft);

        textPanel = new JPanel(new BorderLayout());
        restartButton.setFocusable(false);
        restartButton.addActionListener(e -> setupNewGame());

        changeDifficultyButton.setFocusable(false);
        changeDifficultyButton.addActionListener(e -> {
            // Clear GUI before showing difficulty dialog
            frame.setVisible(false); // hide current frame
            frame.dispose(); // destroy the current frame completely

            // Show dialog after short delay to avoid UI glitches
            SwingUtilities.invokeLater(() -> new Minesweeper());
        });

        textPanel.add(restartButton, BorderLayout.EAST);
        textPanel.add(changeDifficultyButton, BorderLayout.WEST);
        textPanel.add(textLabel, BorderLayout.CENTER);

        boardPanel = new JPanel(new GridLayout(numRows, numCols));
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                MineTile tile = new MineTile(row, col);
                board[row][col] = tile;

                tile.setFocusable(false);
                tile.setMargin(new Insets(0, 0, 0, 0));
                tile.setFont(new Font("Arial Unicode MS", Font.BOLD, 18));

                tile.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (gameOver) return;
                        MineTile tile = (MineTile) e.getSource();

                        if (e.getButton() == MouseEvent.BUTTON1) {
                            if (tile.getText().equals("")) {
                                if (mineList.contains(tile)) {
                                    revealMines();
                                } else {
                                    checkMine(tile.row, tile.col);
                                }
                            }
                        } else if (e.getButton() == MouseEvent.BUTTON3) {
                            if (tile.getText().equals("") && tile.isEnabled()) {
                                tile.setText("🚩");
                                tile.setBackground(new Color(200, 255, 200));
                                flagsLeft--;
                            } else if (tile.getText().equals("🚩")) {
                                tile.setText("");
                                tile.updateBackground();
                                flagsLeft++;
                            }
                            textLabel.setText("🚩 " + flagsLeft);
                        }
                    }
                });
                boardPanel.add(tile);
            }
        }
        frame.add(textPanel, BorderLayout.NORTH);
        frame.add(boardPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }
    void setMines() {
        int minesLeft = mineCount;
        while (minesLeft > 0) {
            int row = random.nextInt(numRows);
            int col = random.nextInt(numCols);
            MineTile tile = board[row][col];
            if (!mineList.contains(tile)) {
                mineList.add(tile);
                minesLeft--;
            }
        }
    }
    void revealMines() {
        for (MineTile tile : mineList) {
            tile.setText("💣");
            tile.setBackground(Color.RED);
        }
        gameOver = true;
        textLabel.setText("Game over!");
    }
    void checkMine(int row, int col) {
        if (row < 0 || row >= numRows || col < 0 || col >= numCols) return;
        MineTile tile = board[row][col];
        if (!tile.isEnabled() || tile.getText().equals("🚩")) return;

        tile.setEnabled(false);
        tile.updateBackground();
        tilesClicked++;

        int minesFound = 0;
        minesFound += countMine(row - 1, col - 1);
        minesFound += countMine(row - 1, col);
        minesFound += countMine(row - 1, col + 1);
        minesFound += countMine(row, col - 1);
        minesFound += countMine(row, col + 1);
        minesFound += countMine(row + 1, col - 1);
        minesFound += countMine(row + 1, col);
        minesFound += countMine(row + 1, col + 1);

        if (minesFound > 0) {
            tile.setText(Integer.toString(minesFound));
        } 
        else  {
            tile.setText("");
            checkMine(row - 1, col - 1);
            checkMine(row - 1, col);
            checkMine(row - 1, col + 1);
            checkMine(row, col - 1);
            checkMine(row, col + 1);
            checkMine(row + 1, col - 1);
            checkMine(row + 1, col);
            checkMine(row + 1, col + 1);
        }
        if (tilesClicked == numRows * numCols - mineList.size()) {
            gameOver = true;
            textLabel.setText("Minefield clear!");
        }
    }
    int countMine(int row, int col) {
        if (row < 0 || row >= numRows || col < 0 || col >= numCols) return 0;
        return mineList.contains(board[row][col]) ? 1 : 0;
    }
}