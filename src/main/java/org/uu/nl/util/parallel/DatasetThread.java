package org.uu.nl.util.parallel;

import org.apache.jena.query.Dataset;

public class DatasetThread extends Thread {

    private final Dataset dataset;

    public DatasetThread(Dataset dataset, Runnable r) {
        super(r);
        this.dataset = dataset;
    }

    public Dataset getDataset() {
        return dataset;
    }
}
