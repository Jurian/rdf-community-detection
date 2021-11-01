package org.uu.nl;

import org.apache.jena.query.Dataset;
import org.apache.log4j.Logger;
import org.uu.nl.embedding.*;
import org.uu.nl.embedding.grad.AMSGrad;
import org.uu.nl.embedding.grad.Adagrad;
import org.uu.nl.embedding.grad.Adam;
import org.uu.nl.nodecontext.ContextBuilder;
import org.uu.nl.nodecontext.ContextMatrix;
import org.uu.nl.nodecontext.KeyIndex;
import org.uu.nl.nodecontext.NodeIndex;
import org.uu.nl.util.config.Configuration;
import org.uu.nl.util.config.InvalidConfigException;
import org.uu.nl.util.read.ConfigReader;
import org.uu.nl.util.read.TDB2Reader;
import org.uu.nl.util.write.EmbeddingWriter;
import org.uu.nl.util.write.GloVeWriter;
import org.uu.nl.util.write.SplitFileWriter;
import org.uu.nl.util.write.Word2VecWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author Jurian Baas
 */
public class Main {

    private final static Logger logger = Logger.getLogger("Graph Embeddings");

    private static void runProgram(Configuration config) throws IOException {

        logger.info("Starting the embedding creation process with following settings:");
        logger.info("Graph File: " + config.getInput().getGraph());
        logger.info("Embedding dimensions: " + config.getEmbedding().getDim());
        logger.info("Threads: " + config.getInput().getThreads());
        logger.info("Gradient Descent Algorithm: " + config.getEmbedding().getOptimizationMethod());
        logger.info(config.getEmbedding().getEmbeddingMethod() + " Tolerance: " + config.getEmbedding().getTolerance());
        logger.info(config.getEmbedding().getEmbeddingMethod() + " Maximum Iterations: " + config.getEmbedding().getMaxiter());

        String outFileName = config.getOutput().getName();
        if(outFileName == null || outFileName.isEmpty()) {
            outFileName = createFileName(config);
        }
        logger.info("Writing files with prefix: " + outFileName);

        Configuration.setThreadLocalRandom();

        final TDB2Reader loader = new TDB2Reader();

        // Create an index of all nodes in the graph
        // Additionally, we force focus nodes te be in the front of the index
        final Dataset dataset = loader.load(config.getInput().getGraphFile());
        final NodeIndex nodeIndex = new NodeIndex(config.getInput().getFocusType(), dataset);

        final ContextMatrix matrix = new ContextBuilder(config.getInput().getThreads())
                .build(dataset, config.getInput().getMetaTrees(), nodeIndex);

        final IOptimizer optimizer = createOptimizer(config, matrix, nodeIndex);

        final Optimum optimum = optimizer.optimize();

        final EmbeddingWriter writer = getWriter(outFileName, config);
        writer.write(optimum, matrix, Paths.get("").toAbsolutePath().resolve("out"));
    }

    private static EmbeddingWriter getWriter(String outFileName, Configuration config) {
        switch(config.getOutput().getWriterEnum()) {
            case GLOVE: return new GloVeWriter(outFileName, config);
            case WORD2VEC: return new Word2VecWriter(outFileName, config);
            default:
            case SPLIT: return new SplitFileWriter(outFileName, config);
        }
    }

    private static String createFileName(Configuration config) {
        String outFileName = config.getInput().getGraphFile().getName().toLowerCase();
        if(outFileName.contains(".")) {
            outFileName = outFileName.substring(0, outFileName.lastIndexOf("."));
        }
        outFileName += "_" + config.getEmbedding().getEmbeddingMethod().toLowerCase();

        outFileName += "_" + config.getEmbedding().getOptimizationMethod();
        outFileName += "_" + config.getEmbedding().getDim();

        return outFileName;
    }

    private static IOptimizer createOptimizer(final Configuration config, final CoOccurrenceMatrix coMatrix, final KeyIndex keyIndex) {

        CostFunction cf;
        switch (config.getEmbedding().getEmbeddingMethodEnum()) {
            default:
                throw new IllegalArgumentException("Invalid cost function");
            case GLOVE:
                cf = new GloveCost();
                break;
            case PGLOVE:
                cf = new PGloveCost();
                break;
        }

        switch(config.getEmbedding().getOptimizationEnum()) {
            default:
                throw new IllegalArgumentException("Invalid optimization method");
            case ADAGRAD:
                return new Adagrad(coMatrix, keyIndex, config, cf);
            case ADAM:
                return new Adam(coMatrix, keyIndex, config, cf);
            case AMSGRAD:
                return new AMSGrad(coMatrix, keyIndex, config, cf);
        }
    }

    public static void main(String[] args) {

        for(int i = 0; i < args.length; i++) {
            if(args[i].equals("-c")) {
                if(i + 1 < args.length) {
                    try {

                        final File configFile = Paths.get("").toAbsolutePath().resolve( args[i + 1]).toFile();

                        if(!configFile.exists() || !configFile.isFile()) {
                            logger.error("Cannot find configuration file + " + configFile.getPath());
                            System.exit(1);
                        } else {
                            Configuration config = new ConfigReader().load(configFile);
                            Configuration.check(config);
                            runProgram(config);
                        }
                    } catch (IOException | InvalidConfigException e) {
                        logger.error(e.getMessage(), e);
                        System.exit(1);
                    }
                } else {
                    logger.error("No configuration file specified, exiting...");
                    System.exit(1);
                }
            }
        }
    }
}
