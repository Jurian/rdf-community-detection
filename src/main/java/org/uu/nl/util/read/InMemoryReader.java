package org.uu.nl.util.read;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class InMemoryReader implements Reader<Dataset> {
    @Override
    public Dataset load(File file) throws IOException {
        if(file.isDirectory()) {
            Model model = ModelFactory.createDefaultModel();
            Files.find(file.toPath(),999,(p, bfa) -> bfa.isRegularFile()).forEach(bfa -> {
                model.add(RDFDataMgr.loadModel(bfa.toFile().getAbsolutePath()));
            });
            return DatasetFactory.create(model);
        }
        return DatasetFactory.create(file.getAbsolutePath());
    }
}
