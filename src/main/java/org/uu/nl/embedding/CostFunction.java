package org.uu.nl.embedding;

public interface CostFunction {
    float innerCost(Optimizer opt, float Xij, int u, int v);
    float weightedCost(Optimizer opt, float innerCost, float Xij);
}
