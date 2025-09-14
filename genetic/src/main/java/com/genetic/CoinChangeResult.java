package com.genetic;

import org.jgap.Genotype;
import org.jgap.IChromosome;

/**
 * Holds the result of the Coin Change solver:
 * - The best chromosome (solution).
 * - The final population after evolution.
 */
public record CoinChangeResult(IChromosome bestChromosome, Genotype population) {
}
