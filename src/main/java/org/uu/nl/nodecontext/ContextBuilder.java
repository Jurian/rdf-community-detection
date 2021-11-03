package org.uu.nl.nodecontext;

import me.tongfei.progressbar.ProgressBar;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.uu.nl.util.config.Configuration;
import org.uu.nl.util.parallel.DatasetThreadFactory;
import org.uu.nl.util.read.TDB2Reader;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ContextBuilder {

    final ExecutorService es;
    final CompletionService<ContextVector> completionService;

    public ContextBuilder(DatasetThreadFactory factory) {
        es = Executors.newCachedThreadPool(factory);
        completionService = new ExecutorCompletionService<>(es);
    }

    public ContextMatrix build(MetaTree metaTree, NodeIndex nodeIndex ) {

        try {
            final int nFocusNodes = nodeIndex.nFocusNodes;

            for(Node root : nodeIndex.getFocusNodes().keySet()) {
                completionService.submit(new MetaTreeJob(root, metaTree, nodeIndex ));
            }

            Map<Integer, ContextVector> processedVectors = new HashMap<>();
            int jobsDone = 0;
            int nOccurrences = 0;

            try(ProgressBar pb = Configuration.progressBar("Finding trees", nFocusNodes, "nodes")) {
                while (jobsDone < nFocusNodes) {
                    try {

                        final ContextVector context = completionService.take().get();
                        final ContextVector precomputedContext = processedVectors.get(context.getRoot());

                        if(precomputedContext == null) {
                            nOccurrences += context.size();
                            processedVectors.put(context.getRoot(), context);
                        } else {
                            int sizeBefore = precomputedContext.size();
                            processedVectors.put(context.getRoot(), precomputedContext.merge(context));
                            int sizeAfter = precomputedContext.size();
                            nOccurrences += (sizeAfter - sizeBefore);
                        }

                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        jobsDone++;
                        pb.step();
                    }
                }
            }


            ContextMatrix matrix = new ContextMatrix(nodeIndex, nOccurrences);
            matrix.fill(processedVectors);
            return matrix;

        } finally {
            es.shutdown();
            nodeIndex.clear();
        }
    }
}
