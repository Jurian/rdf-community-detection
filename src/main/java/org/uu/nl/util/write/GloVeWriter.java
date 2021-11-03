package org.uu.nl.util.write;

import me.tongfei.progressbar.ProgressBar;
import org.uu.nl.embedding.CoOccurrenceMatrix;
import org.uu.nl.embedding.Optimizer;
import org.uu.nl.embedding.Embedding;
import org.uu.nl.util.config.Configuration;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * Write the embedding to a GloVe type file, of the format:
 *     word1 0.123 0.134 0.532 0.152
 *     word2 0.934 0.412 0.532 0.159
 *     word3 0.334 0.241 0.324 0.188
 *     ...
 *     word9 0.334 0.241 0.324 0.188
 */
public class GloVeWriter extends EmbeddingWriter {

    public GloVeWriter(String fileName, Configuration config) {
        super(fileName,config);
    }

    @Override
    public void write(Embedding embedding, CoOccurrenceMatrix coMatrix, Path outputFolder) throws IOException {
        Files.createDirectories(outputFolder);

        final int vocabSize = coMatrix.nrOfFocusVectors();
        final int dimension = config.getEmbedding().getDim();
        final String[] out = new String[dimension];
        final Iterator<Embedding.EmbeddedEntity> entityIterator = embedding.iterator();

        // Create a tab-separated file
        final String delimiter = "\t";
        final String newLine = "\n";

        try (ProgressBar pb = Configuration.progressBar("Writing to file", vocabSize, "vectors");
             Writer w = new BufferedWriter(new FileWriter(outputFolder.resolve(VECTORS_FILE).toFile()))) {
            writeConfig(w);
            writeLines(entityIterator, out, pb, w);
        }
    }

}
