package org.uu.nl.util.write;

import me.tongfei.progressbar.ProgressBar;
import org.uu.nl.embedding.CoOccurrenceMatrix;
import org.uu.nl.embedding.Optimizer;
import org.uu.nl.embedding.Embedding;
import org.uu.nl.util.config.Configuration;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * @author Jurian Baas
 */
public abstract class EmbeddingWriter {

	public static final String FILETYPE = ".tsv";
	protected final String VECTORS_FILE;
	protected final Configuration config;

	public EmbeddingWriter(String fileName, Configuration config){
		this.VECTORS_FILE = fileName + FILETYPE;
		this.config = config;
	}

	protected void writeConfig(Writer writer) throws IOException {

		writer.write("Starting the embedding creation process with following settings:");
		writer.write("Graph File: " + config.getInput().getGraph());
		writer.write("Embedding dimensions: " + config.getEmbedding().getDim());
		writer.write("Threads: " + config.getInput().getThreads());
		writer.write("Gradient Descent Algorithm: " + config.getEmbedding().getOptimizationMethod());
		writer.write(config.getEmbedding().getEmbeddingMethod() + " Tolerance: " + config.getEmbedding().getTolerance());
		writer.write(config.getEmbedding().getEmbeddingMethod() + " Maximum Iterations: " + config.getEmbedding().getMaxiter());
	}

	public abstract void write(Embedding embedding, CoOccurrenceMatrix coMatrix, Path outputFolder) throws IOException;

	protected void writeLines(Iterator<Embedding.EmbeddedEntity> entityIterator, String[] out, ProgressBar pb, Writer w) throws IOException {
		while(entityIterator.hasNext()) {
			Embedding.EmbeddedEntity entity = entityIterator.next();

			for (int d = 0; d < out.length; d++)
				out[d] = String.format("%11.6E", entity.getPoint()[d]);

			w.write(entity.getKey()
					.replace("\n", "")
					.replace("\r", "")
					.replace("	", "")
					+ "	"
					+ String.join("	", out) + "\n"
			);
			pb.step();
		}
	}
}
