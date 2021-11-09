package org.uu.nl.util.parallel;

import org.apache.jena.query.Dataset;
import org.uu.nl.util.read.Reader;
import org.uu.nl.util.read.TDB2Reader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

public class DatasetThreadFactory implements ThreadFactory {

    private final List<Dataset> openDatasets;
    private final Reader<Dataset> reader;
    private final File datasetLocation;

    public DatasetThreadFactory(Reader<Dataset> reader, File datasetLocation) {
        this.reader = reader;
        this.datasetLocation = datasetLocation;
        this.openDatasets = new ArrayList<>();
    }

    @Override
    public Thread newThread(Runnable r) {
        Dataset dataset = null;
        try {
            dataset = reader.load(datasetLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }
        openDatasets.add(dataset);
        return new DatasetThread(dataset, r);
    }

    public void closeAllDatasets() {
        for(Dataset dataset : openDatasets) {
            dataset.close();
        }
    }
}
