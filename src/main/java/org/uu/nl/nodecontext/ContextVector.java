package org.uu.nl.nodecontext;

import java.util.HashMap;

public class ContextVector extends HashMap<Integer, Float> {

    private final int root;

    public ContextVector(int root) {
        this.root = root;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ContextVector vector = (ContextVector) o;
        return root == vector.root;
    }

    public ContextVector normalize(float nTrees) {
        remove(root);
        for(Entry<Integer, Float> entry : entrySet()) {
            entry.setValue(entry.getValue() / nTrees);
        }
        return this;
    }

    @Override
    public int hashCode() {
        return this.root;
    }

    public void addNode(int node) {
        compute(node, (k,v)-> (v == null) ? 1f : v + 1f);
    }

    public ContextVector merge(ContextVector other) {
        other.forEach((key, value) -> this.merge(key, value, Float::sum));

        return this;
    }

    public int getRoot(){ return this.root; }
}
