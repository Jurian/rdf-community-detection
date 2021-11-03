package org.uu.nl.embedding;

import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.math3.util.FastMath;
import org.apache.log4j.Logger;
import org.uu.nl.nodecontext.KeyIndex;
import org.uu.nl.util.config.Configuration;
import org.uu.nl.util.rnd.ExtendedRandom;

import java.math.BigDecimal;
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
	protected final double[][] focus;
	protected final double[][] context;
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

		this.focus = new double[focusVectors][dimension];
		this.context = new double[contextVectors][dimension];
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
	public Embedding optimize() throws OptimizationFailedException {

		final Embedding opt = new Embedding(this);
		final ExecutorService es = Executors.newFixedThreadPool(numThreads);
		final CompletionService<Double> completionService = new ExecutorCompletionService<>(es);

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





}
