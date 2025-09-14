package com.genetic;

import org.jgap.Genotype;
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
   */
  public static String execute(final int amount) {
    CoinChangeResult result;

    long timeStart = System.currentTimeMillis();

    try {
      result = CoinChangeSolver.solve(amount);
    } catch (IllegalArgumentException | InvalidConfigurationException e) {
      return e.getMessage();
    }

    long timeEnd = System.currentTimeMillis();
    long duration = timeEnd - timeStart;

    if (result == null) {
      return "No solution found.";
    }

    return formatCoinChangeResult(amount, result.bestChromosome(), result.population(), duration);
  }

  private static String formatCoinChangeResult(
      final int amount,
      final IChromosome best,
      final Genotype population,
      final long duration
  ) {
    StringBuilder sb = new StringBuilder();
    sb.append("Target amount: ").append(amount).append(" cents (").append(amount / 100.0).append(" dollars)\n\n");
    sb.append("Best solution found:\n");
    sb.append("\t").append(CoinChangeFitness.getGene(best, 0)).append(" x $1 coins\n");
    sb.append("\t").append(CoinChangeFitness.getGene(best, 1)).append(" x 50¢ coins\n");
    sb.append("\t").append(CoinChangeFitness.getGene(best, 2)).append(" x 25¢ coins\n");
    sb.append("\t").append(CoinChangeFitness.getGene(best, 3)).append(" x 10¢ coins\n");
    sb.append("\t").append(CoinChangeFitness.getGene(best, 4)).append(" x 5¢ coins\n");
    sb.append("\t").append(CoinChangeFitness.getGene(best, 5)).append(" x 1¢ coins\n");

    sb.append("Amount represented: ")
        .append(CoinChangeFitness.calculateAmount(best))
        .append(" cents\n");
    sb.append("Total coins used: ")
        .append(CoinChangeFitness.calculateTotalCoins(best))
        .append("\n");
    sb.append("Fitness value: ").append(best.getFitnessValue()).append("\n\n");

    sb.append("Population statistics:\n");
    sb.append("\tBest fitness: ").append(best.getFitnessValue());
    sb.append("\n\tWorst fitness: ").append(CoinChangeFitness.calculateWorstFitness(population));
    sb.append("\n\tAverage fitness: ").append(CoinChangeFitness.calculateAverageFitness(population)).append("\n\n");

    sb.append("Execution time: ").append(duration).append(" ms\n");

    return sb.toString();
  }
}
