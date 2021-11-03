package org.uu.nl.embedding;

/**
 * @author Jurian Baas
 */
public interface IOptimizer {
	Embedding optimize() throws OptimizationFailedException;
	String getName();
	OptimizeJob createJob(int id, int iteration);
}
