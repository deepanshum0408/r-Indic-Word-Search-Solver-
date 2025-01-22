import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Random;
import java.util.Set;
import java.util.LinkedHashSet;


public class WordGuessingGameGUI extends JFrame {
    private static final Random random = new Random();
    private static final String dbPath = "C:\\Users\\deepa\\IdeaProjects\\Wordhunt_data\\data.db";
    private static final String url = "jdbc:sqlite:" + dbPath;

    private int mode = 1;
    private int score = 0;
    private String playerName;
    private JTextField nameField;
    private JComboBox<String> difficultyComboBox;
    private JTextArea gridArea;
    private JTextField guessField;
    private JLabel scoreLabel;
    private JButton startButton, guessButton, hintButton, resetButton, quitButton, playAgainButton, highScoresButton;
    private String hiddenWord;

    public WordGuessingGameGUI() {
        setTitle("शब्द अनुमान खेल");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(true);

        // Maximize the window when it starts
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Top panel for name and difficulty selection
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        topPanel.setBackground(new Color(220, 240, 255));

        JLabel nameLabel = new JLabel("नाम:");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        topPanel.add(nameLabel);

        nameField = new JTextField(10);
        nameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        topPanel.add(nameField);

        JLabel difficultyLabel = new JLabel("कठिनाई स्तर:");
        difficultyLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        topPanel.add(difficultyLabel);

        String[] difficultyLevels = {"आसान", "मध्यम", "कठिन"};
        difficultyComboBox = new JComboBox<>(difficultyLevels);
        difficultyComboBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        topPanel.add(difficultyComboBox);

        startButton = new JButton("खेलें");
        startButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        topPanel.add(startButton);

        add(topPanel, BorderLayout.NORTH);

        // Center panel for split grid area and keyboard
        JSplitPane centerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerSplitPane.setResizeWeight(0.5); // 50:50 ratio

        // Left panel for grid display
        gridArea = new JTextArea(10, 20);
        gridArea.setEditable(false);
        gridArea.setFont(new Font("Monospaced", Font.BOLD, 18));
        gridArea.setForeground(new Color(50, 50, 50));
        gridArea.setBackground(new Color(230, 230, 255));
        gridArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(gridArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("शब्द पहेली"));
        centerSplitPane.setLeftComponent(scrollPane);

        // Right panel for on-screen Hindi keyboard
        JPanel keyboardPanel = createHindiKeyboard();
        centerSplitPane.setRightComponent(keyboardPanel);

        add(centerSplitPane, BorderLayout.CENTER);

        // Bottom panel for controls
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        bottomPanel.setBackground(new Color(220, 240, 255));

        bottomPanel.add(new JLabel("अपना अनुमान:"));
        guessField = new JTextField(10);
        guessField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        bottomPanel.add(guessField);

        guessButton = new JButton("अनुमान लगाएं");
        guessButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        hintButton = new JButton("संकेत");
        hintButton.setFont(new Font("SansSerif", Font.BOLD, 14));

        playAgainButton = new JButton("फिर से खेलें");
        playAgainButton.setFont(new Font("SansSerif", Font.BOLD, 14));

        resetButton = new JButton("रीसेट");
        resetButton.setFont(new Font("SansSerif", Font.BOLD, 14));

        quitButton = new JButton("बाहर निकलें");
        quitButton.setFont(new Font("SansSerif", Font.BOLD, 14));

        highScoresButton = new JButton("शीर्ष स्कोर");
        highScoresButton.setFont(new Font("SansSerif", Font.BOLD, 14));

        scoreLabel = new JLabel("स्कोर: 0");
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        scoreLabel.setForeground(Color.BLUE);

        bottomPanel.add(guessButton);
        bottomPanel.add(hintButton);
        bottomPanel.add(playAgainButton);
        bottomPanel.add(resetButton);
        bottomPanel.add(quitButton);
        bottomPanel.add(highScoresButton);
        bottomPanel.add(scoreLabel);

        add(bottomPanel, BorderLayout.SOUTH);

        // Action listeners
        startButton.addActionListener(e -> startGame());
        guessButton.addActionListener(e -> makeGuess());
        hintButton.addActionListener(e -> showHint());
        playAgainButton.addActionListener(e -> playAgain());
        resetButton.addActionListener(e -> resetGame());
        quitButton.addActionListener(e -> System.exit(0));
        highScoresButton.addActionListener(e -> showHighScores());

        // Initialize the database
        initializeDatabase();

        setVisible(true);
    }

