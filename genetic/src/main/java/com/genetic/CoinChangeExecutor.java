package com.genetic;

import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;

/**
 * Executes the Coin Change problem and formats the result as a String.
 */
public class CoinChangeExecutor {

  // Private constructor to prevent instantiation
  private CoinChangeExecutor() {
  }

  /**
   * Executes the solver and returns a formatted result.
   *
   * @param amount target amount in cents
   * @return formatted solution
   * @throws InvalidConfigurationException if solver fails
   * @throws IllegalArgumentException      if amount is out of valid range
   */
  public static String execute(int amount) throws InvalidConfigurationException, IllegalArgumentException {
    IChromosome best;
    try {
      best = CoinChangeSolver.solve(amount);
    } catch (IllegalArgumentException | InvalidConfigurationException e) {
      return e.getMessage();
    }

    if (best == null) {
      return "No solution found.";
    }

    StringBuilder sb = new StringBuilder();
    sb.append("Target amount: ").append(amount).append(" cents\n");
    sb.append("Best solution found:\n");
    sb.append("\t").append(CoinChangeFitness.getGene(best, 0)).append(" x $1 coins\n");
    sb.append("\t").append(CoinChangeFitness.getGene(best, 1)).append(" x 50¢ coins\n");
    sb.append("\t").append(CoinChangeFitness.getGene(best, 2)).append(" x 25¢ coins\n");
    sb.append("\t").append(CoinChangeFitness.getGene(best, 3)).append(" x 10¢ coins\n");
    sb.append("\t").append(CoinChangeFitness.getGene(best, 4)).append(" x 5¢ coins\n");
    sb.append("\t").append(CoinChangeFitness.getGene(best, 5)).append(" x 1¢ coins\n");

    sb.append("Total coins: ")
        .append(CoinChangeFitness.calculateTotalCoins(best))
        .append("\n");

    return sb.toString();
  }
}
