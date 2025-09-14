package com.genetic;

import java.util.Random;

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

  // Diversity check parameters
  // These parameters control the diversity maintenance strategy
  private static final int DIVERSITY_CHECK_INTERVAL = 50;
  private static final double DIVERSITY_THRESHOLD = 0.1;
  private static final Random RANDOM = new Random();

  // Private constructor to prevent instantiation
  private CoinChangeSolver() {
  }

  /**
   * Solves the coin change problem for a given amount using JGAP.
   *
   * @param amount target amount in cents
   * @return result containing the fittest chromosome (best solution) and
   *         population
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
    conf.addGeneticOperator(new MutationOperator(conf, 12)); // 12% mutation rate
    conf.addGeneticOperator(new CrossoverOperator(conf, 0.35)); // 35% crossover rate

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

    // Evolve population with diversity management
    double previousDiversity = -1.0;
    int stagnationCount = 0;

    for (int i = 0; i < MAX_EVOLUTIONS; i++) {
      population.evolve();

      var currentBest = population.getFittestChromosome();
      double currentBestFitness = currentBest.getFitnessValue();

      if (Math.abs(currentBestFitness - previousDiversity) < 0.001) {
        stagnationCount++;
      } else {
        stagnationCount = 0;
      }
      previousDiversity = currentBestFitness;

      // Inject diversity if population becomes too homogeneous
      if (i % DIVERSITY_CHECK_INTERVAL == 0) {
        double diversity = calculatePopulationDiversity(population);
        if (diversity < DIVERSITY_THRESHOLD || stagnationCount > 30) {
          injectDiversity(population, conf, amount, 0.3);
          stagnationCount = 0; // Reset stagnation count after injection
        }
      }

      // Early stopping if perfect solution is found
      if (CoinChangeFitness.calculateAmount(currentBest) == amount) {
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
    int[] coinValues = { 100, 50, 25, 10, 5, 1 };
    int[] maxCoins = new int[coinValues.length];

    for (int i = 0; i < coinValues.length; i++) {
      int maxCoin = (int) Math.ceil((amount / (double) coinValues[i]) * SAFETY_MULTIPLIER);
      maxCoins[i] = Math.max(10, maxCoin);
    }

    return maxCoins;
  }

  /**
   * Creates an initial population with some promising solutions.
   * @param conf   configuration
   * @param amount target amount in cents
   * @return initial population
   * @throws InvalidConfigurationException if configuration fails
   */
  private static Genotype createInitialPopulation(
      final Configuration conf,
      final int amount) throws InvalidConfigurationException {
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
   *
   * @param conf   configuration
   * @param amount target amount in cents
   * @return array of chromosomes representing greedy solutions
   * @throws InvalidConfigurationException if configuration fails
   */
  private static IChromosome[] generateGreedySolutions(
      final Configuration conf, final int amount) throws InvalidConfigurationException {
    int[] coinValues = { 100, 50, 25, 10, 5, 1 };
    IChromosome[] solutions = new IChromosome[3]; // Generate 3 variant

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

  /**
   * Calculates the diversity of the population based on the fitness variance.
   */
  private static double calculatePopulationDiversity(final Genotype population) {
    var chromosomes = population.getPopulation().getChromosomes();
    if (chromosomes.size() < 2) {
      return 0.0;
    }

    double mean = 0.0;
    for (Object chromo : chromosomes) {
      mean += ((IChromosome) chromo).getFitnessValue();
    }
    mean /= chromosomes.size();

    double variance = 0.0;
    for (Object chromo : chromosomes) {
      double fitness = ((IChromosome) chromo).getFitnessValue();
      variance += Math.pow(fitness - mean, 2);
    }
    variance /= chromosomes.size();

    // Normalize diversity to [0,1] range
    return Math.sqrt(variance) / (mean + 1.0);
  }

  /**
   * Injects diversity into the population by replacing a fraction of the worst
   * chromosomes.
   */
  private static void injectDiversity(
      final Genotype population,
      final Configuration conf,
      final int targetAmount,
      final double replacementRate) throws InvalidConfigurationException {
    var chromosomes = population.getPopulation().getChromosomes();
    int numToReplace = (int) (chromosomes.size() * replacementRate);

    // Maintain the best chromosomes
    chromosomes.sort((a, b) -> Double.compare(
        ((IChromosome) b).getFitnessValue(),
        ((IChromosome) a).getFitnessValue()));

    // Replace the worst chromosomes with diverse ones
    for (int i = chromosomes.size() - numToReplace; i < chromosomes.size(); i++) {
      var newChromo = createDiverseChromosome(conf, targetAmount);
      chromosomes.set(i, newChromo);
    }
  }

  /**
   * Creates a diverse chromosome using different strategies.
   */
  private static IChromosome createDiverseChromosome(
      Configuration conf, int amount) throws InvalidConfigurationException {
    Gene[] genes = new Gene[6];
    int[] coinValues = { 100, 50, 25, 10, 5, 1 };

    // Use different strategies to create diversity
    int strategy = RANDOM.nextInt(4);

    switch (strategy) {
      case 0 -> purelyRandomStrategy(genes, conf, amount, coinValues);
      case 1 -> modifiedGreedyStrategy(genes, conf, amount, coinValues);
      case 2 -> smallerCoinsStrategy(genes, conf, amount, coinValues);
      case 3 -> largerCoinsStrategy(genes, conf, amount, coinValues);
      default -> throw new IllegalStateException("Unexpected strategy: " + strategy);
    }

    return new Chromosome(conf, genes);
  }

  /**
   * Purely random strategy for chromosome creation.
   */
  private static void purelyRandomStrategy(
      final Gene[] genes, final Configuration conf, final int amount, final int[] coinValues)
      throws InvalidConfigurationException {
    for (int i = 0; i < genes.length; i++) {
      genes[i] = new IntegerGene(conf);
      int maxCoins = (int) Math.ceil(amount / (double) coinValues[i] * 1.2);
      genes[i].setAllele(RANDOM.nextInt(Math.max(1, maxCoins)));
    }
  }

  /**
   * Modified greedy strategy for chromosome creation.
   */
  private static void modifiedGreedyStrategy(
      final Gene[] genes, final Configuration conf, final int amount, final int[] coinValues)
      throws InvalidConfigurationException {
    int remaining = amount;
    for (int i = 0; i < genes.length; i++) {
      genes[i] = new IntegerGene(conf);
      if (remaining > 0 && coinValues[i] <= remaining) {
        int coinNeeded = remaining / coinValues[i];
        // Add some randomness
        coinNeeded += RANDOM.nextInt(coinNeeded + 3) - 1; // -1 to +2 variation
        coinNeeded = Math.max(0, coinNeeded);
        genes[i].setAllele(coinNeeded);
        remaining -= coinNeeded * coinValues[i];
      } else {
        genes[i].setAllele(0);
      }
    }
  }

  /**
   * Strategy focusing on smaller coins for chromosome creation.
   */
  private static void smallerCoinsStrategy(
      final Gene[] genes, final Configuration conf, final int amount, final int[] coinValues)
      throws InvalidConfigurationException {
    for (int i = 0; i < genes.length; i++) {
      genes[i] = new IntegerGene(conf);
      if (coinValues[i] >= 3) { // Focus on smaller coins (10¢ and below)
        int maxCoins = (int) Math.ceil(amount / (double) coinValues[i] * 0.8);
        genes[i].setAllele(RANDOM.nextInt(Math.max(1, maxCoins)));
      } else {
        genes[i].setAllele(RANDOM.nextInt(5));
      }
    }
  }

  /**
   * Strategy focusing on larger coins for chromosome creation.
   */
  private static void largerCoinsStrategy(
      final Gene[] genes, final Configuration conf, final int amount, final int[] coinValues)
      throws InvalidConfigurationException {
    for (int i = 0; i < genes.length; i++) {
      genes[i] = new IntegerGene(conf);
      if (i <= 2) { // Focus on larger coins ($1, 50¢, 25¢)
        int maxCoins = (int) Math.ceil(amount / (double) coinValues[i] * 0.9);
        genes[i].setAllele(RANDOM.nextInt(Math.max(1, maxCoins)));
      } else {
        genes[i].setAllele(RANDOM.nextInt(3));
      }
    }
  }
}