    private JPanel createHindiKeyboard() {
        JPanel keyboardPanel = new JPanel(new GridLayout(6, 12, 5, 5));
        keyboardPanel.setBorder(BorderFactory.createTitledBorder("हिंदी कीबोर्ड"));

        // Combine characters and matras
        String allHindiSymbols = "अआइईउऊऋएऐओऔअंअःकखगघङचछजझञटठडढणतथदधनपफबभमयरलवशषसहक्षत्रज्ञ" +
                "ँ ा ि ी ु ू ृ े ै ो ौ ं ः";
        // Use a Set to eliminate duplicates
        Set<Character> uniqueCharacters = new LinkedHashSet<>();
        for (char ch : allHindiSymbols.toCharArray()) {
            uniqueCharacters.add(ch);
        }

        // Add unique buttons to the panel
        for (char ch : uniqueCharacters) {
            JButton button = new JButton(String.valueOf(ch));
            button.setFont(new Font("SansSerif", Font.BOLD, 18));
            button.addActionListener(e -> guessField.setText(guessField.getText() + ch));
            keyboardPanel.add(button);
        }

        // Clear button
        JButton clearButton = new JButton("मिटाएं");
        clearButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        clearButton.addActionListener(e -> {
            String currentText = guessField.getText();
            if (!currentText.isEmpty()) {
                guessField.setText(currentText.substring(0, currentText.length() - 1));
            }
        });
        keyboardPanel.add(clearButton);

        return keyboardPanel;
    }


    private void startGame() {
        playerName = nameField.getText();
        mode = difficultyComboBox.getSelectedIndex() + 1;
        int gridSize = (mode == 3) ? 12 : (mode == 2) ? 8 : 4;

        score = 0;
        scoreLabel.setText("स्कोर: " + score);
        hiddenWord = selectWord();
        char[][] grid = new char[gridSize][gridSize];

        if (placeWordInGrid(grid, hiddenWord)) {
            fillRandomLetters(grid);
            displayGrid(grid);
        } else {
            gridArea.setText("ग्रिड में शब्द रखने में विफल रहे।");
        }
    }

    private void playAgain() {
        guessField.setText("");        // Clear the guess field
        gridArea.setText("");          // Clear the grid area
        gridArea.setForeground(Color.BLACK); // Reset the grid text color
        hiddenWord = selectWord();     // Select a new hidden word
        int gridSize = (mode == 3) ? 12 : (mode == 2) ? 8 : 4; // Determine grid size

        char[][] grid = new char[gridSize][gridSize];
        if (placeWordInGrid(grid, hiddenWord)) {
            fillRandomLetters(grid);
            displayGrid(grid);         // Display the new grid
        } else {
            gridArea.setText("ग्रिड में शब्द रखने में विफल रहे।");
        }

        JOptionPane.showMessageDialog(this, "नया दौर शुरू हुआ है!");
    }


