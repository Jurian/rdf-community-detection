package org.uu.nl.util.parallel;

import org.apache.jena.query.Dataset;
import org.uu.nl.util.read.TDB2Reader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

public class DatasetThreadFactory implements ThreadFactory {

    private final List<Dataset> openDatasets;
    private final TDB2Reader reader;
    private final File datasetLocation;

    public DatasetThreadFactory(TDB2Reader reader, File datasetLocation) {
        this.reader = reader;
        this.datasetLocation = datasetLocation;
        this.openDatasets = new ArrayList<>();
    }

    @Override
    public Thread newThread(Runnable r) {
        Dataset dataset = reader.load(datasetLocation);
        openDatasets.add(dataset);
        return new DatasetThread(dataset, r);
    }

    public void closeAllDatasets() {
        for(Dataset dataset : openDatasets) {
            dataset.close();
        }
    }
}
