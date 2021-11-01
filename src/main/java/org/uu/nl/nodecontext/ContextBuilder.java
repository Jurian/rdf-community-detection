package org.uu.nl.nodecontext;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ContextBuilder {

    final ExecutorService es;
    final CompletionService<ContextVector> completionService;

    public ContextBuilder(int nThreads) {
        es = Executors.newWorkStealingPool(nThreads);
        completionService = new ExecutorCompletionService<>(es);
    }

    public ContextMatrix build(Dataset dataset, List<MetaTree> metaTreeList, NodeIndex nodeIndex ) {

        try {
            final int nFocusNodes = nodeIndex.nFocusNodes;
            final int totalJobs = metaTreeList.size() * nFocusNodes;
            for(MetaTree metaTree : metaTreeList) {
                for(Node root : nodeIndex.getFocusNodes().keySet()) {
                    completionService.submit(new MetaTreeJob(root, dataset, metaTree, nodeIndex ));
                }
            }

            Map<Integer, ContextVector> processedVectors = new HashMap<>();
            int jobsDone = 0;
            int nOccurrences = 0;
            while (jobsDone < totalJobs) {
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
                }
            }

            ContextMatrix matrix = new ContextMatrix(nodeIndex, nOccurrences);
            matrix.fill(processedVectors);
            return matrix;

        } finally {
            nodeIndex.clear();
            dataset.close();
        }
    }
}
