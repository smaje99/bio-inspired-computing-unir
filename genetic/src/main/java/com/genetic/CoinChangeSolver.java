package com.genetic;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.CrossoverOperator;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.IntegerGene;
import org.jgap.impl.MutationOperator;

/**
 * Solver for the Coin Change Problem using Genetic Algorithms.
 */
public class CoinChangeSolver {
  private static final int POPULATION_SIZE = 300;
  private static final int MAX_EVOLUTIONS = 1000;

  // Safety multiplier to avoid premature convergence
  // Dynamic limits based on target amount could be implemented
  private static final double SAFETY_MULTIPLIER = 1.5;

  // Private constructor to prevent instantiation
  private CoinChangeSolver() {
  }

  /**
   * Solves the coin change problem for a given amount using JGAP.
   *
   * @param amount target amount in cents
   * @return result containing the fittest chromosome (best solution) and population
   * @throws InvalidConfigurationException if configuration fails
   * @throws IllegalArgumentException      if amount is out of valid range
   */
  public static CoinChangeResult solve(int amount) throws InvalidConfigurationException, IllegalArgumentException {
    Configuration.reset();
    Configuration conf = new DefaultConfiguration();

    conf.setPreservFittestIndividual(true);
    conf.setKeepPopulationSizeConstant(true);
    conf.setFitnessFunction(new CoinChangeFitness(amount));

    // Set up genetic operators
    conf.getGeneticOperators().clear();
    conf.addGeneticOperator(new MutationOperator(conf, 12));
    conf.addGeneticOperator(new CrossoverOperator(conf, 0.35));

    // Calculate dynamic limits based on target amount
    int[] maxCoins = calculateMaxCoinsPerType(amount);

    // Define chromosome structure (6 types of coins)
    Gene[] genes = new Gene[6];
    genes[0] = new IntegerGene(conf, 0, maxCoins[0]); // $1
    genes[1] = new IntegerGene(conf, 0, maxCoins[1]); // 50¢
    genes[2] = new IntegerGene(conf, 0, maxCoins[2]); // 25¢
    genes[3] = new IntegerGene(conf, 0, maxCoins[3]); // 10¢
    genes[4] = new IntegerGene(conf, 0, maxCoins[4]); // 5¢
    genes[5] = new IntegerGene(conf, 0, maxCoins[5]); // 1¢

    conf.setSampleChromosome(new Chromosome(conf, genes));
    conf.setPopulationSize(POPULATION_SIZE);

    // Create initial random population
    Genotype population = createInitialPopulation(conf, amount);

    // Evolve population
    for (int i = 0; i < MAX_EVOLUTIONS; i++) {
      population.evolve();

      // Early stopping if perfect solution is found
      IChromosome fittest = population.getFittestChromosome();
      if (CoinChangeFitness.calculateAmount(fittest) == amount) {
        // Continue for a few more generations to optimize coin count
        for (int j = 0; j < Math.min(100, MAX_EVOLUTIONS - i); j++) {
          population.evolve();
        }
        break;
      }
    }

    var bestChromosome = population.getFittestChromosome();
    return new CoinChangeResult(bestChromosome, population);
  }

  /**
   * Calculates the maximum limits for each coin type based on the target amount.
   * @param amount target amount in cents
   * @return array of maximum coins for each type
   */
  private static int[] calculateMaxCoinsPerType(final int amount) {
    int[] coinValues = {100, 50, 25, 10, 5, 1};
    int[] maxCoins = new int[coinValues.length];

    for (int i = 0; i < coinValues.length; i++) {
      int maxCoin = (int) Math.ceil((amount / (double) coinValues[i]) * SAFETY_MULTIPLIER);
      maxCoins[i] = Math.max(10, maxCoin);
    }

    return maxCoins;
  }

  /**
   * Creates an initial population with some promising solutions.
   * @param conf configuration
   * @param amount target amount in cents
   * @return initial population
   * @throws InvalidConfigurationException if configuration fails
   */
  private static Genotype createInitialPopulation(
      final Configuration conf,
      final int amount
  ) throws InvalidConfigurationException {
    Genotype population = Genotype.randomInitialGenotype(conf);

    // Try to create some greedy solutions as seeds
    try {
      var greedySolutions = generateGreedySolutions(conf, amount);
      var chromosomes = population.getPopulation().getChromosomes();

      // Replace some random chromosomes with greedy solutions
      for (int i = 0; i < Math.min(greedySolutions.length, chromosomes.size() / 4); i++) {
        chromosomes.set(i, greedySolutions[i]);
      }
    } catch (Exception e) {
      // If greedy generation fails, proceed with random population
    }

    return population;
  }

  /**
   * Generates some solutions using a greedy algorithm as starting points.
   * @param conf configuration
   * @param amount target amount in cents
   * @return array of chromosomes representing greedy solutions
   * @throws InvalidConfigurationException if configuration fails
   */
  private static IChromosome[] generateGreedySolutions(
      final Configuration conf, final int amount
  ) throws InvalidConfigurationException {
    int[] coinValues = {100, 50, 25, 10, 5, 1};
    IChromosome[] solutions = new IChromosome[3];  // Generate 3 variant

    for (int variant = 0; variant < solutions.length; variant++) {
      Gene[] genes = new Gene[coinValues.length];
      int remainingAmount = amount;

      // Greedy algorithm with different starting points
      int startIndex = variant % coinValues.length;

      for (int i = 0; i < coinValues.length; i++) {
        int coinIndex = (startIndex + i) % coinValues.length;
        int coinNeeded = remainingAmount / coinValues[coinIndex];
        remainingAmount -= coinNeeded * coinValues[coinIndex];

        genes[coinIndex] = new IntegerGene(conf);
        genes[coinIndex].setAllele(coinNeeded);
      }

      solutions[variant] = new Chromosome(conf, genes);
    }

    return solutions;
  }
}
