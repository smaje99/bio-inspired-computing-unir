package com.genetic;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.IntegerGene;

/**
 * Solver for the Coin Change Problem using Genetic Algorithms.
 */
public class CoinChangeSolver {
  private static final int POPULATION_SIZE = 200;
  private static final int MAX_EVOLUTIONS = 2500;

  // Private constructor to prevent instantiation
  private CoinChangeSolver() {
  }

  /**
   * Solves the coin change problem for a given amount using JGAP.
   *
   * @param amount target amount in cents
   * @return the fittest chromosome (best solution)
   * @throws InvalidConfigurationException if configuration fails
   * @throws IllegalArgumentException      if amount is out of valid range
   */
  public static IChromosome solve(int amount) throws InvalidConfigurationException, IllegalArgumentException {
    Configuration.reset();
    Configuration conf = new DefaultConfiguration();

    conf.setPreservFittestIndividual(true);
    conf.setFitnessFunction(new CoinChangeFitness(amount));

    // Define chromosome structure (6 types of coins)
    Gene[] genes = new Gene[6];
    genes[0] = new IntegerGene(conf, 0, amount / 100); // $1
    genes[1] = new IntegerGene(conf, 0, amount / 50); // 50¢
    genes[2] = new IntegerGene(conf, 0, amount / 25); // 25¢
    genes[3] = new IntegerGene(conf, 0, amount / 10); // 10¢
    genes[4] = new IntegerGene(conf, 0, amount / 5); // 5¢
    genes[5] = new IntegerGene(conf, 0, amount); // 1¢

    conf.setSampleChromosome(new Chromosome(conf, genes));
    conf.setPopulationSize(POPULATION_SIZE);

    // Create initial random population
    Genotype population = Genotype.randomInitialGenotype(conf);

    // Evolve population
    for (int i = 0; i < MAX_EVOLUTIONS; i++) {
      population.evolve();
    }

    return population.getFittestChromosome();
  }
}
