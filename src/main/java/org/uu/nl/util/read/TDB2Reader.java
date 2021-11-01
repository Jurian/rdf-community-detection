package org.uu.nl.util.read;

import org.apache.jena.query.Dataset;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * @author Jurian Baas
 */
public class TDB2Reader implements Reader<Dataset> {

    private final static Logger logger = Logger.getLogger(TDB2Reader.class);

    public Dataset load(File directory){
        if(!directory.isDirectory()) {
            throw new IllegalArgumentException("Please specify a directory");
        }
        return TDB2Factory.connectDataset(directory.getAbsolutePath());
    }

}
