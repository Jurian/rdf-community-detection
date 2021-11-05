package org.uu.nl.embedding;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.uu.nl.nodecontext.KeyIndex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Jurian Baas
 */
public class Embedding implements Iterable<Embedding.EmbeddedEntity>{

	private final KeyIndex keys;
	private final double[][] vectors;
	private final int dimension;
	private final int nPoints;
	private final List<Double> costHistory = new ArrayList<>();

	public Embedding(Optimizer optimizer) {
		this.dimension = optimizer.dimension;
		this.nPoints = optimizer.focusVectors;
		this.vectors = optimizer.focus;
		this.keys = optimizer.nodeIndex;
	}

	public int getDimension() {
		return dimension;
	}

	public int getNumberOfPoints() {
		return nPoints;
	}

	public double[][] getVectors() {
		return vectors;
	}

	public KeyIndex getKeyIndex() {
		return this.keys;
	}

	public void addIntermediaryResult(double result) {
		costHistory.add(result);
	}

	public List<Double> getCostHistory() {
		return costHistory;
	}

	@Override
	public Iterator<EmbeddedEntity> iterator() {
		return new EmbeddingIterator();
	}

	/**
	 * Instead of wasting RAM by copying the entire embedding to a new double array,
	 * we can access it as a stream of double arrays with this iterator.
	 */
	class EmbeddingIterator implements Iterator<EmbeddedEntity> {

		private int focusIndex = 0;

		@Override
		public boolean hasNext() {
			return focusIndex < nPoints;
		}

		@Override
		public EmbeddedEntity next() {

			final EmbeddedEntity entity = new EmbeddedEntity(
					focusIndex,
					keys.getKey(focusIndex),
					vectors[focusIndex]
			);

			focusIndex++;
			return entity;
		}
	}

	/**
	 * View of an embedded entity
	 */
	public static class EmbeddedEntity implements Clusterable {

		private final int index;
		private final String key;
		private final double[] vector;

		public EmbeddedEntity(int index, String key, double[] vector) {
			this.index = index;
			this.key = key;
			this.vector = vector;
		}

		public int getIndex() {
			return index;
		}

		public String getKey() {
			return key;
		}

		@Override
		public double[] getPoint() {
			return vector;
		}
	}
}
