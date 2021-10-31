package org.uu.nl.embedding.util.read;

import org.apache.jena.query.Dataset;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * @author Jurian Baas
 */
public class JenaReader implements Reader<Dataset> {

    private final static Logger logger = Logger.getLogger(JenaReader.class);

    public Dataset load(File directory) throws IOException {
        return TDB2Factory.connectDataset(directory.getAbsolutePath());
    }

}
