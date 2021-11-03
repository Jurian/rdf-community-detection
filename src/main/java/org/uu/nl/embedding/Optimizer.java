package org.uu.nl.embedding;

import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.math3.util.FastMath;
import org.apache.log4j.Logger;
import org.uu.nl.nodecontext.KeyIndex;
import org.uu.nl.util.config.Configuration;
import org.uu.nl.util.rnd.ExtendedRandom;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.concurrent.*;

/**
 * @author Jurian Baas
 */
public abstract class Optimizer implements IOptimizer {

	private final static Logger logger = Logger.getLogger(Optimizer.class);
	private static final ExtendedRandom random = Configuration.getThreadLocalRandom();

	protected final KeyIndex nodeIndex;
	protected final CoOccurrenceMatrix coMatrix;
	protected final int dimension;
	protected final int contextVectors, focusVectors;
	protected final int numThreads;
	protected final int coCount;
	protected final float learningRate = 0.05f;
	protected final float[][] focus, context;
	protected final float[] fBias, cBias;
	protected final int[] linesPerThread;
	protected final CostFunction costFunction;
	private final int maxIterations;
	private final double tolerance;

	protected Optimizer(CoOccurrenceMatrix coMatrix, KeyIndex nodeIndex, Configuration config, CostFunction costFunction) {

		this.costFunction = costFunction;
		this.nodeIndex = nodeIndex;
		this.coMatrix = coMatrix;
		this.maxIterations = config.getEmbedding().getMaxiter();
		this.tolerance = config.getEmbedding().getTolerance();
		this.contextVectors = coMatrix.nrOfContextVectors();
		this.focusVectors = coMatrix.nrOfFocusVectors();
		this.numThreads = config.getInput().getThreads();
		this.coCount = coMatrix.coOccurrenceCount();
		this.dimension = config.getEmbedding().getDim();

		this.focus = new float[focusVectors][dimension];
		this.context = new float[contextVectors][dimension];
		this.fBias = new float[focusVectors];
		this.cBias = new float[contextVectors];

		for (int i = 0; i < focusVectors; i++) {
			fBias[i] = (float) (random.nextFloat() - 0.5) / dimension;

			for (int d = 0; d < dimension; d++) {
				focus[i][d] = (float) (random.nextFloat() - 0.5) / dimension;
			}
		}

		for (int i = 0; i < contextVectors; i++) {
			cBias[i] = (float) (random.nextFloat() - 0.5) / dimension;

			for (int d = 0; d < dimension; d++) {
				context[i][d] = (float) (random.nextFloat() - 0.5) / dimension;
			}
		}

		this.linesPerThread = new int[numThreads];
		for (int i = 0; i < numThreads - 1; i++) {
			linesPerThread[i] = coCount / numThreads;
		}
		linesPerThread[numThreads - 1] = coCount / numThreads + coCount % numThreads;
	}

	@Override
	public Optimum optimize() throws OptimizationFailedException {

		final Optimum opt = new Optimum();
		final ExecutorService es = Executors.newFixedThreadPool(numThreads);
		final CompletionService<Float> completionService = new ExecutorCompletionService<>(es);

		try(ProgressBar pb = Configuration.progressBar(getName(), maxIterations, "epochs")) {

			double prevCost = 0;
			double iterDiff;
			for (int iteration = 0; iteration < maxIterations; iteration++) {

				coMatrix.shuffle();

				for (int id = 0; id < numThreads; id++)
					completionService.submit(createJob(id, iteration));
				
				int received = 0;
				double localCost = 0;

				while(received < numThreads) {
					try {
						localCost += completionService.take().get();
						received++;
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
				}

				if(Double.isNaN(localCost) || Double.isInfinite(localCost)) {
					throw new OptimizationFailedException("Cost infinite or NAN");
				}

				localCost = (localCost / coCount);

				opt.addIntermediaryResult(localCost);
				iterDiff= FastMath.abs(prevCost - localCost);

				pb.step();
				pb.setExtraMessage(formatMessage(iterDiff));
				prevCost = localCost;

				if(iterDiff <= tolerance) {

					opt.setResultIterator(new EmbeddingIterator());
					opt.setFinalCost(localCost);

					break;
				}
			}
			
		} finally {
			es.shutdown();
		}

		return opt;
	}

	private String formatMessage(double iterDiff) {
		return new BigDecimal(iterDiff).stripTrailingZeros().toPlainString();
	}

	/**
	 * Instead of wasting RAM by copying the entire embedding to a new double array,
	 * we can access it as a stream of float arrays with this iterator.
	 */
	class EmbeddingIterator implements Iterator<EmbeddedEntity> {

		private int focusIndex = 0;

		@Override
		public boolean hasNext() {
			return focusIndex < focusVectors;
		}

		@Override
		public EmbeddedEntity next() {

			final EmbeddedEntity entity = new EmbeddedEntity(
					focusIndex,
					nodeIndex.getKey(focusIndex),
					focus[focusIndex]
			);

			focusIndex++;
			return entity;
		}
	}

	/**
	 * View of an embedded entity
	 */
	public static class EmbeddedEntity {

		private final int index;
		private final String key;
		private final float[] vector;

		public EmbeddedEntity(int index, String key, float[] vector) {
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

		public float[] getVector() {
			return vector;
		}
	}

}
