package org.uu.nl.embedding.metatree;

import org.uu.nl.embedding.util.CoOccurrenceMatrix;

import java.util.Map;

public class ContextMatrix implements CoOccurrenceMatrix {

    // Represent our sparse matrix this way for efficient lookups
    public final int[] rows;
    public final int[] cols;
    public final float[] values; // Use floats for lower memory footprint

    public ContextMatrix(int nContextNodes) {
        this.rows = new int[nContextNodes];
        this.cols = new int[nContextNodes];
        this.values = new float[nContextNodes];
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
                i++;
            }
        }
    }

    @Override
    public int contextIndex2Focus(int i) {
        return 0;
    }

    @Override
    public int focusIndex2Context(int i) {
        return 0;
    }

    @Override
    public int nrOfContextVectors() {
        return 0;
    }

    @Override
    public int nrOfFocusVectors() {
        return 0;
    }

    @Override
    public double max() {
        return 0;
    }

    @Override
    public String getKey(int index) {
        return null;
    }

    @Override
    public byte getType(int index) {
        return 0;
    }

    @Override
    public int cIdx_I(int i) {
        return 0;
    }

    @Override
    public int cIdx_J(int j) {
        return 0;
    }

    @Override
    public float cIdx_C(int i) {
        return 0;
    }

    @Override
    public int coOccurrenceCount() {
        return 0;
    }

    @Override
    public void shuffle() {

    }
}
