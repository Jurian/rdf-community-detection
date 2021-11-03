package org.uu.nl.util.config;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.uu.nl.nodecontext.MetaTree;
import org.uu.nl.util.rnd.ExtendedRandom;
import org.uu.nl.util.rnd.ThreadLocalSeededRandom;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class Configuration {

    private Input input;
    private Output output;
    private Embedding embedding;

    public Input getInput(){
        return this.input;
    }

    public void setInput(Input input) {
        this.input = input;
    }

    public Embedding getEmbedding() {
        return embedding;
    }

    public void setEmbedding(Embedding embedding) {
        this.embedding = embedding;
    }

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output output) {
        this.output = output;
    }

    private static ThreadLocalSeededRandom threadLocalRandom;

    public static void setThreadLocalRandom() {
        threadLocalRandom = new ThreadLocalSeededRandom(System.currentTimeMillis());
    }

    public static void setThreadLocalRandom(long seed) {
        threadLocalRandom = new ThreadLocalSeededRandom(seed);
    }

    public static ExtendedRandom getThreadLocalRandom() {
        return threadLocalRandom.get();
    }

    public static ProgressBar progressBar(String name, long max, String unitName) {
        return new ProgressBar (
                name,
                max,
                250,
                System.out,
                ProgressBarStyle.COLORFUL_UNICODE_BLOCK,
                " " + unitName,
                1L,
                false,
                null,
                ChronoUnit.SECONDS,
                0L,
                Duration.ZERO
        );
    }

    public static class Input {

        private String metaTree;
        private int threads;
        private String graph;
        private String focusType;

        public String getFocusType() {
            return focusType;
        }

        public void setFocusType(String focusType) {
            this.focusType = focusType;
        }

        public File getGraphFile() {
            return Paths.get("").toAbsolutePath().resolve(graph).toFile();
        }

        public String getGraph() {
            return graph;
        }

        public void setGraph(String graph) {
            this.graph = graph;
        }

        public int getThreads() {
            return threads == 0 ? (Runtime.getRuntime().availableProcessors() -1) : threads;
        }

        public void setThreads(int threads) {
            this.threads = threads;
        }

        public String getMetaTree() {
            return this.metaTree;
        }

        public void setMetaTree(String metaTree) {
            this.metaTree = metaTree;
        }

    }

    public static class Embedding {

        private int dim;
        private String embeddingMethod;
        private String optimizationMethod;
        private double tolerance;
        private int maxiter;

        public int getDim() {
            return dim;
        }

        public void setDim(int dim) {
            this.dim = dim;
        }

        public OptimizationMethod getOptimizationEnum() {
            return OptimizationMethod.valueOf(optimizationMethod.toUpperCase());
        }

        public String getOptimizationMethod() {
            return optimizationMethod;
        }

        public void setOptimizationMethod(String method) {
            this.optimizationMethod = method;
        }

        public double getTolerance() {
            return tolerance;
        }

        public void setTolerance(double tolerance) {
            this.tolerance = tolerance;
        }

        public int getMaxiter() {
            return maxiter;
        }

        public void setMaxiter(int maxiter) {
            this.maxiter = maxiter;
        }

        public Embedding.EmbeddingMethod getEmbeddingMethodEnum() {
            return EmbeddingMethod.valueOf(this.embeddingMethod.toUpperCase());
        }

        public String getEmbeddingMethod() {
            return  embeddingMethod;
        }

        public void setEmbeddingMethod(String embeddingMethod) {
            this.embeddingMethod = embeddingMethod;
        }

        public enum EmbeddingMethod {
            GLOVE, PGLOVE
        }

        public enum OptimizationMethod {
            ADAGRAD, AMSGRAD, ADAM
        }
    }

    public static class Output {

        private String writer;
        private String name;

        public void setWriter(String writer) {
            this.writer = writer;
        }
        public String getWriter() {return this.writer;}
        public EmbeddingWriter getWriterEnum() {
            return EmbeddingWriter.valueOf(writer.toUpperCase());
        }

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }

        public enum EmbeddingWriter {
            GLOVE, WORD2VEC, SPLIT
        }
    }

    public static void check(Configuration config) throws InvalidConfigException {
        boolean hasDim = config.embedding.dim > 0;
        boolean hasGraph = config.getInput().graph != null && !config.getInput().graph.isEmpty();
        boolean hasMethod = config.getEmbedding().getEmbeddingMethod() != null && !config.getEmbedding().getEmbeddingMethod().isEmpty();
        //boolean hasOut = config.output != null && config.output.getType() != null && config.output.getType().size() != 0;

        if(!hasDim) throw new InvalidConfigException("No dimension specified");
        if(!hasGraph) throw new InvalidConfigException("No input graph specified");
        if(!hasMethod) throw new InvalidConfigException("Invalid method, choose one of: glove, pglove");
        //if(!hasOut) throw new InvalidConfigException("Invalid output parameters, specify at least one type");
    }
}
