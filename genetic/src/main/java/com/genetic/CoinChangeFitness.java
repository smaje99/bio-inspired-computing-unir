package com.genetic;

import org.jgap.FitnessFunction;
import org.jgap.IChromosome;

/**
 * Fitness function for the Coin Change Problem.
 *
 * Each chromosome represents a possible solution (distribution of coins).
 * The goal is to achieve the target amount with the minimum number of coins.
 */
public class CoinChangeFitness extends FitnessFunction {
  private final int targetAmount;
  public static final int MAX_AMOUNT = 10000; // 10 dollars in cents
  public static final int MAX_COINS = MAX_AMOUNT;

  /**
   * Creates a fitness function for a given target amount.
   *
   * @param amount target amount in cents
   */
  public CoinChangeFitness(int amount) {
    if (amount < 1 || amount >= MAX_AMOUNT) {
      throw new IllegalArgumentException("Amount must be between 1 and " + MAX_AMOUNT + " cents.");
    }
    this.targetAmount = amount;
  }

  /**
   * Evaluates a chromosome.
   * A solution is better if it exactly matches the target amount and uses fewer
   * coins.
   *
   * @param chromosome candidate solution
   * @return fitness value (higher is better)
   */
  @Override
  protected double evaluate(IChromosome chromosome) {
    int amount = calculateAmount(chromosome);
    int totalCoins = calculateTotalCoins(chromosome);
    int difference = Math.abs(targetAmount - amount);

    // Penalize if solution does not match the target amount
    if (difference != 0) {
      return 0.0;
    }

    // Higher fitness for fewer coins
    return Math.max(0, MAX_COINS - totalCoins);
  }

  /**
   * Calculates the total amount represented by the chromosome.
   */
  public static int calculateAmount(IChromosome chromosome) {
    return getGene(chromosome, 0) * 100 + // $1
        getGene(chromosome, 1) * 50 + // 50¢
        getGene(chromosome, 2) * 25 + // 25¢
        getGene(chromosome, 3) * 10 + // 10¢
        getGene(chromosome, 4) * 5 + // 5¢
        getGene(chromosome, 5); // 1¢
  }

  /**
   * Calculates the total number of coins in the chromosome.
   */
  public static int calculateTotalCoins(IChromosome chromosome) {
    int total = 0;
    for (int i = 0; i < chromosome.size(); i++) {
      total += getGene(chromosome, i);
    }
    return total;
  }

  /**
   * Retrieves the integer value of a given gene.
   */
  public static int getGene(IChromosome chromosome, int index) {
    return (Integer) chromosome.getGene(index).getAllele();
  }
}
