package com.genetic;

import org.jgap.FitnessFunction;
import org.jgap.Genotype;
import org.jgap.IChromosome;

/**
 * Fitness function for the Coin Change Problem.
 *
 * Each chromosome represents a possible solution (distribution of coins).
 * The goal is to achieve the target amount with the minimum number of coins.
 */
public class CoinChangeFitness extends FitnessFunction {
  private final int targetAmount;
  public static final int MAX_AMOUNT = 10000; // 100 dollars in cents
  public static final int MAX_COINS = MAX_AMOUNT;

  // Weights to balance the objectives
  private static final double AMOUNT_PENALTY_WEIGHT = 1000.0;
  private static final double COIN_COUNT_WEIGHT = 1.0;
  private static final double BASE_FITNESS = 10_000.0;

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
   * Evaluates a chromosome using a multi-objective fitness function.
   *
   * It considers:
   * - How close is the amount to the target (heavy penalty)
   * - Total number of coins (minor penalty to minimize)
   *
   * @param chromosome candidate solution
   * @return fitness value (higher is better)
   */
  @Override
  protected double evaluate(IChromosome chromosome) {
    int amount = calculateAmount(chromosome);
    int totalCoins = calculateTotalCoins(chromosome);
    int amountDifference = Math.abs(targetAmount - amount);

    // High-level fitness
    double fitness = BASE_FITNESS;

    // Heavy penalty for quantity difference
    // This guides towards the correct quantity
    fitness -= amountDifference * AMOUNT_PENALTY_WEIGHT;

    // If the amount is exact, penalice by number of coins
    // This optimizes towards fewer coins
    if (amountDifference == 0) {
      fitness -= totalCoins * COIN_COUNT_WEIGHT;
    }

    // Additional penalty if the target amount is significantly exceeded
    if (amount > targetAmount) {
      fitness -= (amount - targetAmount) * AMOUNT_PENALTY_WEIGHT * 2;
    }

    // Ensure that fitness is non-negative
    return Math.max(1d, fitness);
  }

  /**
   * Calculates the total amount represented by the chromosome.
   */
  public static int calculateAmount(final IChromosome chromosome) {
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
  public static int calculateTotalCoins(final IChromosome chromosome) {
    int total = 0;
    for (int i = 0; i < chromosome.size(); i++) {
      total += getGene(chromosome, i);
    }
    return total;
  }

  /**
   * Retrieves the integer value of a given gene.
   */
  public static int getGene(final IChromosome chromosome, final int index) {
    return (Integer) chromosome.getGene(index).getAllele();
  }

  /**
   * Calculates the average fitness of a population.
   */
  public static double calculateAverageFitness(final Genotype population) {
    double totalFitness = 0.0;
    for (Object chromosome : population.getPopulation().getChromosomes()) {
      totalFitness += ((IChromosome) chromosome).getFitnessValue();
    }
    return totalFitness / population.getPopulation().size();
  }

  /**
   * Retrieves the worst (minimum) fitness value in the population.
   */
  public static double calculateWorstFitness(final Genotype population) {
    if (population.getPopulation().size() == 0) {
      return 0.0;
    }

    double worstFitness = Double.MAX_VALUE;

    for (Object chromosome : population.getPopulation().getChromosomes()) {
      double fitness = ((IChromosome) chromosome).getFitnessValue();
      if (fitness < worstFitness) {
        worstFitness = fitness;
      }
    }
    return worstFitness;
  }
}
