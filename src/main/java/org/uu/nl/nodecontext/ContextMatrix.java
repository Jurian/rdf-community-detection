package org.uu.nl.nodecontext;

import org.apache.commons.math3.util.FastMath;
import org.uu.nl.embedding.CoOccurrenceMatrix;
import org.uu.nl.util.rnd.Permutation;

import java.util.Map;

public class ContextMatrix implements CoOccurrenceMatrix {

    // Represent our sparse matrix this way for efficient lookups
    public final int[] rows;
    public final int[] cols;
    public final float[] values; // Use floats for lower memory footprint
    private float max;
    private final int focusVectors, contextVectors;
    private final int coOccurrenceCount;
    private final Permutation permutation;


    public ContextMatrix(NodeIndex nodeIndex, int nOccurrences) {
        this.rows = new int[nOccurrences];
        this.cols = new int[nOccurrences];
        this.values = new float[nOccurrences];
        this.coOccurrenceCount = nOccurrences;
        this.permutation = new Permutation(coOccurrenceCount);
        this.focusVectors = nodeIndex.nFocusNodes;
        this.contextVectors = nodeIndex.nContextNodes;
    }

    public void fill(Map<Integer, ContextVector> vectors) {
        int i = 0;
        for(Map.Entry<Integer, ContextVector> vectorEntry : vectors.entrySet()) {

            final int root = vectorEntry.getKey();
            for(Map.Entry<Integer, Float> contextEntry : vectorEntry.getValue().entrySet()) {

                final int col = contextEntry.getKey();
                final float val = contextEntry.getValue();

                rows[i] = root;
                cols[i] = col;
                values[i] = val;
                max = FastMath.max(val, max);
                i++;
            }
        }
    }

    @Override
    public void shuffle() {
        permutation.shuffle();
    }

    public int cIdx_I(int x) {
        return rows[permutation.randomAccess(x)];
    }

    public int cIdx_J(int x) {
        return this.cols[permutation.randomAccess(x)];
    }

    public float cIdx_C(int x) {
        return this.values[permutation.randomAccess(x)];
    }

    public int coOccurrenceCount() {
        return this.coOccurrenceCount;
    }

    @Override
    public int nrOfContextVectors() {
        return contextVectors;
    }

    @Override
    public int nrOfFocusVectors() {
        return focusVectors;
    }

    @Override
    public double max() {
        return this.max;
    }

}
