package org.uu.nl.embedding;

public interface CostFunction {
    double innerCost(Optimizer opt, double Xij, int u, int v);
    double weightedCost(Optimizer opt, double innerCost, double Xij);
}