    private void makeGuess() {
        String guess = guessField.getText();
        if (guess.equalsIgnoreCase(hiddenWord)) {
            score++;
            scoreLabel.setText("स्कोर: " + score);
            gridArea.setForeground(Color.GREEN);
            JOptionPane.showMessageDialog(this, "बधाई! आपने सही शब्द अनुमान किया।");

            // Save the score
            savePlayerScore(playerName, score);
        } else {
            gridArea.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this, "क्षमा करें! आपका अनुमान गलत था। सही शब्द था: " + hiddenWord);
        }
        guessField.setText("");
    }

    private void showHint() {
        JOptionPane.showMessageDialog(this, "पहेली में शब्द की लंबाई है: " + hiddenWord.length());
    }

    private void resetGame() {
        nameField.setText("");         // Clear the player's name
        guessField.setText("");        // Clear the guess field
        gridArea.setText("");          // Clear the grid area
        score = 0;                     // Reset score for a new player
        scoreLabel.setText("स्कोर: 0"); // Update the UI
        hiddenWord = null;             // Reset the hidden word
        JOptionPane.showMessageDialog(this, "खेल रीसेट हो गया है। नया खेल शुरू करें।");
    }


    private void initializeDatabase() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS players (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                score INTEGER NOT NULL
            );
        """;
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void savePlayerScore(String name, int score) {
        String insertSQL = "INSERT INTO players (name, score) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, score);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showHighScores() {
        StringBuilder scores = new StringBuilder("शीर्ष स्कोर:\n");
        String selectSQL = "SELECT name, score FROM players ORDER BY score DESC LIMIT 10";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {
            while (rs.next()) {
                scores.append(rs.getString("name"))
                        .append(": ")
                        .append(rs.getInt("score"))
                        .append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        JOptionPane.showMessageDialog(this, scores.toString());
    }

    private String selectWord() {
        String[] words = {"कोड", "शिक", "प्रोज", "स्के", "नोड", "सीपी", "डेट", "पायथ",
                "वेब", "सॉफ़", "नेटव", "एप्ल", "जावा", "ग्राफ", "ऐप्प", "मैथ",
                "लिंक", "आर्क", "चिप", "बूट", "मॉड", "टेक", "यूआइ", "डिज",
                "गेम", "प्लस", "थ्रेड", "क्लौड", "आइटी", "डिव", "कोडर", "साइबर",
                "लिंक", "डेटा", "मशीन", "लैंग", "लैब", "आईक्यू", "डॉट", "लॉजिक",
                "रेखा", "जोक", "वीज़", "पिक्स", "जेन", "रेड", "प्वाइ", "स्किल",
                "स्क्रै", "ह्यूम", "कंपस", "सिस्टम", "स्मार", "मैसे", "फ्रेम", "पायल",
                "सेन्क", "नेच", "लैपट", "सीपु", "विजि", "क्वे", "पोर्ट", "रीड",
                "फ्लॉप", "मेम", "डिस", "क्लस्ट", "डिबग", "ड्यूल", "आर्ट", "सीओडी",
                "सर्च", "फोक", "कंट्र", "मल्टी", "चैनल", "सीक्व", "लूप", "स्मैश",
                "एआई", "कोम्प", "प्रोफ", "फोर", "मोड", "एल्स", "नेच"};
        return words[random.nextInt(words.length)];
    }

    private boolean placeWordInGrid(char[][] grid, String word) {
        // Iterate over each cell in the grid
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                // Attempt to place the word horizontally
                if (canPlaceWordHorizontally(grid, row, col, word)) {
                    placeWordHorizontally(grid, row, col, word);
                    return true; // Exit as soon as the word is placed
                }

                // Attempt to place the word vertically
                if (canPlaceWordVertically(grid, row, col, word)) {
                    placeWordVertically(grid, row, col, word);
                    return true; // Exit as soon as the word is placed
                }
            }
        }
        // If no valid position is found, return false
        return false;
    }


    private boolean backtrackWordPlacement(char[][] grid, String word, int row, int col) {
        if (row >= grid.length) return false; // Reached beyond the grid without success

        // Attempt to place the word horizontally
        if (canPlaceWordHorizontally(grid, row, col, word)) {
            placeWordHorizontally(grid, row, col, word);
            return true;
        }

        // Attempt to place the word vertically
        if (canPlaceWordVertically(grid, row, col, word)) {
            placeWordVertically(grid, row, col, word);
            return true;
        }

        // Move to the next cell, adjusting row and col indices
        if (col + 1 < grid.length) {
            return backtrackWordPlacement(grid, word, row, col + 1);
        } else {
            return backtrackWordPlacement(grid, word, row + 1, 0);
        }
    }


    private boolean canPlaceWordHorizontally(char[][] grid, int row, int col, String word) {
        if (col + word.length() > grid.length) return false;
        for (int i = 0; i < word.length(); i++) {
            if (grid[row][col + i] != 0) return false;
        }
        return true;
    }

    private void placeWordHorizontally(char[][] grid, int row, int col, String word) {
        for (int i = 0; i < word.length(); i++) {
            grid[row][col + i] = word.charAt(i);
        }
    }

    private boolean canPlaceWordVertically(char[][] grid, int row, int col, String word) {
        if (row + word.length() > grid.length) return false;
        for (int i = 0; i < word.length(); i++) {
            if (grid[row + i][col] != 0) return false;
        }
        return true;
    }

    private void placeWordVertically(char[][] grid, int row, int col, String word) {
        for (int i = 0; i < word.length(); i++) {
            grid[row + i][col] = word.charAt(i);
        }
    }

    private void fillRandomLetters(char[][] grid) {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid.length; j++) {
                if (grid[i][j] == 0) {
                    grid[i][j] = (char) ('\u0905' + random.nextInt(34));
                }
            }
        }
    }

    private void displayGrid(char[][] grid) {
        StringBuilder gridText = new StringBuilder();
        for (char[] row : grid) {
            for (char cell : row) {
                gridText.append(cell == 0 ? '-' : cell).append(' ');
            }
            gridText.append('\n');
        }
        gridArea.setForeground(Color.BLACK);
        gridArea.setText(gridText.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WordGuessingGameGUI::new);
    }
}
