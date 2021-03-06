package org.uu.nl.embedding;

import org.apache.commons.math3.util.FastMath;

public class GloveCost implements CostFunction {

    public double innerCost(Optimizer opt, double Xij, int u, int v) {

        double innerCost = 0;
        for (int d = 0; d < opt.dimension; d++)
            innerCost += opt.focus[u][d] * opt.context[v][d]; // dot product of node and context node vector
        // Add separate bias for each node
        innerCost += opt.fBias[u] + opt.cBias[v] - FastMath.log(Xij);
        return innerCost;
    }

    @Override
    public double weightedCost(Optimizer opt, double innerCost, double Xij) {
        return (Xij > opt.coMatrix.max()) ? innerCost : (float) FastMath.pow(Xij /  opt.coMatrix.max(), 0.75) * innerCost;
    }
}