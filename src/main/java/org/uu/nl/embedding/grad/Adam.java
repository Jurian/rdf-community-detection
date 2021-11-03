package org.uu.nl.embedding.grad;

import org.apache.commons.math3.util.FastMath;
import org.uu.nl.embedding.CoOccurrenceMatrix;
import org.uu.nl.embedding.CostFunction;
import org.uu.nl.embedding.OptimizeJob;
import org.uu.nl.embedding.Optimizer;
import org.uu.nl.nodecontext.KeyIndex;
import org.uu.nl.util.config.Configuration;

/**
 * <p>
 * Adaptive Moment Estimation (Adam) is another method that computes adaptive
 * learning rates for each parameter. In addition to storing an exponentially
 * decaying average of past squared gradients vt like Adadelta and RMSprop, Adam
 * also keeps an exponentially decaying average of past gradients mt, similar to
 * momentum.
 * </p>
 * <p>
 * Whereas momentum can be seen as a ball running down a slope, Adam behaves
 * like a heavy ball with friction, which thus prefers flat minima in the error
 * surface.
 * </p>
 *
 * @see <a href="https://arxiv.org/pdf/1412.6980.pdf">Adam paper</a>
 * @see <a href="http://nlp.stanford.edu/projects/glove/">Stanford GloVe page</a>
 * @author Jurian Baas
 */
@SuppressWarnings("DuplicatedCode")
public class Adam extends Optimizer {

	/**
	 * Contains the decaying averages of the past first moments w.r.t. to all parameters
	 */
	private final double[][] M1focus;
    private final double[][] M1context;
	private final double[] M1fBias;
    private final double[] M1cBias;
	/**
	 * Contains the decaying averages of the past second moments w.r.t. to all parameters
	 */
	private final double[][] M2focus;
    private final double[][] M2context;
	private final double[] M2fBias;
    private final double[] M2cBias;

	/**
	 * Decay rate for first momentum
	 */
	private final double beta1 = 0.9f;
	/**
	 * Decay rate for second momentum
	 */
	private final double beta2 = 0.999f;
	/**
	 * Mainly used to prevent divisions by zero, in some cases setting this to 0.1 or 1 can help improve stability
	 */
	private final double epsilon = 1e-1f;
	
	public Adam(CoOccurrenceMatrix coMatrix, KeyIndex keyIndex, Configuration config, CostFunction costFunction) {
		super(coMatrix, keyIndex, config, costFunction);

		this.M1focus = new double[focusVectors][dimension];
		this.M2focus = new double[focusVectors][dimension];
		this.M1context = new double[contextVectors][dimension];
		this.M2context = new double[contextVectors][dimension];
		this.M1fBias = new double[focusVectors];
		this.M2fBias = new double[focusVectors];
		this.M1cBias = new double[contextVectors];
		this.M2cBias = new double[contextVectors];

	}
	
	@Override
	public String getName() {
		return "Adam";
	}

	@Override
	public OptimizeJob createJob(int id, int iteration) {
		return () -> {

			int i, d, i_u, i_v, bu, bv;
			double Xij;
            double m;
            double v;
            double grad_u;
            double grad_v;
			double cost = 0;
            double innerCost;
            double weightedCost;
            final int offset = coCount / numThreads * id;

			// From the paper, a slight improvement of efficiency can be obtained this way
			final double correction = learningRate * FastMath.sqrt(1 - FastMath.pow(beta2, iteration + 1)) / (1 - FastMath.pow(beta1, iteration + 1));

			for (i = 0; i < linesPerThread[id]; i++) {

				i_u = coMatrix.cIdx_I(i + offset); // Index of focus bias
				i_v = coMatrix.cIdx_J(i + offset); // Index of context bias
				//i_u = bu * dimension; // Index of focus vector
				//i_v = bv * dimension; // Index of bias vector
				Xij = coMatrix.cIdx_C(i + offset); // Co-occurrence

				/* Calculate cost, save diff for gradients */
				innerCost = costFunction.innerCost(this, Xij, i_u, i_v);
				weightedCost = costFunction.weightedCost(this, innerCost, Xij);
				cost += 0.5 * weightedCost * innerCost; // weighted squared error

				/*---------------------------
				 * Adaptive gradient updates *
				 ---------------------------*/

				// Update the moments for the word vectors
				for (d = 0; d < dimension; d++) {

					//d1 = d + i_u; // Index of specific dimension in focus vector
					//d2 = d + i_v; // Index of specific dimension in context vector

					// Compute gradients
					grad_u = weightedCost * context[i_v][d];
					grad_v = weightedCost * focus[i_u][d];

					// Update biased first and second moment estimates
					m = beta1 * M1focus[i_u][d] + (1 - beta1) * grad_u;
					v = beta2 * M2focus[i_u][d] + (1 - beta2) * (grad_u * grad_u);
					focus[i_u][d] -= correction * m / (FastMath.sqrt(v) + epsilon);
					M1focus[i_u][d] = m;
					M2focus[i_u][d] = v;


					m = beta1 * M1context[i_v][d] + (1 - beta1) * grad_v;
					v = beta2 * M2context[i_v][d] + (1 - beta2) * (grad_v * grad_v);
					context[i_v][d] -= correction * m / (FastMath.sqrt(v) + epsilon);
					M1context[i_v][d] = m;
					M2context[i_v][d] = v;
				}

				/*---------------------
				 * Compute for biases *
				 ---------------------*/

				// Update the first, second moment for the biases
				m = beta1 * M1fBias[i_u] + (1 - beta1) * weightedCost;
				v = beta2 * M2fBias[i_u] + (1 - beta2) * (weightedCost * weightedCost);
				fBias[i_u] -= correction * m / (FastMath.sqrt(v) + epsilon);
				M1fBias[i_u] = m;
				M2fBias[i_u] = v;

				m = beta1 * M1cBias[i_v] + (1 - beta1) * weightedCost;
				v = beta2 * M2cBias[i_v] + (1 - beta2) * (weightedCost * weightedCost);
				cBias[i_v] -= correction * m / (FastMath.sqrt(v) + epsilon);
				M1cBias[i_v] = m;
				M2cBias[i_v] = v;
			}
			return cost;
		};
	}
}
