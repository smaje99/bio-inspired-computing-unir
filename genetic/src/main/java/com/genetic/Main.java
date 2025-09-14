package com.genetic;

import javax.swing.SwingUtilities;

import com.genetic.ui.CoinChangeGUI;

/**
 * Entry point for testing the Coin Change Genetic Algorithm.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CoinChangeGUI gui = new CoinChangeGUI();
            gui.setVisible(true);
        });
    }
}
