package com.genetic.ui;

import javax.swing.*;

import java.awt.*;

import com.genetic.CoinChangeExecutor;

/**
 * Swing GUI for the Coin Change problem using Genetic Algorithms.
 */
public class CoinChangeGUI extends JFrame {
  private final JTextField amountField;
  private final JTextArea resultArea;

  public CoinChangeGUI() {
    setTitle("Coin Change Genetic Algorithm");
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    // Top panel with input and button
    JPanel topPanel = new JPanel();
    topPanel.setLayout(new FlowLayout());

    JLabel label = new JLabel("Enter amount (cents):");
    amountField = new JTextField(10);
    JButton solveButton = new JButton("Solve");

    topPanel.add(label);
    topPanel.add(amountField);
    topPanel.add(solveButton);

    // Result area
    resultArea = new JTextArea(15, 40);
    resultArea.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(resultArea);

    // Layout
    setLayout(new BorderLayout());
    add(topPanel, BorderLayout.NORTH);
    add(scrollPane, BorderLayout.CENTER);

    // Button action
    solveButton.addActionListener(e -> onSolve());

    // Dimensions and centering the window
    pack();
    setLocationRelativeTo(null);
  }

  /**
   * Handles the Solve button action.
   */
  private void onSolve() {
    try {
      int amount = Integer.parseInt(amountField.getText().trim());
      String result = CoinChangeExecutor.execute(amount);
      resultArea.setText(result);
    } catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(this, "Please enter a valid integer amount.",
          "Input Error", JOptionPane.ERROR_MESSAGE);
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error solving the problem: " + ex.getMessage(),
          "Execution Error", JOptionPane.ERROR_MESSAGE);
    }
  }
}
